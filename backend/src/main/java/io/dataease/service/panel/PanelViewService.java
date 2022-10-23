package io.dataease.service.panel;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.dataease.ext.ExtChartViewMapper;
import io.dataease.ext.ExtPanelGroupMapper;
import io.dataease.ext.ExtPanelViewMapper;
import io.dataease.commons.utils.AuthUtils;
import io.dataease.commons.utils.BeanUtils;
import io.dataease.dto.panel.PanelViewDto;
import io.dataease.dto.panel.PanelViewTableDTO;
import io.dataease.dto.panel.po.PanelViewInsertDTO;
import io.dataease.dto.panel.po.PanelViewPo;
import io.dataease.plugins.common.base.domain.ChartViewWithBLOBs;
import io.dataease.plugins.common.base.domain.PanelGroupWithBLOBs;
import io.dataease.plugins.common.base.domain.PanelView;
import io.dataease.plugins.common.base.domain.PanelViewExample;
import io.dataease.plugins.common.base.mapper.PanelViewMapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.pentaho.di.core.util.UUIDUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PanelViewService {

    @Autowired(required = false)
    private ExtPanelViewMapper extPanelViewMapper;

    @Resource
    private PanelViewMapper panelViewMapper;

    @Resource
    private ExtChartViewMapper extChartViewMapper;

    private final static String SCENE_TYPE = "scene";

    public List<PanelViewDto> groups() {
        return extPanelViewMapper.groups(String.valueOf(AuthUtils.getUser().getUserId()));
    }

    public List<PanelViewDto> views() {
        return extPanelViewMapper.views(String.valueOf(AuthUtils.getUser().getUserId()));
    }

    public List<PanelViewDto> buildTree(List<PanelViewPo> groups, List<PanelViewPo> views) {
        if (CollectionUtils.isEmpty(groups) || CollectionUtils.isEmpty(views)) return null;
        Map<String, List<PanelViewPo>> viewsMap = views.stream().collect(Collectors.groupingBy(PanelViewPo::getPid));
        List<PanelViewDto> dtos = groups.stream().map(group -> BeanUtils.copyBean(new PanelViewDto(), group)).collect(Collectors.toList());
        List<PanelViewDto> roots = new ArrayList<>();
        dtos.forEach(group -> {
            // 查找跟节点
            if (ObjectUtils.isEmpty(group.getPid())) {
                roots.add(group);
            }
            // 查找当前节点的子节点
            // 当前group是场景
            if (StringUtils.equals(group.getType(), SCENE_TYPE)) {
                Optional.ofNullable(viewsMap.get(group.getId())).ifPresent(lists -> lists.forEach(view -> {
                    PanelViewDto dto = BeanUtils.copyBean(new PanelViewDto(), view);
                    group.addChild(dto);
                }));
                return;
            }
            // 当前group是分组
            dtos.forEach(item -> {
                if (StringUtils.equals(item.getPid(), group.getId())) {
                    group.addChild(item);
                }
            });
        });
        // 最后 没有孩子的老东西淘汰
        return roots.stream().filter(item -> CollectionUtils.isNotEmpty(item.getChildren())).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<String> syncPanelViews(PanelGroupWithBLOBs panelGroup) {
        List<String> viewIds = new ArrayList<>();
        Boolean mobileLayout = null;
        String panelId = panelGroup.getId();
        Assert.notNull(panelId, "panelId cannot be null");
        String panelData = panelGroup.getPanelData();
        if (StringUtils.isNotEmpty(panelData)) {
            mobileLayout = false;
            JsonArray dataArray = JsonParser.parseString(panelData).getAsJsonArray();

            List<PanelViewInsertDTO> panelViewInsertDTOList = new ArrayList<>();
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject jsonObject = dataArray.get(i).getAsJsonObject();
                if (jsonObject.get("type")!=null && "view".equals(jsonObject.get("type").getAsString())) {
                    panelViewInsertDTOList.add(new PanelViewInsertDTO(jsonObject.get("propValue").getAsJsonObject().get("viewId").getAsString(), panelId));
                }
                // 选项卡内部视图
                if (jsonObject.get("type")!=null && "de-tabs".equals(jsonObject.get("type").getAsString())) {
                    JsonObject options = jsonObject.getAsJsonObject("options");
                    if (options != null) {
                        JsonArray tabList = options.getAsJsonArray("tabList");
                        if (tabList != null && tabList.size() > 0) {
                            for (int y = 0; y < tabList.size(); y++) {
                                if (tabList.get(y).getAsJsonObject().get("content").toString().indexOf("viewId") > -1) {
                                    panelViewInsertDTOList.add(new PanelViewInsertDTO(tabList.get(y).getAsJsonObject().getAsJsonObject("content").getAsJsonObject("propValue").get("viewId").getAsString(), panelId, "tab"));
                                }
                            }
                        }
                    }
                }
                if (jsonObject.get("mobileSelected") != null && jsonObject.get("mobileSelected").getAsBoolean()) {
                    mobileLayout = true;
                }
            }
            extPanelViewMapper.deleteWithPanelId(panelId);
            if (CollectionUtils.isNotEmpty(panelViewInsertDTOList)) {
                extPanelViewMapper.savePanelView(panelViewInsertDTOList);
                //将视图从cache表中更新到正式表中
                viewIds = panelViewInsertDTOList.stream().map(panelView -> panelView.getChartViewId()).collect(Collectors.toList());
            }
            extChartViewMapper.deleteCacheWithPanel(viewIds, panelId);
            extChartViewMapper.deleteNoUseView(viewIds, panelId);
        }
        panelGroup.setMobileLayout(mobileLayout);
        return viewIds;
    }

    public Boolean haveMobileLayout(String panelData){
        Boolean mobileLayout = false;
        if (StringUtils.isNotEmpty(panelData)) {
            JsonArray dataArray = JsonParser.parseString(panelData).getAsJsonArray();
            for (int i = 0; i < dataArray.size(); i++) {
                JsonObject jsonObject = dataArray.get(i).getAsJsonObject();
                if (jsonObject.get("mobileSelected") != null && jsonObject.get("mobileSelected").getAsBoolean()) {
                    mobileLayout = true;
                }
            }
        }

        return mobileLayout;
    }

    public List<PanelViewTableDTO> detailList(String panelId) {
        return extPanelViewMapper.getPanelViewDetails(panelId);
    }

    public List<PanelView> findPanelViews(String copyId) {
        PanelViewExample panelViewExample = new PanelViewExample();
        panelViewExample.createCriteria().andCopyIdEqualTo(copyId);
        return panelViewMapper.selectByExample(panelViewExample);
    }

    public PanelView findByViewId(String viewId) {
        PanelViewExample panelViewExample = new PanelViewExample();
        panelViewExample.createCriteria().andChartViewIdEqualTo(viewId);
        List<PanelView> result = panelViewMapper.selectByExample(panelViewExample);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.get(0);
        } else {
            return null;
        }
    }

    public List<ChartViewWithBLOBs> findByPanelId(String panelId) {
        return extChartViewMapper.findByPanelId(panelId);
    }

    public List<PanelView>  findPanelViewsByPanelId(String panelId){
        PanelViewExample example = new PanelViewExample();
        example.createCriteria().andPanelIdEqualTo(panelId);
        return panelViewMapper.selectByExample(example);
    }

    public void save(PanelView panelView){
        panelViewMapper.insertSelective(panelView);
    }
}
