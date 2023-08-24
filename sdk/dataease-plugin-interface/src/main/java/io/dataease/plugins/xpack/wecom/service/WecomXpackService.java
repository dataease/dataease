package io.dataease.plugins.xpack.wecom.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;
import io.dataease.plugins.xpack.wecom.dto.entity.BaseQrResult;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomAuthResult;
import io.dataease.plugins.xpack.wecom.dto.entity.WecomMsgResult;
import io.dataease.plugins.xpack.wecom.dto.response.WecomInfo;

import java.io.File;
import java.util.List;
import java.util.Map;

public abstract class WecomXpackService extends PluginComponentService {

    public abstract WecomInfo info();

    public abstract void save(List<SysSettingDto> settings);

    public abstract void testConn(WecomInfo info) throws Exception;

    public abstract Boolean isOpen();

    public abstract BaseQrResult getQrParam();

    public abstract WecomAuthResult auth(String code);

    public abstract Map<String, Object> userInfo(String userId);

    public abstract WecomMsgResult pushMsg(List<String> userIds, String message);

    public abstract WecomMsgResult pushOaMsg(List<String> userIds, String title, String content, byte[] bytes, List<File> files);

}
