package com.hans.hanpicturebackend.model.dto.video;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 视频编辑请求（MVP 只允许改元数据，不涉及剪辑）
 */
@Data
public class VideoEditRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 视频名称
     */
    private String name;

    /**
     * 简介
     */
    private String introduction;

    /**
     * 分类
     */
    private String category;

    /**
     * 标签
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}
