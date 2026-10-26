<script lang="ts" setup>
import { Loading } from '@element-plus/icons-vue'
import { computed, defineEmits, onUnmounted, ref } from 'vue'
import { logMsgApi } from './api'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
// 自定义指令-列表新增元素后自动滚动底部
const vScrollBottom = {
  updated: el => {
    el.scrollTo({
      top: el.scrollHeight - el.clientHeight,
      behavior: 'smooth'
    })
  }
}

const jobLogDetailVisible = ref<boolean>(false)

const emit = defineEmits(['jobLogDetailVisibleClose'])
const logId = ref()
const taskId = ref()
const logContent = ref('')
const loading = ref<boolean>(false)

let fromLineNum = 1
const pullLog = (done: boolean) => {
  loading.value = true
  const param = {
    instanceId: logId.value,
    taskId: taskId.value
  }
  logMsgApi(param)
    .then(data => {
      if (done) {
        loading.value = false
      }
      // 添加内容
      fromLineNum = 5
      logContent.value += data.data ?? ''
      logRunStop('')
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

const logSegments = computed(() =>
  logContent.value
    .split(/(ErrorCode|IllegalArgumentException|SQLSyntaxErrorException)/g)
    .map(text => ({
      text,
      highlighted: errorMsgKeywords.includes(text)
    }))
)

const logRunStop = (content: string) => {
  loading.value = false
  logContent.value += content
}
onUnmounted(() => {
  loading.value = false
})

const startInterval = (instanceId?: string, cTaskId?: string) => {
  logId.value = instanceId
  taskId.value = cTaskId
  logContent.value = ''
  fromLineNum = 1
  pullLog(true)
}

defineExpose({ jobLogDetailVisible, startInterval })
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
      on-close="cancelClick"
    >
      <template #title>
        <div class="drawer-title">
          <div class="title">{{ t('sync_task.execute_log') }}</div>
        </div>
      </template>

      <div v-scroll-bottom class="job-log-detail-class">
        <div class="log-content">
          <template v-for="(segment, index) in logSegments" :key="index">
            <b v-if="segment.highlighted" class="log-keyword">{{ segment.text }}</b>
            <template v-else>{{ segment.text }}</template>
          </template>
        </div>
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
}

.log-content {
  white-space: pre;
}

.log-keyword {
  color: red;
}
</style>
