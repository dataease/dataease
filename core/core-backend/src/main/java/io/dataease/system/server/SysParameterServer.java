package io.dataease.system.server;

import io.dataease.api.system.SysParameterApi;
import io.dataease.api.system.request.OnlineMapEditor;
import io.dataease.api.system.vo.SettingItemVO;
import io.dataease.constant.XpackSettingConstants;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.manage.SysParameterManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sysParameter")
public class SysParameterServer implements SysParameterApi {

    @Resource
    private SysParameterManage sysParameterManage;

    @Override
    public String singleVal(String key) {
        return sysParameterManage.singleVal(key);
    }

    @Override
    public void saveOnlineMap(OnlineMapEditor editor) {
        sysParameterManage.saveOnlineMap(editor);
    }

    @Override
    public OnlineMapEditor queryOnlineMap() {
        return sysParameterManage.queryOnlineMap();
    }

    @Override
    public List<SettingItemVO> queryBasicSetting() {
        String key = "basic.";
        List<CoreSysSetting> coreSysSettings = sysParameterManage.groupList(key);
        return sysParameterManage.convert(coreSysSettings);
    }

    @Override
    public void saveBasicSetting(List<SettingItemVO> settingItemVOS) {
        sysParameterManage.saveBasic(settingItemVOS);
    }

    @Override
    public Integer RequestTimeOut() {
        Integer frontTimeOut = 60;
        List<SettingItemVO> settingItemVOS = queryBasicSetting();
        for (int i = 0; i < settingItemVOS.size(); i++) {
            SettingItemVO settingItemVO = settingItemVOS.get(i);
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(XpackSettingConstants.Front_Time_Out) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                frontTimeOut = Integer.parseInt(settingItemVO.getPval());
            }
        }
        return frontTimeOut;
    }

    @Override
    public List<Object> ui() {
        return sysParameterManage.getUiList();
    }

    @Override
    public Integer defaultLogin() {
        return sysParameterManage.defaultLogin();
    }
}
