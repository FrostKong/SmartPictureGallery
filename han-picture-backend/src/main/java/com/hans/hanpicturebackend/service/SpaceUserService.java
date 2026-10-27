package com.hans.hanpicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hans.hanpicturebackend.model.dto.spaceuser.SpaceUserAddRequest;
import com.hans.hanpicturebackend.model.dto.spaceuser.SpaceUserQueryRequest;
import com.hans.hanpicturebackend.model.dto.vo.SpaceUserVO;
import com.hans.hanpicturebackend.model.entity.SpaceUser;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Hans
 * @description 针对表【space_user(空间用户关联)】的数据库操作Service
 * @createDate 2026-03-10 18:07:54
 */
public interface SpaceUserService extends IService<SpaceUser> {

    /**
     * 添加空间用户
     *
     * @param spaceUserAddRequest
     * @return
     */
    long addSpaceUser(SpaceUserAddRequest spaceUserAddRequest);

    /**
     * 验证空间用户
     *
     * @param spaceUser
     * @param add
     */
    void validSpaceUser(SpaceUser spaceUser, boolean add);

    /**
     * 获取查询条件
     *
     * @param spaceUserQueryRequest
     * @return
     */
    QueryWrapper<SpaceUser> getQueryWrapper(SpaceUserQueryRequest spaceUserQueryRequest);

    /**
     * 获取空间用户VO
     *
     * @param spaceUser
     * @param request
     * @return
     */
    SpaceUserVO getSpaceUserVO(SpaceUser spaceUser, HttpServletRequest request);

    /**
     * 获取空间用户VO列表
     *
     * @param spaceUserList
     * @return
     */
    List<SpaceUserVO> getSpaceUserVOList(List<SpaceUser> spaceUserList);
}
