package io.dataease.system.api;


/*import com.fit2cloud.commons.auth.entity.User;
import com.fit2cloud.commons.auth.mapper.UserMapper;*/
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BusiApi {

    @GetMapping("/busi/dashboard")
    public Object dashboard(){
        return "dashboard";
    }


    /*@Autowired
    private UserMapper userMapper;

    @GetMapping("/page")
    public Object list(){
        int startpage = 1;
        int limit = 10;
        Page<Object> page = PageHelper.startPage(startpage, limit);
        List<User> users = userMapper.selectList(null);
        Pager<List<User>> pageInfo = PageUtils.setPageInfo(page, users);
        return pageInfo;
    }*/
}
