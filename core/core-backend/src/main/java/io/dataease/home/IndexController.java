package io.dataease.home;

import io.dataease.utils.ModelUtils;
import io.dataease.utils.RsaUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

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

    @GetMapping("/dekey")
    @ResponseBody
    public String dekey() {
        return RsaUtils.publicKey();
    }

    @GetMapping("/model")
    @ResponseBody
    public boolean model() {
        return ModelUtils.isDesktop();
    }

    @GetMapping("/oidc/callback")
    public String oidcCallback() {
        return INDEX_PAGE;
    }
}
