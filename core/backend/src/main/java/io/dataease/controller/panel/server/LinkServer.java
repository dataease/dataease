package io.dataease.controller.panel.server;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.DeLogUtils;
import io.dataease.controller.panel.api.LinkApi;
import io.dataease.controller.request.chart.ChartExtRequest;
import io.dataease.controller.request.panel.link.*;
import io.dataease.dto.panel.link.GenerateDto;
import io.dataease.dto.panel.link.ValidateDto;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import io.dataease.plugins.common.base.domain.PanelLink;
import io.dataease.service.chart.ChartViewService;
import io.dataease.service.panel.PanelLinkService;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
public class LinkServer implements LinkApi {

    @Autowired
    private PanelLinkService panelLinkService;

    @Resource
    private ChartViewService chartViewService;

    @Override
    public void replacePwd(@RequestBody PasswordRequest request) {
        panelLinkService.password(request);
    }

    @Override
    public void enablePwd(@RequestBody EnablePwdRequest request) {
        panelLinkService.changeEnablePwd(request);
    }

    @Override
    public void resetOverTime(@RequestBody OverTimeRequest request) {
        panelLinkService.overTime(request);

    }

    @Override
    public void switchLink(@RequestBody LinkRequest request) {
        panelLinkService.changeValid(request);
    }

    @Override
    public GenerateDto currentGenerate(@PathVariable("resourceId") String resourceId) {
        return panelLinkService.currentGenerate(resourceId);
    }

    @Override
    public ValidateDto validate(@RequestBody LinkValidateRequest request) throws Exception {
        String link = request.getLink();
        link = URLDecoder.decode(link, StandardCharsets.UTF_8);
        String json = panelLinkService.decryptParam(link);
        String[] jsonArray = json.split(",");
        String uuid = null;
        int len = jsonArray.length;
        if (len > 1) {
            uuid = jsonArray[1];
        }
        String user = request.getUser();
        user = URLDecoder.decode(user, StandardCharsets.UTF_8);
        user = panelLinkService.decryptParam(user);

        ValidateDto dto = new ValidateDto();
        dto.setUserId(user);
        String resourceId = jsonArray[0];
        PanelLink one = panelLinkService.findOne(resourceId, Long.valueOf(user));
        dto.setResourceId(resourceId);
        if (ObjectUtils.isEmpty(one)) {
            dto.setValid(false);
            return dto;
        }
        String mappingUuid = panelLinkService.getMappingUuid(one);
        if (!StringUtils.equals(uuid, mappingUuid)) {
            dto.setValid(false);
            return dto;
        }
        dto.setValid(one.getValid());
        dto.setEnablePwd(one.getEnablePwd());
        dto.setPassPwd(panelLinkService.validateHeads(one));
        dto.setExpire(panelLinkService.isExpire(one));
        return dto;
    }

    @Override
    public boolean validatePwd(@RequestBody PasswordRequest request) throws Exception {
        return panelLinkService.validatePwd(request);
    }

    @Override
    public Object resourceDetail(@PathVariable String resourceId, @PathVariable String userId) {
        return panelLinkService.resourceInfo(resourceId, userId);
    }

    @Override
    public Object viewDetail(String viewId, String panelId, ChartExtRequest requestList) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String linkToken = request.getHeader(F2CLinkFilter.LINK_TOKEN_KEY);
        DecodedJWT jwt = JWT.decode(linkToken);
        Long userId = jwt.getClaim("userId").asLong();
        requestList.setUser(userId);
        return chartViewService.getData(viewId, requestList);
    }

    @Override
    public String shortUrl(Map<String, String> param) {
        String resourceId = param.get("resourceId");
        return panelLinkService.getShortUrl(resourceId);
    }

    @Override
    public void viewLinkLog(LinkViewLogRequest request) {
        String panelId = request.getPanelId();
        Boolean mobile = request.getMobile();
        Long userId = request.getUserId();
        SysLogConstants.OPERATE_TYPE operateType = SysLogConstants.OPERATE_TYPE.PC_VIEW;
        if (ObjectUtils.isNotEmpty(mobile) && mobile) {
            operateType = SysLogConstants.OPERATE_TYPE.MB_VIEW;
        }
        if (ObjectUtils.isEmpty(userId)) return;
        PanelGroupWithBLOBs panelGroupWithBLOBs = panelLinkService.resourceInfo(panelId, String.valueOf(userId));
        String pid = panelGroupWithBLOBs.getPid();
        DeLogUtils.save(operateType, SysLogConstants.SOURCE_TYPE.LINK, panelId, pid, userId, SysLogConstants.SOURCE_TYPE.USER);
    }
}
