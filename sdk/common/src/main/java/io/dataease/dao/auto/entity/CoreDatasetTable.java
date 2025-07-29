package io.dataease.dao.auto.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Comment;

@Getter
@Setter
@Entity
@Table(name = "core_dataset_table")
@Comment("数据集表")
public class CoreDatasetTable {
    @Comment("ID")
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    @Comment("名称")
    @Size(max = 128)
    @Column(name = "name", length = 128)
    private String name;

    @Comment("物理表名")
    @Size(max = 128)
    @Column(name = "table_name", length = 128)
    private String tableName;

    @Comment("数据源ID")
    @Column(name = "datasource_id")
    private Long datasourceId;

    @Comment("数据集ID")
    @NotNull
    @Column(name = "dataset_group_id", nullable = false)
    private Long datasetGroupId;

    @Comment("db,sql,union,excel,api")
    @Size(max = 50)
    @Column(name = "type", length = 50)
    private String type;

    @Comment("表原始信息,表名,sql等")
    @Column(name = "info", length = 16777216)
    private String info;

    @Comment("SQL参数")
    @Column(name = "sql_variable_details", length = 16777216)
    private String sqlVariableDetails;

    public CoreDatasetTable() {

    }

    public CoreDatasetTable(Long id, String name, String tableName, Long datasourceId, Long datasetGroupId, String type, String info, String sqlVariableDetails) {
        this.id = id;
        this.name = name;
        this.tableName = tableName;
        this.datasourceId = datasourceId;
        this.datasetGroupId = datasetGroupId;
        this.type = type;
        this.info = info;
        this.sqlVariableDetails = sqlVariableDetails;
    }
}
