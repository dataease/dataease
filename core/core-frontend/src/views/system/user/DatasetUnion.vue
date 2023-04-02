<script lang="ts" setup>
import { reactive, computed, ref } from 'vue'
// import { throttle } from 'lodash'
const state = reactive({
  leftList: [],
  nodeList: [],
  pathList: [],
  visualNode: null,
  visualNodeParent: null,
  visualPath: null
})

const maskShow = ref(false)

const offsetX = ref(0)
const offsetY = ref(0)

const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

const possibleNodeAreaList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  leafNode(nodeListLocation, flatArr)
  return flatArr.filter(ele => !ele.isShadow)
})

function elementInterectArea(pos1, pos2) {
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

const flatNodeList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  flatNode(nodeListLocation, flatArr)
  return flatArr
})

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
        idxChild = Math.max(last.y, index, pre.y) + 1
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

const dfsNodeShadow = (arr, label, position = 'b') => {
  return arr.some((ele, index) => {
    if (ele.label === label) {
      const flag = label + '_' + position
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

const flatNode = (arr, flatNodeList) => {
  arr.forEach(ele => {
    flatNodeList.push(ele)
    if (ele.children?.length) {
      flatNode(ele.children, flatNodeList)
    }
  })
}

const flatLine = ({ x, y, children = [] }, flatNodeList) => {
  const from = { x, y, d: '' }
  children.forEach(ele => {
    flatNodeList.push({
      from,
      to: {
        x: ele.x,
        y: ele.y
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

state.nodeList = [
  {
    label: '12312',
    children: [
      {
        label: 'opop',
        children: [
          {
            label: 'lll'
          },
          {
            label: 'kkkyk',
            children: [
              {
                label: 'kkkykllll'
              }
            ]
          },
          {
            label: 'kkkyllk'
          },
          {
            label: 'kkkyklllpl'
          }
        ]
      },
      {
        label: 'llloo'
      },
      {
        label: 'llloops'
      },
      {
        label: '1231jjj2',
        children: [
          {
            label: 'ljjkklll',
            children: [
              {
                label: 'lll'
              },
              {
                label: 'kkkk'
              }
            ]
          },
          {
            label: 'kkkkg'
          }
        ]
      },
      {
        label: 'gggg',
        children: [
          {
            label: '1458'
          },
          {
            label: '9087'
          }
        ]
      }
    ]
  }
]

const dragstart = (e: MouseEvent, ele: string) => {
  offsetX.value = e.offsetX
  offsetY.value = e.offsetY
  e.dataTransfer.setData('text/plain', ele)
  maskShow.value = true
}

const dragover_handler = ev => {
  console.log('dragOver')
  // prevent Default event
  ev.preventDefault()

  dragOffsetX.value = ev.offsetX - offsetX.value
  dragOffsetY.value = ev.offsetY - offsetY.value

  let resultList = possibleNodeAreaList.value.map(ele => {
    const { fromX, fromY, toX, toY, isLeaf = false, label } = ele
    const [k] = (state.visualNode?.flag || '').split('_')
    if (k === label) {
      console.log('obj.isShadow', JSON.stringify(ele), JSON.stringify(state.visualNode))
    }

    return [
      elementInterectArea(
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
      isLeaf || k === label
        ? elementInterectArea(
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

  console.log(maxArr, 'maxArr', resultList)

  if (Math.max(...maxArr)) {
    const { label, isShadow = false } = possibleNodeAreaList.value[maxIndex]
    const [b, r] = maxArr

    if (!isShadow) {
      dfsNodeShadow(state.nodeList, label, b > r ? 'b' : 'r')
    }
    console.log(
      'Math.max(...resultList[maxIndex]',
      Math.max(...resultList[maxIndex]),
      maxIndex,
      label,
      isShadow,
      b,
      r
    )
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
  // let data = ev.dataTransfer.getData('text')
}

state.leftList = Array(15)
  .fill(1)
  .map((_, index) => 'test' + index)
</script>

<template>
  <div style="display: flex">
    <div style="width: 200px">
      <div
        @dragstart="$event => dragstart($event, ele)"
        @dragend="maskShow = false"
        draggable="true"
        class="list-item_primary"
        :key="ele"
        v-for="ele in state.leftList"
      >
        {{ ele }}
      </div>
    </div>

    <div
      @drop="$event => drop_handler($event)"
      @dragenter="$event => dragenter_handler($event)"
      @dragover="$event => dragover_handler($event)"
      class="drag-mask"
    >
      <svg
        version="1.1"
        baseProfile="full"
        width="200%"
        height="100%"
        xmlns="http://www.w3.org/2000/svg"
      >
        <foreignObject
          :key="ele.label"
          v-for="ele in flatNodeList"
          :x="ele.x * 300 + 24"
          :y="ele.y * 64 + 24"
          width="200"
          height="40"
        >
          <div class="node-union" :class="[ele.isShadow ? 'shadow-node' : '']">
            {{ ele.label }}
          </div>
        </foreignObject>
        <path
          :key="ele.d"
          class="path-point"
          v-for="ele in flatPathList"
          :d="ele.d"
          stroke="blue"
          stroke-width="0.5"
          fill="none"
        />
        <!-- <path d="M 224 44 l 100 0" stroke="blue" stroke-width="0.5" fill="none" /> -->
        <!-- <path d="M 240 44 l 0 128 l 84 0" stroke="red" stroke-width="0.5" fill="none" /> -->
        <!-- <path stroke-dasharray="4,4" d="M 224 44 L 324 44 z" stroke="blue" stroke-width="1" /> -->
        <!-- <foreignObject
          :key="ele.label"
          v-for="ele in possibleNodeList"
          :x="ele.x * 300 + 24"
          :y="ele.y * 64 + 24"
          width="200"
          height="40"
        >
          <div class="node-union dasharray">
            {{ ele.label || 123 }}
          </div>
        </foreignObject> -->
      </svg>
      <div class="mask-dataset" v-if="maskShow"></div>
    </div>
  </div>
</template>

<style lang="less">
.path-point {
  cursor: pointer;
}

.node-union {
  height: 100%;
  width: 100%;
  border: 1px solid #3370ff;
  border-radius: 4px;

  &.dasharray {
    border: dashed;
  }
}

.shadow-node {
  border: dotted;
}

.drag-mask {
  flex: 1;
  overflow-x: auto;
  border: 1px solid #ccc;
  position: relative;
}

.mask-dataset {
  position: absolute;
  left: 0;
  top: 0;
  width: 100%;
  height: 100%;
  z-index: 5;
  background: #cccccc52;
  user-select: none;
}
</style>
