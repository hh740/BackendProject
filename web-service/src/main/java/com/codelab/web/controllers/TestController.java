package com.codelab.web.controllers;

import com.codelab.common.service.EmailService;
import com.codelab.web.services.ExampleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by wangke on 16/4/6.
 * 本例是展示一个简单的分层式的服务结构:
 * 各个组件为可插拔式,需在pom中配置属性之后,创建*.properties文件,每个组件会扫描属性配置文件
 */
@Controller
@RequestMapping("/test")
public class TestController {

    @Resource
    private ExampleService exampleService;

    @Resource
    private EmailService emailService;

    Logger logger = LoggerFactory.getLogger(TestController.class);

    @RequestMapping("/mybatis")
    public void testPath(HttpServletResponse response, HttpServletRequest request) throws IOException {

        String sb = exampleService.getUsers();
        response.getWriter().write(sb.toString());

    }

    @RequestMapping("/mail/{sender}")
    public void sendMail(@PathVariable("sender") String sender) throws Exception {

        logger.debug("sender:{}",sender);

       // emailService.sendEmail(sender,"707071062@qq.com","hello","hello world",false,false);

    }

}
