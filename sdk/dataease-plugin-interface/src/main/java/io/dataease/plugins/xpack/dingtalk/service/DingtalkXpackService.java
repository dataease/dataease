package io.dataease.plugins.xpack.dingtalk.service;

import io.dataease.plugins.common.service.PluginComponentService;
import io.dataease.plugins.xpack.dingtalk.dto.entity.DingtalkMsgResult;
import io.dataease.plugins.xpack.dingtalk.dto.response.DingQrResult;
import io.dataease.plugins.xpack.dingtalk.dto.response.DingUserEntity;
import io.dataease.plugins.xpack.dingtalk.dto.response.DingtalkInfo;
import io.dataease.plugins.xpack.display.dto.response.SysSettingDto;

import java.io.File;
import java.util.List;


public abstract class DingtalkXpackService extends PluginComponentService {

    public abstract DingtalkInfo info();

    public abstract void save(List<SysSettingDto> settings);

    public abstract void testConn(DingtalkInfo info) throws Exception;

    public abstract Boolean isOpen();

    public abstract DingQrResult getQrParam();

    public abstract DingUserEntity userInfo(String loginTmpCode);

    public abstract DingUserEntity userInfoWithoutLogin(String code);

    public abstract DingtalkMsgResult pushMsg(List<String> userIds, String message);

    public abstract DingtalkMsgResult pushOaMsg(List<String> userIds, String title, String content, byte[] bytes, List<File> files);

}
