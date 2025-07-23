package com.bpmsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// BpmSystemApplication.java
@SpringBootApplication(exclude = org.activiti.core.common.spring.identity.config.ActivitiSpringIdentityAutoConfiguration.class)
public class BpmSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(BpmSystemApplication.class, args);
    }
}