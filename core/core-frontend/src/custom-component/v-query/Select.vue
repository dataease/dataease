<script lang="ts" setup>
import {
  ref,
  toRefs,
  PropType,
  onBeforeMount,
  shallowRef,
  watch,
  nextTick,
  computed,
  inject,
  onUnmounted,
  Ref
} from 'vue'
import { enumValueObj, type EnumValue, getEnumValue } from '@/api/dataset'
import { cloneDeep, debounce } from 'lodash-es'
import { useEmitt } from '@/hooks/web/useEmitt'

interface SelectConfig {
  selectValue: any
  defaultMapValue: any
  mapValue: any
  defaultValue: any
  checkedFieldsMap: object
  displayType: string
  showEmpty: boolean
  id: string
  resultMode: number
  displayId: string
  sort: string
  sortId: string
  checkedFields: string[]
  dataset: {
    id: string
  }
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
        resultMode: 0,
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
  }
})
const { config } = toRefs(props)
let enumValueArr = []
const selectValue = ref()
const loading = ref(false)
const multiple = ref(false)
const options = shallowRef([])
const unMountSelect: Ref = inject('unmount-select')
const releaseSelect = inject('release-unmount-select', Function, true)
const queryDataForId = inject('query-data-for-id', Function, true)
const isConfirmSearch = inject('is-confirm-search', Function, true)
const queryConditionWidth = inject('com-width', Function, true)
const cascadeList = inject('cascade-list', Function, true)
const setCascadeDefault = inject('set-cascade-default', Function, true)

const cascade = computed(() => {
  return cascadeList() || []
})

const setDefaultMapValue = arr => {
  const { displayId, field } = config.value
  if (config.value.optionValueSource !== 1) {
    return []
  }
  let defaultMapValue = {}
  let defaultValue = []
  arr.forEach(ele => {
    defaultMapValue[ele] = []
  })
  enumValueArr.forEach(ele => {
    if (defaultMapValue[ele[displayId || field?.id]]) {
      defaultMapValue[ele[displayId || field?.id]].push(ele[field?.id])
    }
  })
  Object.values(defaultMapValue).forEach(ele => {
    defaultValue = [...new Set([...defaultValue, ...(ele as unknown as string[])])]
  })
  return defaultValue
}

onUnmounted(() => {
  enumValueArr = []
})

const setCascadeValueBack = val => {
  cascade.value.forEach(ele => {
    ele.forEach(item => {
      if (item.datasetId.split('--')[1] === config.value.id) {
        if (props.isConfig) {
          item.selectValue = Array.isArray(val) ? [...val] : val
        }
        item.currentSelectValue = Array.isArray(val) ? [...val] : val
      }
    })
  })
}

const emitCascade = () => {
  cascade.value.forEach(ele => {
    let trigger = false
    ele.forEach(item => {
      if (item.datasetId.split('--')[1] === config.value.id) {
        trigger = true
      } else if (trigger) {
        useEmitt().emitter.emit(`${item.datasetId.split('--')[1]}-select`)
        trigger = false
      }
    })
  })
}

const emitCascadeConfig = () => {
  const arr = []
  cascade.value.forEach(ele => {
    let trigger = false
    ele.forEach(item => {
      if (item.datasetId.split('--')[1] === config.value.id) {
        trigger = true
      } else if (trigger) {
        arr.push(item.datasetId.split('--')[1])
        trigger = false
      }
    })
  })
  return arr
}

const getCascadeFieldId = () => {
  const filter = []
  cascade.value.forEach(ele => {
    let condition = null
    ele.forEach(item => {
      const [_, queryId, fieldId] = item.datasetId.split('--')
      if (queryId === config.value.id && condition) {
        if (item.fieldId) {
          condition.fieldId = item.fieldId
        }
        filter.push(condition)
      } else {
        if (props.isConfig) {
          if (!!item.selectValue?.length) {
            condition = {
              fieldId: fieldId,
              operator: 'in',
              value: [...item.selectValue]
            }
          }
        } else {
          if (!!item.currentSelectValue?.length) {
            condition = {
              fieldId: fieldId,
              operator: 'in',
              value: [...item.currentSelectValue]
            }
          }
        }
      }
    })
  })
  return filter
}

