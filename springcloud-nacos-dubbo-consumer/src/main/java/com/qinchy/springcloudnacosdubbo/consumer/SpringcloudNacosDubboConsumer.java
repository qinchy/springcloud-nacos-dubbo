package com.qinchy.springcloudnacosdubbo.consumer;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableDubbo
public class SpringcloudNacosDubboConsumer {
    public static void main(String[] args) {
        SpringApplication.run(SpringcloudNacosDubboConsumer.class);
    }
}
