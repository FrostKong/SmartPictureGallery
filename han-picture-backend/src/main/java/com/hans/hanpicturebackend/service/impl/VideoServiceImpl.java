package com.hans.hanpicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hans.hanpicturebackend.config.CosClientConfig;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import com.hans.hanpicturebackend.execption.ThrowUtils;
import com.hans.hanpicturebackend.manager.CosManager;
import com.hans.hanpicturebackend.manager.upload.FileVideoUpload;
import com.hans.hanpicturebackend.mapper.VideoMapper;
import com.hans.hanpicturebackend.model.dto.file.UploadVideoResult;
import com.hans.hanpicturebackend.model.dto.video.VideoEditRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoQueryRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoUploadRequest;
import com.hans.hanpicturebackend.model.dto.vo.UserVO;
import com.hans.hanpicturebackend.model.dto.vo.VideoVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.entity.Video;
import com.hans.hanpicturebackend.model.enums.PictureReviewStatusEnum;
import com.hans.hanpicturebackend.service.UserService;
import com.hans.hanpicturebackend.service.VideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 视频服务实现
 */
@Service
@Slf4j
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements VideoService {

    @Resource
    private FileVideoUpload fileVideoUpload;

    @Resource
    private UserService userService;

    @Resource
    private CosManager cosManager;

    @Resource
    private CosClientConfig cosClientConfig;

    @Override
    public VideoVO uploadVideo(MultipartFile multipartFile, VideoUploadRequest videoUploadRequest, User loginUser) {
        ThrowUtils.throwIf(multipartFile == null, ErrorCode.PARAMS_ERROR, "视频不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);

        Long spaceId = videoUploadRequest == null ? null : videoUploadRequest.getSpaceId();
        Long videoId = videoUploadRequest == null ? null : videoUploadRequest.getId();
        Video oldVideo = null;
        if (videoId != null) {
            oldVideo = this.getById(videoId);
            ThrowUtils.throwIf(oldVideo == null, ErrorCode.NOT_FOUND_ERROR, "视频不存在");
            this.checkVideoAuth(loginUser, oldVideo);
            if (spaceId == null) {
                // 没传 spaceId 时复用原有归属
                spaceId = oldVideo.getSpaceId();
            } else if (ObjUtil.notEqual(spaceId, oldVideo.getSpaceId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "视频空间不一致");
            }
        }

        // 视频单独放 video 子目录，避免和图片混在同一目录；此处不做任何空间额度校验
        String uploadPathPrefix = spaceId == null
                ? String.format("public/%s/video", loginUser.getId())
                : String.format("space/%s/video", spaceId);
        UploadVideoResult uploadResult = fileVideoUpload.uploadVideo(multipartFile, uploadPathPrefix);

        Video video = new Video();
        video.setUrl(uploadResult.getUrl());
        video.setCoverUrl(uploadResult.getCoverUrl());
        video.setDuration(uploadResult.getDuration());
        video.setVideoSize(uploadResult.getVideoSize());
        video.setVideoFormat(uploadResult.getVideoFormat());
        String name = uploadResult.getVideoName();
        if (videoUploadRequest != null && StrUtil.isNotBlank(videoUploadRequest.getVideoName())) {
            name = videoUploadRequest.getVideoName();
        }
        video.setName(name);
        video.setUserId(loginUser.getId());
        video.setSpaceId(spaceId);
        if (videoId != null) {
            video.setId(videoId);
            video.setEditTime(new Date());
        }
        this.fillReviewParams(video, loginUser);

        boolean result = this.saveOrUpdate(video);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "视频上传失败");

        // 重新上传时清理旧文件（旧 url 与本次不同才删）
        if (oldVideo != null && StrUtil.isNotBlank(oldVideo.getUrl())
                && !oldVideo.getUrl().equals(video.getUrl())) {
            this.clearVideoFile(oldVideo);
        }
        return VideoVO.objToVo(video);
    }

    @Override
    public QueryWrapper<Video> getQueryWrapper(VideoQueryRequest request) {
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        if (request == null) {
            return queryWrapper;
        }
        if (StrUtil.isNotBlank(request.getSearchText())) {
            queryWrapper.and(qw -> qw.like("name", request.getSearchText())
                    .or()
                    .like("introduction", request.getSearchText()));
        }
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getId()), "id", request.getId());
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getUserId()), "userId", request.getUserId());
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getSpaceId()), "spaceId", request.getSpaceId());
        queryWrapper.isNull(request.isNullSpaceId(), "spaceId");
        queryWrapper.like(StrUtil.isNotBlank(request.getName()), "name", request.getName());
        queryWrapper.like(StrUtil.isNotBlank(request.getIntroduction()), "introduction", request.getIntroduction());
        queryWrapper.eq(StrUtil.isNotBlank(request.getCategory()), "category", request.getCategory());
        queryWrapper.like(StrUtil.isNotBlank(request.getVideoFormat()), "videoFormat", request.getVideoFormat());
        // MVP 尚无视频审核台，只有显式传 reviewStatus 才过滤，避免自己上传后查不到
        queryWrapper.eq(ObjUtil.isNotEmpty(request.getReviewStatus()), "reviewStatus", request.getReviewStatus());
        if (CollUtil.isNotEmpty(request.getTags())) {
            for (String tag : request.getTags()) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.orderBy(StrUtil.isNotEmpty(request.getSortField()),
                "ascend".equals(request.getSortOrder()), request.getSortField());
        return queryWrapper;
    }

    @Override
    public VideoVO getVideoVO(Video video) {
        VideoVO videoVO = VideoVO.objToVo(video);
        Long userId = video.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            videoVO.setUser(userService.getUserVO(user));
        }
        return videoVO;
    }

    @Override
    public Page<VideoVO> getVideoVOPage(Page<Video> videoPage) {
        List<Video> videoList = videoPage.getRecords();
        Page<VideoVO> videoVOPage = new Page<>(videoPage.getCurrent(), videoPage.getSize(), videoPage.getTotal());
        if (CollUtil.isEmpty(videoList)) {
            return videoVOPage;
        }
        List<VideoVO> videoVOList = videoList.stream().map(VideoVO::objToVo).collect(Collectors.toList());
        Set<Long> userIdSet = videoList.stream().map(Video::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        videoVOList.forEach(videoVO -> {
            User user = null;
            List<User> userList = userIdUserListMap.get(videoVO.getUserId());
            if (CollUtil.isNotEmpty(userList)) {
                user = userList.get(0);
            }
            UserVO userVO = userService.getUserVO(user);
            videoVO.setUser(userVO);
        });
        videoVOPage.setRecords(videoVOList);
        return videoVOPage;
    }

    @Override
    public void validVideo(Video video) {
        ThrowUtils.throwIf(video == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(ObjUtil.isNull(video.getId()), ErrorCode.PARAMS_ERROR, "id 不能为空");
        if (StrUtil.isNotBlank(video.getUrl())) {
            ThrowUtils.throwIf(video.getUrl().length() > 1024, ErrorCode.PARAMS_ERROR, "url 过长");
        }
        if (StrUtil.isNotBlank(video.getIntroduction())) {
            ThrowUtils.throwIf(video.getIntroduction().length() > 512, ErrorCode.PARAMS_ERROR, "简介过长");
        }
    }

    @Override
    public void deleteVideo(long videoId, User loginUser) {
        ThrowUtils.throwIf(videoId <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        Video oldVideo = this.getById(videoId);
        ThrowUtils.throwIf(oldVideo == null, ErrorCode.NOT_FOUND_ERROR);
        this.checkVideoAuth(loginUser, oldVideo);
        boolean result = this.removeById(videoId);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        this.clearVideoFile(oldVideo);
    }

    @Override
    public void editVideo(VideoEditRequest videoEditRequest, User loginUser) {
        ThrowUtils.throwIf(videoEditRequest == null || videoEditRequest.getId() == null, ErrorCode.PARAMS_ERROR);
        Video video = new Video();
        BeanUtils.copyProperties(videoEditRequest, video);
        if (CollUtil.isNotEmpty(videoEditRequest.getTags())) {
            video.setTags(JSONUtil.toJsonStr(videoEditRequest.getTags()));
        }
        video.setEditTime(new Date());
        this.validVideo(video);
        Video oldVideo = this.getById(videoEditRequest.getId());
        ThrowUtils.throwIf(oldVideo == null, ErrorCode.NOT_FOUND_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        this.checkVideoAuth(loginUser, oldVideo);
        boolean result = this.updateById(video);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }

    @Override
    public void checkVideoAuth(User loginUser, Video video) {
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NO_AUTH_ERROR);
        if (!video.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
    }

    @Async
    @Override
    public void clearVideoFile(Video oldVideo) {
        ThrowUtils.throwIf(oldVideo == null, ErrorCode.PARAMS_ERROR);
        String url = oldVideo.getUrl();
        if (StrUtil.isBlank(url)) {
            return;
        }
        // 同一文件被多条记录引用时保留
        long count = this.lambdaQuery().eq(Video::getUrl, url).count();
        if (count > 1) {
            return;
        }
        cosManager.deleteObject(this.getCosKey(url));
    }

    @Override
    public String getCosKey(String url) {
        ThrowUtils.throwIf(StrUtil.isBlank(url), ErrorCode.PARAMS_ERROR, "视频地址为空");
        String key = url;
        String host = cosClientConfig.getHost();
        if (StrUtil.isNotBlank(host) && key.startsWith(host)) {
            key = key.substring(host.length());
        }
        while (key.startsWith("/")) {
            key = key.substring(1);
        }
        return key;
    }

    @Override
    public void fillReviewParams(Video video, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            video.setReviewStatus(PictureReviewStatusEnum.PASS.getValue());
        } else {
            video.setReviewStatus(PictureReviewStatusEnum.REVIEWING.getValue());
        }
    }
}
