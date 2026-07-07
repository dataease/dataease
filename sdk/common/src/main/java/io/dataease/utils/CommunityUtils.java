package io.dataease.utils;

import com.querydsl.core.types.EntityPath;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPAExpressions;
import io.dataease.dao.auto.entity.QPerBusiResource;

public class CommunityUtils {

    private static final ThreadLocal<Boolean> COMMUNITY_FLAG = new ThreadLocal<>();

    public static void setCommunityMode(boolean communityMode) {
        if (communityMode) {
            COMMUNITY_FLAG.set(true);
        } else {
            COMMUNITY_FLAG.remove();
        }
    }

    public static boolean isCommunityMode() {
        return COMMUNITY_FLAG.get() != null;
    }

    public static void clear() {
        COMMUNITY_FLAG.remove();
    }

    /**
     * 构建 NOT EXISTS 子查询，排除 per_busi_resource 中已注册的资源
     */
    public static BooleanExpression buildNotExistsCondition(EntityPath<?> entityPath, int rtId) {
        QPerBusiResource qPerBusiResource = QPerBusiResource.perBusiResource;
        PathBuilder<?> pathBuilder = new PathBuilder<>(entityPath.getType(), entityPath.getMetadata());

        boolean idIsLong = false;
        try {
            if (entityPath.getType().getDeclaredField("id").getType().equals(Long.class)) {
                idIsLong = true;
            }
        } catch (NoSuchFieldException ignored) {
        }

        BooleanExpression idMatch = idIsLong
                ? qPerBusiResource.id.eq(pathBuilder.getNumber("id", Long.class))
                : qPerBusiResource.id.stringValue().eq(pathBuilder.getString("id"));

        return JPAExpressions.selectOne()
                .from(qPerBusiResource)
                .where(qPerBusiResource.rtId.eq(rtId), idMatch)
                .notExists();
    }
}
