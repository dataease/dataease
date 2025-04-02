package io.dataease.api.xpack.dataFilling;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.report.dto.ReportInstanceMsgRequest;
import io.dataease.api.report.vo.ReportGridVO;
import io.dataease.api.xpack.dataFilling.dto.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasetTableDTO;
import io.dataease.extensions.datasource.dto.SimpleDatasourceDTO;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATA_FILLING;

@Tag(name = "数据填报")
@DeApiPath(value = "/data-filling", rt = DATA_FILLING)
public interface DataFillingApi {

    @Operation(summary = "查询列表")
    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request) throws DEException;

    @Operation(summary = "根据ID查询数据填报")
    @GetMapping("/get/{id}")
    DataFillingDTO get(@PathVariable("id") Long id);

    @Operation(summary = "移动数据填报至文件夹")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/move")
    DataFillingDTO move(@RequestBody DataFillingDTO dataFillingDTO);

    @Operation(summary = "保存数据填报")
    @PostMapping("/save")
    DataFillingDTO save(@RequestBody DataFillingDTO dataFillingDTO) throws Exception;

    @Operation(summary = "编辑数据填报")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/update")
    DataFillingDTO update(@RequestBody DataFillingDTO dataFillingDTO) throws Exception;

    @Operation(summary = "重命名数据填报")
    @DePermit({"#p0.id+':manage'"})
    @PostMapping("/rename")
    DataFillingDTO rename(@RequestBody DataFillingDTO dataFillingDTO);

    @Operation(summary = "删除数据填报")
    @DePermit({"#p0+':manage'"})
    @GetMapping("delete/{id}")
    void delete(@PathVariable("id") Long id);

    @Operation(summary = "获取创建数据填报表单用的数据源列表")
    @GetMapping("/datasource/list")
    List<SimpleDatasourceDTO> listDatasourceList();

    @Operation(summary = "获取数据源列表")
    @GetMapping("/datasource/listAll")
    List<SimpleDatasourceDTO> listDatasourceListAll();

    @Operation(summary = "获取选项值列表")
    @PostMapping("/form/{optionDatasource}/options")
    List<ColumnOption> listColumnData(@PathVariable("optionDatasource") Long optionDatasource, @RequestBody DatasourceOptionsRequest request) throws Exception;

    @Operation(summary = "获取额外信息")
    @PostMapping("/form/extraDetails")
    List<ExtraDetails> extraDetails(@RequestBody ExtraDetailsRequest request) throws Exception;

    @Operation(summary = "获取数据填报表内数据列表")
    @PostMapping("/form/{id}/tableData")
    DataFillFormTableDataResponse tableData(@PathVariable("id") Long id, @RequestBody DataFillFormTableDataRequest request) throws Exception;

    @Operation(summary = "删除数据填报表内数据")
    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/delete/{id}")
    void deleteRowData(@PathVariable("formId") Long formId, @PathVariable("id") String id) throws Exception;

    @Operation(summary = "晴空数据填报表内数据")
    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/truncate")
    void truncateRowData(@PathVariable("formId") Long formId) throws Exception;

    @Operation(summary = "批量删除数据填报表内数据")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/batch-delete")
    void batchDeleteRowData(@PathVariable("formId") Long formId, @RequestBody List<String> ids) throws Exception;

    @Operation(summary = "保存数据填报内数据")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/rowData/save")
    DataFillFormTableDataResponse saveRowData(@PathVariable("formId") Long formId, @RequestBody Map<String, Object> data) throws Exception;

    @Operation(summary = "查看数据填报任务信息")
    @GetMapping("/task/info/{taskId}")
    TaskInfoVO info(@PathVariable("taskId") Long taskId);

    @Operation(summary = "保存数据填报任务")
    @DePermit({"#p0.formId+':manage'"})
    @PostMapping("/task/save")
    Long saveTask(@RequestBody TaskInfoVO task);

    @Operation(summary = "立即执行数据填报任务")
    @DePermit({"#p0.formId+':manage'"})
    @PostMapping("/task/executeNow")
    void executeNow(@RequestBody TaskInfoVO task);

    @Operation(summary = "查询数据填报任务日志")
    @PostMapping("/task/logMsg")
    String logMsg(@RequestBody ReportInstanceMsgRequest request);

    @Operation(summary = "查询数据填报任务列表")
    @PostMapping("/form/{formId}/task/page/{goPage}/{pageSize}")
    IPage<ReportGridVO> taskPager(@PathVariable("formId") Long formId, @PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfTaskInfoRequest request);

    @Operation(summary = "查询数据填报子任务列表")
    @PostMapping("/sub-task/page/{goPage}/{pageSize}")
    IPage<DfSubTaskVo> subTaskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfSubTaskInfoRequest request);

    @Operation(summary = "删除数据填报任务")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/task/delete")
    void batchDeleteTask(@PathVariable("formId") Long formId, @RequestBody List<Long> ids) throws Exception;

    @Operation(summary = "停止数据填报任务")
    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/task/{id}/stop")
    void stopTask(@PathVariable("formId") Long formId, @PathVariable("id") Long id) throws Exception;

    @Operation(summary = "开始数据填报任务")
    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/task/{id}/start")
    void startTask(@PathVariable("formId") Long formId, @PathVariable("id") Long id) throws Exception;

    @Operation(summary = "删除数据填报子任务")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/sub-task/delete")
    void batchDeleteSubTask(@PathVariable("formId") Long formId, @RequestBody List<Long> ids) throws Exception;

    @Operation(summary = "列出用户的数据填报子任务")
    @GetMapping("/sub-task/{id}/users/list/{type}")
    List<Map<String, Object>> listSubTaskUser(@PathVariable("id") Long id, @PathVariable("type") String type) throws Exception;

    @Operation(summary = "查询用户待任务列表")
    @PostMapping("/user-task/page/{goPage}/{pageSize}")
    IPage<DfUserTaskVo> listUserTask(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfUserTaskRequest request) throws Exception;

    @Operation(summary = "查询用户待填报任务条数")
    @PostMapping("/user-task/todo/count")
    long countUserTodoList() throws Exception;

    @Operation(summary = "根据ID查询用户填报任务列表")
    @GetMapping("/user-task/list/{id}")
    DfUserTaskData listUserTaskData(@PathVariable("id") Long id) throws Exception;

    @Operation(summary = "用户填报数据")
    @PostMapping("/user-task/saveData/{id}")
    DataFillFormTableDataResponse saveFormRowData(@PathVariable("id") Long id, @RequestBody Map<String, Object> data) throws Exception;

    @Operation(summary = "数据填报操作日志")
    @DePermit({"#p0.formId+':read'"})
    @PostMapping("/log/page/{goPage}/{pageSize}")
    IPage<DfCommitLog> logPager(@RequestBody DfCommitLogRequest request, @PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize);

    @Operation(summary = "清理数据填报操作日志")
    @DePermit({"#p0.formId+':manage'"})
    @PostMapping("/log/clear")
    void clearLog(@RequestBody DfClearCommitLogRequest request) throws Exception;

    @Operation(summary = "上传Excel")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/uploadFile")
    DfExcelData excelUpload(@PathVariable("formId") Long formId, @RequestParam("file") MultipartFile file) throws Exception;

    @Operation(summary = "下载Excel模板")
    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/excelTemplate")
    void excelTemplate(@PathVariable("formId") Long formId);

    @Operation(summary = "确认上传Excel")
    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/confirmUpload")
    void confirmUpload(@PathVariable("formId") Long formId, @RequestBody Map<String, String> data);

    @Operation(summary = "获取表单模版配置")
    @GetMapping("/template/{itemId}")
    String getTemplateByUserTaskItemId(@PathVariable("itemId") Long itemId);

    void writeExcel(String file, DataFillFormTableDataRequest request, Long userId, Long org) throws Exception;

    void geFullName(Long pid, List<String> fullName);

    @Operation(summary = "下载数据填报表数据")
    @PostMapping("/innerExport/{isDataEaseBi}/{formId}")
    void innerExport(@PathVariable("formId") Long formId, @PathVariable("isDataEaseBi") boolean isDataEaseBi, HttpServletResponse response) throws Exception;

    @PostMapping("getBuiltInTables")
    @Operation(summary = "获取内置数据源表")
    List<DatasetTableDTO> getBuiltInTables() throws DEException;
}
