package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewProject implements Serializable {
    private String reviewId;

    private String projectId;

    private static final long serialVersionUID = 1L;
}
