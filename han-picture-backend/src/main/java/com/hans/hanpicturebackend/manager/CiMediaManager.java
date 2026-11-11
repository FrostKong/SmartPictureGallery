package com.hans.hanpicturebackend.manager;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.hans.hanpicturebackend.config.CosClientConfig;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.model.dto.file.VideoMediaInfo;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.ciModel.common.MediaInputObject;
import com.qcloud.cos.model.ciModel.common.MediaOutputObject;
import com.qcloud.cos.model.ciModel.job.MediaJobObject;
import com.qcloud.cos.model.ciModel.job.MediaJobOperation;
import com.qcloud.cos.model.ciModel.job.MediaJobResponse;
import com.qcloud.cos.model.ciModel.job.MediaJobsRequest;
import com.qcloud.cos.model.ciModel.snapshot.CosSnapshotRequest;
import com.qcloud.cos.model.ciModel.template.MediaSmartCoverObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.InputStream;

/**
 * 数据万象（CI）「视频」能力门面。
 *
 * 这一层只跟 COS / CI 打交道：入参是对象 key，出参是元信息、图片字节或任务 id，
 * 不碰 video 表、不做归属校验、不拼对外可访问 url、不决定失败要不要降级。
 * 业务编排（谁可以设封面、写哪张表、失败怎么处理）在 VideoServiceImpl。
 *
 * 三个能力都是同步接口，没有 jobId / 回调 / 轮询：
 * 1. 元信息 avinfo（460/124610，GET ?ci-process=avinfo&magicvar=1，返回 JSON）。
 *    不用 460/49284 的 videoinfo（返回 XML，对应 SDK generateMediainfo）：avinfo 加 magicvar=1
 *    会直接把视频流分到 video 节点，不必遍历 streams，也避开 SDK MediaInfoResponse 的解析不确定性。
 * 2. 截帧（460/72911，GET ?ci-process=snapshot&time=...），SDK 用 getSnapshot 返回图片流，
 *    由调用方自己 putObject 落盘，不依赖 CI 是否替我们写 COS。
 * 3. 智能封面：下面两个方法刻意保持孤立，当前没有任何调用方，留待以后前端加「智能挑封面」按钮再接。
 *
 * 前置条件（控制台侧）：开通数据万象 → 绑定存储桶 → 开通媒体处理。
 * 免费额度见 https://cloud.tencent.com/document/product/460/36381 ，均为首次用量起 2 个月有效。
 */
@Slf4j
@Component
public class CiMediaManager {

    /**
     * 元信息接口超时（毫秒）。同步调用挂在上传链路上，不能让一个 CI 请求拖死上传。
     */
    private static final int HTTP_TIMEOUT_MS = 10000;

    @Resource
    private COSClient cosClient;

    @Resource
    private CosClientConfig cosClientConfig;

    /**
     * 同步获取视频元信息（avinfo + magicvar=1）。
     *
     * @param videoKey COS 对象 key，不带前导斜杠
     */
    public VideoMediaInfo getMediaInfo(String videoKey) {
        String url = buildCiUrl(videoKey, "ci-process=avinfo&magicvar=1");
        HttpResponse response = HttpRequest.get(url).timeout(HTTP_TIMEOUT_MS).execute();
        if (!response.isOk()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频元信息接口返回 " + response.getStatus());
        }
        JSONObject root = JSONUtil.parseObj(response.body());
        VideoMediaInfo info = new VideoMediaInfo();

        JSONObject format = root.getJSONObject("format");
        if (format != null) {
            info.setDuration(parseSeconds(format.getStr("duration")));
        }

        // magicvar=1 时视频流直接在 video 节点；保留对未分组响应的兼容
        JSONObject video = root.getJSONObject("video");
        if (video == null) {
            video = findVideoStream(root.getJSONArray("streams"));
        }
        if (video != null) {
            info.setWidth(toInteger(video.get("width")));
            info.setHeight(toInteger(video.get("height")));
        }
        return info;
    }

