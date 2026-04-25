package com.gxt.aicodegenerationplatform.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史展示对象。
 */
@Data
public class ChatHistoryVO implements Serializable {

    private Long id;

    private String message;

    /**
     * 消息类型：user / ai / ai_error
     */
    private String messageType;

    private Long appId;

    private Long userId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}
