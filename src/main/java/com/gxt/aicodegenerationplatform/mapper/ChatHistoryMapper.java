package com.gxt.aicodegenerationplatform.mapper;

import com.gxt.aicodegenerationplatform.model.entity.ChatHistory;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 对话历史 映射层。
 */
public interface ChatHistoryMapper extends BaseMapper<ChatHistory> {

    /**
     * 按应用游标分页：在指定游标之前（更早）的消息，按时间、id 降序，取前 limit 条。
     *
     * @param appId      应用 id
     * @param beforeTime 游标时间，首次可为 null
     * @param beforeId   游标 id，首次可为 null
     * @param limit      最大条数
     * @return 查询结果（时间降序）
     */
    List<ChatHistory> listAppHistoryByCursor(@Param("appId") Long appId,
                                             @Param("beforeTime") LocalDateTime beforeTime,
                                             @Param("beforeId") Long beforeId,
                                             @Param("limit") int limit);
}
