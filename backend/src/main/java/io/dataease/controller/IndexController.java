package io.dataease.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class IndexController {

    @GetMapping(value = "/")
    public String index() {
        return "index.html";
    }

    @GetMapping(value = "/login")
    public String login() {
        return "index.html";
    }

    @GetMapping("/link")
    public String link() {
        return "link.html";
    }

    @GetMapping("/test")
    public String test() {
        return "test.html";
    }


}
