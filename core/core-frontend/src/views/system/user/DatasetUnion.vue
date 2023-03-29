<script lang="ts" setup>
import { reactive, computed, ref } from 'vue'

const state = reactive({
  leftList: [],
  nodeList: [],
  pathList: [],
  visualNode: null,
  visualPath: null
})

const offsetX = ref(0)
const offsetY = ref(0)

const dragOffsetX = ref(0)
const dragOffsetY = ref(0)

const visualNode = computed(() => {
  return {}
})

const visualPath = computed(() => {
  return {}
})

const possibleNodeAreaList = computed(() => {
  let flatArr = []
  let nodeListLocation = []
  dfsNode(state.nodeList, nodeListLocation)
  leafNode(nodeListLocation, flatArr)
  console.log('cbd', flatArr)
  return flatArr
})

const mostlyIntersects = arr => {
  let len = arr.length
  let [x, y, l, h] = arr[0]
  let [a, b, c, d] = [y, x + l, y - h, x]
  for (let i = 1; i < len; i++) {
    ;[x, y, l, h] = arr[i]
    ;[a, b, c, d] = [
      Math.min(a, y), // 上边界为三个坐标Y轴坐标的最小值
      Math.min(b, x + l), //右边界为三个坐标（X轴坐标+宽度）的最小值
      Math.max(c, y - h), //下边界为三个坐标（Y轴坐标-高度）的最大值
      Math.max(d, x) //左边界为三个坐标X轴坐标的最大值
    ]
  }
  if (a > c && b > d) {
    return (a - c) * (b - d) //高为（上边界-下边界）*宽为（右边界-左边界）
  }
  return 0
}

console.log(
  'mostlyIntersects(0,0 , 4, 4, 2, 0, 4,4)',
  mostlyIntersects([
    [324, -152, 524, -344],
    [326, -74, 526, -114]
  ]),
  mostlyIntersects([
    [0, 0, 4, 4],
    [2, 0, 4, 4]
  ])
)

const leafNode = (arr, leafList) => {
  arr.forEach(({ x, y, label, children = [] }, index) => {
    const fromX = x * 300 + 24
    const fromY = y * 64 + 24
    let toX = fromX + 500
    let toY = fromY + 64
    const next = arr[index + 1]
    if (next && !next.isShadow) {
      toY = next.y * 64 + 24
    }
    if (children?.length) {
      toX = fromX + 200
      leafNode(children, leafList)
    }
    if (x || y) {
      leafList.push({
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
  console.log('dfsNode(state.nodeList)', nodeListLocation)
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
    if (!ele.children?.length) {
      nodeListLocation.push({
        x,
        y: index + y,
        label: ele.label
      })
    } else {
      const pre = nodeListLocation[index - 1]
      let idx = y
      if (pre) {
        console.log('pre.children?.length', pre.label, pre.children?.length, pre.y, index)
        idx = (pre.children?.length || 0) + pre.y
      }
      const children = []
      dfsNode(ele.children, children, x + 1, idx)
      nodeListLocation.push({
        x,
        y: idx,
        children,
        label: ele.label
      })
    }
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
            label: 'kkkk'
          }
        ]
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
            label: 'kkkk'
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
}

// const dragover = e => {
//   console.log('ee', e, e.dataTransfer.getData('text'))
// }

const dragover_handler = ev => {
  console.log('dragOver')
  // prevent Default event
  ev.preventDefault()
  dragOffsetX.value = ev.offsetX - offsetX.value
  dragOffsetY.value = ev.offsetY - offsetY.value
}

const dragenter_handler = ev => {
  console.log('dragenter')
  // prevent Default event
  ev.preventDefault()
}

const drop_handler = ev => {
  console.log('Drop')
  ev.preventDefault()
  var data = ev.dataTransfer.getData('text')
  let maxArea = 0

  console.log(
    'data',
    possibleNodeAreaList,
    possibleNodeAreaList.value.map(ele => {
      const { fromX, fromY, toX, toY } = ele
      console.log(
        'dragOffsetX.value, dragOffsetY.value, dragOffsetX.value + 200, dragOffsetY.value + 40',
        dragOffsetX.value,
        dragOffsetY.value,
        dragOffsetX.value + 200,
        dragOffsetY.value + 40
      )

      return mostlyIntersects([
        [dragOffsetX.value, -dragOffsetY.value, dragOffsetX.value + 200, -dragOffsetY.value + 40],
        [fromX, -fromY, toX, -toY]
      ])
    })
  )
  //   maxArea = Math.max(
  //     ...possibleNodeAreaList.value.map(ele => {
  //       const { fromX, fromY, toX, toY } = ele
  //       console.log(
  //         'dragOffsetX.value, dragOffsetY.value, dragOffsetX.value + 200, dragOffsetY.value + 40',
  //         dragOffsetX.value,
  //         dragOffsetY.value,
  //         dragOffsetX.value + 200,
  //         dragOffsetY.value + 40
  //       )

  //       return mostlyIntersects([
  //         [dragOffsetX.value, dragOffsetY.value, dragOffsetX.value + 200, dragOffsetY.value + 40],
  //         [fromX, fromY, toX, toY]
  //       ])
  //     })
  //   )

  //   console.log('maxArea', maxArea)

  // ev.dataTransfer.clearData();
}

state.leftList = Array(10)
  .fill(1)
  .map((_, index) => 'test' + index)
</script>

<template>
  <div style="display: flex">
    <div style="width: 200px">
      <div
        @dragstart="$event => dragstart($event, ele)"
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
      style="flex: 1; overflow-x: auto; border: 1px solid #ccc"
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
          <div class="node-union">
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
</style>
