package io.dataease.controller.dataset;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.google.gson.Gson;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.dataset.MultFieldValuesRequest;
import io.dataease.controller.response.DatasetTableField4Type;
import io.dataease.plugins.common.dto.dataset.DataTableInfoDTO;
import io.dataease.dto.dataset.DatasetTableFieldDTO;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.datasource.entity.Dateformat;
import io.dataease.plugins.datasource.query.QueryProvider;
import io.dataease.plugins.xpack.auth.dto.request.ColumnPermissionItem;
import io.dataease.plugins.datasource.provider.ProviderFactory;
import io.dataease.service.dataset.DataSetFieldService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.PermissionService;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.engine.EngineService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author gin
 * @Date 2021/2/24 4:28 下午
 */
@Api(tags = "数据集：数据集字段")
@ApiSupport(order = 60)
@RestController
@RequestMapping("/dataset/field")
public class DataSetTableFieldController {
    @Resource
    private DataSetTableFieldsService dataSetTableFieldsService;
    @Autowired
    private DataSetFieldService dataSetFieldService;
    @Resource
    private DataSetTableService dataSetTableService;
    @Resource
    private PermissionService permissionService;
    @Resource
    private EngineService engineService;
    @Resource
    private DatasourceService datasourceService;

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("查询表下属字段")
    @PostMapping("list/{tableId}")
    public List<DatasetTableField> list(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        fields = permissionService.filterColumnPermissions(fields, new HashMap<>(), tableId, null);
        return fields;
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("查询表下属字段")
    @PostMapping("listWithPermission/{tableId}")
    public List<DatasetTableField> listWithPermission(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        fields = permissionService.filterColumnPermissions(fields, desensitizationList, tableId, null);
        fields = fields.stream().filter(item -> !desensitizationList.keySet().contains(item.getDataeaseName())).collect(Collectors.toList());
        return fields;
    }

    //管理权限，可以列出所有字段
    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("查询表下属字段")
    @PostMapping("listForPermissionSeting/{tableId}")
    public List<DatasetTableField> listForPermissionSeting(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        return fields;
    }

    //管理权限，可以列出所有字段
    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("分组查询表下属字段")
    @PostMapping("listByDQ/{tableId}")
    public DatasetTableField4Type listByDQ(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        datasetTableField.setGroupType("d");
        DatasetTable datasetTable = dataSetTableService.get(tableId);
        DataTableInfoDTO dataTableInfoDTO = new Gson().fromJson(datasetTable.getInfo(), DataTableInfoDTO.class);
        List<DatasetTableFieldDTO> dimensionList = new ArrayList<>();
        dataSetTableFieldsService.list(datasetTableField).forEach(o -> {
            DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
            BeanUtils.copyProperties(o, datasetTableFieldDTO);
            List<Object> deTypeCascader = new ArrayList<>();
            deTypeCascader.add(datasetTableFieldDTO.getDeType());
            if (datasetTableFieldDTO.getDeExtractType() == 0 && datasetTableFieldDTO.getDeType() == 1) {
                deTypeCascader.add(datasetTableFieldDTO.getDateFormatType());
            }
            datasetTableFieldDTO.setDeTypeCascader(deTypeCascader);
            if (dataTableInfoDTO.isSetKey() && dataTableInfoDTO.getKeys().contains(datasetTableFieldDTO.getOriginName())){
                datasetTableFieldDTO.setKey(true);
            }
            dimensionList.add(datasetTableFieldDTO);
        });


        datasetTableField.setGroupType("q");
        List<DatasetTableFieldDTO> quotaList = new ArrayList<>();
        dataSetTableFieldsService.list(datasetTableField).forEach(o -> {
            DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
            BeanUtils.copyProperties(o, datasetTableFieldDTO);
            List<Object> deTypeCascader = new ArrayList<>();
            deTypeCascader.add(datasetTableFieldDTO.getDeType());
            if (datasetTableFieldDTO.getDeExtractType() == 0 && datasetTableFieldDTO.getDeType() == 1) {
                deTypeCascader.add(datasetTableFieldDTO.getDateFormatType());
            }
            datasetTableFieldDTO.setDeTypeCascader(deTypeCascader);
            if (dataTableInfoDTO.isSetKey() && dataTableInfoDTO.getKeys().contains(datasetTableFieldDTO.getOriginName())){
                datasetTableFieldDTO.setKey(true);
            }
            quotaList.add(datasetTableFieldDTO);
        });

        DatasetTableField4Type datasetTableField4Type = new DatasetTableField4Type();
        datasetTableField4Type.setDimensionList(dimensionList);
        datasetTableField4Type.setQuotaList(quotaList);
        return datasetTableField4Type;
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("批量更新")
    @PostMapping("batchEdit")
    public void batchEdit(@RequestBody List<DatasetTableField> list) {
        dataSetTableFieldsService.batchEdit(list);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存")
    @PostMapping("save")
    public DatasetTableField save(@RequestBody DatasetTableField datasetTableField) throws Exception {
        dataSetTableFieldsService.checkFieldName(datasetTableField);
        // 非直连数据集需先完成数据同步
        DatasetTable datasetTable = dataSetTableService.get(datasetTableField.getTableId());
        if (datasetTable.getMode() == 1) {
            if (!dataSetTableService.checkEngineTableIsExists(datasetTableField.getTableId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }
        }
        try {
            // 执行一次sql，确保数据集中所有字段均能正确执行
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            BeanUtils.copyProperties(datasetTable, dataSetTableRequest);
            dataSetTableService.getPreviewData(dataSetTableRequest, 1, 1, Collections.singletonList(datasetTableField), null);
        } catch (Exception e) {
            DataEaseException.throwException(Translator.get("i18n_calc_field_error"));
        }
        return dataSetTableFieldsService.save(datasetTableField);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("保存不校验表达式")
    @PostMapping("saveNotCheck")
    public DatasetTableField saveNotCheck(@RequestBody DatasetTableField datasetTableField) throws Exception {
        dataSetTableFieldsService.checkFieldName(datasetTableField);
        // 非直连数据集需先完成数据同步
        DatasetTable datasetTable = dataSetTableService.get(datasetTableField.getTableId());
        if (datasetTable.getMode() == 1) {
            if (!dataSetTableService.checkEngineTableIsExists(datasetTableField.getTableId())) {
                throw new RuntimeException(Translator.get("i18n_data_not_sync"));
            }
        }
        return dataSetTableFieldsService.save(datasetTableField);
    }

    @DePermission(type = DePermissionType.DATASET, value = "tableId", level = ResourceAuthLevel.DATASET_LEVEL_MANAGE)
    @ApiOperation("设置主键")
    @PostMapping("saveKey")
    public void saveKey(@RequestBody DatasetTableFieldDTO datasetTableField) throws Exception {
        DatasetTable datasetTable = dataSetTableService.get(datasetTableField.getTableId());
        if (datasetTable.getMode() == 1) {
            dataSetTableService.saveKey(datasetTable, datasetTableField);
        }
    }

    @DePermissions(value = {
            @DePermission(type = DePermissionType.DATASET, level = ResourceAuthLevel.DATASET_LEVEL_MANAGE, paramIndex = 1)
    })
    @ApiOperation("删除")
    @PostMapping("delete/{id}/{tableId}")
    public void delete(@PathVariable String id, @PathVariable String tableId) {
        dataSetTableFieldsService.delete(id);
    }

    @ApiIgnore
    @PostMapping("linkMultFieldValues")
    public List<Object> linkMultFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest)
            throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                .getRequest();
        String linkToken = request.getHeader(F2CLinkFilter.LINK_TOKEN_KEY);
        DecodedJWT jwt = JWT.decode(linkToken);
        Long userId = jwt.getClaim("userId").asLong();
        multFieldValuesRequest.setUserId(userId);
        return multFieldValues(multFieldValuesRequest);
    }

    @ApiIgnore
    @PostMapping("multFieldValues")
    public List<Object> multFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Object> results = new ArrayList<>();
        for (String fieldId : multFieldValuesRequest.getFieldIds()) {
            List<Object> fieldValues = dataSetFieldService.fieldValues(fieldId, multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, false, multFieldValuesRequest.getKeyword());
            if (CollectionUtils.isNotEmpty(fieldValues)) {
                results.addAll(fieldValues);
            }

        }
        List<Object> list = results.stream().distinct().collect(Collectors.toList());
        list = dataSetFieldService.chineseSort(list, multFieldValuesRequest.getSort());
        return list;
    }

    @ApiIgnore
    @PostMapping("linkMappingFieldValues")
    public List<Object> linkMappingFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String linkToken = request.getHeader(F2CLinkFilter.LINK_TOKEN_KEY);
        DecodedJWT jwt = JWT.decode(linkToken);
        Long userId = jwt.getClaim("userId").asLong();
        multFieldValuesRequest.setUserId(userId);
        return dataSetFieldService.fieldValues(multFieldValuesRequest.getFieldIds(), multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, true, false, multFieldValuesRequest.getKeyword());
    }

    @ApiIgnore
    @PostMapping("mappingFieldValues")
    public List<Object> mappingFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        return dataSetFieldService.fieldValues(multFieldValuesRequest.getFieldIds(), multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, true, false, multFieldValuesRequest.getKeyword());
    }

    @ApiIgnore
    @PostMapping("multFieldValuesForPermissions")
    public List<Object> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Object> results = new ArrayList<>();
        for (String fieldId : multFieldValuesRequest.getFieldIds()) {
            List<Object> fieldValues = dataSetFieldService.fieldValues(fieldId, multFieldValuesRequest.getUserId(), false, true);
            if (CollectionUtils.isNotEmpty(fieldValues)) {
                results.addAll(fieldValues);
            }

        }
        ArrayList<Object> list = results.stream().collect(
                Collectors.collectingAndThen(
                        Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(t -> {
                                    if (ObjectUtils.isEmpty(t))
                                        return "";
                                    return t.toString();
                                }))),
                        ArrayList::new));
        return list;
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("时间格式")
    @PostMapping("dateformats/{tableId}")
    public List<Dateformat> dateformats(@PathVariable String tableId) throws Exception {
        DatasetTable datasetTable = dataSetTableService.get(tableId);
        Datasource ds = datasetTable.getMode() == 0 ? datasourceService.get(datasetTable.getDataSourceId()) : engineService.getDeEngine();
        QueryProvider qp = ProviderFactory.getQueryProvider(ds.getType());
        return qp.dateformat();
    }
}
