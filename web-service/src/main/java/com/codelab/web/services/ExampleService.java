package com.codelab.web.services;

import com.codelab.web.dao.AdminUserMapper;
import com.codelab.web.entity.User;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by wangke on 16/4/12.
 */
@Service
public class ExampleService {


    Logger logger = LoggerFactory.getLogger(ExampleService.class);

    @Resource
    private AdminUserMapper adminUserMapper;

    @Transactional
    public String getUsers(){

        List<User> users = adminUserMapper.findAll();

        StringBuilder sb = new StringBuilder();

        for(User user:users){
            sb.append(user).append("\n");
        }
        return sb.toString();

    }

    public void sechduler(){

        logger.info("shchduler executor");

    }


}
