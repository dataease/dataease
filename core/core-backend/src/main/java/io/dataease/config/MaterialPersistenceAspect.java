package io.dataease.config;

import io.dataease.api.material.MaterialLifecycle;
import io.dataease.utils.MaterialReferences;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.entity.SnapshotDataVisualizationInfo;
import io.dataease.template.dao.auto.entity.VisualizationTemplate;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import io.dataease.visualization.dao.auto.mapper.SnapshotDataVisualizationInfoRepository;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

/** Reconciles the actual merged persisted entity, including bulk and internal repository paths. */
@Aspect
@Component
@org.springframework.core.annotation.Order(100)
public class MaterialPersistenceAspect {
    private final ObjectProvider<MaterialLifecycle> lifecycle;
    private final TransactionTemplate transactions;
    @PersistenceContext
    private EntityManager entityManager;

    public MaterialPersistenceAspect(ObjectProvider<MaterialLifecycle> lifecycle, PlatformTransactionManager manager) {
        this.lifecycle = lifecycle;
        this.transactions = new TransactionTemplate(manager);
    }

    @Around("execution(* org.springframework.data.repository.CrudRepository+.save*(..)) || "
            + "execution(* org.springframework.data.repository.CrudRepository+.delete*(..)) || "
            + "execution(* org.springframework.data.jpa.repository.JpaRepository+.save*(..)) || "
            + "execution(* org.springframework.data.jpa.repository.JpaRepository+.delete*(..))")
    public Object persist(ProceedingJoinPoint point) throws Throwable {
        Object repository = point.getThis();
        if (!(repository instanceof DataVisualizationInfoRepository)
                && !(repository instanceof SnapshotDataVisualizationInfoRepository)
                && !(repository instanceof VisualizationTemplateRepository)) return point.proceed();
        String method = point.getSignature().getName();
        boolean save = Set.of("save", "saveAndFlush", "saveAll", "saveAllAndFlush").contains(method);
        boolean delete = Set.of("delete", "deleteById", "deleteAll", "deleteAllById", "deleteAllInBatch",
                "deleteAllByIdInBatch", "deleteInBatch").contains(method);
        if (!save && !delete) return point.proceed();
        MaterialLifecycle service = lifecycle.getIfAvailable();
        if (service == null) {
            if (save) for (Object value : values(point.getArgs()[0])) {
                if (!ids(value).isEmpty()) throw new IllegalStateException("Material service unavailable");
            }
            return point.proceed();
        }
        try {
            return transactions.execute(status -> {
                service.lock();
                List<Object> deleted = delete ? deleteTargets(point, method) : List.of();
                Object result;
                try { result = point.proceed(); } catch (Throwable e) { throw new PersistenceFailure(e); }
                if (delete && method.endsWith("InBatch")) {
                    // Bulk DELETE does not evict the owners loaded above. A later save of the same
                    // ID must not merge into a managed entity whose database row was deleted.
                    for (Object value : deleted) {
                        if (entityManager.contains(value)) entityManager.detach(value);
                    }
                }
                if (save) {
                    for (Object value : values(result)) service.reconcile(type(value), id(value), ids(value));
                } else for (Object value : deleted) service.release(type(value), id(value));
                return result;
            });
        } catch (PersistenceFailure failure) {
            throw failure.getCause();
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<Object> deleteTargets(ProceedingJoinPoint point, String method) {
        JpaRepository repository = (JpaRepository) point.getThis();
        Object[] args = point.getArgs();
        if (args.length == 0) return repository.findAll();
        if (method.equals("delete")) return values(args[0]);
        if (method.equals("deleteById")) {
            Object target = repository.findById(args[0]).orElse(null);
            return target == null ? List.of() : List.of(target);
        }
        if (method.contains("ById")) return repository.findAllById((Iterable) args[0]);
        List<Object> targets = values(args[0]);
        if (method.endsWith("InBatch")) {
            // Entity arguments may be detached copies of owners already managed by this transaction.
            var unit = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();
            return repository.findAllById(targets.stream().filter(Objects::nonNull)
                    .map(unit::getIdentifier).filter(Objects::nonNull).toList());
        }
        return targets;
    }

    private List<Object> values(Object value) {
        List<Object> result = new ArrayList<>();
        if (value instanceof Iterable<?> values) values.forEach(result::add);
        else if (value != null) result.add(value);
        return result;
    }

    private Set<Long> ids(Object value) {
        if (value instanceof DataVisualizationInfo entity) return MaterialReferences.collect(entity.getCanvasStyleData(), entity.getComponentData());
        if (value instanceof SnapshotDataVisualizationInfo entity) return MaterialReferences.collect(entity.getCanvasStyleData(), entity.getComponentData());
        if (value instanceof VisualizationTemplate entity) return MaterialReferences.collect(entity.getTemplateStyle(), entity.getTemplateData());
        throw new IllegalArgumentException("Unsupported material owner");
    }

    private String type(Object value) {
        if (value instanceof DataVisualizationInfo) return "core";
        if (value instanceof SnapshotDataVisualizationInfo) return "snapshot";
        return "template";
    }

    private String id(Object value) {
        if (value instanceof DataVisualizationInfo entity) return entity.getId().toString();
        if (value instanceof SnapshotDataVisualizationInfo entity) return entity.getId().toString();
        return ((VisualizationTemplate) value).getId();
    }

    private static final class PersistenceFailure extends RuntimeException {
        private PersistenceFailure(Throwable cause) { super(cause); }
    }
}
