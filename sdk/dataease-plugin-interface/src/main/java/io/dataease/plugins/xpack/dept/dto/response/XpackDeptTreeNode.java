package io.dataease.plugins.xpack.dept.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class XpackDeptTreeNode implements Serializable {

    private Long id;

    private String label;

    private Boolean hasChildren;

    private List<XpackDeptTreeNode> children;

    public List<XpackDeptTreeNode> toList(){
        List<XpackDeptTreeNode> lists = new ArrayList<>();
        lists.add(this);
        return lists;
    }
}