    /**
     * 同步截帧，返回 jpg 字节流。time 支持小数，mode=exactframe 取指定时间点的精确帧。
     * 不落 COS —— 由业务层决定存到哪里、叫什么名字，避免写出「coverUrl 指向不存在的对象」。
     *
     * @param videoKey COS 对象 key
     * @param time     时间点（秒）
     */
    public byte[] snapshot(String videoKey, double time) {
        CosSnapshotRequest request = new CosSnapshotRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setObjectKey(videoKey);
        request.setTime(String.valueOf(time));
        request.setFormat("jpg");
        request.setMode("exactframe");
        try (InputStream inputStream = cosClient.getSnapshot(request)) {
            if (inputStream == null) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频截帧失败：未返回图片流");
            }
            byte[] bytes = IoUtil.readBytes(inputStream);
            if (bytes == null || bytes.length == 0) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频截帧失败：图片流为空");
            }
            return bytes;
        } catch (Exception e) {
            log.warn("视频截帧失败，key={}, time={}, err={}", videoKey, time, e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "视频截帧失败");
        }
    }

    /**
     * 【预留，当前无调用方】提交智能封面任务，返回 jobId。
     * 智能封面按视频时长计费，且和「用户自己挑一帧」的产品方向冲突，暂不接入上传链路。
     */
    public String submitSmartCover(String videoKey, String coverKey) {
        MediaJobsRequest request = new MediaJobsRequest();
        request.setBucketName(cosClientConfig.getBucket());
        request.setTag("SmartCover");
        MediaInputObject input = new MediaInputObject();
        input.setObject(videoKey);
        request.setInput(input);

        MediaJobOperation operation = request.getOperation() == null ? new MediaJobOperation() : request.getOperation();
        MediaSmartCoverObject smartCover = operation.getSmartCover() == null
                ? new MediaSmartCoverObject() : operation.getSmartCover();
        smartCover.setFormat("jpg");
        smartCover.setWidth("1280");
        smartCover.setHeight("720");
        smartCover.setCount("1");
        smartCover.setDeleteDuplicates("true");
        operation.setSmartCover(smartCover);

        MediaOutputObject output = operation.getOutput() == null ? new MediaOutputObject() : operation.getOutput();
        output.setBucket(cosClientConfig.getBucket());
        output.setRegion(cosClientConfig.getRegion());
        // count=1 时用固定 key，避免 ${Number} 占位导致封面地址不可预测
        output.setObject(coverKey);
        operation.setOutput(output);
        request.setOperation(operation);

        MediaJobResponse response = cosClient.createMediaJobs(request);
        MediaJobObject detail = response.getJobsDetail();
        return detail == null ? null : detail.getJobId();
    }

    /**
     * 【预留，当前无调用方】查询智能封面任务，返回任务详情（调用方自行判断 State）。
     * 不在本层轮询：轮询是业务编排决策，且会长时间占用线程。
     */
    public MediaJobObject describeSmartCover(String jobId) {
        MediaJobsRequest query = new MediaJobsRequest();
        query.setBucketName(cosClientConfig.getBucket());
        query.setJobId(jobId);
        MediaJobResponse response = cosClient.describeMediaJob(query);
        return response.getJobsDetail();
    }

    private JSONObject findVideoStream(JSONArray streams) {
        if (streams == null) {
            return null;
        }
        for (int i = 0; i < streams.size(); i++) {
            JSONObject stream = streams.getJSONObject(i);
            if (stream != null && "video".equalsIgnoreCase(stream.getStr("codec_type"))) {
                return stream;
            }
        }
        return null;
    }

    private Integer parseSeconds(String value) {
        if (StrUtil.isBlank(value)) {
            return null;
        }
        try {
            return (int) Math.round(Double.parseDouble(value.trim()));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer toInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString().trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 拼 CI 处理地址。当前桶为公有读直链，host 是标准 COS 域名，可直接带 ci-process 参数访问。
     * 若将来切到私有读 + 预签名 URL（演进阶段 3），这里要一并改为 SDK 生成带签名参数的 URL。
     */
    private String buildCiUrl(String objectKey, String query) {
        String host = cosClientConfig.getHost();
        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }
        String key = objectKey;
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        return host + "/" + key + "?" + query;
    }
}
