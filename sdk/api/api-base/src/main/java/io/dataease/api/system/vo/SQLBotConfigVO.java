package io.dataease.api.system.vo;

import io.dataease.api.system.request.SQLBotConfigCreator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SQLBotConfigVO extends SQLBotConfigCreator implements Serializable {
}
