package com.hans.hanpicturebackend.model.dto.space.analyze;

import lombok.Data;
import lombok.EqualsAndHashCode;

// @EqualsAndHashCode(callSuper = true)是核心优化：确保 equals/hashCode 方法比较时包含父类属性，避免空子类比较逻辑错误。
@EqualsAndHashCode(callSuper = true)
@Data
public class SpaceUsageAnalyzeRequest extends SpaceAnalyzeRequest {

}

