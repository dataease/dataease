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
import java.util.*;
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

        checkName(uuid, dataFillForm.getName(), dataFillForm.getPid(), dataFillForm.getNodeType(), DataFillConstants.OPT_TYPE_INSERT);

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
    public ResultHolder updateForm(DataFillFormWithBLOBs dataFillForm, String type) {

        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(dataFillForm.getId(), "id cannot be null");

        DataFillFormWithBLOBs form = dataFillFormMapper.selectByPrimaryKey(dataFillForm.getId());

        if (StringUtils.equals("move", type)) {
            //改变文件夹位置
            checkName(dataFillForm.getId(), form.getName(), dataFillForm.getPid(), form.getNodeType(), DataFillConstants.OPT_TYPE_UPDATE);
            dataFillForm.setName(null);
            dataFillForm.setNodeType(null);
        } else {
            checkName(dataFillForm.getId(), dataFillForm.getName(), form.getPid(), form.getNodeType(), DataFillConstants.OPT_TYPE_UPDATE);
        }

        dataFillForm.setUpdateTime(new Date());
        dataFillFormMapper.updateByPrimaryKeySelective(dataFillForm);

        DeLogUtils.save(SysLogConstants.OPERATE_TYPE.MODIFY, SysLogConstants.SOURCE_TYPE.DATA_FILL_FORM, dataFillForm.getId(), dataFillForm.getPid(), null, null);

        return ResultHolder.success(dataFillForm.getId());
    }

    private void checkName(String id, String name, String pid, String nodeType, String optType) {
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
            DataEaseException.throwException(Translator.get("I18N_DATA_FILL_TASK_NOT_EXIST"));
        }

        if (!AuthUtils.getUser().getUserId().equals(task.getUser())) {
            DataEaseException.throwException(Translator.get("I18N_DATA_FILL_USER_NOT_TASK_USER"));
        }

        if (task.getEndTime() != null) {
            if (task.getEndTime().getTime() < System.currentTimeMillis()) {
                DataEaseException.throwException(Translator.get("I18N_DATA_FILL_TASK_EXPIRED"));
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

        rowId = dataFillDataService.updateOrInsertRowData(formId, Collections.singletonList(new RowDataDatum().setId(rowId).setData(data))).get(0);

        task.setValueId(rowId);
        task.setFinishTime(new Date());

        dataFillUserTaskMapper.updateByPrimaryKeySelective(task);
    }

    public CommentWriteHandler getCommentWriteHandler(String formId) {
        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);
        List<ExtTableField> formFields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        List<ExtTableField> fields = new ArrayList<>();
        for (ExtTableField field : formFields) {
            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                ExtTableField start = gson.fromJson(gson.toJson(field), ExtTableField.class);
                start.getSettings().getMapping().setColumnName(start.getSettings().getMapping().getColumnName1());
                fields.add(start);

                ExtTableField end = gson.fromJson(gson.toJson(field), ExtTableField.class);
                end.getSettings().getMapping().setColumnName(end.getSettings().getMapping().getColumnName2());
                fields.add(end);
            } else {
                fields.add(field);
            }
        }
        CommentWriteHandler commentWriteHandler = new CommentWriteHandler();
        commentWriteHandler.setFields(fields);
        return commentWriteHandler;
    }

    public List<List<String>> getExcelHead(String formId) {
        List<List<String>> list = new ArrayList<>();

        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);
        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());
        for (ExtTableField formField : fields) {
            String name = formField.getSettings().getName();

            if (StringUtils.equalsIgnoreCase(formField.getType(), "dateRange")) {
                String name1 = formField.getSettings().getName() + "(开始) ";
                String name2 = formField.getSettings().getName() + "(结束) ";

                List<String> head1 = List.of(name1);
                List<String> head2 = List.of(name2);

                list.add(head1);
                list.add(head2);
            } else {
                list.add(List.of(name));
            }
        }

        return list;

    }

}
