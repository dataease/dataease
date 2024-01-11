package io.dataease.api.sync.summary.api;

import io.dataease.auth.DeApiPath;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.SUMMARY;

/**
 * @author fit2cloud
 * @date 2023/12/4 12:43
 **/
@DeApiPath(value = "/sync/summary", rt = SUMMARY)
public interface SummaryApi {

    @GetMapping("/resourceCount")
    Map<String, Long> resourceCount();

    @PostMapping("/logChartData")
    Map<String, Object> logChartData(@RequestBody String executeTaskLogDate);


}
