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
import { onMounted, reactive, ref } from 'vue'
import { instanceGridApi, instanceDelApi } from './api'
import { find } from 'lodash-es'
import dayjs from 'dayjs'
import LogDetails from './LogDetails.vue'
import ClearJobLogForm from './ClearJobLogForm.vue'
import { useI18n } from '@/hooks/web/useI18n'
import { convertFilterText, FilterText } from '@/components/filter-text'
import { useEmitt } from '@/hooks/web/useEmitt'
const { t } = useI18n()
const keyword = ref()
const imgType = ref()
const emptyDesc = ref('')
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
  task: {
    type: Object,
    default: null
  }
})

const state = reactive({
  taskLogList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  conditions: [],
  multipleSelection: []
})

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
  return param
}
const tableLoading = ref(false)
const search = () => {
  tableLoading.value = true
  const param = buildParam()
  instanceGridApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, param)
    .then(res => {
      state.taskLogList = res.data.records
      if (state.paginationConfig.currentPage > 1 && state.taskLogList.length === 0) {
        state.paginationConfig.currentPage--
        search()
      }
      state.paginationConfig.total = res.data.total
      imgType.value = getEmptyImg()
      emptyDesc.value = getEmptyDesc()
    })
    .finally(() => {
      tableLoading.value = false
    })
}
const noticeCall = args => {
  const argObj = JSON.parse(args)
  if (!state.taskLogList?.length || !argObj?.instanceId) {
    return
  }
  if (state.taskLogList.some(item => item['id'] === argObj.instanceId)) {
    search()
  }
}
onMounted(() => {
  tableLoading.value = true
  useEmitt({ name: 'report-notice-call', callback: noticeCall })
  if (props.task) {
    keyword.value = props.task.name
  }
  search()
})

const pageChange = (index: any) => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.currentPage = 1
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
  if (value === 2) {
    iconObj.icon = icon_succeed_filled
    iconObj.color = '#34C724'
  }
  if (value === 3) {
    iconObj.icon = icon_close_filled
    iconObj.color = '#F54A45'
  }
  if (value === 1) {
    iconObj.icon = icon_replace_outlined
    iconObj.color = '#2c5fd9'
  }
  return iconObj
}
const taskLogStatus = [
  { label: t('report.last_status_running'), value: 1 },
  { label: t('report.last_status_success'), value: 2 },
  { label: t('report.last_status_fail'), value: 3 }
]
const getStatusLabel = value => {
  if (value) {
    const status = find(taskLogStatus, ['value', value])
    if (status) {
      return status.label
    }
  }
  return '-'
}
const delHandler = row => {
  ElMessageBox.confirm(t('commons.confirm') + t('common.delete') + t('sync_task.log'), {
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    const param = {
      instanceId: row.id
    }
    instanceDelApi(param).then(() => {
      ElMessage.success(t('common.delete_success'))
      search()
    })
  })
}

