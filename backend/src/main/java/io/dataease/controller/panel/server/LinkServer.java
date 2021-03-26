package io.dataease.controller.panel.server;


import com.google.gson.Gson;
import io.dataease.base.domain.PanelLink;
import io.dataease.controller.panel.api.LinkApi;
import io.dataease.controller.request.panel.link.EnablePwdRequest;
import io.dataease.controller.request.panel.link.LinkRequest;
import io.dataease.controller.request.panel.link.PasswordRequest;
import io.dataease.controller.request.panel.link.ValidateRequest;
import io.dataease.dto.panel.link.GenerateDto;
import io.dataease.dto.panel.link.ValidateDto;
import io.dataease.service.panel.PanelLinkService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;


@RestController
public class LinkServer implements LinkApi {



    @Autowired
    private PanelLinkService panelLinkService;


    @Override
    public void replacePwd(@RequestBody PasswordRequest request) {
        panelLinkService.password(request);
    }

    @Override
    public void enablePwd(@RequestBody EnablePwdRequest request) {
        panelLinkService.changeEnablePwd(request);
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
    public ValidateDto validate(@RequestBody Map<String, String> param)  {
        String link = param.get("link");
        String json = null;
        try {
            json = panelLinkService.decryptParam(link);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();

        ValidateRequest request = gson.fromJson(json, ValidateRequest.class);
        ValidateDto dto = new ValidateDto();
        String resourceId = request.getResourceId();
        PanelLink one = panelLinkService.findOne(resourceId);
        if (ObjectUtils.isEmpty(one)){
            dto.setValid(false);
            return dto;
        }
        dto.setValid(one.getValid());
        dto.setEnablePwd(one.getEnablePwd());
        dto.setPassPwd(panelLinkService.validateHeads(one));
        return dto;
    }

    @Override
    public boolean validatePwd(@RequestBody PasswordRequest request) {
        return panelLinkService.validatePwd(request);
    }
}
