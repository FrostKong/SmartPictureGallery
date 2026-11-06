package com.hans.hanpicturebackend.model.dto.vo;

import com.hans.hanpicturebackend.model.entity.VideoTask;
import com.hans.hanpicturebackend.model.enums.VideoTaskStateEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 视频 AI 任务封装类
 */
@Data
public class VideoTaskVO implements Serializable {

    private Long id;

    private Long videoId;

    private Long userId;

    /**
     * 用户自然语言指令
     */
    private String prompt;

    /**
     * 任务状态值
     */
    private Integer state;

    /**
     * 任务状态文案
     */
    private String stateText;

    private String resultVideoUrl;

    private String resultJson;

    private String errorMessage;

    private Date createTime;

    private Date updateTime;

    private static final long serialVersionUID = 1L;

    public static VideoTaskVO objToVo(VideoTask videoTask) {
        if (videoTask == null) {
            return null;
        }
        VideoTaskVO videoTaskVO = new VideoTaskVO();
        BeanUtils.copyProperties(videoTask, videoTaskVO);
        VideoTaskStateEnum stateEnum = VideoTaskStateEnum.getEnumByValue(videoTask.getState());
        if (stateEnum != null) {
            videoTaskVO.setStateText(stateEnum.getText());
        }
        return videoTaskVO;
    }
}
