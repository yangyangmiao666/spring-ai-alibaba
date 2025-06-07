package com.alibaba.cloud.ai.example.graph.react.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@LocalTool
public class UserTool {

    @Tool(name = "getUserInfoByName", description = "根据用户名获取用户信息")
    public String getUserInfoByName(String name) {
        return "张三 zhangsan.qq.com";
    }

    @Tool(name = "getAllUser", description = "获取所有用户信息")
    public String getAllUser() {
        return "张三 zhangsan.qq.com\n李四 lisi.qq.com";
    }
}
