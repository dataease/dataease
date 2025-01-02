package io.dataease.controller.datafill;

import com.alibaba.excel.EasyExcel;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.gson.Gson;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.PageUtils;
import io.dataease.commons.utils.Pager;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.datafill.*;
import io.dataease.controller.response.datafill.DataFillFormTableDataResponse;
import io.dataease.dto.datafill.DataFillCommitLogDTO;
import io.dataease.dto.datafill.DataFillFormDTO;
import io.dataease.dto.datafill.DataFillTaskDTO;
import io.dataease.dto.datafill.DataFillUserTaskDTO;
import io.dataease.plugins.common.base.domain.DataFillFormWithBLOBs;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.service.datafill.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import springfox.documentation.annotations.ApiIgnore;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@ApiIgnore
@RequestMapping("dataFilling")
@RestController
public class DataFillController {

    @Resource
    private DataFillService dataFillService;
    @Resource
    private DataFillLogService dataFillLogService;
    @Resource
    private DataFillTaskService dataFillTaskService;
    @Resource
    private DataFillDataService dataFillDataService;


    @ApiIgnore
    @PostMapping("/form/save")
    public ResultHolder saveForm(@RequestBody DataFillFormWithBLOBs dataFillForm) throws Exception {
        return dataFillService.saveForm(dataFillForm);
    }

    @ApiIgnore
    @PostMapping("/form/updateName")
    public ResultHolder updateFormName(@RequestBody DataFillFormWithBLOBs dataFillForm) throws Exception {
        return dataFillService.updateForm(dataFillForm, null);
    }

    @ApiIgnore
    @PostMapping("/form/update")
    public ResultHolder updateForm(@RequestBody DataFillFormWithBLOBs dataFillForm) throws Exception {
        return dataFillService.updateForm(dataFillForm);
    }

    @ApiIgnore
    @PostMapping("/form/move")
    public ResultHolder moveForm(@RequestBody DataFillFormWithBLOBs dataFillForm) throws Exception {
        return dataFillService.updateForm(dataFillForm, "move");
    }

    @PostMapping("/manage/form/{id}")
    public DataFillFormDTO getWithPrivileges(@PathVariable String id) throws Exception {
        return dataFillService.getWithPrivileges(id);
    }

    @PostMapping("/form/get/{id}")
    public DataFillFormWithBLOBs get(@PathVariable String id) throws Exception {
        return dataFillService.get(id);
    }

    @ApiIgnore
    @PostMapping("/form/delete/{id}")
    public void deleteForm(@PathVariable String id) throws Exception {
        dataFillService.deleteForm(id);
    }

    @ApiOperation("查询树")
    @PostMapping("/form/tree")
    public List<DataFillFormDTO> tree(@RequestBody DataFillFormRequest request) {
        return dataFillService.tree(request);
    }

    @ApiIgnore
    @PostMapping("/form/{id}/tableData")
    public DataFillFormTableDataResponse tableData(@PathVariable String id, @RequestBody DataFillFormTableDataRequest request) throws Exception {
        request.setId(id);
        return dataFillDataService.listData(request);
    }

