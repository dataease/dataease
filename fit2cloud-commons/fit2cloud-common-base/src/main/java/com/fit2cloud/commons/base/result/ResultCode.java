package com.fit2cloud.commons.base.result;


import lombok.Data;


public enum ResultCode {

    OK(null, 20000),TOKENIll("Illegal token", 50008),
    RELOG("Other clients logged in", 50012),TOKENEXP("Token expired", 50014),NOPERMISSION("No permission!", 50016);

    private String message;

    private int value;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    ResultCode() {
    }

    ResultCode(String message, int value) {
        this.message = message;
        this.value = value;
    }
}
