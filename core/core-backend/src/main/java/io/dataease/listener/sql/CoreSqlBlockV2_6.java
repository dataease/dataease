package io.dataease.listener.sql;

import io.dataease.chart.dao.auto.mapper.CoreChartViewRepository;
import io.dataease.dao.auto.entity.CoreChartView;
import io.dataease.dao.auto.entity.CoreDatasetGroup;
import io.dataease.dao.auto.entity.CoreDatasetTable;
import io.dataease.dao.auto.entity.CoreDatasetTableField;
import io.dataease.dao.auto.repo.CoreDatasetTableFieldRepository;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetGroupRepository;
import io.dataease.dataset.dao.auto.mapper.CoreDatasetTableRepository;
import io.dataease.initSql.Version;
import io.dataease.listener.demo.DemoTeaMaterial;
import io.dataease.listener.demo.DemoTeaMaterialRepository;
import io.dataease.listener.demo.DemoTeaOrder;
import io.dataease.listener.demo.DemoTeaOrderRepository;
import io.dataease.map.dao.auto.mapper.AreaRepository;
import io.dataease.template.dao.auto.mapper.VisualizationTemplateRepository;
import io.dataease.visualization.dao.auto.mapper.DataVisualizationInfoRepository;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class CoreSqlBlockV2_6 implements CoreSqlBlock {

    @Resource
    private DataVisualizationInfoRepository dataVisualizationInfoRepository;
    @Resource
    private VisualizationTemplateRepository visualizationTemplateRepository;
    @Resource
    private CoreDatasetTableRepository coreDatasetTableRepository;
    @Resource
    private AreaRepository areaRepository;
    @Resource
    private CoreChartViewRepository coreChartViewRepository;
    @Resource
    private CoreDatasetTableFieldRepository coreDatasetTableFieldRepository;
    @Resource
    private CoreDatasetGroupRepository coreDatasetGroupRepository;
    @Resource
    private DemoTeaMaterialRepository demoTeaMaterialRepository;
    @Resource
    private DemoTeaOrderRepository demoTeaOrderRepository;

    @Override
    public Version getVersion() {
        return new Version("2.6");
    }

    @Override
    public void execute() {

        dataVisualizationInfoRepository.updateVersion();
        visualizationTemplateRepository.updateVersion();
        areaRepository.updateArea("156330103", "156330113", "临平区");
        areaRepository.updateArea("156330104", "156330114", "钱塘区");

        List<CoreChartView> pivotViews = coreChartViewRepository.findAllTablePivotViews();
        for (CoreChartView view : pivotViews) {
            String temp = view.getXAxis();
            view.setXAxis(view.getXAxisExt());
            view.setXAxisExt(temp);
        }
        coreChartViewRepository.saveAllAndFlush(pivotViews);

        CoreDatasetTable teaMaterial = new CoreDatasetTable(
                7193457660727922688L,
                null,
                "demo_tea_material",
                985188400292302848L,
                985189703189925888L,
                "db",
                "{\"table\":\"demo_tea_material\",\"sql\":\"\"}",
                null
        );

        CoreDatasetTable teaOrder = new CoreDatasetTable(
                7193537020143079424L,
                null,
                "demo_tea_order",
                985188400292302848L,
                985189053949415424L,
                "db",
                "{\"table\":\"demo_tea_order\",\"sql\":\"\"}",
                null
        );
        coreDatasetTableRepository.saveAllAndFlush(Arrays.asList(teaMaterial, teaOrder));


        List<CoreDatasetTableField> fields = new ArrayList<>();

        // First dataset fields (demo_tea_material)
        fields.add(new CoreDatasetTableField(
                1715053944934L, 985188400292302848L, 7193457660727922688L, 985189703189925888L,
                null, "shop", "shop", null, "f_4a4cd188441bb10a", "f_4a4cd188441bb10a",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715053944935L, 985188400292302848L, 7193457660727922688L, 985189703189925888L,
                null, "date", "date", null, "f_7fedb6b454fd0ddb", "f_7fedb6b454fd0ddb",
                "d", "DATETIME", null, 1, 1, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715053944936L, 985188400292302848L, 7193457660727922688L, 985189703189925888L,
                null, "purpose", "purpose", null, "f_703aac67af8ea53d", "f_703aac67af8ea53d",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715053944937L, 985188400292302848L, 7193457660727922688L, 985189703189925888L,
                null, "amount", "amount", null, "f_8cc276e515d2de6d", "f_8cc276e515d2de6d",
                "q", "BIGINT", null, 2, 2, 0, true, null, null, 0, null, null
        ));

        // Second dataset fields (demo_tea_order)
        fields.add(new CoreDatasetTableField(
                1715072798360L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "temperature", "temperature", null, "f_68bd7361c951941a", "f_68bd7361c951941a",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798361L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "price", "price", null, "f_878cf3320c82724f", "f_878cf3320c82724f",
                "q", "BIGINT", null, 2, 2, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798362L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "product", "product", null, "f_f8fc4f728f1e6fa2", "f_f8fc4f728f1e6fa2",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798363L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "shop", "shop", null, "f_4a4cd188441bb10a", "f_4a4cd188441bb10a",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798364L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "dish", "dish", null, "f_7c7894e776e3b8ec", "f_7c7894e776e3b8ec",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798365L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "specifications", "specifications", null, "f_5c1a43f6150f3a56", "f_5c1a43f6150f3a56",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798366L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "serial", "serial", null, "f_252845fa1a250405", "f_252845fa1a250405",
                "d", "LONGTEXT", null, 0, 0, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798367L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "sales", "sales", null, "f_59fcc2c2b0f47cde", "f_59fcc2c2b0f47cde",
                "q", "BIGINT", null, 2, 2, 0, true, null, null, 0, null, null
        ));

        fields.add(new CoreDatasetTableField(
                1715072798368L, 985188400292302848L, 7193537020143079424L, 985189053949415424L,
                null, "sale_date", "sale_date", null, "f_852cde987322fd1d", "f_852cde987322fd1d",
                "d", "DATETIME", null, 1, 1, 0, true, null, null, 0, null, null
        ));

        // Calculated fields
        fields.add(new CoreDatasetTableField(
                7193537137675866112L, null, null, 985189053949415424L,
                null, "[1715072798361]*[1715072798367]", "销售金额", null, "f_ebd405e534ce8c6c", "f_ebd405e534ce8c6c",
                "q", "VARCHAR", null, 3, 3, 2, true, null, null, 0, "", ""
        ));

        fields.add(new CoreDatasetTableField(
                7193537244429291520L, null, null, 985189053949415424L,
                null, "round(sum([7193537137675866112])/count([1715072798366])/100,2)", "客price", null,
                "f_39fd4542efb6a572", "f_39fd4542efb6a572", "q", "VARCHAR", null, 3, 3, 2, true, null, null, 0, "", ""
        ));

        fields.add(new CoreDatasetTableField(
                7193537490169368576L, null, null, 985189053949415424L,
                null, "round(sum([7193537137675866112])/sum([1715072798367]),2)", "杯均价", null,
                "f_47f238401ac173f1", "f_47f238401ac173f1", "q", "VARCHAR", null, 3, 3, 2, true, null, null, 0, "", ""
        ));

        coreDatasetTableFieldRepository.saveAllAndFlush(fields);

        initCoreDatasetGroup();

        initDemoTeaMaterial();
        initDemoTeaOrder();
    }

    private void initCoreDatasetGroup() {
        CoreDatasetGroup datasetGroup1 = new CoreDatasetGroup(
                985189703189925888L,
                "茶饮原料费用",
                985189269226262528L,
                0,
                "dataset",
                null,
                0,
                "[{\"currentDs\":{\"id\":\"7193457660727922688\",\"name\":null,\"tableName\":\"demo_tea_material\",\"datasourceId\":\"985188400292302848\",\"datasetGroupId\":null,\"type\":\"db\",\"info\":\"{\\\"table\\\":\\\"demo_tea_material\\\",\\\"sql\\\":\\\"\\\"}\",\"sqlVariableDetails\":null,\"fields\":null,\"lastUpdateTime\":0,\"status\":null},\"currentDsField\":null,\"currentDsFields\":[{\"id\":\"1715053944934\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193457660727922688\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"shop\",\"name\":\"shop\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_4a4cd188441bb10a\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_4a4cd188441bb10a\",\"desensitized\":null},{\"id\":\"1715053944935\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193457660727922688\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"date\",\"name\":\"date\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_7fedb6b454fd0ddb\",\"groupType\":\"d\",\"type\":\"DATETIME\",\"precision\":null,\"scale\":null,\"deType\":1,\"deExtractType\":1,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_7fedb6b454fd0ddb\",\"desensitized\":null},{\"id\":\"1715053944936\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193457660727922688\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"purpose\",\"name\":\"purpose\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_703aac67af8ea53d\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_703aac67af8ea53d\",\"desensitized\":null},{\"id\":\"1715053944937\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193457660727922688\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"amount\",\"name\":\"amount\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_8cc276e515d2de6d\",\"groupType\":\"q\",\"type\":\"BIGINT\",\"precision\":null,\"scale\":null,\"deType\":2,\"deExtractType\":2,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_8cc276e515d2de6d\",\"desensitized\":null}],\"childrenDs\":[],\"unionToParent\":{\"unionType\":\"left\",\"unionFields\":[],\"parentDs\":null,\"currentDs\":null,\"parentSQLObj\":null,\"currentSQLObj\":null},\"allChildCount\":0}]",
                "1",
                1715053994811L,
                null,
                null,
                "1",
                1715054022426L,
                "SELECT t_a_0.`shop` AS `f_4a4cd188441bb10a`,t_a_0.`date` AS `f_7fedb6b454fd0ddb`,t_a_0.`purpose` AS `f_703aac67af8ea53d`,t_a_0.`amount` AS `f_8cc276e515d2de6d` FROM s_a_985188400292302848.`demo_tea_material` t_a_0"
        );

        CoreDatasetGroup datasetGroup2 = new CoreDatasetGroup(
                985189053949415424L,
                "茶饮订单明细",
                985189269226262528L,
                0,
                "dataset",
                null,
                0,
                "[{\"currentDs\":{\"id\":\"7193537020143079424\",\"name\":null,\"tableName\":\"demo_tea_order\",\"datasourceId\":\"985188400292302848\",\"datasetGroupId\":null,\"type\":\"db\",\"info\":\"{\\\"table\\\":\\\"demo_tea_order\\\",\\\"sql\\\":\\\"\\\"}\",\"sqlVariableDetails\":null,\"fields\":null,\"lastUpdateTime\":0,\"status\":null},\"currentDsField\":null,\"currentDsFields\":[{\"id\":\"1715072798360\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"temperature\",\"name\":\"temperature\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_68bd7361c951941a\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_68bd7361c951941a\",\"desensitized\":null},{\"id\":\"1715072798361\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"price\",\"name\":\"price\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_878cf3320c82724f\",\"groupType\":\"q\",\"type\":\"BIGINT\",\"precision\":null,\"scale\":null,\"deType\":2,\"deExtractType\":2,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_878cf3320c82724f\",\"desensitized\":null},{\"id\":\"1715072798362\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"product\",\"name\":\"product\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_f8fc4f728f1e6fa2\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_f8fc4f728f1e6fa2\",\"desensitized\":null},{\"id\":\"1715072798363\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"shop\",\"name\":\"shop\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_4a4cd188441bb10a\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_4a4cd188441bb10a\",\"desensitized\":null},{\"id\":\"1715072798364\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"dish\",\"name\":\"dish\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_7c7894e776e3b8ec\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_7c7894e776e3b8ec\",\"desensitized\":null},{\"id\":\"1715072798365\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"specifications\",\"name\":\"specifications\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_5c1a43f6150f3a56\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_5c1a43f6150f3a56\",\"desensitized\":null},{\"id\":\"1715072798366\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"serial\",\"name\":\"serial\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_252845fa1a250405\",\"groupType\":\"d\",\"type\":\"LONGTEXT\",\"precision\":null,\"scale\":null,\"deType\":0,\"deExtractType\":0,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_252845fa1a250405\",\"desensitized\":null},{\"id\":\"1715072798367\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"sales\",\"name\":\"sales\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_59fcc2c2b0f47cde\",\"groupType\":\"q\",\"type\":\"BIGINT\",\"precision\":null,\"scale\":null,\"deType\":2,\"deExtractType\":2,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_59fcc2c2b0f47cde\",\"desensitized\":null},{\"id\":\"1715072798368\",\"datasourceId\":\"985188400292302848\",\"datasetTableId\":\"7193537020143079424\",\"datasetGroupId\":null,\"chartId\":null,\"originName\":\"sale_date\",\"name\":\"sale_date\",\"dbFieldName\":null,\"description\":null,\"dataeaseName\":\"f_852cde987322fd1d\",\"groupType\":\"d\",\"type\":\"DATETIME\",\"precision\":null,\"scale\":null,\"deType\":1,\"deExtractType\":1,\"extField\":0,\"checked\":true,\"columnIndex\":null,\"lastSyncTime\":null,\"dateFormat\":null,\"dateFormatType\":null,\"fieldShortName\":\"f_852cde987322fd1d\",\"desensitized\":null}],\"childrenDs\":[],\"unionToParent\":{\"unionType\":\"left\",\"unionFields\":[],\"parentDs\":null,\"currentDs\":null,\"parentSQLObj\":null,\"currentSQLObj\":null},\"allChildCount\":0}]",
                "1",
                1715053994811L,
                null,
                null,
                "1",
                1715054022426L,
                "SELECT t_a_0.`temperature` AS `f_68bd7361c951941a`,t_a_0.`price` AS `f_878cf3320c82724f`,t_a_0.`product` AS `f_f8fc4f728f1e6fa2`,t_a_0.`shop` AS `f_4a4cd188441bb10a`,t_a_0.`dish` AS `f_7c7894e776e3b8ec`,t_a_0.`specifications` AS `f_5c1a43f6150f3a56`,t_a_0.`serial` AS `f_252845fa1a250405`,t_a_0.`sales` AS `f_59fcc2c2b0f47cde`,t_a_0.`sale_date` AS `f_852cde987322fd1d` FROM s_a_985188400292302848.`demo_tea_order` t_a_0"
        );

        CoreDatasetGroup datasetGroup3 = new CoreDatasetGroup(
                985189269226262528L,
                "【官方示例】",
                0L,
                0,
                "folder",
                null,
                0,
                null,
                "1",
                1715053891346L,
                null,
                null,
                "1",
                1715067736873L,
                null);
        coreDatasetGroupRepository.saveAllAndFlush(Arrays.asList(datasetGroup1, datasetGroup2, datasetGroup3));
    }

    private void initDemoTeaMaterial() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<DemoTeaMaterial> materials = Arrays.asList(
                    new DemoTeaMaterial(1, sdf.parse("2024-03-10 17:00:18"), "欢果店", "原料购进", 162L),
                    new DemoTeaMaterial(2, sdf.parse("2024-03-25 01:07:42"), "蓝墨店", "原料购进", 141L),
                    new DemoTeaMaterial(3, sdf.parse("2024-03-28 05:35:18"), "果元店", "原料购进", 802L),
                    new DemoTeaMaterial(4, sdf.parse("2024-03-03 15:26:33"), "蓝墨店", "原料购进", 646L),
                    new DemoTeaMaterial(5, sdf.parse("2024-03-26 18:36:21"), "南都店", "原料购进", 680L),
                    new DemoTeaMaterial(6, sdf.parse("2024-03-04 19:55:07"), "香橙店", "原料购进", 190L),
                    new DemoTeaMaterial(7, sdf.parse("2024-03-21 09:57:12"), "乐园店", "原料购进", 183L),
                    new DemoTeaMaterial(8, sdf.parse("2024-03-18 01:25:25"), "欢果店", "原料购进", 568L),
                    new DemoTeaMaterial(9, sdf.parse("2024-03-10 23:20:21"), "红叶店", "原料购进", 145L),
                    new DemoTeaMaterial(10, sdf.parse("2024-03-01 07:55:58"), "蓝墨店", "原料购进", 571L),
                    new DemoTeaMaterial(11, sdf.parse("2024-03-16 16:51:17"), "乐园店", "原料购进", 563L),
                    new DemoTeaMaterial(12, sdf.parse("2024-03-21 09:33:37"), "果元店", "原料购进", 337L),
                    new DemoTeaMaterial(13, sdf.parse("2024-03-23 13:17:04"), "果元店", "原料购进", 743L),
                    new DemoTeaMaterial(14, sdf.parse("2024-03-10 22:30:29"), "水围店", "原料购进", 208L),
                    new DemoTeaMaterial(15, sdf.parse("2024-03-25 08:59:12"), "水围店", "原料购进", 357L),
                    new DemoTeaMaterial(16, sdf.parse("2024-03-19 06:08:16"), "果元店", "原料购进", 579L),
                    new DemoTeaMaterial(17, sdf.parse("2024-03-05 23:41:43"), "香橙店", "原料购进", 278L),
                    new DemoTeaMaterial(18, sdf.parse("2024-03-20 07:53:58"), "南都店", "原料购进", 604L),
                    new DemoTeaMaterial(19, sdf.parse("2024-03-21 11:39:25"), "果元店", "原料购进", 155L),
                    new DemoTeaMaterial(20, sdf.parse("2024-03-25 00:44:09"), "果元店", "原料购进", 211L),
                    new DemoTeaMaterial(21, sdf.parse("2024-03-13 10:30:44"), "水围店", "原料购进", 576L),
                    new DemoTeaMaterial(22, sdf.parse("2024-03-09 20:07:20"), "蓝墨店", "原料购进", 243L),
                    new DemoTeaMaterial(23, sdf.parse("2024-03-04 02:07:47"), "香橙店", "原料购进", 277L),
                    new DemoTeaMaterial(24, sdf.parse("2024-03-13 00:45:00"), "南都店", "原料购进", 101L),
                    new DemoTeaMaterial(25, sdf.parse("2024-03-07 16:39:38"), "果元店", "原料购进", 546L),
                    new DemoTeaMaterial(26, sdf.parse("2024-03-30 00:16:49"), "欢果店", "原料购进", 581L),
                    new DemoTeaMaterial(27, sdf.parse("2024-03-21 09:28:40"), "南都店", "原料购进", 123L),
                    new DemoTeaMaterial(27, sdf.parse("2024-03-11 11:05:26"), "欢果店", "原料购进", 628L),
                    new DemoTeaMaterial(29, sdf.parse("2024-03-09 02:22:10"), "乐园店", "原料购进", 194L),
                    new DemoTeaMaterial(30, sdf.parse("2024-03-10 01:43:49"), "水围店", "原料购进", 122L)
            );

            demoTeaMaterialRepository.saveAllAndFlush(materials);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initDemoTeaOrder() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            List<DemoTeaOrder> materials = Arrays.asList(
                    new DemoTeaOrder(1, "香橙店", "浓郁椰奶", "超大酷柠", "冷", "50塑", 165, 16, "131696143796", sdf.parse("2024-03-13 01:39:25")),
                    new DemoTeaOrder(2, "果元店", "果粒果汁", "爆粒鲜橙", "热", "40塑", 228, 10, "600033642270", sdf.parse("2024-03-20 16:43:33")),
                    new DemoTeaOrder(3, "蓝墨店", "浓郁椰奶", "爆粒鲜橙", "冷", "1000ml", 154, 16, "884244813757", sdf.parse("2024-03-17 20:13:47")),
                    new DemoTeaOrder(4, "水围店", "暖饮果汁", "酷乐鲜柠", "热", "纸大", 149, 10, "264979363423", sdf.parse("2024-03-06 00:50:16")),
                    new DemoTeaOrder(5, "南都店", "暖饮果汁", "布丁珍珠奶茶", "冷", "50塑", 101, 10, "385870702878", sdf.parse("2024-03-14 17:18:29")),
                    new DemoTeaOrder(6, "乐园店", "软糯芋泥", "爆粒鲜橙", "冷", "纸大", 234, 6, "791454535962", sdf.parse("2024-03-13 14:06:58")),
                    new DemoTeaOrder(7, "香橙店", "浓郁椰奶", "超大酷柠", "冷", "50塑", 121, 10, "413995522699", sdf.parse("2024-03-02 04:33:00")),
                    new DemoTeaOrder(8, "水围店", "浓郁椰奶", "生榨纯椰", "冷", "40塑", 243, 6, "414209828587", sdf.parse("2024-03-14 20:08:33")),
                    new DemoTeaOrder(9, "蓝墨店", "果粒果汁", "布丁珍珠奶茶", "热", "50塑", 299, 10, "958393980949", sdf.parse("2024-03-12 19:10:48")),
                    new DemoTeaOrder(10, "香橙店", "浓郁椰奶", "超大酷柠", "冷", "纸大", 192, 23, "520552711676", sdf.parse("2024-03-11 09:08:44")),
                    new DemoTeaOrder(11, "果元店", "超大果茶", "爆粒鲜橙", "热", "塑大", 247, 6, "498009486160", sdf.parse("2024-03-19 14:19:11")),
                    new DemoTeaOrder(12, "乐园店", "滋味果昔", "爆粒鲜橙", "热", "磨砂", 211, 6, "767676599378", sdf.parse("2024-03-07 19:16:15")),
                    new DemoTeaOrder(13, "南都店", "暖饮果汁", "生榨纯椰", "冷", "塑大", 232, 16, "760679036005", sdf.parse("2024-03-18 14:09:09")),
                    new DemoTeaOrder(14, "蓝墨店", "滋味果昔", "珍珠奶茶", "冷", "纸大", 246, 6, "343759610725", sdf.parse("2024-03-23 08:59:58")),
                    new DemoTeaOrder(15, "果元店", "超大果茶", "杨枝甘露", "冷", "50塑", 192, 13, "667202430558", sdf.parse("2024-03-30 00:50:34")),
                    new DemoTeaOrder(16, "红叶店", "暖饮果汁", "芋泥芋圆", "热", "塑大", 130, 29, "973738448731", sdf.parse("2024-03-19 15:22:34")),
                    new DemoTeaOrder(17, "南都店", "浓郁椰奶", "超大酷柠", "热", "50塑", 220, 29, "611315260914", sdf.parse("2024-03-15 17:03:38")),
                    new DemoTeaOrder(18, "果元店", "爆料果汁", "珍珠奶茶", "冷", "塑大", 106, 9, "032924534896", sdf.parse("2024-03-10 19:59:28")),
                    new DemoTeaOrder(19, "果元店", "暖饮果汁", "生榨纯椰", "冷", "塑大", 129, 23, "138461315351", sdf.parse("2024-03-26 17:59:54")),
                    new DemoTeaOrder(20, "果元店", "醇香奶茶", "超大桃桃", "冷", "纸大", 271, 10, "840668169759", sdf.parse("2024-03-26 04:11:48")),
                    new DemoTeaOrder(21, "乐园店", "暖饮果汁", "杨枝甘露", "冷", "纸", 257, 10, "328888056905", sdf.parse("2024-03-28 05:42:51")),
                    new DemoTeaOrder(22, "南都店", "浓郁椰奶", "超大酷柠", "热", "1000ml", 131, 23, "549500625936", sdf.parse("2024-03-26 05:17:30")),
                    new DemoTeaOrder(23, "蓝墨店", "暖饮果汁", "珍珠奶茶", "冷", "塑大", 155, 6, "413132617712", sdf.parse("2024-03-28 09:04:21")),
                    new DemoTeaOrder(24, "香橙店", "爆料果汁", "爆粒鲜橙", "冷", "磨砂", 135, 6, "439234733151", sdf.parse("2024-03-10 15:17:50")),
                    new DemoTeaOrder(25, "南都店", "软糯芋泥", "超大酷柠", "冷", "纸", 203, 13, "562586243741", sdf.parse("2024-03-08 23:07:55")),
                    new DemoTeaOrder(26, "红叶店", "暖饮果汁", "爆粒鲜橙", "热", "50塑", 281, 9, "172802630686", sdf.parse("2024-03-15 11:54:14")),
                    new DemoTeaOrder(27, "果元店", "超大果茶", "芒果西番莲", "冷", "50塑", 258, 13, "309515944911", sdf.parse("2024-03-07 08:11:46")),
                    new DemoTeaOrder(28, "南都店", "超大果茶", "超大酷柠", "冷", "磨砂", 246, 9, "376472713531", sdf.parse("2024-03-28 02:24:12")),
                    new DemoTeaOrder(29, "红叶店", "爆料果汁", "爆粒鲜橙", "冷", "1000ml", 267, 29, "142753377390", sdf.parse("2024-03-17 04:05:34")),
                    new DemoTeaOrder(30, "乐园店", "滋味果昔", "超大酷柠", "冷", "塑大", 144, 6, "845083976435", sdf.parse("2024-03-29 20:55:30")),
                    new DemoTeaOrder(31, "红叶店", "暖饮果汁", "酷乐鲜柠", "冷", "50塑", 226, 16, "886773485680", sdf.parse("2024-03-01 07:27:56")),
                    new DemoTeaOrder(32, "南都店", "爆料果汁", "爆粒鲜橙", "冷", "40塑", 144, 6, "349492386865", sdf.parse("2024-03-11 06:56:47")),
                    new DemoTeaOrder(33, "香橙店", "浓郁椰奶", "杨枝甘露", "热", "40塑", 284, 10, "408801195648", sdf.parse("2024-03-29 15:20:29")),
                    new DemoTeaOrder(34, "果元店", "超大果茶", "杨枝甘露", "冷", "40塑", 137, 29, "819668467639", sdf.parse("2024-03-05 18:39:59")),
                    new DemoTeaOrder(35, "水围店", "浓郁椰奶", "芋泥芋圆", "热", "40塑", 283, 16, "682199136858", sdf.parse("2024-03-09 02:59:53")),
                    new DemoTeaOrder(36, "欢果店", "爆料果汁", "爆粒鲜橙", "热", "50塑", 232, 23, "227621563468", sdf.parse("2024-03-02 16:12:58")),
                    new DemoTeaOrder(37, "蓝墨店", "醇香奶茶", "杨枝甘露", "冷", "纸", 202, 29, "092256992336", sdf.parse("2024-03-22 10:59:10")),
                    new DemoTeaOrder(38, "红叶店", "果粒果汁", "原味奶茶", "冷", "50塑", 280, 10, "432615585424", sdf.parse("2024-03-21 06:48:10")),
                    new DemoTeaOrder(39, "水围店", "超大果茶", "超大桃桃", "冷", "纸", 290, 29, "033917157071", sdf.parse("2024-03-31 22:01:04")),
                    new DemoTeaOrder(40, "红叶店", "暖饮果汁", "爆粒鲜橙", "冷", "塑大", 145, 9, "026608724006", sdf.parse("2024-03-15 04:55:43")),
                    new DemoTeaOrder(41, "南都店", "醇香奶茶", "杨枝甘露", "热", "40塑", 273, 10, "849584185483", sdf.parse("2024-03-25 05:18:32")),
                    new DemoTeaOrder(42, "欢果店", "爆料果汁", "芒果西番莲", "冷", "纸大", 261, 16, "877168481742", sdf.parse("2024-03-08 16:12:33")),
                    new DemoTeaOrder(43, "欢果店", "浓郁椰奶", "爆粒鲜橙", "冷", "塑大", 269, 10, "522723708126", sdf.parse("2024-03-01 07:02:45")),
                    new DemoTeaOrder(44, "果元店", "软糯芋泥", "生榨纯椰", "冷", "1000ml", 132, 16, "234741396784", sdf.parse("2024-03-01 05:20:32")),
                    new DemoTeaOrder(45, "香橙店", "醇香奶茶", "超大酷柠", "冷", "1000ml", 121, 10, "169346306025", sdf.parse("2024-03-07 07:48:10")),
                    new DemoTeaOrder(46, "乐园店", "醇香奶茶", "生榨纯椰", "冷", "纸大", 174, 6, "033478969174", sdf.parse("2024-03-24 07:56:50")),
                    new DemoTeaOrder(47, "果元店", "爆料果汁", "杨枝甘露", "冷", "塑大", 190, 13, "308866895780", sdf.parse("2024-03-11 07:45:56")),
                    new DemoTeaOrder(48, "红叶店", "暖饮果汁", "杨枝甘露", "冷", "40塑", 203, 16, "977260171260", sdf.parse("2024-03-15 07:51:31")),
                    new DemoTeaOrder(49, "香橙店", "爆料果汁", "爆粒鲜橙", "冷", "纸", 194, 6, "026538512943", sdf.parse("2024-03-21 16:27:13")),
                    new DemoTeaOrder(50, "香橙店", "超大果茶", "原味奶茶", "冷", "40塑", 160, 13, "722177202483", sdf.parse("2024-03-24 02:12:10")),
                    new DemoTeaOrder(51, "南都店", "软糯芋泥", "超大酷柠", "冷", "磨砂", 161, 16, "978077236096", sdf.parse("2024-03-06 04:28:39")),
                    new DemoTeaOrder(52, "香橙店", "软糯芋泥", "杨枝甘露", "冷", "40塑", 119, 16, "571583846849", sdf.parse("2024-03-31 13:56:23")),
                    new DemoTeaOrder(53, "红叶店", "暖饮果汁", "芋泥芋圆", "热", "20纸大", 146, 16, "153942260550", sdf.parse("2024-03-06 19:26:24")),
                    new DemoTeaOrder(54, "水围店", "超大果茶", "杨枝甘露", "热", "塑大", 167, 23, "533436086428", sdf.parse("2024-03-08 22:13:39")),
                    new DemoTeaOrder(55, "蓝墨店", "浓郁椰奶", "爆粒鲜橙", "冷", "50塑", 165, 29, "899072569391", sdf.parse("2024-03-07 19:29:55")),
                    new DemoTeaOrder(56, "红叶店", "果粒果汁", "杨枝甘露", "冷", "磨砂", 124, 13, "064192214887", sdf.parse("2024-03-17 05:43:45")),
                    new DemoTeaOrder(57, "南都店", "爆料果汁", "超大酷柠", "冷", "纸大", 117, 9, "952241530599", sdf.parse("2024-03-31 00:11:47")),
                    new DemoTeaOrder(58, "果元店", "醇香奶茶", "布丁珍珠奶茶", "冷", "磨砂", 236, 16, "361733375659", sdf.parse("2024-03-28 19:11:00")),
                    new DemoTeaOrder(59, "红叶店", "滋味果昔", "爆粒鲜橙", "冷", "纸", 244, 16, "456681384664", sdf.parse("2024-03-06 18:06:27")),
                    new DemoTeaOrder(60, "果元店", "超大果茶", "超大桃桃", "热", "塑大", 271, 23, "239545648049", sdf.parse("2024-03-01 14:42:10"))
            );
            demoTeaOrderRepository.saveAllAndFlush(materials);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
