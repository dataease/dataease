package io.dataease.service.sys;

import io.dataease.dto.RelationDTO;
import io.dataease.ext.ExtDataSetTableMapper;
import io.dataease.ext.ExtDataSourceMapper;
import io.dataease.ext.ExtPanelGroupMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author WiSoniC
 * @date 2022年12月8日15:17:17
 */
@Service
public class RelationService {

    @Resource
    private ExtDataSourceMapper extDataSourceMapper;

    @Resource
    private ExtDataSetTableMapper extDataSetTableMapper;

    @Resource
    private ExtPanelGroupMapper extPanelGroupMapper;

    public List<RelationDTO> getRelationForDatasource(String datasourceId, Long userId) {
        return extDataSourceMapper.queryDatasourceRelation(datasourceId, userId);
    }

    public RelationDTO getRelationForDataset(String datasetId, Long userId) {
        return extDataSetTableMapper.queryDatasetRelation(datasetId, userId);
    }

    public List<RelationDTO> getRelationForPanel(String panelId, Long userId) {
        return extPanelGroupMapper.queryPanelRelation(panelId, userId);
    }

}
