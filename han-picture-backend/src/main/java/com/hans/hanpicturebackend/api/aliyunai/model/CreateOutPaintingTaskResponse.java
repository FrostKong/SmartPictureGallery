package com.hans.hanpicturebackend.api.aliyunai.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 创建 AI 扩图任务（wanx2.1-imageedit / image-synthesis）响应体
 * <p>文档：https://help.aliyun.com/zh/model-studio/wanx-image-edit-api-reference
 * <p>注意：本类由 Hutool JSONUtil.toBean 反序列化（AliYunAiApi 内，自动支持下划线转驼峰），
 * 再由 Spring MVC 用 Jackson 序列化返回给前端（保持驼峰，与旧版前端契约一致）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateOutPaintingTaskResponse {

    /**
     * 表示任务的输出信息
     */
    private Output output;

    /**
     * 接口错误码。
     * <p>接口成功请求不会返回该参数。</p>
     */
    private String code;

    /**
     * 接口错误信息。
     * <p>接口成功请求不会返回该参数。</p>
     */
    private String message;

    /**
     * 请求唯一标识。
     * <p>可用于请求明细溯源和问题排查。</p>
     */
    private String requestId;

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
    }
}
