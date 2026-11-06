package com.hans.hanpicturebackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hans.hanpicturebackend.model.dto.video.VideoTaskCreateRequest;
import com.hans.hanpicturebackend.model.dto.vo.VideoTaskVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.entity.VideoTask;

/**
 * 视频 AI 任务服务
 * MVP 阶段只做「记录指令 + 查询状态」占位，不接模型、不发消息、不启后台线程。
 */
public interface VideoTaskService extends IService<VideoTask> {

    /**
     * 创建任务（状态固定为 WAITING）
     */
    VideoTaskVO createTask(VideoTaskCreateRequest request, User loginUser);

    /**
     * 根据 id 查询任务（带归属校验）
     */
    VideoTaskVO getTaskVOById(long taskId, User loginUser);
}
