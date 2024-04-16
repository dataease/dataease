package io.dataease.ext;

public interface ExtSystemParameterMapper {
    String email();

    long queryPwdResetTime(Long userId);
}
