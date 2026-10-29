package com.hans.hanpicturebackend.api.aliyunai.model;

import cn.hutool.core.annotation.Alias;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 查询 AI 扩图任务（wanx2.1-imageedit / image-synthesis）响应体
 * <p>文档：https://help.aliyun.com/zh/model-studio/wanx-image-edit-api-reference
 * <p>注意：本类由 Hutool JSONUtil.toBean 反序列化（AliYunAiApi 内，自动支持下划线转驼峰），
 * 再由 Spring MVC 用 Jackson 序列化返回给前端（保持驼峰，与旧版前端契约一致）。
 * <p>新版接口结果图 URL 在 output.results[].url（不再是 output_image_url）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetOutPaintingTaskResponse {

    /**
     * 请求唯一标识
     */
    private String requestId;

    /**
     * 输出信息
     */
    private Output output;

    /**
     * 输出信息统计，只对成功的结果计数
     */
    private Usage usage;

    /**
     * 表示任务的输出信息
     */
    @Data
    public static class Output {

        /**
         * 任务 ID，查询有效期 24 小时
         */
        private String taskId;

        /**
         * 任务状态
         * <ul>
         *     <li>PENDING：排队中</li>
         *     <li>RUNNING：处理中</li>
         *     <li>SUCCEEDED：执行成功</li>
         *     <li>FAILED：执行失败</li>
         *     <li>CANCELED：任务已取消</li>
         *     <li>UNKNOWN：任务不存在或状态未知</li>
         * </ul>
         */
        private String taskStatus;

        /**
         * 提交时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String submitTime;

        /**
         * 调度时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String scheduledTime;

        /**
         * 结束时间，格式：YYYY-MM-DD HH:mm:ss.SSS
         */
        private String endTime;

        /**
         * 任务结果列表，包括图像 URL、部分任务执行失败报错信息等
         */
        private List<ResultItem> results;

        /**
         * 任务指标信息
         */
        private TaskMetrics taskMetrics;

        /**
         * 接口错误码（任务失败时返回）
         * <p>接口成功请求不会返回该参数。</p>
         */
        private String code;

        /**
         * 接口错误信息（任务失败时返回）
         * <p>接口成功请求不会返回该参数。</p>
         */
        private String message;
    }

    /**
     * 任务结果项
     */
    @Data
    public static class ResultItem {

        /**
         * 生成图像的 URL（有效期 24 小时）
         */
        private String url;

        /**
         * 该结果项的错误码（部分失败时返回）
         */
        private String code;

        /**
         * 该结果项的错误信息（部分失败时返回）
         */
        private String message;
    }

    /**
     * 表示任务的统计信息
     * <p>接口返回的键为大写 TOTAL/SUCCEEDED/FAILED，用 Hutool @Alias 映射。
     */
    @Data
    public static class TaskMetrics {

        /**
         * 总任务数
         */
        @Alias("TOTAL")
        private Integer total;

        /**
         * 成功任务数
         */
        @Alias("SUCCEEDED")
        private Integer succeeded;

        /**
         * 失败任务数
         */
        @Alias("FAILED")
        private Integer failed;
    }

    /**
     * 输出信息统计，只对成功的结果计数
     */
    @Data
    public static class Usage {

        /**
         * 模型成功生成图片的数量。计费公式：费用 = 图片数量 × 单价。
         */
        private Integer imageCount;
    }
}
