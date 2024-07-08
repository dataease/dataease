<script lang="ts" setup>
import { PropType, computed } from 'vue'
interface Copilot {
  msgType: string
  question: string
  chart: object
  chartData: object
  msgStatus: number
}
const props = defineProps({
  copilotInfo: {
    type: Object as PropType<Copilot>,
    default: () => ({
      msgType: 'api',
      chart: {},
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
  }
})

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
    :class="copilotInfo.msgType === 'user' ? 'user-dialogue' : 'api-dialogue'"
  >
    <el-icon style="font-size: 32px" class="dialogue-chart_icon">
      <Icon :name="copilotInfo.msgType === 'api' ? 'copilot' : 'default_avatar'" />
    </el-icon>
    <div class="content">
      <div v-if="isWelcome" class="question-or-title" style="font-size: 16px; font-weight: 500">
        您好，我是 Copilot，很高兴为你服务～
      </div>
      <div v-else class="question-or-title">
        {{ tips }}
      </div>
      <div v-if="isWelcome" class="is-welcome">这是一句 Copilot 的功能描述</div>
      <div v-else-if="copilotInfo.msgType === 'api'" class="chart-type"></div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.dialogue-chart {
  display: flex;
  & + & {
    margin-bottom: 24px;
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
    }

    .chart-type {
      height: 360px;
      border-top: 1px solid #1f232926;
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
