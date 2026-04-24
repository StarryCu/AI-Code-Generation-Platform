package com.gxt.aicodegenerationplatform.model.dto.app;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新自己的应用（当前仅支持名称）。
 */
@Data
public class AppUserUpdateRequest implements Serializable {

    private Long id;

    private String appName;

    private static final long serialVersionUID = 1L;
}
