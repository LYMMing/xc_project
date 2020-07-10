package com.xuecheng.govern.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer //加载eurekaServer相关的类
@SpringBootApplication
public class GovernCenterApplicaion {
    public static void main(String[] args) {
        SpringApplication.run(GovernCenterApplicaion.class);
    }
}
