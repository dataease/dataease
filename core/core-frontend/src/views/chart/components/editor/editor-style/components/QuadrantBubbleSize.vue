<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { isValidBubbleRange } from '../../../js/panel/charts/g2/relation/quadrant-bubble'

const props = defineProps<{
  config?: ChartBasicStyle['quadrantBubble']
  themes: EditorTheme
}>()
const emit = defineEmits<{
  (event: 'change', config: ChartBasicStyle['quadrantBubble']): void
}>()
const { t } = useI18n()
const savedRange = computed(() =>
  isValidBubbleRange(props.config?.min, props.config?.max)
    ? { min: props.config.min, max: props.config.max }
    : { min: 5, max: 30 }
)
const draft = reactive({ mode: 'legacy', min: 5, max: 30 })
const minUpperBound = computed(() => Math.min(50, savedRange.value.max))
const maxLowerBound = computed(() => Math.max(5, savedRange.value.min))
watch(
  () => props.config,
  config => {
    draft.mode = config?.mode ?? 'legacy'
    draft.min = savedRange.value.min
    draft.max = savedRange.value.max
  },
  { immediate: true, deep: true }
)
const normalizeRadius = (value: number, fallback: number, min: number, max: number) =>
  Number.isFinite(value) ? Math.max(min, Math.min(max, Math.round(value))) : fallback
const change = (field?: 'min' | 'max') => {
  if (draft.mode !== 'auto' && draft.mode !== 'custom') return
  if (field === 'min') {
    draft.min = normalizeRadius(draft.min, savedRange.value.min, 1, minUpperBound.value)
  } else if (field === 'max') {
    draft.max = normalizeRadius(draft.max, savedRange.value.max, maxLowerBound.value, 100)
  } else {
    draft.min = savedRange.value.min
    draft.max = savedRange.value.max
  }
  if (!isValidBubbleRange(draft.min, draft.max)) return
  if (
    props.config?.mode === draft.mode &&
    props.config.min === draft.min &&
    props.config.max === draft.max
  ) {
    return
  }
  emit('change', { mode: draft.mode, min: draft.min, max: draft.max })
}
</script>

<template>
  <el-form-item
    :label="t('chart.quadrant_bubble_mode')"
    class="form-item"
    :class="'form-item-' + themes"
  >
    <el-select v-model="draft.mode" :effect="themes" @change="change()">
      <el-option
        v-if="!config"
        value="legacy"
        :label="t('chart.quadrant_bubble_legacy')"
        disabled
      />
      <el-option value="auto" :label="t('chart.quadrant_bubble_auto')" />
      <el-option value="custom" :label="t('chart.quadrant_bubble_custom')" />
    </el-select>
  </el-form-item>
  <template v-if="draft.mode === 'custom'">
    <el-form-item
      :label="t('chart.quadrant_bubble_min')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-input-number
        v-model="draft.min"
        :aria-label="t('chart.quadrant_bubble_min')"
        :effect="themes"
        :min="1"
        :max="minUpperBound"
        :step="1"
        :precision="0"
        :value-on-clear="savedRange.min"
        controls-position="right"
        @change="change('min')"
        @blur="change('min')"
      />
    </el-form-item>
    <el-form-item
      :label="t('chart.quadrant_bubble_max')"
      class="form-item"
      :class="'form-item-' + themes"
    >
      <el-input-number
        v-model="draft.max"
        :aria-label="t('chart.quadrant_bubble_max')"
        :effect="themes"
        :min="maxLowerBound"
        :max="100"
        :step="1"
        :precision="0"
        :value-on-clear="savedRange.max"
        controls-position="right"
        @change="change('max')"
        @blur="change('max')"
      />
    </el-form-item>
  </template>
</template>
