<template>
  <div class="chart-type">
    <div v-if="chart.render && chart.render === 'antv'">
      <div class="radio-row">
        <el-radio value="table-normal" label="table-normal">
          <span :title="$t('chart.chart_table_normal')">
            <svg-icon icon-class="table-normal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="table-info" label="table-info">
          <span :title="$t('chart.chart_table_info')">
            <svg-icon icon-class="table-info" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="text" label="text">
          <span :title="$t('chart.chart_card')">
            <svg-icon icon-class="text" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar" label="bar">
          <span :title="$t('chart.chart_bar')">
            <svg-icon icon-class="bar" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar-stack" label="bar-stack">
          <span :title="$t('chart.chart_bar_stack')">
            <svg-icon icon-class="bar-stack" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <el-radio value="bar-horizontal" label="bar-horizontal">
          <span :title="$t('chart.chart_bar_horizontal')">
            <svg-icon icon-class="bar-horizontal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal">
          <span :title="$t('chart.chart_bar_stack_horizontal')">
            <svg-icon icon-class="bar-stack-horizontal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="line" label="line">
          <span :title="$t('chart.chart_line')">
            <svg-icon icon-class="line" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="line-stack" label="line-stack">
          <span :title="$t('chart.chart_line_stack')">
            <svg-icon icon-class="line-stack" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="scatter" label="scatter">
          <span :title="$t('chart.chart_scatter')">
            <svg-icon icon-class="scatter" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <!--        <el-radio value="chart-mix" label="chart-mix">-->
        <!--          <span :title="$t('chart.chart_mix')">-->
        <!--            <svg-icon icon-class="chart-mix" class="chart-icon" />-->
        <!--          </span>-->
        <!--        </el-radio>-->
        <!--        <el-radio value="map" label="map">-->
        <!--          <span :title="$t('chart.chart_map')">-->
        <!--            <svg-icon icon-class="map" class="chart-icon" />-->
        <!--          </span>-->
        <!--        </el-radio>-->
        <el-radio value="waterfall" label="waterfall">
          <span :title="$t('chart.chart_waterfall')">
            <svg-icon icon-class="waterfall" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="radar" label="radar">
          <span :title="$t('chart.chart_radar')">
            <svg-icon icon-class="radar" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="gauge" label="gauge">
          <span :title="$t('chart.chart_gauge')">
            <svg-icon icon-class="gauge" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="pie" label="pie">
          <span :title="$t('chart.chart_pie')">
            <svg-icon icon-class="pie" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="pie-rose" label="pie-rose">
          <span :title="$t('chart.chart_pie_rose')">
            <svg-icon icon-class="pie-rose" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <el-radio value="funnel" label="funnel">
          <span :title="$t('chart.chart_funnel')">
            <svg-icon icon-class="funnel" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="treemap" label="treemap">
          <span :title="$t('chart.chart_treemap')">
            <svg-icon icon-class="treemap" class="chart-icon" />
          </span>
        </el-radio>
<!--        <el-radio value="word-cloud" label="word-cloud">-->
<!--          <span :title="$t('chart.chart_word_cloud')">-->
<!--            <svg-icon icon-class="word-cloud" class="chart-icon" />-->
<!--          </span>-->
<!--        </el-radio>-->
        <el-radio value="liquid" label="liquid">
          <span :title="$t('chart.chart_liquid')">
            <svg-icon icon-class="liquid" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
      </div>
    </div>
    <div v-else-if="chart.render && chart.render === 'echarts'">
      <div class="radio-row">
        <el-radio value="table-normal" label="table-normal">
          <span :title="$t('chart.chart_table_normal')">
            <svg-icon icon-class="table-normal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="table-info" label="table-info">
          <span :title="$t('chart.chart_table_info')">
            <svg-icon icon-class="table-info" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="text" label="text">
          <span :title="$t('chart.chart_card')">
            <svg-icon icon-class="text" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar" label="bar">
          <span :title="$t('chart.chart_bar')">
            <svg-icon icon-class="bar" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar-stack" label="bar-stack">
          <span :title="$t('chart.chart_bar_stack')">
            <svg-icon icon-class="bar-stack" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <el-radio value="bar-horizontal" label="bar-horizontal">
          <span :title="$t('chart.chart_bar_horizontal')">
            <svg-icon icon-class="bar-horizontal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal">
          <span :title="$t('chart.chart_bar_stack_horizontal')">
            <svg-icon icon-class="bar-stack-horizontal" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="line" label="line">
          <span :title="$t('chart.chart_line')">
            <svg-icon icon-class="line" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="line-stack" label="line-stack">
          <span :title="$t('chart.chart_line_stack')">
            <svg-icon icon-class="line-stack" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="scatter" label="scatter">
          <span :title="$t('chart.chart_scatter')">
            <svg-icon icon-class="scatter" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <el-radio value="chart-mix" label="chart-mix">
          <span :title="$t('chart.chart_mix')">
            <svg-icon icon-class="chart-mix" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="map" label="map">
          <span :title="$t('chart.chart_map')">
            <svg-icon icon-class="map" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="radar" label="radar">
          <span :title="$t('chart.chart_radar')">
            <svg-icon icon-class="radar" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="gauge" label="gauge">
          <span :title="$t('chart.chart_gauge')">
            <svg-icon icon-class="gauge" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="pie" label="pie">
          <span :title="$t('chart.chart_pie')">
            <svg-icon icon-class="pie" class="chart-icon" />
          </span>
        </el-radio>
      </div>
      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">
        <el-radio value="pie-rose" label="pie-rose">
          <span :title="$t('chart.chart_pie_rose')">
            <svg-icon icon-class="pie-rose" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="funnel" label="funnel">
          <span :title="$t('chart.chart_funnel')">
            <svg-icon icon-class="funnel" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="treemap" label="treemap">
          <span :title="$t('chart.chart_treemap')">
            <svg-icon icon-class="treemap" class="chart-icon" />
          </span>
        </el-radio>
        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
      </div>
    </div>
    <!--占位行-->
  <!--                      <div style="width: 100%;display: flex;display: -webkit-flex;justify-content: space-between;flex-direction: row;flex-wrap: wrap;">-->
  <!--                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>-->
  <!--                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>-->
  <!--                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>-->
  <!--                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>-->
  <!--                        <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>-->
  <!--                      </div>-->
  </div>
</template>

<script>
export default {
  name: 'ChartType',
  props: {
    chart: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped lang="scss">
.chart-type{
  >>>div{
    width: 100%;
  }
  padding: 4px;
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
}

.chart-icon{
  width: 20px;
  height: 20px;
}

.chart-type>>>.el-radio__input{
  display: none!important;
}

.el-radio{
  margin:5px;
}

.el-radio>>>.el-radio__label{
  padding-left: 0;
}

.radio-row{
  width: 100%;
  display: flex;
  display: -webkit-flex;
  justify-content: space-between;
  flex-direction: row;
  flex-wrap: wrap;
}
</style>
