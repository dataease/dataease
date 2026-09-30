<script lang="ts" setup>
import { computed, reactive, watch } from 'vue'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { formatterItem } from '@/views/chart/components/js/formatter'
import ValueFormatterEdit from '../../drag-item/components/ValueFormatterEdit.vue'
import {
  heatmapDataDomain,
  validHeatmapRange
} from '@/views/chart/components/js/panel/charts/g2/table/heatmapLegend'

const props = defineProps<{
  modelValue?: HeatmapLegendOptions
  chart: Chart
  themes: EditorTheme
}>()
const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()
const normalize = (value?: HeatmapLegendOptions) => ({
  rangeMode: 'auto',
  formatMode: 'inherit',
  min: 0,
  max: 1,
  prefix: '',
  ...cloneDeep(value),
  formatterCfg: { ...formatterItem, ...cloneDeep(value?.formatterCfg) }
})
const draft = reactive(normalize(props.modelValue))
watch(
  () => props.modelValue,
  value => Object.assign(draft, normalize(value)),
  { deep: true }
)
const invalidRange = computed(
  () => draft.rangeMode === 'custom' && !validHeatmapRange(draft.min, draft.max)
)
const save = () => {
  if (
    !invalidRange.value &&
    JSON.stringify(draft) !== JSON.stringify(normalize(props.modelValue))
  ) {
    emit('update:modelValue', cloneDeep(draft))
  }
}
const changeRangeMode = () => {
  if (draft.rangeMode === 'custom' && props.modelValue?.min === undefined) {
    const [min, max] = heatmapDataDomain(props.chart) || [0, 1]
    draft.min = min
    draft.max = max > min ? max : min + 1
  }
  save()
}
watch(() => draft.formatterCfg, save, { deep: true })
</script>

<template>
  <el-form-item :label="t('chart.heatmap_range')" class="form-item" :class="'form-item-' + themes">
    <el-select v-model="draft.rangeMode" :effect="themes" @change="changeRangeMode">
      <el-option value="auto" :label="t('chart.heatmap_range_auto')" />
      <el-option value="custom" :label="t('chart.heatmap_range_custom')" />
    </el-select>
  </el-form-item>
  <el-form-item
    v-if="draft.rangeMode === 'custom'"
    :error="invalidRange ? t('chart.heatmap_range_invalid') : ''"
    class="form-item"
    :class="'form-item-' + themes"
  >
    <el-row :gutter="8">
      <el-col :span="12">
        <div style="margin-bottom: 4px">{{ t('chart.heatmap_min') }}</div>
        <el-input-number
          :effect="themes"
          v-model="draft.min"
          :aria-label="t('chart.heatmap_min')"
          :placeholder="t('chart.heatmap_min')"
          controls-position="right"
          style="width: 100%"
          @change="save"
        />
      </el-col>
      <el-col :span="12">
        <div style="margin-bottom: 4px">{{ t('chart.heatmap_max') }}</div>
        <el-input-number
          :effect="themes"
          v-model="draft.max"
          :aria-label="t('chart.heatmap_max')"
          :placeholder="t('chart.heatmap_max')"
          controls-position="right"
          style="width: 100%"
          @change="save"
        />
      </el-col>
    </el-row>
  </el-form-item>
  <el-form-item :label="t('chart.heatmap_format')" class="form-item" :class="'form-item-' + themes">
    <el-select v-model="draft.formatMode" :effect="themes" @change="save">
      <el-option value="inherit" :label="t('chart.heatmap_format_inherit')" />
      <el-option value="custom" :label="t('chart.heatmap_format_custom')" />
    </el-select>
  </el-form-item>
  <el-form-item
    v-if="draft.formatMode === 'custom'"
    class="form-item"
    :class="'form-item-' + themes"
  >
    <el-popover trigger="click" :width="320" placement="left" :effect="themes">
      <template #reference
        ><el-button
          class="format-button"
          :class="{ 'format-button--dark': themes === 'dark' }"
          :secondary="themes !== 'dark'"
          >{{ t('chart.heatmap_format_custom') }}</el-button
        ></template
      >
      <el-form
        class="prefix-form"
        :class="{ 'prefix-form--dark': themes === 'dark' }"
        label-position="top"
      >
        <el-form-item :label="t('chart.heatmap_prefix')">
          <el-input
            :effect="themes"
            v-model="draft.prefix"
            maxlength="30"
            clearable
            @change="save"
          />
        </el-form-item>
      </el-form>
      <ValueFormatterEdit
        :themes="themes"
        :formatter-item="draft"
        :chart="chart"
        :example-prefix="draft.prefix"
      />
    </el-popover>
  </el-form-item>
</template>

<style lang="less" scoped>
.prefix-form--dark :deep(.ed-form-item__label) {
  color: #ebebeb;
}
.format-button--dark {
  --ed-button-text-color: #ebebeb;
  --ed-button-bg-color: transparent;
  --ed-button-border-color: #505050;
  --ed-button-hover-text-color: #ebebeb;
  --ed-button-hover-bg-color: #414141;
  --ed-button-hover-border-color: #666666;
  --ed-button-active-text-color: #ebebeb;
  --ed-button-active-bg-color: #505050;
  --ed-button-active-border-color: #666666;
}
</style>
