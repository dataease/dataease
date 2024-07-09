package io.dataease.copilot.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.copilot.dto.ChartDTO;
import io.dataease.api.copilot.dto.HistoryDTO;
import io.dataease.api.copilot.dto.MsgDTO;
import io.dataease.copilot.dao.auto.entity.CoreCopilotMsg;
import io.dataease.copilot.dao.auto.mapper.CoreCopilotMsgMapper;
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
    private CoreCopilotMsgMapper coreCopilotMsgMapper;

    private ObjectMapper objectMapper = new ObjectMapper();

    public void save(MsgDTO msgDTO) throws Exception {
        msgDTO.setId(IDUtils.snowID());
        msgDTO.setCreateTime(System.currentTimeMillis());
        msgDTO.setUserId(AuthUtils.getUser().getUserId());
        coreCopilotMsgMapper.insert(transDTO(msgDTO));
    }

    public List<MsgDTO> getMsg(MsgDTO msgDTO) {
        QueryWrapper<CoreCopilotMsg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", msgDTO.getUserId());
        wrapper.eq("dataset_group_id", msgDTO.getDatasetGroupId());
        wrapper.orderByAsc("create_time");
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgMapper.selectList(wrapper);
        return perCopilotMsgs.stream().map(this::transRecord).toList();
    }

    public void deleteMsg(MsgDTO msgDTO) {
        QueryWrapper<CoreCopilotMsg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", msgDTO.getUserId());
        wrapper.ne("dataset_group_id", msgDTO.getDatasetGroupId());
        coreCopilotMsgMapper.delete(wrapper);
    }

    public void clearAllUserMsg(Long userId) {
        QueryWrapper<CoreCopilotMsg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        coreCopilotMsgMapper.delete(wrapper);
    }

    public MsgDTO getLastMsg(Long userId) {
        QueryWrapper<CoreCopilotMsg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.orderByDesc("create_time");
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgMapper.selectList(wrapper);
        return ObjectUtils.isEmpty(perCopilotMsgs) ? null : transRecord(perCopilotMsgs.getFirst());
    }

    public MsgDTO getLastSuccessMsg(Long userId, Long datasetGroupId) {
        QueryWrapper<CoreCopilotMsg> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId);
        wrapper.eq("dataset_group_id", datasetGroupId);
        wrapper.eq("msg_status", 1);
        wrapper.eq("msg_type", "api");
        wrapper.orderByDesc("create_time");
        List<CoreCopilotMsg> perCopilotMsgs = coreCopilotMsgMapper.selectList(wrapper);
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
