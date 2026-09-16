package io.dataease.exportCenter.util;

public final class ExportFileNameUtils {

    private ExportFileNameUtils() {
    }

    /** 仅用于新导出任务的下载名称，不用于解析服务器上的文件路径。 */
    public static String excelFileName(String name) {
        if (name == null || name.isBlank()) {
            return "export.xlsx";
        }
        StringBuilder result = new StringBuilder(name.length());
        for (int i = 0; i < name.length(); i++) {
            char character = name.charAt(i);
            if ("/\\:*?\"<>|".indexOf(character) >= 0 || Character.isISOControl(character)) {
                result.append('_');
            } else {
                result.append(character);
            }
        }
        // 保持与现有文件名校验一致；末尾的点不能与扩展名前的点组成 ".."。
        String safeName = result.toString().replaceAll("\\.{2,}", "_").replaceAll("[. ]+$", "_");
        return safeName + ".xlsx";
    }
}
