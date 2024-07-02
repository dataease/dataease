package io.dataease.commons.constants;

public class DataVisualizationConstants {

    //新建仪表板来源
    public static final class QUERY_SOURCE {

        // 定时报告
        public static final String REPORT = "report";

        // 主工程
        public static final String MAIN = "main";
    }

    //新建仪表板来源
    public static final class NEW_PANEL_FROM {

        // 直接新建
        public static final String NEW = "new";

        // 内部模板新建
        public static final String NEW_INNER_TEMPLATE = "new_inner_template";

        // 外部模板新建
        public static final String NEW_OUTER_TEMPLATE = "new_outer_template";

        // 模板市场新建
        public static final String NEW_MARKET_TEMPLATE = "new_market_template";

    }

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

        public static final String EDIT = "edit";
        //复制
        public static final String COPY = "copy";
    }

    public static final class TEMPLATE_SOURCE {
        //模板市场
        public static final String MARKET = "market";
        //模板管理
        public static final String MANAGE = "manage";
        //公共
        public static final String PUBLIC = "public";
    }

}
