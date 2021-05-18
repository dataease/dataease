package io.dataease.provider.mysql;

import io.dataease.provider.DDLProvider;
import org.springframework.stereotype.Service;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("mysqlDDL")
public class MysqlDDLProvider extends DDLProvider {
    @Override
    public String createView(String name, String viewSQL) {
        return "CREATE VIEW IF NOT EXISTS " + name + " AS (" + viewSQL + ")";
    }

    @Override
    public String dropTableOrView(String name) {
        return "DROP TABLE IF EXISTS " + name;
    }
}
