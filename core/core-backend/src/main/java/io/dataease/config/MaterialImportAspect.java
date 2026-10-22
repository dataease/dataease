package io.dataease.config;

import io.dataease.api.material.MaterialBundleService;
import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.utils.MaterialDocuments;
import io.dataease.utils.MaterialReferences;
import io.dataease.exception.DEException;
import io.dataease.result.ResultCode;
import io.dataease.i18n.Translator;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.core.annotation.Order;
import java.util.*;

@Aspect
@Component
@Order(0)
public class MaterialImportAspect {
    private final ObjectProvider<MaterialBundleService> bundles;
    private final TransactionTemplate transactions;

    public MaterialImportAspect(ObjectProvider<MaterialBundleService> bundles, PlatformTransactionManager manager) {
        this.bundles = bundles;
        this.transactions = new TransactionTemplate(manager);
    }

    @Around("execution(* io.dataease.visualization.server.DataVisualizationServer.saveCanvas(..)) || "
            + "execution(* io.dataease.template.service.TemplateManageService.save(..)) || "
            + "execution(* io.dataease.visualization.server.DataVisualizationServer.decompression(..))")
    public Object restore(ProceedingJoinPoint point) throws Throwable {
        Object argument = point.getArgs()[0];
        var bundle = argument instanceof DataVisualizationBaseRequest request ? request.getMaterialBundle()
                : ((TemplateManageRequest) argument).getMaterialBundle();
        if (bundle == null) return point.proceed();
        var service = bundles.getIfAvailable();
        if (service == null) throw invalid();
        String style = argument instanceof DataVisualizationBaseRequest request ? request.getCanvasStyleData()
                : ((TemplateManageRequest) argument).getTemplateStyle();
        String components = argument instanceof DataVisualizationBaseRequest request ? request.getComponentData()
                : ((TemplateManageRequest) argument).getTemplateData();
        if (bundle.getFiles() == null) throw invalid();
        Set<String> provided = new HashSet<>();
        for (var file : bundle.getFiles()) {
            if (file == null || !provided.add(file.getReference())) throw invalid();
        }
        Set<String> images = MaterialDocuments.images(style, components);
        boolean finalCanvasSave = point.getSignature().getName().equals("saveCanvas");
        if ((!finalCanvasSave && !images.equals(provided)) || images.stream()
                .anyMatch(image -> image.startsWith("material-file://") && !provided.contains(image))) {
            throw invalid();
        }
        if (point.getSignature().getName().equals("decompression")) {
            service.validate(bundle);
            return point.proceed();
        }
        var selectedBundle = new io.dataease.api.material.vo.MaterialBundle();
        selectedBundle.setVersion(bundle.getVersion());
        if (finalCanvasSave) {
            // Validate the entire source package, then restore only images still used after
            // editing the import preview. Unused entries cannot become independent uploads.
            service.validate(bundle);
            selectedBundle.setFiles(bundle.getFiles().stream().filter(file -> images.contains(file.getReference())).toList());
        } else selectedBundle.setFiles(bundle.getFiles());
        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];
        transactions.executeWithoutResult(status -> {
            Map<String, String> mapping = service.restore(selectedBundle);
            if (argument instanceof DataVisualizationBaseRequest request) {
                request.setCanvasStyleData(MaterialDocuments.remap(request.getCanvasStyleData(), mapping));
                request.setComponentData(MaterialDocuments.remap(request.getComponentData(), mapping));
                if (request.getCanvasViewInfo() != null) {
                    String views = MaterialDocuments.remap(io.dataease.utils.JsonUtil.toJSONString(request.getCanvasViewInfo()).toString(), mapping);
                    request.setCanvasViewInfo(io.dataease.utils.JsonUtil.parseObject(views,
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<Long, io.dataease.extensions.view.dto.ChartViewDTO>>() { }));
                }
                request.setDynamicData(MaterialDocuments.remap(request.getDynamicData(), mapping));
                validateComplete(request.getCanvasStyleData(), request.getComponentData(), mapping);
            } else {
                var request = (TemplateManageRequest) argument;
                request.setTemplateStyle(MaterialDocuments.remap(request.getTemplateStyle(), mapping));
                request.setTemplateData(MaterialDocuments.remap(request.getTemplateData(), mapping));
                request.setDynamicData(MaterialDocuments.remap(request.getDynamicData(), mapping));
                validateComplete(request.getTemplateStyle(), request.getTemplateData(), mapping);
            }
            try { result[0] = point.proceed(); }
            catch (Throwable e) { status.setRollbackOnly(); error[0] = e; }
        });
        if (error[0] != null) throw error[0];
        return result[0];
    }

    private void validateComplete(String style, String components, Map<String, String> mapping) {
        // Any remaining portable reference is invalid; normal library selections are checked
        // by the same lifecycle authorization as ordinary resource saves.
        try { MaterialReferences.collect(style, components); }
        catch (IllegalArgumentException e) { throw invalid(); }
    }

    private DEException invalid() {
        return new DEException(ResultCode.PARAM_IS_INVALID.code(), Translator.get("i18n_material_invalid"));
    }
}
