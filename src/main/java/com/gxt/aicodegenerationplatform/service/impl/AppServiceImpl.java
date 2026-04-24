package com.gxt.aicodegenerationplatform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.gxt.aicodegenerationplatform.constant.AppConstant;
import com.gxt.aicodegenerationplatform.core.facade.AiCodeGeneratorFacade;
import com.gxt.aicodegenerationplatform.exception.BusinessException;
import com.gxt.aicodegenerationplatform.exception.ErrorCode;
import com.gxt.aicodegenerationplatform.exception.ThrowUtils;
import com.gxt.aicodegenerationplatform.mapper.AppMapper;
import com.gxt.aicodegenerationplatform.model.dto.app.AppAddRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppAdminQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppAdminUpdateRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppFeaturedQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppUserQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppUserUpdateRequest;
import com.gxt.aicodegenerationplatform.model.entity.App;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.enums.CodeGenTypeEnum;
import com.gxt.aicodegenerationplatform.model.enums.UserRoleEnum;
import com.gxt.aicodegenerationplatform.model.vo.AppVO;
import com.gxt.aicodegenerationplatform.service.AppService;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 应用 服务层实现。
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {

    @Resource
    private AiCodeGeneratorFacade aiCodeGeneratorFacade;
    @Override
    public long addApp(AppAddRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isBlank(request.getInitPrompt())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "initPrompt 不能为空");
        }
        App app = App.builder()
                .appName(StrUtil.blankToDefault(StrUtil.trim(request.getAppName()), "未命名应用"))
                .initPrompt(request.getInitPrompt().trim())
                .userId(loginUser.getId())
                .priority(0)
                .editTime(LocalDateTime.now())
                .build();
        boolean ok = this.save(app);
        ThrowUtils.throwIf(!ok, ErrorCode.OPERATION_ERROR);
        return app.getId();
    }

    @Override
    public boolean updateAppByUser(AppUserUpdateRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        if (StrUtil.isBlank(request.getAppName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
        }
        App old = this.getById(request.getId());
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        if (!old.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        old.setAppName(request.getAppName().trim());
        old.setEditTime(LocalDateTime.now());
        return this.updateById(old);
    }

    @Override
    public boolean deleteAppByUser(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App old = this.getById(id);
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        if (!old.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        return this.removeById(id);
    }

    @Override
    public boolean deleteAppByAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        return this.removeById(id);
    }

    @Override
    public App getAppByIdForAdmin(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return app;
    }

    @Override
    public AppVO getAppVOByIdForUser(long id, User loginUser) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        App app = this.getById(id);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
        return getAppVO(app, loginUser);
    }

    @Override
    public Page<AppVO> listMyAppVOPage(AppUserQueryRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        int pageSize = capUserPageSize(request.getPageSize());
        QueryWrapper qw = QueryWrapper.create()
                .eq("userId", loginUser.getId());
        if (StrUtil.isNotBlank(request.getAppName())) {
            qw.like("appName", request.getAppName());
        }
        applyUserSort(qw, request.getSortField(), request.getSortOrder());
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords(), loginUser));
        return voPage;
    }

    @Override
    public Page<AppVO> listFeaturedAppVOPage(AppFeaturedQueryRequest request, User loginUser) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        long pageNum = request.getPageNum();
        int pageSize = capUserPageSize(request.getPageSize());
        QueryWrapper qw = QueryWrapper.create()
                .gt("priority", AppConstant.FEATURED_PRIORITY_THRESHOLD);
        if (StrUtil.isNotBlank(request.getAppName())) {
            qw.like("appName", request.getAppName());
        }
        qw.orderBy("priority", false).orderBy("createTime", false);
        Page<App> appPage = this.page(Page.of(pageNum, pageSize), qw);
        Page<AppVO> voPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
        voPage.setRecords(getAppVOList(appPage.getRecords(), loginUser));
        return voPage;
    }

    @Override
    public boolean updateAppByAdmin(AppAdminUpdateRequest request) {
        ThrowUtils.throwIf(request == null || request.getId() == null || request.getId() <= 0, ErrorCode.PARAMS_ERROR);
        App old = this.getById(request.getId());
        ThrowUtils.throwIf(old == null, ErrorCode.NOT_FOUND_ERROR);
        if (StrUtil.isNotBlank(request.getAppName())) {
            old.setAppName(request.getAppName().trim());
        }
        if (request.getCover() != null) {
            old.setCover(request.getCover());
        }
        if (request.getPriority() != null) {
            old.setPriority(request.getPriority());
        }
        old.setEditTime(LocalDateTime.now());
        return this.updateById(old);
    }

    @Override
    public QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        QueryWrapper qw = QueryWrapper.create();
        if (request.getId() != null) {
            qw.eq("id", request.getId());
        }
        if (StrUtil.isNotBlank(request.getAppName())) {
            qw.like("appName", request.getAppName());
        }
        if (StrUtil.isNotBlank(request.getCover())) {
            qw.like("cover", request.getCover());
        }
        if (StrUtil.isNotBlank(request.getInitPrompt())) {
            qw.like("initPrompt", request.getInitPrompt());
        }
        if (StrUtil.isNotBlank(request.getCodeGenType())) {
            qw.eq("codeGenType", request.getCodeGenType());
        }
        if (StrUtil.isNotBlank(request.getDeployKey())) {
            qw.eq("deployKey", request.getDeployKey());
        }
        if (request.getDeployedTime() != null) {
            qw.eq("deployedTime", request.getDeployedTime());
        }
        if (request.getPriority() != null) {
            qw.eq("priority", request.getPriority());
        }
        if (request.getUserId() != null) {
            qw.eq("userId", request.getUserId());
        }
        String sortField = request.getSortField();
        String sortOrder = request.getSortOrder();
        if (StrUtil.isNotBlank(sortField)) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("updateTime", false);
        }
        return qw;
    }

    @Override
    public List<AppVO> getAppVOList(List<App> appList, User viewer) {
        if (CollUtil.isEmpty(appList)) {
            return new ArrayList<>();
        }
        return appList.stream().map(a -> getAppVO(a, viewer)).collect(Collectors.toList());
    }

    @Override
    public AppVO getAppVO(App app, User viewer) {
        if (app == null) {
            return null;
        }
        AppVO vo = new AppVO();
        BeanUtil.copyProperties(app, vo);
        if (!showSensitiveFields(app, viewer)) {
            vo.setInitPrompt(null);
            vo.setDeployKey(null);
        }
        return vo;
    }

    private static void applyUserSort(QueryWrapper qw, String sortField, String sortOrder) {
        if (StrUtil.isNotBlank(sortField)) {
            qw.orderBy(sortField, "ascend".equals(sortOrder));
        } else {
            qw.orderBy("updateTime", false);
        }
    }

    private static int capUserPageSize(int pageSize) {
        if (pageSize <= 0) {
            return AppConstant.MAX_USER_PAGE_SIZE;
        }
        return Math.min(pageSize, AppConstant.MAX_USER_PAGE_SIZE);
    }

    private static boolean showSensitiveFields(App app, User viewer) {
        if (viewer == null) {
            return false;
        }
        if (UserRoleEnum.ADMIN.getValue().equals(viewer.getUserRole())) {
            return true;
        }
        return app.getUserId() != null && app.getUserId().equals(viewer.getId());
    }

    @Override
    public Flux<String> chatToGenCode(Long appId, String message, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(StrUtil.isBlank(message), ErrorCode.PARAMS_ERROR, "用户消息不能为空");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限访问该应用，仅本人可以生成代码
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
        }
        // 4. 获取应用的代码生成类型
        String codeGenTypeStr = app.getCodeGenType();
        CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getEnumByValue(codeGenTypeStr);
        if (codeGenTypeEnum == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的代码生成类型");
        }
        // 5. 调用 AI 生成代码
        return aiCodeGeneratorFacade.generateAndSaveCodeStream(message, codeGenTypeEnum, appId);
    }

    @Override
    public String deployApp(Long appId, User loginUser) {
        // 1. 参数校验
        ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用 ID 不能为空");
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
        // 2. 查询应用信息
        App app = this.getById(appId);
        ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
        // 3. 验证用户是否有权限部署该应用，仅本人可以部署
        if (!app.getUserId().equals(loginUser.getId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限部署该应用");
        }
        // 4. 检查是否已有 deployKey
        String deployKey = app.getDeployKey();
        // 没有则生成 6 位 deployKey（大小写字母 + 数字）
        if (StrUtil.isBlank(deployKey)) {
            deployKey = RandomUtil.randomString(6);
        }
        // 5. 获取代码生成类型，构建源目录路径
        String codeGenType = app.getCodeGenType();
        String sourceDirName = codeGenType + "_" + appId;
        String sourceDirPath = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
        // 6. 检查源目录是否存在
        File sourceDir = new File(sourceDirPath);
        if (!sourceDir.exists() || !sourceDir.isDirectory()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "应用代码不存在，请先生成代码");
        }
        // 7. 复制文件到部署目录
        String deployDirPath = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
        try {
            FileUtil.copyContent(sourceDir, new File(deployDirPath), true);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "部署失败：" + e.getMessage());
        }
        // 8. 更新应用的 deployKey 和部署时间
        App updateApp = new App();
        updateApp.setId(appId);
        updateApp.setDeployKey(deployKey);
        updateApp.setDeployedTime(LocalDateTime.now());
        boolean updateResult = this.updateById(updateApp);
        ThrowUtils.throwIf(!updateResult, ErrorCode.OPERATION_ERROR, "更新应用部署信息失败");
        // 9. 返回可访问的 URL
        return String.format("%s/%s/", AppConstant.CODE_DEPLOY_HOST, deployKey);
    }

}
