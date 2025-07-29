package io.dataease.copilot.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.copilot.dto.ChartDTO;
import io.dataease.api.copilot.dto.HistoryDTO;
import io.dataease.api.copilot.dto.MsgDTO;
import io.dataease.copilot.dao.auto.entity.CoreCopilotMsg;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotMsgRepository;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author Junjun
 */
@Component
public class MsgManage {
    @Resource
    private CoreCopilotMsgRepository coreCopilotMsgRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void save(MsgDTO msgDTO) throws Exception {
        msgDTO.setId(IDUtils.snowID());
        msgDTO.setCreateTime(System.currentTimeMillis());
        msgDTO.setUserId(AuthUtils.getUser().getUserId());
        coreCopilotMsgRepository.saveAndFlush(transDTO(msgDTO));
    }

    public List<MsgDTO> getMsg(MsgDTO msgDTO) {
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgRepository.findByUserIdAndDatasetGroupIdOrderByCreateTimeAsc(
                msgDTO.getUserId(), msgDTO.getDatasetGroupId());
        return perCopilotMsgs.stream().map(this::transRecord).toList();
    }

    public void deleteMsg(MsgDTO msgDTO) {
        coreCopilotMsgRepository.deleteByUserIdAndNotDatasetGroupId(msgDTO.getUserId(), msgDTO.getDatasetGroupId());
    }

    public void clearAllUserMsg(Long userId) {
        coreCopilotMsgRepository.deleteByUserId(userId);
    }

    public MsgDTO getLastMsg(Long userId) {
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return ObjectUtils.isEmpty(perCopilotMsgs) ? null : transRecord(perCopilotMsgs.getFirst());
    }

    public MsgDTO getLastSuccessMsg(Long userId, Long datasetGroupId) {
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgRepository.findLastSuccessMsg(userId, datasetGroupId);
        return ObjectUtils.isEmpty(perCopilotMsgs) ? null : transRecord(perCopilotMsgs.getFirst());
    }

    private CoreCopilotMsg transDTO(MsgDTO dto) throws Exception {
        CoreCopilotMsg record = new CoreCopilotMsg();
        BeanUtils.copyBean(record, dto);
        record.setHistory(dto.getHistory() == null ? null : objectMapper.writeValueAsString(dto.getHistory()));
        record.setChart(dto.getChart() == null ? null : objectMapper.writeValueAsString(dto.getChart()));
        record.setChartData(dto.getChartData() == null ? null : objectMapper.writeValueAsString(dto.getChartData()));
        return record;
    }

    private MsgDTO transRecord(CoreCopilotMsg record) {
        MsgDTO dto = new MsgDTO();
        BeanUtils.copyBean(dto, record);
        TypeReference<List<HistoryDTO>> tokenType = new TypeReference<>() {
        };
        dto.setHistory(record.getHistory() == null ? new ArrayList<>() : JsonUtil.parseList(record.getHistory(), tokenType));
        dto.setChart(record.getChart() == null ? null : JsonUtil.parseObject(record.getChart(), ChartDTO.class));
        dto.setChartData(record.getChartData() == null ? null : JsonUtil.parse(record.getChartData(), Map.class));
        return dto;
    }
}
