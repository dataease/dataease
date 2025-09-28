package io.dataease.visualization.utils;

public enum ImageFileType {

    /**
     * JPEG
     */
    JPEG("FFD8FF", "jpg"),

    /**
     * PNG
     */
    PNG("89504E47", "png"),

    /**
     * GIF
     */
    GIF("47494638", "gif");

    private String value = "";
    private String ext = "";

    ImageFileType(String value) {
        this.value = value;
    }

    ImageFileType(String value, String ext) {
        this(value);
        this.ext = ext;
    }

    public String getExt() {
        return ext;
    }

    public String getValue() {
        return value;
    }

}