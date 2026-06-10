package io.dataease.exportCenter.manage;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.Verification;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.chart.request.ChartExcelRequest;
import io.dataease.api.dataset.dto.DataSetExportRequest;
import io.dataease.api.export.BaseExportApi;
import io.dataease.api.xpack.dataFilling.DataFillingApi;
import io.dataease.auth.bo.TokenUserBO;
import io.dataease.commons.utils.ExcelWatermarkUtils;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dataset.manage.*;
import io.dataease.exception.DEException;
import io.dataease.exportCenter.dao.auto.entity.CoreExportDownloadTask;
import io.dataease.exportCenter.dao.auto.entity.CoreExportTask;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportDownloadTaskMapper;
import io.dataease.exportCenter.dao.auto.mapper.CoreExportTaskMapper;
import io.dataease.exportCenter.dao.ext.mapper.ExportTaskExtMapper;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.log.DeLog;
import io.dataease.model.ExportTaskDTO;
import io.dataease.constant.XpackSettingConstants;
import io.dataease.system.manage.SysParameterManage;
import io.dataease.utils.*;
import io.dataease.visualization.dao.auto.entity.CoreStore;
import io.dataease.visualization.dao.auto.entity.VisualizationWatermark;
import io.dataease.visualization.dao.auto.mapper.VisualizationWatermarkMapper;
import io.dataease.visualization.dao.ext.mapper.ExtDataVisualizationMapper;
import io.dataease.visualization.server.DataVisualizationServer;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Data;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import io.dataease.visualization.dto.WatermarkContentDTO;
import io.dataease.api.permissions.user.vo.UserFormVO;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Future;

@Component
@Transactional(rollbackFor = Exception.class)
public class ExportCenterManage implements BaseExportApi {
    @Resource
    private CoreExportTaskMapper exportTaskMapper;
    @Resource
    private CoreExportDownloadTaskMapper coreExportDownloadTaskMapper;
    @Resource
    private ExportTaskExtMapper exportTaskExtMapper;
    @Resource
    private DatasetGroupManage datasetGroupManage;
    @Resource
    DataVisualizationServer dataVisualizationServer;
    @Resource
    private ExportCenterDownLoadManage exportCenterDownLoadManage;
    @Resource
    private SysParameterManage sysParameterManage;
    @Value("${dataease.export.core.size:10}")
    private int core;
    @Value("${dataease.export.max.size:10}")
    private int max;

