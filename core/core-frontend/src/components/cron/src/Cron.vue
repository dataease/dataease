<script lang="ts" setup>
import { reactive, computed, watch, onBeforeMount } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
import { ElMessage } from 'element-plus-secondary'
import SecondAndMinute from './SecondAndMinute.vue'
import hour from './Hour.vue'
import day from './Day.vue'
import month from './Month.vue'
import week from './Week.vue'
import year from './Year.vue'
const props = defineProps({
  modelValue: propTypes.string.def('?'),
  isRate: propTypes.bool.def(false)
})

const { t } = useI18n()

const state = reactive({
  activeName: 's',
  sVal: '',
  mVal: '',
  hVal: '',
  dVal: '',
  monthVal: '',
  weekVal: '',
  yearVal: ''
})

const tableData = computed(() => {
  return [
    {
      sVal: state.sVal,
      mVal: state.mVal,
      hVal: state.hVal,
      dVal: state.dVal,
      monthVal: state.monthVal,
      weekVal: state.weekVal,
      yearVal: state.yearVal
    }
  ]
})

const resultValue = computed(() => {
  if (!state.dVal && !state.weekVal) {
    return ''
  }
  if (state.dVal === '?' && state.weekVal === '?') {
    ElMessage.error(t('cron.d_w_cant_not_set'))
  }
  if (state.dVal !== '?' && state.weekVal !== '?') {
    ElMessage.error(t('cron.d_w_must_one_set'))
  }
  const v = `${state.sVal} ${state.mVal} ${state.hVal} ${state.dVal} ${state.monthVal} ${state.weekVal} ${state.yearVal}`
  return v
})

onBeforeMount(() => {
  updateVal()
})

watch(
  () => props.modelValue,
  () => {
    if (!props.isRate) return
    updateVal()
  },
  {
    immediate: true
  }
)

watch(
  () => resultValue.value,
  () => {
    emits('update:modelValue', resultValue.value)
  }
)

const updateVal = () => {
  if (!props.modelValue) {
    return
  }
  const arrays = props.modelValue.split(' ')
  state.sVal = arrays[0]
  state.mVal = arrays[1]
  state.hVal = arrays[2]
  state.dVal = arrays[3]
  state.monthVal = arrays[4]
  state.weekVal = arrays[5]
  state.yearVal = arrays[6]
}

const emits = defineEmits(['update:modelValue'])
</script>

<template>
  <div>
    <el-tabs v-model="state.activeName">
      <el-tab-pane :label="t('cron.second')" name="s">
        <second-and-minute v-model="state.sVal" :label="t('cron.second')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.minute')" name="m">
        <second-and-minute v-model="state.mVal" :label="t('cron.minute')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.hour')" name="h">
        <hour v-model="state.hVal" :label="t('cron.hour')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.day')" name="d">
        <day v-model="state.dVal" :label="t('cron.day')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.month')" name="month">
        <month v-model="state.monthVal" :label="t('cron.month')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.week')" name="week">
        <week v-model="state.weekVal" :label="t('cron.week')" />
      </el-tab-pane>
      <el-tab-pane :label="t('cron.year')" name="year">
        <year v-model="state.yearVal" :label="t('cron.year')" />
      </el-tab-pane>
    </el-tabs>
    <!-- table -->
    <el-table
      header-cell-class-name="header-cell"
      :data="tableData"
      size="mini"
      border
      style="width: 100%"
    >
      <el-table-column prop="sVal" :label="t('cron.second')" width="70" />
      <el-table-column prop="mVal" :label="t('cron.minute')" width="70" />
      <el-table-column prop="hVal" :label="t('cron.hour')" width="70" />
      <el-table-column prop="dVal" :label="t('cron.day')" width="70" />
      <el-table-column prop="monthVal" :label="t('cron.month')" width="70" />
      <el-table-column prop="weekVal" :label="t('cron.week')" width="70" />
      <el-table-column prop="yearVal" :label="t('cron.year')" />
    </el-table>
  </div>
</template>
