package io.dataease.base.mapper.ext;

public interface ExtTaskMapper {

    int runningCount(Long taskId);

    void resetRunnings(Long taskId);

}
