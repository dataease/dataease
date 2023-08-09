<script setup lang="ts">
import eventBus from '@/utils/eventBus'
import { getComponentRotatedStyle } from '@/utils/style'
import { getCurrentInstance, onMounted, ref } from 'vue'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { storeToRefs } from 'pinia'

const lines = ref(['xt', 'xc', 'xb', 'yl', 'yc', 'yr']) // 分别对应三条横线和三条竖线
const diff = ref(3) // 相距 dff 像素将自动吸附
const lineStatus = ref({
  xt: false,
  xc: false,
  xb: false,
  yl: false,
  yc: false,
  yr: false
})

const dvMainStore = dvMainStoreWithOut()
const { curComponent, componentData } = storeToRefs(dvMainStore)
let currentInstance
const hideLine = () => {
  Object.keys(lineStatus.value).forEach(line => {
    lineStatus.value[line] = false
  })
}

const showLine = (isDownward, isRightward) => {
  const lines = currentInstance.ctx.$refs
  const components = componentData.value
  const curComponentStyle = getComponentRotatedStyle(curComponent.value.style)
  const curComponentHalfWidth = curComponentStyle.width / 2
  const curComponentHalfHeight = curComponentStyle.height / 2

  hideLine()
  components.forEach(component => {
    if (component === curComponent.value) return
    const componentStyle = getComponentRotatedStyle(component.style)
    const { top, left, bottom, right } = componentStyle
    const componentHalfWidth = componentStyle.width / 2
    const componentHalfHeight = componentStyle.height / 2

    const conditions = {
      top: [
        {
          isNearly: isNearly(curComponentStyle.top, top),
          lineNode: lines.xt[0], // xt
          line: 'xt',
          dragShift: top,
          lineShift: top
        },
        {
          isNearly: isNearly(curComponentStyle.bottom, top),
          lineNode: lines.xt[0], // xt
          line: 'xt',
          dragShift: top - curComponentStyle.height,
          lineShift: top
        },
        {
          // 组件与拖拽节点的中间是否对齐
          isNearly: isNearly(
            curComponentStyle.top + curComponentHalfHeight,
            top + componentHalfHeight
          ),
          lineNode: lines.xc[0], // xc
          line: 'xc',
          dragShift: top + componentHalfHeight - curComponentHalfHeight,
          lineShift: top + componentHalfHeight
        },
        {
          isNearly: isNearly(curComponentStyle.top, bottom),
          lineNode: lines.xb[0], // xb
          line: 'xb',
          dragShift: bottom,
          lineShift: bottom
        },
        {
          isNearly: isNearly(curComponentStyle.bottom, bottom),
          lineNode: lines.xb[0], // xb
          line: 'xb',
          dragShift: bottom - curComponentStyle.height,
          lineShift: bottom
        }
      ],
      left: [
        {
          isNearly: isNearly(curComponentStyle.left, left),
          lineNode: lines.yl[0], // yl
          line: 'yl',
          dragShift: left,
          lineShift: left
        },
        {
          isNearly: isNearly(curComponentStyle.right, left),
          lineNode: lines.yl[0], // yl
          line: 'yl',
          dragShift: left - curComponentStyle.width,
          lineShift: left
        },
        {
          // 组件与拖拽节点的中间是否对齐
          isNearly: isNearly(
            curComponentStyle.left + curComponentHalfWidth,
            left + componentHalfWidth
          ),
          lineNode: lines.yc[0], // yc
          line: 'yc',
          dragShift: left + componentHalfWidth - curComponentHalfWidth,
          lineShift: left + componentHalfWidth
        },
        {
          isNearly: isNearly(curComponentStyle.left, right),
          lineNode: lines.yr[0], // yr
          line: 'yr',
          dragShift: right,
          lineShift: right
        },
        {
          isNearly: isNearly(curComponentStyle.right, right),
          lineNode: lines.yr[0], // yr
          line: 'yr',
          dragShift: right - curComponentStyle.width,
          lineShift: right
        }
      ]
    }

    const needToShow = []
    const { rotate } = curComponent.value.style
    if (conditions) {
      Object.keys(conditions).forEach(key => {
        // 遍历符合的条件并处理
        if (conditions[key]) {
          conditions[key].forEach(condition => {
            if (!condition.isNearly) return
            // 修改当前组件位移
            dvMainStore.setShapeSingleStyle({
              key,
              value:
                rotate != 0
                  ? translateCurComponentShift(key, condition, curComponentStyle)
                  : condition.dragShift
            })
            if (condition.lineNode) {
              condition.lineNode.style[key] = `${condition.lineShift}px`
            }
            needToShow.push(condition.line)
          })
        }
      })
    }

    // 同一方向上同时显示三条线可能不太美观，因此才有了这个解决方案
    // 同一方向上的线只显示一条，例如多条横条只显示一条横线
    if (needToShow.length) {
      chooseTheTrueLine(needToShow, isDownward, isRightward)
    }
  })
}

