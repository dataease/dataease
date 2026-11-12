<script setup lang="ts">
import icon_succeed_filled from '@/assets/svg/icon_succeed_filled.svg'
import icon_close_filled from '@/assets/svg/icon_close_filled.svg'
import icon_replace_outlined from '@/assets/svg/icon_replace_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import icon_sync_logs_outlined from '@/assets/svg/icon_sync_logs_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import type { PropType } from 'vue'
import {
  clearEventLogs,
  getClearResult,
  getTaskEventLogListApi,
  removeTaskEventLogApi
} from '@/api/sync/syncTaskLog'
import type { TaskEventLog, TaskLogClearResult, TaskLogType } from '@/api/sync/syncTaskLog'
import dayjs from 'dayjs'
import LogDetails from './LogDetails.vue'
import FieldChangeLogDetails from './FieldChangeLogDetails.vue'
import { isTaskEventLogActive, taskLogStatusKeys } from './logStatus'
import ClearJobLogForm from './ClearJobLogForm.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { propTypes } from '@/utils/propTypes'
import { convertFilterText, FilterText } from '@/components/filter-text'

const { t } = useI18n()
const keyword = ref(null)
const imgType = ref()
const emptyDesc = ref('')
const tableLoading = ref(false)
const getEmptyImg = (): string => {
  if (keyword.value) {
    return 'tree'
  }
  return 'noneWhite'
}

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t('work_branch.relevant_content_found')
  }

  return ''
}
const drawerMainRef = ref()
const props = defineProps({
  jobId: propTypes.string.def(''),
  initialTaskType: String as PropType<TaskLogType>
})
const currentJobId = ref(props.jobId)
const initialConditions = props.initialTaskType
  ? [{ field: 'taskTypes', value: [props.initialTaskType], operator: 'in' }]
  : []
const state = reactive({
  taskLogList: [] as TaskEventLog[],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  conditions: initialConditions,
  orders: [],
  multipleSelection: []
})
const timeInterval = reactive({
  timeId: null
})
const startInterval = () => {
  if (!timeInterval.timeId) {
    timeInterval.timeId = window.setInterval(() => {
      partDataUpdate()
    }, 3000)
  }
}
const stopInterval = () => {
  timeInterval.timeId !== null && window.clearInterval(timeInterval.timeId)
  timeInterval.timeId = null
}
const buildParam = () => {
  const param = {}
  if (state.conditions?.length) {
    state.conditions.forEach(condition => {
      if (condition['value']) {
        param[condition['field']] = condition['value']
      }
    })
  }
  if (keyword.value) {
    param['keyword'] = keyword.value
  }
  if (currentJobId.value !== '') {
    param['taskId'] = currentJobId.value
  }
  return param
}
const search = () => {
  tableLoading.value = true
  getTaskEventLogListApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    buildParam()
  )
    .then(res => {
      tableLoading.value = false
      state.taskLogList = res.data.records
      if (state.taskLogList.some(isTaskEventLogActive)) {
        startInterval()
      }
      if (state.paginationConfig.currentPage > 1 && state.taskLogList.length === 0) {
        state.paginationConfig.currentPage--
        search()
      }
      state.paginationConfig.total = res.data.total
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
    })
    .catch(() => {
      tableLoading.value = false
    })
}
const partDataUpdate = () => {
  const param = buildParam()
  getTaskEventLogListApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    param
  ).then(res => {
    let isRunning = false
    const resDataMap = res.data.records.reduce((acc, cur) => {
      acc[cur.id] = cur
      return acc
    }, {})
    state.taskLogList.forEach(item => {
      const resItem = resDataMap[item.id]
      if (resItem) {
        item.status = resItem.status
        item.deletable = resItem.deletable
        item.executorEndTime = resItem.executorEndTime
        if (isTaskEventLogActive(resItem)) {
          isRunning = true
        }
      }
    })
    if (isRunning) {
      startInterval()
    } else {
      stopInterval()
    }
  })
}
onMounted(() => {
  fillFilterText()
  search()
  loadCleanupResult()
})
onUnmounted(() => {
  cleanupDisposed = true
  stopCleanupPolling()
  stopInterval()
})
const pageChange = (index: any) => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  search()
}
const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return dayjs(new Date(value)).format('YYYY-MM-DD HH:mm')
}

