package io.dataease.api.xpack.share;

import io.dataease.api.xpack.share.request.TicketCreator;
import io.dataease.api.xpack.share.request.TicketDelRequest;
import io.dataease.api.xpack.share.request.TicketSwitchRequest;
import io.dataease.api.xpack.share.vo.TicketVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "分享:TICKET")
public interface ShareTicketApi {

    @PostMapping("/saveTicket")
    String saveTicket(@RequestBody TicketCreator creator);

    @PostMapping("/delTicket")
    void deleteTicket(@RequestBody TicketDelRequest request);

    @PostMapping("/enableTicket")
    void switchRequire(@RequestBody TicketSwitchRequest request);

    @GetMapping("/query/{resourceId}")
    List<TicketVO> query(@PathVariable("resourceId") Long resourceId);
}
