package com.gxt.aicodegenerationplatform.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用展示对象。
 */
@Data
public class AppVO implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    /**
     * 非所有者访问时为 null
     */
    private String initPrompt;

    private String codeGenType;

    /**
     * 非所有者访问时为 null
     */
    private String deployKey;

    private LocalDateTime deployedTime;

    private Integer priority;

    private Long userId;

    private LocalDateTime editTime;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
