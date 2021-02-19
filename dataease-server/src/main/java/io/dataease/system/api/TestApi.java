package io.dataease.system.api;




import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
public class TestApi {



    @GetMapping("/test")
    public Object test(){
        return "apple";
    }

    @GetMapping("/test1")
    public Map<String, String> test1(){

        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "a");
        map.put("2", "b");
        return map;
    }

    @RequiresPermissions("")
    @GetMapping("/test2")
    public Map<String, String> test2(){

        //Map<String, String> map = new HashMap<String, String>();
        Map<String, String> map = null;

        try {
            map.put("1", "a");
            map.put("2", "b");
        }catch (Exception e){
            e.printStackTrace();
        }

        return map;
    }







}
