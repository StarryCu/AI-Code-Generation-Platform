package com.gxt.aicodegenerationplatform.model.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

/**
 * 对话消息类型。
 */
@Getter
public enum MessageTypeEnum {

    USER("用户消息", "user"),
    AI("AI 回复", "ai"),
    AI_ERROR("AI 失败", "ai_error");

    private final String text;

    private final String value;

    MessageTypeEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据 value 获取枚举
     *
     * @param value 存储值
     * @return 枚举，未匹配时返回 null
     */
    public static MessageTypeEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }
        for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
            if (anEnum.value.equals(value)) {
                return anEnum;
            }
        }
        return null;
    }
}
