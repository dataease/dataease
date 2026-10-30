<script lang="ts" setup>
import { computed, toRefs } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { createClockOffset } from './dynamic-default-time'

const props = defineProps({
  config: { type: Object, required: true },
  end: { type: Boolean, default: false }
})
const { t } = useI18n()
const { config } = toRefs(props)
const key = computed(() => (props.end ? 'clockOffsetRange' : 'clockOffset'))
const timeKey = computed(() => (props.end ? 'arbitraryTimeRange' : 'arbitraryTime'))
const offset = computed(() => config.value[key.value])
const changeMode = (mode: 'fixed' | 'relative') => {
  config.value[key.value] = { ...createClockOffset(), ...offset.value, mode }
}
const units = ['hours', 'minutes', 'seconds'] as const
const numberValue = (value: number | string | null) =>
  value === null || String(value).trim() === '' || !Number.isFinite(Number(value))
    ? null
    : Number(value)
</script>

<template>
  <div class="clock-offset-editor">
    <div class="clock-row">
      <div class="clock-label">{{ t('dynamic_time.clock') }}</div>
      <el-radio-group :model-value="offset?.mode || 'fixed'" @change="changeMode">
        <el-radio value="fixed">{{ t('dynamic_time.fixed_clock') }}</el-radio>
        <el-radio value="relative">{{ t('dynamic_time.relative') }}</el-radio>
      </el-radio-group>
    </div>
    <div class="clock-row">
      <div class="clock-label">{{ t('dynamic_time.clock_offset') }}</div>
      <el-time-picker
        v-if="!offset || offset.mode === 'fixed'"
        v-model="config[timeKey]"
        class="fixed-clock"
      />
      <div v-else class="relative-clock">
        <div class="clock-inputs">
          <label v-for="unit in units" :key="unit">
            <el-input-number
              :model-value="numberValue(offset[unit])"
              @update:model-value="value => (offset[unit] = value)"
              controls-position="right"
              :min="-Infinity"
              :max="Infinity"
              :aria-label="t(`dynamic_time.${unit}`)"
            />
            <span>{{ t(`dynamic_time.${unit}`) }}</span>
          </label>
          <el-select v-model="offset.around" :aria-label="t('dynamic_time.clock_direction')">
            <el-option value="f" :label="t('dynamic_time.before')" />
            <el-option value="b" :label="t('dynamic_time.after')" />
          </el-select>
        </div>
        <div class="clock-hint">{{ t('dynamic_time.offset_hint') }}</div>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.clock-offset-editor {
  width: 100%;
  .clock-row {
    display: flex;
    align-items: center;
    margin-top: 8px;
    gap: 8px;
  }
  .clock-label {
    width: 80px;
    flex-shrink: 0;
  }
  .fixed-clock,
  .relative-clock {
    flex: 1;
    min-width: 0;
  }
  :deep(.fixed-clock.ed-date-editor.ed-input) {
    width: 100%;
    flex: 1;
  }
  .clock-inputs {
    display: flex;
    gap: 6px;
    label {
      display: flex;
      flex: 1;
      min-width: 0;
      align-items: center;
      gap: 4px;
    }
    .ed-input-number {
      width: 100%;
      min-width: 0;
      :deep(.ed-input__wrapper) {
        padding-left: 6px;
        padding-right: 36px;
      }
    }
    .ed-select {
      width: 64px;
      flex-shrink: 0;
    }
    span {
      white-space: nowrap;
    }
  }
  .clock-hint {
    margin-top: 4px;
    font-size: 12px;
    line-height: 18px;
    color: #8f959e;
  }
}
</style>
