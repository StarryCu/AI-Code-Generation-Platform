package com.gxt.aicodegenerationplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建应用。
 */
@Data
public class AppAddRequest implements Serializable {

    /**
     * 应用名称（可选）
     */
    private String appName;

    /**
     * 应用初始化的 prompt（必填）
     */
    private String initPrompt;

    private static final long serialVersionUID = 1L;
}
