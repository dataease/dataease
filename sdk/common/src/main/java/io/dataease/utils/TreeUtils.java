package io.dataease.utils;

import cn.hutool.core.collection.CollectionUtil;
import io.dataease.model.ITreeBase;
import io.dataease.model.TreeBaseModel;
import io.dataease.model.TreeModel;
import io.dataease.model.TreeResultModel;
import org.apache.commons.lang3.ObjectUtils;
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

    private final static String I18N_PREFIX = "i18n_auth_menu.";

    public static <T extends TreeResultModel, R extends TreeBaseModel> List<T> mergeTree(List<R> list, Class<T> tClass, boolean appendI18nPrefix) {
        List<TreeModel> modelList = list.stream().map(item -> new TreeModel(item)).toList();
        List<TreeModel> modelResult = new ArrayList<>();
        Map<Long, List<TreeModel>> childMap = modelList.stream().collect(Collectors.groupingBy(TreeModel::getPid));
        List<Long> existedList = new ArrayList<>();
        modelList.forEach(po -> {
            List<TreeModel> children = null;
            if (CollectionUtil.isNotEmpty(children = childMap.get(po.getId()))) {
                po.setChildren(children);
                existedList.addAll(children.stream().map(TreeModel::getId).toList());
            }
        });
        if (CollectionUtil.isEmpty(modelList)) {
            return null;
        }
        if (CollectionUtil.isNotEmpty(existedList)) {
            modelResult = modelList.stream().filter(node -> !existedList.contains(node.getId())).toList();
        } else {
            modelResult = modelList;
        }
        return convertTree(modelResult, tClass, appendI18nPrefix);
    }

    public static <T extends TreeResultModel> List<T> convertTree(List<TreeModel> roots, Class<T> tClass, boolean appendI18nPrefix) {
        List<T> result = new ArrayList<>();
        for (int i = 0; i < roots.size(); i++) {
            TreeModel node = roots.get(i);
            if (appendI18nPrefix) {
                node.getData().setName(I18N_PREFIX + node.getName());
            }
            T instance = null;
            try {
                instance = tClass.newInstance();
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            T vo = BeanUtils.copyBean(instance, node.getData(), "children");
            result.add(vo);
            List<TreeModel> children = null;
            if (!CollectionUtils.isEmpty(children = node.getChildren())) {
                vo.setChildren(convertTree(children, tClass, appendI18nPrefix));
            }
        }
        return result;
    }

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
