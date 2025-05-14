package io.dataease.exportCenter.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.exportCenter.ExportCenterApi;
import io.dataease.exportCenter.manage.ExportCenterManage;
import io.dataease.exportCenter.util.ExportCenterUtils;
import io.dataease.model.ExportTaskDTO;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/exportCenter")
@Transactional(rollbackFor = Exception.class)
public class ExportCenterServer implements ExportCenterApi {
    @Resource
    private ExportCenterManage exportCenterManage;

    @Override
    public Map<String, Long> exportTasks() {
        return exportCenterManage.exportTasks();
    }

    @Override
    public IPage<ExportTaskDTO> pager(int goPage, int pageSize, String status) {
        Page<ExportTaskDTO> page = new Page<>(goPage, pageSize);
        return exportCenterManage.pager(page, status);
    }

    @Override
    public void delete(String id) {
        exportCenterManage.delete(id);
    }

    @Override
    public void delete(List<String> ids) {
        exportCenterManage.delete(ids);
    }

    @Override
    public void deleteAll(String type) {
        exportCenterManage.deleteAll(type);
    }

    @Override
    public void download(String id, HttpServletResponse response) throws Exception {
        exportCenterManage.download(id, response);
    }

    @Override
    public String generateDownloadUri(String id) throws Exception {
        exportCenterManage.generateDownloadUri(id);
        return "";
    }

    @Override
    public void retry(String id) {
        exportCenterManage.retry(id);
    }

    public String exportLimit() {
        return String.valueOf(ExportCenterUtils.getExportLimit("dataset"));
    }
}
