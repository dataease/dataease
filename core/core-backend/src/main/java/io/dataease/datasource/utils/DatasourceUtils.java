package io.dataease.datasource.utils;

import io.dataease.exception.DEException;
import io.dataease.extensions.datasource.dto.DatasourceSchemaDTO;
import io.dataease.i18n.Translator;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @Author Junjun
 */
public class DatasourceUtils {
    public static void checkDsStatus(Map<Long, DatasourceSchemaDTO> dsMap) {
        if (ObjectUtils.isEmpty(dsMap)) {
            DEException.throwException(Translator.get("i18n_datasource_delete"));
        }
        for (Map.Entry<Long, DatasourceSchemaDTO> next : dsMap.entrySet()) {
            DatasourceSchemaDTO ds = next.getValue();
            if (StringUtils.isNotEmpty(ds.getStatus()) && "Error".equalsIgnoreCase(ds.getStatus())) {
                DEException.throwException(Translator.get("i18n_invalid_ds"));
            }
        }
    }
}
