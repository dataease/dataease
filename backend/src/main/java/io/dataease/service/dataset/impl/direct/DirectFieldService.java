package io.dataease.service.dataset.impl.direct;

import com.google.gson.Gson;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.base.domain.Datasource;
import io.dataease.commons.constants.ColumnPermissionConstants;
import io.dataease.dto.chart.ChartFieldCustomFilterDTO;
import io.dataease.i18n.Translator;
import io.dataease.provider.datasource.DatasourceProvider;
import io.dataease.provider.ProviderFactory;
import io.dataease.controller.request.datasource.DatasourceRequest;
import io.dataease.service.dataset.*;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.dto.dataset.DataSetTableUnionDTO;
import io.dataease.dto.dataset.DataTableInfoDTO;
import io.dataease.provider.QueryProvider;
import io.dataease.service.engine.EngineService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


@Service("directDataSetFieldService")
public class DirectFieldService implements DataSetFieldService {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private DatasourceService datasourceService;
    @Resource
    private DataSetTableUnionService dataSetTableUnionService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private EngineService engineService;

    @Override
    public List<Object> fieldValues(String fieldId, Long userId, Boolean userPermissions) throws Exception {
        DatasetTableField field = dataSetTableFieldsService.selectByPrimaryKey(fieldId);
        if (field == null || StringUtils.isEmpty(field.getTableId())) return null;

        DatasetTable datasetTable = dataSetTableService.get(field.getTableId());
        if (ObjectUtils.isEmpty(datasetTable) || StringUtils.isEmpty(datasetTable.getName())) return null;

        DatasetTableField datasetTableField = DatasetTableField.builder().tableId(field.getTableId()).checked(Boolean.TRUE).build();
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);

        List<ChartFieldCustomFilterDTO> customFilter = new ArrayList<>();
        if(userPermissions){
            //列权限
            List<String> desensitizationList = new ArrayList<>();
            fields = permissionService.filterColumnPermissons(fields, desensitizationList, datasetTable.getId(), userId);
            //禁用的
            if(!fields.stream().map(DatasetTableField::getId).collect(Collectors.toList()).contains(fieldId)){
                return new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(desensitizationList) && desensitizationList.contains(field.getDataeaseName())) {
                List<Object> results = new ArrayList<>();
                results.add(ColumnPermissionConstants.Desensitization_desc);
                return results;
            }
            //行权限
            customFilter = permissionService.getCustomFilters(fields, datasetTable, userId);
        }

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        DatasourceProvider datasourceProvider = null;
        if (datasetTable.getMode() == 0) {// 直连
            if (StringUtils.isEmpty(datasetTable.getDataSourceId())) return null;
            Datasource ds = datasourceService.get(datasetTable.getDataSourceId());
            if (StringUtils.isNotEmpty(ds.getStatus()) && ds.getStatus().equalsIgnoreCase("Error")) {
                throw new Exception(Translator.get("i18n_invalid_ds"));
            }
            datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "db")) {
                datasourceRequest.setTable(dataTableInfoDTO.getTable());
                datasourceRequest.setQuery(qp.createQuerySQL(dataTableInfoDTO.getTable(), Collections.singletonList(field), true, ds, customFilter));
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "sql")) {
                datasourceRequest.setQuery(qp.createQuerySQLAsTmp(dataTableInfoDTO.getSql(), Collections.singletonList(field), true, customFilter));
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "custom")) {
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                List<DataSetTableUnionDTO> listUnion = dataSetTableUnionService.listByTableId(dt.getList().get(0).getTableId());
                String sql = dataSetTableService.getCustomSQLDatasource(dt, listUnion, ds);
                datasourceRequest.setQuery(qp.createQuerySQLAsTmp(sql, Collections.singletonList(field), true, customFilter));
            } else if (StringUtils.equalsIgnoreCase(datasetTable.getType(), "union")) {
                DataTableInfoDTO dt = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
                String sql = (String) dataSetTableService.getUnionSQLDatasource(dt, ds).get("sql");
                datasourceRequest.setQuery(qp.createQuerySQLAsTmp(sql, Collections.singletonList(field), true, customFilter));
            }
        } else if (datasetTable.getMode() == 1) {// 抽取
            // 连接doris，构建doris数据源查询
            Datasource ds = engineService.getDeEngine();
            datasourceProvider = ProviderFactory.getProvider(ds.getType());
            datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            String tableName = "ds_" + datasetTable.getId().replaceAll("-", "_");
            datasourceRequest.setTable(tableName);
            QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
            datasourceRequest.setQuery(qp.createQuerySQL(tableName, Collections.singletonList(field), true, null, customFilter));
        }

        List<String[]> rows = datasourceProvider.getData(datasourceRequest);
        List<Object> results = rows.stream().map(row -> row[0]).distinct().collect(Collectors.toList());
        return results;
    }
}
