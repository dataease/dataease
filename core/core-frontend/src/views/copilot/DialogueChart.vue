<script lang="ts" setup>
import { PropType, computed, onMounted, shallowRef, ref, nextTick, watch } from 'vue'
import { Column, Line, Pie } from '@antv/g2plot'
import { useElementSize } from '@vueuse/core'
import { downloadCanvas } from '@/utils/imgUtils'
import ExcelJS from 'exceljs'
interface Copilot {
  msgType: string
  question: string
  chart: object
  loading: boolean
  chartData: object
  msgStatus: number
  id: string
}
const props = defineProps({
  copilotInfo: {
    type: Object as PropType<Copilot>,
    default: () => ({
      msgType: 'api',
      chart: {},
      id: '',
      loading: false,
      question: '',
      chartData: {
        data: {},
        title: ''
      },
      msgStatus: 0
    })
  },
  isWelcome: {
    type: Boolean
  },
  isAnswer: {
    type: Boolean
  }
})

const content = ref()
const chartTypeList = ref()
let columnPlot = null
const chartRef = ref()
const { width } = useElementSize(chartRef)
onMounted(() => {
  const { chart, msgType, msgStatus, chartData, id } = props.copilotInfo
  if (msgStatus === 1 && msgType === 'api' && chartData) {
    if (['bar', 'line'].includes(chart.type)) {
      activeCommand.value = chart.type
      const chartType = chart.type === 'bar' ? Column : Line
      columnPlot = new chartType(`de-${id}-ed`, {
        data: chartData.data.data,
        xField: chart.axis?.x?.value,
        yField: chart.axis?.y?.value,
        legend: {
          layout: 'horizontal',
          position: 'left'
        }
      })
      columnPlot.render()
    } else if (chart.type === 'pie') {
      activeCommand.value = chart.type
      columnPlot = new Pie(`de-${id}-ed`, {
        appendPadding: 10,
        data: chartData.data.data,
        angleField: chart.axis?.y?.value,
        colorField: chart.axis?.x?.value,
        radius: 0.9,
        interactions: [{ type: 'element-active' }]
      })
      columnPlot.render()
    } else {
      columns.value = chart.columns.map(_ => ({
        key: `${_.value}`,
        dataKey: `${_.value}`,
        title: `${_.name}`,
        width: 150
      }))

      data.value = chartData.data.data.map((ele, index) => {
        return {
          ...ele,
          id: index + 'row'
        }
      })
      renderTableLocal.value = true
    }
  }
  nextTick(() => {
    ;(chartTypeList.value || content.value).scrollIntoView({
      block: 'end',
      inline: 'nearest',
      behavior: 'smooth'
    })
  })
})
const exportExcel = () => {
  const { chartData, chart } = props.copilotInfo
  const workbook = new ExcelJS.Workbook()
  const worksheet = workbook.addWorksheet('Sheet1')
  // 设置列标题
  worksheet.columns = chart.columns.map(ele => {
    return { header: ele.name, key: ele.value }
  })
  const arr = chart.columns.map(ele => ele.value)
  chartData.data.data.forEach(item => {
    worksheet.addRow(arr.map(ele => item[ele]))
  })
  // 导出excel文件
  workbook.xlsx.writeBuffer().then(buffer => {
    const blob = new Blob([buffer], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = chart.title + '.xlsx'
    link.click()
    window.URL.revokeObjectURL(url)
  })
}
const renderTableLocal = ref(false)
const changeChartType = () => {
  switchChartType(activeCommand.value)
}
const switchChartType = type => {
  renderTableLocal.value = false
  nextTick(() => {
    if (columnPlot?.chart && !columnPlot.chart.wrapperElement) {
      columnPlot.chart.wrapperElement = document.querySelector(
        `#de-${props.copilotInfo.id}-ed > div`
      )
    }

    if (columnPlot?.chart?.wrapperElement) {
      columnPlot.destroy()
    }
    if (['bar', 'line', 'pie'].includes(type)) {
      activeCommand.value = type
    }
    const { chart, msgType, msgStatus, chartData, id } = props.copilotInfo
    if (msgStatus === 1 && msgType === 'api' && chartData) {
      if (['bar', 'line'].includes(type)) {
        const chartType = type === 'bar' ? Column : Line
        const columnPlot = new chartType(`de-${id}-ed`, {
          data: chartData.data.data,
          xField: chart.axis?.x?.value,
          yField: chart.axis?.y?.value,
          legend: {
            layout: 'horizontal',
            position: 'left'
          }
        })
        columnPlot.render()
        return
      }

      if (type === 'pie') {
        columnPlot = new Pie(`de-${id}-ed`, {
          appendPadding: 10,
          data: chartData.data.data,
          angleField: chart.axis?.y?.value,
          colorField: chart.axis?.x?.value,
          radius: 0.9,
          interactions: [{ type: 'element-active' }]
        })
        columnPlot.render()
        return
      }

      columns.value = chart.columns.map(_ => ({
        key: `${_.value}`,
        dataKey: `${_.value}`,
        title: `${_.name}`,
        width: 150
      }))

      data.value = chartData.data.data.map((ele, index) => {
        return {
          ...ele,
          id: index + 'row'
        }
      })

      renderTableLocal.value = true
    }
  })
}
const chartTypeRef = ref()
const downloadChart = () => {
  if (renderTableLocal.value) {
    exportExcel()
    return
  }
  downloadCanvas('img', chartTypeRef.value, '图表')
}
watch(
  () => props.copilotInfo.loading,
  val => {
    if (!val) {
      switchChartType(renderTableLocal.value ? 'table' : activeCommand.value)
    }
  }
)

const renderTable = computed(() => {
  const { chart, msgType, msgStatus, chartData } = props.copilotInfo
  return (
    msgType === 'api' &&
    msgStatus === 1 &&
    !['bar', 'line', 'pie'].includes(chart?.type) &&
    chartData.data
  )
})
const activeCommand = ref('')
const curTypeList = [
  {
    label: '折线图',
    value: 'line',
    icon: 'icon_chart-line-c'
  },
  {
    label: '柱状图',
    icon: 'icon_dashboard_outlined-c',
    value: 'bar'
  },
  {
    label: '饼图',
    icon: 'icon_pie_outlined-c',
    value: 'pie'
  }
]

const columns = shallowRef([])
const data = shallowRef([])

const tips = computed(() => {
  const { chart, msgType, question, msgStatus } = props.copilotInfo
  if (msgType === 'api' && msgStatus === 1) {
    return chart.title
  }
  if (msgStatus === 0) {
    return '抱歉，根据已知信息无法回答这个问题，请重新描述你的问题或提供更多信息～'
  } else if (msgType === 'user') {
    return question
  }
  return ''
})
</script>

<template>
  <div
    class="dialogue-chart"
    :class="[
      copilotInfo.msgType === 'user' ? 'user-dialogue' : 'api-dialogue',
      copilotInfo.msgType === 'api' && copilotInfo.msgStatus === 1 && 'chart-dialogue'
    ]"
  >
    <el-icon style="font-size: 32px" class="dialogue-chart_icon">
      <Icon :name="copilotInfo.msgType === 'api' ? 'copilot' : 'default_avatar'" />
    </el-icon>
    <div ref="content" class="content">
      <div v-if="isWelcome" class="question-or-title" style="font-size: 16px; font-weight: 500">
        您好，我是 Copilot，很高兴为你服务～
      </div>
      <div v-else-if="isAnswer" class="question-or-title" style="font-size: 16px; font-weight: 500">
        回答中<span class="dot">...</span>
      </div>
      <div v-else class="question-or-title">
        {{ tips }}
      </div>
      <div v-if="isWelcome" class="is-welcome">您可以问我: 2020年各个销售部门销售额占比的饼图</div>
      <div
        v-else-if="copilotInfo.msgType === 'api' && copilotInfo.msgStatus === 1"
        class="chart-type"
        ref="chartTypeRef"
      >
        <div
          v-if="!copilotInfo.loading"
          ref="chartRef"
          class="column-plot_de"
          :id="`de-${copilotInfo.id}-ed`"
        >
          <el-table-v2
            v-if="renderTable || renderTableLocal"
            :columns="columns"
            :data="data"
            :width="width"
            :height="335"
            fixed
          />
        </div>
      </div>
    </div>

    <div
      ref="chartTypeList"
      class="chart-type_list"
      v-if="copilotInfo.msgType === 'api' && copilotInfo.msgStatus === 1"
    >
      <template v-if="!renderTable">
        <el-icon
          :class="!(renderTable || renderTableLocal) && 'active'"
          v-if="activeCommand"
          class="select-prefix"
        >
          <Icon :name="curTypeList.find(ele => ele.value === activeCommand).icon" />
        </el-icon>
        <el-tooltip effect="dark" content="切换图表类型" placement="top">
          <div
            v-show="renderTable || renderTableLocal"
            @click="switchChartType(activeCommand)"
            class="fake-mask_select"
          ></div>
        </el-tooltip>
        <el-tooltip effect="dark" content="切换图表类型" placement="top">
          <el-select
            popper-class="copilot-select_popper"
            class="select-copilot-list"
            :class="!(renderTable || renderTableLocal) && 'active'"
            v-model="activeCommand"
            size="small"
            @change="changeChartType"
          >
            <el-option
              v-for="item in curTypeList"
              :key="item.label"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-tooltip>
        <el-divider direction="vertical" />
      </template>
      <el-tooltip effect="dark" content="切换至明细表" placement="top">
        <el-icon
          :class="(renderTable || renderTableLocal) && 'active'"
          class="ed-icon_chart"
          @click="switchChartType('table')"
        >
          <Icon name="chart-table" />
        </el-icon>
      </el-tooltip>
      <el-divider direction="vertical" />
      <el-tooltip effect="dark" content="下载" placement="top">
        <el-icon class="ed-icon_chart" @click="downloadChart">
          <Icon name="chart-download" />
        </el-icon>
      </el-tooltip>
    </div>
  </div>
