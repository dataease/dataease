package io.dataease.datasource.service;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.*;
import io.dataease.commons.exception.DEException;
import io.dataease.datasource.provider.DatasourceProvider;
import io.dataease.datasource.provider.ProviderFactory;
import io.dataease.datasource.request.DatasourceRequest;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(rollbackFor = Exception.class)
public class DatasourceService {

    @Resource
    private DatasourceMapper datasourceMapper;

    public Datasource addDatasource(Datasource datasource) {
        DatasourceExample example = new DatasourceExample();
        example.createCriteria().andNameEqualTo(datasource.getName());
        if (CollectionUtils.isNotEmpty(datasourceMapper.selectByExample(example))) {
            DEException.throwException("Exist data connection with the same name ");
        }
        long currentTimeMillis = System.currentTimeMillis();
        datasource.setId(UUID.randomUUID().toString());
        datasource.setUpdateTime(currentTimeMillis);
        datasource.setCreateTime(currentTimeMillis);
        datasourceMapper.insertSelective(datasource);
        return datasource;
    }

    public List<Datasource> getDatasourceList(Datasource request) throws Exception {
        DatasourceExample example = new DatasourceExample();
        DatasourceExample.Criteria criteria = example.createCriteria();
        if (StringUtils.isNotBlank(request.getName())) {
            criteria.andNameLike(StringUtils.wrapIfMissing(request.getName(), "%"));
        }
        if (StringUtils.isNotBlank(request.getType())) {
            criteria.andTypeEqualTo(request.getType());
        }
        example.setOrderByClause("update_time desc");
        return datasourceMapper.selectByExampleWithBLOBs(example);
    }

    public void deleteDatasource(String datasourceId) {
        datasourceMapper.deleteByPrimaryKey(datasourceId);
    }

    public void updateDatasource(Datasource datasource) {
        datasource.setCreateTime(null);
        datasource.setUpdateTime(System.currentTimeMillis());
        datasourceMapper.updateByPrimaryKeySelective(datasource);
    }

    public void validate(Datasource datasource) throws Exception {
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(datasource.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(datasource);
        datasourceProvider.test(datasourceRequest);
    }

    public List<String> getTables(Datasource datasource) throws Exception {
        Datasource ds = datasourceMapper.selectByPrimaryKey(datasource.getId());
        DatasourceProvider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        return datasourceProvider.getTables(datasourceRequest);
    }

}
