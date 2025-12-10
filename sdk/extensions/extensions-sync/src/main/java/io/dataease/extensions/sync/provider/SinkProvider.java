package io.dataease.extensions.sync.provider;

import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceRequest;
import io.dataease.extensions.sync.model.task.Source;
import io.dataease.extensions.sync.model.task.Target;
import io.dataease.extensions.sync.model.task.TaskInfoVO;

import java.util.List;

/**
 * 目标源接口
 *
 * @author jianneng
 * @date 2025/11/12 18:16
 **/
public interface SinkProvider {

    String TEMP_TABLE_PREFIX = "like_";

    default String getTempTableNamePrefix() {
        return TEMP_TABLE_PREFIX;
    }

    String getProviderName();

    /**
     * 删除表
     *
     * @param datasourceRequest 数据源请求
     * @param tableName         表名
     */
    boolean dropTable(DatasourceRequest datasourceRequest, String tableName);

    /**
     * 创建表
     *
     * @param datasourceRequest 数据源请求
     * @param createSql         创建语句
     */
    boolean createTable(DatasourceRequest datasourceRequest, String createSql);

    /**
     * 检查表是否存在
     *
     * @param datasourceRequest 数据源请求
     */
    boolean checkTableExists(DatasourceRequest datasourceRequest);

    /**
     * 获取创建表语句
     *
     * @param source 数据源
     * @param target 目标数据源
     * @return 创建表语句
     */
    String getCreateTableSql(Source source, Target target);

    /**
     * 根据datasource获取增量字段最大值
     *
     * @return 最大值
     */
    String getTargetTableIncrementFieldMaxValue(DatasourceRequest datasourceRequest, TaskInfoVO incrementTaskInfo);

    /**
     * 创建表
     * 创建一个与tableName一样结构的表
     * 默认为like_tableName
     * test_table -> like_test_table
     *
     * @param datasourceRequest datasource 配置
     * @param createTableSql    创建表语句
     * @return 是否创建成功
     */
    boolean createTableLike(DatasourceRequest datasourceRequest, String createTableSql);

    /**
     * 替换表
     * 替换test_table的数据为like_test_table的数据
     * 并删除like_test_table
     *
     * @param datasourceRequest datasource 配置
     * @param tableName         表名
     * @return 是否替换成功
     */
    boolean replaceTable(DatasourceRequest datasourceRequest, String tableName);

    /**
     * 检查表是否有数据
     *
     * @param datasourceRequest datasource 配置
     * @param tableName         表名
     * @return 是否有数据
     */
    boolean checkTableHasData(DatasourceRequest datasourceRequest, String tableName);


    /**
     * 验证建表SQL是否有效
     *
     * @param datasourceRequest datasource 配置
     * @param createTableSql    创建表语句
     * @param tableName         表名
     * @return 是否有效
     */
    boolean validateCreateTableSql(DatasourceRequest datasourceRequest, String createTableSql, String tableName);

    /**
     * 获取支持的数据类型
     *
     * @return 数据类型列表
     */
    List<String> dataTypes(DatasourceRequest datasourceRequest);

    /**
     * 数据类型映射
     * 将源表字段的数据类型映射为目标表字段的数据类型
     *
     * @param sourceTableFields 源表字段列表
     * @param source            源数据源
     * @param target            目标数据源
     */
    void dataTypeMapping(List<TableFieldDTO> sourceTableFields, DatasourceRequest source, DatasourceRequest target);

    /**
     * 验证目标源参数
     *
     * @param taskTargetDataSource 目标数据源
     */
    void validateTarget(Target taskTargetDataSource);

    /**
     * 生成Sink
     * 更多属性配置参考 SeaTunnel 官网：<a href="https://seatunnel.apache.org/zh-CN/docs/2.3.12/connector-v2/sink">...</a>
     *
     * @param taskInfoVO 任务信息
     * @return 替换后的配置
     */
    String generatorSinkConfig(TaskInfoVO taskInfoVO);

    /**
     * 默认主键字段
     *
     * @param datasourceRequest datasource 配置
     * @return 默认主键字段
     */
    default TableFieldDTO defaultKeyField(DatasourceRequest datasourceRequest) {
        return null;
    }

}
