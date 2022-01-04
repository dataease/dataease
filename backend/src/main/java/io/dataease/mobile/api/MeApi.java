package io.dataease.mobile.api;

import io.dataease.mobile.dto.MeItemDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;



@RequestMapping("/mobile/me")
public interface MeApi {

    @PostMapping("/query")
    MeItemDTO query();
}
