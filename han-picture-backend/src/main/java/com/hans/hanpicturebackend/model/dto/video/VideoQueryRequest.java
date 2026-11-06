package com.hans.hanpicturebackend.model.dto.video;

import com.hans.hanpicturebackend.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * 视频查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VideoQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 是否只查询 spaceId 为 null 的数据
     */
    private boolean nullSpaceId;

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

    /**
     * 搜索词（同时搜名称、简介）
     */
    private String searchText;

    /**
     * 用户 id
     */
    private Long userId;

    /**
     * 视频格式
     */
    private String videoFormat;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    private static final long serialVersionUID = 1L;
}