const getLogStatusIcon = value => {
  const iconObj = {
    icon: '-',
    color: ''
  }
  if (value === 'Success'.toUpperCase()) {
    iconObj.icon = icon_succeed_filled
    iconObj.color = '#34C724'
  }
  if (
    value === 'Fail_retry'.toUpperCase() ||
    value === 'No_process'.toUpperCase() ||
    value === 'Fail'.toUpperCase() ||
    value === 'Termination'.toUpperCase() ||
    value === 'connection_lost'.toUpperCase()
  ) {
    iconObj.icon = icon_close_filled
    iconObj.color = '#F54A45'
  }
  if (value === 'Running'.toUpperCase()) {
    iconObj.icon = icon_replace_outlined
    iconObj.color = '#2c5fd9'
  }
  if (['PARTIAL', 'WAITING_INITIALIZATION', 'RECOVERY_REQUIRED'].includes(value)) {
    iconObj.icon = icon_replace_outlined
    iconObj.color = '#FF8800'
  }
  if (value === 'SUPERSEDED') {
    iconObj.icon = icon_replace_outlined
    iconObj.color = '#8F959E'
  }
  return iconObj
}
const getStatusLabel = value => {
  const key = value && taskLogStatusKeys[value.toUpperCase()]
  return key ? t(key) : '-'
}
const delHandler = (row: TaskEventLog) => {
  ElMessageBox.confirm(t('sync_task.confirm_delete_msg'), {
    confirmButtonText: t('sync_task.delete'),
    cancelButtonText: t('sync_datasource.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    removeTaskEventLogApi(row.taskType, row.id).then(() => {
      ElMessage.success(t('sync_task.op_success'))
      search()
    })
  })
}

const logStatus = ref()
const jobLogDetailRef = ref()
const fieldLogDetailRef = ref<InstanceType<typeof FieldChangeLogDetails>>()
const showLogDetail = (row: TaskEventLog) => {
  if (row.taskType === 'FIELD_CHANGE') {
    void fieldLogDetailRef.value?.openLog(row.id)
    return
  }
  logStatus.value = row.status
  void jobLogDetailRef.value?.openLog(row.id)
}

const jobLogDetailVisibleClose = () => {
  jobLogDetailRef.value.jobLogDetailVisible = false
}

const clearJobLogDialogRef = ref()
const closeClearDialog = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
}
const showClearJobLogDialogFormVisible = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = true
}