const jobLogDetailRef = ref()
const showLogDetail = row => {
  jobLogDetailRef.value.jobLogDetailVisible = true
  jobLogDetailRef.value?.startInterval(row.id, null)
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
  taskId: '',
  timeFlag: '1'
})
const clearJobLog = (clearTypeLabel: string) => {
  if (!clearTypeLabel) {
    clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
    return
  }
  ElMessageBox.confirm(t('sync_task.confirm_clear_msg', [t(`sync_task.${clearTypeLabel}`)]), {
    confirmButtonText: t('sync_task.clear'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    instanceDelApi(clearLogForm).then(() => {
      clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
      ElMessage({
        message: t('sync_task.op_success_refresh'),
        type: 'success'
      })
      search()
    })
  })
}
const filterOption = [
  {
    type: 'enum',
    option: [
      {
        id: '2',
        name: t('sync_task.status_success')
      },
      {
        id: '3',
        name: t('sync_task.status_failed')
      },
      {
        id: '1',
        name: t('report.last_status_running')
      }
    ],
    field: 'execStatusList',
    title: t('sync_task.execute_result'),
    operate: 'in'
  },
  {
    type: 'time',
    option: [],
    property: {
      showType: 'datetimerange',
      format: 'YYYY-MM-DD HH:mm:ss',
      valueFormat: 'x',
      rangeSeparator: '-',
      startPlaceholder: t('report.start_time'),
      endPlaceholder: t('report.end_time')
    },
    field: 'timeList',
    title: t('sync_task.execute_time'),
    operate: 'between'
  }
]
const searchCondition = conditions => {
  state.conditions = conditions
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
  <div
    :class="!!state.multipleSelection.length && 'report-instance-table-selection'"
    class="report-instance-table de-search-table report-instance_table"
  >
    <el-row class="report-instance-table__filter top-operate">
      <el-col :span="12">
        <el-button @click="showClearJobLogDialogFormVisible" type="primary">{{
          t('sync_task.clear_log')
        }}</el-button>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input v-model="keyword" clearable :placeholder="t('auth.search_name')" @change="search">
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
    <div :class="[state.filterTexts.length ? 'is-in-filter' : 'report-instance-table__content']">
      <GridTable
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.taskLogList"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        :show-empty-img="!tableLoading"
      >
        <el-table-column
          key="id"
          show-overflow-tooltip
          prop="id"
          :label="`${t('sync_task.log')} ID`"
        >
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
            <span>{{ scope.row.name }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="startTime" :label="t('sync_task.execute_time')" min-width="170">
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.startTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="execStatus"
          key="execStatus"
          :label="t('sync_task.execute_result')"
          show-overflow-tooltip
          min-width="180"
        >
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon
                size="16px"
                v-if="scope.row.execStatus"
                :class="scope.row.execStatus === 1 ? 'is-loading' : ''"
              >
                <Icon
                  ><component
                    :style="'color:' + getLogStatusIcon(scope.row.execStatus).color"
                    :is="getLogStatusIcon(scope.row.execStatus).icon"
                  ></component
                ></Icon>
              </el-icon>
              <span style="padding: 0 8px 0 8px">{{ getStatusLabel(scope.row.execStatus) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          key="_operation"
          :label="t('sync_task.operation')"
          width="90"
        >
          <template #default="scope">
            <div class="operate-icon-container">
              <el-tooltip
                effect="dark"
                :content="t('sync_task.view_execute_log')"
                placement="top"
                v-if="scope.row.execStatus === 3"
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
                <el-button text @click="delHandler(scope.row)">
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
  <log-details
    ref="jobLogDetailRef"
    @job-log-detail-visible-close="jobLogDetailVisibleClose"
    @refresh-list="search"
  />
  <clear-job-log-form
    :model-value="clearLogForm"
    ref="clearJobLogDialogRef"
    @clear-job-log="clearJobLog"
    @close-clear-dialog="closeClearDialog"
  />
  <drawer-main
    :filter-options="filterOption"
    @trigger-filter="searchCondition"
    ref="drawerMainRef"
  ></drawer-main>
</template>

<style scoped lang="less">
.operate-icon-container {
  float: right;
  .detail-button {
    margin: 0 4px 0 0;
  }
  :nth-child(2) {
    margin-left: 0px;
  }
}
.report-instance-table {
  height: calc(100% - 88px);
  box-sizing: border-box;
  margin-top: 12px;
  background: white;
  padding: 24px;
  border-radius: 4px;

  .report-instance-table__content {
    height: calc(100vh - 274px);
  }

  .is-in-filter {
    height: calc(100vh - 324px);
  }
}

.report-instance-table-selection {
  height: calc(100% - 126px);
  .report-instance-table__content {
    height: calc(100vh - 313px);
  }

  .is-in-filter {
    height: calc(100vh - 363px);
  }
}

.right-filter {
  .ed-input__wrapper {
    padding-left: 12px;
    padding-right: 12px;
  }
  .filter-button {
    &:hover {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #f5f6f7;
      outline: 0;
    }
    &:focus {
      color: #bbbfc4;
      border-color: #bbbfc4;
      background-color: #eff0f1;
      outline: 0;
    }
  }
}
</style>
