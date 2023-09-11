package io.dataease.commons.constants;

public interface NoticeConstants {

    interface TaskType {
        String JENKINS_TASK = "JENKINS_TASK";
        String TEST_PLAN_TASK = "TEST_PLAN_TASK";
        String REVIEW_TASK = "REVIEW_TASK";
        String DEFECT_TASK = "DEFECT_TASK";
    }

    interface Mode {
        String API = "API";
        String SCHEDULE = "SCHEDULE";
    }

    interface Type {
        String EMAIL = "EMAIL";
        String NAIL_ROBOT = "NAIL_ROBOT";
        String WECHAT_ROBOT = "WECHAT_ROBOT";
    }

    interface Event {
        String EXECUTE_SUCCESSFUL = "EXECUTE_SUCCESSFUL";
        String EXECUTE_FAILED = "EXECUTE_FAILED";
        String CREATE = "CREATE";
        String UPDATE = "UPDATE";
        String DELETE = "DELETE";
        String COMMENT = "COMMENT";
    }

    interface RelatedUser {
        String FOUNDER = "FOUNDER";//创建人
        String EXECUTOR = "EXECUTOR";//负责人(评审人）
        String MAINTAINER = "MAINTAINER";//维护人
    }
}
