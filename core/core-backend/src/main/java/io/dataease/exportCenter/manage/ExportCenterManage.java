package io.dataease.exportCenter.manage;

import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.export.BaseExportApi;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dao.auto.entity.CoreExportTask;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.entity.CoreExportDownloadTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportDownloadTaskRepository;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskRepository;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.ExportTaskDTO;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.PageResult;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository;
import io.dataease.visualization.dto.WatermarkContentDTO;
import io.dataease.visualization.server.DataVisualizationServer;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.Future;
import java.util.function.Function;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterManage implements BaseExportApi {
    @Resource
    private CoreExportTaskRepository coreExportTaskRepository;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    DataVisualizationServer dataVisualizationServer;
    @Resource
    private ExportCenterDownLoadManage exportCenterDownLoadManage;
    @Resource
    private SysParameterManage sysParameterManage;
    @Value("${dataease.path.exportData:/opt/dataease3.0/data/exportData/}")
    private String exportData_path;
    @Resource
    private VisualizationWatermarkRepository visualizationWatermarkRepository;
    @Autowired
    private CoreExportDownloadTaskRepository coreExportDownloadTaskRepository;
    static private List<String> STATUS = Arrays.asList("SUCCESS", "FAILED", "PENDING", "IN_PROGRESS", "ALL");
    private Map<String, Future> Running_Task = new HashMap<>();
    @Autowired(required = false)
    private DataFillingApi dataFillingApi = null;

    private DataFillingApi getDataFillingApi() {
        return dataFillingApi;
    }


    public void download(String id, HttpServletResponse response) throws Exception {
        if (!coreExportDownloadTaskRepository.existsById(id)) {
            DEException.throwException("任务不存在");
        }
        CoreExportTask exportTask = coreExportTaskRepository.findById(id).orElse(null);
        exportCenterDownLoadManage.download(exportTask, response);
    }

    public void delete(String id) {
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(id)) {
                entry.getValue().cancel(true);
                iterator.remove();
            }
        }
        FileUtils.deleteFile(exportData_path + id);
        coreExportTaskRepository.deleteById(id);
    }

    public void deleteAll(String type) {
        if (!STATUS.contains(type)) {
            DEException.throwException("无效的状态");
        }
        Specification<CoreExportTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), V3UserUtil.getUid()));
            if (!type.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(root.get("exportStatus"), type));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<CoreExportTask> exportTasks = coreExportTaskRepository.findAll(spec);
        exportTasks.parallelStream().forEach(exportTask -> {
            Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Future> entry = iterator.next();
                if (entry.getKey().equalsIgnoreCase(exportTask.getId())) {
                    entry.getValue().cancel(true);
                    iterator.remove();
                }
            }
            FileUtils.deleteFile(exportData_path + exportTask.getId());
            coreExportTaskRepository.deleteById(exportTask.getId());
        });

    }

    public void delete(List<String> ids) {
        ids.forEach(this::delete);
    }

    public void retry(String id) {
        CoreExportTask exportTask = coreExportTaskRepository.findById(id).orElse(null);
        if (!exportTask.getExportStatus().equalsIgnoreCase("FAILED")) {
            DEException.throwException("正在导出中!");
        }
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        FileUtils.deleteFile(exportData_path + id);
        if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
            ChartExcelRequest request = JsonUtil.parseObject(exportTask.getParams(), ChartExcelRequest.class);
            exportCenterDownLoadManage.startViewTask(exportTask, request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("dataset")) {
            DataSetExportRequest request = JsonUtil.parseObject(exportTask.getParams(), DataSetExportRequest.class);
            exportCenterDownLoadManage.startDatasetTask(exportTask, request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("data_filling")) {
            HashMap request = JsonUtil.parseObject(exportTask.getParams(), HashMap.class);
            exportCenterDownLoadManage.startDataFillingTask(exportTask, request);
        }
    }

    public PageResult<ExportTaskDTO> pager(int goPage, int pageSize, String status) {
        if (!STATUS.contains(status)) {
            DEException.throwException("Invalid status: " + status);
        }
        Pageable pageable = PageRequest.of(goPage - 1, pageSize, Sort.by(Sort.Direction.DESC, "exportTime"));
        Specification<CoreExportTask> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("userId"), V3UserUtil.getUid()));
            if (!status.equalsIgnoreCase("ALL")) {
                predicates.add(cb.equal(root.get("exportStatus"), status));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<ExportTaskDTO> pager = coreExportTaskRepository.findAll(spec, pageable).map(coreExportToDtoConverter);
        pager.getContent().forEach(exportTask -> {
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                setExportFromAbsName(exportTask);
            }
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                proxy().setOrg(exportTask);
            }
        });
        return new PageResult<>(pager.getContent(), pager.getTotalElements(), pageable);
    }

    private Function<CoreExportTask, ExportTaskDTO> coreExportToDtoConverter = c -> {
        ExportTaskDTO dto = new ExportTaskDTO();
        BeanUtils.copyBean(dto, c);
        return dto;
    };


    public Map<String, Long> exportTasks() {
        Map<String, Long> result = new HashMap<>();
        result.put("IN_PROGRESS", coreExportTaskRepository.countByUserIdAndExportStatus(V3UserUtil.getUid(), "IN_PROGRESS"));

        result.put("SUCCESS", coreExportTaskRepository.countByUserIdAndExportStatus(V3UserUtil.getUid(), "SUCCESS"));

        result.put("FAILED", coreExportTaskRepository.countByUserIdAndExportStatus(V3UserUtil.getUid(), "FAILED"));

        result.put("PENDING", coreExportTaskRepository.countByUserIdAndExportStatus(V3UserUtil.getUid(), "PENDING"));

        result.put("ALL", coreExportTaskRepository.countByUserId(V3UserUtil.getUid()));
        return result;
    }

    @XpackInteract(value = "exportCenter", before = false)
    public void setOrg(ExportTaskDTO exportTaskDTO) {
    }

    private ExportCenterManage proxy() {
        return CommonBeanFactory.getBean(ExportCenterManage.class);
    }

    private void setExportFromAbsName(ExportTaskDTO exportTaskDTO) {
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("chart")) {
            exportTaskDTO.setExportFromName(dataVisualizationServer.getAbsPath(exportTaskDTO.getExportFrom()));
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("dataset")) {
            List<String> fullName = new ArrayList<>();
            datasetGroupManage.geFullName(Long.valueOf(exportTaskDTO.getExportFrom()), fullName);
            Collections.reverse(fullName);
            List<String> finalFullName = fullName;
            exportTaskDTO.setExportFromName(String.join("/", finalFullName));
        }
        if (exportTaskDTO.getExportFromType().equalsIgnoreCase("data_filling")) {
            List<String> fullName = new ArrayList<>();
            getDataFillingApi().geFullName(Long.valueOf(exportTaskDTO.getExportFrom()), fullName);
            Collections.reverse(fullName);
            List<String> finalFullName = fullName;
            exportTaskDTO.setExportFromName(String.join("/", finalFullName));
        }
    }

    private String hostName() {
        String hostname = null;
        try {
            InetAddress localMachine = InetAddress.getLocalHost();
            hostname = localMachine.getHostName();
        } catch (Exception e) {
            DEException.throwException("请设置主机名！");
        }
        return hostname;
    }

    public void addTask(String exportFrom, String exportFromType, ChartExcelRequest request, String busiFlag) {
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(V3UserUtil.getUid());
        exportTask.setExportFrom(Long.valueOf(exportFrom));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getViewName() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        if (busiFlag.equalsIgnoreCase("dashboard")) {
            exportCenterDownLoadManage.startPanelViewTask(exportTask, request);
        } else {
            exportCenterDownLoadManage.startDataVViewTask(exportTask, request);
        }
    }

    public void addTask(Long exportFrom, String exportFromType, DataSetExportRequest request) throws Exception {
        datasetGroupManage.getDatasetGroupInfoDTO(exportFrom, null);
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(UUID.randomUUID().toString());
        exportTask.setUserId(V3UserUtil.getUid());
        exportTask.setExportFrom(exportFrom);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getFilename() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportCenterDownLoadManage.startDatasetTask(exportTask, request);
    }

    @Override
    public void addTask(String exportFromId, String exportFromType, HashMap<String, Object> request, Long userId, Long org) {
        CoreExportTask exportTask = new CoreExportTask();
        request.put("org", org);
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(userId);
        exportTask.setExportFrom(Long.valueOf(exportFromId));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.get("name") + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        if (StringUtils.equals(exportFromType, "data_filling")) {
            exportCenterDownLoadManage.startDataFillingTask(exportTask, request);
        }
    }

    public void cleanLog() {
        String key = "basic.exportFileLiveTime";
        String val = sysParameterManage.singleVal(key);
        if (StringUtils.isBlank(val)) {
            DEException.throwException("未获取到文件保留时间");
        }
        long expTime = Long.parseLong(val) * 24L * 3600L * 1000L;
        long threshold = System.currentTimeMillis() - expTime;
        coreExportTaskRepository.deleteByExportTimeLessThan(threshold);

    }

    public void addWatermarkTools(Workbook wb) {
        VisualizationWatermark watermark = visualizationWatermarkRepository.findById("system_default").orElse(null);
        WatermarkContentDTO watermarkContent = JsonUtil.parseObject(watermark.getSettingContent(), WatermarkContentDTO.class);
        if (watermarkContent.getEnable() && watermarkContent.getExcelEnable()) {
            UserFormVO userInfo = CommonBeanFactory.getBean(UserApi.class).queryById(V3UserUtil.getUid());
            // 在主逻辑中添加水印
            int watermarkPictureIdx = ExcelWatermarkUtils.addWatermarkImage(wb, watermarkContent, userInfo); // 生成水印图片并获取 ID
            for (Sheet sheet : wb) {
                ExcelWatermarkUtils.addWatermarkToSheet(sheet, watermarkPictureIdx); // 为每个 Sheet 添加水印
            }
        }
    }

    @DeLog(id = "#p0", ot = LogOT.DOWNLOAD, st = LogST.DATA)
    public void generateDownloadUri(String id) {
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskRepository.findById(id).orElse(null);
        if (coreExportDownloadTask != null) {
            coreExportDownloadTask.setCreateTime(System.currentTimeMillis());
            coreExportDownloadTaskRepository.saveAndFlush(coreExportDownloadTask);
        } else {
            coreExportDownloadTask = new CoreExportDownloadTask();
            coreExportDownloadTask.setId(id);
            coreExportDownloadTask.setCreateTime(System.currentTimeMillis());
            coreExportDownloadTask.setValidTime(5L);
            coreExportDownloadTaskRepository.saveAndFlush(coreExportDownloadTask);
        }
    }


    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void checkDownLoadInfos() {
        coreExportDownloadTaskRepository.findAll().forEach(downLoadInfo -> {
            if (System.currentTimeMillis() - downLoadInfo.getCreateTime() > downLoadInfo.getValidTime() * 60 * 1000) {
                coreExportDownloadTaskRepository.deleteById(downLoadInfo.getId());
            }
        });
    }

}

