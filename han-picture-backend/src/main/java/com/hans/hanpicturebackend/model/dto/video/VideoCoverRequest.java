package com.hans.hanpicturebackend.model.dto.video;

import lombok.Data;

import java.io.Serializable;

/**
 * 按时间点截帧设置封面的请求。
 */
@Data
public class VideoCoverRequest implements Serializable {

    /**
     * 视频 id
     */
    private Long id;

    /**
     * 截帧时间点（秒，支持小数）
     */
    private Double time;

    private static final long serialVersionUID = 1L;
}
