package com.rpc.service.impl;

import org.springframework.stereotype.Service;

import com.rpc.service.User;
import com.rpc.service.UserService;

@Service("userService")
public class UserServiceImpl implements UserService{

	@Override
	public User addUser(User user) {
		user.setId(200);
		System.out.println("==========receive User:" + user.getName());
		return user;
	}

}
