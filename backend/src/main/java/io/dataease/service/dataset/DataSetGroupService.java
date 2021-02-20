package io.dataease.service.dataset;

import com.alibaba.nacos.common.util.UuidUtils;
import io.dataease.base.domain.DatasetGroup;
import io.dataease.base.mapper.DatasetGroupMapper;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.dto.dataset.DataSetGroupDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author gin
 * @Date 2021/2/20 8:10 下午
 */
@Service
public class DataSetGroupService {
    @Resource
    private DatasetGroupMapper datasetGroupMapper;

    public DataSetGroupDTO save(DatasetGroup datasetGroup) {
        if (StringUtils.isEmpty(datasetGroup.getId())) {
            datasetGroup.setId(UuidUtils.generateUuid());
            datasetGroup.setCreateTime(System.currentTimeMillis());
            datasetGroupMapper.insert(datasetGroup);
        } else {
            datasetGroupMapper.updateByPrimaryKey(datasetGroup);
        }
        DataSetGroupDTO dataSetGroupDTO = new DataSetGroupDTO();
        BeanUtils.copyBean(dataSetGroupDTO, datasetGroup);
        dataSetGroupDTO.setLabel(dataSetGroupDTO.getName());
        return dataSetGroupDTO;
    }
}
