package io.dataease.plugins.server;

import cn.hutool.core.io.FileUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import io.dataease.auth.api.dto.CurrentUserDto;
import io.dataease.commons.exception.DEException;
import io.dataease.commons.model.excel.ExcelSheetModel;
import io.dataease.commons.pool.PriorityThreadPoolExecutor;
import io.dataease.commons.utils.*;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.entity.GlobalTaskEntity;
import io.dataease.plugins.common.entity.GlobalTaskInstance;
import io.dataease.plugins.common.entity.XpackConditionEntity;
import io.dataease.plugins.common.entity.XpackGridRequest;
import io.dataease.plugins.config.SpringContextUtil;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailCreate;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailTaskRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackEmailViewRequest;
import io.dataease.plugins.xpack.email.dto.request.XpackPixelEntity;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskGridDTO;
import io.dataease.plugins.xpack.email.dto.response.XpackTaskInstanceDTO;
import io.dataease.plugins.xpack.email.service.EmailXpackService;
import io.dataease.service.ScheduleService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import java.io.*;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

@ApiIgnore
@RequestMapping("/plugin/task")
@RestController
public class XEmailTaskServer {

    @Autowired
    private ScheduleService scheduleService;

    @Resource
    private PriorityThreadPoolExecutor priorityExecutor;

    @RequiresPermissions("task-email:read")
    @PostMapping("/queryTasks/{goPage}/{pageSize}")
    public Pager<List<XpackTaskGridDTO>> queryTask(@PathVariable int goPage, @PathVariable int pageSize,
            @RequestBody XpackGridRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        CurrentUserDto user = AuthUtils.getUser();
        if (!user.getIsAdmin()) {
            Long userId = user.getUserId();
            XpackConditionEntity condition = new XpackConditionEntity();
            condition.setField("u.user_id");
            condition.setOperator("eq");
            condition.setValue(userId);
            List<XpackConditionEntity> conditions = CollectionUtils.isEmpty(request.getConditions()) ? new ArrayList<>() : request.getConditions();
            conditions.add(condition);
            request.setConditions(conditions);
        }

        List<XpackTaskGridDTO> tasks = emailXpackService.taskGrid(request);
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.forEach(item -> {
                if (CronUtils.taskExpire(item.getEndTime())) {
                    item.setNextExecTime(null);
                }else {
                    GlobalTaskEntity globalTaskEntity = new GlobalTaskEntity();
                    globalTaskEntity.setRateType(item.getRateType());
                    globalTaskEntity.setRateVal(item.getRateVal());
                    try{
                        String cron = CronUtils.cron(globalTaskEntity);
                        if (StringUtils.isNotBlank(cron)) {
                            Long nextTime = CronUtils.getNextTriggerTime(cron).getTime();
                            item.setNextExecTime(nextTime);
                        }
                    }catch (Exception e) {
                        item.setNextExecTime(null);
                    }
                }

            });
        }

