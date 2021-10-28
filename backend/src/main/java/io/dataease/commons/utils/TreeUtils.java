package io.dataease.commons.utils;

import io.dataease.commons.model.ITreeBase;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: wangjiahao
 * Date: 2021-05-24
 * Description:树构建工具
 */
public class TreeUtils{

    /**
     * Description: rootPid 是根节点PID
     */
    public static<T extends ITreeBase> List<T> mergeTree(List<T> tree,String ... rootPid) {
        Assert.notNull(rootPid, "Root Pid cannot be null");
        List<T> result = new ArrayList<>();
        // 构建id-节点map映射
        Map<String, T> treePidMap = tree.stream().collect(Collectors.toMap(T::getId, t -> t));
        tree.stream().forEach(node -> {
            // 判断根节点
            if (Arrays.asList(rootPid).contains(node.getPid())) {
                result.add(node);
            } else {
                //找到父元素
                T parentNode = treePidMap.get(node.getPid());
                if(parentNode==null){
                    // 可能出现 rootPid 更高的节点 这个操作相当于截断
                    return;
                }
                if (parentNode.getChildren() == null) {
                    parentNode.setChildren(new ArrayList());
                }
                parentNode.getChildren().add(node);
            }
        });
        return result;
    }


    /**
     * Description: rootPid 是根节点PID 档期那默认是0
     */
    public static<T extends ITreeBase> List<T> mergeTree(List<T> tree) {
        return mergeTree(tree,"0");
    }

}
