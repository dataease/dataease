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
interface SelectConfig {
  selectValue: any
  defaultMapValue: any
  defaultValue: any
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

const props = defineProps({
  config: {
    type: Object as PropType<SelectConfig>,
    default: () => {
      return {
        selectValue: '',
        defaultValue: '',
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
  if (placeholder.value.placeholderShow) {
    return props.config.placeholder
  }
  return ' '
})
const { config } = toRefs(props)

const multiple = ref(false)

const treeSelectConfirm = val => {
  treeValue.value = val
  handleValueChange()
}

const handleValueChange = () => {
  const value = Array.isArray(treeValue.value) ? [...treeValue.value] : treeValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(treeValue.value)
      ? [...treeValue.value]
      : treeValue.value
    nextTick(() => {
      isConfirmSearch(config.value.id)
    })
    return
  }
  config.value.defaultValue = value
}

watch(
  () => config.value.defaultValue,
  val => {
    if (config.value.multiple) {
      treeValue.value = Array.isArray(val) ? [...val] : val
    }
    nextTick(() => {
      multiple.value = config.value.multiple
    })
  }
)

watch(
  () => config.value.treeFieldList,
  () => {
    treeValue.value = config.value.multiple ? [] : undefined
    showOrHide.value = false
    getTreeOption()
  }
)

const init = () => {
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
    multiple.value = config.value.multiple
  })
  getTreeOption()
}

watch(
  () => config.value.id,
  () => {
    init()
  }
)
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
  () => config.value.selectValue,
  val => {
    if (props.isConfig) return
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
    if (!props.isConfig) return
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
const fakeValue = ''
const treeValue = ref()
const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: queryConditionWidth() + 'px' }
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
    :placeholder="placeholderText"
    collapse-tags
    :showWholePath="showWholePath"
    collapse-tags-tooltip
    key="multipleTree"
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
    :placeholder="placeholderText"
    :render-after-expand="false"
    v-else-if="!multiple && !loading"
    key="singleTree"
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
</style>
