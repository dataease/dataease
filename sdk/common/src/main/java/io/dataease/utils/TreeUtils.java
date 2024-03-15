package io.dataease.utils;

import io.dataease.constant.SortConstants;
import io.dataease.model.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.text.Collator;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class TreeUtils {

    public final static String DEFAULT_ROOT = "root";
    public final static String SEPARATOR = "-de-";

    private final static String I18N_PREFIX = "i18n_auth_menu.";

    public static <T extends TreeResultModel, R extends TreeBaseModel> List<T> mergeTree(List<R> list, Class<T> tClass, boolean appendI18nPrefix) {
        AtomicBoolean rootExist = new AtomicBoolean(false);
        List<TreeModel> modelList = list.stream().map(item -> {
            TreeModel treeModel = new TreeModel(item);
            if (isRoot(treeModel)) {
                rootExist.set(true);
            }
            return treeModel;
        }).toList();
        List<TreeModel> modelResult = new ArrayList<>();
        Map<Long, List<TreeModel>> childMap = modelList.stream().collect(Collectors.groupingBy(TreeModel::getPid));
        List<Long> existedList = new ArrayList<>();
        modelList.forEach(po -> {
            List<TreeModel> children = null;
            if (CollectionUtils.isNotEmpty(children = childMap.get(po.getId()))) {
                po.setChildren(children);
                existedList.addAll(children.stream().map(TreeModel::getId).toList());
            }
        });
        if (CollectionUtils.isEmpty(modelList)) {
            return null;
        }
        List<TreeModel> floatingList = modelList.stream().filter(node -> !isRoot(node) && !existedList.contains(node.getId())).toList();
        if (CollectionUtils.isNotEmpty(existedList)) {
            modelResult = modelList.stream().filter(node -> !existedList.contains(node.getId())).toList();
        } else {
            modelResult = modelList;
        }
        if (rootExist.get() && CollectionUtils.isNotEmpty(floatingList)) {
            modelResult = modelResult.stream().filter(TreeUtils::isRoot).collect(Collectors.toList());
            TreeModel root = modelResult.get(0);
            if (root.getChildren() == null) {
                root.setChildren(new ArrayList<>());
            }
            root.getChildren().addAll(floatingList);
        }

        return convertTree(modelResult, tClass, appendI18nPrefix);
    }

    private static boolean isRoot(TreeModel node) {
        return node.getId().equals(0L) && (ObjectUtils.isEmpty(node.getPid()) || node.getPid().equals(-1L));
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

    public static List<BusiNodeVO> customSortVO(List<BusiNodeVO> list, String sortType) {
        Collator collator = Collator.getInstance(Locale.CHINA);
        if (StringUtils.equalsIgnoreCase(SortConstants.NAME_DESC, sortType)) {
            Set<BusiNodeVO> poSet = new TreeSet<>(Comparator.comparing(BusiNodeVO::getName, collator));
            poSet.addAll(list);
            return poSet.stream().collect(Collectors.toList());
        } else if (StringUtils.equalsIgnoreCase(SortConstants.NAME_ASC, sortType)) {
            Set<BusiNodeVO> poSet = new TreeSet<>(Comparator.comparing(BusiNodeVO::getName, collator).reversed());
            poSet.addAll(list);
            return poSet.stream().collect(Collectors.toList());
        } else if (StringUtils.equalsIgnoreCase(SortConstants.TIME_ASC, sortType)) {
            Collections.reverse(list);
            return list;
        } else {
            // 默认时间倒序
            return list;
        }
    }
}
