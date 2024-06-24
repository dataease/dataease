package io.dataease.home;

import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.ModelUtils;
import io.dataease.utils.RsaUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class RestIndexController {

    @Value("${dataease.xpack-front-distributed:false}")
    private boolean xpackFrontDistributed;

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


    @GetMapping("/xpackModel")
    @ResponseBody
    public boolean xpackModel() {
        return xpackFrontDistributed && LicenseUtil.licenseValid();
    }

}
