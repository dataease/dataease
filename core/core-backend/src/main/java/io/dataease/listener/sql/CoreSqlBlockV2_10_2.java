package io.dataease.listener.sql;

import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dao.auto.entity.DataVisualizationInfo;
import io.dataease.initSql.Version;
import io.dataease.map.dao.auto.entity.Area;
import io.dataease.map.dao.auto.mapper.AreaRepository;
import io.dataease.menu.dao.auto.mapper.CoreMenuRepository;
import io.dataease.system.dao.auto.entity.CoreSysSetting;
import io.dataease.system.dao.auto.mapper.CoreSysSettingRepository;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class CoreSqlBlockV2_10_2 implements CoreSqlBlock {

    @Resource
    private CoreChartViewRepository coreChartViewRepository;
    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;
    @Resource
    private AreaRepository areaRepository;
    @Resource
    private CoreSysSettingRepository coreSysSettingRepository;

    @Override
    public Version getVersion() {
        return new Version("2.10.2");
    }

    @Override
    public void execute() {
        Area area = new Area();
        area.setId("156440315");
        area.setLevel("district");
        area.setName("大鹏新区");
        area.setPid("156440300");
        areaRepository.saveAndFlush(area);

        Specification<DataVisualizationInfo> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("deleteFlag")));
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        List<DataVisualizationInfo> dataVisualizationInfos = dataVisualizationInfoRepository.findAll(spec);
        if (!CollectionUtils.isEmpty(dataVisualizationInfos)) {
            Specification<CoreChartView> coreChartViewSpec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                predicates.add(cb.in(root.get("sceneId")).value(dataVisualizationInfos.stream().map(dataVisualizationInfo -> Long.valueOf(dataVisualizationInfo.getId())).toList()));
                return cb.and(predicates.toArray(new Predicate[0]));
            };
            coreChartViewRepository.delete(coreChartViewSpec);
        }
        dataVisualizationInfoRepository.deleteAll(dataVisualizationInfos);

        areaRepository.deleteByPidOrId("156710100");


        CoreSysSetting coreSysSetting1 = new CoreSysSetting();
        coreSysSetting1.setId(1048232869488627717L);
        coreSysSetting1.setPkey("basic.shareDisable");
        coreSysSetting1.setPval("false");
        coreSysSetting1.setType("text");
        coreSysSetting1.setSort(11);
        CoreSysSetting coreSysSetting2 = new CoreSysSetting();
        coreSysSetting2.setId(1048232869488627718L);
        coreSysSetting2.setPkey("basic.sharePeRequire");
        coreSysSetting2.setPval("false");
        coreSysSetting2.setType("text");
        coreSysSetting2.setSort(12);
        coreSysSettingRepository.saveAndFlush(coreSysSetting1);
        coreSysSettingRepository.saveAndFlush(coreSysSetting2);
    }

}
