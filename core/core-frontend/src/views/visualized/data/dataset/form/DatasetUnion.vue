<script lang="ts" setup>
import { reactive, computed, ref, nextTick } from 'vue'
// import { throttle } from 'lodash'
import { guid } from './util.js'
import { HandleMore } from '@/components/handle-more'
import { propTypes } from '@/utils/propTypes'

const state = reactive({
  nodeList: [],
  pathList: [],
  visualNode: null,
  visualNodeParent: null,
  visualPath: null
})
const props = defineProps({
  maskShow: propTypes.bool.def(false),
  offsetX: propTypes.number.def(0),
  offsetY: propTypes.number.def(0),
  dragHeight: propTypes.number.def(280)
})

const iconName = {
  left: 'icon_left-association',
  right: 'icon_left-association',
  inner: 'icon_intersect'
}

const nodeNameList = computed(() => {
  const arr = []
  dfsNodeNameList(state.nodeList, arr)
  return arr
})

const dfsNodeNameList = (list, arr) => {
  return list.forEach(ele => {
    arr.push(ele.tableName)
    if (ele.children?.length) {
      dfsNodeNameList(ele.children, arr)
    }
  })
}

const initState = nodeList => {
  Object.assign(state.nodeList, nodeList)
}

const activeNode = ref('')

const handleCommand = (ele, command) => {
  console.log('ele, command', ele, command)
}

const handlePathClick = ele => {
  const { from, to } = ele
  const arr = []
  const idArr = [from.id, to.id]
  dfsPath(arr, idArr, state.nodeList)
  emits('joinEditor', arr)
}

const dfsPath = (arr, idArr, list) => {
  list.forEach(ele => {
    if (idArr.includes(ele.id)) {
      arr.unshift(ele)
    }
    if (ele.children?.length) {
      dfsPath(arr, idArr, ele.children)
    }
  })
}

const dfsNodeBack = (arr, idArr, list) => {
  list.forEach(ele => {
    if (idArr.includes(ele.id)) {
      idArr.shift()
      const node = arr.shift()
      Object.assign(ele, node)
    }
    if (ele.children?.length) {
      dfsNodeBack(arr, idArr, ele.children)
    }
  })
}

const menuList = [
  {
    svgName: 'join-join',
    label: '关联',
    command: 'join'
  },
  {
    svgName: 'icon_edit_outlined',
    label: '编辑',
    command: 'editer'
  },
  {
    svgName: 'icon_delete-trash_outlined',
    label: '删除',
    command: 'del'
  }
]

const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

function elementInteractArea(pos1, pos2) {
  const pos1Width = pos1.right - pos1.left
  const pos1Height = pos1.bottom - pos1.top
  const pos2Width = pos2.right - pos2.left
  const pos2Height = pos2.bottom - pos2.top

  const axisOverlap =
    pos1Width + pos2Width - (Math.max(pos1.right, pos2.right) - Math.min(pos1.left, pos2.left))
  const crossOverlap =
    pos1Height + pos2Height - (Math.max(pos1.bottom, pos2.bottom) - Math.min(pos1.top, pos2.top))
  if (axisOverlap <= 0 || crossOverlap <= 0) {
    return 0
  }
  return axisOverlap * crossOverlap
}

const possibleNodeAreaList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  leafNode(nodeListLocation, flatArr)
  return flatArr.filter(ele => !ele.isShadow)
})

const leafNode = (arr, leafList) => {
  arr.forEach((ele, index) => {
    const fromX = ele.x * 300 + 24
    const fromY = ele.y * 64 + 24
    let toX = fromX + 200
    let toY = fromY + 64
    const next = arr[index + 1]
    if (next) {
      toY = next.y * 64 + 24
    }
    if (ele.children?.length) {
      leafNode(ele.children, leafList)
    }
    if (ele.x || ele.y) {
      leafList.push({
        ...ele,
        isShadow: ele.isShadow,
        isLeaf: !ele.children?.length,
        fromX,
        fromY,
        toX,
        toY
      })
    }
  })
}

const flatNodeList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  flatNode(nodeListLocation, flatArr)
  return flatArr
})

const flatNode = (arr, flatNodeList) => {
  arr.forEach(ele => {
    flatNodeList.push(ele)
    if (ele.children?.length) {
      flatNode(ele.children, flatNodeList)
    }
  })
}

const flatPathList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  const [root = {}] = nodeListLocation
  flatLine(root, flatArr)
  return flatArr
})

