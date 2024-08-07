package io.dataease.extensions.view.filter;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author Junjun
 */
@Data
public class DynamicTimeSetting implements Serializable {
    private String relativeToCurrent;//相对当前 thisYear ｜ lastYear ｜ thisMonth ｜ lastMonth ｜ today ｜ yesterday ｜ monthBeginning ｜ yearBeginning
    private String timeGranularity;//时间粒度 year ｜ month ｜ date ｜ datetime
    private Integer timeNum;// 数值
    private String relativeToCurrentType;// year ｜ month ｜ date
    private String around;// 前 f ｜ 后 b
    private String arbitraryTime;//timeGranularity = datetime时 取时分秒
}
