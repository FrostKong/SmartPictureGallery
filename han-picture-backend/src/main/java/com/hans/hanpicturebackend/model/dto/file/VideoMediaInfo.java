package com.hans.hanpicturebackend.model.dto.file;

import lombok.Data;

import java.io.Serializable;

/**
 * 视频元信息（由数据万象 avinfo 同步接口解析而来，只保留本站用到的三个字段）。
 */
@Data
public class VideoMediaInfo implements Serializable {

    /**
     * 时长（秒，四舍五入）
     */
    private Integer duration;

    /**
     * 宽度（px）
     */
    private Integer width;

    /**
     * 高度（px）
     */
    private Integer height;

    private static final long serialVersionUID = 1L;
}