const handleValueChange = () => {
  const value = Array.isArray(selectValue.value) ? [...selectValue.value] : selectValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(selectValue.value)
      ? [...selectValue.value]
      : selectValue.value
    config.value.mapValue = setDefaultMapValue(
      Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
    )
    setCascadeValueBack(config.value.mapValue)
    emitCascade()
    nextTick(() => {
      isConfirmSearch(config.value.id)
    })
    return
  }

  setCascadeDefault(emitCascadeConfig())

  config.value.defaultValue = value
  config.value.mapValue = setDefaultMapValue(
    Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
  )
  config.value.defaultMapValue = setDefaultMapValue(
    Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
  )
  setCascadeValueBack(config.value.mapValue)
}

const displayTypeChange = () => {
  if (!props.isConfig) return
  config.value.defaultValue = config.value.multiple ? [] : undefined
  selectValue.value = config.value.multiple ? [] : undefined
}

const handleFieldIdDefaultChange = (val: string[]) => {
  loading.value = true
  getEnumValue({
    fieldIds: val,
    resultMode: config.value.resultMode || 0
  })
    .then(res => {
      options.value = (res || [])
        .filter(ele => ele !== null)
        .map(ele => {
          return {
            label: `${ele}`,
            value: `${ele}`
          }
        })
    })
    .finally(() => {
      loading.value = false
      if (config.value.defaultValueCheck) {
        selectValue.value = Array.isArray(config.value.defaultValue)
          ? [...config.value.defaultValue]
          : config.value.defaultValue
      } else {
        selectValue.value = Array.isArray(selectValue.value)
          ? [...selectValue.value]
          : selectValue.value
      }
      setEmptyData()
    })
}

const setOldMapValue = arr => {
  const { displayId } = config.value
  if (!displayId) {
    return []
  }
  let defaultMapValue = {}
  let defaultValue = []
  arr.forEach(ele => {
    defaultMapValue[ele] = []
  })
  enumValueArr.forEach(ele => {
    if (defaultMapValue[ele[displayId]]) {
      defaultMapValue[ele[displayId]].push(ele)
    }
  })
  Object.values(defaultMapValue).forEach(ele => {
    defaultValue = [...defaultValue, ...(ele as unknown as string[])]
  })
  return defaultValue
}

const handleFieldIdChange = (val: EnumValue) => {
  loading.value = true
  enumValueObj(val)
    .then(res => {
      let oldArr = []
      let oldEnumValueArr = []
      if (selectValue.value?.length && config.value.multiple) {
        oldArr = [...selectValue.value]
        oldEnumValueArr = setOldMapValue(oldArr)
      }
      enumValueArr = [...(res || []), ...oldEnumValueArr] || []
      options.value = [
        ...new Set(
          (res || [])
            .map(ele => {
              return `${ele[val.displayId || val.queryId]}`
            })
            .concat(oldArr)
        )
      ].map(ele => {
        return {
          label: `${ele}`,
          value: `${ele}`,
          checked: oldArr.includes(ele)
        }
      })
    })
    .finally(() => {
      loading.value = false
      if (isFromRemote.value) return
      if (config.value.defaultValueCheck) {
        selectValue.value = Array.isArray(config.value.defaultValue)
          ? [...config.value.defaultValue]
          : config.value.defaultValue
        let shouldReSearch = false
        if (unMountSelect.value.includes(config.value.id)) {
          const mapValue = setDefaultMapValue(
            Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
          )
          if (mapValue?.length !== config.value.defaultMapValue?.length) {
            shouldReSearch = true
          } else if (!mapValue.every(value => config.value.defaultMapValue.includes(value))) {
            shouldReSearch = true
          }
          releaseSelect(config.value.id)
        }
        config.value.mapValue = setDefaultMapValue(
          Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
        )
        if (shouldReSearch) {
          queryDataForId(config.value.id)
        }
      } else {
        selectValue.value = Array.isArray(selectValue.value)
          ? [...selectValue.value]
          : selectValue.value
      }
    })
}

const visible = ref(false)
const visibleChange = (val: boolean) => {
  setTimeout(() => {
    visible.value = !val
    if (!val) {
      isFromRemote.value = false
      searchText.value = ''
      remoteMethod('')
    }
  }, 50)
}

