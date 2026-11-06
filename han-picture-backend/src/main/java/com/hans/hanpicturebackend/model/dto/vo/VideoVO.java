package com.hans.hanpicturebackend.model.dto.vo;

import cn.hutool.json.JSONUtil;
import com.hans.hanpicturebackend.model.entity.Video;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 视频封装类
 */
@Data
public class VideoVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 视频 url（前端 <video src> 直接使用）
     */
    private String url;

    /**
     * 封面图 url
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
     * 标签
     */
    private List<String> tags;

    /**
     * 文件体积（字节）
     */
    private Long videoSize;

    /**
     * 时长（秒）
     */
    private Integer duration;

    /**
     * 视频格式
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
     * 用户 id
     */
    private Long userId;

    /**
     * 空间 id
     */
    private Long spaceId;

    /**
     * 审核状态
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
     * 创建用户信息（脱敏）
     */
    private UserVO user;

    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     */
    public static Video voToObj(VideoVO videoVO) {
        if (videoVO == null) {
            return null;
        }
        Video video = new Video();
        BeanUtils.copyProperties(videoVO, video);
        video.setTags(JSONUtil.toJsonStr(videoVO.getTags()));
        return video;
    }

    /**
     * 对象转封装类
     */
    public static VideoVO objToVo(Video video) {
        if (video == null) {
            return null;
        }
        VideoVO videoVO = new VideoVO();
        BeanUtils.copyProperties(video, videoVO);
        videoVO.setTags(JSONUtil.toList(video.getTags(), String.class));
        return videoVO;
    }
}
