package io.dataease.extensions.sync.provider;

import io.dataease.exception.DEException;
import io.dataease.extensions.sync.datatype.StandardDataType;
import io.dataease.extensions.sync.model.TableDTO;
import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.SourceFieldMappingPolicy;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.extensions.sync.model.task.TaskInfoVO;

import java.util.List;

/**
 * 源数据源接口
 *
 * @author jianneng
 * @date 2025/11/12 18:15
 **/
public interface SourceProvider {

    String getProviderName();

    /**
     * 获取数据源下的表信息
     *
     * @param datasourceRequest 数据源请求参数
     * @return 表信息列表
     */
    List<TableDTO> getTables(DatasourceRequest datasourceRequest);

    /**
     * 获取表字段信息
     * 物理表存在主键时，应将对应字段的 fieldPk 设置为 true；没有主键时保持 false
     * fieldSourceType 应保留源数据库返回的原始类型
     * fieldSourceStandardType 和 fieldType 应返回 DataEase 标准类型
     *
     * @param datasourceRequest 数据源请求
     * @return 表字段信息列表
     * @throws DEException 异常
     */
    List<TableFieldDTO> fetchTableField(DatasourceRequest datasourceRequest) throws DEException;

    /**
     * 通过SQL获取字段信息
     * fieldSource 可不用赋值
     * 主要的字段有 fieldName, fieldSourceType, fieldSourceStandardType, fieldType, fieldSize, fieldPrecision
     *
     * @param datasourceRequest 数据源请求参数
     * @return 字段信息列表
     */
    List<TableFieldDTO> getFieldsBySql(DatasourceRequest datasourceRequest);

    /**
     * 生成源端配置
     * 配置 SeaTunnel 作业中源端的配置信息
     * 注意!!!!! 其中 result_table_name 固定 fake ,请不要修改！
     * 返回格式类似如下：
     * {
     * "Jdbc": {
     * "plugin_name": "Jdbc",
     * "result_table_name": "fake",
     * "url": "jdbc:mysql://host:3306/db?characterEncoding=UTF-8&connectTimeout=5000",
     * "driver": "com.mysql.cj.jdbc.Driver",
     * "user": "root",
     * "password": "123456",
     * "query": "select `id`,`name`,`pid` from `t_demo`",
     * "partition_num": 10,
     * "fetch_size": 20000,
     * "connection_check_timeout_sec": 100,
     * "properties": {
     * "hikari.maximum-pool-size": 20,
     * "hikari.minimum-idle": 5,
     * "hikari.connection-timeout": 30000,
     * "hikari.idle-timeout": 600000,
     * "hikari.max-lifetime": 1800000
     * }
     * }
     * }
     * 更多属性配置参考 SeaTunnel 官网：<a href="https://seatunnel.apache.org/zh-CN/docs/2.3.12/connector-v2/source">...</a>
     * 也可参考 DataEase 官方插件源码的实现
     *
     * @param taskInfoVO 任务信息
     * @return 替换后的配置
     */
    String generatorSourceConfig(TaskInfoVO taskInfoVO);

    /**
     * 追加增量条件
     * 增量同步时，追加增量条件到作业参数中
     * 如增量字段为update_time，则追加类似 "WHERE update_time > last_sync_time" 的条件
     *
     * @param taskInfo     任务信息
     * @param jobParameter 作业参数
     * @return 追加增量条件后的作业参数
     */
    String appendIncrementConditions(TaskInfoVO taskInfo, String jobParameter);

    /**
     * 将源数据类型转换为标准数据类型
     *
     * @param sourceType 源数据类型
     * @return 标准数据类型
     */
    StandardDataType dataTypeToStandardDataType(String sourceType);

    /**
     * 返回源字段的默认目标映射策略
     * 连接器仅在无法可靠验证 SeaTunnel 类型转换时覆写为 REQUIRE_MANUAL_MAPPING
     *
     * @param field 源字段
     * @return 默认映射策略
     */
    default SourceFieldMappingPolicy fieldMappingPolicy(TableFieldDTO field) {
        return SourceFieldMappingPolicy.AUTO;
    }

}
