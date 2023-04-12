<script lang="ts" setup>
import { reactive, computed, ref } from 'vue'
// import { throttle } from 'lodash'
import { HandleMore } from '@/components/handle-more'
import { propTypes } from '@/utils/propTypes'

const state = reactive({
  leftList: [],
  nodeList: [],
  pathList: [],
  visualNode: null,
  visualNodeParent: null,
  visualPath: null
})
const props = defineProps({
  maskShow: propTypes.bool.def(false),
  offsetX: propTypes.number.def(0),
  offsetY: propTypes.number.def(0)
})

const activeNode = ref('')

const handleCommand = (ele, command) => {
  console.log('ele, command', ele, command)
}

const handlePathClick = ele => {
  console.log('ele', ele)
}

const menuList = [
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
  arr.forEach(({ x, y, label, children = [], isShadow = false }, index) => {
    const fromX = x * 300 + 24
    const fromY = y * 64 + 24
    let toX = fromX + 200
    let toY = fromY + 64
    const next = arr[index + 1]
    if (next) {
      toY = next.y * 64 + 24
    }
    if (children?.length) {
      leafNode(children, leafList)
    }
    if (x || y) {
      leafList.push({
        isShadow,
        isLeaf: !children?.length,
        fromX,
        fromY,
        toX,
        toY,
        label
      })
    }
  })
}

// const travelsNode = computed(() => {
//   let nodeListLocation = []
//   dfsNode(state.nodeList, nodeListLocation)
//   return nodeListLocation
// })

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
      if (pre) {
        const last = pre.children?.length ? pre.children[pre.children.length - 1] : { y: 0 }
        idxChild = Math.max(last.y, pre.y) + 1
      }
      nodeListLocation.push({
        x,
        y: idxChild,
        isShadow: !!ele.isShadow,
        label: ele.label
      })
    } else {
      const children = []
      const pre = nodeListLocation[index - 1]
      let idx = y
      if (pre) {
        const last = pre.children?.length ? pre.children[pre.children.length - 1] : { y: 0 }
        idx = Math.max(last.y, pre.y) + 1
      }
      dfsNode(ele.children, children, x + 1, idx)
      nodeListLocation.push({
        x,
        y: idx,
        isShadow: !!ele.isShadow,
        children,
        label: ele.label
      })
    }
  })
}

const dfsNodeShadow = (arr, label, position) => {
  return arr.some((ele, index) => {
    if (ele.label === label) {
      const flag = label + '_&&' + position
      if (ele.isShadow && state.visualNode.flag === flag) return true
      state.visualNode = {
        label: '',
        isShadow: true,
        flag
      }

      if (position === 'b') {
        state.visualNodeParent = arr
        arr.splice(index + 1, 0, state.visualNode)
      } else {
        state.visualNodeParent = ele
        ele.children = [state.visualNode]
      }
      return true
    }
    if (ele.children?.length) {
      return dfsNodeShadow(ele.children, label, position)
    }
    return false
  })
}

const flatLine = ({ x, y, children = [], isShadow, label }, flatNodeList) => {
  const from = { x, y, d: '', label }
  children.forEach(ele => {
    flatNodeList.push({
      from,
      isShadow: ele.isShadow || isShadow,
      to: {
        x: ele.x,
        y: ele.y,
        label: ele.label
      },
      d:
        ele.y === from.y
          ? `M ${x * 300 + 224} ${ele.y * 64 + 44} l 100 0`
          : `M ${x * 300 + 240} ${from.y * 64 + 44} l 0 ${(ele.y - from.y) * 64} l 84 0`
    })
    if (ele.children?.length) {
      flatLine(ele, flatNodeList)
    }
  })
}

state.nodeList = []

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
      label: '',
      isShadow: true,
      flag: '_&&'
    }

    state.nodeList[0].children = [state.visualNode]
    return
  }

  let resultList = possibleNodeAreaList.value.map(ele => {
    const { fromX, fromY, toX, toY, isLeaf = false, label } = ele
    // const [k] = (state.visualNode?.flag || '').split('_')
    // if (k === label) {
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
      isLeaf || state.visualNode?.flag === label + '_&&r'
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

  if (Array.isArray(state.visualNodeParent)) {
    const shadowIndex = state.visualNodeParent.findIndex(ele => ele.isShadow)
    if (shadowIndex > -1) {
      state.visualNodeParent.splice(shadowIndex, 1)
    }
  } else if (state.visualNodeParent) {
    state.visualNodeParent.children = []
    delete state.visualNodeParent.children
  }

  if (Math.max(...maxArr)) {
    const { label, isShadow = false } = possibleNodeAreaList.value[maxIndex]
    const [b, r] = maxArr

    if (!isShadow) {
      dfsNodeShadow(state.nodeList, label, b > r || b === r ? 'b' : 'r')
    }
  }
}

const dragenter_handler = ev => {
  console.log('dragenter')
  // prevent Default event
  ev.preventDefault()
}

const drop_handler = ev => {
  console.log('Drop')
  ev.preventDefault()
  let data = ev.dataTransfer.getData('text')
  if (!state.nodeList.length) {
    state.nodeList.push({
      label: data
    })
    return
  }
  if (!state.visualNode) return
  state.visualNode.label = data
  state.visualNode.isShadow = false
  delete state.visualNode.flag
  state.visualNode = null
  state.visualNodeParent = null
}

state.leftList = Array(15)
  .fill(1)
  .map((_, index) => 'test' + index)
</script>

<template>
  <div
    @drop="$event => drop_handler($event)"
    @dragenter="$event => dragenter_handler($event)"
    @dragover="$event => dragover_handler($event)"
    class="drag-mask"
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
        @click="handlePathClick(ele)"
        class="path-point"
        v-for="ele in flatPathList"
        :d="ele.d"
        :stroke-dasharray="ele.isShadow ? '4,4' : '0'"
        :stroke="ele.isShadow ? '#3370ff' : '#BBBFC4'"
        stroke-width="1"
        fill="none"
      />
      <foreignObject
        :key="ele.label"
        v-for="ele in flatNodeList"
        :x="ele.x * 300 + 24"
        :y="ele.y * 64 + 24"
        width="200"
        height="40"
      >
        <div
          @click="activeNode = ele.label"
          class="node-union"
          :class="[
            {
              'shadow-node': ele.isShadow,
              'active-node': activeNode === ele.label
            }
          ]"
        >
          <span class="label">{{ ele.label }}</span>
          <handle-more
            style="margin-left: auto"
            v-if="activeNode === ele.label"
            :menu-list="menuList"
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
            <Icon name="icon_intersect"></Icon>
            <!-- <Icon name="icon_right-association"></Icon>
              <Icon name="icon_left-association"></Icon> -->
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
  .label {
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
}

.active-node {
  border-color: #3370ff;
}

.drag-mask {
  background: #f5f6f7;
  overflow-x: auto;
  border: 1px solid #ccc;
  position: relative;
  width: 100%;
  height: 50%;
}

.mask-dataset {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  user-select: none;
}

.mask-dataset-none {
  background-color: #e5ebf8;
  border: 1px dashed;
  border-color: #3370ff;
}
</style>
