package com.gxt.aicodegenerationplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.gxt.aicodegenerationplatform.constant.ChatHistoryConstant;
import com.gxt.aicodegenerationplatform.exception.BusinessException;
import com.gxt.aicodegenerationplatform.exception.ErrorCode;
import com.gxt.aicodegenerationplatform.exception.ThrowUtils;
import com.gxt.aicodegenerationplatform.mapper.AppMapper;
import com.gxt.aicodegenerationplatform.mapper.ChatHistoryMapper;
import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAppCursorRequest;
import com.gxt.aicodegenerationplatform.model.entity.App;
import com.gxt.aicodegenerationplatform.model.entity.ChatHistory;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.enums.MessageTypeEnum;
import com.gxt.aicodegenerationplatform.model.enums.UserRoleEnum;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAdminVO;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAppPageVO;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryVO;
import com.gxt.aicodegenerationplatform.service.ChatHistoryService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 对话历史 服务层实现。
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {

    @Resource
    private AppMapper appMapper;

    @Override
    public void saveUserMessage(Long appId, Long appOwnerUserId, String message) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(appOwnerUserId == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "消息不能为空");
        ChatHistory row = ChatHistory.builder()
                .message(message.trim())
                .messageType(MessageTypeEnum.USER.getValue())
                .appId(appId)
                .userId(appOwnerUserId)
                .build();
        boolean ok = this.save(row);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR, "保存用户消息失败");
    }

    @Override
    public void saveAiMessage(Long appId, Long appOwnerUserId, String message) {
        try {
            if (appId == null || appId <= 0 || appOwnerUserId == null) {
                return;
            }
            String content = message == null ? "" : message;
            ChatHistory row = ChatHistory.builder()
                    .message(content)
                    .messageType(MessageTypeEnum.AI.getValue())
                    .appId(appId)
                    .userId(appOwnerUserId)
                    .build();
            boolean ok = this.save(row);
            if (!ok) {
                log.warn("保存 AI 消息失败, appId={}", appId);
            }
        } catch (Exception e) {
            log.error("保存 AI 消息异常, appId={}", appId, e);
        }
    }

    @Override
    public void saveAiErrorMessage(Long appId, Long appOwnerUserId, String errorMessage) {
        try {
            if (appId == null || appId <= 0 || appOwnerUserId == null) {
                return;
            }
            String content = StrUtil.blankToDefault(errorMessage, "未知错误");
            ChatHistory row = ChatHistory.builder()
                    .message(content)
                    .messageType(MessageTypeEnum.AI_ERROR.getValue())
                    .appId(appId)
                    .userId(appOwnerUserId)
                    .build();
            boolean ok = this.save(row);
            if (!ok) {
                log.warn("保存 AI 失败记录失败, appId={}", appId);
            }
        } catch (Exception e) {
            log.error("保存 AI 失败记录异常, appId={}", appId, e);
        }
    }

    @Override
    public ChatHistoryAppPageVO listAppHistoryByCursor(ChatHistoryAppCursorRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || request.getAppId() == null || request.getAppId() <= 0,
                ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
        App app = appMapper.selectOneById(request.getAppId());
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        assertCanViewAppChatHistory(app, loginUser);

        boolean hasBeforeTime = request.getBeforeCreateTime() != null;
        boolean hasBeforeId = request.getBeforeId() != null;
        if (hasBeforeTime != hasBeforeId) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游标参数 beforeCreateTime 与 beforeId 需同时传入或同时不传");
        }
        if (hasBeforeId && request.getBeforeId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "游标 id 无效");
        }

        int pageSize = capAppPageSize(request.getPageSize());
        int queryLimit = pageSize + 1;
        List<ChatHistory> rows = this.getMapper().listAppHistoryByCursor(
                request.getAppId(),
                hasBeforeTime ? request.getBeforeCreateTime() : null,
                hasBeforeId ? request.getBeforeId() : null,
                queryLimit
        );
        if (rows == null) {
            rows = Collections.emptyList();
        }

        boolean hasMore = rows.size() > pageSize;
        if (hasMore) {
            rows = new ArrayList<>(rows.subList(0, pageSize));
        }
        Collections.reverse(rows);

        ChatHistoryAppPageVO vo = new ChatHistoryAppPageVO();
        vo.setRecords(toChatHistoryVOList(rows));
        vo.setHasMore(hasMore);
        if (hasMore && CollUtil.isNotEmpty(rows)) {
            ChatHistory oldest = rows.get(0);
            vo.setNextBeforeCreateTime(oldest.getCreateTime());
            vo.setNextBeforeId(oldest.getId());
        } else {
            vo.setNextBeforeCreateTime(null);
            vo.setNextBeforeId(null);
        }
        return vo;
    }

    @Override
    public Page<ChatHistoryAdminVO> listChatHistoryAdminVOPage(ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        int pageSize = request.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);

        QueryWrapper qw = QueryWrapper.create();
        if (request.getAppId() != null) {
            qw.eq("appId", request.getAppId());
        }
        if (request.getUserId() != null) {
            qw.eq("userId", request.getUserId());
        }
        if (StrUtil.isNotBlank(request.getMessageType())) {
            qw.eq("messageType", request.getMessageType().trim());
        }
        qw.orderBy("createTime", false).orderBy("id", false);

        Page<ChatHistory> entityPage = this.page(Page.of(pageNum, pageSize), qw);
        Page<ChatHistoryAdminVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotalRow());
        List<ChatHistory> records = entityPage.getRecords();
        if (CollUtil.isEmpty(records)) {
            voPage.setRecords(Collections.emptyList());
            return voPage;
        }

        Set<Long> appIds = records.stream().map(ChatHistory::getAppId).filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, String> appIdToName = Collections.emptyMap();
        if (CollUtil.isNotEmpty(appIds)) {
            List<App> apps = appMapper.selectListByQuery(QueryWrapper.create().in("id", appIds));
            if (CollUtil.isNotEmpty(apps)) {
                appIdToName = apps.stream().collect(Collectors.toMap(App::getId, a -> StrUtil.blankToDefault(a.getAppName(), ""), (a, b) -> a));
            }
        }

        Map<Long, String> finalAppIdToName = appIdToName;
        List<ChatHistoryAdminVO> vos = records.stream().map(h -> {
            ChatHistoryAdminVO avo = new ChatHistoryAdminVO();
            BeanUtil.copyProperties(h, avo);
            avo.setAppName(finalAppIdToName.getOrDefault(h.getAppId(), ""));
            return avo;
        }).collect(Collectors.toList());
        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public boolean removeByAppId(Long appId) {
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
        QueryWrapper qw = QueryWrapper.create().eq("appId", appId);
        return this.remove(qw);
    }

    private static void assertCanViewAppChatHistory(App app, User loginUser) {
        if (UserRoleEnum.ADMIN.getValue().equals(loginUser.getUserRole())) {
            return;
        }
        if (app.getUserId() != null && app.getUserId().equals(loginUser.getId())) {
            return;
        }
        throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "仅应用创建者或管理员可查看对话历史");
    }

    private static int capAppPageSize(Integer pageSize) {
        if (pageSize == null || pageSize <= 0) {
            return ChatHistoryConstant.DEFAULT_APP_PAGE_SIZE;
        }
        return Math.min(pageSize, ChatHistoryConstant.MAX_APP_PAGE_SIZE);
    }

    private static List<ChatHistoryVO> toChatHistoryVOList(List<ChatHistory> list) {
        if (CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        return list.stream().map(ChatHistoryServiceImpl::toChatHistoryVO).collect(Collectors.toList());
    }

    private static ChatHistoryVO toChatHistoryVO(ChatHistory entity) {
        if (entity == null) {
            return null;
        }
        ChatHistoryVO vo = new ChatHistoryVO();
        BeanUtil.copyProperties(entity, vo);
        return vo;
    }

    @Override
    public int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount) {
        try {
            // 直接构造查询条件，起始点为 1 而不是 0，用于排除最新的用户消息
            QueryWrapper queryWrapper = QueryWrapper.create()
                    .eq(ChatHistory::getAppId, appId)
                    .orderBy(ChatHistory::getCreateTime, false)
                    .limit(1, maxCount);
            List<ChatHistory> historyList = this.list(queryWrapper);
            if (CollUtil.isEmpty(historyList)) {
                return 0;
            }
            // 反转列表，确保按时间正序（老的在前，新的在后）
            historyList = historyList.reversed();
            // 按时间顺序添加到记忆中
            int loadedCount = 0;
            // 先清理历史缓存，防止重复加载
            chatMemory.clear();
            for (ChatHistory history : historyList) {
                if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
                    chatMemory.add(UserMessage.from(history.getMessage()));
                    loadedCount++;
                } else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
                    chatMemory.add(AiMessage.from(history.getMessage()));
                    loadedCount++;
                }
            }
            log.info("成功为 appId: {} 加载了 {} 条历史对话", appId, loadedCount);
            return loadedCount;
        } catch (Exception e) {
            log.error("加载历史对话失败，appId: {}, error: {}", appId, e.getMessage(), e);
            // 加载失败不影响系统运行，只是没有历史上下文
            return 0;
        }
    }

}
