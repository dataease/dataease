package io.dataease.service.panel.applog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.controller.sys.request.LogTypeItem;
import io.dataease.dto.log.FolderItem;
import io.dataease.ext.ExtSysLogMapper;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.base.domain.SysLogWithBLOBs;
import io.dataease.plugins.common.dto.datasource.DataSourceType;
import io.dataease.service.datasource.DatasourceService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AppLogManager {

    protected static final String contentFormat = "【%s】";

    protected static final String positionFormat = "在【%s】";

    protected static final String format = "给%s【%s】";

    protected Gson gson = new Gson();


    protected Type type = new TypeToken<List<FolderItem>>() {}.getType();



    @Resource
    private ExtSysLogMapper extSysLogMapper;

    @Resource
    private DatasourceService datasourceService;


    public String detailInfo(SysLogWithBLOBs vo) {
        String sourceName = vo.getSourceName();
        String position = null;
        String operateTypeName = SysLogConstants.operateTypeName(vo.getOperateType());
        operateTypeName = Translator.get(operateTypeName);
        String sourceTypeName = SysLogConstants.sourceTypeName(vo.getSourceType());
        sourceTypeName = Translator.get(sourceTypeName);
        String result = operateTypeName + sourceTypeName + String.format(contentFormat, sourceName) + remarkInfo(vo);

        if ((position = vo.getPosition()) != null) {
            List<FolderItem> folderItems = gson.fromJson(position, type);
            String template = folderItems.stream().map(folderItem -> folderItem.getName()).collect(Collectors.joining("/"));
            String positionResult = String.format(positionFormat, template);
            return positionResult + result;
        }
        return result;
    }



    public String remarkInfo(SysLogWithBLOBs vo) {
        String remakrk = null;
        if ((remakrk = vo.getRemark()) != null) {
            String targetTypeName = null;
            List<FolderItem> targetInfos = gson.fromJson(remakrk, type);
            if (CollectionUtils.isNotEmpty(targetInfos)) {
                String template = targetInfos.stream().map(folderItem -> folderItem.getName()).collect(Collectors.joining("/"));
                FolderItem item = targetInfos.get(0);
                Integer targetType = item.getType();
                targetTypeName = SysLogConstants.sourceTypeName(targetType);
                targetTypeName = Translator.get(targetTypeName);
                return String.format(format, targetTypeName, template);
            }
        }
        return "";
    }



    private LogTypeItem parentIds(String id, Integer value) {
        LogTypeItem result = new LogTypeItem();
        String typeValue = "";
        Boolean reversed = false;
        switch (value) {
            case 2:
                typeValue = "dataset";
                break;
            case 3:
                typeValue = "panel";
                reversed = true;
                break;
            case 7:
                typeValue = "dept";
                break;
            case 11:
                typeValue = "menu";
                break;
            default:
                break;
        }
        List<String> ids = new ArrayList<>();
        if (StringUtils.isNotBlank(typeValue)) {
            ids.addAll(AuthUtils.parentResources(id, typeValue));
            if (reversed) {
                Collections.reverse(ids);
            }
        }
        result.setParentIds(ids);
        result.setTypeValue(typeValue);
        return result;
    }


    private List<FolderItem> parentInfos(List<String> ids, Integer value){
        List<FolderItem> folderItems = extSysLogMapper.idAndName(ids, value);
        if (value == 3) {
            folderItems.forEach(item -> {
                if (StringUtils.equals("i18n_panel_list", item.getName())) {
                    item.setName(Translator.get(item.getName()));
                }
            });
        }
        folderItems.forEach(item -> item.setType(value));
        return folderItems;
    }

    public List<FolderItem> justParents(String id, SysLogConstants.SOURCE_TYPE type) {

        Integer value = type.getValue();
        LogTypeItem typeItem = parentIds(id, value);
        List<String> ids = typeItem.getParentIds();
        ids = ids.stream().filter(item -> !StringUtils.equals(id, item)).collect(Collectors.toList());
        return parentInfos(ids, value);
    }

    public List<FolderItem> parentsAndSelf(String id, SysLogConstants.SOURCE_TYPE type) {
        Integer value = type.getValue();
        LogTypeItem logTypeItem = parentIds(id, value);
        if (CollectionUtils.isEmpty(logTypeItem.getParentIds())) {
            logTypeItem.getParentIds().add(id);
        }
        return parentInfos(logTypeItem.getParentIds(), value);
    }

    public FolderItem nameWithId(String id, Integer type) {
        List<String> ids = new ArrayList<>();
        ids.add(id);
        List<FolderItem> folderItems = extSysLogMapper.idAndName(ids, type);
        if (CollectionUtils.isNotEmpty(folderItems)) {
            return folderItems.get(0);
        }
        return null;
    }

    public FolderItem dsTypeInfo(String typeId) {
        ArrayList<DataSourceType> dataSourceTypes = new ArrayList<>(datasourceService.types());
        String name = null;
        for (int i = 0; i < dataSourceTypes.size(); i++) {
            if (dataSourceTypes.get(i).getType().equals(typeId)){
                name = dataSourceTypes.get(i).getName();
                break;
            }
        }
        FolderItem folderItem = new FolderItem();
        folderItem.setId(typeId);
        folderItem.setName(StringUtils.isNotBlank(name) ? name : typeId);
        return folderItem;
    }

    public FolderItem dsTypeInfoById(String dsId) {
        Datasource datasource = datasourceService.get(dsId);
        return dsTypeInfo(datasource.getType());
    }


}
