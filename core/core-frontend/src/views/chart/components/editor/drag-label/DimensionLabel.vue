<script lang="tsx" setup>
import { useI18n } from '@/hooks/web/useI18n'
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
        (props.view.type.includes('bar') ||
          props.view.type.includes('line') ||
          props.view.type.includes('scatter') ||
          props.view.type === 'chart-mix' ||
          props.view.type === 'waterfall' ||
          props.view.type === 'area')
      "
      >{{ t('chart.drag_block_type_axis') }}</span
    >
    <span v-else-if="props.view.type && props.view.type.includes('pie')">{{
      t('chart.drag_block_pie_label')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('funnel')">{{
      t('chart.drag_block_funnel_split')
    }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('radar')">{{
      t('chart.drag_block_radar_label')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'map'">{{ t('chart.area') }}</span>
    <span v-else-if="props.view.type && props.view.type.includes('treemap')">{{
      t('chart.drag_block_treemap_label')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'word-cloud'">{{
      t('chart.drag_block_word_cloud_label')
    }}</span>
    <span v-else-if="props.view.type && props.view.type === 'label'">{{
      t('chart.drag_block_label')
    }}</span>
    <span v-else-if="props.view.type === 'flow-map'">{{ t('chart.start_point') }}</span>
    <span v-show="props.view.type !== 'richTextView'"> / </span>
    <span v-if="props.view.type && props.view.type !== 'table-info'">
      {{ t('chart.dimension') }}
    </span>
    <span v-else-if="props.view.type && props.view.type === 'table-info'">{{
      t('chart.dimension_or_quota')
    }}</span>
  </div>
</template>

<style lang="less" scoped></style>
