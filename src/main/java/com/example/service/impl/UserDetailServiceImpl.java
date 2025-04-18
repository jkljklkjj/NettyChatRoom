package com.example.service.impl;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.model.mysql.User;
import com.example.service.mysql.UserService;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserService UserService;

    public UserDetailServiceImpl(com.example.service.mysql.UserService userService) {
        UserService = userService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = UserService.getUserById(Integer.parseInt(username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return new org.springframework.security.core.userdetails.User(String.valueOf(user.getId()), user.getPassword(), new ArrayList<>());
    }
}
