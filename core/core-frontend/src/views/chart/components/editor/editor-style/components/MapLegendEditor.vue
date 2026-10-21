<script lang="ts" setup>
import { computed, reactive, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { formatterItem } from '@/views/chart/components/js/formatter'
import { mapRangeKey } from '@/views/chart/components/js/panel/charts/map/mapLegend'
import { parseJson } from '@/views/chart/components/js/util'
import ValueFormatterEdit from '../../drag-item/components/ValueFormatterEdit.vue'

const props = defineProps<{ modelValue?: MapLegendOptions; chart: Chart; themes: EditorTheme }>()
const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()
const normalize = (value?: MapLegendOptions) => ({
  formatMode: 'legacy',
  prefix: '',
  syncTooltip: false,
  ...cloneDeep(value),
  rangeLabels: { ...value?.rangeLabels },
  formatterCfg: { ...formatterItem, ...cloneDeep(value?.formatterCfg) }
})
const draft = reactive(normalize(props.modelValue))
watch(
  () => props.modelValue,
  value => {
    const next = normalize(value)
    if (JSON.stringify(draft) !== JSON.stringify(next)) Object.assign(draft, next)
  },
  { deep: true }
)
const save = () => {
  if (JSON.stringify(draft) !== JSON.stringify(normalize(props.modelValue))) {
    emit('update:modelValue', cloneDeep(draft))
  }
}
const ranges = computed(() => {
  const misc = parseJson(props.chart.customAttr)?.misc
  if (misc?.mapAutoLegend || misc?.mapLegendRangeType !== 'custom' || props.chart.drill) return []
  const values = misc.mapLegendCustomRange || []
  return values.slice(0, -1).map((value, index) => [value, values[index + 1]])
})
</script>

<template>
  <el-form-item :label="t('chart.heatmap_format')" class="form-item" :class="'form-item-' + themes">
    <el-select v-model="draft.formatMode" :effect="themes" @change="save">
      <el-option value="legacy" :label="t('chart.map_format_legacy')" />
      <el-option value="inherit" :label="t('chart.heatmap_format_inherit')" />
      <el-option value="custom" :label="t('chart.heatmap_format_custom')" />
    </el-select>
  </el-form-item>
  <ValueFormatterEdit
    v-if="draft.formatMode === 'custom'"
    inline
    format-type-select
    :themes="themes"
    :formatter-item="draft"
    :chart="chart"
    :example-prefix="draft.prefix"
    @change="save"
  >
    <el-form-item
      :label="t('chart.heatmap_prefix')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-input v-model="draft.prefix" :effect="themes" maxlength="30" clearable @change="save" />
    </el-form-item>
  </ValueFormatterEdit>
  <el-form-item class="form-item" :class="'form-item-' + themes">
    <el-checkbox v-model="draft.syncTooltip" :effect="themes" @change="save">
      {{ t('chart.map_format_sync_tooltip') }}
    </el-checkbox>
  </el-form-item>
  <el-form-item
    v-for="range in ranges"
    :key="mapRangeKey(range)"
    :label="`${t('chart.map_range_label')} (${range.join(' - ')})`"
    class="form-item"
    :class="'form-item-' + themes"
  >
    <el-input
      v-model="draft.rangeLabels[mapRangeKey(range)]"
      :effect="themes"
      :placeholder="t('chart.map_range_label_auto')"
      maxlength="100"
      clearable
      @change="save"
    />
  </el-form-item>
</template>
