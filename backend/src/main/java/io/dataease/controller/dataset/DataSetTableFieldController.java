package io.dataease.controller.dataset;

import cn.hutool.core.collection.CollectionUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.annotation.DePermission;
import io.dataease.auth.annotation.DePermissions;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.ResourceAuthLevel;
import io.dataease.commons.exception.DEException;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.dataset.MultFieldValuesRequest;
import io.dataease.controller.response.DatasetTableField4Type;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.DatasetTable;
import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.service.dataset.DataSetFieldService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.dataease.service.dataset.PermissionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("查询表下属字段")
    @PostMapping("list/{tableId}")
    public List<DatasetTableField> list(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        fields = permissionService.filterColumnPermissons(fields, new ArrayList<>(), tableId, null);
        return fields;
    }

    @DePermission(type = DePermissionType.DATASET)
    @ApiOperation("查询表下属字段")
    @PostMapping("listWithPermission/{tableId}")
    public List<DatasetTableField> listWithPermission(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        List<DatasetTableField> fields = dataSetTableFieldsService.list(datasetTableField);
        List<String> desensitizationList = new ArrayList<>();
        fields = permissionService.filterColumnPermissons(fields, desensitizationList, tableId, null);
        fields = fields.stream().filter(item -> !desensitizationList.contains(item.getDataeaseName())).collect(Collectors.toList());
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
        List<DatasetTableField> dimensionList = dataSetTableFieldsService.list(datasetTableField);
        datasetTableField.setGroupType("q");
        List<DatasetTableField> quotaList = dataSetTableFieldsService.list(datasetTableField);

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
    public DatasetTableField save(@RequestBody DatasetTableField datasetTableField) {
        dataSetTableFieldsService.checkFieldName(datasetTableField);
        try {
            // 执行一次sql，确保数据集中所有字段均能正确执行
            DatasetTable datasetTable = dataSetTableService.get(datasetTableField.getTableId());
            DataSetTableRequest dataSetTableRequest = new DataSetTableRequest();
            BeanUtils.copyProperties(datasetTable, dataSetTableRequest);
            dataSetTableService.getPreviewData(dataSetTableRequest, 1, 1, Collections.singletonList(datasetTableField));
        } catch (Exception e) {
            DEException.throwException(Translator.get("i18n_calc_field_error"));
        }
        return dataSetTableFieldsService.save(datasetTableField);
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
            List<Object> fieldValues = dataSetFieldService.fieldValues(fieldId, multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, false);
            if (CollectionUtil.isNotEmpty(fieldValues)) {
                results.addAll(fieldValues);
            }

        }
        List<Object> list = results.stream().distinct().collect(Collectors.toList());
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
        return dataSetFieldService.fieldValues(multFieldValuesRequest.getFieldIds(), multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, true,false);
    }

    @ApiIgnore
    @PostMapping("mappingFieldValues")
    public List<Object> mappingFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        return dataSetFieldService.fieldValues(multFieldValuesRequest.getFieldIds(), multFieldValuesRequest.getSort(), multFieldValuesRequest.getUserId(), true, true, false);
    }

    @ApiIgnore
    @PostMapping("multFieldValuesForPermissions")
    public List<Object> multFieldValuesForPermissions(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Object> results = new ArrayList<>();
        for (String fieldId : multFieldValuesRequest.getFieldIds()) {
            List<Object> fieldValues = dataSetFieldService.fieldValues(fieldId, multFieldValuesRequest.getUserId(), false, true);
            if (CollectionUtil.isNotEmpty(fieldValues)) {
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
}
