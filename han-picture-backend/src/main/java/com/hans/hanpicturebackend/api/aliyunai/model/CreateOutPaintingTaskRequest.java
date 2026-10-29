package com.hans.hanpicturebackend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 万相通用图像编辑（wanx2.1-imageedit）请求体 —— 扩图（expand）
 * <p>接口：POST https://{WorkspaceId}.cn-beijing.maas.aliyuncs.com/api/v1/services/aigc/image2image/image-synthesis
 * <p>文档：https://help.aliyun.com/zh/model-studio/wanx-image-edit-api-reference
 * <p>注意：字段上加 @Alias 用于 Hutool 序列化时输出 snake_case（发给阿里云）；
 * @JsonProperty 用于 Jackson 反序列化前端传来的 camelCase（与旧版前端约定一致）。
 * <p>旧版 image-out-painting 请求体已备份到 CreateOutPaintingTaskRequest_Old.java。
 */
@Data
public class CreateOutPaintingTaskRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 必选，模型，例如 "wanx2.1-imageedit"
     */
    private String model = "wanx2.1-imageedit";

    /**
     * 必选，输入图像信息
     */
    private Input input;

    /**
     * 可选，图像处理参数
     */
    private Parameters parameters;

    @Data
    public static class Input implements Serializable {

        /**
         * 必选，图像编辑功能，扩图固定为 "expand"
         */
        private String function = "expand";

        /**
         * 必选，提示词，描述扩图内容，最长 800 字符，超过部分自动截断
         */
        private String prompt ="你看着来";

        /**
         * 必选，输入图像 URL（HTTP/HTTPS，不能含中文字符）或 Base64 数据
         * 格式：data:{MIME_type};base64,{base64_data}；图像大小不超过 10MB
         */
        @Alias("base_image_url")
        @JsonProperty("baseImageUrl")
        private String baseImageUrl;
    }

    @Data
    public static class Parameters implements Serializable {

        /**
         * 可选，向上扩展比例，默认 1.0，范围 [1.0, 2.0]
         */
        @Alias("top_scale")
        @JsonProperty("topScale")
        private Float topScale;

        /**
         * 可选，向下扩展比例，默认 1.0，范围 [1.0, 2.0]
         */
        @Alias("bottom_scale")
        @JsonProperty("bottomScale")
        private Float bottomScale;

        /**
         * 可选，向左扩展比例，默认 1.0，范围 [1.0, 2.0]
         */
        @Alias("left_scale")
        @JsonProperty("leftScale")
        private Float leftScale;

        /**
         * 可选，向右扩展比例，默认 1.0，范围 [1.0, 2.0]
         */
        @Alias("right_scale")
        @JsonProperty("rightScale")
        private Float rightScale;

        /**
         * 可选，生成图片的数量，范围 1~4，默认 1
         */
        private Integer n = 1;

        /**
         * 可选，随机数种子，范围 [0, 2147483647]，使用相同值可保持结果稳定
         */
        private Integer seed;

        /**
         * 可选，是否添加水印，水印位于图片右下角，文案为 "AI生成"，默认 false 不添加
         */
        private Boolean watermark = false;
    }
}
