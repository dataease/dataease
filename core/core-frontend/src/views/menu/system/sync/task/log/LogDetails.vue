<script lang="ts" setup>
import {computed, defineEmits, onUnmounted, reactive, ref} from 'vue'
import {getTaskLogDetailApi} from "@/api/sync/syncTaskLog";
import { useI18n } from "@/hooks/web/useI18n";
const { t } = useI18n();
// 自定义指令-列表新增元素后自动滚动底部
const vScrollBottom = {
  updated: (el => {
    el.scrollTo({
      top: el.scrollHeight - el.clientHeight,
      behavior: 'smooth'
    })
  })
}

const jobLogDetailVisible = ref<boolean>(false)
const props = defineProps({
  jobLogStatus: {
    type: String,
    default: ''
  }
})
const logStatus = computed(() => {
  return props.jobLogStatus
})
const emit = defineEmits(['jobLogDetailVisibleClose'])
const logId = ref()
const logSegments = ref<{ text: string; className: string }[]>([])
const loading = ref<boolean>(false)
const timeInterval = reactive({
  timeId: null
})
let fromLineNum = 1
let pullFailCount = 0
let logSession = 0
const pullLog = (done: boolean) => {
  loading.value = true
  // 查了20次都查不到就结束
  if (pullFailCount++ > 20) {
    logRunStop(t('sync_task.log_read_finished'))
    return
  }
  const session = logSession
  const requestLogId = logId.value
  const requestLine = fromLineNum
  getTaskLogDetailApi(requestLogId, requestLine)
      .then(data => {
        if (session !== logSession || requestLogId !== logId.value) {
          return
        }
        const res = data.data
        if (done) {
          loading.value = false
        }
        if (fromLineNum != res.fromLineNum) {
          return
        }
        // 兼容后端状态检查的占位响应，不改变已读取的位置
        const placeholder = res.logContent === '...' && res.toLineNum < requestLine
        if (placeholder) {
          // 状态检查返回的 end 不代表日志文件已读完，继续从原位置读取
          if (done) {
            timeInterval.timeId = window.setTimeout(() => {
              timeInterval.timeId = null
              pullLog(true)
            }, 3000)
          }
          return
        }
        fromLineNum = Math.max(fromLineNum, res.toLineNum + 1)
        appendLogContent(res.logContent)
        pullFailCount = 0
        if (fromLineNum > res.toLineNum) {
          let resultContent
          // 内容读取结束
          if (res.end) {
            resultContent = t('sync_task.log_read_finished')
            logRunStop(`\n[${resultContent}]`, 'done')
            return
          }
          if (logStatus.value === "CONNECTION_LOST") {
            resultContent = t('sync_task.connection_lost')
            logRunStop(`\n[${resultContent}]`, 'lost')
          }
          return
        }
      })
      .catch(err => {
        if (session !== logSession || requestLogId !== logId.value) {
          return
        }
        loading.value = false
        throw err
      })
}

/**
 * 高亮关键字
 */
const errorMsgKeywords = ['ErrorCode', 'IllegalArgumentException', 'SQLSyntaxErrorException']

const appendLogContent = (content: string) => {
  content.split(new RegExp(`(${errorMsgKeywords.join('|')})`, 'g')).forEach(text => {
    if (text) {
      logSegments.value.push({
        text,
        className: errorMsgKeywords.includes(text) ? 'log-keyword' : ''
      })
    }
  })
}

const logRunStop = (content: string, type: '' | 'done' | 'lost' = '') => {
  loading.value = false
  timeInterval.timeId && window.clearInterval(timeInterval.timeId)
  timeInterval.timeId = null
  logSegments.value.push({
    text: content,
    className: type ? `log-end-message ${type}` : 'log-end-message'
  })
}
onUnmounted(() => {
  loading.value = false
  stopInterval()
})

const startInterval = (taskLogId: string, jobLogStatus: string) => {
  stopInterval()
  logId.value = taskLogId
  logSegments.value = []
  fromLineNum = 1
  pullFailCount = 0
  if (jobLogStatus === 'RUNNING') {
    pullLog(false)
    timeInterval.timeId = window.setInterval(() => {
      pullLog(false)
    }, 3000)
  } else {
    pullLog(true)
  }
}

const stopInterval = () => {
  logSession++
  loading.value = false
  timeInterval.timeId && window.clearInterval(timeInterval.timeId)
  timeInterval.timeId = null
}

const closeClick = () => {
  stopInterval()
  emit('jobLogDetailVisibleClose')
}
defineExpose({jobLogDetailVisible, startInterval, logId, stopInterval})
</script>
<template>
  <div>
    <el-drawer
      v-model="jobLogDetailVisible"
      :close-on-click-modal="false"
      size="calc(100% - 50px)"
      modal-class="log-details-drawer-fullscreen"
      direction="btt"
      :show-close="true"
      @close="closeClick"
    >
      <template #title>
        <div class="drawer-title">
          <div class="title">{{ t('sync_task.execute_log') }}</div>
        </div>
      </template>
      <div v-if="logStatus === 'RUNNING'" v-scroll-bottom class="job-log-detail-class">
        <span class="log-content"
          ><span v-for="(segment, index) in logSegments" :key="index" :class="segment.className">{{
            segment.text
          }}</span></span
        >
        <span v-if="loading">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
        </span>
      </div>
      <div v-if="logStatus != 'RUNNING'" v-scroll-bottom class="job-log-detail-class">
        <span class="log-content"
          ><span v-for="(segment, index) in logSegments" :key="index" :class="segment.className">{{
            segment.text
          }}</span></span
        >
        <span v-if="loading">
          <el-icon class="is-loading">
            <Loading />
          </el-icon>
        </span>
      </div>
    </el-drawer>
  </div>
</template>

<style scoped lang="less">
:deep(.log-details-drawer-fullscreen .el-drawer__body) {
  display: block;
  justify-content: center;
}

.job-log-detail-class {
  padding-left: 24px;
  display: flex;
  flex-direction: column;
  height: 90%;
  overflow-y: auto;
  .log-content {
    white-space: pre-wrap;
    overflow-wrap: anywhere;
    min-width: 0;
  }

  .log-keyword {
    color: red;
    font-weight: bold;
  }

  .log-end-message.done {
    color: inherit;
  }

  .log-end-message.lost {
    color: red;
  }
}
</style>
