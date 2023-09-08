package io.dataease.plugins.common.util;


import cn.hutool.core.util.ReflectUtil;
import io.dataease.plugins.common.constants.datasource.SQLConstants;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.io.ResolverUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConstantsUtil {

    public static final String TYPE_KEY_FIELD = "NAME";

    private static final String constsPackageName = "io.dataease.plugins.common.constants";

    private static final List<Class> SQLConstantsCache = new ArrayList<>();

    /*public static void add(Class classz) {
        SQLConstantsCache.add(classz);
    }*/

    public static List<Class> getAllSQLConstants() {
        if (CollectionUtils.isEmpty(SQLConstantsCache)){
            Set<Class<? extends Class<?>>> classes = scanConstants(constsPackageName, SQLConstants.class);
            SQLConstantsCache.addAll(classes);
        }
        return SQLConstantsCache;
    }

    public static String constantsValue(String dsType, String constantKey) {
        String[] mysqlTreaties = {"mariadb", "ds_doris", "TiDB", "StarRocks"};
        if (Stream.of(mysqlTreaties).collect(Collectors.toList()).contains(dsType)) {
            dsType = "mysql";
        }
        List<Class> allSQLConstantsClass = ConstantsUtil.getAllSQLConstants();
        Object result;

        for (int i = 0; i < allSQLConstantsClass.size(); i++) {
            Class classz = allSQLConstantsClass.get(i);
            Object fieldValue = ReflectUtil.getFieldValue(classz, ConstantsUtil.TYPE_KEY_FIELD);
            if (ObjectUtils.isNotEmpty(fieldValue) && StringUtils.equals(dsType, fieldValue.toString())) {
                result = ReflectUtil.getFieldValue(classz, constantKey);
                return ObjectUtils.isNotEmpty(result) ? result.toString() : null;
            }
        }
        return null;
    }

    private static Set<Class<? extends Class<?>>> scanConstants(String packageName, Class<?> superType) {
        ResolverUtil<Class<?>> resolverUtil = new ResolverUtil<>();
        resolverUtil.find(new ResolverUtil.IsA(superType), packageName);
        Set<Class<? extends Class<?>>> classesSet = resolverUtil.getClasses();
        return classesSet;
    }
}
