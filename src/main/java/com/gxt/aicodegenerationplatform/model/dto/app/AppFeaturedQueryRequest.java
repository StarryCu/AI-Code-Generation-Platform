package com.gxt.aicodegenerationplatform.model.dto.app;

import com.gxt.aicodegenerationplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 用户分页查询精选应用。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class AppFeaturedQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用名称（模糊）
     */
    private String appName;

    private static final long serialVersionUID = 1L;
}
