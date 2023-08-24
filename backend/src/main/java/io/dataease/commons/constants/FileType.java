package io.dataease.commons.constants;

public enum FileType {
    JMX(".jmx"), CSV(".csv"), JSON(".json"), PDF(".pdf"),
    JPG(".jpg"), PNG(".png"), JPEG(".jpeg"), DOC(".doc"),
    XLSX(".xlsx"), DOCX(".docx"), JAR(".jar");

    // 保存后缀
    private String suffix;

    FileType(String suffix) {
        this.suffix = suffix;
    }

    public String suffix() {
        return this.suffix;
    }
}
