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
  value => Object.assign(draft, normalize(value)),
  { deep: true }
)
const save = () => {
  if (JSON.stringify(draft) !== JSON.stringify(normalize(props.modelValue))) {
    emit('update:modelValue', cloneDeep(draft))
  }
}
watch(() => draft.formatterCfg, save, { deep: true })
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
