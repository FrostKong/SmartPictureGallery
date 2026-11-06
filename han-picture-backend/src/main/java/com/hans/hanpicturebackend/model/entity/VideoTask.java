package com.hans.hanpicturebackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频 AI 任务（MVP 占位：只记录指令与状态，不触发 AI 执行）
 *
 * @TableName video_task
 */
@TableName(value = "video_task")
@Data
public class VideoTask implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 原视频 id
     */
    private Long videoId;

    /**
     * 提交用户 id
     */
    private Long userId;

    /**
     * 用户自然语言指令
     */
    private String prompt;

    /**
     * 任务状态：0-待处理 1-处理中 2-成功 3-失败
     */
    private Integer state;

    /**
     * 结果视频 url（AI 跑完回填）
     */
    private String resultVideoUrl;

    /**
     * AI 结构化输出（预留，暂替素材/片段/脚本表）
     */
    private String resultJson;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
