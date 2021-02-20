package io.dataease.commons.constants;

public enum MsRequestBodyType {


    KV("KeyValue"), FORM_DATA("Form Data"), RAW("Raw");

    private String value;

    MsRequestBodyType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
