package io.dataease.dataset.manage;

import io.dataease.dao.auto.entity.CoreDatasetTable;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.listener.sql.DatasetSemanticsInit;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class Regression {
    @SuppressWarnings("unchecked")
    private static <T> T repository(Class<T> type, java.util.function.Function<String, Object> result) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type},
                (proxy, method, args) -> result.apply(method.getName()));
    }

    private static void snapshot() {
        var sourceTable = new io.dataease.datasource.dao.auto.entity.CoreDatasourceTableSemantic();
        sourceTable.setId(1L); sourceTable.setTableName("orders"); sourceTable.setActive(true); sourceTable.setAvailable(true);
        sourceTable.setDescription("源表说明");
        var sourceField = new io.dataease.datasource.dao.auto.entity.CoreDatasourceFieldSemantic();
        sourceField.setFieldName("status"); sourceField.setBusinessName("源字段说明"); sourceField.setValueReferences("[]");
        var tables = repository(io.dataease.datasource.dao.auto.repository.CoreDatasourceTableSemanticRepository.class,
                name -> name.equals("findByDatasource_IdOrderById") ? java.util.List.of(sourceTable) : java.util.Optional.of(sourceTable));
        var fields = repository(io.dataease.datasource.dao.auto.repository.CoreDatasourceFieldSemanticRepository.class,
                name -> java.util.List.of(sourceField));
        var snapshots = new io.dataease.datasource.manage.DatasourceSemanticSnapshot(tables, fields);
        var table = new io.dataease.extensions.datasource.dto.DatasetTableDTO(); table.setTableName("orders");
        var field = new io.dataease.extensions.datasource.dto.DatasetTableFieldDTO(); field.setOriginName("status");
        snapshots.tables(1L, java.util.List.of(table)); snapshots.fields(1L, "orders", java.util.List.of(field));
        sourceTable.setDescription("源表已改"); sourceField.setBusinessName("源字段已改");
        if (!"源表说明".equals(table.getSemanticDescription()) || !"源字段说明".equals(field.getSemanticBusinessName())) throw new AssertionError("snapshot independence");
        field.setSemanticBusinessName("数据集修改");
        if (!"源字段已改".equals(sourceField.getBusinessName())) throw new AssertionError("writeback");
    }

    public static void main(String[] args) {
        snapshot();
        String url = "jdbc:h2:mem:dataset_semantics;DB_CLOSE_DELAY=-1";
        var registry = new StandardServiceRegistryBuilder()
                .applySetting("hibernate.connection.url", url)
                .applySetting("hibernate.connection.driver_class", "org.h2.Driver")
                .applySetting("hibernate.hbm2ddl.auto", "create-drop").build();
        try (var factory = new MetadataSources(registry).addAnnotatedClass(CoreDatasetTable.class)
                .addAnnotatedClass(CoreDatasetTableField.class).buildMetadata().buildSessionFactory()) {
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(url));
            // Simulate an installation predating dataset semantics, then retry migration.
            for (String table : new String[]{"core_dataset_table", "core_dataset_table_field"}) {
                for (String column : jdbc.queryForList("select column_name from information_schema.columns where table_name = ? and column_name like 'SEMANTIC_%'", String.class, table.toUpperCase())) {
                    jdbc.execute("alter table " + table + " drop column " + column);
                }
            }
            jdbc.execute("create table core_dataset_group (id bigint primary key)");
            jdbc.update("insert into core_dataset_group (id) values (1)");
            var migration = new DatasetSemanticsInit(jdbc, factory);
            migration.execute();
            migration.execute();
            if (jdbc.queryForObject("select business_description from core_dataset_group where id = 1", String.class) != null) {
                throw new AssertionError("historical dataset business description must remain null");
            }
            jdbc.update("update core_dataset_group set business_description = ? where id = 1", "数据集业务说明");
            migration.execute();
            if (!"数据集业务说明".equals(jdbc.queryForObject("select business_description from core_dataset_group where id = 1", String.class))) {
                throw new AssertionError("migration retry must preserve business description");
            }
            try (var em = factory.createEntityManager()) {
                em.getTransaction().begin();
                var table = new CoreDatasetTable();
                table.setId(1L); table.setDatasetGroupId(1L); table.setSemanticDescription("独立表说明");
                em.persist(table);
                var field = new CoreDatasetTableField();
                field.setId(1L); field.setOriginName("status"); field.setType("VARCHAR");
                field.setDeType(0); field.setDeExtractType(0);
                field.setSemanticBusinessName("订单状态");
                field.setSemanticValueReferences("[{\"value\":\"2\",\"meaning\":\"已完成\"}]");
                em.persist(field); em.getTransaction().commit(); em.clear();
                em.getTransaction().begin();
                var loaded = em.find(CoreDatasetTableField.class, 1L);
                if (!"订单状态".equals(loaded.getSemanticBusinessName())) throw new AssertionError("round trip");
                loaded.setSemanticValueReferences("[]"); loaded.setSemanticDescription("");
                em.getTransaction().commit(); em.clear();
                if (!"[]".equals(em.find(CoreDatasetTableField.class, 1L).getSemanticValueReferences())) throw new AssertionError("clear");
                if (!"独立表说明".equals(em.find(CoreDatasetTable.class, 1L).getSemanticDescription())) throw new AssertionError("table round trip");
            }
            DatasetSemanticValidation.references("[{\"value\":\"0\"}]");
            DatasetSemanticValidation.references("[]");
            System.out.println("PASS: retryable migration, table/field round trip, explicit clearing, optional meaning");
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
