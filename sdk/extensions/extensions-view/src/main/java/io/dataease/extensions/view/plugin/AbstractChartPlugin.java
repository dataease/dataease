package io.dataease.extensions.view.plugin;

import io.dataease.extensions.datasource.model.SQLMeta;
import io.dataease.extensions.datasource.provider.Provider;
import io.dataease.extensions.view.dto.*;

import java.util.List;
import java.util.Map;


public abstract class AbstractChartPlugin {

    /**
     * 处理视图需要用到的轴字段，大部分图表都聚合成两个字段，X 和 Y。
     *
     * @param view 视图对象
     * @param <T>  泛化的返回类型，可继承。
     * @return 处理后的轴和过程变量，变量放到整个流程上下文中。
     */
    public abstract <T extends AxisFormatResult> T formatAxis(ChartViewDTO view);

    /**
     * 处理视图的过滤条件，包括联动、跳转、过滤、外部参数、下钻等等。
     * DE 本身有标准的处理流程将所有的条件处理成统一的过滤列表，如果有额外的条件需要处理可在这边操作，例如子维度下钻。
     *
     * @param view         视图对象
     * @param filterList   DE 标准化处理后的过滤条件列表。
     * @param formatResult 处理后的轴和过程变量，变量放到整个流程上下文中。
     * @param <T>          泛化的返回类型，可继承。
     * @return 处理后的过滤条件列表和过程变量，变量放到整个流程上下文中。
     */
    public abstract <T extends CustomFilterResult> T customFilter(ChartViewDTO view, List<ChartExtFilterDTO> filterList, AxisFormatResult formatResult);

    /**
     * 计算视图的最终结果,各个参数使用方式可参考主工程。
     *
     * @param view         视图对象
     * @param formatResult 轴字段处理结果
     * @param filterResult 过滤条件处理结果
     * @param sqlMap       SQL
     * @param sqlMeta      SQL元数据，用于生成最终SQL
     * @param provider     数据源查询工具
     * @param <T>          泛化的过滤列表，和上面一致。
     * @return 视图数据计算结果
     */
    public abstract <T extends ChartCalcDataResult> T calcChartResult(ChartViewDTO view, AxisFormatResult formatResult, CustomFilterResult filterResult, Map<String, Object> sqlMap, SQLMeta sqlMeta, Provider provider);

    /**
     * 构建视图，将计算结果处理成最终视图。
     *
     * @param view         原视图对象
     * @param calcResult   计算结果
     * @param view         原视图对象
     * @param calcResult   计算结果
     * @param formatResult 轴字段处理结果
     * @param filterResult 过滤条件处理结果
     * @return 返回前端的视图，建议数据放在 data 中，过滤条件放在 filter 中，其他字段可自行添加，可参考主工程。
     */
    public abstract ChartViewDTO buildChart(ChartViewDTO view, ChartCalcDataResult calcResult, AxisFormatResult formatResult, CustomFilterResult filterResult);
}