const translateCurComponentShift = (key, condition, curComponentStyle) => {
  const { width, height } = curComponent.value.style
  if (key == 'top') {
    return Math.round(condition.dragShift - (height - curComponentStyle.height) / 2)
  }

  return Math.round(condition.dragShift - (width - curComponentStyle.width) / 2)
}

const chooseTheTrueLine = (needToShow, isDownward, isRightward) => {
  // 如果鼠标向右移动 则按从右到左的顺序显示竖线 否则按相反顺序显示
  // 如果鼠标向下移动 则按从下到上的顺序显示横线 否则按相反顺序显示
  if (isRightward) {
    if (needToShow.includes('yr')) {
      lineStatus.value.yr = true
    } else if (needToShow.includes('yc')) {
      lineStatus.value.yc = true
    } else if (needToShow.includes('yl')) {
      lineStatus.value.yl = true
    }
  } else {
    // eslint-disable-next-line no-lonely-if
    if (needToShow.includes('yl')) {
      lineStatus.value.yl = true
    } else if (needToShow.includes('yc')) {
      lineStatus.value.yc = true
    } else if (needToShow.includes('yr')) {
      lineStatus.value.yr = true
    }
  }

  if (isDownward) {
    if (needToShow.includes('xb')) {
      lineStatus.value.xb = true
    } else if (needToShow.includes('xc')) {
      lineStatus.value.xc = true
    } else if (needToShow.includes('xt')) {
      lineStatus.value.xt = true
    }
  } else {
    // eslint-disable-next-line no-lonely-if
    if (needToShow.includes('xt')) {
      lineStatus.value.xt = true
    } else if (needToShow.includes('xc')) {
      lineStatus.value.xc = true
    } else if (needToShow.includes('xb')) {
      lineStatus.value.xb = true
    }
  }
}

const isNearly = (dragValue, targetValue) => {
  return Math.abs(dragValue - targetValue) <= diff.value
}

onMounted(() => {
  currentInstance = getCurrentInstance()
  // 监听元素移动和不移动的事件
  eventBus.on('move', ({ isDownward, isRightward }) => {
    showLine(isDownward, isRightward)
  })

  eventBus.on('unMove', () => {
    hideLine()
  })
})
</script>
<template>
  <div class="mark-line">
    <div
      v-for="line in lines"
      v-show="lineStatus[line] || false"
      :key="line"
      :ref="line"
      class="line"
      :class="line.includes('x') ? 'xLine' : 'yLine'"
    ></div>
  </div>
</template>

<style lang="less" scoped>
.mark-line {
  height: 100%;
}

.line {
  background: #59c7f9;
  position: absolute;
  z-index: 1000;
}

.xLine {
  width: 100%;
  height: 1px;
}

.yLine {
  width: 1px;
  height: 100%;
}
</style>
