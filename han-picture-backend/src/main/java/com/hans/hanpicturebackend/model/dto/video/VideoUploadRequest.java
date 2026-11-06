package com.hans.hanpicturebackend.model.dto.video;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频上传请求（MVP 只支持本地文件，不支持 URL 上传）
 */
@Data
public class VideoUploadRequest implements Serializable {

    /**
     * 视频 id（传入时表示重新上传覆盖原视频）
     */
    private Long id;

    /**
     * 视频名称（不传则取原始文件名）
     */
    private String videoName;

    /**
     * 空间 id（暂不参与配额统计，仅用于分目录，可为空）
     */
    private Long spaceId;

    private static final long serialVersionUID = 1L;
}
