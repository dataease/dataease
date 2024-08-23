package io.dataease.copilot.manage;

import com.fasterxml.jackson.core.type.TypeReference;
import io.dataease.api.copilot.dto.*;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.api.dataset.union.UnionDTO;
import io.dataease.chart.utils.ChartDataBuild;
import io.dataease.copilot.api.CopilotAPI;
import io.dataease.dataset.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupMapper;
import io.dataease.dataset.manage.DatasetDataManage;
import io.dataease.dataset.manage.DatasetSQLManage;
import io.dataease.dataset.manage.DatasetTableFieldManage;
import io.dataease.dataset.manage.PermissionManage;
import io.dataease.dataset.utils.FieldUtils;
import io.dataease.engine.constant.DeTypeConstants;
import io.dataease.engine.utils.Utils;
import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.constant.SqlPlaceholderConstants;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.dto.DatasourceRequest;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.extensions.datasource.dto.TableField;
import io.dataease.extensions.datasource.factory.ProviderFactory;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.ColumnPermissionItem;
import io.dataease.i18n.Translator;
import io.dataease.license.dao.po.LicensePO;
import io.dataease.license.manage.F2CLicManage;
import io.dataease.license.utils.LicenseUtil;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.BeanUtils;
import io.dataease.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
@Component
public class CopilotManage {
    @Resource
    private DatasetSQLManage datasetSQLManage;
    @Resource
    private CoreDatasetGroupMapper coreDatasetGroupMapper;
    @Resource
    private DatasetTableFieldManage datasetTableFieldManage;
    @Resource
    private DatasetDataManage datasetDataManage;
    @Resource
    private PermissionManage permissionManage;
    @Resource
    private MsgManage msgManage;

    private static Logger logger = LoggerFactory.getLogger(CopilotManage.class);

    @Resource
    private TokenManage tokenManage;

    @Resource
    private CopilotAPI copilotAPI;

    @Resource
    private F2CLicManage f2CLicManage;

    private String[] chartType = {"bar", "line", "pie"};

    public MsgDTO chat(MsgDTO msgDTO) throws Exception {
        CoreDatasetGroup coreDatasetGroup = coreDatasetGroupMapper.selectById(msgDTO.getDatasetGroupId());
        if (coreDatasetGroup == null) {
            return null;
        }
        DatasetGroupInfoDTO dto = new DatasetGroupInfoDTO();
        BeanUtils.copyBean(dto, coreDatasetGroup);
        dto.setUnionSql(null);
        List<UnionDTO> unionDTOList = JsonUtil.parseList(coreDatasetGroup.getInfo(), new TypeReference<>() {
        });
        dto.setUnion(unionDTOList);

        // 获取field
        List<DatasetTableFieldDTO> dsFields = datasetTableFieldManage.selectByDatasetGroupId(msgDTO.getDatasetGroupId());
        List<DatasetTableFieldDTO> allFields = dsFields.stream().filter(ele -> ele.getExtField() == 0)
                .map(ele -> {
                    DatasetTableFieldDTO datasetTableFieldDTO = new DatasetTableFieldDTO();
                    BeanUtils.copyBean(datasetTableFieldDTO, ele);
                    datasetTableFieldDTO.setFieldShortName(ele.getDataeaseName());
                    return datasetTableFieldDTO;
                }).collect(Collectors.toList());

        Map<String, Object> sqlMap = datasetSQLManage.getUnionSQLForEdit(dto, null);
        String sql = (String) sqlMap.get("sql");// 数据集原始SQL
        Map<Long, DatasourceSchemaDTO> dsMap = (Map<Long, DatasourceSchemaDTO>) sqlMap.get("dsMap");
        boolean crossDs = Utils.isCrossDs(dsMap);
        if (crossDs) {
            DEException.throwException("跨源数据集不支持该功能");
        }

        // 调用copilot service 获取SQL和chart struct，将返回SQL中表名替换成数据集SQL
        // deSendDTO 构建schema和engine
        if (ObjectUtils.isEmpty(dsMap)) {
            DEException.throwException("No datasource");
        }

        DatasourceSchemaDTO ds = dsMap.entrySet().iterator().next().getValue();
        String type = engine(ds.getType());// 数据库类型，如mysql，oracle等，可能需要映射成copilot需要的类型

        datasetDataManage.buildFieldName(sqlMap, allFields);
        List<String> strings = transCreateTableFields(allFields);
        String createSql = "CREATE TABLE de_tmp_table (" + StringUtils.join(strings, ",") + ")";
        logger.debug("Copilot Schema SQL: " + createSql);

//        PerMsgDTO perMsgDTO = new PerMsgDTO();
        msgDTO.setDatasetGroupId(dto.getId());
        msgDTO.setMsgType("user");
        msgDTO.setEngineType(type);
        msgDTO.setSchemaSql(createSql);
        msgDTO.setHistory(msgDTO.getHistory());
        msgDTO.setMsgStatus(1);
        msgManage.save(msgDTO);// 到这里为止，提问所需参数构建完毕，往数据库插入一条提问记录

        DESendDTO deSendDTO = new DESendDTO();
        deSendDTO.setDatasetGroupId(dto.getId());
        deSendDTO.setQuestion(msgDTO.getQuestion());
        deSendDTO.setHistory(msgDTO.getHistory());
        deSendDTO.setEngine(type);
        deSendDTO.setSchema(createSql);

        // do copilot chat
        ReceiveDTO receiveDTO = copilotChat(deSendDTO);

        // copilot 请求结束，继续de获取数据
        // 获取数据集相关行列权限、最终再套一层SQL
        Map<String, ColumnPermissionItem> desensitizationList = new HashMap<>();
        allFields = permissionManage.filterColumnPermissions(allFields, desensitizationList, dto.getId(), null);
        if (ObjectUtils.isEmpty(allFields)) {
            DEException.throwException(Translator.get("i18n_no_column_permission"));
        }

        List<String> dsList = new ArrayList<>();
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            dsList.add(next.getValue().getType());
        }

