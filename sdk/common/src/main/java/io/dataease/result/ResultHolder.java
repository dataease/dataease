package io.dataease.result;


import java.io.Serial;
import java.io.Serializable;

public class ResultHolder implements Serializable {

    @Serial
    private static final long serialVersionUID = -6070157865089258673L;

    public ResultHolder() {
        this.success = true;
    }

    private ResultHolder(Object data) {
        this.data = data;
        this.success = true;
    }

    private ResultHolder(boolean success, String msg) {
        this.success = success;
        this.message = msg;
    }

    private ResultHolder(boolean success, String msg, Object data) {
        this.success = success;
        this.message = msg;
        this.data = data;
    }

    // 请求是否成功
    private boolean success = false;
    // 描述信息
    private String message;
    // 返回数据
    private Object data = "";

    public boolean isSuccess() {
        return this.success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public static ResultHolder success(Object obj) {
        return new ResultHolder(obj);
    }

    public static ResultHolder error(String message) {
        return new ResultHolder(false, message, null);
    }

    public static ResultHolder error(String message, Object object) {
        return new ResultHolder(false, message, object);
    }


}
