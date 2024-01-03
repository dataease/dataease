package io.dataease.plugins.xpack.lark.dto.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class LarkGroupData implements Serializable {

    private boolean has_more;

    private String page_token;

    private List<LarkGroupItem> items;
}
