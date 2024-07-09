package io.dataease.copilot.dao.auto.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author fit2cloud
 * @since 2024-07-04
 */
@TableName("core_copilot_msg")
public class CoreCopilotMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 数据集ID
     */
    private Long datasetGroupId;

    /**
     * user or api
     */
    private String msgType;

    /**
     * mysql oracle ...
     */
    private String engineType;

    /**
     * create sql
     */
    private String schemaSql;

    /**
     * 用户提问
     */
    private String question;

    /**
     * 历史信息
     */
    private String history;

    /**
     * copilot 返回 sql
     */
    private String copilotSql;

    /**
     * copilot 返回信息
     */
    private String apiMsg;

    /**
     * sql 状态
     */
    private Integer sqlOk;

    /**
     * chart 状态
     */
    private Integer chartOk;

    /**
     * chart 内容
     */
    private String chart;

    /**
     * 视图数据
     */
    private String chartData;

    /**
     * 执行请求的SQL
     */
    private String execSql;

    /**
     * msg状态，0失败 1成功
     */
    private Integer msgStatus;

    /**
     * de错误信息
     */
    private String errMsg;

    /**
     * 创建时间
     */
    private Long createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getDatasetGroupId() {
        return datasetGroupId;
    }

    public void setDatasetGroupId(Long datasetGroupId) {
        this.datasetGroupId = datasetGroupId;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getEngineType() {
        return engineType;
    }

    public void setEngineType(String engineType) {
        this.engineType = engineType;
    }

    public String getSchemaSql() {
        return schemaSql;
    }

    public void setSchemaSql(String schemaSql) {
        this.schemaSql = schemaSql;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getHistory() {
        return history;
    }

    public void setHistory(String history) {
        this.history = history;
    }

    public String getCopilotSql() {
        return copilotSql;
    }

    public void setCopilotSql(String copilotSql) {
        this.copilotSql = copilotSql;
    }

    public String getApiMsg() {
        return apiMsg;
    }

    public void setApiMsg(String apiMsg) {
        this.apiMsg = apiMsg;
    }

    public Integer getSqlOk() {
        return sqlOk;
    }

    public void setSqlOk(Integer sqlOk) {
        this.sqlOk = sqlOk;
    }

    public Integer getChartOk() {
        return chartOk;
    }

    public void setChartOk(Integer chartOk) {
        this.chartOk = chartOk;
    }

    public String getChart() {
        return chart;
    }

    public void setChart(String chart) {
        this.chart = chart;
    }

    public String getChartData() {
        return chartData;
    }

    public void setChartData(String chartData) {
        this.chartData = chartData;
    }

    public String getExecSql() {
        return execSql;
    }

    public void setExecSql(String execSql) {
        this.execSql = execSql;
    }

    public Integer getMsgStatus() {
        return msgStatus;
    }

    public void setMsgStatus(Integer msgStatus) {
        this.msgStatus = msgStatus;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CoreCopilotMsg{" +
        "id = " + id +
        ", userId = " + userId +
        ", datasetGroupId = " + datasetGroupId +
        ", msgType = " + msgType +
        ", engineType = " + engineType +
        ", schemaSql = " + schemaSql +
        ", question = " + question +
        ", history = " + history +
        ", copilotSql = " + copilotSql +
        ", apiMsg = " + apiMsg +
        ", sqlOk = " + sqlOk +
        ", chartOk = " + chartOk +
        ", chart = " + chart +
        ", chartData = " + chartData +
        ", execSql = " + execSql +
        ", msgStatus = " + msgStatus +
        ", errMsg = " + errMsg +
        ", createTime = " + createTime +
        "}";
    }
}
