package io.dataease.provider.oracle;

import io.dataease.provider.DDLProvider;
import org.springframework.stereotype.Service;

/**
 * @Author gin
 * @Date 2021/5/17 4:27 下午
 */
@Service("oracleDDL")
public class OracleDDLProvider extends DDLProvider {
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
}
