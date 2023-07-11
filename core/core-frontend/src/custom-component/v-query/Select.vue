<script lang="ts" setup>
import { ref, toRefs, provide, PropType } from 'vue'
const loading = ref(false)

interface SelectConfig {
  selectValue: any
  defaultValue: any
  temporaryValue: any
  multiple: boolean
  options?: Array<{
    label: string
    value: string
  }>
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
        temporaryValue: '',
        multiple: false,
        options: []
      }
    }
  },
  isConfig: {
    type: Boolean,
    default: false
  },
  customStyle: {
    type: Object as PropType<{
      border: string
      background: string
      text: string
    }>,
    default: () => ({
      border: '',
      background: '',
      text: ''
    })
  }
})
const { config, customStyle } = toRefs(props)

provide('$custom-style-filter', customStyle)

const handleValueChange = () => {
  const value = Array.isArray(config.value.selectValue)
    ? [...config.value.selectValue]
    : config.value.selectValue
  if (!props.isConfig) {
    config.value.temporaryValue = value
    return
  }
  config.value.defaultValue = value
}
const visibleChange = (val: boolean) => {
  setTimeout(() => {
    loading.value = !val
  }, 50)
}
</script>

<template>
  <el-select-v2
    v-if="config.multiple"
    key="multiple"
    v-model="config.selectValue"
    filterable
    @change="handleValueChange"
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    multiple
    show-checked
    collapse-tags
    :options="config.options"
    collapse-tags-tooltip
  >
  </el-select-v2>
  <el-select-v2
    v-else
    v-model="config.selectValue"
    key="single"
    @change="handleValueChange"
    filterable
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    :options="config.options"
  >
    <template #default="{ item }">
      <el-radio-group v-model="config.selectValue">
        <el-radio :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
</template>
