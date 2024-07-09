<script lang="ts" setup>
import { PropType, computed, onMounted, shallowRef, ref, nextTick } from 'vue'
import { Column, Line } from '@antv/g2plot'
import { downloadCanvas } from '@/utils/imgUtils'
import ExcelJS from 'exceljs'
interface Copilot {
  msgType: string
  question: string
  chart: object
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
onMounted(() => {
  const { chart, msgType, msgStatus, chartData, id } = props.copilotInfo
  if (msgStatus === 1 && msgType === 'api' && chartData) {
    if (['bar', 'line'].includes(chart.type)) {
      isLine.value = chart.type === 'line'
      const chartType = chart.type === 'bar' ? Column : Line
      columnPlot = new chartType(`de-${id}-ed`, {
        data: chartData.data.data,
        xField: chart.axis.x,
        yField: chart.axis.y,
        legend: {
          layout: 'horizontal',
          position: 'left'
        }
      })
      columnPlot.render()
    } else {
      columns.value = chartData.data.fields.map(_ => ({
        key: `${_.originName}`,
        dataKey: `${_.originName}`,
        title: `${_.originName}`,
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
  worksheet.columns = chartData.data.fields.map(ele => {
    return { header: ele.originName, key: ele.originName }
  })
  const arr = chartData.data.fields.map(ele => ele.originName)
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
const switchChartType = type => {
  columnPlot?.destroy()
  isLine.value = type === 'line'
  const { chart, msgType, msgStatus, chartData, id } = props.copilotInfo
  renderTableLocal.value = false
  if (msgStatus === 1 && msgType === 'api' && chartData) {
    if (['bar', 'line'].includes(type)) {
      const chartType = type === 'bar' ? Column : Line
      const columnPlot = new chartType(`de-${id}-ed`, {
        data: chartData.data.data,
        xField: chart.axis.x,
        yField: chart.axis.y,
        legend: {
          layout: 'horizontal',
          position: 'left'
        }
      })
      columnPlot.render()
      return
    }

    columns.value = chartData.data.fields.map(_ => ({
      key: `${_.originName}`,
      dataKey: `${_.originName}`,
      title: `${_.originName}`,
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
const chartTypeRef = ref()
const downloadChart = () => {
  if (renderTableLocal.value) {
    exportExcel()
    return
  }
  downloadCanvas('img', chartTypeRef.value, '图表')
}

const renderTable = computed(() => {
  const { chart, msgType, msgStatus, chartData } = props.copilotInfo
  return (
    msgType === 'api' && msgStatus === 1 && !['bar', 'line'].includes(chart?.type) && chartData.data
  )
})

const isLine = ref(false)

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
      <div v-if="isWelcome" class="is-welcome">这是一句 Copilot 的功能描述</div>
      <div
        v-else-if="copilotInfo.msgType === 'api' && copilotInfo.msgStatus === 1"
        class="chart-type"
        ref="chartTypeRef"
      >
        <div class="column-plot_de" :id="`de-${copilotInfo.id}-ed`">
          <el-table-v2
            v-if="renderTable || renderTableLocal"
            :columns="columns"
            :data="data"
            :width="718"
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
      <el-tooltip effect="dark" content="切换至柱状图" placement="top">
        <el-icon v-show="isLine" @click="switchChartType('bar')">
          <Icon name="chart-bar" />
        </el-icon>
      </el-tooltip>

      <el-tooltip effect="dark" content="切换至折线图" placement="top">
        <el-icon style="font-size: 16px" v-show="!isLine" @click="switchChartType('line')">
          <Icon name="icon_chart-line" />
        </el-icon>
      </el-tooltip>

      <el-divider direction="vertical" />
      <el-tooltip effect="dark" content="切换至明细表" placement="top">
        <el-icon @click="switchChartType('table')">
          <Icon name="chart-table" />
        </el-icon>
      </el-tooltip>

      <el-divider direction="vertical" />
      <el-tooltip effect="dark" content="下载" placement="top">
        <el-icon @click="downloadChart">
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

  .chart-type_list {
    position: absolute;
    bottom: -36px;
    right: 0;
    display: flex;
    align-items: center;
    font-size: 24px;
    .ed-icon {
      position: relative;
      cursor: pointer;
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
    width: 718px;
    height: 335px;
  }

  .content {
    flex: 1;
    margin-left: 8px;
    border-radius: 8px;
    .question-or-title {
      font-family: PingFang SC;
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
      font-family: PingFang SC;
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
