package io.dataease.commons.constants;

public class DataVisualizationConstants {
    //删除标志
    public static final class DELETE_FLAG {
        //已删除
        public static final boolean DELETED = true;
        //未删除（可用）
        public static final boolean AVAILABLE = false;

    }

    //节点类型
    public static final class NODE_TYPE {
        //目录
        public static final String FOLDER = "folder";
        //资源节点
        public static final String LEAF = "leaf";
    }


    //操作
    public static final class RESOURCE_OPT_TYPE {
        //新建资源节点
        public static final String NEW_LEAF = "newLeaf";
        //新建文件夹
        public static final String NEW_FOLDER = "newFolder";
        //移动
        public static final String MOVE = "move";
        //重命名
        public static final String RENAME = "rename";
        //复制
        public static final String COPY = "copy";
    }


}
