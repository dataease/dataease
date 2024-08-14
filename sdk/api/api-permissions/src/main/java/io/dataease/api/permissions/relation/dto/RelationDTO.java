package io.dataease.api.permissions.relation.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.util.List;

/**
 * @Author Junjun
 */
@Data
public class RelationDTO {
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String name;
    private String auths;
    private String type;
    private String creator;
    private Long updateTime;
    private List<RelationDTO> subRelation;
}
