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
const logContent = ref<string>()
const loading = ref<boolean>(false)
const timeInterval = reactive({
  timeId: null
})
let fromLineNum = 1
let pullFailCount = 0
const pullLog = (done: boolean) => {
  loading.value = true
  // 查了20次都查不到就结束
  if (pullFailCount++ > 20) {
    logRunStop(t('sync_task.done'))
    return
  }
  getTaskLogDetailApi(logId.value, fromLineNum)
      .then(data => {
        let res = data.data
        if (done) {
          loading.value = false
        }
        if (fromLineNum != res.fromLineNum) {
          return
        }
        // 添加内容
        fromLineNum = res.toLineNum + 1
        res.logContent = highlightMsgKeywords(res.logContent)
        if (res.logContent==="...") {
          fromLineNum--
        }
        logContent.value += res.logContent
        logContent.value = logContent.value?.replaceAll('\n', '<br>')
        pullFailCount = 0
        if (fromLineNum > res.toLineNum) {
          let resultContent
          // 内容读取结束
          if (res.end) {
            resultContent = t('sync_task.done')
            logRunStop(`<br><span style="color: green;">[${resultContent}]</span>`)
            return
          }
          if (logStatus.value === "CONNECTION_LOST") {
            resultContent = t('sync_task.connection_lost')
            logRunStop(`<br><span style="color: red;">[${resultContent}]</span>`)
          }
          return
        }
      })
      .catch(err => {
        loading.value = false
        throw err
      })
}

/**
 * 高亮关键字
 */
const errorMsgKeywords = ['ErrorCode', 'IllegalArgumentException', 'SQLSyntaxErrorException']

const highlightMsgKeywords = (logContent: string) => {
  for (let i = 0; i < errorMsgKeywords.length; i++) {
    logContent = logContent.replace(
        new RegExp(errorMsgKeywords[i], 'g'),
        `<b style="color: red;">${errorMsgKeywords[i]}</b>`
    )
  }
  return logContent
}

const logRunStop = (content: string) => {
  loading.value = false
  timeInterval.timeId && window.clearInterval(timeInterval.timeId)
  timeInterval.timeId = null
  logContent.value += content
}
onUnmounted(() => {
  loading.value = false
  stopInterval()
})

const startInterval = (taskLogId: string, jobLogStatus: string) => {
  if (!timeInterval.timeId) {
    logId.value = taskLogId
    logContent.value = ''
    fromLineNum = 1
    pullFailCount = 0
    if (jobLogStatus === 'RUNNING') {
      pullLog(false)
      timeInterval.timeId = window.setInterval(() => {
        pullLog(false)
      }, 3000)
    } else {
      pullLog(true)
      stopInterval()
    }
  }
}

const stopInterval = () => {
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
        <span v-html="logContent"/>
        <span v-if="loading">
        <el-icon class="is-loading">
          <Loading/>
        </el-icon>
      </span>
      </div>
      <div v-if="logStatus != 'RUNNING'" v-scroll-bottom class="job-log-detail-class">
        <span v-html="logContent"/>
        <span v-if="loading">
        <el-icon class="is-loading">
          <Loading/>
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
  span{
    white-space: nowrap;
  }
}
</style>
