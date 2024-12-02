package io.dataease.system.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.system.request.OnlineMapEditor;
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
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

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

    public OnlineMapEditor queryOnlineMap() {
        var editor = new OnlineMapEditor();
        List<String> fields = BeanUtils.getFieldNames(OnlineMapEditor.class);
        Map<String, String> mapVal = groupVal(MAP_KEY_PREFIX);
        fields.forEach(field -> {
            String val = mapVal.get(MAP_KEY_PREFIX + field);
            if (StringUtils.isNotBlank(val)) {
                BeanUtils.setFieldValueByName(editor, field, val, String.class);
            }
        });
        return editor;
    }

    public void saveOnlineMap(OnlineMapEditor editor) {
        List<String> fieldNames = BeanUtils.getFieldNames(OnlineMapEditor.class);
        fieldNames.forEach(field -> {
            QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("pkey", MAP_KEY_PREFIX + field);
            CoreSysSetting sysSetting = coreSysSettingMapper.selectOne(queryWrapper);
            var val = (String) BeanUtils.getFieldValueByName(field, editor);
            if (ObjectUtils.isEmpty(sysSetting)) {
                sysSetting = new CoreSysSetting();
                sysSetting.setId(IDUtils.snowID());
                sysSetting.setPkey(MAP_KEY_PREFIX + field);
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
        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("pkey", groupKey);
        coreSysSettingMapper.delete(queryWrapper);
        List<CoreSysSetting> sysSettings = vos.stream().filter(vo -> !SystemSettingUtils.xpackSetting(vo.getPkey())).map(item -> {
            CoreSysSetting sysSetting = BeanUtils.copyBean(new CoreSysSetting(), item);
            sysSetting.setId(IDUtils.snowID());
            return sysSetting;
        }).collect(Collectors.toList());
        extCoreSysSettingMapper.saveBatch(sysSettings);
        datasourceServer.addJob(sysSettings);
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
}
