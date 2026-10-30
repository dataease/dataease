package io.dataease.excel.internal;

import org.apache.commons.lang3.StringUtils;
import org.apache.fesod.sheet.converters.string.StringNumberConverter;
import org.apache.fesod.sheet.metadata.GlobalConfiguration;
import org.apache.fesod.sheet.metadata.data.ReadCellData;
import org.apache.fesod.sheet.metadata.property.ExcelContentProperty;
import org.apache.fesod.sheet.util.DateUtils;

import java.util.Collections;
import java.util.Map;

/**
 * 按列指定日期格式的字符串转换器，集中承载填报读取的日期/文本格式规则。
 * 未配置按列格式时，使用传入的 defaultDatePattern 作为无模型读取默认日期格式。
 */
public class FesodColumnDateConverter extends StringNumberConverter {

    private final Map<Integer, String> columnDateFormats;

    private final String defaultDatePattern;

    public FesodColumnDateConverter(Map<Integer, String> columnDateFormats) {
        this(columnDateFormats, null);
    }

    public FesodColumnDateConverter(Map<Integer, String> columnDateFormats, String defaultDatePattern) {
        this.columnDateFormats = columnDateFormats == null ? Collections.emptyMap() : columnDateFormats;
        this.defaultDatePattern = defaultDatePattern;
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
        String columnFormat = columnDateFormats.get(cellData.getColumnIndex());
        String targetFormat = StringUtils.isNotBlank(columnFormat) ? columnFormat : defaultDatePattern;
        if (StringUtils.isNotBlank(targetFormat)) {
            boolean applyFormat = StringUtils.isNotBlank(columnFormat) || isDateCell(cellData, dataFormat);
            if (applyFormat) {
                if (StringUtils.equals("yyyy", targetFormat) && !StringUtils.equals(cellData.getDataFormatData().getFormat(), "General")) {
                    cellData.getDataFormatData().setFormat(targetFormat);
                } else if (!StringUtils.equals("yyyy", targetFormat)) {
                    cellData.getDataFormatData().setFormat(targetFormat);
                }
            }
        }
        return super.convertToJavaData(cellData, contentProperty, globalConfiguration);
    }

    private boolean isDateCell(ReadCellData<?> cellData, String dataFormat) {
        Short formatIndex = cellData.getDataFormatData().getIndex();
        return formatIndex != null && StringUtils.isNotBlank(dataFormat) && DateUtils.isADateFormat(formatIndex, dataFormat);
    }
}
