package io.dataease.controller.sys.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeptTreeNode implements Serializable {

    private Long id;

    private String label;

    private Boolean hasChildren;

    private List<DeptTreeNode> children;

    public List<DeptTreeNode> toList(){
        List<DeptTreeNode> lists = new ArrayList<>();
        lists.add(this);
        return lists;
    }
}
