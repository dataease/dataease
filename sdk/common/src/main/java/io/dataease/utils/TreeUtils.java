package io.dataease.utils;

import io.dataease.model.ITreeBase;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TreeUtils {

    public final static String DEFAULT_ROOT = "root";
    public final static String SEPARATOR = "-de-";

    /**
     * Description: rootPid 是根节点PID
     */
    public static <T extends ITreeBase> List<T> mergeTree(List<T> tree, Long... rootPid) {
        Assert.notNull(rootPid, "Root Pid cannot be null");
        if (CollectionUtils.isEmpty(tree)) {
            return null;
        }
        List<T> result = new ArrayList<>();
        // 构建id-节点map映射
        Map<Long, T> treePidMap = tree.stream().collect(Collectors.toMap(T::getId, t -> t));
        tree.stream().forEach(node -> {
            // 判断根节点
            if (Arrays.asList(rootPid).contains(node.getPid())) {
                result.add(node);
            } else {
                //找到父元素
                T parentNode = treePidMap.get(node.getPid());
                if (parentNode == null) {
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
    public static <T extends ITreeBase> List<T> mergeTree(List<T> tree) {
        return mergeTree(tree, 0L);
    }


    public static <T extends ITreeBase> List<T> mergeDuplicateTree(List<T> tree, Long... rootPid) {
        Assert.notNull(rootPid, "Root Pid cannot be null");
        if (CollectionUtils.isEmpty(tree)) {
            return null;
        }
        List<T> result = new ArrayList<>();
        // 构建id-节点map映射
        Map<String, T> treePidMap = tree.stream().collect(Collectors.toMap(node -> node.getNodeType(), t -> t));
        tree.stream().filter(item -> ObjectUtils.isNotEmpty(item.getId())).forEach(node -> {

            String nodeType = node.getNodeType();
            String[] links = nodeType.split(SEPARATOR);
            int length = links.length;
            int level = Integer.parseInt(links[length - 1]);
            // 判断根节点
            if (Arrays.asList(rootPid).contains(node.getPid()) && 0 == level) {
                result.add(node);
            } else {
                //找到父元素
                String[] pLinks = new String[level];
                System.arraycopy(links, 0, pLinks, 0, level);
                String parentType = Arrays.stream(pLinks).collect(Collectors.joining(SEPARATOR)) + TreeUtils.SEPARATOR + (level - 1);
                T parentNode = treePidMap.get(parentType);
                if (parentNode == null) {
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

}
