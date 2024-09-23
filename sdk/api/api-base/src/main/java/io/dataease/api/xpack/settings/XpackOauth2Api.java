package io.dataease.api.xpack.settings;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.xpack.settings.request.XpackOauth2TokenRequest;
import io.dataease.api.xpack.settings.vo.XpackOauthAuthVO;
import io.dataease.api.xpack.settings.vo.XpackOauthTokenVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Oauth2认证")
@ApiSupport(order = 899)
public interface XpackOauth2Api {

    @GetMapping("/auth")
    XpackOauthAuthVO auth();

    @PostMapping("/token")
    XpackOauthTokenVO oauth2Token(@RequestBody XpackOauth2TokenRequest request);

}
