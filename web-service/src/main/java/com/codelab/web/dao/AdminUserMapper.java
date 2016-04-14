package com.codelab.web.dao;


import com.codelab.web.entity.User;

import java.util.List;

public interface AdminUserMapper {

	public List<User> findAll();

}
