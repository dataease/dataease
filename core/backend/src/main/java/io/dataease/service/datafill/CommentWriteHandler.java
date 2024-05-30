package io.dataease.service.datafill;

import com.alibaba.excel.write.handler.RowWriteHandler;
import com.alibaba.excel.write.metadata.holder.WriteSheetHolder;
import com.alibaba.excel.write.metadata.holder.WriteTableHolder;
import io.dataease.plugins.common.dto.datafill.ExtTableField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.util.List;

@Getter
@Setter
public class CommentWriteHandler implements RowWriteHandler {

    private List<ExtTableField> fields;

    @Override
    public void beforeRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Integer rowIndex, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterRowCreate(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {

    }

    @Override
    public void afterRowDispose(WriteSheetHolder writeSheetHolder, WriteTableHolder writeTableHolder, Row row, Integer relativeRowIndex, Boolean isHead) {
        if (isHead) {
            Drawing<?> drawingPatriarch = writeSheetHolder.getSheet().createDrawingPatriarch();
            for (int i = 0; i < fields.size(); i++) {
                ExtTableField field = fields.get(i);
                String required = field.getSettings().isRequired() ? "必填 " : "";
                String unique = field.getSettings().isUnique() && StringUtils.equalsIgnoreCase("input", field.getType()) ? "不允许重复值" : "";
                String example = "";
                StringBuilder options = new StringBuilder();
                switch (field.getSettings().getMapping().getType()) {
                    case datetime:
                        String dateFormat = "yyyy/MM/dd";
                        switch (field.getSettings().getDateType()) {
                            case "year":
                                dateFormat = "yyyy";
                                break;
                            case "month":
                            case "monthrange":
                                dateFormat = "yyyy/MM";
                                break;
                            case "datetime":
                            case "datetimerange":
                                dateFormat = "yyyy/MM/dd HH:mm:ss";
                                break;
                            case "date":
                            case "daterange":
                                dateFormat = "yyyy/MM/dd";
                                break;
                            default:
                                if (field.getSettings().isEnableTime()) { //兼容旧版
                                    dateFormat = "yyyy/MM/dd HH:mm:ss";
                                }
                        }
                        example = "\n(日期格式: " + dateFormat + ")";
                        break;
                    case number:
                        example = "\n(整形数字)";
                        break;
                    case decimal:
                        example = "\n(小数数字)";
                        break;
                    case text:
                    case nvarchar:
                        if (StringUtils.equalsIgnoreCase("select", field.getType()) && field.getSettings().isMultiple() || StringUtils.equalsIgnoreCase("checkbox", field.getType())) {
                            example = "\n(多个值使用分号\";\"分割)";
                        } else if (StringUtils.equalsIgnoreCase("email", field.getSettings().getInputType())) {
                            example = "\n(邮箱格式)";
                        } else if (StringUtils.equalsIgnoreCase("phone", field.getSettings().getInputType())) {
                            example = "\n(手机号格式)";
                        }
                        if (StringUtils.equalsIgnoreCase("select", field.getType()) || StringUtils.equalsIgnoreCase("checkbox", field.getType()) || StringUtils.equalsIgnoreCase("radio", field.getType())) {
                            options = new StringBuilder("\n选项值为:\n");
                            for (ExtTableField.Option option : field.getSettings().getOptions()) {
                                options.append(option.getName()).append("\n");
                            }
                        }
                        break;
                }

                if (StringUtils.isBlank(required) && StringUtils.isBlank(unique) && StringUtils.isBlank(example) && StringUtils.isBlank(options.toString())) {
                    continue;
                }

                Comment comment = drawingPatriarch.createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) i + 1, i, (short) i + 7, i + 6));
                comment.setString(new XSSFRichTextString(required + example + options.toString()));

                row.getCell(i).setCellComment(comment);

            }


        }
    }
}