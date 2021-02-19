package io.dataease.system.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class SysApi {


    @GetMapping("/sys/manager")
    public Object manager(){
        return "manager";
    }
}
