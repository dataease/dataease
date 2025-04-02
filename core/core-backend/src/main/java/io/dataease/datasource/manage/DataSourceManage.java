package io.dataease.datasource.manage;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.dataease.commons.constants.OptConstants;
import io.dataease.commons.constants.TaskStatus;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import io.dataease.datasource.dao.auto.mapper.CoreDatasourceMapper;
import io.dataease.datasource.dao.ext.mapper.CoreDatasourceExtMapper;
import io.dataease.datasource.dao.ext.mapper.DataSourceExtMapper;
import io.dataease.datasource.dao.ext.po.DataSourceNodePO;
import io.dataease.datasource.dao.ext.po.DsItem;
import io.dataease.datasource.dto.DatasourceNodeBO;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.api.PluginManageApi;
import io.dataease.extensions.datasource.dto.DatasourceDTO;
import io.dataease.extensions.datasource.vo.DatasourceConfiguration;
import io.dataease.extensions.datasource.vo.XpackPluginsDatasourceVO;
import io.dataease.i18n.Translator;
import io.dataease.license.config.XpackInteract;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.model.BusiNodeRequest;
import io.dataease.model.BusiNodeVO;
import io.dataease.operation.manage.CoreOptRecentManage;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.CommunityUtils;
import io.dataease.utils.TreeUtils;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@Component
public class DataSourceManage {

    @Resource
    private DataSourceExtMapper dataSourceExtMapper;

    @Resource
    private CoreDatasourceMapper coreDatasourceMapper;

    @Resource
    private CoreOptRecentManage coreOptRecentManage;

    @Resource
    private CoreDatasourceExtMapper coreDatasourceExtMapper;

    @Resource
    private EngineManage engineManage;

    @Autowired(required = false)
    private PluginManageApi pluginManage;

    private DatasourceNodeBO rootNode() {
        return new DatasourceNodeBO(0L, "root", false, 7, -1L, 0, "mysql");
    }

    private Integer getFlag(String type) {
        Integer flag = null;
        for (DatasourceConfiguration.DatasourceType datasourceType : DatasourceConfiguration.DatasourceType.values()) {
            if (datasourceType.getType().equals(type)) {
                flag = datasourceType.getFlag();
            }
        }
        if (ObjectUtils.isEmpty(flag)) {
            List<XpackPluginsDatasourceVO> xpackPluginsDatasourceVOS = pluginManage.queryPluginDs();
            List<XpackPluginsDatasourceVO> list = xpackPluginsDatasourceVOS.stream().filter(ele -> StringUtils.equals(ele.getType(), type)).toList();
            if (ObjectUtils.isNotEmpty(list)) {
                XpackPluginsDatasourceVO first = list.getFirst();
                flag = first.getFlag();
            }
        }
        if (ObjectUtils.isEmpty(flag)) {
            flag = 27;
        }
        return flag;
    }

    private DatasourceNodeBO convert(DataSourceNodePO po) {
        Integer flag = getFlag(po.getType());
        int extraFlag = StringUtils.equalsIgnoreCase("error", po.getStatus()) ? Math.negateExact(flag) : flag;
        return new DatasourceNodeBO(po.getId(), po.getName(), !StringUtils.equals(po.getType(), "folder"), 9, po.getPid(), extraFlag, po.getType());
    }

