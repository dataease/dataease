<script lang="ts" setup>
import { ref, reactive, computed, watch, onBeforeMount } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
const props = defineProps({
  value: propTypes.string.def('?')
})
const { t } = useI18n()

const type = ref('5')
const work = ref(0)
const last = ref(0)
const state = reactive({
  cycle: {
    // 周期
    start: 0,
    end: 0
  },
  loop: {
    // 循环
    start: 0,
    end: 0
  },
  week: {
    // 指定周
    start: 0,
    end: 0
  },
  appoint: []
})

const resultValue = computed(() => {
  const result = []
  switch (type.value) {
    case '1': // 每秒
      result.push('*')
      break
    case '2': // 周期
      result.push(`${state.cycle.start}-${state.cycle.end}`)
      break
    case '3': // 循环
      result.push(`${state.loop.start}/${state.loop.end}`)
      break
    case '4': // 指定
      result.push(state.appoint.join(','))
      break
    case '6': // 最后
      result.push(`${last.value === 0 ? '' : last.value}L`)
      break
    case '7': // 指定周
      result.push(`${state.week.start}#${state.week.end}`)
      break
    case '8': // 工作日
      result.push(`${work.value}W`)
      break
    default: // 不指定
      result.push('?')
      break
  }
  return result.join('')
})

onBeforeMount(() => {
  updateVal()
})

watch(
  () => props.value,
  () => {
    updateVal()
  }
)

watch(
  () => resultValue.value,
  () => {
    emits('update:modelValue', resultValue.value)
  }
)

const updateVal = () => {
  if (!props.value) {
    return
  }
  if (props.value === '?') {
    type.value = '5'
  } else if (props.value.indexOf('-') !== -1) {
    // 2周期
    if (props.value.split('-').length === 2) {
      type.value = '2'
      state.cycle.start = props.value.split('-')[0]
      state.cycle.end = props.value.split('-')[1]
    }
  } else if (props.value.indexOf('/') !== -1) {
    // 3循环
    if (props.value.split('/').length === 2) {
      type.value = '3'
      state.loop.start = props.value.split('/')[0]
      state.loop.end = props.value.split('/')[1]
    }
  } else if (props.value.indexOf('*') !== -1) {
    // 1每
    type.value = '1'
  } else if (props.value.indexOf('L') !== -1) {
    // 6最后
    type.value = '6'
    last.value = props.value.replace('L', '')
  } else if (props.value.indexOf('#') !== -1) {
    // 7指定周
    if (props.value.split('#').length === 2) {
      type.value = '7'
      state.week.start = props.value.split('#')[0]
      state.week.end = props.value.split('#')[1]
    }
  } else if (props.value.indexOf('W') !== -1) {
    // 8工作日
    type.value = '8'
    work.value = props.value.replace('W', '')
  } else {
    // *
    type.value = '4'
    state.appoint = props.value.split(',')
  }
}

const emits = defineEmits(['update:modelValue'])
</script>

<template>
  <div>
    <div>
      <el-radio v-model="type" label="1" size="small" border>{{ t('cron.every_day') }}</el-radio>
    </div>
    <div>
      <el-radio v-model="type" label="5" size="small" border>{{ t('cron.not_set') }}</el-radio>
    </div>
    <div>
      <el-radio v-model="type" label="2" size="small" border>{{ t('cron.cycle') }}</el-radio>
      <span style="margin-left: 10px; margin-right: 5px">{{ t('cron.from') }}</span>
      <el-input-number
        v-model="state.cycle.start"
        :min="1"
        :max="31"
        size="small"
        style="width: 100px"
        @change="type = '2'"
      />
      <span style="margin-left: 5px; margin-right: 5px">{{ t('cron.to') }}</span>
      <el-input-number
        v-model="state.cycle.end"
        :min="2"
        :max="31"
        size="small"
        style="width: 100px"
        @change="type = '2'"
      />
      {{ t('cron.day') }}
    </div>
    <div>
      <el-radio v-model="type" label="3" size="small" border>{{ t('cron.repeat') }}</el-radio>
      <span style="margin-left: 10px; margin-right: 5px">{{ t('cron.from') }}</span>
      <el-input-number
        v-model="state.loop.start"
        :min="1"
        :max="31"
        size="small"
        style="width: 100px"
        @change="type = '3'"
      />
      <span style="margin-left: 5px; margin-right: 5px">{{ t('cron.day_begin') }}</span>
      <el-input-number
        v-model="state.loop.end"
        :min="1"
        :max="31"
        size="small"
        style="width: 100px"
        @change="type = '3'"
      />
      {{ t('cron.day_exec') }}
    </div>
    <div>
      <el-radio v-model="type" label="8" size="small" border>{{ t('cron.work_day') }}</el-radio>
      <span style="margin-left: 10px; margin-right: 5px">{{ t('cron.this_month') }}</span>
      <el-input-number
        v-model="work"
        :min="1"
        :max="31"
        size="small"
        style="width: 100px"
        @change="type = '8'"
      />
      {{ t('cron.day_near_work_day') }}
    </div>
    <div>
      <el-radio v-model="type" label="6" size="small" border>{{
        t('cron.this_week_last_day')
      }}</el-radio>
    </div>
    <div>
      <el-radio v-model="type" label="4" size="small" border>{{ t('cron.set') }}</el-radio>
      <el-checkbox-group v-model="state.appoint">
        <div v-for="i in 4" :key="i" style="margin-left: 10px; line-height: 25px">
          <template v-for="j in 10">
            <el-checkbox
              v-if="parseInt(i - 1 + '' + (j - 1), 10) < 32 && !(i === 1 && j === 1)"
              :key="j"
              :label="i - 1 + '' + (j - 1)"
              @change="type = '4'"
            />
          </template>
        </div>
      </el-checkbox-group>
    </div>
  </div>
</template>

<style lang="less" scoped></style>
