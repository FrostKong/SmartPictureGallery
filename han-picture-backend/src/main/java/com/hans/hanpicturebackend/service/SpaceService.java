package com.hans.hanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hans.hanpicturebackend.model.dto.space.SpaceAddRequest;
import com.hans.hanpicturebackend.model.dto.space.SpaceQueryRequest;
import com.hans.hanpicturebackend.model.dto.space.SpaceUpdateRequest;
import com.hans.hanpicturebackend.model.entity.Space;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hans.hanpicturebackend.model.entity.User;
import com.hans.hanpicturebackend.model.dto.vo.SpaceVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Hans
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2025-11-28 17:15:05
 */
public interface SpaceService extends IService<Space> {

    /**
     * 增加空间
     *
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);

    /**
     * 上传图片，接收上传的图片文件、请求参数和登录用户信息，返回SpaceVO对象
     *
     * @param inputSource
     * @param spaceUploadRequest
     * @param loginUser
     * @return
     */
    SpaceVO uploadSpace(Object inputSource,
                        SpaceUpdateRequest spaceUploadRequest,
                        User loginUser);

    /**
     * 根据查询请求获取查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);

    /**
     * 获取图片VO对象（单条）
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取图片VO对象（多条）
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 校验图片
     *
     * @param space
     */
    void validSpace(Space space, boolean add);


    /**
     * 根据空间级别，自动填充限额
     *
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);

    /**
     * 校验空间权限
     *
     * @param loginUser
     * @param space
     */
    void checkSpaceAuth(User loginUser, Space space);
}
