package io.dataease.visualization.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.dataset.union.DatasetTableInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.api.report.bo.DatasetPermissionTemplate;
import io.dataease.api.report.bo.TableSysVariable;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.utils.DatasetTableTypeConstants;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.utils.JsonUtil;
import io.dataease.visualization.dao.perext.ResourcePermissionMapper;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ResourcePermissionManage {


    @Resource
    private ResourcePermissionMapper resourcePermissionMapper;


    public static final String regex2 = "\\$f2cde\\[(.*?)\\]";

    public List<DatasetPermissionTemplate> queruDatasetPermissionTemplate(Long resourceId) {
        String componentDataText = resourcePermissionMapper.queryResourceData(resourceId);
        TypeReference<List<Map<String, Object>>> tokenType = new TypeReference<>() {
        };
        List<Map<String, Object>> componentData = JsonUtil.parseList(componentDataText, tokenType);
        List<Map<String, Object>> userViewList = getUserViewList(componentData);
        List<Long> viewIds = userViewList.stream().filter(item -> ObjectUtils.isNotEmpty(item.get("id"))).map(item -> Long.parseLong(item.get("id").toString())).collect(Collectors.toList());
        List<CoreDatasetGroup> datasetGroups = resourcePermissionMapper.queryDataSetList(viewIds);
        return getDatasetPermissionTemplate(datasetGroups);
    }

    private static boolean isParams(String paramId) {
        if (Arrays.asList("sysParams.userId", "sysParams.userEmail", "sysParams.userName").contains(paramId)) {
            return true;
        }
        boolean isLong = false;
        try {
            Long.valueOf(paramId);
            isLong = true;
        } catch (Exception e) {
            isLong = false;
        }
        if (paramId.length() >= 18 && isLong) {
            return true;
        }
        return false;
    }

    private List<Map<String, Object>> getUserViewList(List<Map<String, Object>> componentData) {
        List<Map<String, Object>> userViewList = new ArrayList<>();

        Stack<Map<String, Object>> stack = new Stack<>();
        stack.addAll(componentData);

        while (!stack.isEmpty()) {
            Map<String, Object> node = stack.pop();
            if ("UserView".equals(node.get("component"))) {
                userViewList.add(node);
            }
            if ("DeTabs".equals(node.get("component"))) {
                Object propValueObj = null;
                List<Map<String, Object>> tabComponentList = null;
                if (ObjectUtils.isNotEmpty(propValueObj = node.get("propValue")) && CollectionUtils.isNotEmpty(tabComponentList = (List<Map<String, Object>>) propValueObj)) {
                    List<Map<String, Object>> innerComponentList = tabComponentList.stream().filter(item -> ObjectUtils.isNotEmpty(item.get("componentData"))).flatMap(propValueItem -> {
                        List<Map<String, Object>> mapList = (List<Map<String, Object>>) propValueItem.get("componentData");
                        return mapList.stream();
                    }).collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(innerComponentList)) {
                        stack.addAll(innerComponentList);
                    }
                }
            }
        }

        return userViewList;
    }


    private List<DatasetPermissionTemplate> getDatasetPermissionTemplate(List<CoreDatasetGroup> datasetGroups) {
        TypeReference<List<UnionDTO>> typeReference = new TypeReference<>() {
        };
        List<DatasetPermissionTemplate> templateList = new ArrayList<>();
        datasetGroups.forEach(group -> {
            DatasetPermissionTemplate template = new DatasetPermissionTemplate();
            template.setDatasetId(group.getId());
            String info = group.getInfo();
            List<UnionDTO> unionList = JsonUtil.parseList(info, typeReference);
            Stack<UnionDTO> stack = new Stack<>();
            stack.addAll(unionList);
            Set<Long> dsIdSet = new HashSet<>();
            List<TableSysVariable> tableSysVariables = new ArrayList<>();
            while (!stack.isEmpty()) {
                UnionDTO union = stack.pop();
                DatasetTableDTO currentDs = union.getCurrentDs();
                dsIdSet.add(currentDs.getDatasourceId());
                Long currentTableId = currentDs.getId();
                if (ObjectUtils.isNotEmpty(currentDs.getType()) && currentDs.getType().equals(DatasetTableTypeConstants.DATASET_TABLE_SQL)) {
                    String tableInfoText = currentDs.getInfo();
                    DatasetTableInfoDTO tableInfoDTO = JsonUtil.parseObject(tableInfoText, DatasetTableInfoDTO.class);
                    String s = new String(Base64.getDecoder().decode(tableInfoDTO.getSql()));
                    Pattern pattern = Pattern.compile(regex2);
                    Matcher matcher = pattern.matcher(s);
                    List<String> sysVariables = new ArrayList<>();
                    while (matcher.find()) {
                        String paramId = matcher.group().substring(7, matcher.group().length() - 1);
                        if (!isParams(paramId)) {
                            continue;
                        }
                        sysVariables.add(paramId);
                    }
                    if (CollectionUtils.isNotEmpty(sysVariables)) {
                        TableSysVariable tableSysVariable = new TableSysVariable();
                        tableSysVariable.setTableId(currentTableId);
                        tableSysVariable.setSysVariables(sysVariables);
                        tableSysVariables.add(tableSysVariable);
                    }
                }
                if (CollectionUtils.isNotEmpty(union.getChildrenDs())) {
                    stack.addAll(union.getChildrenDs());
                }
            }
            template.setDsIdSet(dsIdSet);
            template.setTableSysVariables(tableSysVariables);
            templateList.add(template);
        });
        return templateList;
    }

}
