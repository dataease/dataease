package io.dataease.api.log.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogOpVO implements Serializable {

    private String value;

    private String label;

    private List<LogOpVO> children;
}
