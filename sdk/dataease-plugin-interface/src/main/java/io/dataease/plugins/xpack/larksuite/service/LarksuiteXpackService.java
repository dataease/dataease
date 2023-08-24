package io.dataease.plugins.xpack.larksuite.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.lark.dto.entity.LarkQrResult;
import io.dataease.plugins.xpack.lark.dto.response.LarkInfo;
import io.dataease.plugins.xpack.larksuite.dto.response.LarksuiteMsgResult;
import io.dataease.plugins.xpack.larksuite.dto.response.LarksuiteUserResult;

import java.io.File;
import java.util.List;

public abstract class LarksuiteXpackService extends PluginComponentService {

    public abstract LarkInfo info();

    public abstract void save(List<SysSettingDto> settings);

    public abstract void testConn(LarkInfo info) throws Exception;

    public abstract Boolean isOpen();

    public abstract LarkQrResult getQrParam();

    public abstract LarksuiteUserResult userInfo(String code, String state, Boolean useBind);

    public abstract LarksuiteMsgResult pushMsg(List<String> userIds, String message);

    public abstract LarksuiteMsgResult pushOaMsg(List<String> userIds, String title, String content, byte[] bytes, List<File> files);
}
