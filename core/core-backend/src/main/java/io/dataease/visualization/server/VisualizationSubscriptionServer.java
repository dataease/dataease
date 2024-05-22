package io.dataease.visualization.server;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.dataease.api.visualization.VisualizationSubscriptionApi;
import io.dataease.api.visualization.vo.DataVisualizationVO;
import io.dataease.chart.dao.auto.entity.CoreChartView;
import io.dataease.chart.dao.auto.mapper.CoreChartViewMapper;
import io.dataease.exception.DEException;
import io.dataease.utils.AuthUtils;
import io.dataease.utils.IDUtils;
import io.dataease.visualization.dao.auto.entity.DataVisualizationInfo;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoMapper;
import io.dataease.visualization.manage.CoreVisualizationManage;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RequestMapping("/subscription")
@RestController
public class VisualizationSubscriptionServer implements VisualizationSubscriptionApi {

    @Resource
    private DataVisualizationServer dataVisualizationServer;
    @Resource
    private DataVisualizationInfoMapper visualizationInfoMapper;
    @Resource
    private CoreChartViewMapper coreChartViewMapper;
    @Resource
    private CoreVisualizationManage coreVisualizationManage;

    @Override
    public void subscription(Long dvId, Long resourceId) {
        // 管理员首页大屏
        DataVisualizationVO adminDataV = dataVisualizationServer.findById(dvId, "dataV");
        QueryWrapper<DataVisualizationInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("create_by", AuthUtils.getUser().getUserId());
        wrapper.eq("name", "用户首页大屏");
        wrapper.eq("delete_flag", false);
        DataVisualizationInfo userDataV = visualizationInfoMapper.selectList(wrapper)
                .stream().findFirst().orElse(null);
        if (ObjectUtils.isEmpty(userDataV)) DEException.throwException("用户首页大屏不存在或已经被删除...");

        String adminComponentDataJson = adminDataV.getComponentData();

        try {
            Map<Long, JSONObject> componentDataMap = new HashMap<>();
            if (StringUtils.isNotBlank(adminComponentDataJson)) {
                JSONArray adminJsonArray = new JSONArray(adminComponentDataJson);
                for (int i = 0; i < adminJsonArray.length(); i++) {
                    JSONObject adminJsonObject = adminJsonArray.getJSONObject(i);
                    componentDataMap.put(adminJsonObject.getLong("id"), adminJsonObject);
                }
            }

            JSONObject targetJsonObject = componentDataMap.get(resourceId);

            String component = targetJsonObject.getString("component");
            log.info("component: {}", component);

            JSONArray userJsonArray = new JSONArray(userDataV.getComponentData());

            // 判断 component
            if ("UserView".equalsIgnoreCase(component)) {
                // 图表
                // 新增chart关联用户首页大屏
                addChartForSubscription(targetJsonObject, userDataV);
            } else if ("Group".equalsIgnoreCase(component)) {
                // 组合
                JSONArray propValueJsonArray = targetJsonObject.getJSONArray("propValue");
                for (int i = 0; i < propValueJsonArray.length(); i++) {
                    JSONObject targetPropValueJSONObject = propValueJsonArray.getJSONObject(i);
                    addChartForSubscription(targetPropValueJSONObject, userDataV);
                    propValueJsonArray.put(i, targetPropValueJSONObject);
                }
            } else if ("DeTabs".equalsIgnoreCase(component)) {
                log.info("DeTabs...");
            }

            calcPosition(targetJsonObject, userDataV.getCanvasStyleData(), userJsonArray);

            userJsonArray.put(targetJsonObject);

            // 修改用户大屏
            userDataV.setComponentData(userJsonArray.toString());
            coreVisualizationManage.innerEdit(userDataV);
        } catch (Exception e) {
            log.error("内部错误: {}", e.getMessage());
            DEException.throwException(e);
        }
    }