    @XpackInteract(value = "datasourceResourceTree", replace = true, invalid = true)
    public List<BusiNodeVO> tree(BusiNodeRequest request) {

        QueryWrapper<DataSourceNodePO> queryWrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(request.getLeaf()) && !request.getLeaf()) {
            queryWrapper.eq("type", "folder");
        }
        String info = CommunityUtils.getInfo();
        if (StringUtils.isNotBlank(info)) {
            queryWrapper.notExists(String.format(info, "core_datasource.id"));
        }
        queryWrapper.orderByDesc("create_time");
        List<DatasourceNodeBO> nodes = new ArrayList<>();
        List<DataSourceNodePO> pos = dataSourceExtMapper.selectList(queryWrapper);
        if (ObjectUtils.isEmpty(request.getLeaf()) || !request.getLeaf()) nodes.add(rootNode());
        if (CollectionUtils.isNotEmpty(pos)) {
            nodes.addAll(pos.stream().map(this::convert).toList());
        }
        return TreeUtils.mergeTree(nodes, BusiNodeVO.class, false);
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerSave(DatasourceDTO dataSourceDTO) {
        CoreDatasource coreDatasource = new CoreDatasource();
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        BeanUtils.copyBean(coreDatasource, dataSourceDTO);
        checkName(dataSourceDTO);
        coreDatasourceMapper.insert(coreDatasource);
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.NEW);
    }

    public void checkName(DatasourceDTO dto) {
        if (StringUtils.isEmpty(dto.getName()) || StringUtils.isEmpty(dto.getName().trim())) {
            DEException.throwException(Translator.get("i18n_df_name_can_not_empty"));
        }
        QueryWrapper<CoreDatasource> wrapper = new QueryWrapper<>();
        if (ObjectUtils.isNotEmpty(dto.getPid())) {
            if (LicenseUtil.licenseValid() && dto.getPid().equals(0L)) {
                wrapper.eq("pid", -100L);
            } else {
                wrapper.eq("pid", dto.getPid());
            }
        }
        if (StringUtils.isNotEmpty(dto.getName())) {
            wrapper.eq("name", dto.getName());
        }
        if (ObjectUtils.isNotEmpty(dto.getId())) {
            wrapper.ne("id", dto.getId());
        }
        if (ObjectUtils.isNotEmpty(dto.getNodeType())) {
            if (dto.getNodeType().equalsIgnoreCase("folder")) {
                wrapper.eq("type", dto.getType());
            } else {
                wrapper.ne("type", "folder");
            }

        }
        List<CoreDatasource> list = coreDatasourceMapper.selectList(wrapper);
        if (list.size() > 0) {
            DEException.throwException(Translator.get("i18n_ds_name_exists"));
        }
    }

    @XpackInteract(value = "larkManage", replace = true)
    public String getTenantAccessToken() {
        return null;
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEdit(CoreDatasource coreDatasource) {
        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", coreDatasource.getId());
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        coreDatasource.setUpdateBy(AuthUtils.getUser().getUserId());
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        coreDatasourceMapper.update(coreDatasource, updateWrapper);
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEditName(CoreDatasource coreDatasource) {
        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", coreDatasource.getId());
        coreDatasource.setTaskStatus(TaskStatus.WaitingForExecution.name());
        coreDatasource.setUpdateTime(System.currentTimeMillis());
        coreDatasource.setUpdateBy(AuthUtils.getUser().getUserId());
        coreDatasourceMapper.update(coreDatasource, updateWrapper);
        coreOptRecentManage.saveOpt(coreDatasource.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void innerEditStatus(CoreDatasource coreDatasource) {
        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", coreDatasource.getId());
        updateWrapper.set("status", coreDatasource.getStatus());
        coreDatasourceMapper.update(null, updateWrapper);
    }


    @XpackInteract(value = "datasourceResourceTree", before = false)
    public void move(DatasourceDTO dataSourceDTO) {
        Long id = dataSourceDTO.getId();
        CoreDatasource sourceData = null;
        if (ObjectUtils.isEmpty(id) || ObjectUtils.isEmpty(sourceData = getCoreDatasource(id))) {
            DEException.throwException("resource not exist");
        }
        checkName(dataSourceDTO);

        UpdateWrapper<CoreDatasource> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", id);
        updateWrapper.set("update_time", System.currentTimeMillis());
        updateWrapper.set("pid", dataSourceDTO.getPid());
        updateWrapper.set("name", dataSourceDTO.getName());
        updateWrapper.set("update_by", AuthUtils.getUser().getUserId());
        coreDatasourceMapper.update(null, updateWrapper);

        coreOptRecentManage.saveOpt(sourceData.getId(), OptConstants.OPT_RESOURCE_TYPE.DATASOURCE, OptConstants.OPT_TYPE.UPDATE);
    }


    public void encryptDsConfig() {
        coreDatasourceMapper.selectList(null).forEach(dataSource -> {
            coreDatasourceMapper.updateById(dataSource);
        });
    }

    @XpackInteract(value = "datasourceResourceTree", before = false)
    public CoreDatasource getCoreDatasource(Long id) {
        if (id == -1L) {
            return engineManage.getDeEngine();
        }
        return coreDatasourceMapper.selectById(id);
    }

    public List<Long> getPidList(Long pid) {
        if (ObjectUtils.isEmpty(pid) || pid.equals(0L)) {
            return null;
        }
        List<Long> result = new ArrayList<>();
        Stack<Long> stack = new Stack<>();
        stack.push(pid);
        while (!stack.isEmpty()) {
            Long cid = stack.pop();
            DsItem item = coreDatasourceExtMapper.queryItem(cid);
            if (ObjectUtils.isNotEmpty(item)) {
                result.add(cid);
                Long cpid = null;
                if (ObjectUtils.isNotEmpty(cpid = item.getPid()) && !cpid.equals(0L)) {
                    stack.add(cpid);
                }
            }
        }
        return result;
    }

    public CoreDatasource getDatasource(Long id) {
        return getCoreDatasource(id);
    }

    public DatasourceDTO getDs(Long id) {
        CoreDatasource coreDatasource = getCoreDatasource(id);
        DatasourceDTO dto = new DatasourceDTO();
        BeanUtils.copyBean(dto, coreDatasource);
        return dto;
    }
}
