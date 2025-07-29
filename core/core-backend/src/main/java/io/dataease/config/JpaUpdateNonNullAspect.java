package io.dataease.config;

import io.dataease.utils.LogUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.BeanUtils;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JPA 默认的 save 或 saveAndFlush 方法会更新所有字段（包括为 null 的字段）
 * 不像 MyBatis-Plus 的 updateById 只更新非 null 字段
 * 这会导致一些问题，比如：对于一些不需要更新的字段，传 null 可能会导致数据丢失, 如createBy , createTime等字段
 * 所以添加此切面，在保存时只更新非 null 字段
 **/
@Aspect
@Component
public class JpaUpdateNonNullAspect {

    /**
     * 缓存属性描述符，提升性能
     */
    private static final Map<Class<?>, PropertyDescriptor[]> PD_CACHE = new ConcurrentHashMap<>();

    /**
     * 拦截 JpaRepository 的 save/saveAndFlush/saveAll/saveAllAndFlush 方法
     */
    @Around("execution(* org.springframework.data.jpa.repository.JpaRepository.save(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.saveAndFlush(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.saveAll(..)) || " +
            "execution(* org.springframework.data.jpa.repository.JpaRepository.saveAllAndFlush(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        Object[] args = pjp.getArgs();
        Object entityOrList = args[0];
        JpaRepository<?, ?> repository = (JpaRepository<?, ?>) pjp.getTarget();

        if (entityOrList instanceof Iterable) {
            List<Object> mergedList = new ArrayList<>();
            for (Object entity : (Iterable<?>) entityOrList) {
                mergedList.add(mergeEntityIfExists(entity, repository));
            }
            args[0] = mergedList;
        } else {
            args[0] = mergeEntityIfExists(entityOrList, repository);
        }
        return pjp.proceed(args);
    }

    /**
     * 合并实体的非空属性，如果实体已存在则更新，否则返回原实体
     */
    @SuppressWarnings("unchecked")
    private Object mergeEntityIfExists(Object entity, JpaRepository<?, ?> repository) {
        try {
            Object id = getEntityId(entity);
            if (id == null) return entity; // 新增，直接返回
            Optional<?> dbOpt = ((JpaRepository<Object, Object>) repository).findById(id);
            if (dbOpt.isPresent()) {
                Object dbEntity = dbOpt.get();
                copyNonNullProperties(entity, dbEntity);
                return dbEntity;
            }
        } catch (Exception e) {
            System.out.println("合并实体属性失败: " + e.getMessage());
        }
        return entity;
    }

    /**
     * 通过反射获取实体的主键值，支持主键名不为id
     */
    private Object getEntityId(Object entity) {
        Class<?> clazz = entity.getClass();
        // 先查找带@Id注解的字段
        for (java.lang.reflect.Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(jakarta.persistence.Id.class)) {
                field.setAccessible(true);
                try {
                    return field.get(entity);
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        // 再查找带@Id注解的方法
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(jakarta.persistence.Id.class) && method.getParameterCount() == 0) {
                try {
                    return method.invoke(entity);
                } catch (Exception ignored) {
                }
            }
        }
        // 都没有找到，返回null
        return null;
    }

    /**
     * 复制源对象的非空属性到目标对象
     * 缓存属性描述符，提升性能
     */
    private void copyNonNullProperties(Object src, Object target) {
        PropertyDescriptor[] pds = PD_CACHE.computeIfAbsent(src.getClass(),
                BeanUtils::getPropertyDescriptors);
        for (PropertyDescriptor pd : pds) {
            try {
                Method getter = pd.getReadMethod();
                Method setter = pd.getWriteMethod();
                if (getter == null || setter == null) continue;
                Object value = getter.invoke(src);
                if (value != null) {
                    Class<?> setterType = setter.getParameterTypes()[0];
                    if (!setterType.isAssignableFrom(value.getClass())) {
                        System.out.println("属性类型不一致: " + pd.getName() +
                                " getter返回: " + value.getClass().getName() +
                                " setter参数: " + setterType.getName());
                        continue;
                    }
                    setter.invoke(target, value);
                }
            } catch (Exception e) {
                LogUtil.error(e);
                throw new RuntimeException("copyNonNullProperties 复制属性失败: " + pd.getName(), e);
            }
        }
    }
}
