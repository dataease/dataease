<script lang="ts" setup>
import { Loading } from '@element-plus/icons-vue'
import { computed, onUnmounted, ref } from 'vue'
import { checkTaskLogConnectionApi, getTaskLogDetailApi } from '@/api/sync/syncTaskLog'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const vScrollBottom = {
  updated: el => {
    el.scrollTo({ top: el.scrollHeight - el.clientHeight, behavior: 'smooth' })
  }
}
const jobLogDetailVisible = ref(false)
const props = defineProps({ jobLogStatus: { type: String, default: '' } })
const logStatus = computed(() => props.jobLogStatus)
const emit = defineEmits(['jobLogDetailVisibleClose'])
const logId = ref<string>()
const logSegments = ref<{ text: string; className: string }[]>([])
const loading = ref(false)
const polling = ref(false)
const readFailed = ref(false)
let timeId: ReturnType<typeof window.setTimeout> | undefined
let fromLineNum = 1
let pullFailCount = 0
let logSession = 0
let openRequest = 0
let disposed = false

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
const stopInterval = () => {
  logSession++
  loading.value = false
  polling.value = false
  window.clearTimeout(timeId)
  timeId = undefined
}
const logRunStop = (content: string, type = '') => {
  stopInterval()
  logSegments.value.push({
    text: content,
    className: type ? `log-end-message ${type}` : 'log-end-message'
  })
}
const pullLog = async () => {
  const session = logSession
  const requestLogId = logId.value
  const requestLine = fromLineNum
  loading.value = true
  try {
    const data = await getTaskLogDetailApi(requestLogId, requestLine)
    if (session !== logSession || requestLogId !== logId.value) return
    const res = data.data
    if (requestLine !== res.fromLineNum) return
    // 状态检查的占位响应不能推进文件读取位置
    const placeholder = res.logContent === '...' && res.toLineNum < requestLine
    if (placeholder || !res.logContent) {
      // 长任务可能暂时没有新日志，运行中不能按空结果次数结束轮询
      if (++pullFailCount > 20 && logStatus.value !== 'RUNNING') {
        logRunStop(`\n[${t('sync_task.log_read_finished')}]`, 'done')
        return
      }
    } else {
      appendLogContent(res.logContent)
      fromLineNum = Math.max(fromLineNum, res.toLineNum + 1)
      pullFailCount = 0
    }
    if (!placeholder && res.end) {
      logRunStop(`\n[${t('sync_task.log_read_finished')}]`, 'done')
      return
    }
    // 上一次请求结束后再安排下一次，避免慢请求重叠及失败后持续弹错
    polling.value = true
    timeId = window.setTimeout(pullLog, 3000)
  } catch (error) {
    if (session !== logSession || requestLogId !== logId.value) return
    stopInterval()
    readFailed.value = true
    if (typeof error === 'string') appendLogContent(`\n[${error}]`)
  } finally {
    if (session === logSession) loading.value = false
  }
}
const openLog = async (taskLogId: string) => {
  const request = ++openRequest
  try {
    await checkTaskLogConnectionApi(taskLogId)
    if (disposed || request !== openRequest) return false
    stopInterval()
    logId.value = taskLogId
    logSegments.value = []
    fromLineNum = 1
    pullFailCount = 0
    readFailed.value = false
    jobLogDetailVisible.value = true
    void pullLog()
    return true
  } catch {
    return false
  }
}
const retryRead = async () => {
  const session = logSession
  loading.value = true
  try {
    await checkTaskLogConnectionApi(logId.value)
    if (session !== logSession) return
    readFailed.value = false
    pullFailCount = 0
    await pullLog()
  } catch {
    // 请求层展示诊断信息，保留已有日志和重试入口
  } finally {
    if (session === logSession) loading.value = false
  }
}
const closeClick = () => {
  openRequest++
  stopInterval()
  emit('jobLogDetailVisibleClose')
}
onUnmounted(() => {
  disposed = true
  openRequest++
  stopInterval()
})
defineExpose({ jobLogDetailVisible, openLog, logId, stopInterval })
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
          <el-button v-if="readFailed" :loading="loading" @click="retryRead">{{
            t('common.refresh')
          }}</el-button>
        </div>
      </template>
      <div v-if="logStatus === 'RUNNING'" v-scroll-bottom class="job-log-detail-class">
        <span class="log-content"
          ><span v-for="(segment, index) in logSegments" :key="index" :class="segment.className">{{
            segment.text
          }}</span></span
        >
        <span v-if="loading || polling">
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
        <span v-if="loading || polling">
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