const clearLogForm = reactive({
  jobId: '',
  clearType: '1'
})
const cleanupResult = ref<TaskLogClearResult | null>(null)
const cleanupVisible = ref(false)
const cleanupSubmitting = ref(false)
const cleanupReadFailed = ref(false)
const cleanupActive = computed(() =>
  ['QUEUED', 'RUNNING'].includes(cleanupResult.value?.status ?? '')
)
let cleanupTimer: ReturnType<typeof setTimeout> | undefined
let cleanupDisposed = false
let cleanupGeneration = 0
const stopCleanupPolling = () => {
  if (cleanupTimer !== undefined) clearTimeout(cleanupTimer)
  cleanupTimer = undefined
}
const loadCleanupResult = async () => {
  const generation = ++cleanupGeneration
  stopCleanupPolling()
  try {
    const { data } = await getClearResult()
    if (cleanupDisposed || generation !== cleanupGeneration) return
    const wasActive = cleanupActive.value
    cleanupResult.value = data
    cleanupReadFailed.value = false
    if (wasActive && !cleanupActive.value) search()
    if (cleanupActive.value) cleanupTimer = setTimeout(loadCleanupResult, 3000)
  } catch {
    if (cleanupDisposed || generation !== cleanupGeneration) return
    cleanupReadFailed.value = true
  }
}
const showCleanupResult = () => {
  cleanupVisible.value = true
  loadCleanupResult()
}
const clearJobLog = (clearTypeLabel: string) => {
  ElMessageBox.confirm(t('sync_task.confirm_clear_msg', [clearTypeLabel]), {
    confirmButtonText: t('sync_task.clear'),
    cancelButtonText: t('sync_datasource.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    if (cleanupSubmitting.value) return
    cleanupSubmitting.value = true
    clearEventLogs({ ...clearLogForm, jobId: currentJobId.value })
      .then(({ data }) => {
        if (cleanupDisposed) return
        cleanupGeneration++
        stopCleanupPolling()
        cleanupResult.value = data
        cleanupReadFailed.value = false
        clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
        ElMessage.success(
          t(data.status === 'SUCCEEDED' ? 'sync_task.cleanup_empty' : 'sync_task.cleanup_submitted')
        )
        cleanupVisible.value = true
        if (cleanupActive.value) cleanupTimer = setTimeout(loadCleanupResult, 1000)
      })
      .catch(() => {
        // 参数错误由统一拦截器展示，保留清理弹窗便于处理
      })
      .finally(() => {
        cleanupSubmitting.value = false
      })
  })
}
const filterOption = [
  {
    type: 'enum',
    option: [
      { id: 'DATA_SYNC', name: t('sync_task_log.data_sync') },
      { id: 'FIELD_CHANGE', name: t('sync_task_log.field_change') }
    ],
    field: 'taskTypes',
    title: t('sync_task_log.task_type'),
    operate: 'in'
  },
  {
    type: 'enum',
    option: [
      {
        id: 'SUCCESS',
        name: t('sync_task.status_success')
      },
      {
        id: 'FAIL',
        name: t('sync_task.status_failed')
      },
      {
        id: 'RUNNING',
        name: t('sync_task.status_running')
      },
      {
        id: 'CONNECTION_LOST',
        name: t('sync_task.status_connection_lost')
      },
      {
        id: 'TERMINATION',
        name: t('sync_task.status_terminated')
      },
      {
        id: 'PARTIAL',
        name: t('sync_task_log.partial')
      },
      {
        id: 'WAITING_INITIALIZATION',
        name: t('sync_task_log.pending_initialization')
      },
      {
        id: 'RECOVERY_REQUIRED',
        name: t('sync_task_log.recovery_required')
      },
      {
        id: 'SUPERSEDED',
        name: t('sync_task_log.superseded')
      }
    ],
    field: 'status',
    title: t('sync_task.execute_result'),
    operate: 'in'
  },
  {
    type: 'time',
    option: [],
    property: {
      showType: 'datetimerange',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'YYYY-MM-DD HH:mm:ss',
      rangeSeparator: '-',
      startPlaceholder: t('sync_datasource.start_time'),
      endPlaceholder: t('sync_datasource.end_time')
    },
    field: 'lastExecuteTime',
    title: t('sync_task.execute_time'),
    operate: 'between'
  }
]
const searchCondition = conditions => {
  state.conditions = conditions
  // 移除跳转预设的类型筛选时，同时解除入口携带的任务范围
  if (props.initialTaskType && !conditions.some(condition => condition.field === 'taskTypes')) {
    currentJobId.value = ''
  }
  state.paginationConfig.currentPage = 1
  search()
  fillFilterText()
  drawerMainClose()
}
const fillFilterText = () => {
  const textArray = state.conditions?.length
    ? convertFilterText(state.conditions, filterOption)
    : []
  state.filterTexts = [...textArray]
  Object.assign(state.filterTexts, textArray)
}
const clearFilter = (params?: number) => {
  let index = params ? params : 0
  if (isNaN(index)) {
    state.filterTexts = []
  } else {
    state.filterTexts.splice(index, 1)
  }
  drawerMainRef.value.clearFilter(index)
}

const drawerMainOpen = async () => {
  drawerMainRef.value.init()
}
const drawerMainClose = () => {
  drawerMainRef.value.close()
}
</script>

<template>
  <div class="source-ds-table_log de-search-table">
    <div
      :class="!!state.multipleSelection.length && 'source-ds-table-selection'"
      class="source-ds de-search-table"
    >
      <el-row class="ds-table__filter top-operate">
        <el-col :span="12">
          <el-button
            @click="showClearJobLogDialogFormVisible"
            :loading="cleanupSubmitting"
            :disabled="cleanupActive"
            type="primary"
            >{{ t('sync_task.clear_log') }}</el-button
          >
          <el-button @click="showCleanupResult">{{ t('sync_task.cleanup_result') }}</el-button>
        </el-col>
        <el-col :span="12" class="right-filter">
          <el-input
            v-model="keyword"
            clearable
            :placeholder="t('sync_task.search_input_name_id_placeholder')"
            @change="search"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                  ><icon_searchOutline_outlined class="svg-icon"
                /></Icon>
              </el-icon>
            </template>
          </el-input>
          <el-button
            @click="drawerMainOpen"
            :plain="!!state.conditions.length"
            :class="state.conditions.length ? 'filter-condition-button' : 'filter-button'"
          >
            <template #icon>
              <Icon name="icon-filter"><iconFilter class="svg-icon" /></Icon>
            </template>
            {{
              t('common.filter') + (state.conditions.length ? `(${state.conditions?.length})` : '')
            }}
          </el-button>
        </el-col>
      </el-row>
      <filter-text
        @clear-filter="clearFilter"
        :total="state.paginationConfig.total"
        :filter-texts="state.filterTexts"
      ></filter-text>
      <div :class="[state.filterTexts.length ? 'is-in-filter' : 'source-ds-table__content']">
        <GridTable
          ref="multipleTableRef"
          :pagination="state.paginationConfig"
          :table-data="state.taskLogList"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="popper-max-width"
          @current-change="pageChange"
          @size-change="sizeChange"
          :data-loading="tableLoading"
          :show-empty-img="!tableLoading"
        >
          <el-table-column key="id" show-overflow-tooltip prop="id" :label="t('sync_task.log_id')">
            <template #default="scope">
              <span>{{ scope.row.id }}</span>
            </template>
          </el-table-column>
          <el-table-column
            key="name"
            show-overflow-tooltip
            prop="name"
            :label="t('sync_task.task_name')"
            min-width="150"
          >
            <template #default="scope">
              <span>{{ scope.row.jobName }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="taskType" :label="t('sync_task_log.task_type')" min-width="120">
            <template #default="scope">
              <span :class="{ 'field-change-type': scope.row.taskType === 'FIELD_CHANGE' }">{{
                t(
                  scope.row.taskType === 'FIELD_CHANGE'
                    ? 'sync_task_log.field_change'
                    : 'sync_task_log.data_sync'
                )
              }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="executorStartTime"
            :label="t('sync_task.execute_time')"
            min-width="170"
          >
            <template #default="scope">
              <span>{{ timestampFormatDate(scope.row.executorStartTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="lastExecutedStatus"
            key="lastExecutedStatus"
            :label="t('sync_task.execute_result')"
            show-overflow-tooltip
            min-width="180"
          >
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <el-icon
                  size="16px"
                  v-if="scope.row.status"
                  :class="scope.row.status === 'RUNNING' ? 'is-loading' : ''"
                >
                  <Icon :style="'color:' + getLogStatusIcon(scope.row.status).color"
                    ><component
                      :is="getLogStatusIcon(scope.row.status).icon"
                      :style="'color:' + getLogStatusIcon(scope.row.status).color"
                      class="svg-icon"
                    ></component
                  ></Icon>
                </el-icon>
                <span style="padding: 0 8px">{{ getStatusLabel(scope.row.status) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column fixed="right" key="_operation" :label="t('sync_task.op')" width="100">
            <template #default="scope">
              <div class="operate-icon-container">
                <el-tooltip
                  effect="dark"
                  :content="
                    t(
                      scope.row.taskType === 'FIELD_CHANGE'
                        ? 'sync_task_log.details'
                        : 'sync_task.view_execute_log'
                    )
                  "
                  placement="top"
                >
                  <template #default>
                    <el-button text @click="showLogDetail(scope.row)" class="detail-button">
                      <template #icon>
                        <Icon name="icon_sync_logs_outlined"
                          ><icon_sync_logs_outlined class="svg-icon"
                        /></Icon>
                      </template>
                    </el-button>
                  </template>
                </el-tooltip>
                <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
                  <el-button text :disabled="!scope.row.deletable" @click="delHandler(scope.row)">
                    <template #icon>
                      <Icon name="icon_delete-trash_outlined"
                        ><icon_deleteTrash_outlined class="svg-icon"
                      /></Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
  </div>
  <el-dialog v-model="cleanupVisible" :title="t('sync_task.cleanup_result')" width="620px">
    <p>{{ t('sync_task.cleanup_retention') }}</p>
    <p v-if="cleanupReadFailed">{{ t('sync_task.cleanup_read_failed') }}</p>
    <template v-if="cleanupResult">
      <p>
        {{ t('sync_task.cleanup_status') }}：{{
          t(`sync_task.cleanup_${cleanupResult.status.toLowerCase()}`)
        }}
      </p>
      <p>{{ t('sync_task.cleanup_total', [cleanupResult.total]) }}</p>
      <p>{{ dayjs(cleanupResult.createdTime).format('YYYY-MM-DD HH:mm:ss') }}</p>
      <p v-if="cleanupResult.status === 'FAILED'">{{ t('sync_task.cleanup_failed_help') }}</p>
      <pre v-if="cleanupResult.message" class="cleanup-error-message">{{
        cleanupResult.message
      }}</pre>
    </template>
    <p v-else-if="!cleanupReadFailed">{{ t('sync_task.cleanup_no_result') }}</p>
    <template #footer>
      <el-button @click="loadCleanupResult">{{ t('sync_task.cleanup_refresh') }}</el-button>
      <el-button @click="cleanupVisible = false">{{ t('sync_task.cleanup_close') }}</el-button>
    </template>
  </el-dialog>
  <log-details
    ref="jobLogDetailRef"
    :job-log-status="logStatus"
    @job-log-detail-visible-close="jobLogDetailVisibleClose"
    @refresh-list="search"
  />
  <FieldChangeLogDetails ref="fieldLogDetailRef" @closed="search" />
  <clear-job-log-form
    :model-value="clearLogForm"
    ref="clearJobLogDialogRef"
    @clear-job-log="clearJobLog"
    @close-clear-dialog="closeClearDialog"
  />
  <drawer-main
    :filter-options="filterOption"
    :initial-conditions="initialConditions"
    @trigger-filter="searchCondition"
    ref="drawerMainRef"
  ></drawer-main>
</template>

<style lang="less">
.source-ds-table_log .field-change-type {
  color: var(--ed-color-primary);
}
.cleanup-error-message {
  white-space: pre-wrap;
  overflow-wrap: anywhere;
  max-height: 280px;
  overflow: auto;
}

.source-ds-table_log {
  line-height: 1.15;
  border-radius: 12px !important;
  height: calc(100% - 60px);
  box-sizing: border-box;
  background: #fff;
  padding: 24px;
  .source-ds-table__content {
    height: calc(100vh - 276px);
  }
  .operate-icon-container {
    .detail-button {
      margin: 0 4px 0 0;
    }
  }
}
</style>
