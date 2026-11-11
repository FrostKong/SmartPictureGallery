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
     * 发布视频：把 spaceId 置空，从「我的空间草稿」变为「公共图库可见」
     * （当前不做审核，reviewStatus 不参与公共列表过滤）
     */
    void publishVideo(long videoId, User loginUser);

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

    /**
     * 按时间点截帧设封面：归属校验 → CI 同步截帧 → 写 COS → 回写 coverUrl。
     * 返回后端拼好的封面绝对地址（前端只读 url，不自行拼域名）。
     *
     * @param videoId   视频 id
     * @param time      截帧时间点（秒，支持小数）
     * @param loginUser 当前登录用户
     */
    String snapshotCover(long videoId, double time, User loginUser);
}
