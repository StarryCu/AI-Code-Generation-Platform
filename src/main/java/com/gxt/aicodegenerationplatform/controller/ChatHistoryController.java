package com.gxt.aicodegenerationplatform.controller;

import com.gxt.aicodegenerationplatform.annotation.AuthCheck;
import com.gxt.aicodegenerationplatform.common.BaseResponse;
import com.gxt.aicodegenerationplatform.common.ResultUtils;
import com.gxt.aicodegenerationplatform.constant.UserConstant;
import com.gxt.aicodegenerationplatform.exception.ErrorCode;
import com.gxt.aicodegenerationplatform.exception.ThrowUtils;
import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAdminQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.chat.ChatHistoryAppCursorRequest;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAdminVO;
import com.gxt.aicodegenerationplatform.model.vo.ChatHistoryAppPageVO;
import com.gxt.aicodegenerationplatform.service.ChatHistoryService;
import com.gxt.aicodegenerationplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话历史 控制层。
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {

    @Resource
    private ChatHistoryService chatHistoryService;

    @Resource
    private UserService userService;

    /**
     * 应用创建者或管理员：游标分页查询某应用对话历史（默认最新 10 条，向前加载传游标）。
     */
    @PostMapping("/app/list/page/vo")
    public BaseResponse<ChatHistoryAppPageVO> listAppChatHistoryByCursor(@RequestBody ChatHistoryAppCursorRequest request,
                                                                         HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null || request.getAppId() == null || request.getAppId() <= 0,
                ErrorCode.PARAMS_ERROR, "应用 ID 无效");
        User loginUser = userService.getLoginUser(httpRequest);
        ChatHistoryAppPageVO vo = chatHistoryService.listAppHistoryByCursor(request, loginUser);
        return ResultUtils.success(vo);
    }

    /**
     * 管理员：分页查询全站对话历史（按消息时间降序）。
     */
    @PostMapping("/admin/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ChatHistoryAdminVO>> listChatHistoryAdminByPage(@RequestBody ChatHistoryAdminQueryRequest request) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        Page<ChatHistoryAdminVO> page = chatHistoryService.listChatHistoryAdminVOPage(request);
        return ResultUtils.success(page);
    }
}
