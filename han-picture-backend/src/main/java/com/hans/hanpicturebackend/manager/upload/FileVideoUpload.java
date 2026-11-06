package com.hans.hanpicturebackend.manager.upload;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.hans.hanpicturebackend.config.CosClientConfig;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.execption.ThrowUtils;
import com.hans.hanpicturebackend.manager.CosManager;
import com.hans.hanpicturebackend.model.dto.file.UploadVideoResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 本地视频文件上传。
 *
 * 注意：这里刻意不复用 PictureUploadTemplate —— 那套模板底层调的是 COS 万象「图片」处理
 * （PicOperations / ImageInfo / 转 webp / 缩略图），视频走不通。
 * MVP 只有「本地文件」一种来源，也不需要为 URL 上传再复制一套模板方法。
 */
@Service
@Slf4j
public class FileVideoUpload {

    /**
     * 单文件上限 100MB，与 application.yml 的 multipart 配置保持一致
     */
    public static final long MAX_VIDEO_SIZE = 100 * 1024 * 1024L;

    private static final List<String> ALLOW_FORMAT_LIST = Arrays.asList("mp4", "webm", "mov", "m4v");

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传视频到 COS，返回可访问地址与基础信息
     *
     * @param multipartFile    视频文件
     * @param uploadPathPrefix 上传路径前缀（如 public/1/video）
     * @return 上传结果
     */
    public UploadVideoResult uploadVideo(MultipartFile multipartFile, String uploadPathPrefix) {
        String suffix = validVideo(multipartFile);
        String originFilename = multipartFile.getOriginalFilename();
        String uuid = RandomUtil.randomString(16);
        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, suffix);
        // key 不带前导斜杠，保证后续 getObject / deleteObject 用的 key 一致
        String uploadPath = String.format("%s/%s", uploadPathPrefix, uploadFilename);
        File file = null;
        try {
            file = File.createTempFile("video_upload_", "." + suffix);
            multipartFile.transferTo(file);
            cosManager.putObject(uploadPath, file);

            UploadVideoResult result = new UploadVideoResult();
            result.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
            result.setVideoName(FileUtil.mainName(originFilename));
            result.setVideoSize(multipartFile.getSize());
            result.setVideoFormat(suffix);
            // duration / coverUrl 需要视频解析或截帧能力，MVP 阶段留空
            return result;
        } catch (Exception e) {
            log.error("视频上传到对象存储失败, uploadPath = {}", uploadPath, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            deleteTempFile(file);
        }
    }

    /**
     * 校验文件并返回小写后缀
     */
    private String validVideo(MultipartFile multipartFile) {
        ThrowUtils.throwIf(multipartFile == null || multipartFile.isEmpty(), ErrorCode.PARAMS_ERROR, "文件不能为空");
        ThrowUtils.throwIf(multipartFile.getSize() > MAX_VIDEO_SIZE, ErrorCode.PARAMS_ERROR, "文件大小不能超过 100MB");
        String originFilename = multipartFile.getOriginalFilename();
        String suffix = originFilename == null ? "" : FileUtil.getSuffix(originFilename);
        suffix = StrUtil.isBlank(suffix) ? "" : suffix.toLowerCase(Locale.ROOT);
        ThrowUtils.throwIf(!ALLOW_FORMAT_LIST.contains(suffix), ErrorCode.PARAMS_ERROR,
                "仅支持 mp4 / webm / mov / m4v 格式");
        return suffix;
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("video temp file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}
