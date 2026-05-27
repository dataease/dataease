package io.dataease.datasource.provider;

import io.dataease.extensions.datasource.dto.TableField;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ApiUtilsTest {

    @Test
    public void paramFieldMatchUsesRenamedFieldNameWhenOriginNameIsDuplicated() {
        TableField firstField = tableField("id1", "id");
        TableField secondField = tableField("id2", "id");
        List<TableField> fields = Arrays.asList(firstField, secondField);

        assertTrue(ApiUtils.isParamFieldMatched(fields, firstField, "id1"));
        assertFalse(ApiUtils.isParamFieldMatched(fields, firstField, "id2"));
        assertTrue(ApiUtils.isParamFieldMatched(fields, secondField, "id2"));
        assertFalse(ApiUtils.isParamFieldMatched(fields, secondField, "id1"));
    }

    @Test
    public void paramFieldMatchKeepsOriginNameFallbackForSavedConfigurations() {
        TableField field = tableField("id1", "id");

        assertTrue(ApiUtils.isParamFieldMatched(Arrays.asList(field), field, "id"));
    }

    @Test
    public void paramFieldMatchPrefersRenamedFieldNameOverAnotherFieldOriginName() {
        TableField renamedField = tableField("id", "user_id");
        TableField originNameField = tableField("name", "id");
        List<TableField> fields = Arrays.asList(renamedField, originNameField);

        assertTrue(ApiUtils.isParamFieldMatched(
                fields,
                renamedField,
                "id"
        ));
        assertFalse(ApiUtils.isParamFieldMatched(
                fields,
                originNameField,
                "id"
        ));
    }

    @Test
    public void paramFieldMatchHandlesEmptyValues() {
        TableField field = tableField("id1", "id");

        assertFalse(ApiUtils.isParamFieldMatched(Arrays.asList(field), field, ""));
        assertFalse(ApiUtils.isParamFieldMatched(null, null, "id1"));
    }

    private TableField tableField(String name, String originName) {
        TableField field = new TableField();
        field.setName(name);
        field.setOriginName(originName);
        return field;
    }
}
