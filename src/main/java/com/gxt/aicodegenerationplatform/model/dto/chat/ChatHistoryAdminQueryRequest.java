package com.gxt.aicodegenerationplatform.model.dto.chat;

import com.gxt.aicodegenerationplatform.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 管理员分页查询全站对话历史。
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ChatHistoryAdminQueryRequest extends PageRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 应用创建者用户 id（对话记录中的 userId 为应用创建者）
     */
    private Long userId;

    /**
     * 消息类型（user / ai / ai_error）
     */
    private String messageType;

    private static final long serialVersionUID = 1L;
}
