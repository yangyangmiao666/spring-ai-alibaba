package com.alibaba.cloud.ai.example.graph.react.tool;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ToolsConfig {

    private final ApplicationContext context;

    public ToolsConfig(ApplicationContext context) {
        this.context = context;
    }

    @Bean
    public ToolCallbackProvider userTools() {
        return MethodToolCallbackProvider.builder().toolObjects(
                context.getBeansWithAnnotation(LocalTool.class).values().toArray()
        ).build();
    }

}
