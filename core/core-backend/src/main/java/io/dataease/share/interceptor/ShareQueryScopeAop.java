package io.dataease.share.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dataease.api.dataset.dto.EnumValueRequest;
import io.dataease.api.dataset.dto.MultFieldValuesRequest;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.permission.util.V3UserUtil;
import io.dataease.result.ResultCode;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/** Enumeration endpoints must only use fields present in the shared canvas configuration. */
@Aspect
@Component
public class ShareQueryScopeAop {
    @Resource
    private DataVisualizationInfoRepository visualizations;

    @Around("execution(* io.dataease.dataset.server.DatasetDataServer.getFieldEnum(..)) || "
            + "execution(* io.dataease.dataset.server.DatasetDataServer.getFieldEnumObj(..)) || "
            + "execution(* io.dataease.dataset.server.DatasetDataServer.getFieldValueTree(..))")
    public Object checkQuery(ProceedingJoinPoint point) throws Throwable {
        if (V3UserUtil.getLink() != null) checkRequest(point.getArgs()[0]);
        return point.proceed();
    }

    public void checkRequest(Object argument) {
        if (V3UserUtil.getLink() == null) return;
        if (argument instanceof MultFieldValuesRequest request) {
            if (request.getUserId() != null && !Objects.equals(request.getUserId(), V3UserUtil.getUid())) {
                throw denied();
            }
            requireFields(request.getFieldIds());
        } else if (argument instanceof EnumValueRequest request) {
            List<Long> fields = new ArrayList<>(Arrays.asList(request.getQueryId(), request.getDisplayId(), request.getSortId()));
            if (request.getFilter() != null) {
                for (var filter : request.getFilter()) {
                    if (filter.getFieldId() != null && !filter.getFieldId().isBlank()) {
                        Arrays.stream(filter.getFieldId().split(",")).map(Long::valueOf).forEach(fields::add);
                    }
                }
            }
            requireFields(fields);
        } else {
            throw denied();
        }
    }

    public void requireFields(Collection<Long> fieldIds) {
        var identity = V3UserUtil.getLink();
        if (identity == null) return;
        try {
            String data = visualizations.queryComponentData(identity.resourceId());
            JsonNode components = new ObjectMapper().readTree(data);
            for (Long field : fieldIds) {
                if (field != null && !contains(components, field.toString())) {
                    throw denied();
                }
            }
        } catch (Exception e) {
            throw denied();
        }
    }

    private DEException denied() {
        return new DEException(ResultCode.PERMISSION_NO_ACCESS.code(),
                Translator.get("i18n_share_operation_denied"));
    }

    private boolean contains(JsonNode node, String id) {
        if (node == null) return false;
        if (node.isValueNode()) return id.equals(node.asText());
        for (JsonNode child : node) if (contains(child, id)) return true;
        return false;
    }
}
