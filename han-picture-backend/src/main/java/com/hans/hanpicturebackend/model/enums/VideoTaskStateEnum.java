package com.hans.hanpicturebackend.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 视频 AI 任务状态
 * MVP 阶段只会写入 WAITING，其余状态留给后续接入 AI agent 时使用。
 */
@Getter
public enum VideoTaskStateEnum {

    WAITING("待处理", 0),
    RUNNING("处理中", 1),
    SUCCESS("成功", 2),
    FAILED("失败", 3);

    private final String text;

    private final int value;

    VideoTaskStateEnum(String text, int value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 枚举值
     * @return 枚举，未找到返回 null
     */
    public static VideoTaskStateEnum getEnumByValue(Integer value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (VideoTaskStateEnum anEnum : VideoTaskStateEnum.values()) {
            if (anEnum.getValue() == value) {
                return anEnum;
            }
        }
        return null;
    }
}