        if (!crossDs) {
            sql = Utils.replaceSchemaAlias(sql, dsMap);
        }

        Provider provider;
        if (crossDs) {
            provider = ProviderFactory.getDefaultProvider();
        } else {
            provider = ProviderFactory.getProvider(dsList.getFirst());
        }

//        List<DataSetRowPermissionsTreeDTO> rowPermissionsTree = new ArrayList<>();
//        TokenUserBO user = AuthUtils.getUser();
//        if (user != null) {
//            rowPermissionsTree = permissionManage.getRowPermissionsTree(dto.getId(), user.getUserId());
//        }

        // build query sql
//        SQLMeta sqlMeta = new SQLMeta();
//        Table2SQLObj.table2sqlobj(sqlMeta, null, "(" + sql + ")", crossDs);
//        Field2SQLObj.field2sqlObj(sqlMeta, allFields, allFields, crossDs, dsMap);
//        WhereTree2Str.transFilterTrees(sqlMeta, rowPermissionsTree, allFields, crossDs, dsMap);
//        Order2SQLObj.getOrders(sqlMeta, dto.getSortFields(), allFields, crossDs, dsMap);
//        String querySQL = SQLProvider.createQuerySQL(sqlMeta, false, false, needOrder);
//        querySQL = provider.rebuildSQL(querySQL, sqlMeta, crossDs, dsMap);
//        logger.debug("preview sql: " + querySQL);

        // 无法加行权限的情况下，直接用sql
        String querySQL = sql;

        String copilotSQL = receiveDTO.getSql();
        // 用calcite尝试将SQL转方言，如果失败了，就按照原SQL执行
//        copilotSQL = transSql(type, copilotSQL, provider, receiveDTO);

