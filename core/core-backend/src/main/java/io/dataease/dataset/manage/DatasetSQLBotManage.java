package io.dataease.dataset.manage;

import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.vo.DataSQLBotAssistantVO;
import io.dataease.api.dataset.vo.SQLBotAssistanTable;
import io.dataease.api.dataset.vo.SQLBotAssistantField;
import io.dataease.commons.utils.EncryptUtils;
import io.dataease.dataset.dao.ext.mapper.DataSetAssistantMapper;
import io.dataease.extensions.datasource.vo.Configuration;
import io.dataease.utils.JsonUtil;
import io.dataease.utils.LogUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DatasetSQLBotManage {

    @Resource
    private DataSetAssistantMapper dataSetAssistantMapper;


    public List<DataSQLBotAssistantVO> getDatasourceList(List<Long> ids) {
        List<Map<String, Object>> list = dataSetAssistantMapper.query();

        List<DataSQLBotAssistantVO> result = new ArrayList<>();
        Map<String, DataSQLBotAssistantVO> dsFlagMap = new HashMap<>();
        Map<String, SQLBotAssistanTable> tableFlagMap = new HashMap<>();
        Map<String, SQLBotAssistantField> fieldFlagMap = new HashMap<>();
        for (Map<String, Object> row : list) {
            // build ds
            String datasourceId = row.get("datasource_id").toString();
            DataSQLBotAssistantVO vo = dsFlagMap.get(datasourceId);
            if (ObjectUtils.isEmpty(vo)) {
                vo = buildDs(row);
                if (ObjectUtils.isEmpty(vo))
                    continue;
                dsFlagMap.put(datasourceId, vo);
                result.add(vo);
            }
            // build table
            String tableId = row.get("id").toString();
            SQLBotAssistanTable table = tableFlagMap.get(tableId);
            if (ObjectUtils.isEmpty(table)) {
                table = buildTable(row);
                if (ObjectUtils.isEmpty(table))
                    continue;
                tableFlagMap.put(tableId, table);
                vo.getTables().add(table);
            }
            // build field
            String fieldId = row.get("field_id").toString();
            SQLBotAssistantField field = fieldFlagMap.get(fieldId);
            if (ObjectUtils.isEmpty(field)) {
                field = buildField(row);
                if (ObjectUtils.isEmpty(field))
                    continue;
                fieldFlagMap.put(fieldId, field);
                table.getFields().add(field);
            }
        }
        LogUtil.info("sqlbot ds api result: {}", result);
        return result;
    }

    private SQLBotAssistantField buildField(Map<String, Object> row) {
        SQLBotAssistantField field = new SQLBotAssistantField();
        field.setName(row.get("origin_name").toString());
        field.setType(row.get("field_type").toString());
        field.setComment(row.get("field_show_name").toString());
        return field;
    }


    private DataSQLBotAssistantVO buildDs(Map<String, Object> row) {
        Object dsConfig = row.get("ds_config");
        if (ObjectUtils.isEmpty(dsConfig) || StringUtils.isBlank(dsConfig.toString())) {
            return null;
        }
        String config_json = EncryptUtils.aesDecrypt(dsConfig.toString()).toString();
        Configuration config = JsonUtil.parseObject(config_json, Configuration.class);
        DataSQLBotAssistantVO vo = new DataSQLBotAssistantVO();
        vo.setDataBase(config.getDataBase());
        vo.setExtraParams(config.getExtraParams());
        vo.setHost(config.getHost());
        vo.setPort(config.getPort());
        vo.setName(row.get("ds_name").toString());
        vo.setComment(ObjectUtils.isEmpty(row.get("ds_desc")) ? vo.getName() : row.get("ds_desc").toString());
        vo.setType(row.get("ds_type").toString());
        vo.setSchema(config.getSchema());
        vo.setUser(config.getUsername());
        vo.setPassword(config.getPassword());
        return vo;
    }

    private SQLBotAssistanTable buildTable(Map<String, Object> row) {
        SQLBotAssistanTable table = new SQLBotAssistanTable();
        table.setName(row.get("table_name").toString());
        table.setComment(row.get("dataset_name").toString());
        Object infoObj = null;
        if (ObjectUtils.isNotEmpty(infoObj = row.get("info"))) {
            String info = infoObj.toString();
            DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(info, DatasetTableInfoDTO.class);
            if (StringUtils.isNotBlank(tableInfoDTO.getSql())) {
                String sql = new String(Base64.getDecoder().decode(tableInfoDTO.getSql()));
                table.setSql(sql);
            }
        }
        return table;
    }

}
