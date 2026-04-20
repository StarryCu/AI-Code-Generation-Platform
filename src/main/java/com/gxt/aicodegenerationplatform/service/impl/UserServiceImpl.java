package com.gxt.aicodegenerationplatform.service.impl;

import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.gxt.aicodegenerationplatform.entity.User;
import com.gxt.aicodegenerationplatform.mapper.UserMapper;
import com.gxt.aicodegenerationplatform.service.UserService;
import org.springframework.stereotype.Service;

/**
 * 用户 服务层实现。
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService{

}
