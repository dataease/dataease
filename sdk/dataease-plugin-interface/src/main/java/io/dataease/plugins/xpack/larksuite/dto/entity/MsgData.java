package io.dataease.plugins.xpack.larksuite.dto.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class MsgData implements Serializable {

    private String message_id;

    private List<String> invalid_department_ids;

    private List<String> invalid_open_ids;

    private List<String> invalid_user_ids;

    private List<String> invalid_union_ids;
}
