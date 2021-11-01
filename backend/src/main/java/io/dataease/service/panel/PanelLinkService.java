package io.dataease.service.panel;

import cn.hutool.http.HttpUtil;
import com.google.gson.Gson;
import io.dataease.auth.config.RsaProperties;
import io.dataease.auth.util.JWTUtils;
import io.dataease.auth.util.RsaUtil;
import io.dataease.base.domain.PanelGroupWithBLOBs;
import io.dataease.base.domain.PanelLink;
import io.dataease.base.mapper.PanelGroupMapper;
import io.dataease.base.mapper.PanelLinkMapper;
import io.dataease.base.mapper.ext.ExtPanelLinkMapper;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.panel.link.EnablePwdRequest;
import io.dataease.controller.request.panel.link.LinkRequest;
import io.dataease.controller.request.panel.link.OverTimeRequest;
import io.dataease.controller.request.panel.link.PasswordRequest;
import io.dataease.dto.panel.link.GenerateDto;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class PanelLinkService {

    private static final String baseUrl = "/link.html?link=";

    @Value("${dataease.public-link-salt:DataEaseLinkSalt}")
    private String salt;

    @Value("${dataease.short-url-site:https://dh6.ink/}")
    private String shortUrlSite;

    @Value("${dataease.short-url-api:api/url/add}")
    private String shortUrlApi;

    @Resource
    private PanelLinkMapper mapper;

    @Resource
    private PanelGroupMapper panelGroupMapper;

    @Resource
    private ExtPanelLinkMapper extPanelLinkMapper;

    public void changeValid(LinkRequest request){
        PanelLink po = new PanelLink();
        po.setResourceId(request.getResourceId());
        po.setValid(request.isValid());
        mapper.updateByPrimaryKeySelective(po);
    }

    public void changeEnablePwd(EnablePwdRequest request){
        PanelLink po = new PanelLink();
        po.setResourceId(request.getResourceId());
        po.setEnablePwd(request.isEnablePwd());
        mapper.updateByPrimaryKeySelective(po);
    }

    public void password(PasswordRequest request){
        PanelLink po = new PanelLink();
        po.setResourceId(request.getResourceId());
        po.setPwd(request.getPassword());
        mapper.updateByPrimaryKeySelective(po);
    }

    public void overTime(OverTimeRequest request) {
        /* PanelLink po = new PanelLink();
        po.setResourceId(request.getResourceId());
        po.setOverTime(request.getOverTime());
        mapper.updateByPrimaryKeySelective(po); */
        extPanelLinkMapper.updateOverTime(request);
    }

    public PanelLink findOne(String resourceId){
        PanelLink panelLink = mapper.selectByPrimaryKey(resourceId);
        return panelLink;
    }

    public GenerateDto currentGenerate(String resourceId) {
        PanelLink one = findOne(resourceId);
        if (ObjectUtils.isEmpty(one)) {
            one = new PanelLink();
            one.setPwd(null);
            one.setResourceId(resourceId);
            one.setValid(false);
            one.setEnablePwd(false);
            mapper.insert(one);
        }
        return convertDto(one);
    }

    public void deleteByResourceId(String resourceId){
        mapper.deleteByPrimaryKey(resourceId);
    }

    public String decryptParam(String text) throws Exception {
        return RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, text);
    }

    // 使用共钥加密
    private String encrypt(String sourceValue) {
        try {
            return RsaUtil.encryptByPublicKey(RsaProperties.publicKey, sourceValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String buildLinkParam(String resourceId){
       /*  Map<String,Object> map = new HashMap<>();
        map.put("resourceId", resourceId);
        map.put("time", System.currentTimeMillis());
        map.put("salt", salt);
        Gson gson = new Gson();
        String encrypt = encrypt(gson.toJson(map)); */
        String encrypt = encrypt(resourceId);
        /* String s = null;
        try {
            s = RsaUtil.decryptByPrivateKey(RsaProperties.privateKey, encrypt);
        } catch (Exception e) {
            e.printStackTrace();
        } */
        return encrypt;
    }
    private GenerateDto convertDto(PanelLink linl){
        GenerateDto result = new GenerateDto();
        result.setValid(linl.getValid());
        result.setEnablePwd(linl.getEnablePwd());
        result.setPwd(linl.getPwd());
        result.setUri(baseUrl+buildLinkParam(linl.getResourceId()));
        result.setOverTime(linl.getOverTime());
        return result;
    }

    // 验证请求头部携带的信息 如果正确说明通过密码验证 否则没有通过
    public Boolean validateHeads(PanelLink panelLink) throws Exception{
        HttpServletRequest request = ServletUtils.request();
        String token = request.getHeader("LINK-PWD-TOKEN");
        if (!panelLink.getEnablePwd() || StringUtils.isEmpty(token) || StringUtils.equals("undefined", token) || StringUtils.equals("null", token)) {
            String resourceId = panelLink.getResourceId();
            String pwd = "dataease";
            String tk = JWTUtils.signLink(resourceId, pwd);
            HttpServletResponse httpServletResponse = ServletUtils.response();
            httpServletResponse.addHeader("Access-Control-Expose-Headers", "LINK-PWD-TOKEN");
            httpServletResponse.setHeader("LINK-PWD-TOKEN", tk);
            return false;
        }
        if (StringUtils.isEmpty(panelLink.getPwd())) return false;
        boolean verify = JWTUtils.verifyLink(token, panelLink.getResourceId(), panelLink.getPwd());
        /* boolean verify = JWTUtils.verifyLink(token, panelLink.getResourceId(), decryptParam(panelLink.getPwd())); */
        return verify;
    }

    // 验证链接是否过期
    public Boolean isExpire(PanelLink panelLink) {
        if (ObjectUtils.isEmpty(panelLink.getOverTime())) {
            return false;
        }
        return System.currentTimeMillis() > panelLink.getOverTime();        
    }

    public boolean validatePwd(PasswordRequest request) throws Exception {
        String password = request.getPassword();
        /* String password = decryptParam(request.getPassword()); */
        String resourceId = request.getResourceId();
        PanelLink one = findOne(resourceId);
        String pwd = one.getPwd();
        /* String pwd = decryptParam(one.getPwd()); */
        boolean pass = StringUtils.equals(pwd, password);
        if (pass){
            String token = JWTUtils.signLink(resourceId, password);
            HttpServletResponse httpServletResponse = ServletUtils.response();
            httpServletResponse.addHeader("Access-Control-Expose-Headers", "LINK-PWD-TOKEN");
            httpServletResponse.setHeader("LINK-PWD-TOKEN", token);
        }
        return pass;
    }

    public PanelGroupWithBLOBs resourceInfo(String resourceId) {
        return panelGroupMapper.selectByPrimaryKey(resourceId);
    }


    public ResultHolder getShortUrl(String url) {
        Gson gson = new Gson();
        Map param = new HashMap<>();
        param.put("diy", false);
        param.put("link", url);
        param.put("sort", "");
        String post = HttpUtil.post(shortUrlSite + shortUrlApi, param);
        try{
            Map map = gson.fromJson(post, Map.class);
            Map data = (Map) map.get("data");
            String sort = shortUrlSite + data.get("sort").toString();
            ResultHolder success = ResultHolder.success(sort);
            return success;
        }catch (Exception e) {
            ResultHolder error = ResultHolder.error(e.getMessage());
            return error;
        }

    }
}
