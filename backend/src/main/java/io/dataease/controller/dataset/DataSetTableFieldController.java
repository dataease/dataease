package io.dataease.controller.dataset;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.filter.F2CLinkFilter;
import io.dataease.base.domain.DatasetTable;
import io.dataease.base.domain.DatasetTableField;
import io.dataease.commons.exception.DEException;
import io.dataease.controller.request.dataset.DataSetTableRequest;
import io.dataease.controller.request.dataset.MultFieldValuesRequest;
import io.dataease.controller.response.DatasetTableField4Type;
import io.dataease.i18n.Translator;
import io.dataease.service.dataset.DataSetFieldService;
import io.dataease.service.dataset.DataSetTableFieldsService;
import io.dataease.service.dataset.DataSetTableService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import cn.hutool.core.collection.CollectionUtil;

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

    @ApiOperation("查询表下属字段")
    @PostMapping("list/{tableId}")
    public List<DatasetTableField> list(@PathVariable String tableId) {
        DatasetTableField datasetTableField = DatasetTableField.builder().build();
        datasetTableField.setTableId(tableId);
        return dataSetTableFieldsService.list(datasetTableField);
    }

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

    @ApiOperation("批量更新")
    @PostMapping("batchEdit")
    public void batchEdit(@RequestBody List<DatasetTableField> list) {
        dataSetTableFieldsService.batchEdit(list);
    }

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

    @ApiOperation("删除")
    @PostMapping("delete/{id}")
    public void delete(@PathVariable String id) {
        dataSetTableFieldsService.delete(id);
    }

    @ApiOperation("多字段值枚举")
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

    @ApiOperation("多字段值枚举")
    @PostMapping("multFieldValues")
    public List<Object> multFieldValues(@RequestBody MultFieldValuesRequest multFieldValuesRequest) throws Exception {
        List<Object> results = new ArrayList<>();
        for (String fieldId : multFieldValuesRequest.getFieldIds()) {
            List<Object> fieldValues = dataSetFieldService.fieldValues(fieldId, multFieldValuesRequest.getUserId());
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
