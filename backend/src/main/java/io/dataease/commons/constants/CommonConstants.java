package io.dataease.commons.constants;

/**
 * Author: wangjiahao
 * Date: 2021-05-25
 * Description:
 */
public class CommonConstants {


    //操作类型
    public static final class OPT_TYPE{

        public static final String INSERT = "insert";

        public static final String UPDATE = "update";

        public static final String DELETE = "delete";

        public static final String SELECT = "select";

    }

    //操作类型
    public static final class CHECK_RESULT{

        // 不存在
        public static final String NONE = "none";

        // 全局存在
        public static final String EXIST_ALL= "exist_all";

        // 当前用户存在
        public static final String EXIST_USER= "exist_user";

        // 其他用户存在
        public static final String EXIST_OTHER= "exist_other";

    }
}
