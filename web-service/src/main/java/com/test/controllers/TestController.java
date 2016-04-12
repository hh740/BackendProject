package com.test.controllers;

import com.test.dao.AdminUserMapper;
import com.test.entity.User;
import com.test.services.TestService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * Created by wangke on 16/4/6.
 * 本例是展示一个简单的分层式的服务结构:
 * 各个组件为可插拔式,需在pom中配置属性之后,创建*.properties文件,每个组件会扫描属性配置文件
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @Resource
    private TestService testService;

    @RequestMapping("/mybatis")
    public void testPath(HttpServletResponse response, HttpServletRequest request) throws IOException {

        String sb = testService.getUsers();
        response.getWriter().write(sb.toString());

    }

}
