package io.dataease.controller.panel.server;

import io.dataease.base.domain.PanelLink;
import io.dataease.commons.utils.ServletUtils;
import io.dataease.controller.panel.api.LinkApi;
import io.dataease.controller.request.panel.link.LinkRequest;
import io.dataease.controller.request.panel.link.PasswordRequest;
import io.dataease.service.panel.PanelLinkService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class LinkServer implements LinkApi {



    @Autowired
    private PanelLinkService panelLinkService;


    @Override
    public void replacePwd(@RequestBody PasswordRequest request) {
        panelLinkService.password(request);
    }

    @Override
    public void generateLink(@RequestBody LinkRequest request) {
        panelLinkService.generator(request);
    }

}
