package io.dataease.substitute.permissions.login;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import io.dataease.auth.config.SubstituleLoginConfig;
import io.dataease.auth.vo.TokenVO;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.utils.LogUtil;
import io.dataease.utils.Md5Utils;
import io.dataease.utils.RsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

@Component
@ConditionalOnMissingBean(name = "loginServer")
@RestController
@RequestMapping
public class SubstituleLoginServer {

    @PostMapping("/login/localLogin")
    public TokenVO localLogin(@RequestBody PwdLoginDTO dto) {

        String name = dto.getName();
        name = RsaUtils.decryptStr(name);
        String pwd = dto.getPwd();
        pwd = RsaUtils.decryptStr(pwd);

        dto.setName(name);
        dto.setPwd(pwd);

        if (!StringUtils.equals("admin", name)) {
            DEException.throwException("仅admin账号可用");
        }
        if (!StringUtils.equals(pwd, SubstituleLoginConfig.getPwd())) {
            DEException.throwException(Translator.get("i18n_login_name_pwd_err"));
        }
        String md5Pwd = Md5Utils.md5(pwd);
        return generate(1L, md5Pwd);
    }


    @GetMapping("/logout")
    public void logout() {
        LogUtil.info("substitule logout");
    }

    private TokenVO generate(Long userId, String secret) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTCreator.Builder builder = JWT.create();
        builder.withClaim("uid", userId);
        String token = builder.sign(algorithm);
        return new TokenVO(token, 0L);
    }
}
