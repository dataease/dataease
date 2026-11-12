package io.dataease.api.ds;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "数据源管理:表关联")
@ApiSupport(order = 969)
@DeApiPath(value = "/datasource/metadata", rt = DATASOURCE)
public interface DatasourceRelationApi {
    @DePermit({"#p0+':read'"})
    @GetMapping("/{datasourceId}/relations")
    @Operation(summary = "查询表关联列表")
    List<Relation> list(@PathVariable("datasourceId") Long datasourceId);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/relation")
    @Operation(summary = "保存并确认表关联")
    void saveRelation(@PathVariable("datasourceId") Long datasourceId, @RequestBody Relation relation);

    @DePermit({"#p0+':manage'"})
    @DeleteMapping("/{datasourceId}/relation/{id}")
    @Operation(summary = "移除表关联")
    void removeRelation(@PathVariable("datasourceId") Long datasourceId, @PathVariable("id") Long id);

    @Schema(description = "字段等值关联条件")
    record Condition(
            @Schema(description = "左表物理字段名") String leftField,
            @Schema(description = "右表物理字段名") String rightField
    ) {}

    enum Cardinality { ONE_TO_ONE, ONE_TO_MANY, MANY_TO_ONE, MANY_TO_MANY }

    enum JoinType { LEFT, INNER, RIGHT, FULL }

    @Schema(description = "数据源表关联")
    record Relation(
            @JsonSerialize(using = ToStringSerializer.class)
            @Schema(description = "关联 ID，新增时留空", type = "string") Long id,
            @Schema(description = "关联名称") String name,
            @Schema(description = "左侧物理表名") String leftTable,
            @Schema(description = "右侧物理表名") String rightTable,
            @Schema(description = "字段等值条件，多组条件按 AND 组合") List<Condition> conditions,
            @Schema(description = "从左表到右表的关系基数") Cardinality cardinality,
            @Schema(description = "推荐的 SQL 连接方式") JoinType joinType,
            @Schema(description = "关联的业务说明") String description,
            @Schema(description = "是否已确认") boolean confirmed,
            @Schema(description = "关联引用的物理表和字段是否有效") boolean valid,
            @Schema(description = "最近一次校验发现的缺失表或字段") List<MissingReference> missingReferences
    ) {}

    @Schema(description = "关联引用的缺失表或字段")
    record MissingReference(
            @Schema(description = "物理表名") String tableName,
            @Schema(description = "缺失字段名，整张表缺失时为空") String fieldName
    ) {}
}
