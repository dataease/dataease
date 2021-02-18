package com.fit2cloud.system.api;


import com.fit2cloud.common.db.PageUtils;
import com.fit2cloud.common.db.Pager;
import com.fit2cloud.commons.auth.entity.User;
import com.fit2cloud.commons.auth.mapper.UserMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class BusiApi {

    @GetMapping("/busi/dashboard")
    public Object dashboard(){
        return "dashboard";
    }


    @Autowired
    private UserMapper userMapper;

    @GetMapping("/page")
    public Object list(){
        int startpage = 1;
        int limit = 10;
        Page<Object> page = PageHelper.startPage(startpage, limit);
        List<User> users = userMapper.selectList(null);
        Pager<List<User>> pageInfo = PageUtils.setPageInfo(page, users);
        return pageInfo;
    }
}
