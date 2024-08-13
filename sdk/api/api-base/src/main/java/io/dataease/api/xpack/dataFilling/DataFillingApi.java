package io.dataease.api.xpack.dataFilling;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.api.report.dto.ReportInstanceMsgRequest;
import io.dataease.api.report.vo.ReportGridVO;
import io.dataease.api.xpack.dataFilling.dto.*;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.SimpleDatasourceDTO;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATA_FILLING;

@Tag(name = "数据填报")
@ApiSupport(order = 1000, author = "fit2cloud-someone")
@DeApiPath(value = "/data-filling", rt = DATA_FILLING)
public interface DataFillingApi {

    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request) throws DEException;

    @GetMapping("/get/{id}")
    DataFillingDTO get(@PathVariable("id") Long id);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/move")
    DataFillingDTO move(@RequestBody DataFillingDTO dataFillingDTO);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/save")
    DataFillingDTO save(@RequestBody DataFillingDTO dataFillingDTO) throws Exception;

    @DePermit({"#p0+':manage'"})
    @PostMapping("/rename")
    DataFillingDTO rename(@RequestBody DataFillingDTO dataFillingDTO);

    @DePermit({"#p0+':manage'"})
    @GetMapping("delete/{id}")
    void delete(@PathVariable("id") Long id);

    @DePermit({"#p0+':manage'"})
    @GetMapping("/datasource/list")
    List<SimpleDatasourceDTO> listDatasourceList();

    @PostMapping("/form/{optionDatasource}/options")
    List<ColumnOption> listColumnData(@PathVariable("optionDatasource") Long optionDatasource, @RequestBody DatasourceOptionsRequest request) throws Exception;

    @PostMapping("/form/{id}/tableData")
    DataFillFormTableDataResponse tableData(@PathVariable("id") Long id, @RequestBody DataFillFormTableDataRequest request) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/delete/{id}")
    void deleteRowData(@PathVariable("formId") Long formId, @PathVariable("id") Long id) throws Exception;

    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/batch-delete")
    void batchDeleteRowData(@PathVariable("formId") Long formId, @RequestBody List<Long> ids) throws Exception;

    @PostMapping("/form/{formId}/rowData/save")
    DataFillFormTableDataResponse saveRowData(@PathVariable("formId") Long formId, @RequestBody Map<String, Object> data) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/task/info/{taskId}")
    TaskInfoVO info(@PathVariable("taskId") Long taskId);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/task/save")
    Long save(@RequestBody TaskInfoVO task);

    @DePermit({"#p0+':read'"})
    @PostMapping("/task/logMsg")
    String logMsg(@RequestBody ReportInstanceMsgRequest request);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/task/page/{goPage}/{pageSize}")
    IPage<ReportGridVO> taskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfTaskInfoRequest request);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/sub-task/page/{goPage}/{pageSize}")
    IPage<DfSubTaskVo> subTaskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfSubTaskInfoRequest request);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/task/delete")
    void batchDeleteTask(@RequestBody List<Long> ids) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/task/{id}/stop")
    void stopTask(@PathVariable("id") Long id) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/task/{id}/start")
    void startTask(@PathVariable("id") Long id) throws Exception;

    @DePermit({"#p0+':manage'"})
    @PostMapping("/sub-task/delete")
    void batchDeleteSubTask(@RequestBody List<Long> ids) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/sub-task/{id}/users/list/{type}")
    List<Map<String, Object>> listSubTaskUser(@PathVariable("id") Long id, @PathVariable("type") String type) throws Exception;

    @PostMapping("/user-task")
    List<DfUserTaskVo> listUserTask(@RequestBody DfUserTaskRequest request) throws Exception;

    @GetMapping("/user-task/list/{id}")
    DfUserTaskData listUserTaskData(@PathVariable("id") Long id) throws Exception;

    @PostMapping("/user-task/saveData/{id}")
    DataFillFormTableDataResponse saveFormRowData(@PathVariable("id") Long id, @RequestBody Map<String, Object> data) throws Exception;

    @DePermit({"#p0+':read'"})
    @PostMapping("/log/page/{goPage}/{pageSize}")
    IPage<DfCommitLog> logPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfCommitLogRequest request);


    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/uploadFile")
    DfExcelData excelUpload(@RequestParam("file") MultipartFile file, @PathVariable("formId") Long formId) throws Exception;

    @DePermit({"#p0+':manage'"})
    @GetMapping("/form/{formId}/excelTemplate")
    void excelTemplate(@PathVariable("formId") Long formId);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/form/{formId}/confirmUpload")
    void confirmUpload(@PathVariable("formId") Long formId, @RequestBody Map<String, String> data);
}
