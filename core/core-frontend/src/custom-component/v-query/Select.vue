<script lang="ts" setup>
import {
  ref,
  toRefs,
  provide,
  PropType,
  onBeforeMount,
  shallowRef,
  watch,
  nextTick,
  computed
} from 'vue'
import { getEnumValue } from '@/api/dataset'
import { cloneDeep, debounce } from 'lodash-es'

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
    return
  }

  config.value.defaultValue = value
}

watch(
  () => config.value.displayType,
  () => {
    if (!props.isConfig) return
    config.value.defaultValue = config.value.multiple ? [] : ''
    selectValue.value = config.value.multiple ? [] : ''
  }
)

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
      selectValue.value = Array.isArray(selectValue.value)
        ? [...selectValue.value]
        : selectValue.value
      loading.value = false
    })
}

const visible = ref(false)
const visibleChange = (val: boolean) => {
  setTimeout(() => {
    visible.value = !val
  }, 50)
}

watch(
  () => config.value.defaultValue,
  val => {
    if (config.value.multiple) {
      selectValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = config.value.multiple
    })
  }
)

watch(
  () => config.value.id,
  () => {
    init()
  }
)

watch(
  () => config.value.selectValue,
  val => {
    if (props.isConfig) return
    if (config.value.multiple) {
      selectValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = config.value.multiple
      if (!config.value.multiple) {
        selectValue.value = Array.isArray(config.value.selectValue)
          ? [...config.value.selectValue]
          : config.value.selectValue
      }
    })
  }
)

watch(
  () => config.value.multiple,
  val => {
    if (!props.isConfig) return
    if (val) {
      selectValue.value = []
    }
    nextTick(() => {
      multiple.value = val
      if (!val) {
        nextTick(() => {
          selectValue.value = ''
        })
      }
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
    debounceOptions(config.value.optionValueSource)
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

const init = () => {
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
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: '227px' }
})

onBeforeMount(() => {
  init()
})
</script>

<template>
  <el-select-v2
    v-if="multiple"
    key="multiple"
    v-model="selectValue"
    v-loading="loading"
    filterable
    @change="handleValueChange"
    @visible-change="visibleChange"
    :popper-class="
      visible ? 'load-select filter-select-popper_class' : 'filter-select-popper_class'
    "
    multiple
    show-checked
    clearable
    radio
    :style="selectStyle"
    collapse-tags
    :options="options"
    collapse-tags-tooltip
  ></el-select-v2>
  <el-select-v2
    v-else
    v-model="selectValue"
    key="single"
    v-loading="loading"
    @change="handleValueChange"
    clearable
    :style="selectStyle"
    filterable
    radio
    @visible-change="visibleChange"
    :popper-class="
      visible ? 'load-select filter-select-popper_class' : 'filter-select-popper_class'
    "
    :options="options"
  >
    <template #default="{ item }">
      <el-radio-group v-model="selectValue">
        <el-radio :title="item.label" :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
</template>

<style lang="less">
.filter-select-popper_class {
  .ed-select-dropdown__option-item {
    .ed-radio-group,
    .ed-checkbox {
      width: 100%;
      .ed-radio {
        width: 100%;
      }
      .ed-radio__label,
      .ed-checkbox__label {
        width: calc(100% - 16px);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }
    }
  }
}
</style>
