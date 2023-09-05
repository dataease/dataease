package io.dataease.api.dataset;

import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.SqlVariableDetails;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASET;

@DeApiPath(value = "/datasetTree", rt = DATASET)
public interface DatasetTreeApi {

    /**
     * 编辑
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @DePermit({"m:read", "#p0.id+':manage'"})
    @PostMapping("save")
    DatasetNodeDTO save(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @DePermit({"m:read", "#p0.id+':manage'"})
    @PostMapping("rename")
    DatasetNodeDTO rename(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    /**
     * 新建
     *
     * @param dto
     * @return
     * @throws Exception
     */
    @DePermit({"m:read", "#p0.pid+':manage'"})
    @PostMapping("create")
    DatasetNodeDTO create(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @DePermit({"m:read", "#p0.id+':manage'", "#p0.pid+':manage'"})
    @PostMapping("move")
    DatasetNodeDTO move(@RequestBody DatasetGroupInfoDTO dto) throws Exception;

    @DePermit({"m:read", "#p0+':manage'"})
    @PostMapping("delete/{id}")
    void delete(@PathVariable("id") Long id);

    @DePermit({"m:read"})
    @PostMapping("tree")
    List<BusiNodeVO> tree(@RequestBody BusiNodeRequest request);

    @DePermit({"m:read"})
    @GetMapping("/barInfo/{id}")
    DataSetBarVO barInfo(@PathVariable("id") Long id);

    @PostMapping("get/{id}")
    DatasetGroupInfoDTO get(@PathVariable("id") Long id) throws Exception;

    @PostMapping("details/{id}")
    DatasetGroupInfoDTO details(@PathVariable("id") Long id) throws Exception;

    @PostMapping("dsDetails")
    List<DatasetTableDTO> panelGetDsDetails(@RequestBody List<Long> ids) throws Exception;

    @PostMapping("getSqlParams")
    List<SqlVariableDetails> getSqlParams(@RequestBody List<Long> ids) throws Exception;
}
