package io.dataease.system.server;

import io.dataease.api.system.SysParameterApi;
import io.dataease.api.system.request.OnlineMapEditor;
import io.dataease.api.system.vo.SettingItemVO;
import io.dataease.api.system.vo.ShareBaseVO;
import io.dataease.constant.StaticResourceConstants;
import io.dataease.constant.XpackSettingConstants;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.manage.SysParameterManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        int frontTimeOut = 60;
        List<SettingItemVO> settingItemVOS = queryBasicSetting();
        for (SettingItemVO settingItemVO : settingItemVOS) {
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(XpackSettingConstants.Front_Time_Out) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                frontTimeOut = Integer.parseInt(settingItemVO.getPval());
            }
        }
        return frontTimeOut;
    }

    @Override
    public Map<String, Object> defaultSettings() {
        Map<String, Object> map = new HashMap<>();
        map.put(XpackSettingConstants.DEFAULT_SORT, "1");

        List<SettingItemVO> settingItemVOS = queryBasicSetting();
        for (SettingItemVO settingItemVO : settingItemVOS) {
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(XpackSettingConstants.DEFAULT_SORT) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                map.put(XpackSettingConstants.DEFAULT_SORT, settingItemVO.getPval());
            }
            if (StringUtils.isNotBlank(settingItemVO.getPkey()) && settingItemVO.getPkey().equalsIgnoreCase(XpackSettingConstants.DEFAULT_OPEN) && StringUtils.isNotBlank(settingItemVO.getPval())) {
                map.put(XpackSettingConstants.DEFAULT_OPEN, settingItemVO.getPval());
            }
        }
        return map;
    }

    @Override
    public List<Object> ui() {
        return sysParameterManage.getUiList();
    }

    @Override
    public Integer defaultLogin() {
        return sysParameterManage.defaultLogin();
    }

    @Override
    public ShareBaseVO shareBase() {
        return sysParameterManage.shareBase();
    }

    @Override
    public Map<String, String> i18nOptions() {
        File dir = new File(StaticResourceConstants.I18N_DIR);
        File[] files = null;
        if (!dir.exists() || ObjectUtils.isEmpty(files = dir.listFiles())) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (File file : files) {
            String name = file.getName();
            int start = name.indexOf("custom_") + 7;
            int end = name.indexOf("front");
            String i18nName = name.substring(start, end - 1).replace("_", "-");
            String languageName = name.substring(end + 6, name.lastIndexOf("."));
            result.put(i18nName, languageName);
        }
        return result;
    }

}
