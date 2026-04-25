package com.gxt.aicodegenerationplatform.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 某应用对话历史游标分页结果（单页内消息按时间正序，便于前端直接渲染）。
 */
@Data
public class ChatHistoryAppPageVO implements Serializable {

    private List<ChatHistoryVO> records;

    /**
     * 是否还存在更早的消息
     */
    private Boolean hasMore;

    /**
     * 下次向前加载时传入的 beforeCreateTime（本页时间最早一条，无更多时为 null）
     */
    private LocalDateTime nextBeforeCreateTime;

    /**
     * 下次向前加载时传入的 beforeId（与 nextBeforeCreateTime 成对）
     */
    private Long nextBeforeId;

    private static final long serialVersionUID = 1L;
}
