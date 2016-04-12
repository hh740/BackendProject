package com.test.services;

import com.test.dao.AdminUserMapper;
import com.test.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by wangke on 16/4/12.
 */
@Service
public class TestService {

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
}
