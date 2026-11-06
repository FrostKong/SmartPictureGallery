package com.hans.hanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hans.hanpicturebackend.model.dto.video.VideoEditRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoQueryRequest;
import com.hans.hanpicturebackend.model.dto.video.VideoUploadRequest;
import com.hans.hanpicturebackend.model.dto.vo.VideoVO;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.entity.Video;
import org.springframework.web.multipart.MultipartFile;

/**
 * 视频服务
 */
public interface VideoService extends IService<Video> {

    /**
     * 上传视频（传入 id 时表示重新上传覆盖）
     */
    VideoVO uploadVideo(MultipartFile multipartFile, VideoUploadRequest videoUploadRequest, User loginUser);

    /**
     * 构造查询条件
     */
    QueryWrapper<Video> getQueryWrapper(VideoQueryRequest videoQueryRequest);

    /**
     * 单条视频转封装类（关联用户脱敏）
     */
    VideoVO getVideoVO(Video video);

    /**
     * 分页视频转封装类
     */
    Page<VideoVO> getVideoVOPage(Page<Video> videoPage);

    /**
     * 数据校验
     */
    void validVideo(Video video);

    /**
     * 删除视频（含 COS 文件清理）
     */
    void deleteVideo(long videoId, User loginUser);

    /**
     * 编辑视频元数据
     */
    void editVideo(VideoEditRequest videoEditRequest, User loginUser);

    /**
     * 权限校验：本人或管理员
     */
    void checkVideoAuth(User loginUser, Video video);

    /**
     * 清理 COS 上的视频文件（同一 url 被多条记录引用时不清理）
     */
    void clearVideoFile(Video oldVideo);

    /**
     * 从可访问 url 反推出 COS 对象 key
     */
    String getCosKey(String url);

    /**
     * 填入审核参数
     */
    void fillReviewParams(Video video, User loginUser);
}