watch(
  () => config.value.showEmpty,
  () => {
    setEmptyData()
  }
)

const setEmptyData = () => {
  const { showEmpty, displayType, optionValueSource } = config.value
  if (+displayType !== 0 || optionValueSource === 1) return
  const [s] = options.value
  if (showEmpty) {
    if (s?.value !== '_empty_$') {
      options.value = [{ label: '空数据', value: '_empty_$' }, ...options.value]
    }
  } else {
    if (s?.value === '_empty_$') {
      options.value = options.value.slice(1)
    }
  }
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
          selectValue.value = undefined
        })
      }
    })
  }
)

watch(
  [
    () => config.value.field.id,
    () => config.value.displayId,
    () => config.value.sort,
    () => config.value.sortId
  ],
  val => {
    if (!val) return
    debounceOptions(1)
  }
)

watch(
  () => config.value.optionValueSource,
  (valNew, newOld) => {
    if (!props.isConfig) return
    if ([valNew, newOld].includes(2)) {
      selectValue.value = Array.isArray(selectValue.value) ? [] : undefined
      config.value.selectValue = cloneDeep(selectValue.value)
      config.value.defaultValue = cloneDeep(selectValue.value)
    }
    debounceOptions(valNew)
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

const searchText = ref('')
const isFromRemote = ref(false)
const clear = () => {
  remoteMethod('')
}

const remoteMethod = (query: string) => {
  if (config.value.optionValueSource !== 1) return
  isFromRemote.value = true
  searchText.value = query
  debounceOptions(1)
}

watch(
  () => config.value.valueSource,
  () => {
    config.value.optionValueSource === 2 && debounceOptions(2)
  }
)

const setOptions = (num: number) => {
  if (num !== config.value.optionValueSource) return
  const {
    optionValueSource,
    checkedFieldsMap,
    checkedFields,
    field,
    valueSource,
    displayId,
    sort,
    sortId
  } = config.value
  switch (optionValueSource) {
    case 0:
      const arr = Object.values(checkedFieldsMap).filter(ele => !!ele) as string[]
      if (!!checkedFields.length && !!arr.length) {
        handleFieldIdDefaultChange(
          checkedFields.map(ele => checkedFieldsMap[ele]).filter(ele => !!ele)
        )
      } else {
        options.value = []
      }
      break
    case 1:
      if (field.id) {
        handleFieldIdChange({
          queryId: field.id,
          displayId: displayId || field.id,
          sort,
          sortId,
          resultMode: config.value.resultMode || 0,
          searchText: searchText.value,
          filter: getCascadeFieldId()
        })
      } else {
        options.value = []
      }
      break
    case 2:
      options.value = cloneDeep(
        (valueSource || []).map(ele => {
          return {
            label: `${ele}`,
            value: `${ele}`
          }
        })
      )
      setEmptyData()
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
    config.value.selectValue = plus ? [] : undefined
    selectValue.value = plus ? [] : undefined
  }
  nextTick(() => {
    multiple.value = config.value.multiple
  })
  debounceOptions(optionValueSource)
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: queryConditionWidth() + 'px' }
})

const mult = ref()
const single = ref()

const getOptionFromCascade = () => {
  if (config.value.optionValueSource !== 1 || ![0, 2, 5].includes(+config.value.displayType)) return
  debounceOptions(1)
}

onBeforeMount(() => {
  init()
  useEmitt({
    name: `${config.value.id}-select`,
    callback: getOptionFromCascade
  })
})

defineExpose({
  displayTypeChange,
  mult,
  single
})
</script>

<template>
  <el-select-v2
    v-if="multiple"
    key="multiple"
    ref="mult"
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
    :style="selectStyle"
    collapse-tags
    :remote="config.optionValueSource === 1"
    :remote-method="remoteMethod"
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
    ref="single"
    :style="selectStyle"
    filterable
    @clear="clear"
    :remote="config.optionValueSource === 1"
    :remote-method="remoteMethod"
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
  --ed-fill-color-light: #f5f7fa47;
  .ed-select-dropdown__option-item {
    .ed-checkbox__label:hover {
      color: #1f2329;
    }
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

  .ed-select-btn-group {
    color: #1f2329;
  }
}
</style>
