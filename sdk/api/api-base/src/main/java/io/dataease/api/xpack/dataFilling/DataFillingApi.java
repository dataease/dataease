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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATA_FILLING;

@Tag(name = "数据填报")
@ApiSupport(order = 1000, author = "fit2cloud-someone")
@DeApiPath(value = "/data-filling", rt = DATA_FILLING)
public interface DataFillingApi {

    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request) throws DEException;

    @DePermit({"#p0+':read'"})
    @GetMapping("/get/{id}")
    DataFillingDTO get(@PathVariable("id") Long id);

    @PostMapping("/move")
    DataFillingDTO move(@RequestBody DataFillingDTO dataFillingDTO);

    @PostMapping("/save")
    DataFillingDTO save(@RequestBody DataFillingDTO dataFillingDTO) throws Exception;

    @PostMapping("/rename")
    DataFillingDTO rename(@RequestBody DataFillingDTO dataFillingDTO);

    @GetMapping("delete/{id}")
    void delete(@PathVariable("id") Long id);

    @GetMapping("/datasource/list")
    List<SimpleDatasourceDTO> listDatasourceList();

    @PostMapping("/form/{optionDatasource}/options")
    List<ColumnOption> listColumnData(@PathVariable("optionDatasource") Long optionDatasource, @RequestBody DatasourceOptionsRequest request) throws Exception;

    @PostMapping("/form/{id}/tableData")
    DataFillFormTableDataResponse tableData(@PathVariable("id") Long id, @RequestBody DataFillFormTableDataRequest request) throws Exception;

    @GetMapping("/form/{formId}/delete/{id}")
    void deleteRowData(@PathVariable("formId") Long formId, @PathVariable("id") Long id) throws Exception;

    @PostMapping("/form/{formId}/batch-delete")
    void batchDeleteRowData(@PathVariable("formId") Long formId, @RequestBody List<Long> ids) throws Exception;

    @PostMapping("/form/{formId}/rowData/save")
    DataFillFormTableDataResponse saveRowData(@PathVariable("formId") Long formId, @RequestBody Map<String, Object> data) throws Exception;


    @GetMapping("/task/info/{taskId}")
    TaskInfoVO info(@PathVariable("taskId") Long taskId);


    @PostMapping("/task/save")
    Long save(@RequestBody TaskInfoVO task);

    @PostMapping("/task/logMsg")
    String logMsg(@RequestBody ReportInstanceMsgRequest request);

    @PostMapping("/task/page/{goPage}/{pageSize}")
    IPage<ReportGridVO> taskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfTaskInfoRequest request);

    @PostMapping("/sub-task/page/{goPage}/{pageSize}")
    IPage<DfSubTaskVo> subTaskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfSubTaskInfoRequest request);

    @PostMapping("/task/delete")
    void batchDeleteTask(@RequestBody List<Long> ids) throws Exception;

    @GetMapping("/task/{id}/stop")
    void stopTask(@PathVariable("id") Long id) throws Exception;

    @GetMapping("/task/{id}/start")
    void startTask(@PathVariable("id") Long id) throws Exception;

    @PostMapping("/sub-task/delete")
    void batchDeleteSubTask(@RequestBody List<Long> ids) throws Exception;

    @GetMapping("/sub-task/{id}/users/list/{type}")
    List<Map<String, Object>> listSubTaskUser(@PathVariable("id") Long id, @PathVariable("type") String type) throws Exception;

    @PostMapping("/user-task")
    List<DfUserTaskVo> listUserTask(@RequestBody DfUserTaskRequest request) throws Exception;

    @GetMapping("/user-task/list/{id}")
    DfUserTaskData listUserTaskData(@PathVariable("id") Long id) throws Exception;

    @PostMapping("/user-task/saveData/{id}")
    DataFillFormTableDataResponse saveFormRowData(@PathVariable("id") Long id, @RequestBody Map<String, Object> data) throws Exception;


    @PostMapping("/log/page/{goPage}/{pageSize}")
    IPage<DfCommitLog> taskPager(@PathVariable("goPage") int goPage, @PathVariable("pageSize") int pageSize, @RequestBody DfCommitLogRequest request);


}
