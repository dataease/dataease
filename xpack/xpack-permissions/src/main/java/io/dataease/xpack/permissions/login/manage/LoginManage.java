package io.dataease.xpack.permissions.login.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.permissions.login.dto.PwdLoginDTO;
import io.dataease.exception.DEException;
import io.dataease.utils.Md5Utils;
import io.dataease.utils.RsaUtils;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.xpack.permissions.login.dao.LoginMapper;
import io.dataease.xpack.permissions.login.dao.po.LoginUserPO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

@Component
public class LoginManage {
    @Resource
    private LoginMapper loginMapper;

    public TokenUserBO localBuild(PwdLoginDTO dto) {
        String name = dto.getName();
        name = RsaUtils.decryptStr(name);
        String pwd = dto.getPwd();
        pwd = RsaUtils.decryptStr(pwd);
        String md5Pwd = Md5Utils.md5(pwd);
        QueryWrapper<LoginUserPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account", name).eq("pwd", md5Pwd);
        LoginUserPO loginUserPO;
        loginUserPO = loginMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(loginUserPO)) {
            queryWrapper.clear();
            queryWrapper.eq("phone", name).eq("pwd", md5Pwd);
            loginUserPO = loginMapper.selectOne(queryWrapper);
        }
        if (ObjectUtils.isEmpty(loginUserPO)) {
            queryWrapper.clear();
            queryWrapper.eq("email", name).eq("pwd", md5Pwd);
            loginUserPO = loginMapper.selectOne(queryWrapper);
        }
        if (ObjectUtils.isEmpty(loginUserPO)) {
            DEException.throwException("name or pwd invalid");
        }
        return new TokenUserBO(loginUserPO.getUserId(), loginUserPO.getDefaultOid());
    }
}
