<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import AuthTree from './AuthTree.vue'

const { t } = useI18n()

const errorMessage = ref('')
const logic = ref<'or' | 'and'>('or')
const relationList = ref([])

const svgRealinePath = computed(() => {
  const lg = relationList.value.length
  let a = { x: 0, y: 0, child: relationList.value }
  a.y = Math.floor(dfsXY(a, 0) / 2)
  if (!lg) return ''
  let path = calculateDepth(a)
  return path
})

const svgDashinePath = computed(() => {
  const lg = relationList.value.length
  let a = { x: 0, y: 0, child: relationList.value }
  a.y = Math.floor(dfsXY(a, 0) / 2)
  if (!lg) return `M48 20 L68 20`
  let path = calculateDepthDash(a)
  return path
})

const init = expressionTree => {
  const { logic: lg = 'or', items = [] } = expressionTree
  logic.value = lg
  relationList.value = dfsInit(items)
}
const submit = () => {
  errorMessage.value = ''
  emits('save', {
    logic: logic.value,
    items: dfsSubmit(relationList.value),
    errorMessage: errorMessage.value
  })
}
const errorDetected = ({ enumValue, deType, filterType, term, value, name }) => {
  if (!name) {
    errorMessage.value = '过滤字段不能为空'
    return
  }
  if (filterType === 'logic') {
    if (!term) {
      errorMessage.value = '规则条件不能为空'
      return
    }
    if (!term.includes('null') && !term.includes('empty') && value === '') {
      errorMessage.value = t('chart.filter_value_can_null')
      return
    }
    if ([2, 3].includes(deType)) {
      if (parseFloat(value).toString() === 'NaN') {
        errorMessage.value = t('chart.filter_value_can_not_str')
        return
      }
    }
  }
  if (filterType === 'enum') {
    if (enumValue.length < 1) {
      errorMessage.value = t('chart.enum_value_can_not_null')
      return
    }
  }
}
const dfsInit = arr => {
  const elementList = []
  arr.forEach(ele => {
    const { subTree } = ele
    if (subTree) {
      const { items, logic } = subTree
      const child = dfsInit(items)
      elementList.push({ logic, child })
    } else {
      const { enumValue, fieldId, filterType, term, value, field } = ele
      const { name, deType } = field || {}
      elementList.push({
        enumValue: enumValue.join(','),
        fieldId,
        filterType,
        term,
        value,
        name,
        deType
      })
    }
  })
  return elementList
}
const dfsSubmit = arr => {
  const items = []
  arr.forEach(ele => {
    const { child = [] } = ele
    if (child.length) {
      const { logic } = ele
      const subTree = dfsSubmit(child)
      items.push({
        enumValue: [],
        fieldId: '',
        filterType: '',
        term: '',
        type: 'tree',
        value: '',
        subTree: { logic, items: subTree }
      })
    } else {
      const { enumValue, fieldId, filterType, deType, term, value, name } = ele
      errorDetected({ deType, enumValue, filterType, term, value, name })
      if (fieldId) {
        items.push({
          enumValue: enumValue ? enumValue.split(',') : [],
          fieldId,
          filterType,
          term,
          value,
          type: 'item',
          subTree: null
        })
      }
    }
  })
  return items
}
const removeRelationList = () => {
  relationList.value = []
}
const getY = arr => {
  const [a] = arr
  if (a.child?.length) {
    return getY(a.child)
  }
  return a.y
}
const calculateDepthDash = obj => {
  const lg = obj.child?.length
  let path = ''
  if (!lg && Array.isArray(obj.child)) {
    const { x, y } = obj
    path += `M${48 + x * 68} ${y * 41.4 + 20} L${88 + x * 68} ${y * 41.4 + 20}`
  } else if (obj.child?.length) {
    let y = Math.max(dfsY(obj, 0), dfs(obj.child, 0) + getY(obj.child) - 1)
    let parent = (dfs(obj.child, 0) * 41.4) / 2 + (getY(obj.child) || 0) * 41.4
    const { x } = obj
    path += `M${24 + x * 68} ${parent} L${24 + x * 68} ${y * 41.4 + 20} L${64 + x * 68} ${
      y * 41.4 + 20
    }`
    obj.child.forEach(item => {
      path += calculateDepthDash(item)
    })
  }

  return path
}
const calculateDepth = obj => {
  const lg = obj.child.length
  if (!lg) return ''
  let path = ''
  const { x: depth, y } = obj
  obj.child.forEach((item, index) => {
    const { y: sibingLg, z } = item
    if (item.child?.length) {
      let parent = (dfs(obj.child, 0) * 41.4) / 2 + (getY(obj.child) || 0) * 41.4
      let children = (dfs(item.child, 0) * 41.4) / 2 + getY(item.child) * 41.4
      let path1 = 0
      let path2 = 0
      if (parent < children) {
        path1 = parent
        path2 = children
      } else {
        ;[path1, path2] = [children, parent]
      }
      if (y >= sibingLg) {
        path1 = parent
        path2 = children
      }
      path += `M${24 + depth * 68} ${path1} L${24 + depth * 68} ${path2} L${
        68 + depth * 68
      } ${path2}`
      path += calculateDepth(item)
    }
    if (!item.child?.length) {
      if (sibingLg >= y) {
        path += `M${24 + depth * 68} ${y * 40} L${24 + depth * 68} ${
          (sibingLg + 1) * 41.4 - 20.69921875
        } L${68 + depth * 68} ${(sibingLg + 1) * 41.4 - 20.69921875}`
      } else {
        path += `M${24 + depth * 68} ${
          (sibingLg +
            (lg === 1 && index === 0 ? 0 : 1) +
            (obj.child[index + 1]?.child?.length ? y - sibingLg - 1 : 0)) *
            41.4 +
          20 +
          (lg === 1 && index === 0 ? 26 : 0)
        } L${24 + depth * 68} ${
          (sibingLg + 1) * 41.4 - 20.69921875 - (lg === 1 && index === 0 ? (z || 0) * 1.4 : 0)
        } L${68 + depth * 68} ${
          (sibingLg + 1) * 41.4 - 20.69921875 - (lg === 1 && index === 0 ? (z || 0) * 1.4 : 0)
        }`
      }
    }
  })
  return path
}
const changeAndOrDfs = (arr, logic) => {
  arr.forEach(ele => {
    if (ele.child) {
      ele.logic = logic === 'and' ? 'or' : 'and'
      changeAndOrDfs(ele.child, ele.logic)
    }
  })
}
const dfs = (arr, count) => {
  arr.forEach(ele => {
    if (ele.child?.length) {
      count = dfs(ele.child, count)
    } else {
      count += 1
    }
  })
  count += 1
  return count
}
const dfsY = (obj, count) => {
  obj.child.forEach(ele => {
    if (ele.child?.length) {
      count = dfsY(ele, count)
    } else {
      count = Math.max(count, ele.y, obj.y)
    }
  })
  return count
}
const dfsXY = (obj, count) => {
  obj.child.forEach(ele => {
    ele.x = obj.x + 1
    if (ele.child?.length) {
      let l = dfs(ele.child, 0)
      ele.y = Math.floor(l / 2) + count
      count = dfsXY(ele, count)
    } else {
      count += 1
      ele.y = count - 1
    }
  })
  count += 1
  return count
}
const addCondReal = (type, logic) => {
  relationList.value.push(
    type === 'condition'
      ? {
          fieldId: '',
          value: '',
          enumValue: '',
          term: '',
          filterType: 'logic',
          name: '',
          deType: ''
        }
      : { child: [], logic }
  )
}
const del = index => {
  relationList.value.splice(index, 1)
}

defineExpose({
  init,
  submit
})
const emits = defineEmits(['save'])
</script>

<template>
  <div class="rowAuth">
    <auth-tree
      @del="idx => del(idx)"
      @addCondReal="addCondReal"
      @removeRelationList="removeRelationList"
      @changeAndOrDfs="type => changeAndOrDfs(relationList, type)"
      :relationList="relationList"
      v-model:logic="logic"
    />
    <svg width="388" height="100%" class="real-line">
      <path
        stroke-linejoin="round"
        stroke-linecap="round"
        :d="svgRealinePath"
        fill="none"
        stroke="#CCCCCC"
        stroke-width="0.5"
      ></path>
    </svg>
    <svg width="388" height="100%" class="dash-line">
      <path
        stroke-linejoin="round"
        stroke-linecap="round"
        :d="svgDashinePath"
        fill="none"
        stroke="#CCCCCC"
        stroke-width="0.5"
        stroke-dasharray="4,4"
      ></path>
    </svg>
  </div>
</template>

<style lang="less" scoped>
.rowAuth {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  text-align: center;
  color: #2c3e50;
  position: relative;
}
.real-line,
.dash-line {
  position: absolute;
  top: 0;
  left: 0;
  user-select: none;
}
</style>
