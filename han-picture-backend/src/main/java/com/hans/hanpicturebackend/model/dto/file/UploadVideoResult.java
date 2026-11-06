package com.hans.hanpicturebackend.model.dto.file;

import lombok.Data;

/**
 * 视频上传结果
 */
@Data
public class UploadVideoResult {

    /**
     * 视频地址（COS 公有读直链）
     */
    private String url;

    /**
     * 视频名称（取自原始文件名）
     */
    private String videoName;

    /**
     * 文件体积（字节）
     */
    private Long videoSize;

    /**
     * 视频格式
     */
    private String videoFormat;

    /**
     * 时长（秒），MVP 不解析，恒为空
     */
    private Integer duration;

    /**
     * 封面图 url，MVP 不生成，恒为空
     */
    private String coverUrl;
}
