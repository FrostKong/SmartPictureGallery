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
 * 视频
 *
 * @TableName video
 */
@TableName(value = "video")
@Data
public class Video implements Serializable {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 视频 url（COS 公有读直链，前端可直接播放）
     */
    private String url;

    /**
     * 封面图 url（MVP 不生成，预留）
     */
    private String coverUrl;

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
     * 标签（JSON 数组）
     */
    private String tags;

    /**
     * 文件体积（字节）
     */
    private Long videoSize;

    /**
     * 时长（秒），MVP 不解析视频元数据
     */
    private Integer duration;

    /**
     * 视频格式：mp4/webm/mov/m4v
     */
    private String videoFormat;

    /**
     * 宽度
     */
    private Integer width;

    /**
     * 高度
     */
    private Integer height;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 空间 id（暂不参与配额统计，仅预留）
     */
    private Long spaceId;

    /**
     * 审核状态：0-待审核; 1-通过; 2-拒绝
     */
    private Integer reviewStatus;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 编辑时间
     */
    private Date editTime;

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
