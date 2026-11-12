package io.dataease.api.ds;

import com.github.xiaoymin.knife4j.annotations.ApiSupport;
import io.dataease.auth.DeApiPath;
import io.dataease.auth.DePermit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static io.dataease.constant.AuthResourceEnum.DATASOURCE;

@Tag(name = "数据源管理:语义管理")
@ApiSupport(order = 969)
@DeApiPath(value = "/datasource/metadata", rt = DATASOURCE)
public interface DatasourceSemanticApi {
    @DePermit({"#p0+':read'"})
    @GetMapping("/{datasourceId}/semantics")
    @Operation(summary = "查询表和字段语义")
    Semantics list(@PathVariable("datasourceId") Long datasourceId);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/table")
    @Operation(summary = "保存表语义")
    void saveTable(@PathVariable("datasourceId") Long datasourceId, @RequestBody TableSemantic table);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/tables/add")
    @Operation(summary = "添加语义表")
    void addTables(@PathVariable("datasourceId") Long datasourceId, @RequestBody List<String> tableNames);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/tables/remove")
    @Operation(summary = "移除语义表")
    void removeTable(@PathVariable("datasourceId") Long datasourceId, @RequestBody TableReference table);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/tables/refresh")
    @Operation(summary = "同步物理表结构")
    boolean refreshTable(@PathVariable("datasourceId") Long datasourceId, @RequestBody TableReference table);

    @DePermit({"#p0+':manage'"})
    @PostMapping("/{datasourceId}/field")
    @Operation(summary = "保存字段语义")
    void saveField(@PathVariable("datasourceId") Long datasourceId, @RequestBody FieldSemantic field);

    @Schema(description = "数据源语义管理数据")
    record Semantics(
            @Schema(description = "语义表列表") List<TableSemantic> tables,
            @Schema(description = "已添加语义表的字段及其语义") List<FieldSemantic> fields,
            @Schema(description = "以物理表名为键的关联数量，自关联计一次") Map<String, Long> relationCounts
    ) {}

    @Schema(description = "物理表引用")
    record TableReference(
            @Schema(description = "物理表名") String tableName
    ) {}

    @Schema(description = "数据源表语义")
    record TableSemantic(
            @Schema(description = "物理表名") String tableName,
            @Schema(description = "表的业务说明") String description,
            @Schema(description = "数据粒度，例如一行代表一笔订单") String grain,
            @Schema(description = "语义表是否已添加，手工移除后为 false") boolean active,
            @Schema(description = "最近一次同步的物理表显示名称") String physicalLabel,
            @Schema(description = "最近一次同步时物理表是否存在") boolean available
    ) {}

    @Schema(description = "数据源字段语义")
    record FieldSemantic(
            @Schema(description = "物理表名") String tableName,
            @Schema(description = "物理字段名") String fieldName,
            @Schema(description = "字段的业务名称") String businessName,
            @Schema(description = "字段的业务说明，选填") String description,
            @Schema(description = "字段的业务别名") String aliases,
            @Schema(description = "字段语义是否已确认") boolean confirmed,
            @Schema(description = "最近一次同步的物理字段类型") String physicalType,
            @Schema(description = "最近一次同步的物理字段备注") String physicalComment
    ) {}
}
