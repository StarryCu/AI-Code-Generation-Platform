package com.gxt.aicodegenerationplatform.service;

import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAppCursorRequest;
import com.gxt.aicodegenerationplatform.model.entity.ChatHistory;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAdminVO;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAppPageVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.service.IService;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

/**
 * 对话历史 服务层。
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 保存用户消息。
     */
    void saveUserMessage(Long appId, Long appOwnerUserId, String message);

    /**
     * 保存 AI 成功回复全文（失败不抛异常，避免影响流式链路）。
     */
    void saveAiMessage(Long appId, Long appOwnerUserId, String message);

    /**
     * 保存 AI 调用失败信息。
     */
    void saveAiErrorMessage(Long appId, Long appOwnerUserId, String errorMessage);

    /**
     * 应用创建者或管理员：游标分页查询某应用的对话历史（单页内时间正序）。
     */
    ChatHistoryAppPageVO listAppHistoryByCursor(ChatHistoryAppCursorRequest request, User loginUser);

    /**
     * 管理员：分页查询全站对话历史（按消息时间降序），附带应用名称。
     */
    Page<ChatHistoryAdminVO> listChatHistoryAdminVOPage(ChatHistoryAdminQueryRequest request);

    /**
     * 按应用 id 逻辑删除全部对话记录（删除应用时调用）。
     */
    boolean removeByAppId(Long appId);

    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
