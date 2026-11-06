package com.hans.hanpicturebackend.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.execption.ThrowUtils;
import com.hans.hanpicturebackend.mapper.VideoTaskMapper;
import com.hans.hanpicturebackend.model.dto.video.VideoTaskCreateRequest;
import com.hans.hanpicturebackend.model.dto.vo.VideoTaskVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.entity.Video;
import com.hans.hanpicturebackend.model.entity.VideoTask;
import com.hans.hanpicturebackend.model.enums.VideoTaskStateEnum;
import com.hans.hanpicturebackend.service.UserService;
import com.hans.hanpicturebackend.service.VideoService;
import com.hans.hanpicturebackend.service.VideoTaskService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 视频 AI 任务服务实现（MVP 占位）
 */
@Service
public class VideoTaskServiceImpl extends ServiceImpl<VideoTaskMapper, VideoTask> implements VideoTaskService {

    @Resource
    private VideoService videoService;

    @Resource
    private UserService userService;

    @Override
    public VideoTaskVO createTask(VideoTaskCreateRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || request.getVideoId() == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(request.getPrompt()), ErrorCode.PARAMS_ERROR, "指令不能为空");
        ThrowUtils.throwIf(request.getPrompt().length() > 1000, ErrorCode.PARAMS_ERROR, "指令过长");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Video video = videoService.getById(request.getVideoId());
        ThrowUtils.throwIf(video == null, ErrorCode.NOT_FOUND_ERROR, "视频不存在");
        videoService.checkVideoAuth(loginUser, video);

        VideoTask videoTask = new VideoTask();
        videoTask.setVideoId(video.getId());
        videoTask.setUserId(loginUser.getId());
        videoTask.setPrompt(request.getPrompt().trim());
        videoTask.setState(VideoTaskStateEnum.WAITING.getValue());

        boolean saved = this.save(videoTask);
        ThrowUtils.throwIf(!saved, ErrorCode.OPERATION_ERROR, "任务创建失败");
        // 本阶段到此为止：不调用 AI、不投递队列、不启动任何执行逻辑。
        // 后续接入 agent 时，由执行侧把状态改为 RUNNING / SUCCESS / FAILED，
        // 结果写回 resultVideoUrl 或 resultJson。
        return VideoTaskVO.objToVo(videoTask);
    }

    @Override
    public VideoTaskVO getTaskVOById(long taskId, User loginUser) {
        ThrowUtils.throwIf(taskId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        VideoTask videoTask = this.getById(taskId);
        ThrowUtils.throwIf(videoTask == null, ErrorCode.NOT_FOUND_ERROR);
        if (!videoTask.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return VideoTaskVO.objToVo(videoTask);
    }
}
