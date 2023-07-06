package io.dataease.home;

import io.dataease.utils.ModelUtils;
import io.dataease.utils.RsaUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RestIndexController {

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
}