        Pager<List<XpackTaskGridDTO>> listPager = PageUtils.setPageInfo(page, tasks);
        return listPager;
    }

    @RequiresPermissions("task-email:add")
    @PostMapping("/save")
    public void save(@RequestBody XpackEmailCreate param) throws Exception {
        XpackEmailTaskRequest request = param.fillContent();
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        request.setCreator(AuthUtils.getUser().getUserId());
        emailXpackService.save(request);
        GlobalTaskEntity globalTask = BeanUtils.copyBean(new GlobalTaskEntity(), request);
        scheduleService.addSchedule(globalTask);
    }

    @RequiresPermissions("task-email:read")
    @PostMapping("/queryForm/{taskId}")
    public XpackEmailCreate queryForm(@PathVariable Long taskId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);

        XpackEmailTaskRequest taskForm = emailXpackService.taskForm(taskId);
        XpackEmailCreate xpackEmailCreate = new XpackEmailCreate();
        byte[] bytes = taskForm.getContent();

        if (ObjectUtils.isNotEmpty(bytes)) {
            String emailContent;
            try {
                emailContent = new String(bytes, "UTF-8");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            taskForm.setContent(null);
            xpackEmailCreate.setEmailContent(emailContent);
        }
        xpackEmailCreate.setRequest(taskForm);
        return xpackEmailCreate;
    }

    @PostMapping("/preview")
    public String preview(@RequestBody XpackEmailViewRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        String panelId = request.getPanelId();
        String content = request.getContent();

        String url = ServletUtils.domain() + "/#/previewScreenShot/" + panelId + "/true";

        String token = ServletUtils.getToken();
        String fileId = null;
        try {
            Future<?> future = priorityExecutor.submit(() -> {
                try {
                    return emailXpackService.print(url, token, buildPixel(request.getPixel()));
                } catch (Exception e) {
                    LogUtil.error(e.getMessage(), e);
                    DEException.throwException("预览失败，请联系管理员");
                }
                return null;
            }, 0);
            Object object = future.get();
            if (ObjectUtils.isNotEmpty(object)) {
                fileId = object.toString();
            }
        } catch (Exception e) {
            LogUtil.error(e.getMessage(), e);
            DEException.throwException("预览失败，请联系管理员");
        }
        String imageUrl = "/system/ui/image/" + fileId;
        String html = "<div>" +
                "<h2>" + content + "</h2>" +
                "<img style='width: 100%;' id='" + panelId + "' src='" + imageUrl + "' />" +
                "</div>";

        return html;

    }

    @RequiresPermissions("task-email:del")
    @PostMapping("/delete/{taskId}")
    public void delete(@PathVariable Long taskId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        try {
            XpackEmailTaskRequest request = emailXpackService.taskForm(taskId);
            GlobalTaskEntity globalTaskEntity = BeanUtils.copyBean(new GlobalTaskEntity(), request);
            scheduleService.deleteSchedule(globalTaskEntity);
            emailXpackService.delete(taskId);
        } catch (Exception e) {
            LogUtil.error(e);
            DEException.throwException(e);
        }
    }

    @RequiresPermissions("task-email:del")
    @PostMapping("/batchDel")
    public void delete(@RequestBody List<Long> taskIds) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        try {
            taskIds.forEach(taskId -> {
                XpackEmailTaskRequest request = emailXpackService.taskForm(taskId);
                GlobalTaskEntity globalTaskEntity = BeanUtils.copyBean(new GlobalTaskEntity(), request);
                scheduleService.deleteSchedule(globalTaskEntity);
            });
            emailXpackService.batchDel(taskIds);
        } catch (Exception e) {
            LogUtil.error(e);
            DEException.throwException(e);
        }
    }

    @PostMapping("/stop/{taskId}")
    public void stop(@PathVariable Long taskId) throws Exception {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        emailXpackService.stop(taskId);
    }

    @PostMapping("/queryInstancies/{goPage}/{pageSize}")
    public Pager<List<XpackTaskInstanceDTO>> instancesGrid(@PathVariable int goPage, @PathVariable int pageSize,
            @RequestBody XpackGridRequest request) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<XpackTaskInstanceDTO> instances = emailXpackService.taskInstanceGrid(request);
        Pager<List<XpackTaskInstanceDTO>> listPager = PageUtils.setPageInfo(page, instances);
        return listPager;
    }

    @PostMapping("/execInfo/{instanceId}")
    public String execInfo(@PathVariable Long instanceId) {
        EmailXpackService emailXpackService = SpringContextUtil.getBean(EmailXpackService.class);
        GlobalTaskInstance instanceForm = emailXpackService.instanceForm(instanceId);
        return instanceForm.getInfo();
    }

    @RequiresPermissions("task-email:read")
    @PostMapping("/export")
    public void export(@RequestBody XpackGridRequest request) throws Exception{
        Pager<List<XpackTaskInstanceDTO>> listPager = instancesGrid(0, 0, request);
        List<XpackTaskInstanceDTO> instanceDTOS = listPager.getListObject();
        ExcelSheetModel excelSheetModel = excelSheetModel(instanceDTOS);
        List<ExcelSheetModel> sheetModels = new ArrayList<>();
        sheetModels.add(excelSheetModel);
        File file = ExcelUtils.exportExcel(sheetModels, null);
        InputStream inputStream = new FileInputStream(file);
        HttpServletResponse response = ServletUtils.response();
        try {
            String filename = file.getName();
            response.reset();
            response.addHeader("Access-Control-Allow-Origin", "*");
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.addHeader("Content-Disposition", "attachment; filename=" + URLEncoder.encode(filename, "UTF-8"));
            ServletOutputStream outputStream = response.getOutputStream();
            byte[] buff = new byte[1024];
            BufferedInputStream bis = null;
            // 读取文件
            bis = new BufferedInputStream(inputStream);
            int i = bis.read(buff);
            while (i != -1) {
                outputStream.write(buff, 0, buff.length);
                outputStream.flush();
                i = bis.read(buff);
            }
            outputStream.close();
            inputStream.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }finally {
            if (file.exists())
                FileUtil.del(file);
        }

    }

    private ExcelSheetModel excelSheetModel(List<XpackTaskInstanceDTO> instanceDTOS) {
        ExcelSheetModel excelSheetModel = new ExcelSheetModel();
        excelSheetModel.setSheetName(Translator.get("I18N_XPACKTASK_FILE_NAME"));
        String[] headArr = new String[] {Translator.get("I18N_XPACKTASK_NAME"), Translator.get("I18N_XPACKTASK_EXEC_TIME"), Translator.get("I18N_XPACKTASK_STATUS")};
        List<String> head = Arrays.asList(headArr);
        excelSheetModel.setHeads(head);
        List<List<String>> datas = instanceDTOS.stream().map(this::formatExcelData).collect(Collectors.toList());
        excelSheetModel.setDatas(datas);
        return excelSheetModel;
    }

    private List<String> formatExcelData(XpackTaskInstanceDTO instanceDTO) {
        List<String> results = new ArrayList<>();
        results.add(instanceDTO.getTaskName());
        try {
            results.add(DateUtils.getTimeString(instanceDTO.getExecuteTime()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Integer status = instanceDTO.getStatus();
        String i18key = "I18N_XPACKTASK_" + (ObjectUtils.isEmpty(status) || status == -1 ? "ERROR" : status == 0 ? "UNDERWAY" : "SUCCESS");
        results.add(Translator.get(i18key));
        return results;
    }

    private XpackPixelEntity buildPixel(String pixel) {

        if (StringUtils.isBlank(pixel))
            return null;
        String[] arr = pixel.split("\\*");
        if (arr.length != 2)
            return null;
        try {
            XpackPixelEntity result = new XpackPixelEntity();
            int x = Integer.parseInt(arr[0].trim());
            int y = Integer.parseInt(arr[1].trim());
            result.setX(String.valueOf(x));
            result.setY(String.valueOf(y));
            return result;
        } catch (Exception e) {
            return null;
        }
    }

}
