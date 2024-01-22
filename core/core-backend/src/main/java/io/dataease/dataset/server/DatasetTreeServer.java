package io.dataease.dataset.server;

import io.dataease.api.dataset.DatasetTreeApi;
import io.dataease.api.dataset.dto.DatasetNodeDTO;
import io.dataease.api.dataset.dto.DatasetTableDTO;
import io.dataease.api.dataset.dto.SqlVariableDetails;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.vo.DataSetBarVO;
import io.dataease.constant.LogOT;
import io.dataease.constant.LogST;
import io.dataease.dataset.manage.DatasetGroupManage;
import io.dataease.log.DeLog;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("datasetTree")
public class DatasetTreeServer implements DatasetTreeApi {
    @Resource
    private DatasetGroupManage datasetGroupManage;


    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetGroupInfoDTO save(DatasetGroupInfoDTO datasetNodeDTO) throws Exception {
        return datasetGroupManage.save(datasetNodeDTO, false);
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO rename(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.save(dto, true);
    }

    @DeLog(id = "#p0.id", pid = "#p0.pid", ot = LogOT.CREATE, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO create(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.save(dto, false);
    }

    @DeLog(id = "#p0.id", ot = LogOT.MODIFY, st = LogST.DATASET)
    @Override
    public DatasetNodeDTO move(DatasetGroupInfoDTO dto) throws Exception {
        return datasetGroupManage.move(dto);
    }

    @DeLog(id = "#p0", ot = LogOT.DELETE, st = LogST.DATASET)
    @Override
    public void delete(Long id) {
        datasetGroupManage.delete(id);
    }


    public List<BusiNodeVO> tree(BusiNodeRequest request) {
        return datasetGroupManage.tree(request);
    }

    @Override
    public DataSetBarVO barInfo(Long id) {
        return datasetGroupManage.queryBarInfo(id);
    }

    @Override
    public DatasetGroupInfoDTO get(Long id) throws Exception {
        return datasetGroupManage.get(id, "preview");
    }

    @Override
    public DatasetGroupInfoDTO details(Long id) throws Exception {
        return datasetGroupManage.get(id, null);
    }

    @Override
    public List<DatasetTableDTO> panelGetDsDetails(List<Long> ids) throws Exception {
        return datasetGroupManage.getDetail(ids);
    }

    @Override
    public List<SqlVariableDetails> getSqlParams(List<Long> ids) throws Exception {
        return datasetGroupManage.getSqlParams(ids);
    }

    @Override
    public List<DatasetTableDTO> detailWithPerm(List<Long> ids) throws Exception {
        return datasetGroupManage.getDetailWithPerm(ids);
    }

}
