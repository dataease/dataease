package com.fit2cloud.commons.auth.service;


import cn.hutool.core.util.ObjectUtil;
import com.fit2cloud.commons.auth.bean.UserBean;
import com.fit2cloud.commons.auth.dao.ExtUserMapper;
import com.fit2cloud.commons.auth.entity.User;
import com.fit2cloud.commons.auth.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired(required = false)
    private ExtUserMapper extUserMapper;

    @Autowired(required = false)
    private UserMapper userMapper;
    public UserBean getUser(String username){
        User user = userMapper.selectById(username);
        if (ObjectUtil.isNull(user))return null;
        String password = user.getPassword();
        List<String> roles = extUserMapper.getRole(username);
        String role = roles.stream().collect(Collectors.joining(","));
        List<String> permissions = extUserMapper.getPermission(username);
        String permission = permissions.stream().collect(Collectors.joining(","));
        UserBean userBean = UserBean.builder().username(username).password(password).role(role).permission(permission).build();
        return userBean;
    }

    public String getPassword(String username){
        String password = extUserMapper.getPassword(username);
        return password;
    }


}
