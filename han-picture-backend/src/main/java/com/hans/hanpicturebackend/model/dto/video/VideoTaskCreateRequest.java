package com.hans.hanpicturebackend.model.dto.video;

import lombok.Data;

import java.io.Serializable;

/**
 * 创建视频 AI 任务请求（MVP 只记录，不触发 AI 执行）
 */
@Data
public class VideoTaskCreateRequest implements Serializable {

    /**
     * 原视频 id
     */
    private Long videoId;

    /**
     * 用户自然语言指令
     */
    private String prompt;

    private static final long serialVersionUID = 1L;
}
