package com.gxt.aicodegenerationplatform.common;

import org.springframework.util.DigestUtils;

public class PasswordUtils {
    public static String getEncryptPassword(String userPassword) {
        // 盐值，混淆密码
        final String SALT = "togawa_sakiko";
        return DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
    }

}
