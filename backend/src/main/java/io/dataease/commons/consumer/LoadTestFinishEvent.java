package io.dataease.commons.consumer;

import io.dataease.base.domain.LoadTestReport;

public interface LoadTestFinishEvent {
    void execute(LoadTestReport loadTestReport);
}
