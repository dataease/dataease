package io.dataease.provider;

import io.dataease.plugins.common.base.domain.DatasetTableField;
import io.dataease.plugins.common.base.domain.Datasource;
import io.dataease.plugins.common.dto.dataset.DataTableInfoDTO;
import io.dataease.plugins.datasource.provider.DDLProvider;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DDLProviderImpl extends DDLProvider {
    @Override
    public String createView(String name, String viewSQL) {
        return null;
    }

    @Override
    public String dropTable(String name) {
        return null;
    }

    @Override
    public String dropView(String name) {
        return null;
    }

    @Override
    public String replaceTable(String name) {
        return null;
    }

    @Override
    public String createTableSql(DataTableInfoDTO dataTableInfoDTO, String name, List<DatasetTableField> datasetTableFields, Datasource engine, String version) {
        return null;
    }

    @Override
    public String insertSql(String name, List<String[]> dataList, int page, int pageNumber) {
        String insertSql = "INSERT INTO TABLE_NAME VALUES ".replace("TABLE_NAME", name);
        StringBuffer values = new StringBuffer();

        Integer realSize = page * pageNumber < dataList.size() ? page * pageNumber : dataList.size();
        for (String[] strings : dataList.subList((page - 1) * pageNumber, realSize)) {
            String[] strings1 = new String[strings.length];
            for (int i = 0; i < strings.length; i++) {
                if (StringUtils.isEmpty(strings[i])) {
                    strings1[i] = "";
                } else {
                    strings1[i] = strings[i].replace("'", "\\'");
                }
            }
            values.append("('").append(UUID.randomUUID())
                    .append("','").append(String.join("','", Arrays.asList(strings1)))
                    .append("'),");
        }
        return insertSql + values.substring(0, values.length() - 1);
    }


}