    @Value("${dataease.path.exportData:/opt/dataease2.0/data/exportData/}")
    private String exportData_path;
    @Resource
    private VisualizationWatermarkMapper watermarkMapper;
    @Resource
    private ExtDataVisualizationMapper visualizationMapper;
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
        CoreExportTask exportTask = validateDownloadTask(id, ticket);
        exportCenterDownLoadManage.download(exportTask, response);
    }

    public void delete(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        deleteTask(exportTask);
    }

    public void deleteAll(String type) {
        if (!STATUS.contains(type)) {
            DEException.throwException("无效的状态");
        }
        Long currentUserId = currentUserId();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        if (!type.equalsIgnoreCase("ALL")) {
            queryWrapper.eq("export_status", type);
        }
        List<CoreExportTask> exportTasks = exportTaskMapper.selectList(queryWrapper);
        exportTasks.parallelStream().forEach(this::deleteTask);

    }

    public void delete(List<String> ids) {
        ids.forEach(this::delete);
    }

    public void retry(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        if (!exportTask.getExportStatus().equalsIgnoreCase("FAILED")) {
            DEException.throwException("正在导出中!");
        }
        exportTask.setExportStatus("PENDING");
        exportTask.setExportProgress("0");
        exportTask.setExportMachineName(hostName());
        exportTask.setExportTime(System.currentTimeMillis());
        exportTaskMapper.updateById(exportTask);
        FileUtils.deleteDirectoryRecursively(resolveExportTaskDirectory(id));
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

    public IPage<ExportTaskDTO> pager(Page<ExportTaskDTO> page, String status) {
        if (!STATUS.contains(status)) {
            DEException.throwException("Invalid status: " + status);
        }

        Long currentUserId = currentUserId();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        if (!status.equalsIgnoreCase("ALL")) {
            queryWrapper.eq("export_status", status);
        }
        queryWrapper.orderByDesc("export_time");
        IPage<ExportTaskDTO> pager = exportTaskExtMapper.pager(page, queryWrapper);

        List<ExportTaskDTO> records = pager.getRecords();
        records.forEach(exportTask -> {
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                setExportFromAbsName(exportTask);
            }
            if (status.equalsIgnoreCase("ALL") || status.equalsIgnoreCase(exportTask.getExportStatus())) {
                proxy().setOrg(exportTask);
            }
        });

        return pager;
    }

    public Map<String, Long> exportTasks() {
        Long currentUserId = currentUserId();
        Map<String, Long> result = new HashMap<>();
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "IN_PROGRESS");
        result.put("IN_PROGRESS", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "SUCCESS");
        result.put("SUCCESS", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "FAILED");
        result.put("FAILED", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        queryWrapper.eq("export_status", "PENDING");
        result.put("PENDING", exportTaskMapper.selectCount(queryWrapper));

        queryWrapper.clear();
        queryWrapper.eq("user_id", currentUserId);
        result.put("ALL", exportTaskMapper.selectCount(queryWrapper));
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
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(currentUserId);
        exportTask.setExportFrom(Long.valueOf(exportFrom));
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getViewName() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
        if (busiFlag.equalsIgnoreCase("dashboard")) {
            exportCenterDownLoadManage.startPanelViewTask(exportTask, request);
        } else {
            exportCenterDownLoadManage.startDataVViewTask(exportTask, request);
        }

    }

    public void addTask(Long exportFrom, String exportFromType, DataSetExportRequest request) throws Exception {
        datasetGroupManage.getDatasetGroupInfoDTO(exportFrom, null);
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = new CoreExportTask();
        exportTask.setId(IDUtils.snowID().toString());
        exportTask.setUserId(currentUserId);
        exportTask.setExportFrom(exportFrom);
        exportTask.setExportFromType(exportFromType);
        exportTask.setExportStatus("PENDING");
        exportTask.setFileName(request.getFilename() + ".xlsx");
        exportTask.setExportProgress("0");
        exportTask.setExportTime(System.currentTimeMillis());
        exportTask.setParams(JsonUtil.toJSONString(request).toString());
        exportTask.setExportMachineName(hostName());
        exportTaskMapper.insert(exportTask);
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
        exportTaskMapper.insert(exportTask);
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
        QueryWrapper<CoreExportTask> queryWrapper = new QueryWrapper<>();
        long expTime = Long.parseLong(val) * 24L * 3600L * 1000L;
        long threshold = System.currentTimeMillis() - expTime;
        queryWrapper.lt("export_time", threshold);
        exportTaskMapper.selectList(queryWrapper).forEach(coreExportTask -> {
            deleteTask(coreExportTask);
        });

    }

    public void addWatermarkTools(Workbook wb) {
        VisualizationWatermark watermark = watermarkMapper.selectById("system_default");
        WatermarkContentDTO watermarkContent = JsonUtil.parseObject(watermark.getSettingContent(), WatermarkContentDTO.class);
        if (watermarkContent.getEnable() && watermarkContent.getExcelEnable()) {
            UserFormVO userInfo = visualizationMapper.queryInnerUserInfo(currentUserId());
            // 在主逻辑中添加水印
            int watermarkPictureIdx = ExcelWatermarkUtils.addWatermarkImage(wb, watermarkContent, userInfo); // 生成水印图片并获取 ID
            for (Sheet sheet : wb) {
                ExcelWatermarkUtils.addWatermarkToSheet(sheet, watermarkPictureIdx); // 为每个 Sheet 添加水印
            }
        }
    }

    @DeLog(id = "#p0", ot = LogOT.DOWNLOAD, st = LogST.DATA)
    public String generateDownloadUri(String id) {
        CoreExportTask exportTask = getCurrentUserExportTask(id);
        long createTime = System.currentTimeMillis();
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskMapper.selectById(id);
        if (coreExportDownloadTask != null) {
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTaskMapper.updateById(coreExportDownloadTask);
        } else {
            coreExportDownloadTask = new CoreExportDownloadTask();
            coreExportDownloadTask.setId(id);
            coreExportDownloadTask.setCreateTime(createTime);
            coreExportDownloadTask.setValidTime(5L);
            coreExportDownloadTaskMapper.insert(coreExportDownloadTask);
        }
        return "/exportCenter/download/" + id + "?ticket=" + buildDownloadTicket(exportTask, createTime, coreExportDownloadTask.getValidTime());
    }

    private CoreExportTask getCurrentUserExportTask(String id) {
        Long currentUserId = currentUserId();
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
        if (exportTask == null || !Objects.equals(exportTask.getUserId(), currentUserId)) {
            DEException.throwException("任务不存在");
        }
        return exportTask;
    }

    private void deleteTask(CoreExportTask exportTask) {
        if (exportTask == null) {
            return;
        }
        String id = exportTask.getId();
        Iterator<Map.Entry<String, Future>> iterator = Running_Task.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Future> entry = iterator.next();
            if (entry.getKey().equalsIgnoreCase(id)) {
                entry.getValue().cancel(true);
                iterator.remove();
            }
        }
        FileUtils.deleteDirectoryRecursively(resolveExportTaskDirectory(id));
        exportTaskMapper.deleteById(id);
    }

    private Path resolveExportTaskDirectory(String taskId) {
        if (StringUtils.isBlank(taskId) || !StringUtils.isNumeric(taskId)) {
            DEException.throwException("任务不存在");
        }
        Path exportBasePath = Paths.get(exportData_path).toAbsolutePath().normalize();
        Path exportTaskPath = exportBasePath.resolve(taskId).normalize();
        if (!exportTaskPath.startsWith(exportBasePath)) {
            DEException.throwException("Invalid export task path");
        }
        return exportTaskPath;
    }

    public CoreExportTask validateDownloadTask(String id, String ticket) {
        if (StringUtils.isBlank(ticket)) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        CoreExportDownloadTask coreExportDownloadTask = coreExportDownloadTaskMapper.selectById(id);
        if (coreExportDownloadTask == null) {
            DEException.throwException(Translator.get("i18n_download_link_invalid"));
        }
        CoreExportTask exportTask = exportTaskMapper.selectById(id);
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
        coreExportDownloadTaskMapper.deleteById(id);
        return exportTask;
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void checkDownLoadInfos() {
        coreExportDownloadTaskMapper.selectList(null).forEach(downLoadInfo -> {
            if (System.currentTimeMillis() - downLoadInfo.getCreateTime() > downLoadInfo.getValidTime() * 60 * 1000) {
                coreExportDownloadTaskMapper.deleteById(downLoadInfo.getId());
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

    private Long currentUserId() {
        TokenUserBO user = AuthUtils.getUser();
        if (user != null && user.getUserId() != null) {
            return user.getUserId();
        }
        String embeddedToken = ServletUtils.getHead(io.dataease.constant.AuthConstant.EMBEDDED_TOKEN_KEY);
        if (StringUtils.isBlank(embeddedToken)) {
            DEException.throwException("user not found");
        }
        Object apisixTokenManage = CommonBeanFactory.getBean("apisixTokenManage");
        if (apisixTokenManage == null) {
            DEException.throwException("user not found");
        }
        Method validateEmbeddedTokenMethod = ReflectionUtils.findMethod(apisixTokenManage.getClass(), "validateEmbeddedToken", String.class);
        Object tokenBO = ReflectionUtils.invokeMethod(validateEmbeddedTokenMethod, apisixTokenManage, embeddedToken);
        if (tokenBO == null) {
            DEException.throwException("user not found");
        }
        Method getUserIdMethod = DeReflectUtil.findMethod(tokenBO.getClass(), "getUserId");
        Object userId = ReflectionUtils.invokeMethod(getUserIdMethod, tokenBO);
        if (!(userId instanceof Long)) {
            DEException.throwException("user not found");
        }
        return (Long) userId;
    }
}
