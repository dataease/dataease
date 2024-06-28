package io.dataease.service.datafill;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.dataease.auth.annotation.DeCleaner;
import io.dataease.auth.service.AuthUserService;
import io.dataease.commons.constants.DataFillConstants;
import io.dataease.commons.constants.DePermissionType;
import io.dataease.commons.constants.SysAuthConstants;
import io.dataease.commons.constants.SysLogConstants;
import io.dataease.commons.utils.*;
import io.dataease.controller.ResultHolder;
import io.dataease.controller.request.datafill.DataFillFormRequest;
import io.dataease.controller.request.datafill.DataFillFormTableDataRequest;
import io.dataease.controller.response.datafill.DataFillFormTableDataResponse;
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
import io.dataease.service.sys.SysAuthService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
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

            List<ExtIndexField> indexesToCreate = new ArrayList<>();
            if (dataFillForm.getCreateIndex()) {
                List<ExtIndexField> indexes = gson.fromJson(dataFillForm.getTableIndexes(), new TypeToken<List<ExtIndexField>>() {
                }.getType());

                Map<String, String> indexColumnItems = new HashMap<>();
                fields.forEach(f -> {
                    if (StringUtils.equalsIgnoreCase(f.getType(), "dateRange")) {
                        indexColumnItems.put(f.getId() + "_1", f.getSettings().getMapping().getColumnName1());
                        indexColumnItems.put(f.getId() + "_2", f.getSettings().getMapping().getColumnName2());
                    } else {
                        indexColumnItems.put(f.getId(), f.getSettings().getMapping().getColumnName());
                    }
                });

                indexes.forEach(f -> {
                    ExtIndexField index = gson.fromJson(gson.toJson(f), ExtIndexField.class);
                    index.getColumns().forEach(c -> {
                        //根据id获取实际的column名
                        c.setColumn(indexColumnItems.get(c.getColumn()));
                    });
                    indexesToCreate.add(index);
                });

            }

            Datasource ds = dataFillDataService.getDataSource(dataFillForm.getDatasource(), true);

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            datasourceRequest.setQuery("SELECT VERSION()");

            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            String version = jdbcProvider.getData(datasourceRequest).get(0)[0];

            //拼sql
            ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());
            String sql = extDDLProvider.createTableSql(dataFillForm.getTableName(), fields);
            //创建表
            datasourceRequest.setQuery(sql);
            jdbcProvider.exec(datasourceRequest);

            if (dataFillForm.getCreateIndex()) {
                try {
                    List<String> sqls = extDDLProvider.createTableIndexSql(dataFillForm.getTableName(), indexesToCreate);

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

    @DeCleaner(value = DePermissionType.DATA_FILL, key = "pid")
    public ResultHolder updateForm(DataFillFormWithBLOBs dataFillForm) throws Exception {
        if (!CommonBeanFactory.getBean(AuthUserService.class).pluginLoaded()) {
            DataEaseException.throwException("invalid");
        }

        Assert.notNull(dataFillForm.getId(), "id cannot be null");

        checkName(dataFillForm.getId(), dataFillForm.getName(), dataFillForm.getPid(), dataFillForm.getNodeType(), DataFillConstants.OPT_TYPE_UPDATE);

        DataFillFormWithBLOBs oldForm = dataFillFormMapper.selectByPrimaryKey(dataFillForm.getId());
        List<ExtTableField> oldFields = gson.fromJson(oldForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());
        List<String> oldFieldIds = oldFields.stream().map(ExtTableField::getId).collect(Collectors.toList());
        List<String> oldIndexNames;
        if (oldForm.getCreateIndex()) {
            List<ExtIndexField> oldIndexes = gson.fromJson(oldForm.getTableIndexes(), new TypeToken<List<ExtIndexField>>() {
            }.getType());
            oldIndexNames = oldIndexes.stream().map(ExtIndexField::getName).collect(Collectors.toList());
        } else {
            oldIndexNames = new ArrayList<>();
        }
        List<ExtTableField> fields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());
        List<ExtIndexField> indexes = new ArrayList<>();
        if (dataFillForm.getCreateIndex()) {
            indexes = gson.fromJson(dataFillForm.getTableIndexes(), new TypeToken<List<ExtIndexField>>() {
            }.getType());
        }

        List<ExtTableField> fieldsToCreate = fields.stream().filter(f -> !oldFieldIds.contains(f.getId())).collect(Collectors.toList());
        //这里是表单中被删除的字段
        List<String> fieldsIds = fields.stream().map(ExtTableField::getId).collect(Collectors.toList());
        List<ExtTableField> removedFields = oldFields.stream().filter(f -> !fieldsIds.contains(f.getId()) && !f.isRemoved()).collect(Collectors.toList());
        List<ExtTableField> alreadyRemovedFields = oldFields.stream().filter(ExtTableField::isRemoved).collect(Collectors.toList());
        //需要修改列名的字段
        List<ExtTableField> fieldsToModify = removedFields.stream().filter(f -> !f.isRemoved()).collect(Collectors.toList());
        fieldsToModify.forEach(f -> {
            f.setRemoved(true);
            if (StringUtils.equalsIgnoreCase(f.getType(), "dateRange")) {
                f.getSettings().getMapping().setOldColumnName1(f.getSettings().getMapping().getColumnName1());
                f.getSettings().getMapping().setOldColumnName2(f.getSettings().getMapping().getColumnName2());
                f.getSettings().getMapping().setColumnName1(f.getId() + "_1");
                f.getSettings().getMapping().setColumnName2(f.getId() + "_2");
            } else {
                f.getSettings().getMapping().setOldColumnName(f.getSettings().getMapping().getColumnName());
                f.getSettings().getMapping().setColumnName(f.getId());
            }
        });
        Map<String, String> indexColumnItems = new HashMap<>();
        fields.forEach(f -> {
            if (StringUtils.equalsIgnoreCase(f.getType(), "dateRange")) {
                indexColumnItems.put(f.getId() + "_1", f.getSettings().getMapping().getColumnName1());
                indexColumnItems.put(f.getId() + "_2", f.getSettings().getMapping().getColumnName2());
            } else {
                indexColumnItems.put(f.getId(), f.getSettings().getMapping().getColumnName());
            }
        });
        List<ExtIndexField> indexesToCreate = new ArrayList<>();
        indexes.stream()
                .filter(f -> !oldIndexNames.contains(f.getName())).collect(Collectors.toList())
                .forEach(f -> {
                    ExtIndexField index = gson.fromJson(gson.toJson(f), ExtIndexField.class);
                    index.getColumns().forEach(c -> {
                        //根据id获取实际的column名
                        c.setColumn(indexColumnItems.get(c.getColumn()));
                    });
                    indexesToCreate.add(index);
                });

        if (CollectionUtils.isNotEmpty(fieldsToCreate) || CollectionUtils.isNotEmpty(indexesToCreate) || CollectionUtils.isNotEmpty(fieldsToModify)) {

            Datasource ds = dataFillDataService.getDataSource(dataFillForm.getDatasource());

            DatasourceRequest datasourceRequest = new DatasourceRequest();
            datasourceRequest.setDatasource(ds);
            datasourceRequest.setQuery("SELECT VERSION()");

            JdbcProvider jdbcProvider = CommonBeanFactory.getBean(JdbcProvider.class);
            String version = jdbcProvider.getData(datasourceRequest).get(0)[0];

            //拼sql
            ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

            if (CollectionUtils.isNotEmpty(fieldsToCreate) || CollectionUtils.isNotEmpty(fieldsToModify)) {
                String sql = extDDLProvider.addTableColumnSql(dataFillForm.getTableName(), fieldsToCreate, fieldsToModify);
                //创建
                datasourceRequest.setQuery(sql);
                jdbcProvider.exec(datasourceRequest);
            }

            if (CollectionUtils.isNotEmpty(indexesToCreate)) {
                List<ExtIndexField> successCreatedIndex = new ArrayList<>();
                try {
                    List<String> sqls = extDDLProvider.createTableIndexSql(dataFillForm.getTableName(), indexesToCreate);

                    for (int i = 0; i < sqls.size(); i++) {
                        String indexSql = sqls.get(i);
                        datasourceRequest.setQuery(indexSql);
                        jdbcProvider.exec(datasourceRequest);
                        successCreatedIndex.add(indexesToCreate.get(i));
                    }
                } catch (Exception e) {
                    if (CollectionUtils.isNotEmpty(successCreatedIndex)) {
                        List<String> sqls = extDDLProvider.dropTableIndexSql(dataFillForm.getTableName(), indexesToCreate);
                        for (String sql : sqls) {
                            try {
                                datasourceRequest.setQuery(sql);
                                jdbcProvider.exec(datasourceRequest);
                            } catch (Exception e1) {
                                //e1.printStackTrace();
                            }
                        }
                    }

                    if (CollectionUtils.isNotEmpty(fieldsToCreate)) {
                        // 执行到这里说明表已经建成功了，创建index出错，那就需要回滚删除创建的字段
                        datasourceRequest.setQuery(extDDLProvider.dropTableColumnSql(dataFillForm.getTableName(), fields));
                        jdbcProvider.exec(datasourceRequest);
                    }

                    throw e;
                }

            }

        }

        //处理被删除的form
        if (CollectionUtils.isNotEmpty(fieldsToModify) || CollectionUtils.isNotEmpty(alreadyRemovedFields)) {
            List<ExtTableField> finalFields = new ArrayList<>(fields);
            finalFields.addAll(fieldsToModify);
            finalFields.addAll(alreadyRemovedFields);
            dataFillForm.setForms(new Gson().toJson(finalFields));
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

        Datasource d = dataFillDataService.getDataSource(result.getDatasource());
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


    public void fillFormData(String userTaskId, List<Map<String, Object>> data) throws Exception {
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


        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);

        Datasource ds = dataFillDataService.getDataSource(dataFillForm.getDatasource());
        Provider datasourceProvider = ProviderFactory.getProvider(ds.getType());
        ExtDDLProvider extDDLProvider = ProviderFactory.gerExtDDLProvider(ds.getType());

        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDatasource(ds);
        datasourceRequest.setTable(dataFillForm.getTableName());

        DataFillDataService.setLowerCaseRequest(ds, datasourceProvider, extDDLProvider, datasourceRequest);

        List<TableField> tableFields = datasourceProvider.getTableFields(datasourceRequest);

        String pk = "";
        for (TableField tableField : tableFields) {
            if (tableField.isPrimaryKey()) {
                pk = tableField.getFieldName();
                break;
            }
        }

        List<RowDataDatum> updateDatumList = new ArrayList<>();
        List<RowDataDatum> insertDatumList = new ArrayList<>();

        List<RowDataDatum> valueList = new ArrayList<>();

        for (Map<String, Object> datum : data) {
            String pkv = (String) datum.get(pk);
            if (pkv == null) {
                insertDatumList.add(new RowDataDatum().setData(datum));
            } else {
                updateDatumList.add(new RowDataDatum().setId(pkv).setData(datum));
            }
        }

        //针对没有id的数据，即第一次提交的新数据，需要根据表单提交模式进行判断
        if (CollectionUtils.isNotEmpty(insertDatumList)) {
            //表单提交模式为更新时，需要根据条件加上主键id
            List<ExtTableField> formFields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
            }.getType());

            List<ExtTableField> fields = new ArrayList<>();
            for (ExtTableField field : formFields) {
                if (field.isRemoved()) {
                    continue;
                }
                if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                    ExtTableField start = gson.fromJson(gson.toJson(field), ExtTableField.class);
                    start.getSettings().getMapping().setColumnName(start.getSettings().getMapping().getColumnName1());
                    start.getSettings().setName(start.getSettings().getName() + "（开始）");
                    fields.add(start);

                    ExtTableField end = gson.fromJson(gson.toJson(field), ExtTableField.class);
                    end.getSettings().setName(end.getSettings().getName() + "（结束）");
                    end.getSettings().getMapping().setColumnName(end.getSettings().getMapping().getColumnName2());
                    fields.add(end);
                } else {
                    fields.add(field);
                }
            }

            boolean updateMode = false;
            List<ExtTableField> fieldsForCheck = new ArrayList<>();
            if (BooleanUtils.isTrue(dataFillForm.getCommitNewUpdate())) {
                //这里把所有数据都拿出来遍历真的合适吗？
                for (ExtTableField field : fields) {
                    if (field.getSettings().isUpdateRuleCheck()) {
                        updateMode = true;
                        fieldsForCheck.add(field);
                    }
                }
            }

            if (!updateMode) {
                valueList = new ArrayList<>(insertDatumList);
            } else {
                DataFillFormTableDataRequest req = new DataFillFormTableDataRequest();
                req.setId(dataFillForm.getId());
                DataFillFormTableDataResponse res = dataFillDataService.listData(req, false);
                List<Map<String, Object>> searchData = new ArrayList<>();
                if (res != null && res.getData() != null) {
                    searchData = (List<Map<String, Object>>) res.getData();
                }

                for (RowDataDatum datum : insertDatumList) {
                    boolean hasUpdate = false;
                    for (Map<String, Object> searchDatum : searchData) {
                        if (DataFillDataService.processValueList(valueList, pk, datum, searchDatum, fieldsForCheck)) {
                            hasUpdate = true;
                        }
                    }
                    if (!hasUpdate) {
                        RowDataDatum row = new RowDataDatum()
                                .setInsert(true)
                                .setData(new HashMap<>(datum.getData()));
                        valueList.add(row);
                    }
                }

            }

        }

        valueList.addAll(updateDatumList);

        List<String> rowIds = dataFillDataService.updateOrInsertRowData(formId, valueList);

        task.setValueId(String.join(",", rowIds));
        task.setFinishTime(new Date());

        dataFillUserTaskMapper.updateByPrimaryKeySelective(task);
    }

    public CommentWriteHandler getCommentWriteHandler(String formId) {
        DataFillFormWithBLOBs dataFillForm = dataFillFormMapper.selectByPrimaryKey(formId);
        List<ExtTableField> formFields = gson.fromJson(dataFillForm.getForms(), new TypeToken<List<ExtTableField>>() {
        }.getType());

        List<ExtTableField> fields = new ArrayList<>();
        for (ExtTableField field : formFields) {
            if (field.isRemoved()) {
                continue;
            }
            if (StringUtils.equalsIgnoreCase(field.getType(), "dateRange")) {
                ExtTableField start = gson.fromJson(gson.toJson(field), ExtTableField.class);
                start.getSettings().getMapping().setColumnName(start.getSettings().getMapping().getColumnName1());
                fields.add(start);

                ExtTableField end = gson.fromJson(gson.toJson(field), ExtTableField.class);
                end.getSettings().getMapping().setColumnName(end.getSettings().getMapping().getColumnName2());
                fields.add(end);
            } else if (StringUtils.equalsIgnoreCase(field.getType(), "select") || StringUtils.equalsIgnoreCase(field.getType(), "radio") || StringUtils.equalsIgnoreCase(field.getType(), "checkbox")) {
                if (field.getSettings().getOptionSourceType() == 2
                        && StringUtils.isNotBlank(field.getSettings().getOptionDatasource())
                        && StringUtils.isNotBlank(field.getSettings().getOptionTable())
                        && StringUtils.isNotBlank(field.getSettings().getOptionColumn())
                ) {
                    List<ExtTableField.Option> columnData = new ArrayList<>();
                    try {
                        columnData = dataFillDataService.listColumnData(field.getSettings().getOptionDatasource(), field.getSettings().getOptionTable(), field.getSettings().getOptionColumn(), field.getSettings().getOptionOrder());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    field.getSettings().setOptions(columnData);
                }
                fields.add(field);
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
            if (formField.isRemoved()) {
                continue;
            }
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
