package io.dataease.controller;

import io.dataease.commons.utils.SessionUtils;
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
        if (SessionUtils.getUser() == null) {
            return "login.html";
        } else {
            return "redirect:/";
        }
    }
}