    @ApiIgnore
    @PostMapping("/form/fields/{id}")
    public List<ExtTableField> listFields(@PathVariable String id) throws Exception {
        return dataFillService.listFields(id);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/delete/{id}")
    public void deleteRowData(@PathVariable String formId, @PathVariable String id) throws Exception {
        dataFillDataService.deleteRowData(formId, id);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/rowData/save")
    public String newRowData(@PathVariable String formId, @RequestBody Map<String, Object> data) throws Exception {
        return dataFillDataService.updateOrInsertRowData(formId, Collections.singletonList(new RowDataDatum().setData(data))).get(0);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/rowData/save/{id}")
    public String updateRowData(@PathVariable String formId, @PathVariable String id, @RequestBody Map<String, Object> data) throws Exception {
        return dataFillDataService.updateOrInsertRowData(formId, Collections.singletonList(new RowDataDatum().setId(id).setData(data))).get(0);
    }


    @ApiIgnore
    @PostMapping("/form/{formId}/commitLog/{goPage}/{pageSize}")
    public Pager<List<DataFillCommitLogDTO>> commitLogs(@PathVariable String formId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody DataFillCommitLogSearchRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<DataFillCommitLogDTO> logs = dataFillLogService.commitLogs(formId, request);

        return PageUtils.setPageInfo(page, logs);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/task/{goPage}/{pageSize}")
    public Pager<List<DataFillTaskDTO>> tasks(@PathVariable String formId, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody DataFillTaskSearchRequest request) {
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<DataFillTaskDTO> tasks = dataFillTaskService.tasks(formId, request);

        return PageUtils.setPageInfo(page, tasks);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/task/save")
    public void saveTask(@PathVariable String formId, @RequestBody DataFillTaskSearchRequest request) throws Exception {

        dataFillTaskService.saveTask(formId, request);

    }

    @ApiIgnore
    @PostMapping("/form/task/{taskId}/delete")
    public void deleteTask(@PathVariable Long taskId) {

        dataFillTaskService.deleteTask(taskId);

    }

    @ApiIgnore
    @PostMapping("/form/task/{taskId}/enable")
    public void enableTask(@PathVariable Long taskId) throws Exception {

        dataFillTaskService.enableTask(taskId);

    }

    @ApiIgnore
    @PostMapping("/form/task/{taskId}/disable")
    public void disableTask(@PathVariable Long taskId) throws Exception {

        dataFillTaskService.disableTask(taskId);

    }

    @ApiIgnore
    @PostMapping("/myTask/{type}/{goPage}/{pageSize}")
    public Pager<List<DataFillUserTaskDTO>> userTasks(@PathVariable String type, @PathVariable int goPage, @PathVariable int pageSize, @RequestBody DataFillUserTaskSearchRequest request) {
        Long userId = AuthUtils.getUser().getUserId();
        Page<Object> page = PageHelper.startPage(goPage, pageSize, true);
        List<DataFillUserTaskDTO> tasks = dataFillTaskService.userTasks(userId, type, request);

        return PageUtils.setPageInfo(page, tasks);
    }


    @ApiIgnore
    @PostMapping("/myTask/fill/{taskId}")
    public void userFillData(@PathVariable String taskId, @RequestBody List<Map<String, Object>> data) throws Exception {
        dataFillService.fillFormData(taskId, data);
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/excel/template")
    public void getExcelTemplate(@PathVariable String formId, HttpServletResponse response) throws Exception {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码
            String fileName = URLEncoder.encode("template", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 这里需要设置不关闭流
            EasyExcel.write(response.getOutputStream())
                    .head(dataFillService.getExcelHead(formId))
                    .automaticMergeHead(false)
                    .inMemory(true)
                    .registerWriteHandler(dataFillService.getCommentWriteHandler(formId))
                    .autoCloseStream(Boolean.FALSE)
                    .sheet("模板")
                    .doWrite(new ArrayList());
        } catch (Exception e) {
            e.printStackTrace();
            // 重置response
            response.reset();
            response.setContentType("application/json");
            response.setCharacterEncoding("utf-8");
            response.setStatus(500);
            Map<String, Object> map = new HashMap<>();
            map.put("success", false);
            map.put("message", e.getMessage());
            response.getWriter().println(new Gson().toJson(map));
        }
    }

    @ApiIgnore
    @PostMapping("/form/{formId}/excel/upload")
    public void excelUpload(@RequestParam("file") MultipartFile file, @PathVariable String formId) throws Exception {
        String filename = file.getOriginalFilename();
        dataFillDataService.importExcelData(file, formId);
    }

    @ApiIgnore
    @PostMapping("/form/{optionDatasource}/options")
    public List<ExtTableField.Option> listColumnData(@PathVariable String optionDatasource, @RequestBody DatasourceOptionsRequest request) throws Exception {
        return dataFillDataService.listColumnData(optionDatasource, request.getOptionTable(), request.getOptionColumn(), request.getOptionOrder(), request.getQuery(), true);
    }

}
