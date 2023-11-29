package io.dataease.system.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.system.vo.SettingItemVO;
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
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SysParameterManage {

    private static final String mapKey = "map.key";

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

    public String queryOnlineMap() {
        return singleVal(mapKey);
    }

    public void saveOnlineMap(String val) {

        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pkey", mapKey);
        CoreSysSetting sysSetting = coreSysSettingMapper.selectOne(queryWrapper);
        if (ObjectUtils.isEmpty(sysSetting)) {
            sysSetting = new CoreSysSetting();
            sysSetting.setId(IDUtils.snowID());
            sysSetting.setPkey(mapKey);
            sysSetting.setPval(val);
            sysSetting.setType("text");
            sysSetting.setSort(1);
            coreSysSettingMapper.insert(sysSetting);
        }
        sysSetting.setPval(val);
        coreSysSettingMapper.updateById(sysSetting);
    }


    public Map<String, String> groupVal(String groupKey) {
        QueryWrapper<CoreSysSetting> queryWrapper = new QueryWrapper<>();
        queryWrapper.likeRight("pkey", groupKey);
        queryWrapper.orderByAsc("sort");
        List<CoreSysSetting> sysSettings = coreSysSettingMapper.selectList(queryWrapper);
        if (!CollectionUtils.isEmpty(sysSettings)) {
            return sysSettings.stream().collect(Collectors.toMap(CoreSysSetting::getPkey, CoreSysSetting::getPval));
        }
        return null;
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
}
