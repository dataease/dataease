<script lang="ts" setup>
import { ref, PropType, toRefs, nextTick, watch, onBeforeMount } from 'vue'
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
    getTreeOption()
  }
)

onBeforeMount(() => {
  getTreeOption()
})

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
let cacheId = ''
const getTreeOption = debounce(() => {
  getFieldTree(props.config.treeFieldList.map(ele => ele.id)).then(res => {
    console.log(res)
  })
}, 1000)
watch(
  () => props.config.treeFieldList,
  val => {
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
  isLeaf: 'isLeaf'
}
const selectValue = ref()

let id = 0
const cacheData = [{ value: 5, label: 'lazy load node5' }]
const load = (node, resolve) => {
  if (node.isLeaf) return resolve([])

  setTimeout(() => {
    resolve([
      {
        value: ++id,
        label: `lazy load node${id}`
      },
      {
        value: ++id,
        label: `lazy load node${id}`,
        isLeaf: true
      }
    ])
  }, 400)
}
</script>

<template>
  <el-tree-select
    v-model="selectValue"
    lazy
    v-if="multiple"
    check-strictly
    :render-after-expand="false"
    key="multiple"
    filterable
    multiple
    :cache-data="cacheData"
    :load="load"
    :props="propsTree"
    style="width: 240px"
  />
  <el-tree-select
    v-model="selectValue"
    lazy
    check-strictly
    :render-after-expand="false"
    v-else
    key="single"
    filterable
    :cache-data="cacheData"
    :load="load"
    :props="propsTree"
    style="width: 240px"
  />
</template>

<style lang="less" scoped></style>
