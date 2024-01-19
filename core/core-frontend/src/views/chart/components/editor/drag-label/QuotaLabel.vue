<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
import { includesAny, equalsAny } from '../util/StringUtils'
const { t } = useI18n()

const props = defineProps({
  view: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div>
    <span v-if="props.view.type && props.view.type.includes('table')">{{
      t('chart.drag_block_table_data_column')
    }}</span>
    <span
      v-else-if="
        props.view.type &&
        (includesAny(props.view.type, 'bar', 'line', 'scatter') ||
          equalsAny(props.view.type, 'waterfall', 'area', 'area-stack', 'flow-map'))
      "
      >{{ t('chart.drag_block_value_axis') }}</span
    >
    <span v-else-if="props.view.type && props.view.type.includes('pie')">{{
      t('chart.drag_block_pie_angel')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('funnel')">{{
      t('chart.drag_block_funnel_width')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('radar')">{{
      t('chart.drag_block_radar_length')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('gauge')">{{
      t('chart.drag_block_gauge_angel')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('indicator')">{{
      t('chart.drag_block_label_value')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'map'">{{
      t('chart.chart_data')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('tree')">{{
      t('chart.drag_block_treemap_size')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'chart-mix'">{{
      t('chart.drag_block_value_axis_main')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'liquid'">{{
      t('chart.drag_block_progress')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'word-cloud'">{{
      t('chart.drag_block_word_cloud_size')
    }}</span>
    <span v-show="props.view.type !== 'richTextView'"> / </span>
    <span>{{ t('chart.quota') }}</span>
  </div>
</template>

<style lang="less" scoped></style>
