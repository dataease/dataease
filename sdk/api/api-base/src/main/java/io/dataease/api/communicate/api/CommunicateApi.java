package io.dataease.api.communicate.api;

import io.dataease.api.communicate.dto.MessageDTO;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Hidden
public interface CommunicateApi {

    @PostMapping("/send")
    void send(@RequestBody MessageDTO dto);
}
