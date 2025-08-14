<script lang="ts" setup>
import {
  ref,
  PropType,
  toRefs,
  nextTick,
  watch,
  onMounted,
  computed,
  inject,
  Ref,
  shallowRef
} from 'vue'
import { cloneDeep, debounce } from 'lodash-es'
import { getFieldTree } from '@/api/dataset'
import colorFunctions from 'less/lib/less/functions/color.js'
import colorTree from 'less/lib/less/tree/color.js'
import { colorStringToHex } from '@/utils/color'

interface SelectConfig {
  selectValue: any
  defaultMapValue: any
  defaultValue: any
  queryConditionWidth: number
  resultMode: number
  checkedFieldsMap: object
  displayType: string
  id: string
  placeholder: string
  checkedFields: string[]
  treeFieldList: Array<any>
  dataset: {
    id: string
  }
  field: {
    id: string
  }
  defaultValueCheck: boolean
  multiple: boolean
}

const customStyle: any = inject('$custom-style-filter')
const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
        queryConditionWidth: 0,
        displayType: '',
        resultMode: 0,
        defaultValueCheck: false,
        multiple: false,
        checkedFieldsMap: {},
        treeFieldList: []
      }
    }
  },
  isConfig: {
    type: Boolean,
    default: false
  }
})

const placeholder: Ref = inject('placeholder')
const placeholderText = computed(() => {
  if (placeholder?.value?.placeholderShow) {
    return ['', undefined].includes(props.config.placeholder) ? ' ' : props.config.placeholder
  }
  return ' '
})
const { config } = toRefs(props)
const fromTreeSelectConfirm = ref(false)
const multiple = ref(false)
const treeSelectConfirm = val => {
  treeValue.value = val
  handleValueChange()
}

const handleValueChange = () => {
  fromTreeSelectConfirm.value = true
  const value = Array.isArray(treeValue.value) ? [...treeValue.value] : treeValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(treeValue.value)
      ? [...treeValue.value]
      : treeValue.value
    nextTick(() => {
      fromTreeSelectConfirm.value = false
      isConfirmSearch(config.value.id)
    })
    return
  }
  config.value.defaultValue = value
  fromTreeSelectConfirm.value = false
}

const changeFromId = ref(false)
watch(
  () => config.value.id,
  () => {
    changeFromId.value = true
    init()
    nextTick(() => {
      changeFromId.value = false
    })
  }
)
let oldId
watch(
  () => config.value.treeFieldList,
  val => {
    let idStr = val.map(ele => ele.id).join('-')
    if (changeFromId.value || idStr === oldId) return
    oldId = idStr
    treeValue.value = config.value.multiple ? [] : undefined
    config.value.defaultValue = config.value.multiple ? [] : undefined
    config.value.selectValue = config.value.multiple ? [] : undefined
    showOrHide.value = false
    getTreeOption()
  }
)

const init = () => {
  loading.value = true
  const { defaultValueCheck, multiple: plus, defaultValue } = config.value
  if (defaultValueCheck) {
    config.value.selectValue = Array.isArray(defaultValue)
      ? cloneDeep([...defaultValue])
      : defaultValue
    treeValue.value = Array.isArray(defaultValue) ? cloneDeep([...defaultValue]) : defaultValue
  } else {
    config.value.selectValue = plus ? [] : undefined
    treeValue.value = plus ? [] : undefined
  }
  nextTick(() => {
    oldId = config.value.treeFieldList?.map(ele => ele.id).join('-')
    multiple.value = config.value.multiple
  })
  getTreeOption()
}

const tagWidth = computed(() => {
  return Math.min(getCustomWidth() / 3, 50) + 'px'
})

const tagsWidth = computed(() => {
  return getCustomWidth() - 40 + 'px'
})

const tagTextWidth = computed(() => {
  return Math.min(getCustomWidth() / 3, 50) - 25 + 'px'
})

const showOrHide = ref(true)
const queryConditionWidth = inject('com-width', Function, true)
const isConfirmSearch = inject('is-confirm-search', Function, true)
watch(
  () => config.value.id,
  () => {
    getTreeOption()
  }
)
onMounted(() => {
  setTimeout(() => {
    init()
  }, 0)
})

