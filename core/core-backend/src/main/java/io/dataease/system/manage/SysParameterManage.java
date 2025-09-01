package io.dataease.system.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.system.request.OnlineMapEditor;
import io.dataease.api.system.request.SQLBotConfigCreator;
import io.dataease.api.system.vo.SettingItemVO;
import io.dataease.api.system.vo.ShareBaseVO;
import io.dataease.datasource.server.DatasourceServer;
import io.dataease.license.config.XpackInteract;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingMapper;
import io.dataease.system.dao.ext.mapper.ExtCoreSysSettingMapper;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.utils.SystemSettingUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class SysParameterManage {

    @Value("${dataease.show-demo-tips:false}")
    private boolean showDemoTips;

    @Value("${dataease.demo-tips-content:#{null}}")
    private String demoTipsContent;

    private static final String MAP_KEY_PREFIX = "map.";

    @Resource
    private CoreSysSettingMapper coreSysSettingMapper;

    @Resource
    private ExtCoreSysSettingMapper extCoreSysSettingMapper;
    @Resource
    private DatasourceServer datasourceServer;

    public String singleVal(String key) {
        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pkey", key);
        CoreSysSetting sysSetting = coreSysSettingMapper.selectOne(queryWrapper);
        if (ObjectUtils.isNotEmpty(sysSetting)) {
            return sysSetting.getPval();
        }
        return null;
    }

    public OnlineMapEditor queryOnlineMap(String mapType) {
        if (StringUtils.isBlank(mapType)) {
            List<CoreSysSetting> typeList = groupList(MAP_KEY_PREFIX + "mapType");
            mapType = "gaode";
            if (!CollectionUtils.isEmpty(typeList)) {
                mapType = typeList.getFirst().getPval();
            }
        }
        String prefix;
        if (!StringUtils.equals(mapType, "gaode")) {
            prefix = mapType + "." + MAP_KEY_PREFIX;
        } else {
            prefix = MAP_KEY_PREFIX;
        }
        var editor = new OnlineMapEditor();
        List<String> fields = BeanUtils.getFieldNames(OnlineMapEditor.class);
        Map<String, String> mapVal = groupVal(prefix);
        fields.forEach(field -> {
            String val = mapVal.get(prefix + field);
            if (StringUtils.isNotBlank(val)) {
                BeanUtils.setFieldValueByName(editor, field, val, String.class);
            }
        });

        editor.setMapType(mapType);

        return editor;
    }

    public void saveOnlineMap(OnlineMapEditor editor) {
        String mapType = editor.getMapType();
        if (StringUtils.isBlank(mapType)) {
            List<CoreSysSetting> typeList = groupList(MAP_KEY_PREFIX + "mapType");
            mapType = "gaode";
            if (!CollectionUtils.isEmpty(typeList)) {
                mapType = typeList.getFirst().getPval();
            }
        }

        List<String> fieldNames = BeanUtils.getFieldNames(OnlineMapEditor.class);
        String finalMapType = mapType;
        fieldNames.forEach(field -> {
            String prefix = MAP_KEY_PREFIX;
            if (!(StringUtils.equals(field, "mapType") || StringUtils.equals(finalMapType, "gaode"))) {
                prefix = finalMapType + "." + MAP_KEY_PREFIX;
            }

            QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pkey", prefix + field);
            CoreSysSetting sysSetting = coreSysSettingMapper.selectOne(queryWrapper);
            var val = (String) BeanUtils.getFieldValueByName(field, editor);
            if (ObjectUtils.isEmpty(sysSetting)) {
                sysSetting = new CoreSysSetting();
                sysSetting.setId(IDUtils.snowID());
                sysSetting.setPkey(prefix + field);
                sysSetting.setPval(val == null ? "" : val);
                sysSetting.setType("text");
                sysSetting.setSort(1);
                coreSysSettingMapper.insert(sysSetting);
                return;
            }
            sysSetting.setPval(val);
            coreSysSettingMapper.updateById(sysSetting);
        });
    }


    public Map<String, String> groupVal(String groupKey) {
        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("pkey", groupKey);
        queryWrapper.orderByAsc("sort");
        List<CoreSysSetting> sysSettings = coreSysSettingMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(sysSettings)) {
            return sysSettings.stream().collect(Collectors.toMap(CoreSysSetting::getPkey, CoreSysSetting::getPval));
        }
        return new HashMap<>();
    }

    public List<CoreSysSetting> groupList(String groupKey) {
        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("pkey", groupKey);
        queryWrapper.orderByAsc("sort");
        return coreSysSettingMapper.selectList(queryWrapper);
    }

    @XpackInteract(value = "perSetting")
    public List<SettingItemVO> convert(List<CoreSysSetting> sysSettings) {
        return sysSettings.stream().sorted(Comparator.comparing(CoreSysSetting::getSort)).map(item -> BeanUtils.copyBean(new SettingItemVO(), item)).toList();
    }

    @XpackInteract(value = "perSetting", replace = true)
    public List<Object> getUiList() {
        List<Object> result = new ArrayList<>();
        result.add(buildSettingItem("community", true));
        result.add(buildSettingItem("showDemoTips", showDemoTips));
        result.add(buildSettingItem("demoTipsContent", demoTipsContent));
        return result;
    }

    @XpackInteract(value = "perSetting", replace = true)
    public Integer defaultLogin() {
        return 0;
    }

    private Map<String, Object> buildSettingItem(String pkey, Object pval) {
        Map<String, Object> item = new HashMap<>();
        item.put("pkey", pkey);
        item.put("pval", pval);
        return item;
    }


    @Transactional
    public void saveGroup(List<SettingItemVO> vos, String groupKey) {
        List<CoreSysSetting> sysSettings = vos.stream().filter(vo -> !SystemSettingUtils.xpackSetting(vo.getPkey())).map(item -> {
            CoreSysSetting sysSetting = BeanUtils.copyBean(new CoreSysSetting(), item);
            sysSetting.setId(IDUtils.snowID());
            return sysSetting;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(sysSettings)) {
            QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
            sysSettings.forEach(sysSetting -> {
                queryWrapper.clear();
                queryWrapper.eq("pkey", sysSetting.getPkey());
                coreSysSettingMapper.delete(queryWrapper);
            });
            extCoreSysSettingMapper.saveBatch(sysSettings);
        }
        datasourceServer.addJob(sysSettings);
    }

    public void saveSqlBotConfig(SQLBotConfigCreator configVO) {
        List<CoreSysSetting> configList = new ArrayList<>();
        String key = "sqlbot.";
        CoreSysSetting domainVo = new CoreSysSetting();
        domainVo.setPkey(key + "domain");
        domainVo.setPval(configVO.getDomain());
        domainVo.setType("text");
        domainVo.setSort(0);
        domainVo.setId(IDUtils.snowID());
        configList.add(domainVo);

        CoreSysSetting idVo = new CoreSysSetting();
        idVo.setPkey(key + "id");
        idVo.setPval(configVO.getId());
        idVo.setType("text");
        idVo.setSort(0);
        idVo.setId(IDUtils.snowID());
        configList.add(idVo);

        CoreSysSetting enabledVo = new CoreSysSetting();
        enabledVo.setPkey(key + "enabled");
        enabledVo.setPval(configVO.getEnabled().toString());
        enabledVo.setType("text");
        enabledVo.setSort(0);
        enabledVo.setId(IDUtils.snowID());
        configList.add(enabledVo);

        CoreSysSetting validVo = new CoreSysSetting();
        validVo.setPkey(key + "valid");
        validVo.setPval(configVO.getValid().toString());
        validVo.setType("text");
        validVo.setSort(0);
        validVo.setId(IDUtils.snowID());
        configList.add(validVo);


        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("pkey", key);
        coreSysSettingMapper.delete(queryWrapper);

        extCoreSysSettingMapper.saveBatch(configList);
    }

    @XpackInteract(value = "perSetting", before = false)
    @Transactional
    public void saveBasic(List<SettingItemVO> vos) {
        String key = "basic.";
        proxy().saveGroup(vos, key);
    }

    private SysParameterManage proxy() {
        return CommonBeanFactory.getBean(SysParameterManage.class);
    }

    public ShareBaseVO shareBase() {
        String disableText = singleVal("basic.shareDisable");
        String requireText = singleVal("basic.sharePeRequire");
        ShareBaseVO vo = new ShareBaseVO();
        if (StringUtils.isNotBlank(disableText) && StringUtils.equals("true", disableText)) {
            vo.setDisable(true);
        }
        if (StringUtils.isNotBlank(requireText) && StringUtils.equals("true", requireText)) {
            vo.setPeRequire(true);
        }
        return vo;
    }

    public void insert(CoreSysSetting coreSysSetting) {
        coreSysSettingMapper.insert(coreSysSetting);
    }

}
