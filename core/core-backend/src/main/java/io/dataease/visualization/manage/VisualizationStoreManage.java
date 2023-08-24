package io.dataease.visualization.manage;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.dataease.api.visualization.request.VisualizationStoreRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationStoreVO;
import io.dataease.constant.BusiResourceEnum;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.CommonBeanFactory;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.CoreStore;
import io.dataease.visualization.dao.auto.mapper.CoreStoreMapper;
import io.dataease.visualization.dao.ext.mapper.CoreStoreExtMapper;
import io.dataease.visualization.dao.ext.po.StorePO;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class VisualizationStoreManage {

    @Resource
    private CoreStoreMapper coreStoreMapper;

    @Resource
    private CoreStoreExtMapper coreStoreExtMapper;

    public void execute(VisualizationStoreRequest request) {
        Long resourceId = request.getId();
        Long uid = AuthUtils.getUser().getUserId();
        if (favorited(resourceId)) {
            QueryWrapper<CoreStore> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("resource_id", resourceId);
            queryWrapper.eq("uid", uid);
            coreStoreMapper.delete(queryWrapper);
            return;
        }
        String type = request.getType();
        BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(type.toUpperCase());
        if (ObjectUtils.isEmpty(busiResourceEnum)) {
            DEException.throwException("type is invalid");
        }
        CoreStore coreStore = new CoreStore();
        coreStore.setId(IDUtils.snowID());
        coreStore.setTime(System.currentTimeMillis());
        coreStore.setUid(uid);
        coreStore.setResourceId(resourceId);
        coreStore.setResourceType(busiResourceEnum.getFlag());
        coreStoreMapper.insert(coreStore);
    }

    public Boolean favorited(Long resourceId) {
        QueryWrapper<CoreStore> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("resource_id", resourceId);
        queryWrapper.eq("uid", AuthUtils.getUser().getUserId());
        return coreStoreMapper.exists(queryWrapper);
    }

    public List<VisualizationStoreVO> query(VisualizationWorkbranchQueryRequest request) {
        int pageNum = 1;
        int pageSize = 20;
        IPage<StorePO> storePOIPage = proxy().queryStorePage(pageNum, pageSize, request);
        if (ObjectUtils.isEmpty(storePOIPage)) return new ArrayList<>();
        return proxy().formatResult(storePOIPage.getRecords());
    }

    public VisualizationStoreManage proxy() {
        return CommonBeanFactory.getBean(this.getClass());
    }

    public List<VisualizationStoreVO> formatResult(List<StorePO> pos) {
        if (CollectionUtil.isEmpty(pos)) return new ArrayList<>();
        return pos.stream().map(po ->
                new VisualizationStoreVO(
                        po.getStoreId(), po.getResourceId(), po.getName(),
                        po.getType(), "admin", "admin",
                        po.getEditTime(), 9)).toList();
    }

    public IPage<StorePO> queryStorePage(int goPage, int pageSize, VisualizationWorkbranchQueryRequest request) {
        Long uid = AuthUtils.getUser().getUserId();
        QueryWrapper<Object> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("s.uid", uid);
        if (StringUtils.isNotBlank(request.getType())) {
            BusiResourceEnum busiResourceEnum = BusiResourceEnum.valueOf(request.getType().toUpperCase());
            if (ObjectUtils.isEmpty(busiResourceEnum)) {
                DEException.throwException("type is invalid");
            }
            queryWrapper.eq("s.resource_type", busiResourceEnum.getFlag());
        }
        if (StringUtils.isNotBlank(request.getKeyword())) {
            queryWrapper.like("v.name", request.getKeyword());
        }
        queryWrapper.orderBy(true, request.isAsc(), "v.update_time");
        Page<StorePO> page = new Page<>(goPage, pageSize);
        return coreStoreExtMapper.query(page, queryWrapper);
    }
}
