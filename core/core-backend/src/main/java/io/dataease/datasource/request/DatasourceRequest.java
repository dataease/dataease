package io.dataease.datasource.request;

import io.dataease.dataset.dto.DatasourceSchemaDTO;
import io.dataease.datasource.dao.auto.entity.CoreDatasource;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class DatasourceRequest {
    private final String REG_WITH_SQL_FRAGMENT = "((?i)WITH[\\s\\S]+(?i)AS?\\s*\\([\\s\\S]+\\))\\s*(?i)SELECT";
    private Pattern WITH_SQL_FRAGMENT = Pattern.compile("((?i)WITH[\\s\\S]+(?i)AS?\\s*\\([\\s\\S]+\\))\\s*(?i)SELECT");
    protected String query;
    protected String table;
    protected CoreDatasource datasource;
    private Integer pageSize;
    private Integer page;
    private Integer realSize;
    private Integer fetchSize = 10000;
    private boolean pageable = false;
    private boolean previewData = false;
    private boolean totalPageFlag;
    private Map<String, DatasourceSchemaDTO> dsList;

    public DatasourceRequest() {
    }

    public String getQuery() {
        return this.rebuildSqlWithFragment(this.query);
    }

    public void setQuery(String query) {
        this.query = query;
    }

    private String rebuildSqlWithFragment(String sql) {
        if (!sql.toLowerCase().startsWith("with")) {
            Matcher matcher = this.WITH_SQL_FRAGMENT.matcher(sql);
            if (matcher.find()) {
                String withFragment = matcher.group();
                if (!StringUtils.isEmpty(withFragment)) {
                    if (withFragment.length() > 6) {
                        int lastSelectIndex = withFragment.length() - 6;
                        sql = sql.replace(withFragment, withFragment.substring(lastSelectIndex));
                        withFragment = withFragment.substring(0, lastSelectIndex);
                    }

                    sql = withFragment + " " + sql;
                    sql = sql.replaceAll(" {2,}", " ");
                }
            }
        }

        return sql;
    }

    public String getREG_WITH_SQL_FRAGMENT() {
        this.getClass();
        return "((?i)WITH[\\s\\S]+(?i)AS?\\s*\\([\\s\\S]+\\))\\s*(?i)SELECT";
    }
}
