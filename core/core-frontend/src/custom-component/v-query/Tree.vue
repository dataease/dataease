<script lang="ts" setup>
import { ref, PropType, toRefs, nextTick, watch, onMounted, computed, inject } from 'vue'
import { cloneDeep, debounce } from 'lodash-es'
import { getFieldTree } from '@/api/dataset'
interface SelectConfig {
  selectValue: any
  defaultMapValue: any
  defaultValue: any
  checkedFieldsMap: object
  displayType: string
  id: string
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
const { config } = toRefs(props)

const multiple = ref(false)

const handleValueChange = () => {
  const value = Array.isArray(treeValue.value) ? [...treeValue.value] : treeValue.value
  if (!props.isConfig) {
    config.value.selectValue = Array.isArray(treeValue.value)
      ? [...treeValue.value]
      : treeValue.value
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
    cacheData.value = []
    showOrHide.value = false
    getTreeOption()
  }
)

const init = () => {
  const { defaultValueCheck, multiple: plus, defaultValue } = config.value
  if (defaultValueCheck) {
    const arr = Array.isArray(defaultValue) ? '' : (defaultValue || '').split('-de-')
    config.value.selectValue = Array.isArray(defaultValue)
      ? cloneDeep([...defaultValue])
      : defaultValue
    treeValue.value = Array.isArray(defaultValue) ? cloneDeep([...defaultValue]) : defaultValue
    cacheData.value = Array.isArray(defaultValue)
      ? defaultValue.map(ele => ({
          value: ele,
          label: ele.split('-de-')[ele.split('-de-').length - 1]
        }))
      : arr.length
      ? [arr[arr.length - 1]]
      : []
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

watch(
  () => config.value.multiple,
  val => {
    if (!props.isConfig) return
    if (val) {
      treeValue.value = []
    }

    nextTick(() => {
      multiple.value = val
      nextTick(() => {
        resolveFunc(treeOptionList)
      })
      if (!val) {
        nextTick(() => {
          treeValue.value = undefined
        })
      }
    })
  }
)
let cacheId = ''
let treeOptionList = []

const dfs = arr => {
  return (arr || []).map(ele => {
    let children = []
    if (!!ele.children?.length) {
      children = dfs(ele.children)
    }
    return { ...ele, value: ele.id, label: ele.text, children }
  })
}

const getTreeOption = debounce(() => {
  getFieldTree(props.config.treeFieldList.map(ele => ele.id))
    .then(res => {
      treeOptionList = dfs(res)
    })
    .finally(() => {
      showOrHide.value = true
      nextTick(() => {
        resolveFunc(treeOptionList)
      })
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
const propsTree = {
  label: 'label',
  children: 'children',
  isLeaf: data => !data.children?.length
}
const treeValue = ref()
let cacheData = ref([])

let resolveFunc = (arr = []) => {
  arr = cloneDeep(arr)
}
const load = (node, resolve) => {
  resolveFunc = debounce(resolve, 500)
  if (!node.data?.children?.length) return resolve([])
  setTimeout(() => {
    resolve([...node.data?.children])
  }, 400)
}

const selectStyle = computed(() => {
  return props.isConfig ? {} : { width: queryConditionWidth() + 'px' }
})
</script>

<template>
  <el-tree-select
    v-model="treeValue"
    lazy
    v-if="multiple && showOrHide"
    @change="handleValueChange"
    check-strictly
    :render-after-expand="false"
    key="multipleTree"
    filterable
    :style="selectStyle"
    multiple
    :cache-data="cacheData"
    :load="load"
    :props="propsTree"
  />
  <el-tree-select
    v-model="treeValue"
    @change="handleValueChange"
    lazy
    check-strictly
    :render-after-expand="false"
    v-else-if="showOrHide"
    key="singleTree"
    :style="selectStyle"
    filterable
    :cache-data="cacheData"
    :load="load"
    :props="propsTree"
  />
  <el-tree-select
    v-model="treeValue"
    v-loading="!showOrHide"
    :data="[]"
    v-else
    key="hideTree"
    :style="selectStyle"
  />
</template>

<style lang="less" scoped></style>
