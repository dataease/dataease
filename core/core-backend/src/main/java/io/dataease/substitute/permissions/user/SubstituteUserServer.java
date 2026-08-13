package io.dataease.substitute.permissions.user;

import io.dataease.api.permissions.user.dto.LangSwitchRequest;
import io.dataease.api.permissions.user.vo.CurIpVO;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.exception.DEException;
import io.dataease.i18n.Lang;
import io.dataease.utils.CacheUtils;
import io.dataease.utils.IPUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

import static io.dataease.constant.CacheConstant.UserCacheConstant.USER_COMMUNITY_LANGUAGE;

@Component
@ConditionalOnMissingBean(name = "userServer")
@RestController
@RequestMapping("/user")
public class SubstituteUserServer {

    @GetMapping("/info")
    public Map<String, Object> info() {
        Map<String, Object> result = new HashMap<>();
        result.put("id", "1");
        result.put("name", "管理员");
        result.put("oid", "1");
        result.put("language", "zh-CN");
        Object langObj = CacheUtils.get(USER_COMMUNITY_LANGUAGE, "de");
        if (ObjectUtils.isNotEmpty(langObj) && StringUtils.isNotBlank(langObj.toString())) {
            result.put("language", langObj.toString());
        }
        return result;
    }

    @GetMapping("/personInfo")
    public UserFormVO personInfo() {
        UserFormVO userFormVO = new UserFormVO();
        userFormVO.setId(1L);
        userFormVO.setAccount("admin");
        userFormVO.setName("管理员");
        userFormVO.setIp(IPUtils.get());
        // 当前模式为无XPack
        userFormVO.setModel("lose");
        return userFormVO;
    }

    @GetMapping("/ipInfo")
    public CurIpVO ipInfo() {
        CurIpVO curIpVO = new CurIpVO();
        curIpVO.setAccount("admin");
        curIpVO.setName("管理员");
        curIpVO.setIp(IPUtils.get());
        return curIpVO;
    }

    @PostMapping("/switchLanguage")
    public void switchLanguage(@RequestBody LangSwitchRequest request) {
        String lang = request.getLang();
        if (StringUtils.equalsIgnoreCase(Lang.zh_CN.getDesc(), lang)) {
            lang = Lang.zh_CN.getDesc();
        } else if (StringUtils.equalsAnyIgnoreCase(lang, "en", "tw")) {
            lang = lang.toLowerCase();
        } else {
            DEException.throwException("无效language");
        }
        CacheUtils.put(USER_COMMUNITY_LANGUAGE, "de", lang);
    }
}
