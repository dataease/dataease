package io.dataease.copilot.service;

import io.dataease.api.copilot.CopilotApi;
import io.dataease.api.copilot.dto.MsgDTO;
import io.dataease.copilot.manage.CopilotManage;
import io.dataease.utils.AuthUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author Junjun
 */
@RestController
@RequestMapping("copilot")
public class CopilotService implements CopilotApi {
    @Resource
    private CopilotManage copilotManage;

    @Override
    public MsgDTO chat(MsgDTO msgDTO) throws Exception {
        try {
            return copilotManage.chat(msgDTO);
        } catch (Exception e) {
            return copilotManage.errorMsg(msgDTO, e.getMessage());
        }
    }

    @Override
    public List<MsgDTO> getList() throws Exception {
        return copilotManage.getList(AuthUtils.getUser().getUserId());
    }

    @Override
    public void clearAll() throws Exception {
        copilotManage.clearAll(AuthUtils.getUser().getUserId());
    }
}
