package com.gxt.aicodegenerationplatform.model.dto.chat;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 应用对话历史游标分页请求（向前加载更早的消息）。
 * <p>
 * 首次加载不传游标；加载更多时传入当前列表中「最早一条」消息的 createTime 与 id。
 */
@Data
public class ChatHistoryAppCursorRequest implements Serializable {

    /**
     * 应用 id
     */
    private Long appId;

    /**
     * 游标：当前已展示消息中时间最早一条的创建时间（与 beforeId 成对出现）
     */
    private LocalDateTime beforeCreateTime;

    /**
     * 游标：与 beforeCreateTime 同一条消息的 id
     */
    private Long beforeId;

    /**
     * 每页条数，默认 10
     */
    private Integer pageSize;

    private static final long serialVersionUID = 1L;
}
