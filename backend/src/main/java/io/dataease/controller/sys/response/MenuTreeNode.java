package io.dataease.controller.sys.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MenuTreeNode implements Serializable {

    private Long id;

    private String label;

    private Boolean hasChildren;

    private List<MenuTreeNode> children;

    public List<MenuTreeNode> toList(){
        List<MenuTreeNode> lists = new ArrayList<>();
        lists.add(this);
        return lists;
    }
}
