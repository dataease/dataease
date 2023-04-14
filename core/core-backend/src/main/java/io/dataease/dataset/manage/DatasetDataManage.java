package io.dataease.dataset.manage;

import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.DatasetTableFieldDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.union.model.SQLMeta;
import io.dataease.dataset.constant.DatasetTableType;
import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.model.TableField;
import io.dataease.datasource.provider.CalciteProvider;
import io.dataease.datasource.request.DatasourceRequest;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.engine.constant.SQLConstants;
import io.dataease.engine.sql.SQLProvider;
import io.dataease.engine.trans.Field2SQLObj;
import io.dataease.engine.trans.Order2SQLObj;
import io.dataease.engine.trans.Table2SQLObj;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.IDUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class DatasetDataManage {
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private CalciteProvider calciteProvider;
    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;

    public List<DatasetTableFieldDTO> getTableFields(DatasetTableDTO datasetTableDTO) throws Exception {
        List<DatasetTableFieldDTO> list = null;
        String type = datasetTableDTO.getType();
        DatasetTableInfoDTO tableInfoDTO = JsonUtil.parse(datasetTableDTO.getInfo(), DatasetTableInfoDTO.class);
        if (StringUtils.equalsIgnoreCase(type, DatasetTableType.DB)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasetTableDTO.getDatasourceId());

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(coreDatasource);
            datasourceRequest.setTable(tableInfoDTO.getTable());
            List<TableField> tableFields = calciteProvider.getTableFields(datasourceRequest);

            list = transFields(tableFields);
        } else if (StringUtils.equalsIgnoreCase(type, DatasetTableType.SQL)) {
            CoreDatasource coreDatasource = coreDatasourceMapper.selectById(datasetTableDTO.getDatasourceId());
            DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
            BeanUtils.copyBean(datasourceSchemaDTO, coreDatasource);
            datasourceSchemaDTO.setSchemaAlias(String.format(SQLConstants.SCHEMA, 0));

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDsList(Map.of(datasourceSchemaDTO.getId(), datasourceSchemaDTO));
            datasourceRequest.setQuery(tableInfoDTO.getSql());
            List<TableField> tableFields = calciteProvider.fetchResultField(datasourceRequest);

            list = transFields(tableFields);
        } else {
            // todo excel,api
        }
        return list;
    }

    public List<DatasetTableFieldDTO> transFields(List<TableField> tableFields) {
        return tableFields.stream().map(ele -> {
            DatasetTableFieldDTO dto = new DatasetTableFieldDTO();
            dto.setName(StringUtils.isNotEmpty(ele.getRemarks()) ? ele.getRemarks() : ele.getFieldName());
            // todo trans field
            return dto;
        }).collect(Collectors.toList());
    }

    public Map<String, List> previewData(DatasetGroupInfoDTO datasetGroupInfoDTO) throws Exception {
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
        String sql = (String) sqlMap.get("sql");
        List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        List<DatasetTableFieldDTO> calcFields = fields.stream().filter(ele -> Objects.equals(ele.getExtField(), ExtFieldConstant.EXT_CALC)).collect(Collectors.toList());
        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")");
        Field2SQLObj.field2sqlObj(sqlMeta, fields, calcFields);
        Order2SQLObj.getOrders(sqlMeta, fields, calcFields, datasetGroupInfoDTO.getSortFields());
        String querySQL = SQLProvider.createQuerySQL(sqlMeta, false);
        // 通过数据源请求数据
        Map<String, DatasourceSchemaDTO> dsMap = (Map<String, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);
        return calciteProvider.fetchResultAndField(datasourceRequest);
    }

    public Map<String, List> previewDataWithLimit(DatasetGroupInfoDTO datasetGroupInfoDTO, int start, int count) throws Exception {
        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(datasetGroupInfoDTO);
        String sql = (String) sqlMap.get("sql");
        List<DatasetTableFieldDTO> fields = (List<DatasetTableFieldDTO>) sqlMap.get("field");
        List<DatasetTableFieldDTO> calcFields = fields.stream().filter(ele -> Objects.equals(ele.getExtField(), ExtFieldConstant.EXT_CALC)).collect(Collectors.toList());
        // build query sql
        SQLMeta sqlMeta = new SQLMeta();
        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")");
        Field2SQLObj.field2sqlObj(sqlMeta, fields, calcFields);
        Order2SQLObj.getOrders(sqlMeta, fields, calcFields, datasetGroupInfoDTO.getSortFields());
        String querySQL = SQLProvider.createQuerySQLWithLimit(sqlMeta, false, start, count);
        // 通过数据源请求数据
        Map<String, DatasourceSchemaDTO> dsMap = (Map<String, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setQuery(querySQL);
        datasourceRequest.setDsList(dsMap);
        return calciteProvider.fetchResultAndField(datasourceRequest);
    }
}
