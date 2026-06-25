package io.dataease.exportCenter.manage;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.auth0.jwt.interfaces.Verification;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.export.BaseExportApi;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.dataease.api.permissions.user.api.UserApi;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dao.auto.entity.CoreExportTask;
import io.dataease.dataset.manage.*;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.entity.CoreExportDownloadTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportDownloadTaskRepository;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskRepository;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.ExportTaskDTO;
import io.dataease.constant.XpackSettingConstants;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.PageResult;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.CoreStore;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkRepository;

import io.dataease.visualization.server.DataVisualizationServer;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
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
import org.springframework.util.ReflectionUtils;
import io.dataease.visualization.dto.WatermarkContentDTO;
import io.dataease.api.permissions.user.vo.UserFormVO;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

    @XpackInteract(value = "perSetting", replace = true)
    public String singleValue(String key) {
        return "sync";
    }

    public void download(String id, String ticket, HttpServletResponse response) throws Exception {
        String safeTaskId = validateExportTaskId(id);
        CoreExportTask exportTask = validateDownloadTask(safeTaskId, ticket);
        exportCenterDownLoadManage.download(resolveDownloadTarget(safeTaskId, exportTask), resolveDownloadFileName(exportTask), response);
    }

    public void delete(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(validateExportTaskId(id));
        deleteTask(exportTask);
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
        exportTasks.parallelStream().forEach(this::deleteTask);

    }

    public void delete(List<String> ids) {
        ids.forEach(this::delete);
    }

    public void retry(String id) {
        String safeTaskId = validateExportTaskId(id);
        CoreExportTask exportTask = getCurrentUserExportTask(safeTaskId);
        if (!exportTask.getExportStatus().equalsIgnoreCase("FAILED")) {
            DEException.throwException("正在导出中!");
        }
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        deleteExportTaskDirectory(resolveExportTaskDirectory(safeTaskId));
        if (exportTask.getExportFromType().equalsIgnoreCase("chart")) {
            ChartExcelRequest request = JsonUtil.parseObject(exportTask.getParams(), ChartExcelRequest.class);
            exportCenterDownLoadManage.startViewTask(resolveExportTaskFileTarget(safeTaskId), request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("dataset")) {
            DataSetExportRequest request = JsonUtil.parseObject(exportTask.getParams(), DataSetExportRequest.class);
            exportCenterDownLoadManage.startDatasetTask(resolveExportTaskFileTarget(safeTaskId), exportTask.getExportFrom(), request);
        }
        if (exportTask.getExportFromType().equalsIgnoreCase("data_filling")) {
            HashMap request = JsonUtil.parseObject(exportTask.getParams(), HashMap.class);
            exportCenterDownLoadManage.startDataFillingTask(resolveExportTaskFileTarget(safeTaskId), exportTask.getExportFrom(), exportTask.getUserId(), request);
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
        coreExportTaskRepository.saveAndFlush(exportTask);
        String safeTaskId = validateExportTaskId(exportTask.getId());
        if (busiFlag.equalsIgnoreCase("dashboard")) {
            exportCenterDownLoadManage.startPanelViewTask(resolveExportTaskFileTarget(safeTaskId), request);
        } else {
            exportCenterDownLoadManage.startDataVViewTask(resolveExportTaskFileTarget(safeTaskId), request);
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
        coreExportTaskRepository.saveAndFlush(exportTask);
        String safeTaskId = validateExportTaskId(exportTask.getId());
        exportCenterDownLoadManage.startDatasetTask(resolveExportTaskFileTarget(safeTaskId), exportTask.getExportFrom(), request);
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
        coreExportTaskRepository.saveAndFlush(exportTask);
        if (StringUtils.equals(exportFromType, "data_filling")) {
            String safeTaskId = validateExportTaskId(exportTask.getId());
            exportCenterDownLoadManage.startDataFillingTask(resolveExportTaskFileTarget(safeTaskId), exportTask.getExportFrom(), exportTask.getUserId(), request);
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
    public String generateDownloadUri(String id) {
        String safeTaskId = validateExportTaskId(id);
        CoreExportTask exportTask = getCurrentUserExportTask(safeTaskId);
        long createTime = System.currentTimeMillis();
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskRepository.findById(safeTaskId).orElse(null);
        if (coreExportDownloadTask != null) {
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTaskRepository.saveAndFlush(coreExportDownloadTask);
        } else {
            coreExportDownloadTask = new CoreExportDownloadTask();
            coreExportDownloadTask.setId(safeTaskId);
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTask.setValidTime(5L);
            coreExportDownloadTaskRepository.saveAndFlush(coreExportDownloadTask);
        }
        return "/exportCenter/download/" + safeTaskId + "?ticket=" + buildDownloadTicket(exportTask, createTime, coreExportDownloadTask.getValidTime());
    }

    private CoreExportTask getCurrentUserExportTask(String id) {
        CoreExportTask exportTask = coreExportTaskRepository.findById(id).orElse(null);
        if (exportTask == null || !Objects.equals(exportTask.getUserId(), V3UserUtil.getUid())) {
            DEException.throwException("任务不存在");
        }
        return exportTask;
    }

    private void deleteTask(CoreExportTask exportTask) {
        if (exportTask == null) {
            return;
        }
        String id = validateExportTaskId(exportTask.getId());
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(id)) {
                entry.getValue().cancel(true);
                iterator.remove();
            }
        }
        deleteExportTaskDirectory(resolveExportTaskDirectory(id));
        coreExportTaskRepository.deleteById(id);
    }

    private Path resolveExportBasePath() {
        return Paths.get(exportData_path).toAbsolutePath().normalize();
    }

    private Path resolveExportTaskDirectory(String taskId) {
        Path exportBasePath = resolveExportBasePath();
        Path exportTaskPath = exportBasePath.resolve(taskId).normalize();
        if (!exportTaskPath.startsWith(exportBasePath)) {
            DEException.throwException("Invalid export task path");
        }
        return exportTaskPath;
    }

    private Path resolveExportTaskFilePath(String taskId) {
        Path exportTaskDirectory = resolveExportTaskDirectory(taskId);
        Path exportFilePath = exportTaskDirectory.resolve(taskId + ".xlsx").normalize();
        if (!exportFilePath.startsWith(exportTaskDirectory)) {
            DEException.throwException("Invalid export task file path");
        }
        return exportFilePath;
    }

    private ExportTaskFileTarget resolveExportTaskFileTarget(String taskId) {
        return new ExportTaskFileTarget(taskId, resolveExportTaskFilePath(taskId));
    }

    private ExportTaskFileTarget resolveDownloadTarget(String taskId, CoreExportTask exportTask) {
        if (exportTask.getExportTime() < 1730277243491L) {
            return new ExportTaskFileTarget(taskId, resolveExportTaskFilePath(taskId, resolveDownloadFileName(exportTask)));
        }
        return resolveExportTaskFileTarget(taskId);
    }

    private Path resolveExportTaskFilePath(String taskId, String fileName) {
        FileUtils.validateUploadFilename(fileName);
        Path exportTaskDirectory = resolveExportTaskDirectory(taskId);
        Path exportFilePath = exportTaskDirectory.resolve(fileName).normalize();
        if (!exportFilePath.startsWith(exportTaskDirectory)) {
            DEException.throwException("Invalid export task file path");
        }
        return exportFilePath;
    }

    private String resolveDownloadFileName(CoreExportTask exportTask) {
        String fileName = exportTask.getFileName();
        FileUtils.validateUploadFilename(fileName);
        return fileName;
    }

    private String validateExportTaskId(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            DEException.throwException("任务不存在");
        }
        return taskId;
    }

    private void deleteExportTaskDirectory(Path exportTaskPath) {
        Path exportBasePath = resolveExportBasePath();
        if (Files.notExists(exportTaskPath)) {
            return;
        }
        try {
            Files.walkFileTree(exportTaskPath, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws java.io.IOException {
                    validateExportPath(exportBasePath, file);
                    Files.deleteIfExists(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, java.io.IOException exc) throws java.io.IOException {
                    if (exc != null) {
                        throw exc;
                    }
                    validateExportPath(exportBasePath, dir);
                    Files.deleteIfExists(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (java.io.IOException e) {
            DEException.throwException(e);
        }
    }

    private void validateExportPath(Path exportBasePath, Path targetPath) {
        Path normalizedPath = targetPath.toAbsolutePath().normalize();
        if (!normalizedPath.startsWith(exportBasePath)) {
            DEException.throwException("Invalid export task path");
        }
    }

    public CoreExportTask validateDownloadTask(String id, String ticket) {
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskRepository.findById(id).orElse(null);
        if (coreExportDownloadTask == null) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        CoreExportTask exportTask = coreExportTaskRepository.findById(id).orElse(null);
        if (exportTask == null) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        try {
            Algorithm algorithm = Algorithm.HMAC256(resolveTicketSecret(exportTask.getUserId()));
            Verification verification = JWT.require(algorithm);
            JWTVerifier verifier = verification.build();
            DecodedJWT jwt = verifier.verify(ticket);
            String taskId = jwt.getClaim("taskId").asString();
            Long uid = jwt.getClaim("uid").asLong();
            Long ticketTime = jwt.getClaim("ts").asLong();
            if (!StringUtils.equals(id, taskId)
                    || !Objects.equals(uid, exportTask.getUserId())
                    || !Objects.equals(ticketTime, coreExportDownloadTask.getCreateTime())
                    || System.currentTimeMillis() - coreExportDownloadTask.getCreateTime() > coreExportDownloadTask.getValidTime() * 60 * 1000) {
                DEException.throwException(Translator.get("i18n_download_link_invalid"));
            }
        } catch (Exception e) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        coreExportDownloadTaskRepository.deleteById(id);
        return exportTask;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void checkDownLoadInfos() {
        coreExportDownloadTaskRepository.findAll().forEach(downLoadInfo -> {
            if (System.currentTimeMillis() - downLoadInfo.getCreateTime() > downLoadInfo.getValidTime() * 60 * 1000) {
                coreExportDownloadTaskRepository.deleteById(downLoadInfo.getId());
            }
        });
    }

    @Data
    public class DownLoadInfo {
        String id;
        Long validTime; // 单位：minutes
        Long createTime;
    }

    private String buildDownloadTicket(CoreExportTask exportTask, long createTime, Long validTime) {
        Algorithm algorithm = Algorithm.HMAC256(resolveTicketSecret(exportTask.getUserId()));
        return JWT.create()
                .withClaim("taskId", exportTask.getId())
                .withClaim("uid", exportTask.getUserId())
                .withClaim("ts", createTime)
                .withExpiresAt(new Date(createTime + validTime * 60 * 1000))
                .sign(algorithm);
    }

    private String resolveTicketSecret(Long userId) {
        String secret = null;
        if (ObjectUtils.isEmpty(CommonBeanFactory.getBean("loginServer"))) {
            secret = io.dataease.auth.config.SubstituleLoginConfig.getTokenSecret();
        } else {
            Object apisixCacheManage = CommonBeanFactory.getBean("apisixCacheManage");
            Method userCacheMethod = DeReflectUtil.findMethod(apisixCacheManage.getClass(), "userCacheBO");
            Object cacheBO = ReflectionUtils.invokeMethod(userCacheMethod, apisixCacheManage, userId);
            Method secretMethod = DeReflectUtil.findMethod(cacheBO.getClass(), "getSecret");
            Object secretObj = ReflectionUtils.invokeMethod(secretMethod, cacheBO);
            if (secretObj != null) {
                secret = secretObj.toString();
            }
        }
        if (StringUtils.isBlank(secret)) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        return secret;
    }

}
