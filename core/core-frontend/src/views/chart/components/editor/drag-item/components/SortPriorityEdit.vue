<script lang="ts" setup>
import icon_drag_outlined from '@/assets/svg/icon_drag_outlined.svg'
import draggable from 'vuedraggable'
import { reactive, watch, ref } from 'vue'
import chartViewManager from '../../../js/panel'

const loading = ref(false)

const state = reactive({
  sortList: []
})

const props = defineProps({
  chart: {
    type: Object,
    required: true
  }
})

const emit = defineEmits(['onPriorityChange'])

watch(
  () => props.chart,
  () => {
    init()
  },
  { deep: true }
)

const init = () => {
  const chart = props.chart
  if (!chart.sortPriority?.length) {
    state.sortList.splice(0, state.sortList.length)
  } else {
    state.sortList.splice(0, state.sortList.length, ...chart.sortPriority)
  }
  const chartInstance = chartViewManager.getChartView(chart.render, chart.type)
  if (chartInstance) {
    const axis = chartInstance.axis
    const axisMap = axis?.reduce((p, n) => {
      let axisArr
      switch (n) {
        case 'xAxis':
          axisArr = chart.xAxis
          break
        case 'yAxis':
          axisArr = chart.yAxis
          break
        case 'xAxisExt':
          axisArr = chart.xAxisExt
          break
        case 'yAxisExt':
          axisArr = chart.yAxisExt
          break
        case 'extBubble':
          axisArr = chart.extBubble
          break
        case 'flowMapEndName':
          axisArr = chart.flowMapEndName
          break
        case 'flowMapStartName':
          axisArr = chart.flowMapStartName
          break
        case 'extColor':
          axisArr = chart.extColor
          break
        case 'extStack':
          axisArr = chart.extStack
          break
        case 'drill':
          axisArr = chart.drillFields
          break
        default:
          break
      }
      axisArr?.forEach(ele => {
        if (!p[ele.id]) {
          p[ele.id] = ele.chartShowName ?? ele.name
        }
      })
      return p
    }, {})
    state.sortList = state.sortList.reduce((p, n) => {
      if (axisMap[n.id]) {
        n.name = axisMap[n.id]
        p.push(n)
      }
      return p
    }, [])
    Object.entries(axisMap).forEach(([key, value]) => {
      if (!state.sortList.find(item => item.id === key)) {
        state.sortList.push({ id: key, name: value })
      }
    })
  }
}
const onUpdate = () => {
  emit('onPriorityChange', state.sortList)
}

init()
</script>

<template>
  <el-scrollbar height="100%" max-height="599px">
    <draggable
      v-loading="loading"
      :list="state.sortList"
      animation="300"
      class="drag-list"
      item-key="id"
      @update="onUpdate"
    >
      <template #item="{ element }">
        <span :key="element.id" class="item-dimension" :title="element.name">
          <el-icon class="item-icon">
            <Icon name="icon_drag_outlined"><icon_drag_outlined class="svg-icon" /></Icon>
          </el-icon>
          <span class="item-span">
            {{ element.name }}
          </span>
        </span>
      </template>
    </draggable>
  </el-scrollbar>
</template>

<style lang="less" scoped>
.drag-list {
  height: 50vh;
}

.item-dimension {
  padding: 2px;
  margin: 2px;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  text-align: left;
  color: #606266;
  background-color: white;
  display: flex;
  align-items: center;
  cursor: move;
}

.item-icon {
  font-size: 16px;
  margin: 0 4px;
  color: #646a73;
}

.item-span {
  display: inline-block;
  width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;

  color: #1f2329;

  font-size: 14px;
  font-style: normal;
  font-weight: 400;
  line-height: 22px;
}

.blackTheme .item-dimension {
  border: solid 1px;
  border-color: var(--TableBorderColor);
  color: var(--TextPrimary);
  background-color: var(--MainBG);
}

.item-dimension + .item-dimension {
  margin-top: 6px;
}

.item-dimension:hover {
  box-shadow: 0px 4px 8px 0px rgba(31, 35, 41, 0.1);
}

.blackTheme .item-dimension:hover {
  color: var(--Main);
  background: var(--ContentBG);
}
</style>
