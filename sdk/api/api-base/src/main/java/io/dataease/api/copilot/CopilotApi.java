package io.dataease.api.copilot;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.copilot.dto.MsgDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @Author Junjun
 */
@Tag(name = "Copilot")
@ApiSupport(order = 999)
public interface CopilotApi {
    @Operation(summary = "发起一次对话")
    @PostMapping("chat")
    MsgDTO chat(@RequestBody MsgDTO msgDTO) throws Exception;

    @Operation(summary = "获取对话记录")
    @PostMapping("getList")
    List<MsgDTO> getList() throws Exception;

    @Operation(summary = "清空对话")
    @PostMapping("clearAll")
    void clearAll() throws Exception;
}
