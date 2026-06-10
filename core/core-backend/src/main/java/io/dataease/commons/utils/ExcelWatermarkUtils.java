package io.dataease.commons.utils;

import io.dataease.api.permissions.user.vo.UserFormVO;
import io.dataease.i18n.Translator;
import io.dataease.utils.IPUtils;
import io.dataease.visualization.dto.WatermarkContentDTO;
import org.apache.poi.ss.usermodel.*;

import java.awt.Color;
import java.awt.Font;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelWatermarkUtils {
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    public static String transContent(WatermarkContentDTO watermarkContent, UserFormVO userInfo) {
        String content = "";
        switch (watermarkContent.getType()) {
            case "custom" -> content = watermarkContent.getContent();
            case "nickName" -> content = "${nickName}";
            case "ip" -> content = "${ip}";
            case "time" -> content = "${time}";
            default -> content = "${username}";
        }
        String nickName = userInfo.getName().contains("i18n_") ?Translator.get(userInfo.getName()):userInfo.getName();
        content = content.replaceAll("\\$\\{ip}", IPUtils.get() == null ? "127.0.0.1" : IPUtils.get());
        content = content.replaceAll("\\$\\{username}", userInfo.getAccount());
        content = content.replaceAll("\\$\\{nickName}", nickName);
        content = content.replaceAll("\\$\\{time}", sdf.format(new Date()));
        return content;
    }


    /**
     * 添加水印图片到工作簿并返回图片 ID
     */
    public static int addWatermarkImage(Workbook wb, WatermarkContentDTO watermarkContent, UserFormVO userInfo) {
        byte[] imageBytes = createTextImage(transContent(watermarkContent, userInfo), watermarkContent); // 生成文字水印图片
        return wb.addPicture(imageBytes, Workbook.PICTURE_TYPE_PNG); // 添加到工作簿并返回 ID
    }

    public static void addWatermarkToSheet(Sheet sheet, Integer pictureIdx) {
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        CreationHelper helper = sheet.getWorkbook().getCreationHelper();
        // 获取工作表的总列数和行数
        int lastRowNum = sheet.getLastRowNum();
        int totalColumns = 0;
        for (int i = 0; i <= lastRowNum; i++) {
            Row row = sheet.getRow(i);
            if (row != null) {
                totalColumns = Math.max(totalColumns, row.getLastCellNum());
            }
        }

        // 如果没有内容，则假设默认覆盖100行和50列
        if (lastRowNum == 0 && totalColumns == 0) {
            lastRowNum = 100;
            totalColumns = 50;
        }
        int picCount = 0;
        // 根据总行列数循环绘制水印
        for (int row = 0; row <= lastRowNum; row += 15) { // 每15行绘制一行水印
            for (int col = 0; col <= totalColumns; col += 8) { // 每8列绘制一列水印
                // 创建水印图片位置
                addWater(helper, drawing, picCount++, pictureIdx, col, row, col + 5, row + 10);
            }
        }
    }

    public static void addWater(CreationHelper helper, Drawing<?> drawing, int picCount, int pictureIdx, int col1, int row1, int col2, int row2) {
        ClientAnchor anchor = helper.createClientAnchor();
        // 创建水印图片位置
        anchor.setCol1(col1);       // 水印起始列
        anchor.setRow1(row1);       // 水印起始行
        anchor.setCol2(col2);   // 水印结束列
        anchor.setRow2(row2);  // 水印结束行
        Picture picture = drawing.createPicture(anchor, pictureIdx);
        //picture.resize 作用： 因为Excel 显示图片出现bug 使用同一个pictureIdx时如果图片所以参数都一样只是位置不同的时候
        //会只显示一个 使用picture.resize进行放大倍数的小范围调整可以全部显示
        picture.resize(1 + (0.000001 * picCount));
    }

    public static byte[] createTextImage(String text, WatermarkContentDTO watermarkContent) {
        double radians = Math.toRadians(15);// 15度偏转
        int width = watermarkContent.getWatermark_fontsize() * text.length();
        int height = (int) Math.round(watermarkContent.getWatermark_fontsize() + width * Math.sin(radians));
        int fontSize = watermarkContent.getWatermark_fontsize();
        Color baseColor = Color.decode(watermarkContent.getWatermark_color());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        // 设置透明背景
        image = g2d.getDeviceConfiguration().createCompatibleImage(width, height, Transparency.TRANSLUCENT);
        g2d.dispose();
        g2d = image.createGraphics();

        // 设置抗锯齿
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 设置字体
        g2d.setFont(new Font(null, Font.PLAIN, fontSize));
        g2d.setColor(new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 50)); // 半透明颜色
        g2d.rotate(radians, width / 2.0, height / 2.0); // 旋转文字
        // 绘制文字
        FontMetrics fontMetrics = g2d.getFontMetrics();
        int textWidth = fontMetrics.stringWidth(text);
        int textHeight = fontMetrics.getHeight();
        int x = (width - textWidth) / 2;
        int y = (height + textHeight) / 2 - fontMetrics.getDescent();
        g2d.drawString(text, x, y);

        g2d.dispose();

        // 转为字节数组
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(image, "png", baos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
}