const dfsNode = (arr, nodeListLocation, x = 0, y = 0) => {
  arr.map((ele, index) => {
    const pre = nodeListLocation[index - 1]
    if (!ele.children?.length) {
      let idxChild = index + y
      let maxY = 0
      if (pre) {
        const last = pre.children?.length
          ? pre.children[pre.children.length - 1]
          : { y: 0, maxY: 0 }
        idxChild = Math.max(last.y, pre.y) + 1
        maxY += idxChild
        idxChild = Math.max(idxChild, last.maxY)
      }
      nodeListLocation.push({
        ...ele,
        x,
        y: idxChild,
        maxY,
        isShadow: !!ele.isShadow
      })
    } else {
      const children = []
      const pre = nodeListLocation[index - 1]
      let idx = y
      let maxY = 0
      if (pre) {
        const last = pre.children?.length
          ? pre.children[pre.children.length - 1]
          : { y: 0, maxY: 0 }
        idx = Math.max(last.y, pre.y) + 1
        maxY = last.maxY
      }
      dfsNode(ele.children, children, x + 1, idx ? Math.max(idx, maxY) : idx)
      maxY = Math.max(children[children.length - 1].maxY + 1, maxY)

      nodeListLocation.push({
        ...ele,
        x,
        y: idx ? Math.min(idx, maxY) : idx,
        maxY,
        isShadow: !!ele.isShadow,
        children
      })
    }
  })
}

const dfsNodeShadow = (arr, tableName, position, parent) => {
  return arr.some((ele, index) => {
    if (ele.tableName === tableName) {
      const flag = tableName + '_&&' + position
      if (ele.isShadow && state.visualNode.flag === flag) return true
      state.visualNode = {
        tableName: '',
        isShadow: true,
        flag
      }

      if (position === 'b') {
        state.visualNodeParent = parent
        arr.splice(index + 1, 0, state.visualNode)
      } else {
        state.visualNodeParent = ele
        ele.children = [state.visualNode]
      }
      return true
    }
    if (ele.children?.length) {
      return dfsNodeShadow(ele.children, tableName, position, ele)
    }
    return false
  })
}

const flatLine = (item, flatNodeList) => {
  const from = { ...item, d: '' }
  ;(item.children || []).forEach(ele => {
    flatNodeList.push({
      from,
      isShadow: ele.isShadow || item.isShadow,
      to: {
        ...ele
      },
      d:
        ele.y === from.y
          ? `M ${item.x * 300 + 224} ${ele.y * 64 + 44} l 100 0`
          : `M ${item.x * 300 + 240} ${from.y * 64 + 44} l 0 ${(ele.y - from.y) * 64} l 84 0`
    })
    if (ele.children?.length) {
      flatLine(ele, flatNodeList)
    }
  })
}

const dragover_handler = ev => {
  ev.preventDefault()

  dragOffsetX.value = ev.offsetX - props.offsetX
  dragOffsetY.value = ev.offsetY - props.offsetY

  const lg = state.nodeList.length
  const [fir] = state.nodeList

  if (!lg) return

  const [obj] = fir.children || []

  if (!fir.children?.length || obj?.isShadow) {
    if (obj?.isShadow) return
    state.visualNode = {
      tableName: '',
      isShadow: true,
      flag: '_&&'
    }

    state.nodeList[0].children = [state.visualNode]
    state.visualNodeParent = state.nodeList[0]
    return
  }

  let resultList = possibleNodeAreaList.value.map(ele => {
    const { fromX, fromY, toX, toY, isLeaf = false, tableName } = ele
    // const [k] = (state.visualNode?.flag || '').split('_')
    // if (k === tableName) {
    //   console.log('obj.isShadow', JSON.stringify(ele), JSON.stringify(state.visualNode))
    // }

    return [
      elementInteractArea(
        {
          left: dragOffsetX.value,
          right: dragOffsetX.value + 200,
          top: dragOffsetY.value,
          bottom: dragOffsetY.value + 40
        },
        {
          left: fromX,
          right: toX,
          top: fromY,
          bottom: toY
        }
      ),
      isLeaf || state.visualNode?.flag === tableName + '_&&r'
        ? elementInteractArea(
            {
              left: dragOffsetX.value,
              right: dragOffsetX.value + 200,
              top: dragOffsetY.value,
              bottom: dragOffsetY.value + 40
            },
            {
              left: fromX + 200,
              right: toX + 100,
              top: fromY,
              bottom: fromY + 40
            }
          )
        : 0
    ]
  })

  let maxIndex = 0

  resultList.reduce((pre, next, idx) => {
    const max = Math.max(...pre) > Math.max(...next)
    maxIndex = max ? maxIndex : idx
    return max ? pre : next
  })

  let maxArr = resultList[maxIndex]

  if (Array.isArray(state.visualNodeParent?.children)) {
    const shadowIndex = state.visualNodeParent.children.findIndex(ele => ele.isShadow)
    if (shadowIndex > -1) {
      state.visualNodeParent.children.splice(shadowIndex, 1)
    }
  }

  if (Math.max(...maxArr)) {
    const { tableName, isShadow = false } = possibleNodeAreaList.value[maxIndex]
    const [b, r] = maxArr

    if (!isShadow) {
      dfsNodeShadow(state.nodeList, tableName, b >= r ? 'b' : 'r', state.nodeList[0])
    }
  }
}

const dragenter_handler = ev => {
  console.log('dragenter')
  // prevent Default event
  ev.preventDefault()
}

