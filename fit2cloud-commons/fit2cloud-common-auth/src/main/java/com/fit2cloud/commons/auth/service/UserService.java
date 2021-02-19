package com.fit2cloud.commons.auth.service;



import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fit2cloud.commons.auth.bean.UserBean;
import com.fit2cloud.commons.auth.dao.ExtUserMapper;
import com.fit2cloud.commons.auth.entity.SysUser;
import com.fit2cloud.commons.auth.mapper.SysUserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired(required = false)
    private ExtUserMapper extUserMapper;

    @Autowired(required = false)
    private SysUserMapper sysUserMapper;
    public UserBean getUser(String username){

        SysUser sysUser = extUserMapper.getUser(username);

        if (ObjectUtil.isNull(sysUser))return null;
        String password = sysUser.getPassword();
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
