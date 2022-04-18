package io.datains.auth.util;

import io.datains.base.domain.PanelLink;
import io.datains.service.panel.PanelLinkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LinkUtil {


    private static PanelLinkService panelLinkService;

    @Autowired
    public void setPanelLinkService(PanelLinkService panelLinkService) {
        LinkUtil.panelLinkService = panelLinkService;
    }

    public static PanelLink queryLink(String resourceId, Long user) {
        return panelLinkService.findOne(resourceId, user);
    }
}
