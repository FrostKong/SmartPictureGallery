package com.hans.hanpicturebackend.api.imagesearch.sub;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import com.hans.hanpicturebackend.execption.BusinessException;
import com.hans.hanpicturebackend.execption.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class GetImagePageUrlApi {

    /**
     * 获取图片页面地址
     *
     * @param imageUrl
     * @return
     */
    public static String getImagePageUrl(String imageUrl) {
        // 1. 准备请求参数
        Map<String, Object> formData = new HashMap<>();
        formData.put("image", imageUrl);
        formData.put("tn", "pc");
        formData.put("from", "pc");
        formData.put("image_source", "PC_UPLOAD_URL");
        // 获取当前时间戳
        long uptime = System.currentTimeMillis();
        // 请求地址
        String url = "https://graph.baidu.com/upload?uptime=" + uptime;

        log.info("准备发送请求到百度接口，URL: {}, 参数: {}", url, formData);

        // 2. 发送 POST 请求到百度接口
        try {
            HttpResponse response = HttpRequest.post(url)
                    .header("acs-token", RandomUtil.randomString(1))
                    .form(formData)
                    .timeout(5000)
                    .execute();
            // 判断响应状态
            int status = response.getStatus();
            if (HttpStatus.HTTP_OK != status) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败，状态码: " + status);
            }
            // 解析响应
            String responseBody = response.body();

            log.info("接口响应状态码: {}, 响应体: {}", status, responseBody);

            Map<String, Object> result = JSONUtil.toBean(responseBody, Map.class);
            if (result == null || !Integer.valueOf(0).equals(result.get("status"))) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "接口调用失败");
            }
            Map<String, Object> data = (Map<String, Object>) result.get("data");
            // String rawUrl = data.get("url").toString();
            String rawUrl = (String) data.get("url");
            // 对 URL 进行解码
            String searchResultUrl = URLUtil.decode(rawUrl, StandardCharsets.UTF_8);
            // 如果URL为空
            if (searchResultUrl == null) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "未返回有效结果");
            }
            return searchResultUrl;
        } catch (Exception e) {
            log.error("调用百度图片搜索失败", e);
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "搜索失败");
        }
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://gips1.baidu.com/it/u=2602479685,2749726813&fm=3074&app=3074&f=JPEG?w=1280&h=759&type=normal&func=";
        String searchResultUrl = getImagePageUrl(imageUrl);
        System.out.println("搜索成功，结果URL：" + searchResultUrl);
    }
}