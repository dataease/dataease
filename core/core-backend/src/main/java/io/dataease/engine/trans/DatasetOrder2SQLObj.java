package io.dataease.engine.trans;

import io.dataease.api.chart.dto.DeSortField;
import io.dataease.constant.SQLConstants;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.model.SQLObj;
import org.apache.commons.lang3.ObjectUtils;

import java.util.*;

/**
 * @Author Junjun
 */
public class DatasetOrder2SQLObj {

    public static void getOrders(SQLMeta meta, List<DeSortField> sortFields, List<DatasetTableFieldDTO> originFields) {
        SQLObj tableObj = meta.getTable();
        List<SQLObj> xOrders = meta.getXOrders() == null ? new ArrayList<>() : meta.getXOrders();
        if (ObjectUtils.isEmpty(tableObj)) {
            return;
        }
        if (ObjectUtils.isNotEmpty(sortFields)) {
            for (int i = 0; i < sortFields.size(); i++) {
                DeSortField deSortField = sortFields.get(i);
                for (int j = 0; j < originFields.size(); j++) {
                    if (deSortField.getId().equals(originFields.get(j).getId())) {
                        String fieldAlias = String.format(SQLConstants.FIELD_ALIAS_X_PREFIX, j);
                        SQLObj order = SQLObj.builder()
                                .orderField(String.format(SQLConstants.FIELD_DOT, fieldAlias))
                                .orderAlias(String.format(SQLConstants.FIELD_DOT, fieldAlias))
                                .orderDirection(deSortField.getOrderDirection()).build();
                        xOrders.add(order);
                    }
                }


            }
            meta.setXOrders(xOrders);
        }
    }

}
