package io.dataease.plugins.xpack.email.service;

import java.util.List;

import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.common.service.PluginMenuService;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackPixelEntity;
import io.dataease.plugins.xpack.email.dto.response.XpackEmailTemplateDTO;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskEntity;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskGridDTO;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskInstanceDTO;

public abstract class EmailXpackService extends PluginMenuService {

    public abstract int save(XpackEmailTaskRequest request) throws Exception;

    public abstract List<XpackTaskGridDTO> taskGrid(XpackGridRequest request);

    public abstract Boolean status(GlobalTaskEntity taskEntity);

    public abstract List<XpackTaskInstanceDTO> taskInstanceGrid(XpackGridRequest request);

    public abstract void delete(Long taskId) throws Exception;

    public abstract void stop(Long taskId) throws Exception;

    public abstract Boolean start(Long taskId) throws Exception;

    public abstract XpackEmailTaskRequest taskForm(Long taskId);

    public abstract XpackTaskEntity taskDetail(Long taskId);

    public abstract Long saveInstance(GlobalTaskInstance instance);

    public abstract void delInstance(Long instanceId);

    public abstract GlobalTaskInstance instanceForm(Long instanceId);

    public abstract byte[] print(String url, String token, XpackPixelEntity XpackPixelEntity) throws Exception;

    public abstract byte[] printPdf(String url, String token, XpackPixelEntity XpackPixelEntity, boolean showPageNo, boolean picture2pdf) throws Exception;

    public abstract List<GlobalTaskEntity> allTask();

    public abstract XpackEmailTemplateDTO emailTemplate(Long taskId);

    public abstract byte[] printData(String url, String token, XpackPixelEntity XpackPixelEntity) throws Exception;

    public abstract void batchDel(List<Long> taskIds);

    public abstract void export(XpackGridRequest request);

    public abstract Boolean status(Long taskId);

}
