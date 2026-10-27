package com.hans.hanpicturebackend.manager.upload;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.Method;
import com.hans.hanpicturebackend.config.CosClientConfig;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.execption.ThrowUtils;
import com.hans.hanpicturebackend.manager.CosManager;
import com.hans.hanpicturebackend.model.dto.file.UploadPictureResult;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.CIObject;
import com.qcloud.cos.model.ciModel.persistence.ImageInfo;
import com.qcloud.cos.model.ciModel.persistence.ProcessResults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
public abstract class PictureUploadTemplate {

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private CosManager cosManager;

    /**
     * 上传图片
     *
     * @param inputSource      文件
     * @param uploadPathPrefix 上传路径前缀
     * @return
     */
    public UploadPictureResult uploadPicture(Object inputSource, String uploadPathPrefix) {
        log.info("开始上传图片，inputSource类型：{}，uploadPathPrefix：{}",
                inputSource.getClass().getSimpleName(), uploadPathPrefix);
        // 1. 校验图片
        String contentType = validPicture(inputSource);
        // 2. 生成图片cos中地址
        String uuid = RandomUtil.randomString(16);// 生成16位随机字符串作为唯一ID
        String originFilename = getOriginFilename(inputSource);
        log.info("原始文件名：{}，生成的UUID：{}", originFilename, uuid);
        String uploadFilename;
        if (!contentType.isEmpty()) {
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    contentType);
        } else if (StrUtil.isNotBlank(FileUtil.getSuffix(originFilename))) {
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
                    FileUtil.getSuffix(originFilename));
        } else {
            uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid, "jpg");
        }
        // 自己拼接文件名更安全
//        String uploadFilename = String.format("%s_%s.%s", DateUtil.formatDate(new Date()), uuid,
//                FileUtil.getSuffix(originFilename));
        String uploadPath = String.format("/%s/%s", uploadPathPrefix, uploadFilename);
        log.info("生成的上传文件名：{}，完整上传路径：{}", uploadFilename, uploadPath);
        File file = null;
        try {
            // 3. 创建本地临时文件
            file = File.createTempFile(uploadPath, null);
            log.info("临时文件创建成功，路径：{}", file.getAbsolutePath());
            // 4. 处理文件来源（本地或URL）
            processFile(inputSource, file);
            log.info("文件处理完成，临时文件大小：{}字节", file.length());
            // 上传图片并使用万象来解析图片信息，cos有官方文档
            PutObjectResult putObjectResult = cosManager.putPictureObject(uploadPath, file);
            ImageInfo imageInfo = putObjectResult.getCiUploadResult().getOriginalInfo().getImageInfo();
            // 有专门的拿处理后结果的函数
            ProcessResults processResults = putObjectResult.getCiUploadResult().getProcessResults();
            List<CIObject> objectList = processResults.getObjectList();
            if (CollUtil.isNotEmpty(objectList)) {
                CIObject compressedCiObject = objectList.get(0);
                CIObject thumbnailCiObject = compressedCiObject;
                if (objectList.size() > 1) {
                    thumbnailCiObject = objectList.get(1);
                }
                return buildResult(originFilename, compressedCiObject, thumbnailCiObject, imageInfo);
            }
            // 没有压缩结果的话，返回原图结果
            return buildResult(originFilename, file, uploadPath, imageInfo);
        } catch (Exception e) {
            log.error("图片上传到对象存储失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "上传失败");
        } finally {
            this.deleteTempFile(file);
        }
    }

    /**
     * 封装返回对象（压缩）
     *
     * @param originFilename
     * @param compressedCiObject
     * @return
     */
    private UploadPictureResult buildResult(String originFilename, CIObject compressedCiObject, CIObject thumbnailCiObject, ImageInfo imageInfo) {
        // 计算宽高比
        int picWidth = compressedCiObject.getWidth();
        int picHeight = compressedCiObject.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(compressedCiObject.getFormat());
        uploadPictureResult.setPicSize(compressedCiObject.getSize().longValue());
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + compressedCiObject.getKey());
        uploadPictureResult.setPicColor(imageInfo.getAve());
        // 设置缩略图
        uploadPictureResult.setThumbnailUrl(cosClientConfig.getHost() + "/" + thumbnailCiObject.getKey());
        return uploadPictureResult;
    }

    /**
     * 封装返回对象
     *
     * @param originFilename
     * @param file
     * @param uploadPath
     * @param imageInfo
     * @return
     */
    private UploadPictureResult buildResult(String originFilename, File file, String uploadPath, ImageInfo imageInfo) {
        // 计算宽高比
        int picWidth = imageInfo.getWidth();
        int picHeight = imageInfo.getHeight();
        double picScale = NumberUtil.round(picWidth * 1.0 / picHeight, 2).doubleValue();
        // 封装返回结果
        UploadPictureResult uploadPictureResult = new UploadPictureResult();
        uploadPictureResult.setPicName(FileUtil.mainName(originFilename));
        uploadPictureResult.setPicWidth(picWidth);
        uploadPictureResult.setPicHeight(picHeight);
        uploadPictureResult.setPicScale(picScale);
        uploadPictureResult.setPicFormat(imageInfo.getFormat());
        uploadPictureResult.setPicSize(FileUtil.size(file));
        uploadPictureResult.setUrl(cosClientConfig.getHost() + "/" + uploadPath);
        uploadPictureResult.setPicColor(imageInfo.getAve());
        return uploadPictureResult;
    }


    /**
     * 校验输入源（本地文件或 URL）
     */
    protected abstract String validPicture(Object inputSource);

    /**
     * 获取输入源的原始文件名
     */
    protected abstract String getOriginFilename(Object inputSource);

    /**
     * 处理输入源并生成本地临时文件
     */
    protected abstract void processFile(Object inputSource, File file) throws Exception;

    /**
     * 删除临时文件
     */
    public void deleteTempFile(File file) {
        if (file == null) {
            return;
        }
        // 删除临时文件
        boolean deleteResult = file.delete();
        if (!deleteResult) {
            log.error("file delete error, filepath = {}", file.getAbsolutePath());
        }
    }
}

