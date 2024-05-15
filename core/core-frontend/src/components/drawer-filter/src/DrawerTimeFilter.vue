<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { computed, PropType, reactive, toRefs, h } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

interface Config {
  // 显示类型
  showType: string
  // 日期分隔符
  rangeSeparator: string
  // 开始日期label
  startPlaceholder: string
  // 结束日期label
  endPlaceholder: string
  // 日期格式
  format: string
  // 日期值格式
  valueFormat: string
  // 尺寸
  size: string
  // 弹出位置
  placement: string
}

const props = defineProps({
  property: Object as PropType<Config>,
  title: propTypes.string
})
const { property } = toRefs(props)
const timeConfig = computed(() => {
  let obj = Object.assign(
    {
      showType: 'datetime',
      rangeSeparator: '-',
      startPlaceholder: t('datasource.start_time'),
      endPlaceholder: t('datasource.end_time'),
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      size: 'default',
      placement: 'bottom-end'
    },
    property.value
  )
  return obj
})
const state = reactive({
  modelValue: []
})

const emits = defineEmits(['filter-change'])
const onChange = () => {
  emits('filter-change', state.modelValue)
}
const clear = () => {
  state.modelValue = []
}
defineExpose({
  clear
})
</script>

<template>
  <div class="filter">
    <span>{{ title }}</span>
    <div class="filter-item">
      <el-date-picker
        v-model="state.modelValue"
        :type="timeConfig.showType"
        :range-separator="timeConfig.rangeSeparator"
        :start-placeholder="timeConfig.startPlaceholder"
        :end-placeholder="timeConfig.endPlaceholder"
        :format="timeConfig.format"
        :value-format="timeConfig.valueFormat"
        key="drawer-time-filt"
        :size="timeConfig.size"
        @change="onChange"
        :placement="timeConfig.placement"
      />
    </div>
  </div>
</template>
<style lang="less" scope>
.filter {
  display: flex;
  align-items: center;
  min-height: 32px;

  > :nth-child(1) {
    color: var(--deTextSecondary, #1f2329);
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 24px;
    white-space: nowrap;
    width: 116px;
    // margin-top: 5px;
  }

  .filter-item {
    flex: 1;
    .ed-date-editor {
      width: 100%;
    }
  }
}
</style>
