<script lang="ts" setup>
import { ref, reactive, computed, watch, onBeforeMount } from 'vue'
import { propTypes } from '@/utils/propTypes'
import { useI18n } from '@/hooks/web/useI18n'
import type { Corn } from './Hour.vue'
const props = defineProps({
  modelValue: propTypes.string.def('?'),
  label: propTypes.string.def('')
})

const { t } = useI18n()

const type = ref('1')
const work = ref<string | number>(0)
const last = ref<string | number>(0)
const state = reactive<Corn>({
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
    case '2': // 年期
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
  () => props.modelValue,
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
  if (!props.modelValue) {
    return
  }
  if (props.modelValue === '?') {
    type.value = '5'
  } else if (props.modelValue.indexOf('-') !== -1) {
    // 2周期
    if (props.modelValue.split('-').length === 2) {
      type.value = '2'
      state.cycle.start = props.modelValue.split('-')[0] as unknown as number
      state.cycle.end = props.modelValue.split('-')[1] as unknown as number
    }
  } else if (props.modelValue.indexOf('/') !== -1) {
    // 3循环
    if (props.modelValue.split('/').length === 2) {
      type.value = '3'
      state.loop.start = props.modelValue.split('/')[0] as unknown as number
      state.loop.end = props.modelValue.split('/')[1] as unknown as number
    }
  } else if (props.modelValue.indexOf('*') !== -1) {
    // 1每
    type.value = '1'
  } else if (props.modelValue.indexOf('L') !== -1) {
    // 6最后
    type.value = '6'
    last.value = props.modelValue.replace('L', '')
  } else if (props.modelValue.indexOf('#') !== -1) {
    // 7指定周
    if (props.modelValue.split('#').length === 2) {
      type.value = '7'
      state.week.start = props.modelValue.split('#')[0]
      state.week.end = props.modelValue.split('#')[1]
    }
  } else if (props.modelValue.indexOf('W') !== -1) {
    // 8工作日
    type.value = '8'
    work.value = props.modelValue.replace('W', '')
  } else {
    // *
    type.value = '4'
    state.appoint = props.modelValue.split(',')
  }
}

const emits = defineEmits(['update:modelValue'])
</script>

<template>
  <div>
    <div>
      <el-radio v-model="type" label="1" size="small" border
        >{{ t('cron.every') }}{{ label }}</el-radio
      >
    </div>
    <div>
      <el-radio v-model="type" label="2" size="small" border>{{ t('cron.cycle') }}</el-radio>
      <span style="margin-left: 10px; margin-right: 5px">{{ t('cron.from') }}</span>
      <el-input-number
        v-model="state.cycle.start"
        :min="1"
        :max="59"
        size="small"
        style="width: 100px"
        @change="type = '2'"
      />
      <span style="margin-left: 5px; margin-right: 5px">{{ t('cron.to') }}</span>
      <el-input-number
        v-model="state.cycle.end"
        :min="2"
        :max="59"
        size="small"
        style="width: 100px"
        @change="type = '2'"
      />
      {{ label }}
    </div>
    <div>
      <el-radio v-model="type" label="3" size="small" border>{{ t('cron.repeat') }}</el-radio>
      <span style="margin-left: 10px; margin-right: 5px">{{ t('cron.from') }}</span>
      <el-input-number
        v-model="state.loop.start"
        :min="0"
        :max="59"
        size="small"
        style="width: 100px"
        @change="type = '3'"
      />
      <span style="margin-left: 5px; margin-right: 5px"
        >{{ label }}{{ t('cron.every_begin') }}</span
      >
      <el-input-number
        v-model="state.loop.end"
        :min="1"
        :max="59"
        size="small"
        style="width: 100px"
        @change="type = '3'"
      />
      {{ label }}{{ t('cron.every_exec') }}
    </div>
    <div>
      <el-radio v-model="type" label="4" size="small" border>{{ t('cron.set') }}</el-radio>
      <el-checkbox-group v-model="state.appoint">
        <div v-for="i in 6" :key="i" style="margin-left: 10px; line-height: 25px">
          <el-checkbox
            v-for="j in 10"
            :key="j"
            :label="i - 1 + '' + (j - 1)"
            @change="type = '4'"
          />
        </div>
      </el-checkbox-group>
    </div>
  </div>
</template>

<style lang="less" scoped></style>
