package io.dataease.ext;

public interface ExtTaskMapper {

    int runningCount(Long taskId);

    void resetRunnings(Long taskId);

}
