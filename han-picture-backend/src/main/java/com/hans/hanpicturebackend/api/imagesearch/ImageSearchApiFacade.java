package com.hans.hanpicturebackend.api.imagesearch;

import com.hans.hanpicturebackend.api.imagesearch.model.ImageSearchResult;
import com.hans.hanpicturebackend.api.imagesearch.sub.GetImageFirstUrlApi;
import com.hans.hanpicturebackend.api.imagesearch.sub.GetImageListApi;
import com.hans.hanpicturebackend.api.imagesearch.sub.GetImagePageUrlApi;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ImageSearchApiFacade {

    /**
     * 搜索图片
     *
     * @param imageUrl
     * @return
     */
    public static List<ImageSearchResult> searchImage(String imageUrl) {
        String imagePageUrl = GetImagePageUrlApi.getImagePageUrl(imageUrl);
        String imageFirstUrl = GetImageFirstUrlApi.getImageFirstUrl(imagePageUrl);
        List<ImageSearchResult> imageList = GetImageListApi.getImageList(imageFirstUrl);
        return imageList;
    }

    public static void main(String[] args) {
        // 测试以图搜图功能
        String imageUrl = "https://gips1.baidu.com/it/u=2602479685,2749726813&fm=3074&app=3074&f=JPEG?w=1280&h=759&type=normal&func=";
        List<ImageSearchResult> resultList = searchImage(imageUrl);
        System.out.println("结果列表" + resultList);
    }
}
