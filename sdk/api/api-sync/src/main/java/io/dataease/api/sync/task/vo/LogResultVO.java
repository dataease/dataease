package io.dataease.api.sync.task.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 日志返回结果
 *
 * @author fit2cloud
 */
@Getter
@Setter
public class LogResultVO {

    /**
     * 日志开始行号
     */
    private int fromLineNum;
    /**
     * 日志结束行号
     */
    private int toLineNum;
    /**
     * 日志内容
     */
    private String logContent;
    /**
     * 是否是最后一行
     */
    private boolean isEnd;

    public LogResultVO() {

    }

    public LogResultVO(int fromLineNum, int toLineNum, String logContent, boolean isEnd) {
        this.fromLineNum = fromLineNum;
        this.toLineNum = toLineNum;
        this.logContent = logContent;
        this.isEnd = isEnd;
    }


}
