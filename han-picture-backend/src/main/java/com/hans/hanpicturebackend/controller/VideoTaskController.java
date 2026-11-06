package com.hans.hanpicturebackend.controller;

import com.hans.hanpicturebackend.common.BaseResponse;
import com.hans.hanpicturebackend.common.ResultUtils;
import com.hans.hanpicturebackend.model.dto.video.VideoTaskCreateRequest;
import com.hans.hanpicturebackend.model.dto.vo.VideoTaskVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.service.UserService;
import com.hans.hanpicturebackend.service.VideoTaskService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 视频 AI 任务接口（MVP 占位）
 *
 * 当前只把用户的自然语言指令落成一条 WAITING 任务记录，并返回状态，
 * 不调用任何模型、不投递队列、不启动后台执行。状态流转与结果回填留给后续接入 agent 时实现。
 */
@RestController
@RequestMapping("/video/task")
public class VideoTaskController {

    @Resource
    private UserService userService;

    @Resource
    private VideoTaskService videoTaskService;

    /**
     * 提交一句话指令
     */
    @PostMapping("/create")
    public BaseResponse<VideoTaskVO> createTask(@RequestBody VideoTaskCreateRequest videoTaskCreateRequest,
                                                HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(videoTaskService.createTask(videoTaskCreateRequest, loginUser));
    }

    /**
     * 查询任务状态
     */
    @GetMapping("/get")
    public BaseResponse<VideoTaskVO> getTask(long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(videoTaskService.getTaskVOById(id, loginUser));
    }
}
