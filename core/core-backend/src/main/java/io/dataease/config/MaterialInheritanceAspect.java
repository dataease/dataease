package io.dataease.config;

import io.dataease.api.material.*;
import io.dataease.api.material.dto.MaterialContext;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import java.util.Set;

/** Only trusted server-side copying of an authorized source can inherit pending materials. */
@Aspect
@Component
public class MaterialInheritanceAspect {
    private final ObjectProvider<MaterialLifecycle> lifecycle;
    private final MaterialResourceAccess access;

    public MaterialInheritanceAspect(ObjectProvider<MaterialLifecycle> lifecycle, MaterialResourceAccess access) {
        this.lifecycle = lifecycle;
        this.access = access;
    }

    @Around("execution(* io.dataease.visualization.server.DataVisualizationServer.copy(..)) || "
            + "execution(* io.dataease.visualization.server.DataVisualizationServer.saveCanvas(..))")
    public Object inherit(ProceedingJoinPoint point) throws Throwable {
        var service = lifecycle.getIfAvailable();
        if (service == null) return point.proceed();
        var request = (DataVisualizationBaseRequest) point.getArgs()[0];
        MaterialContext context = new MaterialContext();
        Set<Long> ids;
        if (point.getSignature().getName().equals("copy")) {
            context.setResourceId(request.getId());
            ids = access.authorize(context, true);
        } else if (request.getMaterialSourceTemplateId() != null) {
            context.setTemplateId(request.getMaterialSourceTemplateId());
            ids = access.authorize(context, false);
        } else if ("copy".equals(request.getOptType())) {
            context.setResourceId(request.getId());
            ids = access.authorize(context, true);
        } else return point.proceed();
        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];
        service.inherit(ids, () -> {
            try { result[0] = point.proceed(); } catch (Throwable e) { error[0] = e; }
        });
        if (error[0] != null) throw error[0];
        return result[0];
    }
}
