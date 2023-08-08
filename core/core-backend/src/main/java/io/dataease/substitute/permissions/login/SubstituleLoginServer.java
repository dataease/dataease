package io.dataease.substitute.permissions.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.utils.LogUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Component
@ConditionalOnMissingBean(name = "userServer")
@RestController
@RequestMapping
public class SubstituleLoginServer{

    @PostMapping("/login/localLogin")
    public String localLogin(PwdLoginDTO dto) {
        TokenUserBO tokenUserBO = new TokenUserBO();
        tokenUserBO.setUserId(1L);
        tokenUserBO.setDefaultOid(1L);
        String md5Pwd = "83d923c9f1d8fcaa46cae0ed2aaa81b5";
        return generate(tokenUserBO, md5Pwd);
    }


    @GetMapping("/logout")
    public void logout() {
        LogUtil.info("substitule logout");
    }

    private String generate(TokenUserBO bo, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        Long userId = bo.getUserId();
        Long defaultOid = bo.getDefaultOid();
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", userId).withClaim("oid", defaultOid);
        return builder.sign(algorithm);
    }
}
