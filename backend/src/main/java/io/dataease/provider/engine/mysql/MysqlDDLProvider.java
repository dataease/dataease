package io.dataease.provider.engine.mysql;

import io.dataease.commons.utils.TableUtils;
import io.dataease.provider.DDLProviderImpl;
import org.springframework.stereotype.Service;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("mysqlEngineDDL")
public class MysqlDDLProvider extends DDLProviderImpl {
    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE VIEW IF NOT EXISTS " + name + " AS (" + viewSQL + ")";
    }

    @Override
    public String dropTable(String name) {
        return "DROP TABLE IF EXISTS " + name;
    }

    @Override
    public String dropView(String name) {
        return "DROP VIEW IF EXISTS " + name;
    }

    @Override
    public String replaceTable(String name){
        String replaceTableSql = "rename table FROM_TABLE to FROM_TABLE_tmp, TO_TABLE to FROM_TABLE, FROM_TABLE_tmp to TO_TABLE; "
                .replace("FROM_TABLE", name).replace("TO_TABLE", TableUtils.tmpName(name));
        String dropTableSql = "DROP TABLE IF EXISTS " + TableUtils.tmpName(name);
        return  replaceTableSql + ";" + dropTableSql;
    }
}
