<script lang="ts" setup>
import { computed, ref, watch, toRefs } from 'vue'
import dayjs from 'dayjs'
import { Calendar } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  DynamicTimeError,
  evaluateDynamicDefault,
  isCustomClockDefault
} from './dynamic-default-time'

const props = defineProps({
  config: { type: Object, required: true },
  displayType: { type: String, required: true }
})
const { t } = useI18n()
const { config } = toRefs(props)
const baseline = ref(new Date())
const value = ref<Date | [Date, Date]>()
const error = ref('')
const rule = computed(() => {
  const c = config.value
  return {
    displayType: props.displayType,
    timeGranularity: c.timeGranularity,
    timeGranularityMultiple: c.timeGranularityMultiple,
    relativeToCurrent: c.relativeToCurrent,
    relativeToCurrentRange: c.relativeToCurrentRange,
    timeNum: c.timeNum,
    relativeToCurrentType: c.relativeToCurrentType,
    around: c.around,
    arbitraryTime: c.arbitraryTime,
    timeNumRange: c.timeNumRange,
    relativeToCurrentTypeRange: c.relativeToCurrentTypeRange,
    aroundRange: c.aroundRange,
    arbitraryTimeRange: c.arbitraryTimeRange,
    clockOffset: c.clockOffset,
    clockOffsetRange: c.clockOffsetRange
  }
})
watch(
  [rule, () => config.value.id, () => config.value.defaultValueCheck],
  () => {
    baseline.value = new Date()
    error.value = ''
    value.value = undefined
    if (config.value.defaultValueCheck) {
      try {
        value.value = evaluateDynamicDefault(rule.value, baseline.value, 'config')
      } catch (e) {
        if (!(e instanceof DynamicTimeError)) throw e
        error.value = e.key
      }
    }
    config.value.defaultValue = value.value
    config.value.selectValue = value.value
  },
  { immediate: true, deep: true }
)
</script>

<template>
  <div class="dynamic-default-preview">
    <template v-if="displayType === '7' && isCustomClockDefault(rule)">
      <div
        v-for="(label, index) in ['datasource.start_time', 'datasource.end_time']"
        :key="label"
        class="range-preview"
      >
        <div class="endpoint-label">{{ t(label) }}</div>
        <el-date-picker
          disabled
          :model-value="value?.[index]"
          type="datetime"
          :prefix-icon="Calendar"
          :placeholder="t(label)"
        />
      </div>
    </template>
    <el-date-picker
      v-else
      disabled
      :model-value="value"
      :type="displayType === '7' ? config.timeGranularityMultiple : config.timeGranularity"
      :format="config.timeGranularityMultiple === 'yearrange' ? 'YYYY' : undefined"
      :prefix-icon="Calendar"
      :range-separator="t('cron.to')"
      :start-placeholder="t('datasource.start_time')"
      :end-placeholder="t('datasource.end_time')"
      :placeholder="t('commons.date.select_date_time')"
    />
    <div v-if="isCustomClockDefault(rule)" class="baseline">
      {{ t('dynamic_time.baseline') }}{{ dayjs(baseline).format('YYYY-MM-DD HH:mm:ss') }}
    </div>
    <div v-if="error" class="error" role="alert">{{ t(error) }}</div>
  </div>
</template>

<style lang="less" scoped>
.dynamic-default-preview {
  width: 325px;
  max-width: 100%;
  :deep(.ed-date-editor) {
    width: 100%;
  }
  .range-preview + .range-preview {
    margin-top: 8px;
  }
  .endpoint-label {
    font-size: 12px;
    margin-bottom: 4px;
    color: #646a73;
  }
  .baseline,
  .error {
    margin-top: 4px;
    font-size: 12px;
    line-height: 18px;
    white-space: normal;
  }
  .baseline {
    color: #8f959e;
  }
  .error {
    color: var(--ed-color-danger);
  }
}
</style>
