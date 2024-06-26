<script lang="ts" setup>
import { provide, PropType } from 'vue'
import Select from './Select.vue'
import Time from './Time.vue'
import TextSearch from './TextSearch.vue'
import Tree from './Tree.vue'

interface SelectConfig {
  selectValue: any
  defaultValue: any
  checkedFieldsMap: object
  displayType: string
  id: string
  checkedFields: string[]
  field: {
    id: string
  }
  optionValueSource: number
  defaultValueCheck: boolean
  multiple: boolean
  valueSource: {
    label: string
    value: string
  }[]
}

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
        displayType: '',
        defaultValueCheck: false,
        optionValueSource: 0,
        multiple: false,
        checkedFieldsMap: {}
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
const filterTypeCom = (displayType: string) => {
  if (displayType === '8') {
    return TextSearch
  }
  return ['1', '7'].includes(displayType) ? Time : displayType === '9' ? Tree : Select
}
provide('$custom-style-filter', props.customStyle)
</script>

<template>
  <component
    :config="config"
    :is-config="false"
    :is="filterTypeCom(config.displayType)"
  ></component>
</template>
