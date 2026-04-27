package com.gxt.aicodegenerationplatform.service;

import com.gxt.aicodegenerationplatform.model.dto.app.AppAddRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppAdminQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppAdminUpdateRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppFeaturedQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppUserQueryRequest;
import com.gxt.aicodegenerationplatform.model.dto.app.AppUserUpdateRequest;
import com.gxt.aicodegenerationplatform.model.entity.App;
import com.gxt.aicodegenerationplatform.model.entity.User;
import com.gxt.aicodegenerationplatform.model.vo.AppVO;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用 服务层。
 */
public interface AppService extends IService<App> {

    Long addApp(AppAddRequest request, User loginUser);

    boolean updateAppByUser(AppUserUpdateRequest request, User loginUser);

    boolean deleteAppByUser(long id, User loginUser);

    boolean deleteAppByAdmin(long id);

    App getAppByIdForAdmin(long id);

    AppVO getAppVOByIdForUser(long id, User loginUser);

    Page<AppVO> listMyAppVOPage(AppUserQueryRequest request, User loginUser);

    Page<AppVO> listFeaturedAppVOPage(AppFeaturedQueryRequest request, User loginUser);

    boolean updateAppByAdmin(AppAdminUpdateRequest request);

    QueryWrapper getAdminQueryWrapper(AppAdminQueryRequest request);

    List<AppVO> getAppVOList(List<App> appList, User viewer);

    AppVO getAppVO(App app, User viewer);

    Flux<String> chatToGenCode(Long appId, String message, User loginUser);

    String deployApp(Long appId, User loginUser);
}
