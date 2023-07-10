package io.dataease.home;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
public class IndexController {

    private static final String INDEX_PAGE = "index.html";
    private static final String PANEL_PAGE = "panel.html";

    @GetMapping("/")
    public String index() {
        return INDEX_PAGE;
    }

    @GetMapping("/panel")
    public String panel() {
        return PANEL_PAGE;
    }


}
