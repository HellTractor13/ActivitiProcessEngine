package com.bpmsystem.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.activiti.api.runtime.shared.identity.UserGroupManager;
import java.util.Collections;
import java.util.List;

@Configuration
public class ActivitiConfig {

    @Bean
    public UserGroupManager userGroupManager() {
        return new UserGroupManager() {
            @Override
            public List<String> getUserGroups(String userId) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getUserRoles(String userId) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getGroups() {
                return Collections.emptyList();
            }

            @Override
            public List<String> getUsers() {
                return Collections.emptyList();
            }
        };
    }
}