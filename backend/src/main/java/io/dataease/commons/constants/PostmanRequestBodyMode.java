package io.dataease.commons.constants;

public enum PostmanRequestBodyMode {

    RAW("raw"), FORM_DATA("formdata"), URLENCODED("urlencoded"), FILE("file");

    private String value;

    PostmanRequestBodyMode(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
