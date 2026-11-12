package io.dataease.result;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultMessage implements Serializable {

    private Integer code;

    private String msg;

    private Object data;

    // Optional error metadata. Omit absent fields without changing global serialization rules.
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String category;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String messageKey;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Object[] args;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private String requestId;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private java.util.List<FieldError> fieldErrors;
    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private Boolean retryable;

    public record FieldError(String field, String messageKey, String message) implements Serializable { }

    public ResultMessage() {}

    public ResultMessage(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static ResultMessage success() {
        ResultMessage result = new ResultMessage();
        result.setResultCode(ResultCode.SUCCESS);
        return result;
    }

    public static ResultMessage success(Object data) {
        ResultMessage result = new ResultMessage();
        result.setResultCode(ResultCode.SUCCESS);
        result.setData(data);
        return result;
    }

    public static ResultMessage failure(ResultCode resultCode) {
        ResultMessage result = new ResultMessage();
        result.setResultCode(resultCode);
        return result;
    }

    public static ResultMessage failure(ResultCode resultCode, Object data) {
        ResultMessage result = new ResultMessage();
        result.setResultCode(resultCode);
        result.setData(data);
        return result;
    }

    public void setResultCode(ResultCode code) {
        this.code = code.code();
        this.msg = code.message();
    }
}
