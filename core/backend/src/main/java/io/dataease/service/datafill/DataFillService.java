package io.dataease.service.datafill;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.DataFillConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.SysAuthConstants;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.*;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.datafill.DataFillFormRequest;
import io.dataease.dto.DatasourceDTO;
import io.dataease.dto.datafill.DataFillFormDTO;
import io.dataease.ext.ExtDataFillFormMapper;
import io.dataease.i18n.Translator;
import io.dataease.plugins.common.base.domain.*;
import io.dataease.plugins.common.base.mapper.DataFillFormMapper;
import io.dataease.plugins.common.base.mapper.DataFillUserTaskMapper;
import io.dataease.plugins.common.base.mapper.SysUserMapper;
import io.dataease.plugins.common.dto.datafill.ExtIndexField;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import io.dataease.plugins.common.dto.datasource.TableField;
import io.dataease.plugins.common.exception.DataEaseException;
import io.dataease.plugins.common.request.datasource.DatasourceRequest;
import io.dataease.plugins.datasource.provider.ExtDDLProvider;
import io.dataease.plugins.datasource.provider.Provider;
import io.dataease.plugins.datasource.provider.ProviderFactory;
import io.dataease.provider.datasource.JdbcProvider;
import io.dataease.service.datasource.DatasourceService;
import io.dataease.service.sys.SysAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(rollbackFor = Exception.class)
public class DataFillService {

    @Resource
    private Environment env;
    @Resource
    private DataFillFormMapper dataFillFormMapper;
    @Resource
    private ExtDataFillFormMapper extDataFillFormMapper;
    @Resource
    private DatasourceService datasource;
    @Resource
    private SysAuthService sysAuthService;
    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private DataFillTaskService dataFillTaskService;
    @Resource
    private DataFillDataService dataFillDataService;
    @Resource
    private DataFillUserTaskMapper dataFillUserTaskMapper;


    private final static Gson gson = new Gson();


    @DeCleaner(value = DePermissionType.DATA_FILL, key = "pid")
    public ResultHolder saveForm(DataFillFormWithBLOBs dataFillForm) throws Exception {

        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        String uuid = UUIDUtil.getUUID().toString();

        dataFillForm.setId(uuid);

        checkName(uuid, dataFillForm.getName(), dataFillForm.getPid(), dataFillForm.getLevel(), dataFillForm.getNodeType(), DataFillConstants.OPT_TYPE_INSERT);

        if (!StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
            }.getType());

            List<ExtIndexField> indexes = new ArrayList<>();
            if (dataFillForm.getCreateIndex()) {
                indexes = gson.fromJson(dataFillForm.getTableIndexes(), new TypeToken<List<ExtIndexField>>() {
                }.getType());
            }

            DatasourceDTO datasourceDTO = datasource.getDataSourceDetails(dataFillForm.getDatasource());

            datasourceDTO.setConfiguration(new String(java.util.Base64.getDecoder().decode(datasourceDTO.getConfiguration())));

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(datasourceDTO);
            datasourceRequest.setQuery("SELECT VERSION()");

            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            String version = jdbcProvider.getData(datasourceRequest).get(0)[0];

            //拼sql
            ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(datasourceDTO.getType());
            String sql = extDDLProvider.createTableSql(dataFillForm.getTableName(), fields);
            //创建表
            datasourceRequest.setQuery(sql);
            jdbcProvider.exec(datasourceRequest);

