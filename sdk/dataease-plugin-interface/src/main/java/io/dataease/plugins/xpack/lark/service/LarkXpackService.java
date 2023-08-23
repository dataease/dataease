package io.dataease.plugins.xpack.lark.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.lark.dto.entity.LarkMsgResult;
import io.dataease.plugins.xpack.lark.dto.entity.LarkQrResult;
import io.dataease.plugins.xpack.lark.dto.entity.LarkUserInfo;
import io.dataease.plugins.xpack.lark.dto.response.LarkAppUserResult;
import io.dataease.plugins.xpack.lark.dto.response.LarkInfo;


import java.io.File;
import java.util.List;

public abstract class LarkXpackService extends PluginComponentService {

    public abstract LarkInfo info();

    public abstract String appId();

    public abstract void save(List<SysSettingDto> settings);

    public abstract void testConn(LarkInfo info) throws Exception;

    public abstract Boolean isOpen();

    public abstract LarkQrResult getQrParam();

    public abstract LarkUserInfo userInfo(String code, String state, Boolean useBind);

    public abstract LarkAppUserResult userInfoWithoutLogin(String code);

    public abstract LarkMsgResult pushMsg(List<String> userIds, String message);

    public abstract LarkMsgResult pushOaMsg(List<String> userIds, String title, String content, byte[] bytes, List<File> files);
}
