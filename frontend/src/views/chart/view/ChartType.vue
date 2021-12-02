<template>
  <div class="chart-type">
    <div v-if="chart.render && chart.render === 'antv'">
      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_table') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="table-normal" label="table-normal" border class="radio-style">
              <span :title="$t('chart.chart_table_normal')">
                <svg-icon icon-class="table-normal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_table_normal') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="table-info" label="table-info" border class="radio-style">
              <span :title="$t('chart.chart_table_info')">
                <svg-icon icon-class="table-info" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_table_info') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_quota') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="text" label="text" border class="radio-style">
              <span :title="$t('chart.chart_card')">
                <svg-icon icon-class="text" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_card') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="gauge" label="gauge" border class="radio-style">
              <span :title="$t('chart.chart_gauge')">
                <svg-icon icon-class="gauge" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_gauge') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="liquid" label="liquid" border class="radio-style">
              <span :title="$t('chart.chart_liquid')">
                <svg-icon icon-class="liquid" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_liquid') }}</p>
            </el-radio>
          </div>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_trend') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="line" label="line" border class="radio-style">
              <span :title="$t('chart.chart_line')">
                <svg-icon icon-class="line" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_line') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="line-stack" label="line-stack" border class="radio-style">
              <span :title="$t('chart.chart_line_stack')">
                <svg-icon icon-class="line-stack" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_line_stack') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_compare') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="bar" label="bar" border class="radio-style">
              <span :title="$t('chart.chart_bar')">
                <svg-icon icon-class="bar" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="bar-stack" label="bar-stack" border class="radio-style">
              <span :title="$t('chart.chart_bar_stack')">
                <svg-icon icon-class="bar-stack" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_stack') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="waterfall" label="waterfall" border class="radio-style">
              <span :title="$t('chart.chart_waterfall')">
                <svg-icon icon-class="waterfall" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_waterfall') }}</p>
            </el-radio>
          </div>
        </div>
      </div>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="bar-horizontal" label="bar-horizontal" border class="radio-style">
              <span :title="$t('chart.chart_bar_horizontal')">
                <svg-icon icon-class="bar-horizontal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_horizontal') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal" border class="radio-style">
              <span :title="$t('chart.chart_bar_stack_horizontal')">
                <svg-icon icon-class="bar-stack-horizontal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_stack_horizontal') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_distribute') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="pie" label="pie" border class="radio-style">
              <span :title="$t('chart.chart_pie')">
                <svg-icon icon-class="pie" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_pie') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="pie-rose" label="pie-rose" border class="radio-style">
              <span :title="$t('chart.chart_pie_rose')">
                <svg-icon icon-class="pie-rose" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_pie_rose') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="radar" label="radar" border class="radio-style">
              <span :title="$t('chart.chart_radar')">
                <svg-icon icon-class="radar" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_radar') }}</p>
            </el-radio>
          </div>
        </div>
      </div>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="treemap" label="treemap" border class="radio-style">
              <span :title="$t('chart.chart_treemap')">
                <svg-icon icon-class="treemap" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_treemap') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="word-cloud" label="word-cloud" border class="radio-style">
              <span :title="$t('chart.chart_word_cloud')">
                <svg-icon icon-class="word-cloud" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_word_cloud') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_relation') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="scatter" label="scatter" border class="radio-style">
              <span :title="$t('chart.chart_scatter')">
                <svg-icon icon-class="scatter" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_scatter') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="funnel" label="funnel" border class="radio-style">
              <span :title="$t('chart.chart_funnel')">
                <svg-icon icon-class="funnel" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_funnel') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>
    </div>
    <div v-else-if="chart.render && chart.render === 'echarts'">
      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_table') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="table-normal" label="table-normal" border class="radio-style">
              <span :title="$t('chart.chart_table_normal')">
                <svg-icon icon-class="table-normal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_table_normal') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="table-info" label="table-info" border class="radio-style">
              <span :title="$t('chart.chart_table_info')">
                <svg-icon icon-class="table-info" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_table_info') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_quota') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="text" label="text" border class="radio-style">
              <span :title="$t('chart.chart_card')">
                <svg-icon icon-class="text" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_card') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="gauge" label="gauge" border class="radio-style">
              <span :title="$t('chart.chart_gauge')">
                <svg-icon icon-class="gauge" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_gauge') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_trend') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="line" label="line" border class="radio-style">
              <span :title="$t('chart.chart_line')">
                <svg-icon icon-class="line" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_line') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="line-stack" label="line-stack" border class="radio-style">
              <span :title="$t('chart.chart_line_stack')">
                <svg-icon icon-class="line-stack" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_line_stack') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="chart-mix" label="chart-mix" border class="radio-style">
              <span :title="$t('chart.chart_mix')">
                <svg-icon icon-class="chart-mix" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_mix') }}</p>
            </el-radio>
          </div>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_compare') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="bar" label="bar" border class="radio-style">
              <span :title="$t('chart.chart_bar')">
                <svg-icon icon-class="bar" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="bar-stack" label="bar-stack" border class="radio-style">
              <span :title="$t('chart.chart_bar_stack')">
                <svg-icon icon-class="bar-stack" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_stack') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="bar-horizontal" label="bar-horizontal" border class="radio-style">
              <span :title="$t('chart.chart_bar_horizontal')">
                <svg-icon icon-class="bar-horizontal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_horizontal') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="bar-stack-horizontal" label="bar-stack-horizontal" border class="radio-style">
              <span :title="$t('chart.chart_bar_stack_horizontal')">
                <svg-icon icon-class="bar-stack-horizontal" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_bar_stack_horizontal') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_distribute') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="pie" label="pie" border class="radio-style">
              <span :title="$t('chart.chart_pie')">
                <svg-icon icon-class="pie" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_pie') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="pie-rose" label="pie-rose" border class="radio-style">
              <span :title="$t('chart.chart_pie_rose')">
                <svg-icon icon-class="pie-rose" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_pie_rose') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="radar" label="radar" border class="radio-style">
              <span :title="$t('chart.chart_radar')">
                <svg-icon icon-class="radar" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_radar') }}</p>
            </el-radio>
          </div>
        </div>
      </div>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="treemap" label="treemap" border class="radio-style">
              <span :title="$t('chart.chart_treemap')">
                <svg-icon icon-class="treemap" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_treemap') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_relation') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="scatter" label="scatter" border class="radio-style">
              <span :title="$t('chart.chart_scatter')">
                <svg-icon icon-class="scatter" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_scatter') }}</p>
            </el-radio>
          </div>
          <div style="position: relative;display: block;">
            <el-radio value="funnel" label="funnel" border class="radio-style">
              <span :title="$t('chart.chart_funnel')">
                <svg-icon icon-class="funnel" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_funnel') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
      </div>

      <el-divider class="chart-type-divider">{{ $t('chart.chart_type_space') }}</el-divider>
      <div style="display: block;">
        <div class="radio-row">
          <div style="position: relative;display: block;">
            <el-radio value="map" label="map" border class="radio-style">
              <span :title="$t('chart.chart_map')">
                <svg-icon icon-class="map" class="chart-icon" />
              </span>
              <p class="radio-label">{{ $t('chart.chart_map') }}</p>
            </el-radio>
          </div>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
          <el-radio value="" label="" disabled class="disabled-none-cursor"><svg-icon icon-class="" class="chart-icon" /></el-radio>
        </div>
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
  >div{
    width: 100%;
  }
  padding: 4px;
  display: -webkit-flex;
  flex-wrap: wrap;
  justify-content: space-between;
  overflow-y: auto;
  width: 100%;
}

.chart-icon{
  width: 80px;
  height: 40px;
}

.el-radio{
  margin:5px;
}

.radio-row{
  width: 100%;
  display: flex;
  display: -webkit-flex;
  justify-content: space-between;
  flex-direction: row;
  flex-wrap: wrap;
}

.chart-type-divider{
  width: auto;
}

.el-divider--horizontal{
  margin: 30px 0 14px 0;
}

.el-divider__text{
  font-size: 12px;
  font-weight: 400;
}

.radio-label{
  display: block;
  position: absolute;
  bottom: 0;
  width: 100%;
  margin: 0;
  text-align: center;
  height: 20px;
  line-height: 20px;
  text-overflow: ellipsis;
  overflow: hidden;
  white-space: pre;
  font-size: 12px;
}
</style>
