package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.dataease.api.visualization.VisualizationStoreApi;
import io.dataease.api.visualization.request.VisualizationStoreRequest;
import io.dataease.api.visualization.request.VisualizationWorkbranchQueryRequest;
import io.dataease.api.visualization.vo.VisualizationResourceVO;
import io.dataease.api.visualization.vo.VisualizationStoreVO;
import io.dataease.i18n.Translator;
import io.dataease.visualization.manage.VisualizationStoreManage;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/store")
@RestController
public class VisualizationStoreServer implements VisualizationStoreApi {

    @Resource
    private VisualizationStoreManage visualizationStoreManage;

    @Override
    public void execute(VisualizationStoreRequest request) {
        visualizationStoreManage.execute(request);
    }

    @Override
    public List<VisualizationStoreVO> query(VisualizationWorkbranchQueryRequest request) {
        IPage<VisualizationStoreVO> iPage = visualizationStoreManage.query(1, 20, request);
        List<VisualizationStoreVO> resourceVOS = iPage.getRecords();
        if (!CollectionUtils.isEmpty(resourceVOS)) {
            resourceVOS.forEach(item -> {
                item.setCreator(StringUtils.equals(item.getCreator(), "1") ? Translator.get("i18n_sys_admin") : item.getCreator());
                item.setLastEditor(StringUtils.equals(item.getLastEditor(), "1") ? Translator.get("i18n_sys_admin") : item.getCreator());
            });
        }
        return iPage.getRecords();
    }

    @Override
    public boolean favorited(Long id) {
        return visualizationStoreManage.favorited(id);
    }
}
