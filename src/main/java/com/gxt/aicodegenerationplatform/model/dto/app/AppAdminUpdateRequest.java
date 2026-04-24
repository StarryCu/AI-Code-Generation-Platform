package com.gxt.aicodegenerationplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 管理员更新应用。
 */
@Data
public class AppAdminUpdateRequest implements Serializable {

    private Long id;

    private String appName;

    private String cover;

    private Integer priority;

    private static final long serialVersionUID = 1L;
}
