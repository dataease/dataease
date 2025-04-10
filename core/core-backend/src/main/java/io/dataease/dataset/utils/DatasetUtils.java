package io.dataease.dataset.utils;

import io.dataease.api.dataset.dto.BaseTreeNodeDTO;
import io.dataease.api.dataset.union.DatasetGroupInfoDTO;
import io.dataease.engine.constant.ExtFieldConstant;
import io.dataease.extensions.datasource.dto.DatasetTableFieldDTO;
import io.dataease.extensions.view.dto.ChartViewDTO;
import io.dataease.utils.TreeUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.util.Assert;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author Junjun
 */
public class DatasetUtils {
    public final static String SEPARATOR = "-de-";

    public static List<BaseTreeNodeDTO> mergeDuplicateTree(List<BaseTreeNodeDTO> tree, String... rootPid) {
        Assert.notNull(rootPid, "Root Pid cannot be null");
        if (CollectionUtils.isEmpty(tree)) {
            return null;
        }
        List<BaseTreeNodeDTO> result = new ArrayList<>();
        // 构建id-节点map映射
        Map<String, BaseTreeNodeDTO> treePidMap = tree.stream().collect(Collectors.toMap(node -> node.getNodeType(), t -> t));
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
                BaseTreeNodeDTO parentNode = treePidMap.get(parentType);
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

    public static String getEncode(String str) {
        return Base64.getEncoder().encodeToString(str.getBytes());
    }

    public static String getDecode(String str) {
        return new String(Base64.getDecoder().decode(str));
    }

    /**
     * 计算字段表达式base64加密
     *
     * @param obj
     */
    public static void dsEncode(DatasetGroupInfoDTO obj) {
        for (DatasetTableFieldDTO dto : obj.getAllFields()) {
            if (dto.getExtField().equals(ExtFieldConstant.EXT_CALC)) {
                dto.setOriginName(getEncode(dto.getOriginName()));
            }
        }
    }

    /**
     * 计算字段表达式base64解密
     *
     * @param obj
     */
    public static void dsDecode(DatasetGroupInfoDTO obj) {
        for (DatasetTableFieldDTO dto : obj.getAllFields()) {
            if (dto.getExtField().equals(ExtFieldConstant.EXT_CALC)) {
                dto.setOriginName(getDecode(dto.getOriginName()));
            }
        }
    }

    /**
     * 计算字段表达式base64加密
     *
     * @param fields
     */
    public static void listEncode(List<? extends DatasetTableFieldDTO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        for (DatasetTableFieldDTO dto : fields) {
            if (dto.getExtField().equals(ExtFieldConstant.EXT_CALC)) {
                dto.setOriginName(getEncode(dto.getOriginName()));
            }
        }
    }

    /**
     * 计算字段表达式base64解密
     *
     * @param fields
     */
    public static void listDecode(List<? extends DatasetTableFieldDTO> fields) {
        if (CollectionUtils.isEmpty(fields)) {
            return;
        }
        for (DatasetTableFieldDTO dto : fields) {
            if (dto.getExtField().equals(ExtFieldConstant.EXT_CALC)) {
                dto.setOriginName(getDecode(dto.getOriginName()));
            }
        }
    }

    public static void viewDecode(ChartViewDTO view) {
        DatasetUtils.listDecode(view.getXAxis());
        DatasetUtils.listDecode(view.getXAxisExt());
        DatasetUtils.listDecode(view.getYAxis());
        DatasetUtils.listDecode(view.getYAxisExt());
        DatasetUtils.listDecode(view.getExtStack());
        DatasetUtils.listDecode(view.getExtBubble());
        DatasetUtils.listDecode(view.getExtLabel());
        DatasetUtils.listDecode(view.getExtTooltip());
        DatasetUtils.listDecode(view.getExtColor());
    }

    public static void viewEncode(ChartViewDTO view) {
        DatasetUtils.listEncode(view.getXAxis());
        DatasetUtils.listEncode(view.getXAxisExt());
        DatasetUtils.listEncode(view.getYAxis());
        DatasetUtils.listEncode(view.getYAxisExt());
        DatasetUtils.listEncode(view.getExtStack());
        DatasetUtils.listEncode(view.getExtBubble());
        DatasetUtils.listEncode(view.getExtLabel());
        DatasetUtils.listEncode(view.getExtTooltip());
        DatasetUtils.listEncode(view.getExtColor());
    }
}
