package io.dataease.excel.internal;

import org.apache.commons.lang3.StringUtils;
import org.apache.fesod.sheet.converters.string.StringNumberConverter;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;

import java.util.Map;

/**
 * 按列指定日期格式的字符串转换器，集中承载填报读取的日期/文本格式规则。
 */
public class FesodColumnDateConverter extends StringNumberConverter {

    private final Map<Integer, String> columnDateFormats;

    public FesodColumnDateConverter(Map<Integer, String> columnDateFormats) {
        this.columnDateFormats = columnDateFormats;
    }

    @Override
    public String convertToJavaData(ReadCellData<?> cellData, ExcelContentProperty contentProperty, GlobalConfiguration globalConfiguration) {
        if (cellData == null || cellData.getDataFormatData() == null) {
            return super.convertToJavaData(cellData, contentProperty, globalConfiguration);
        }
        String dataFormat = cellData.getDataFormatData().getFormat();
        // 核心判断：如果 DataFormat 是 "@"，说明是文本格式，文本格式的数字不应被当作日期序列号解析
        if (StringUtils.equals("@", dataFormat)) {
            Number numberValue = cellData.getNumberValue();
            if (numberValue != null) {
                double doubleVal = numberValue.doubleValue();
                if (doubleVal == Math.floor(doubleVal) && !Double.isInfinite(doubleVal)) {
                    return String.valueOf((long) doubleVal);
                }
                return String.valueOf(doubleVal);
            }
            return "";
        }
        String dateFormatStr = columnDateFormats.get(cellData.getColumnIndex());
        if (StringUtils.isNotBlank(dateFormatStr)) {
            if (StringUtils.equals("yyyy", dateFormatStr) && !StringUtils.equals(cellData.getDataFormatData().getFormat(), "General")) {
                cellData.getDataFormatData().setFormat(dateFormatStr);
            } else if (!StringUtils.equals("yyyy", dateFormatStr)) {
                cellData.getDataFormatData().setFormat(dateFormatStr);
            }
        }
        return super.convertToJavaData(cellData, contentProperty, globalConfiguration);
    }
}