            if (dataFillForm.getCreateIndex()) {
                try {
                    List<String> sqls = extDDLProvider.createTableIndexSql(dataFillForm.getTableName(), indexes);

                    for (String indexSql : sqls) {
                        datasourceRequest.setQuery(indexSql);
                        jdbcProvider.exec(datasourceRequest);
                    }
                } catch (Exception e) {
                    // 执行到这里说明表已经建成功了，创建index出错，那就需要回滚删除创建的表
                    datasourceRequest.setQuery(extDDLProvider.dropTableSql(dataFillForm.getTableName()));
                    jdbcProvider.exec(datasourceRequest);

                    throw e;
                }

            }
        }

        dataFillForm.setCreateBy(AuthUtils.getUser().getUsername());
        dataFillForm.setUpdateBy(AuthUtils.getUser().getUsername());
        Date current = new Date();
        dataFillForm.setCreateTime(current);
        dataFillForm.setUpdateTime(current);

        dataFillFormMapper.insertSelective(dataFillForm);

        // 清理权限缓存，应该不需要
        //clearPermissionCache();

        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.CREATE, SysLogConstants.SOURCE_TYPE.DATA_FILL_FORM, dataFillForm.getId(), dataFillForm.getPid(), null, null);

        sysAuthService.copyAuth(uuid, SysAuthConstants.AUTH_SOURCE_TYPE_DATA_FILLING);


        return ResultHolder.success(uuid);
    }

    @DeCleaner(value = DePermissionType.DATA_FILL, key = "pid")
    public ResultHolder updateForm(DataFillFormWithBLOBs dataFillForm) {

        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(dataFillForm.getId(), "id cannot be null");

        DataFillFormWithBLOBs form = dataFillFormMapper.selectByPrimaryKey(dataFillForm.getId());
        //todo 改变文件夹位置
        checkName(dataFillForm.getId(), dataFillForm.getName(), form.getPid(), form.getLevel(), form.getNodeType(), DataFillConstants.OPT_TYPE_UPDATE);

        dataFillForm.setUpdateTime(new Date());
        dataFillFormMapper.updateByPrimaryKeySelective(dataFillForm);

        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATA_FILL_FORM, dataFillForm.getId(), dataFillForm.getPid(), null, null);

        return ResultHolder.success(dataFillForm.getId());
    }

    private void checkName(String id, String name, String pid, int level, String nodeType, String optType) {
        DataFillFormExample example = new DataFillFormExample();
        if (DataFillConstants.OPT_TYPE_INSERT.equalsIgnoreCase(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andNodeTypeEqualTo(nodeType);
        } else if (DataFillConstants.OPT_TYPE_UPDATE.equalsIgnoreCase(optType)) {
            example.createCriteria().andPidEqualTo(pid).andNameEqualTo(name).andIdNotEqualTo(id).andNodeTypeEqualTo(nodeType);
        }

        List<DataFillForm> checkResult = dataFillFormMapper.selectByExample(example);
        if (CollectionUtils.isNotEmpty(checkResult)) {
            DataEaseException.throwException(DataFillConstants.DATA_FILL_NODE_TYPE_DATA_FILL.equals(nodeType) ? Translator.get("I18N_DATA_FILL_FORM_EXIST") : Translator.get("I18N_FOlDER_EXIST"));
        }
    }

    public List<DataFillFormDTO> tree(DataFillFormRequest request) {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        request.setUserId(userId);
        List<DataFillFormDTO> list = extDataFillFormMapper.search(request);
        return TreeUtils.mergeTree(list);

    }

    public DataFillFormWithBLOBs get(String id) {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(id, "id cannot be null");
        return dataFillFormMapper.selectByPrimaryKey(id);
    }

    public DataFillFormDTO getWithPrivileges(String id) {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(id, "id cannot be null");
        String userId = String.valueOf(AuthUtils.getUser().getUserId());
        DataFillFormRequest request = new DataFillFormRequest();
        request.setUserId(userId);
        request.setId(id);
        List<DataFillFormDTO> list = extDataFillFormMapper.search(request);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        DataFillFormDTO base = list.stream().filter(dto -> StringUtils.equals(dto.getId(), id)).collect(Collectors.toList()).get(0);
        DataFillFormDTO result = new DataFillFormDTO();

        BeanUtils.copyBean(result, get(id));

        result.setPrivileges(base.getPrivileges());

        SysUserExample userExample = new SysUserExample();
        userExample.createCriteria().andUsernameEqualTo(result.getCreateBy());
        List<SysUser> sysUsers = sysUserMapper.selectByExample(userExample);
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            result.setCreatorName(sysUsers.get(0).getNickName());
        }

        Datasource d = datasource.get(result.getDatasource());
        if (d != null) {
            result.setDatasourceName(d.getName());
        }

        return result;
    }

    public void deleteForm(String id) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(id, "id cannot be null");
        sysAuthService.checkTreeNoManageCount(SysAuthConstants.AUTH_SOURCE_TYPE_DATA_FILLING, id);

        Map<String, String> stringStringMap = extDataFillFormMapper.searchChildrenIds(id, SysAuthConstants.AUTH_SOURCE_TYPE_DATA_FILLING);
        String[] split = stringStringMap.get("ids").split(",");

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(id);

        List<String> ids = new ArrayList<>();
        for (String dsId : split) {
            if (StringUtils.isNotEmpty(dsId)) {
                ids.add(dsId);
            }
        }
        if (CollectionUtils.isNotEmpty(ids)) {
            DataFillFormExample example = new DataFillFormExample();
            example.createCriteria().andIdIn(ids);
            dataFillFormMapper.deleteByExample(example);
        }

        if (dataFillForm != null) {

            DeLogUtils.save(SysLogConstants.OPERATE_TYPE.DELETE, SysLogConstants.SOURCE_TYPE.DATA_FILL_FORM, dataFillForm.getId(), dataFillForm.getPid(), null, null);

            dataFillTaskService.deleteTaskByFormId(id);

        }
    }

    public List<ExtTableField> listFields(String id) throws Exception {
        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(id);

        if (StringUtils.equals(dataFillForm.getNodeType(), "folder")) {
            return null;
        }
        return gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());
    }


    public void fillFormData(String userTaskId, Map<String, Object> data) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        DataFillUserTask task = dataFillUserTaskMapper.selectByPrimaryKey(userTaskId);

        if (task == null) {
            DataEaseException.throwException("任务不存在");
        }

        if (!AuthUtils.getUser().getUserId().equals(task.getUser())) {
            DataEaseException.throwException("当前用户非任务用户");
        }

        if (task.getEndTime() != null) {
            if (task.getEndTime().getTime() < System.currentTimeMillis()) {
                DataEaseException.throwException("已经超过了任务截止时间");
            }
        }

        String formId = task.getFormId();

        String rowId = null;
        if (StringUtils.isNotBlank(task.getValueId())) {
            rowId = task.getValueId();
        } else {
            DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

            Datasource ds = datasource.get(dataFillForm.getDatasource());
            Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
            ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            datasourceRequest.setTable(dataFillForm.getTableName());

            DataFillDataService.setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

            List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);

            for (TableField tableField : tableFields) {
                if (tableField.isPrimaryKey()) {
                    rowId = (String) data.get(tableField.getFieldName());
                    break;
                }
            }
        }

        rowId = dataFillDataService.updateRowData(formId, rowId, data, rowId == null);

        task.setValueId(rowId);
        task.setFinishTime(new Date());

        dataFillUserTaskMapper.updateByPrimaryKeySelective(task);
    }

}