</template>

<style lang="less" scoped>
.dialogue-chart {
  display: flex;
  margin-top: 24px;
  position: relative;

  .select-copilot-list {
    width: 40px;
    position: relative;
    z-index: 100;

    :deep(.ed-input__wrapper) {
      background: transparent;
      box-shadow: none !important;
      padding-right: 4px;
      .ed-input__inner {
        visibility: hidden;
      }
    }

    &.active {
      :deep(.ed-input__wrapper) {
        background: #3370ff1a;
      }
    }
  }

  .chart-type_list {
    position: absolute;
    bottom: -36px;
    right: 0;
    display: flex;
    align-items: center;
    font-size: 24px;
    .fake-mask_select {
      width: 40px;
      height: 24px;
      cursor: pointer;
      border-radius: 4px;
      position: absolute;
      top: 50%;
      left: 0;
      transform: translateY(-50%);
      z-index: 101;
      &:hover {
        background: #1f23291a;
      }
    }
    .ed-icon_chart {
      position: relative;
      cursor: pointer;
      color: #646a73;
      &::after {
        content: '';
        position: absolute;
        top: 50%;
        left: 50%;
        width: 24px;
        height: 24px;
        background: #1f23291a;
        transform: translate(-50%, -50%);
        display: none;
        border-radius: 4px;
      }

      &:hover {
        &::after {
          display: block;
        }
      }

      &.active {
        color: var(--ed-color-primary, #3370ff);
        &::after {
          display: block;
          background: #3370ff1a;
        }
      }
    }

    .select-prefix {
      position: absolute;
      left: 4px;
      top: 50%;
      transform: translateY(-50%);
      font-size: 16px;
      color: #646a73;
      &.active {
        color: var(--ed-color-primary, #3370ff);
      }
    }
    .ed-divider--vertical {
      border-color: #1f232926;
      height: 14px;
      margin: 0 6px;
    }
  }

  &.chart-dialogue {
    margin-bottom: 52px;
  }

  &.user-dialogue {
    .content {
      background: #d6e2ff;
    }
  }

  &.api-dialogue {
    .content {
      background: #fff;
      box-shadow: 0px 4px 8px 0px #1f23291a;
    }
  }

  .column-plot_de {
    width: calc(100% - 32px);
    height: 335px;
  }

  .content {
    flex: 1;
    margin-left: 8px;
    border-radius: 8px;
    .question-or-title {
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      padding: 12px 16px;

      @keyframes identifier {
        0% {
          width: 0px;
        }

        33% {
          width: 10px;
        }

        100% {
          width: 22px;
        }
      }

      .dot {
        overflow: hidden;
        display: inline-block;
        animation: identifier 1s infinite;
      }
    }

    .chart-type {
      height: 360px;
      border-top: 1px solid #1f232926;
      padding: 12px 16px;
    }

    .is-welcome {
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-weight: 400;
      line-height: 22px;
      text-align: left;
      color: #646a73;
      margin: -8px 16px 12px 16px;
    }
  }
}
</style>
<style lang="less">
.copilot-select_popper {
  width: 128px;
}
</style>
