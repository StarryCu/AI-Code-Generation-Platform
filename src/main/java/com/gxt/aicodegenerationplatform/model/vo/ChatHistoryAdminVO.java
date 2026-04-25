package com.gxt.aicodegenerationplatform.model.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理员查看对话历史（附带应用名称）。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminVO extends ChatHistoryVO {

    /**
     * 应用名称
     */
    private String appName;
}
