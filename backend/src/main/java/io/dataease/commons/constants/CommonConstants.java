package io.dataease.commons.constants;

/**
 * Author: wangjiahao
 * Date: 2021-05-25
 * Description:
 */
public class CommonConstants {


    //操作类型
    public static final class OPT_TYPE {

        public static final String INSERT = "insert";

        public static final String UPDATE = "update";

        public static final String DELETE = "delete";

        public static final String SELECT = "select";

    }

    //操作类型
    public static final class CHECK_RESULT {

        // 不存在
        public static final String NONE = "none";

        // 全局存在
        public static final String EXIST_ALL = "exist_all";

        // 当前用户存在
        public static final String EXIST_USER = "exist_user";

        // 其他用户存在
        public static final String EXIST_OTHER = "exist_other";

    }

    //视图数据查询来源
    public static final class VIEW_QUERY_FROM {

        // 仪表板
        public static final String PANEL = "panel";

        // 仪表板编辑
        public static final String PANEL_EDIT = "panel_edit";

    }

    //视图数据查询模式
    public static final class VIEW_RESULT_MODE {

        // 所有
        public static final String ALL = "all";

        // 自定义
        public static final String CUSTOM = "custom";
    }

    //视图数据查询来源
    public static final class VIEW_EDIT_FROM {

        // 仪表板
        public static final String PANEL = "panel";

        // 仪表板编辑
        public static final String CACHE = "cache";

    }

    //视图数据读取来源
    public static final class VIEW_DATA_FROM {

        // 模板数据
        public static final String TEMPLATE = "template";

        //数据集数据
        public static final String CHART = "dataset";

    }
}
