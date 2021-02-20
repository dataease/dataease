package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewTestCase implements Serializable {
    private String id;

    private String reviewId;

    private String caseId;

    private String status;

    private String result;

    private String reviewer;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
