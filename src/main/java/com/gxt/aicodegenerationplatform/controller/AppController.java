package com.gxt.aicodegenerationplatform.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.gxt.aicodegenerationplatform.annotation.AuthCheck;
import com.gxt.aicodegenerationplatform.common.BaseResponse;
import com.gxt.aicodegenerationplatform.common.DeleteRequest;
import com.gxt.aicodegenerationplatform.common.ResultUtils;
import com.gxt.aicodegenerationplatform.constant.UserConstant;
import com.gxt.aicodegenerationplatform.exception.ErrorCode;
import com.gxt.aicodegenerationplatform.exception.ThrowUtils;
import com.gxt.aicodegenerationplatform.model.dto.app.*;
import com.gxt.aicodegenerationplatform.model.entity.App;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.enums.CodeGenTypeEnum;
import com.gxt.aicodegenerationplatform.model.vo.AppVO;
import com.gxt.aicodegenerationplatform.service.AppService;
import com.gxt.aicodegenerationplatform.service.UserService;
import com.mybatisflex.core.paginate.Page;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * 应用 控制层。
 */
@RestController
@RequestMapping("/app")
public class AppController {

    @Autowired
    private AppService appService;

    @Autowired
    private UserService userService;

    /**
     * 创建应用
     *
     * @param appAddRequest 创建应用请求
     * @param request       请求
     * @return 应用 id
     */
    @PostMapping("/add")
    public BaseResponse<Long> addApp(@RequestBody AppAddRequest appAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appAddRequest == null, ErrorCode.PARAMS_ERROR);
        // 参数校验
        String initPrompt = appAddRequest.getInitPrompt();
        ThrowUtils.throwIf(StrUtil.isBlank(initPrompt), ErrorCode.PARAMS_ERROR, "初始化 prompt 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 构造入库对象
        App app = new App();
        BeanUtil.copyProperties(appAddRequest, app);
        app.setUserId(loginUser.getId());
        // 应用名称暂时为 initPrompt 前 12 位
        app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
        // 暂时设置为 VUE 工程生成
        app.setCodeGenType(CodeGenTypeEnum.VUE_PROJECT.getValue());
        // 插入数据库
        boolean result = appService.save(app);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(app.getId());
    }


    /**
     * 用户：根据 id 修改自己的应用（当前仅支持名称）
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateAppByUser(@RequestBody AppUserUpdateRequest appUserUpdateRequest,
                                                   HttpServletRequest request) {
        ThrowUtils.throwIf(appUserUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean ok = appService.updateAppByUser(appUserUpdateRequest, loginUser);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 用户：根据 id 删除自己的应用
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAppByUser(@RequestBody DeleteRequest deleteRequest,
                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        boolean ok = appService.deleteAppByUser(deleteRequest.getId(), loginUser);
        return ResultUtils.success(ok);
    }

    /**
     * 用户：根据 id 查看应用详情（非本人隐藏 initPrompt、deployKey）
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppVO> getAppVOById(long id, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        AppVO vo = appService.getAppVOByIdForUser(id, loginUser);
        return ResultUtils.success(vo);
    }

    /**
     * 用户：分页查询自己的应用列表（名称模糊，每页最多 20）
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppVO>> listMyAppVOByPage(@RequestBody AppUserQueryRequest appUserQueryRequest,
                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appUserQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<AppVO> page = appService.listMyAppVOPage(appUserQueryRequest, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 用户：分页查询精选应用（priority 大于 0，名称模糊，每页最多 20）
     */
    @PostMapping("/featured/list/page/vo")
    public BaseResponse<Page<AppVO>> listFeaturedAppVOByPage(@RequestBody AppFeaturedQueryRequest appFeaturedQueryRequest,
                                                             HttpServletRequest request) {
        ThrowUtils.throwIf(appFeaturedQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Page<AppVO> page = appService.listFeaturedAppVOPage(appFeaturedQueryRequest, loginUser);
        return ResultUtils.success(page);
    }

    /**
     * 管理员：根据 id 删除任意应用
     */
    @PostMapping("/admin/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null || deleteRequest.getId() <= 0,
                ErrorCode.PARAMS_ERROR);
        boolean ok = appService.deleteAppByAdmin(deleteRequest.getId());
        return ResultUtils.success(ok);
    }

    /**
     * 管理员：根据 id 更新任意应用（名称、封面、优先级）
     */
    @PostMapping("/admin/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateRequest appAdminUpdateRequest) {
        ThrowUtils.throwIf(appAdminUpdateRequest == null, ErrorCode.PARAMS_ERROR);
        boolean ok = appService.updateAppByAdmin(appAdminUpdateRequest);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 管理员：分页查询应用（条件不含时间字段，每页数量不限）
     */
    @PostMapping("/admin/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<App>> listAppByPageForAdmin(@RequestBody AppAdminQueryRequest appAdminQueryRequest) {
        ThrowUtils.throwIf(appAdminQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long pageNum = appAdminQueryRequest.getPageNum();
        int pageSize = appAdminQueryRequest.getPageSize();
        ThrowUtils.throwIf(pageNum <= 0 || pageSize <= 0, ErrorCode.PARAMS_ERROR);
        Page<App> appPage = appService.page(Page.of(pageNum, pageSize),
                appService.getAdminQueryWrapper(appAdminQueryRequest));
        return ResultUtils.success(appPage);
    }

    /**
     * 管理员：根据 id 查看应用详情
     */
    @GetMapping("/admin/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<App> getAppByIdForAdmin(long id) {
        App app = appService.getAppByIdForAdmin(id);
        return ResultUtils.success(app);
    }

    /**
     * 应用聊天生成代码（流式 SSE）
     *
     * @param appId   应用 ID
     * @param message 用户消息
     * @param request 请求对象
     * @return 生成结果流
     */
    @GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam Long appId,
                                                       @RequestParam String message,
                                                       HttpServletRequest request) {
        // 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务生成代码（流式）
        Flux<String> contentFlux = appService.chatToGenCode(appId, message, loginUser);
        // 转换为 ServerSentEvent 格式
        return contentFlux
                .map(chunk -> {
                    // 将内容包装成JSON对象
                    Map<String, String> wrapper = Map.of("d", chunk);
                    String jsonData = JSONUtil.toJsonStr(wrapper);
                    return ServerSentEvent.<String>builder()
                            .data(jsonData)
                            .build();
                });
    }

    /**
     * 应用部署
     *
     * @param appDeployRequest 部署请求
     * @param request          请求
     * @return 部署 URL
     */
    @PostMapping("/deploy")
    public BaseResponse<String> deployApp(@RequestBody AppDeployRequest appDeployRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appDeployRequest == null, ErrorCode.PARAMS_ERROR);
        Long appId = appDeployRequest.getAppId();
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        // 调用服务部署应用
        String deployUrl = appService.deployApp(appId, loginUser);
        return ResultUtils.success(deployUrl);
    }


}
