package com.gxt.aicodegenerationplatform.constant;

/**
 * 应用相关常量。
 */
public interface AppConstant {

    /**
     * 用户侧分页单页最大条数
     */
    int MAX_USER_PAGE_SIZE = 20;

    /**
     * 精选应用：优先级大于该值（默认 0 表示非精选）
     */
    int FEATURED_PRIORITY_THRESHOLD = 0;

    /**
     * 应用生成目录
     */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

    /**
     * 应用部署目录
     */
    String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

    /**
     * 应用部署域名
     */
    String CODE_DEPLOY_HOST = "http://localhost";

}
