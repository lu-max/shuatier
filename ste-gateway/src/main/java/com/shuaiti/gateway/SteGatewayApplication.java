package com.shuaiti.gateway;

import com.shuaiti.gateway.config.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableConfigurationProperties(JwtProperties.class)
@SpringBootApplication
public class SteGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteGatewayApplication.class, args);
    }

}
