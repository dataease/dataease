<script lang="ts" setup>
import {
  ref,
  toRefs,
  PropType,
  onBeforeMount,
  onMounted,
  shallowRef,
  watch,
  nextTick,
  computed,
  inject,
  onBeforeUnmount,
  onUnmounted,
  defineAsyncComponent,
  Ref
} from 'vue'
import { enumValueObj, type EnumValue, getEnumValue } from '@/api/dataset'
import { cloneDeep, debounce } from 'lodash-es'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import Flat from './Flat.vue'
import eventBus from '@/utils/eventBus'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import colorFunctions from 'less/lib/less/functions/color.js'
import colorTree from 'less/lib/less/tree/color.js'
import { colorStringToHex } from '@/utils/color'

interface SelectConfig {
  selectValue: any
  defaultMapValue: any
  mapValue: any
  displayFormat?: number
  defaultValue: any
  checkedFieldsMap: object
  displayType: string
  showEmpty: boolean
  id: string
  sortList?: string[]
  queryConditionWidth: number
  placeholder: string
  resultMode: number
  displayId: string
  defaultValueFirstItem: boolean
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

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        displayFormat: 0,
        queryConditionWidth: 0,
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
const placeholder: Ref = inject('placeholder')
const releaseSelect = inject('release-unmount-select', Function, true)
const queryDataForId = inject('query-data-for-id', Function, true)
const isConfirmSearch = inject('is-confirm-search', Function, true)
const queryConditionWidth = inject('com-width', Function, true)
const cascadeList = inject('cascade-list', Function, true)
const setCascadeDefault = inject('set-cascade-default', Function, true)
const customStyle: any = inject('$custom-style-filter')

const placeholderText = computed(() => {
  if (placeholder?.value?.placeholderShow) {
    return ['', undefined].includes(props.config.placeholder) ? ' ' : props.config.placeholder
  }
  return ' '
})

const isMobileDataV = computed(() => {
  const { inMobile, dvInfo } = dvMainStore
  return dvInfo.type === 'dataV' && inMobile
})

const VanPopupSelect = defineAsyncComponent(() => import('./VanPopupSelect.vue'))

const cascade = computed(() => {
  return cascadeList() || []
})
let time
const disabledFirstItem = computed(() => {
  const { defaultValueFirstItem, optionValueSource } = props.config
  return defaultValueFirstItem && optionValueSource === 1
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
  clearTimeout(time)
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
  config.value.defaultValueFirstItem = false
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

const customSort = () => {
  if (config.value.sortList?.length && config.value.sort === 'customSort') {
    options.value = [
      ...options.value
        .sort(a => {
          if (config.value.sortList.indexOf(a.value) !== -1) {
            return -1
          }
        })
        .sort((a, b) => {
          if (config.value.sortList.indexOf(a.value) === -1) {
            return 0
          }
          return config.value.sortList.indexOf(a.value) - config.value.sortList.indexOf(b.value)
        })
    ]
  }
}

const handleFieldIdChange = (val: EnumValue) => {
  loading.value = true
  enumValueObj(val)
    .then(res => {
      let oldArr = []
      if (selectValue.value?.length && config.value.multiple) {
        oldArr = [...selectValue.value]
      }
      enumValueArr = [...(res || [])] || []
      options.value = [
        ...new Set(
          (res || []).map(ele => {
            return `${ele[val.displayId || val.queryId]}`
          })
        )
      ].map(ele => {
        return {
          label: `${ele}`,
          value: `${ele}`,
          checked: oldArr.includes(ele)
        }
      })
      customSort()
      if (!res?.length) {
        options.value = []
        selectValue.value = config.value.multiple ? [] : undefined
        config.value.defaultValue = selectValue.value
      }

      const valArr = options.value.map(ele => ele.value)

      let change = false

      if (
        config.value.multiple &&
        Array.isArray(selectValue.value) &&
        selectValue.value.length &&
        !selectValue.value.every(ele => valArr.includes(ele))
      ) {
        const delArr = selectValue.value.filter(ele => !valArr.includes(ele))
        selectValue.value = selectValue.value.filter(ele => valArr.includes(ele))
        options.value = options.value.filter(ele => !delArr.includes(ele.value))
        config.value.defaultValue = selectValue.value
        change = true
      }

      if (!config.value.multiple && selectValue.value && !valArr.includes(selectValue.value)) {
        options.value = options.value.filter(ele => selectValue.value !== ele.value)
        selectValue.value = undefined
        config.value.defaultValue = selectValue.value
        change = true
      }

      if (change) {
        config.value.mapValue = setDefaultMapValue(
          Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
        )
        config.value.defaultMapValue = setDefaultMapValue(
          Array.isArray(selectValue.value) ? [...selectValue.value] : [selectValue.value]
        )
        setCascadeValueBack(config.value.mapValue)
      }
    })
    .finally(() => {
      loading.value = false
      if (disabledFirstItem.value && config.value.defaultValueCheck) {
        time = setTimeout(() => {
          clearTimeout(time)
          setDefaultValueFirstItem()
        }, 300)
        return
      }

      if (config.value.defaultValueCheck && !isFromRemote.value) {
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
      setCascadeValueBack(config.value.mapValue)
      isFromRemote.value = false
    })
}

const visible = ref(false)

watch(
  () => config.value.showEmpty,
  () => {
    setEmptyData()
  }
)

const setDefaultValueFirstItem = () => {
  if (!options.value.length) return
  selectValue.value = config.value.multiple ? [options.value[0].value] : options.value[0].value
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
      isConfirmSearch(config.value.id, true)
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

watch(
  () => config.value.defaultValueFirstItem,
  val => {
    if (!val) return
    setDefaultValueFirstItem()
  }
)

const setEmptyData = () => {
  const { showEmpty, displayType, optionValueSource } = config.value
  if (+displayType !== 0 || optionValueSource === 1) return
  const [s] = options.value
  if (showEmpty) {
    if (s?.value !== '_empty_$') {
      options.value = [{ label: t('v_query.empty_data'), value: '_empty_$' }, ...options.value]
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
      setDefaultValueFirstItem()
    }
    nextTick(() => {
      multiple.value = val
      if (!val) {
        nextTick(() => {
          selectValue.value = undefined
          if (!config.value.defaultValueFirstItem || !config.value.defaultValueCheck) return
          setDefaultValueFirstItem()
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

watch([() => config.value.sortList], val => {
  if (!val?.length || config.value.sort !== 'customSort') return
  customSort()
})

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
    config.value.defaultValueFirstItem = false
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
          sort: sort === 'customSort' ? 'asc' : sort,
          sortId: sort === 'customSort' ? '' : sortId,
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
            value: `${ele}`,
            checked: Array.isArray(selectValue.value)
              ? selectValue.value.includes(`${ele}`)
              : selectValue.value === ele
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

const getCustomWidth = () => {
  if (placeholder?.value?.placeholderShow) {
    if (props.config.queryConditionWidth === undefined) {
      return queryConditionWidth()
    }
    return props.config.queryConditionWidth
  }
  return 227
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: getCustomWidth() + 'px' }
})

const selectStyleFlat = computed(() => {
  return props.isConfig ? { width: '415px' } : { width: getCustomWidth() + 'px' }
})

const mult = ref()
const single = ref()

const getOptionFromCascade = () => {
  if (config.value.optionValueSource !== 1 || ![0, 2, 5].includes(+config.value.displayType)) return
  isFromRemote.value = true
  debounceOptions(1)
}
const selectHideClick = () => {
  useEmitt().emitter.emit('select-hide_lick', config.value.id)
}

const hideClick = id => {
  if (id === config.value.id) return
  const vnode = single.value || mult.value
  vnode?.handleClickOutside?.()
}

onBeforeMount(() => {
  init()
  useEmitt({
    name: `${config.value.id}-select`,
    callback: getOptionFromCascade
  })

  useEmitt({
    name: 'select-hide_lick',
    callback: hideClick
  })
})

const isDataV = ref(false)

const popperClass = computed(() => {
  let str = 'filter-select-popper_class'
  if (visible.value) {
    str = 'load-select ' + str
  }

  if (isDataV.value) {
    str = str + ' color-scrollbar__thumb'
  }
  return str
})

onMounted(() => {
  isDataV.value =
    Boolean(document.querySelector('#canvas-dv-outer')) ||
    Boolean(document.querySelector('.datav-preview'))
})

const tagColor = computed(() => {
  if (
    !customStyle ||
    ['#FFFFFF', 'rgba(255, 255, 255, 1)', 'rgb(255, 255, 255)'].includes(customStyle.background)
  )
    return ''
  if (customStyle.background === '#131C42') return 'rgb(38, 53, 82)'
  const hexColor = customStyle.background.startsWith('#')
    ? customStyle.background
    : colorStringToHex(customStyle.background)

  return colorFunctions
    .mix(new colorTree('ffffff'), new colorTree(hexColor.substr(1)), { value: 20 })
    .toRGB()
})

const tagWidth = computed(() => {
  return (getCustomWidth() - 65) / 2 + 'px'
})

const tagTextWidth = computed(() => {
  return (getCustomWidth() - 65) / 2 - 20 + 'px'
})

const activeItems = computed(() => {
  return Array.isArray(selectValue.value) ? selectValue.value : [selectValue.value]
})

const handleItemClick = (item: any) => {
  if (multiple.value) {
    if (selectValue.value.includes(item)) {
      selectValue.value = selectValue.value.filter(ele => ele !== item)
    } else {
      selectValue.value = [...selectValue.value, item]
    }
  } else {
    selectValue.value = selectValue.value === item ? undefined : item
  }

  handleValueChange()
}

const componentClick = () => {
  mult.value?.blur()
  single.value?.blur()
}

onMounted(() => {
  eventBus.on('componentClick', componentClick)
})
onBeforeUnmount(() => {
  eventBus.off('componentClick', componentClick)
})

const onClear = () => {
  selectValue.value = multiple.value ? [] : undefined
  handleValueChange()
}

const onConfirm = (val: any) => {
  selectValue.value = multiple.value ? [...val] : val[0]
  handleValueChange()
}

defineExpose({
  displayTypeChange,
  mult,
  single
})
</script>

<template>
  <Flat
    @handleItemClick="handleItemClick"
    :options="options"
    :selectStyle="selectStyleFlat"
    v-loading="loading"
    :multiple="multiple"
    :disabled="disabledFirstItem && props.isConfig"
    :activeItems="activeItems"
    v-if="config.displayFormat === 1"
  ></Flat>
  <el-select-v2
    v-else-if="multiple"
    key="multiple"
    ref="mult"
    v-model="selectValue"
    :placeholder="placeholderText"
    v-loading="loading"
    filterable
    @click="selectHideClick"
    @change="handleValueChange"
    :popper-class="popperClass"
    multiple
    show-checked
    :tagColor="tagColor"
    scrollbar-always-on
    :disabled="disabledFirstItem && props.isConfig"
    clearable
    :style="selectStyle"
    collapse-tags
    :options="options"
    collapse-tags-tooltip
  ></el-select-v2>
  <el-select-v2
    v-else
    v-model="selectValue"
    key="single"
    @click="selectHideClick"
    :placeholder="placeholderText"
    scrollbar-always-on
    v-loading="loading"
    @change="handleValueChange"
    clearable
    :disabled="disabledFirstItem && props.isConfig"
    ref="single"
    :style="selectStyle"
    filterable
    radio
    :popper-class="popperClass"
    :options="options"
  >
    <template #default="{ item }">
      <el-radio-group v-model="selectValue">
        <el-radio :title="item.label" :label="item.value">{{ item.label }}</el-radio>
      </el-radio-group>
    </template>
  </el-select-v2>
  <VanPopupSelect
    @onClear="onClear"
    @onConfirm="onConfirm"
    :options="options"
    :selectValue="selectValue"
    :multiple="multiple"
    v-if="isMobileDataV"
  ></VanPopupSelect>
</template>

<style lang="less">
.filter-select-popper_class {
  --ed-fill-color-light: #f5f7fa47;
  font-family: var(--de-canvas_custom_font);
  .ed-vl__window.ed-select-dropdown__list {
    min-width: 200px;
  }
  .ed-select-dropdown {
    width: auto !important;
    min-width: 150px;
  }
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

.color-scrollbar__thumb {
  .ed-scrollbar__thumb {
    background: #bbbfc4 !important;
    opacity: 1 !important;
  }
}
</style>

<style lang="less" scoped>
:deep(.ed-select__selected-item) {
  .ed-tag {
    max-width: v-bind(tagWidth) !important;
  }

  .ed-select__tags-text {
    max-width: v-bind(tagTextWidth) !important;
  }
}
</style>
