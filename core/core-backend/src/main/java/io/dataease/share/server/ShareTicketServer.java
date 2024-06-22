package io.dataease.share.server;

import io.dataease.api.xpack.share.ShareTicketApi;
import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.api.xpack.share.request.TicketDelRequest;
import io.dataease.api.xpack.share.request.TicketSwitchRequest;
import io.dataease.api.xpack.share.vo.TicketVO;
import io.dataease.share.manage.ShareTicketManage;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/ticket")
public class ShareTicketServer implements ShareTicketApi {

    @Resource
    private ShareTicketManage shareTicketManage;

    @Override
    public String saveTicket(TicketCreator creator) {
        return shareTicketManage.saveTicket(creator);
    }

    @Override
    public void deleteTicket(TicketDelRequest request) {
        shareTicketManage.deleteTicket(request);
    }

    @Override
    public void switchRequire(TicketSwitchRequest request) {
        shareTicketManage.switchRequire(request);
    }

    @Override
    public List<TicketVO> query(Long resourceId) {
        return shareTicketManage.query(resourceId);
    }
}
