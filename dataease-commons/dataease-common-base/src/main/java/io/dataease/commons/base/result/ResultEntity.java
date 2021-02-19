package io.dataease.commons.base.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResultEntity implements Serializable {

    private String message;

    private boolean success;

    private int code;

    private Object data;

    public ResultEntity success(){
        this.code = ResultCode.OK.getValue();
        this.success = true;
        return this;
    }
    public ResultEntity success(Object o){
        this.code = ResultCode.OK.getValue();
        this.success = true;
        this.data = o;
        return this;
    }
    public ResultEntity error(){
        this.code = 500;
        this.success = false;
        return this;
    }
    public ResultEntity error(String msg){
        this.code = 500;
        this.success = false;
        this.message = msg;
        return this;
    }
    public ResultEntity permission(){
        this.code = ResultCode.NOPERMISSION.getValue();
        this.success = false;
        this.message = ResultCode.NOPERMISSION.getMessage();
        return this;
    }

    public ResultEntity tokenExp(){
        this.code = ResultCode.TOKENEXP.getValue();
        this.success = false;
        this.message = ResultCode.TOKENEXP.getMessage();
        return this;
    }

}
