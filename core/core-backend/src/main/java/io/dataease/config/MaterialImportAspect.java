package io.dataease.config;

import io.dataease.api.template.request.TemplateManageRequest;
import io.dataease.api.visualization.request.DataVisualizationBaseRequest;
import io.dataease.exception.DEException;
import io.dataease.i18n.Translator;
import io.dataease.result.ResultCode;
import io.dataease.utils.MaterialDocuments;
import io.dataease.utils.MaterialReferences;
import io.dataease.visualization.manage.ImportedImageManage;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Map;
import java.util.Set;

@Aspect
@Component
@Order(0)
public class MaterialImportAspect {
    private final ImportedImageManage importedImages;
    private final TransactionTemplate transactions;

    public MaterialImportAspect(ImportedImageManage importedImages, PlatformTransactionManager manager) {
        this.importedImages = importedImages;
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
        String style = argument instanceof DataVisualizationBaseRequest request ? request.getCanvasStyleData()
                : ((TemplateManageRequest) argument).getTemplateStyle();
        String components = argument instanceof DataVisualizationBaseRequest request ? request.getComponentData()
                : ((TemplateManageRequest) argument).getTemplateData();
        String method = point.getSignature().getName();
        if (!method.equals("saveCanvas")) {
            importedImages.validatePreview(bundle, style, components);
        }
        if (method.equals("decompression")) {
            return point.proceed();
        }
        Set<String> images = importedImages.imageReferences(style, components);
        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];
        transactions.executeWithoutResult(status -> {
            // The preview can be edited before saving. Restore only the images still in use.
            Map<String, String> mapping = importedImages.restore(bundle, images);
            if (argument instanceof DataVisualizationBaseRequest request) {
                request.setCanvasStyleData(MaterialDocuments.remap(request.getCanvasStyleData(), mapping));
                request.setComponentData(MaterialDocuments.remap(request.getComponentData(), mapping));
                if (request.getCanvasViewInfo() != null) {
                    String views = MaterialDocuments.remap(io.dataease.utils.JsonUtil.toJSONString(request.getCanvasViewInfo()).toString(), mapping);
                    request.setCanvasViewInfo(io.dataease.utils.JsonUtil.parseObject(views,
                            new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<Long, io.dataease.extensions.view.dto.ChartViewDTO>>() { }));
                }
                request.setDynamicData(MaterialDocuments.remap(request.getDynamicData(), mapping));
                validateComplete(request.getCanvasStyleData(), request.getComponentData(), request.getDynamicData());
            } else {
                var request = (TemplateManageRequest) argument;
                request.setTemplateStyle(MaterialDocuments.remap(request.getTemplateStyle(), mapping));
                request.setTemplateData(MaterialDocuments.remap(request.getTemplateData(), mapping));
                request.setDynamicData(MaterialDocuments.remap(request.getDynamicData(), mapping));
                validateComplete(request.getTemplateStyle(), request.getTemplateData(), request.getDynamicData());
            }
            try { result[0] = point.proceed(); }
            catch (Throwable e) { status.setRollbackOnly(); error[0] = e; }
        });
        if (error[0] != null) throw error[0];
        return result[0];
    }

    private void validateComplete(String... documents) {
        // Any remaining portable reference is invalid; normal library selections are checked
        // by the same lifecycle authorization as ordinary resource saves.
        try {
            MaterialReferences.collect(documents);
        } catch (IllegalArgumentException e) {
            throw new DEException(ResultCode.PARAM_IS_INVALID.code(), Translator.get("i18n_static_resource_invalid"));
        }
    }
}
