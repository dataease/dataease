package io.dataease.base.domain;

import java.io.Serializable;
import lombok.Data;

@Data
public class TestCaseReviewUsers implements Serializable {
    private String reviewId;

    private String userId;

    private static final long serialVersionUID = 1L;
}