    /**
     * 计算新增订阅图表位置
     * @param targetJsonObject target
     * @param userCanvasStyleData userCanvasStyle
     * @param userJsonArray userComponentDataArray
     */
    private void calcPosition(JSONObject targetJsonObject, String userCanvasStyleData, JSONArray userJsonArray) {
        try {
            if (StringUtils.isNotBlank(userCanvasStyleData)) {
                List<JSONObject> jsonObjectList = new ArrayList<>();
                for (int i = 0; i < userJsonArray.length(); i++) {
                    jsonObjectList.add(userJsonArray.getJSONObject(i));
                }

                int left = 0;
                int top = 0;

                JSONObject targetStyle = targetJsonObject.getJSONObject("style");
                int targetStyleWidth = targetStyle.getInt("width");

                if (CollectionUtils.isNotEmpty(jsonObjectList)) {
                    // 根据用户图表位置对图表排序，找到坐标加高度最下面的图表
                    jsonObjectList.sort(Comparator.comparingInt((JSONObject obj) -> {
                        try {
                            return -(obj.getJSONObject("style").getInt("top") + obj.getJSONObject("style").getInt("height"));
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }));
                    JSONObject theBottomChart = jsonObjectList.get(0);
                    JSONObject theBottomChartStyle = theBottomChart.getJSONObject("style");
                    int theBottomTop = theBottomChartStyle.getInt("top");
                    int theBottomLeft = theBottomChartStyle.getInt("left");
                    int theBottomWidth = theBottomChartStyle.getInt("width");
                    int theBottomHeight = theBottomChartStyle.getInt("height");

                    // 根据用户图表位置对图表排序，找到坐标最下面且最右边的图表
                    jsonObjectList.sort(Comparator.comparingInt((JSONObject obj) -> {
                                try {
                                    return -obj.getJSONObject("style").getInt("top");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .thenComparingInt((JSONObject obj) -> {
                                try {
                                    return -obj.getJSONObject("style").getInt("left");
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }));

                    JSONObject lastChart = jsonObjectList.get(0);
                    JSONObject lastChartStyle = lastChart.getJSONObject("style");
                    int lastTop = lastChartStyle.getInt("top");
                    int lastLeft = lastChartStyle.getInt("left");
                    int lastWidth = lastChartStyle.getInt("width");
                    int lastHeight = lastChartStyle.getInt("height");

                    JSONObject jsonObject = new JSONObject(userCanvasStyleData);
                    // 比例
                    int scale = jsonObject.getInt("scale");
                    int dvWidth = (jsonObject.getInt("width") * scale) / 100;

                    left = lastLeft + lastWidth; // 新图表的left在上一个图表的右侧
                    top = lastTop; // top与上一个图表相同
                    // 如果超出画布宽度，则换行放置
                    if ((left + targetStyleWidth) > dvWidth) {
                        left = 0;
                        top = lastTop + lastHeight;  // top在上一个图表下方
                        if (top < (theBottomTop + theBottomHeight)) {
                            left = theBottomLeft + theBottomWidth;
                        }
                    }
                }

                targetStyle.put("left", left);
                targetStyle.put("top", top);
                targetJsonObject.put("style", targetStyle);
            }
        } catch (JSONException e) {
            log.error("内部错误: {}", e.getMessage());
            DEException.throwException(e);
        }
    }

    /**
     * 为用户订阅新增图表
     * @param targetJsonObject target
     * @param userDataV userDataV
     */
    private void addChartForSubscription(JSONObject targetJsonObject, DataVisualizationInfo userDataV) {
        try {
            long id = targetJsonObject.getLong("id");
            CoreChartView coreChartView = coreChartViewMapper.selectById(id);
            if (null != coreChartView) {
                Long snowID = IDUtils.snowID();
                coreChartView.setId(snowID);
                coreChartView.setSceneId(userDataV.getId());
                coreChartViewMapper.insert(coreChartView);
                targetJsonObject.put("id", snowID.toString());
            }
        } catch (JSONException e) {
            log.error("内部错误: {}", e.getMessage());
            DEException.throwException(e);
        }
    }
}