const drop_handler = ev => {
  ev.preventDefault()
  let data = ev.dataTransfer.getData('text')
  const { tableName, type = 'db', datasourceId } = JSON.parse(data)
  const extraData = {
    info: JSON.stringify({
      table: tableName,
      sql: ''
    }),
    unionType: 'left',
    unionFields: [],
    currentDsFields: [],
    sqlVariableDetails: null
  }
  if (!state.nodeList.length) {
    state.nodeList.push({
      tableName,
      type,
      datasourceId,
      id: guid(),
      ...extraData
    })
    nextTick(() => {
      emits('addComplete')
    })
    return
  }

  nextTick(() => {
    emits('addComplete')
  })

  if (!state.visualNode) return
  nextTick(() => {
    Object.assign(state.visualNode, {
      tableName,
      type,
      datasourceId,
      id: guid(),
      ...extraData
    })
    emits('joinEditor', [
      {
        tableName,
        type,
        datasourceId,
        id: guid(),
        ...extraData
      },
      state.visualNodeParent
    ])
  })
}

const setStateBack = (node, parent) => {
  if (state.visualNode) {
    Object.assign(state.visualNode, node)
    Object.assign(state.visualNodeParent, parent)
    confirm()
  } else {
    dfsNodeBack([parent, node], [parent.id, node.id], state.nodeList)
  }
}

const confirm = () => {
  state.visualNode.isShadow = false
  delete state.visualNode.flag
  state.visualNode = null
  state.visualNodeParent = null
}

const notConfirm = () => {
  if (!state.visualNodeParent) return
  state.visualNodeParent.children = state.visualNodeParent.children.filter(ele => !ele.isShadow)
  confirm()
}

defineExpose({
  nodeNameList,
  nodeList: state.nodeList,
  setStateBack,
  notConfirm,
  initState
})

const emits = defineEmits(['addComplete', 'joinEditor'])
</script>

<template>
  <div
    @drop="$event => drop_handler($event)"
    @dragenter="$event => dragenter_handler($event)"
    @dragover="$event => dragover_handler($event)"
    class="drag-mask"
    :style="{ height: dragHeight + 'px' }"
  >
    <svg
      version="1.1"
      baseProfile="full"
      width="100%"
      height="100%"
      xmlns="http://www.w3.org/2000/svg"
    >
      <path
        :key="ele.d"
        class="path-point"
        v-for="ele in flatPathList"
        :d="ele.d"
        :stroke-dasharray="ele.isShadow ? '4,4' : '0'"
        :stroke="ele.isShadow ? '#3370ff' : '#BBBFC4'"
        stroke-width="1"
        fill="none"
      />
      <foreignObject
        :key="ele.tableName"
        v-for="ele in flatNodeList"
        :x="ele.x * 300 + 24"
        :y="ele.y * 64 + 24"
        width="200"
        height="40"
      >
        <div
          @click="activeNode = ele.tableName"
          class="node-union"
          :class="[
            {
              'shadow-node': ele.isShadow,
              'active-node': activeNode === ele.tableName
            }
          ]"
        >
          <span class="tableName">{{ ele.tableName }}</span>
          <handle-more
            style="margin-left: auto"
            v-if="activeNode === ele.tableName"
            :menuList="menuList"
            @handle-command="command => handleCommand(ele, command)"
          ></handle-more>
        </div>
      </foreignObject>

      <foreignObject
        :key="ele.d"
        v-for="ele in flatPathList"
        :x="ele.from.x * 300 + 272"
        :y="ele.to.y * 64 + 28"
        width="32"
        height="32"
      >
        <div @click="handlePathClick(ele)" class="path-union">
          <el-icon>
            <Icon :name="iconName[ele.to.unionType]"></Icon>
          </el-icon>
        </div>
      </foreignObject>
    </svg>
    <div
      class="mask-dataset"
      :class="[
        {
          'mask-dataset-none': !state.nodeList.length
        }
      ]"
      v-if="maskShow"
    ></div>
  </div>
</template>

<style lang="less">
.path-point {
  cursor: pointer;
}

.node-union {
  height: 100%;
  width: 100%;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  color: #1f2329;
  padding-left: 15.75px;
  display: flex;
  align-items: center;
  background: #fff;
  cursor: pointer;
  padding-right: 8px;
  .tableName {
    max-width: 100px;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
}

.path-union {
  width: 100%;
  height: 100%;
  border: 1px solid #dee0e3;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  cursor: pointer;
}

.shadow-node {
  border: 1px dashed;
  border-color: #3370ff;
  background-color: rgba(51, 112, 255, 0.08);
  span {
    display: none;
  }
}

.active-node {
  border-color: #3370ff;
}

.drag-mask {
  background: #f5f6f7;
  overflow-x: auto;
  position: relative;
  width: 100%;
}

.mask-dataset {
  position: absolute;
  left: 16px;
  top: 16px;
  width: calc(100% - 32px);
  height: calc(100% - 32px);
  z-index: 5;
  user-select: none;
}

.mask-dataset-none {
  background-color: #e5ebf8;
  border: 1px dashed;
  border-color: #3370ff;
}
</style>
