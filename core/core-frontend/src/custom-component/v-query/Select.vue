<script lang="ts" setup>
import { ref, toRefs, provide, PropType } from 'vue'

const loading = ref(false)

interface SelectConfig {
  selectValue: any
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
        multiple: false,
        options: []
      }
    }
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
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    multiple
    collapse-tags
    :options="config.options"
    collapse-tags-tooltip
    style="width: 240px"
  >
  </el-select-v2>
  <el-select-v2
    v-else
    v-model="config.selectValue"
    key="single"
    filterable
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    :options="config.options"
    style="width: 240px"
  >
    <template #default="{ item }">
      <el-radio-group v-model="config.selectValue">
        <el-radio :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
</template>
