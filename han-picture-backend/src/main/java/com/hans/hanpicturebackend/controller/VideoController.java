package com.hans.hanpicturebackend.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hans.hanpicturebackend.common.BaseResponse;
import com.hans.hanpicturebackend.common.DeleteRequest;
import com.hans.hanpicturebackend.common.ResultUtils;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.execption.ThrowUtils;
import com.hans.hanpicturebackend.manager.CosManager;
import com.hans.hanpicturebackend.model.dto.video.VideoEditRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoQueryRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoUploadRequest;
import com.hans.hanpicturebackend.model.dto.vo.VideoVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.entity.Video;
import com.hans.hanpicturebackend.service.UserService;
import com.hans.hanpicturebackend.service.VideoService;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.COSObjectInputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * 视频接口
 *
 * 播放说明：COS 桶为公有读，前端直接用 VideoVO.url 作为 <video src> 即可，
 * 浏览器自行发起 Range 请求拖动进度条，后端不需要额外的播放/串流接口。
 * 本 Controller 的 /download 只负责「另存为文件」，且采用流式写回，不把视频读进内存。
 */
@Slf4j
@RestController
@RequestMapping("/video")
public class VideoController {

    @Resource
    private UserService userService;

    @Resource
    private VideoService videoService;

    @Resource
    private CosManager cosManager;

    /**
     * 上传视频（传入 id 时表示重新上传覆盖）
     */
    @PostMapping("/upload")
    public BaseResponse<VideoVO> uploadVideo(
            @RequestPart("file") MultipartFile multipartFile,
            VideoUploadRequest videoUploadRequest,
            HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        VideoVO videoVO = videoService.uploadVideo(multipartFile, videoUploadRequest, loginUser);
        return ResultUtils.success(videoVO);
    }

    /**
     * 根据 id 获取视频（封装类）
     */
    @GetMapping("/get/vo")
    public BaseResponse<VideoVO> getVideoVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        userService.getLoginUser(request);
        Video video = videoService.getById(id);
        ThrowUtils.throwIf(video == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(videoService.getVideoVO(video));
    }

    /**
     * 分页获取视频列表（封装类）
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<VideoVO>> listVideoVOByPage(@RequestBody VideoQueryRequest videoQueryRequest,
                                                         HttpServletRequest request) {
        ThrowUtils.throwIf(videoQueryRequest == null, ErrorCode.PARAMS_ERROR);
        userService.getLoginUser(request);
        long current = videoQueryRequest.getCurrent();
        long size = videoQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Video> videoPage = videoService.page(new Page<>(current, size),
                videoService.getQueryWrapper(videoQueryRequest));
        return ResultUtils.success(videoService.getVideoVOPage(videoPage));
    }

    /**
     * 编辑视频元数据
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editVideo(@RequestBody VideoEditRequest videoEditRequest,
                                           HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        videoService.editVideo(videoEditRequest, loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 删除视频
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteVideo(@RequestBody DeleteRequest deleteRequest,
                                             HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        videoService.deleteVideo(deleteRequest.getId(), loginUser);
        return ResultUtils.success(true);
    }

    /**
     * 下载视频（流式，避免大文件读入内存）
     */
    @GetMapping("/download")
    public void downloadVideo(long id, HttpServletRequest request, HttpServletResponse response) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Video video = videoService.getById(id);
        ThrowUtils.throwIf(video == null, ErrorCode.NOT_FOUND_ERROR);
        videoService.checkVideoAuth(loginUser, video);

        String key = videoService.getCosKey(video.getUrl());
        COSObjectInputStream cosObjectInput = null;
        try {
            COSObject cosObject = cosManager.getObject(key);
            cosObjectInput = cosObject.getObjectContent();
            long contentLength = cosObject.getObjectMetadata().getContentLength();
            response.setContentType(resolveContentType(video.getVideoFormat()));
            if (contentLength > 0) {
                response.setHeader("Content-Length", String.valueOf(contentLength));
            }
            String downloadName = buildDownloadName(video);
            String encodedName = URLEncoder.encode(downloadName, "UTF-8").replace("+", "%20");
            String extension = resolveExtension(video.getVideoFormat());
            // 同时给 ASCII 回退名与 RFC 5987 中文名：
            // 前者保证不识别 filename* 的客户端（含 Swagger）也能拿到带正确扩展名的文件名，
            // 后者让现代浏览器显示中文名
            response.setHeader("Content-Disposition",
                    "attachment; filename=\"video-" + video.getId() + "." + extension
                            + "\"; filename*=UTF-8''" + encodedName);
            byte[] buffer = new byte[8192];
            int len;
            while ((len = cosObjectInput.read(buffer)) != -1) {
                response.getOutputStream().write(buffer, 0, len);
            }
            response.getOutputStream().flush();
        } catch (Exception e) {
            log.error("video download error, id = {}, key = {}", id, key, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载失败");
        } finally {
            if (cosObjectInput != null) {
                try {
                    cosObjectInput.close();
                } catch (IOException e) {
                    log.error("video download stream close error, key = {}", key, e);
                }
            }
        }
    }

    /**
     * 视频格式对应的 Content-Type
     */
    private String resolveContentType(String format) {
        if ("webm".equals(format)) {
            return "video/webm";
        }
        if ("mov".equals(format)) {
            return "video/quicktime";
        }
        return "video/mp4";
    }

    /**
     * 视频格式对应的文件扩展名（m4v 按 mp4 落盘）
     */
    private String resolveExtension(String format) {
        if ("webm".equals(format) || "mov".equals(format)) {
            return format;
        }
        return "mp4";
    }

    /**
     * 构造下载文件名：清洗非法字符、去掉响应头注入风险，并补上真实扩展名
     */
    private String buildDownloadName(Video video) {
        String name = video.getName();
        if (StrUtil.isBlank(name)) {
            name = "video";
        }
        name = name.replaceAll("[\\\\/:*?\"<>|\\r\\n]", "_").trim();
        String extension = resolveExtension(video.getVideoFormat());
        if (!name.toLowerCase(Locale.ROOT).endsWith("." + extension)) {
            name = name + "." + extension;
        }
        return name;
    }
}