        // 通过数据源请求数据
        // 调用数据源的calcite获得data
        DatasourceRequest datasourceRequest = new DatasourceRequest();
        datasourceRequest.setDsList(dsMap);
        String s = "";
        Map<String, Object> data;
        try {
            s = copilotSQL
                    .replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + "de_tmp_table" + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, "(" + querySQL + ")")
                    .replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + "DE_TMP_TABLE" + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, "(" + querySQL + ")");
            logger.debug("copilot sql: " + s);
            datasourceRequest.setQuery(s);
            data = provider.fetchResultField(datasourceRequest);
        } catch (Exception e) {
            try {
                s = copilotSQL
                        .replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + "de_tmp_table" + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, "(" + querySQL + ") tmp")
                        .replaceAll(SqlPlaceholderConstants.KEYWORD_PREFIX_REGEX + "DE_TMP_TABLE" + SqlPlaceholderConstants.KEYWORD_SUFFIX_REGEX, "(" + querySQL + ") tmp");
                logger.debug("copilot sql: " + s);
                datasourceRequest.setQuery(s);
                data = provider.fetchResultField(datasourceRequest);
            } catch (Exception e1) {
                // 如果异常，则获取最后一条成功的history
                MsgDTO lastSuccessMsg = msgManage.getLastSuccessMsg(AuthUtils.getUser().getUserId(), dto.getId());
                // 请求数据出错，记录错误信息和copilot返回的信息
                MsgDTO result = new MsgDTO();
                result.setDatasetGroupId(dto.getId());
                result.setMsgType("api");
                result.setHistory(lastSuccessMsg == null ? new ArrayList<>() : lastSuccessMsg.getHistory());
                result.setCopilotSql(receiveDTO.getSql());
                result.setApiMsg(receiveDTO.getApiMessage());
                result.setSqlOk(receiveDTO.getSqlOk() ? 1 : 0);
                result.setChartOk(receiveDTO.getChartOk() ? 1 : 0);
                result.setChart(receiveDTO.getChart());
                result.setExecSql(s);
                result.setMsgStatus(0);
                result.setErrMsg(e1.getMessage());
                msgManage.save(result);
                return result;
            }
        }

        List<TableField> fields = (List<TableField>) data.get("fields");
        fields = transField(fields);
        Map<String, Object> map = new LinkedHashMap<>();
        // 重新构造data
        Map<String, Object> previewData = buildPreviewData(data, fields, desensitizationList);

        map.put("data", previewData);
        map.put("sql", Base64.getEncoder().encodeToString(s.getBytes()));

        // 重构chart结构，补全缺失字段
        rebuildChart(receiveDTO, fields);

        MsgDTO result = new MsgDTO();
        result.setDatasetGroupId(dto.getId());
        result.setMsgType("api");
        result.setHistory(receiveDTO.getHistory());
        result.setCopilotSql(receiveDTO.getSql());
        result.setApiMsg(receiveDTO.getApiMessage());
        result.setSqlOk(receiveDTO.getSqlOk() ? 1 : 0);
        result.setChartOk(receiveDTO.getChartOk() ? 1 : 0);
        result.setChart(receiveDTO.getChart());
        result.setChartData(map);
        result.setExecSql(s);
        result.setMsgStatus(1);
        msgManage.save(result);
        return result;
    }

    public ReceiveDTO copilotChat(DESendDTO deSendDTO) throws Exception {
        boolean valid = LicenseUtil.licenseValid();
        // call copilot service
        TokenDTO tokenDTO = tokenManage.getToken(valid);
        ReceiveDTO receiveDTO;
        if (StringUtils.isEmpty(tokenDTO.getToken())) {
            // get token
            String token;
            if (valid) {
                LicensePO read = f2CLicManage.read();
                token = copilotAPI.getToken(read.getLicense());
            } else {
                token = copilotAPI.getFreeToken();
            }
            tokenManage.updateToken(token, valid);
            receiveDTO = copilotAPI.generateChart(token, deSendDTO);
        } else {
            try {
                receiveDTO = copilotAPI.generateChart(tokenDTO.getToken(), deSendDTO);
            } catch (Exception e) {
                // error, get token again
                String token;
                if (valid) {
                    LicensePO read = f2CLicManage.read();
                    token = copilotAPI.getToken(read.getLicense());
                } else {
                    token = copilotAPI.getFreeToken();
                }
                tokenManage.updateToken(token, valid);
                throw new Exception(e.getMessage());
            }
        }

        if (!receiveDTO.getSqlOk() || !receiveDTO.getChartOk()) {
            DEException.throwException((String) JsonUtil.toJSONString(receiveDTO));
        }
        logger.debug("Copilot Service SQL: " + receiveDTO.getSql());
        logger.debug("Copilot Service Chart: " + JsonUtil.toJSONString(receiveDTO.getChart()));
        return receiveDTO;
    }

    public void rebuildChart(ReceiveDTO receiveDTO, List<TableField> fields) {
        if (StringUtils.equalsIgnoreCase(receiveDTO.getChart().getType(), "pie")) {
            AxisFieldDTO column = receiveDTO.getChart().getColumn();
            if (fields.size() != 2 || column == null) {
                DEException.throwException("当前字段不足以构建饼图: " + JsonUtil.toJSONString(receiveDTO));
            }
            AxisDTO axisDTO = new AxisDTO();
            AxisFieldDTO x = new AxisFieldDTO();
            AxisFieldDTO y = new AxisFieldDTO();
            if (StringUtils.equals(fields.get(0).getOriginName(), column.getValue())) {
                x.setName(column.getName());
                x.setValue(column.getValue());
                y.setName(fields.get(1).getOriginName());
                y.setValue(fields.get(1).getOriginName());
            } else if (StringUtils.equals(fields.get(1).getOriginName(), column.getValue())) {
                x.setName(fields.get(0).getOriginName());
                x.setValue(fields.get(0).getOriginName());
                y.setName(column.getName());
                y.setValue(column.getValue());
            } else {
                DEException.throwException("当前字段不足以构建饼图: " + JsonUtil.toJSONString(receiveDTO));
            }
            axisDTO.setX(x);
            axisDTO.setY(y);
            receiveDTO.getChart().setAxis(axisDTO);
        } else if (StringUtils.equalsIgnoreCase(receiveDTO.getChart().getType(), "table")) {
            // 将fields赋值给columns
            if (ObjectUtils.isEmpty(receiveDTO.getChart().getColumns())) {
                List<AxisFieldDTO> columns = new ArrayList<>();
                for (TableField field : fields) {
                    AxisFieldDTO dto = new AxisFieldDTO();
                    dto.setName(field.getOriginName());
                    dto.setValue(field.getOriginName());
                    columns.add(dto);
                }
                receiveDTO.getChart().setColumns(columns);
            }
        }

        // 所有图表都加上columns字段用于切换明细表展示
        if (Arrays.asList(chartType).contains(receiveDTO.getChart().getType())) {
            List<AxisFieldDTO> columns = new ArrayList<>();
            columns.add(receiveDTO.getChart().getAxis().getX());
            columns.add(receiveDTO.getChart().getAxis().getY());
            receiveDTO.getChart().setColumns(columns);
        }
    }

    public List<MsgDTO> getList(Long userId) {
        MsgDTO lastMsg = msgManage.getLastMsg(userId);
        if (lastMsg == null) {
            return null;
        }
        List<MsgDTO> msg = msgManage.getMsg(lastMsg);
        msgManage.deleteMsg(lastMsg);
        return msg;
    }

    public void clearAll(Long userId) {
        msgManage.clearAllUserMsg(userId);
    }

    public MsgDTO errorMsg(MsgDTO msgDTO, String errMsg) throws Exception {
        // 如果异常，则获取最后一条成功的history
        MsgDTO lastSuccessMsg = msgManage.getLastSuccessMsg(AuthUtils.getUser().getUserId(), msgDTO.getDatasetGroupId());
        MsgDTO dto = new MsgDTO();
        dto.setDatasetGroupId(msgDTO.getDatasetGroupId());
        dto.setHistory(lastSuccessMsg == null ? new ArrayList<>() : lastSuccessMsg.getHistory());
        dto.setMsgStatus(0);
        dto.setMsgType("api");
        dto.setErrMsg(errMsg);
        msgManage.save(dto);
        return dto;
    }

    public List<TableField> transField(List<TableField> fields) {
        fields.forEach(field -> {
            field.setDeExtractType(FieldUtils.transType2DeType(field.getFieldType()));
            field.setDeType(FieldUtils.transType2DeType(field.getFieldType()));
        });
        return fields;
    }

    public Map<String, Object> buildPreviewData(Map<String, Object> data, List<TableField> fields, Map<String, ColumnPermissionItem> desensitizationList) {
        Map<String, Object> map = new LinkedHashMap<>();
        List<String[]> dataList = (List<String[]>) data.get("data");
        List<LinkedHashMap<String, Object>> dataObjectList = new ArrayList<>();
        if (ObjectUtils.isNotEmpty(dataList)) {
            for (int i = 0; i < dataList.size(); i++) {
                String[] row = dataList.get(i);
                LinkedHashMap<String, Object> obj = new LinkedHashMap<>();
                if (row.length > 0) {
                    for (int j = 0; j < fields.size(); j++) {
                        TableField tableField = fields.get(j);
                        if (desensitizationList.containsKey(tableField.getOriginName())) {
                            obj.put(tableField.getOriginName(), ChartDataBuild.desensitizationValue(desensitizationList.get(tableField.getOriginName()), String.valueOf(row[j])));
                        } else {
                            if (tableField.getDeExtractType() == DeTypeConstants.DE_INT
                                    || tableField.getDeExtractType() == DeTypeConstants.DE_FLOAT
                                    || tableField.getDeExtractType() == DeTypeConstants.DE_BOOL) {
                                try {
                                    obj.put(tableField.getOriginName(), new BigDecimal(row[j]));
                                } catch (Exception e) {
                                    obj.put(tableField.getOriginName(), row[j]);
                                }
                            } else {
                                obj.put(tableField.getOriginName(), row[j]);
                            }
                        }
                    }
                }
                dataObjectList.add(obj);
            }
        }

        map.put("fields", fields);
        map.put("data", dataObjectList);
        return map;
    }

    public List<String> transCreateTableFields(List<DatasetTableFieldDTO> allFields) {
        List<String> list = new ArrayList<>();
        for (DatasetTableFieldDTO dto : allFields) {
            list.add(" " + dto.getDataeaseName() + " " + transNum2Type(dto.getDeExtractType()) +
                    " COMMENT '" + dto.getName() + "'");
        }
        return list;
    }

    public String transNum2Type(Integer num) {
        return switch (num) {
            case 0, 1, 5 -> "VARCHAR(50)";
            case 2, 3, 4 -> "INT(10)";
            default -> "VARCHAR(50)";
        };
    }

    public Lex getLex(String type) {
        switch (type) {
            case "oracle":
                return Lex.ORACLE;
            case "sqlServer":
            case "mssql":
                return Lex.SQL_SERVER;
            default:
                return Lex.JAVA;
        }
    }

    public String transSql(String type, String copilotSQL, Provider provider, ReceiveDTO receiveDTO) {
        if (type.equals("oracle") || type.equals("sqlServer")) {
            try {
                copilotSQL = copilotSQL.trim();
                if (copilotSQL.endsWith(";")) {
                    copilotSQL = copilotSQL.substring(0, copilotSQL.length() - 1);
                }
                DatasourceSchemaDTO datasourceSchemaDTO = new DatasourceSchemaDTO();
                datasourceSchemaDTO.setType(type);
                SqlDialect dialect = provider.getDialect(datasourceSchemaDTO);

                SqlParser parser = SqlParser.create(copilotSQL, SqlParser.Config.DEFAULT.withLex(getLex(type)));
                SqlNode sqlNode = parser.parseStmt();
                return sqlNode.toSqlString(dialect).toString().toLowerCase();
            } catch (Exception e) {
                logger.debug("calcite trans copilot SQL error");
                return receiveDTO.getSql();
            }
        } else {
            return copilotSQL;
        }
    }

    private String engine(String type) {
        switch (type) {
            case "ck":
                return "ClickHouse";
            case "pg":
                return "PostgreSQL";
            case "mysql":
                return "MySQL";
            case "sqlServer":
                return "SQL Server";
            default:
                return type;
        }
    }
}
