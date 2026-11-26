package io.dataease.extensions.sync.model.task;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.dataease.extensions.sync.model.TableFieldDTO;
import io.dataease.extensions.sync.model.datasource.DatasourceDTO;
import lombok.Data;

import java.io.IOException;
import java.util.List;

/**
 * @author fit2cloud
 **/
@Data
public class Target {
    /**
     * 当前字段映射协议版本
     */
    public static final int CURRENT_FIELD_MAPPING_VERSION = 2;

    private String type;
    private String createTable;
    private List<TableFieldDTO> fieldList;
    private String tableName;
    private DatasourceDTO datasource;
    private String datasourceId;
    private String targetProperty;
    private String incrementSync;
    private String incrementField;
    private String datasourceVersion;
    private Integer fieldMappingVersion;
    private double faultToleranceRate;
    private String incrementFieldType;
    private String remarks;
    private Long incrementOffset;
    private String incrementOffsetUnit;
    /**
     * 空值表示继承部署配置，不将系统默认值固化为任务配置
     */
    @JsonDeserialize(using = DorisBatchSizeDeserializer.class)
    private Integer dorisBatchSize;

    public Target() {
    }

    public Target(Target target) {
        this.type = target.type;
        this.createTable = target.createTable;
        this.fieldList = target.fieldList;
        this.tableName = target.tableName;
        this.datasource = target.datasource;
        this.datasourceId = target.datasourceId;
        this.targetProperty = target.targetProperty;
        this.incrementSync = target.incrementSync;
        this.incrementField = target.incrementField;
        this.datasourceVersion = target.datasourceVersion;
        this.fieldMappingVersion = target.fieldMappingVersion;
        this.faultToleranceRate = target.faultToleranceRate;
        this.incrementFieldType = target.incrementFieldType;
        this.remarks = target.remarks;
        this.incrementOffset = target.incrementOffset;
        this.incrementOffsetUnit = target.incrementOffsetUnit;
        this.dorisBatchSize = target.dorisBatchSize;
    }

    /**
     * 拒绝小数和字符串强制转换，避免非法批次在进入 Provider 前被截断
     */
    public static class DorisBatchSizeDeserializer extends JsonDeserializer<Integer> {
        @Override
        public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
                return (Integer) context.handleUnexpectedToken(Integer.class, parser);
            }
            return parser.getIntValue();
        }
    }
}
