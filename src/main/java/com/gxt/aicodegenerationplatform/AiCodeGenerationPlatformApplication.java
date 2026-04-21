package com.gxt.aicodegenerationplatform;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@MapperScan("com.gxt.aicodegenerationplatform.mapper")
@EnableAspectJAutoProxy(exposeProxy = true)
public class AiCodeGenerationPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiCodeGenerationPlatformApplication.class, args);
    }

}
