<script lang="ts" setup>
import { ref, toRefs, provide, PropType, onBeforeMount, shallowRef, watch, nextTick } from 'vue'
import { getEnumValue } from '@/api/dataset'
import { cloneDeep, debounce } from 'lodash-es'

interface SelectConfig {
  selectValue: any
  defaultValue: any
  temporaryValue: any
  checkedFieldsMap: object
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
        temporaryValue: '',
        defaultValueCheck: false,
        optionValueSource: 0,
        multiple: false,
        checkedFieldsMap: {}
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

const selectValue = ref()
const loading = ref(false)
const multiple = ref(false)
const options = shallowRef([])

const handleValueChange = () => {
  const value = Array.isArray(selectValue.value) ? [...selectValue.value] : selectValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(selectValue.value)
      ? [...selectValue.value]
      : selectValue.value
    config.value.temporaryValue = value
    return
  }
  config.value.defaultValue = value
}

const handleFieldIdChange = (val: string[]) => {
  loading.value = true
  getEnumValue(val)
    .then(res => {
      options.value = (res || []).map(ele => {
        return {
          label: ele,
          value: ele
        }
      })
    })
    .finally(() => {
      loading.value = false
    })
}
const visibleChange = (val: boolean) => {
  setTimeout(() => {
    loading.value = !val
  }, 50)
}

watch(
  () => config.value.selectValue,
  val => {
    if (!props.isConfig) return
    selectValue.value = Array.isArray(val) ? [...val] : val
    nextTick(() => {
      multiple.value = config.value.multiple
    })
  }
)

watch(
  () => config.value.multiple,
  val => {
    if (props.isConfig) return
    selectValue.value = val ? [] : ''
    nextTick(() => {
      multiple.value = val
    })
  }
)

watch(
  () => config.value.field.id,
  val => {
    if (!val) return
    debounceOptions(1)
  }
)

watch(
  () => config.value.optionValueSource,
  val => {
    debounceOptions(val)
  }
)

watch(
  [() => config.value.checkedFields, () => config.value.checkedFieldsMap],
  () => {
    debounceOptions(0)
  },
  {
    deep: true
  }
)

watch(
  () => config.value.valueSource,
  () => {
    debounceOptions(2)
  }
)

const setOptions = (num: number) => {
  if (num !== config.value.optionValueSource) return
  const { optionValueSource, checkedFieldsMap, checkedFields, field, valueSource } = config.value
  switch (optionValueSource) {
    case 0:
      const arr = Object.values(checkedFieldsMap).filter(ele => !!ele) as string[]
      if (!!checkedFields.length && !!arr.length) {
        handleFieldIdChange(checkedFields.map(ele => checkedFieldsMap[ele]).filter(ele => !!ele))
      } else {
        options.value = []
      }
      break
    case 1:
      if (field.id) {
        handleFieldIdChange([field.id])
      } else {
        options.value = []
      }
      break
    case 2:
      options.value = cloneDeep(
        (valueSource || []).map(ele => {
          return {
            label: ele,
            value: ele
          }
        })
      )
      break
    default:
      break
  }
}

const debounceOptions = debounce(setOptions, 300)

onBeforeMount(() => {
  const { defaultValueCheck, multiple: plus, defaultValue, optionValueSource } = config.value
  if (defaultValueCheck) {
    config.value.selectValue = Array.isArray(defaultValue) ? [...defaultValue] : defaultValue
    selectValue.value = Array.isArray(defaultValue) ? [...defaultValue] : defaultValue
  } else {
    config.value.selectValue = plus ? [] : ''
    selectValue.value = plus ? [] : ''
  }
  nextTick(() => {
    multiple.value = config.value.multiple
  })
  debounceOptions(optionValueSource)
})
</script>

<template>
  <el-select-v2
    v-if="multiple"
    key="multiple"
    v-model="selectValue"
    filterable
    @change="handleValueChange"
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    multiple
    show-checked
    clearable
    collapse-tags
    :options="options"
    collapse-tags-tooltip
  >
  </el-select-v2>
  <el-select-v2
    v-else
    v-model="selectValue"
    key="single"
    @change="handleValueChange"
    clearable
    filterable
    @visible-change="visibleChange"
    :popper-class="loading ? 'load-select' : ''"
    :options="options"
  >
    <template #default="{ item }">
      <el-radio-group v-model="selectValue">
        <el-radio :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
</template>