watch(
  () => config.value.defaultValue,
  val => {
    if (props.isConfig) return
    if (config.value.multiple) {
      treeValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = config.value.multiple
    })
  }
)

watch(
  () => config.value.selectValue,
  val => {
    if (props.isConfig || fromTreeSelectConfirm.value) return

    if (config.value.multiple) {
      treeValue.value = Array.isArray(val) ? [...val] : val
    }

    nextTick(() => {
      multiple.value = config.value.multiple
      if (!config.value.multiple) {
        treeValue.value = Array.isArray(config.value.selectValue)
          ? [...config.value.selectValue]
          : config.value.selectValue
      }
    })
  }
)

const showWholePath = ref(false)
watch(
  () => config.value.multiple,
  val => {
    if (!props.isConfig || changeFromId.value) return
    showWholePath.value = false
    if (val) {
      treeValue.value = []
    }
    nextTick(() => {
      multiple.value = val
      if (!val) {
        nextTick(() => {
          treeValue.value = undefined
        })
      }
      nextTick(() => {
        showWholePath.value = true
      })
    })
  }
)
let cacheId = ''
let treeOptionList = shallowRef([])
const filterMethod = (value, data) =>
  (data.label ?? '').toLowerCase().includes((value ?? '').toLowerCase())
const dfs = arr => {
  return (arr || []).map(ele => {
    let children = []
    if (!!ele.children?.length) {
      children = dfs(ele.children)
    }
    return { ...ele, value: ele.id, label: ele.text, children }
  })
}

const loading = ref(false)

const getTreeOption = debounce(() => {
  loading.value = true
  getFieldTree({
    fieldIds: props.config.treeFieldList.map(ele => ele.id),
    resultMode: config.value.resultMode || 0
  })
    .then(res => {
      treeOptionList.value = dfs(res)
    })
    .finally(() => {
      loading.value = false
      showWholePath.value = true
    })
}, 300)
watch(
  () => props.config.treeFieldList,
  val => {
    if (!props.isConfig) return
    const ids = val.map(ele => ele.id).join('')
    if (cacheId !== val.map(ele => ele.id).join('')) {
      cacheId = ids
      getTreeOption()
    }
  }
)
const fakeValue = ref('')
const treeValue = ref()
const getCustomWidth = () => {
  if (placeholder?.value?.placeholderShow) {
    if (props.config.queryConditionWidth !== undefined) {
      return props.config.queryConditionWidth
    }
    return queryConditionWidth()
  }
  return 227
}
const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: getCustomWidth() + 'px' }
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
</script>

<template>
  <el-tree-select
    v-model="treeValue"
    :data="treeOptionList"
    clearable
    v-if="multiple && !loading"
    @treeSelectConfirm="treeSelectConfirm"
    :render-after-expand="false"
    show-checkbox
    showBtn
    @change="handleValueChange"
    :placeholder="placeholderText"
    collapse-tags
    :filter-node-method="filterMethod"
    :showWholePath="showWholePath"
    collapse-tags-tooltip
    :tagColor="tagColor"
    :key="'multipleTree' + getCustomWidth()"
    filterable
    :style="selectStyle"
    multiple
  />
  <el-tree-select
    v-model="treeValue"
    @change="handleValueChange"
    :data="treeOptionList"
    check-strictly
    clearable
    :filter-node-method="filterMethod"
    :placeholder="placeholderText"
    :render-after-expand="false"
    v-else-if="!multiple && !loading"
    :key="'singleTree' + getCustomWidth()"
    :showWholePath="showWholePath"
    :style="selectStyle"
    filterable
  />
  <el-tree-select
    v-model="fakeValue"
    v-loading="loading"
    :data="[]"
    :placeholder="placeholderText"
    :render-after-expand="false"
    v-else
    key="fakeTree"
    :style="selectStyle"
  />
</template>

<style lang="less" scoped>
:deep(.ed-select-tags-wrapper) {
  display: inline-flex !important;
}

:deep(.ed-select__tags) {
  max-width: v-bind(tagsWidth) !important;
  .ed-tag {
    max-width: v-bind(tagWidth);
  }

  .ed-select__tags-text {
    max-width: v-bind(tagTextWidth) !important;
  }
}
</style>
