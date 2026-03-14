package com.shuaitier.user;

import com.shuaitier.user.config.JwtProperties;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@MapperScan("com.shuaitier.user.mapper")
@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class SteUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteUserApplication.class, args);
        log.info("用户模块启动完成");
    }
}
