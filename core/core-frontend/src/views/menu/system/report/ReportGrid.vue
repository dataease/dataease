<script lang="ts" setup>
import icon_succeed_filled from '@/assets/svg/icon_succeed_filled.svg'
import icon_close_filled from '@/assets/svg/icon_close_filled.svg'
import icon_sync_progress from '@/assets/svg/icon_sync_progress.svg'
import iconMaybe_outlined from '@/assets/svg/icon-maybe_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import icon_syncPlayRound_outlined from '@/assets/svg/icon_sync-play-round_outlined.svg'
import { onMounted, reactive, ref, defineEmits } from 'vue'
import {
  reportGridApi,
  reportFireApi,
  reportStopApi,
  reportStartApi,
  reportDelApi,
  isOrgAdminApi
} from './api'
import { queryAllSubjectsApi } from '@/api/auth'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import ReportForm from './form/ReportForm.vue'
import { find, map } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import dayjs from 'dayjs'
import LogDetails from './LogDetails.vue'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import { convertFilterText, FilterText } from '@/components/filter-text'
import { filterOption } from './options'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useCache } from '@/hooks/web/useCache'

interface FieldSort {
  field: string
  type: boolean
}
const { t } = useI18n()
const isOrgAdmin = ref(false)
const { wsCache } = useCache()
const reportFormDialogRef = ref()
const drawerMainRef = ref()
const taskId = ref<string>('')
const keyword = ref(null)
const multipleTableRef = ref()
const tableLoading = ref<boolean>(false)
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
const state = reactive({
  taskList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  conditions: [],
  orders: [] as FieldSort[],
  multipleSelection: []
})
state.filterTexts = []
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

const getMoreList = row => {
  const stopMenu = {
    label: t('sync_task.stop'),
    divided: false,
    command: 'stop'
  }
  const startMenu = {
    label: t('sync_task.start'),
    divided: false,
    command: 'start'
  }
  const moreList = [
    {
      label: t('sync_task.show_log'),
      divided: false,
      command: 'showLog'
    },
    {
      label: t('commons.delete'),
      divided: false,
      command: 'delete'
    }
  ]
  if (row.status === 2) {
    moreList.splice(0, 0, startMenu)
  } else if (row.status < 2) {
    moreList.splice(0, 0, stopMenu)
  }
  return moreList
}
const emits = defineEmits(['openTaskLog'])
const openTaskLogList = row => {
  emits('openTaskLog', { id: row.id, name: row.name })
}
const moreHandler = (cmd: string, row) => {
  if (cmd === 'stop' || cmd === 'start') {
    changeStatus(row)
    return
  }
  if (cmd === 'showLog') {
    openTaskLogList(row)
    return
  }
  if (cmd === 'delete') {
    delHandler(row)
    return
  }
}

const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return dayjs(new Date(value)).format('YYYY-MM-DD HH:mm:ss')
}

const lastStatus = [
  { label: t('report.last_status_running'), value: 1 },
  { label: t('report.last_status_success'), value: 2 },
  { label: t('report.last_status_fail'), value: 3 }
]

const taskStatus = [
  { label: t('report.status_wait'), value: 0 },
  { label: t('report.status_send'), value: 1 },
  { label: t('report.status_stop'), value: 2 },
  { label: t('report.status_finish'), value: 3 }
]

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
    iconObj.icon = icon_sync_progress
    iconObj.color = '#2c5fd9'
  }
  return iconObj
}
const getStatusLabel = value => {
  if (value !== null && value !== undefined) {
    const status = find(taskStatus, ['value', value])
    if (status) {
      return status.label
    }
  }
  return '-'
}

const getLastStatusLabel = value => {
  if (value !== null && value !== undefined) {
    const status = find(lastStatus, ['value', value])
    if (status) {
      return status.label
    }
  }
  return '-'
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
  if (state.orders?.length) {
    state.orders.forEach(item => {
      param[item['field']] = item.type
    })
  }
  return param
}
const search = () => {
  const param = buildParam()
  tableLoading.value = true
  reportGridApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, param)
    .then(res => {
      state.taskList = res.data.records
      if (state.paginationConfig.currentPage > 1 && state.taskList.length === 0) {
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
  if (!state.taskList?.length || !argObj?.taskId) {
    return
  }
  if (state.taskList.some(item => item['id'] === argObj.taskId)) {
    search()
  }
}

onMounted(async () => {
  useEmitt({ name: 'report-notice-call', callback: noticeCall })
  const res = await isOrgAdminApi()
  isOrgAdmin.value = res.data
  search()
})

const batchDelHandler = () => {
  ElMessageBox.confirm(t('report.batch_confirm'), {
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('commons.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    tableLoading.value = true
    reportDelApi(map(state.multipleSelection, 'id')).then(() => {
      tableLoading.value = false
      ElMessage({
        message: t('free.batch') + t('common.delete_success'),
        type: 'success'
      })
      search()
    })
  })
}

const handleSelectionChange = (rows: any) => {
  state.multipleSelection = rows
}
const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
}

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
const sortChange = param => {
  state.orders = []
  if (param.order && param.prop === 'lastExecTime') {
    const type = param.order.substring(0, param.order.indexOf('ending'))
    state.orders.push({
      field: 'timeDesc',
      type: type !== 'asc'
    })
    search()
  }
}

const handleAddTask = () => {
  reportFormDialogRef.value.reportFormVisible = true
}

const taskAddVisibleClose = () => {
  taskId.value = ''
  reportFormDialogRef.value.reportFormVisible = false
}

const refreshList = () => {
  search()
}

const edit = row => {
  taskId.value = row.id
  reportFormDialogRef.value.edit(taskId.value)
}

const changeStatus = row => {
  ElMessageBox.confirm(
    (row.status === 2 ? t('sync_task.start') : t('sync_task.stop')) + t('sync_task.task_text'),
    {
      confirmButtonText: '',
      cancelButtonText: t('commons.cancel'),
      showCancelButton: true,
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    }
  ).then(() => {
    if (row.status === 2) {
      reportStartApi(row.id).then(() => {
        ElMessage.success(t('sync_task.start') + t('sync_task.status_success'))
        search()
      })
    } else {
      reportStopApi(row.id).then(() => {
        ElMessage.success(t('sync_task.stop') + t('sync_task.status_success'))
        search()
      })
    }
  })
}

const execute = row => {
  const key = `fire-report-${row.id}`
  if (wsCache.get(key)) {
    ElMessage.error(t('report.fire_now_tips'))
    return
  }
  if (row.status === 1) {
    ElMessage.error(t('report.task_running_tips'))
    return
  }
  ElMessageBox.confirm(t('commons.confirm') + t('sync_task.running_one'), {
    confirmButtonText: t('commons.confirm'),
    cancelButtonText: t('commons.cancel'),
    showCancelButton: true,
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    wsCache.set(key, 1, { exp: 5 })
    reportFireApi(row.id).then(() => {
      ElMessage.success(t('report.start_success'))
      search()
    })
  })
}

const delHandler = row => {
  ElMessageBox.confirm(t('data_source.sure_to_delete'), {
    confirmButtonText: t('commons.delete'),
    cancelButtonText: t('commons.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    tableLoading.value = true
    reportDelApi([row.id]).then(() => {
      tableLoading.value = false
      ElMessage.success(t('commons.delete_success'))
      search()
    })
  })
}

const jobLogDetailRef = ref()
const showLogDetail = row => {
  jobLogDetailRef.value.jobLogDetailVisible = true
  jobLogDetailRef.value?.startInterval(null, row.id)
}

const jobLogDetailVisibleClose = () => {
  jobLogDetailRef.value.jobLogDetailVisible = false
}

const drawerMainOpen = async () => {
  if (!isOrgAdmin.value && filterOption.length === 4) {
    filterOption.splice(0, 1)
  } else if (isOrgAdmin.value && filterOption.length === 3) {
    filterOption.splice(0, 0, {
      type: 'select',
      option: [],
      field: 'uidList',
      title: t('report.creator'),
      operate: 'in',
      property: {
        placeholder: t('report.creator')
      }
    })
    const res = await queryAllSubjectsApi(0)
    let options = res.data
    if (!options) {
      options = []
    }
    /*options.splice(0, 0, {
      id: "1",
      name: "管理员",
    });*/
    filterOption[0].option = options
  }

  drawerMainRef.value.init()
}
const drawerMainClose = () => {
  drawerMainRef.value.close()
}
</script>
<template>
  <div
    :class="!!state.multipleSelection.length && 'report-table-selection'"
    class="report-table de-search-table report-list_table"
  >
    <el-row class="report-table__filter top-operate">
      <el-col :span="12">
        <el-button @click="handleAddTask" type="primary">{{ t('sync_task.add_task') }}</el-button>
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
    <div :class="[state.filterTexts.length ? 'is-in-filter' : 'report-table__content']">
      <GridTable
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.taskList"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        @selection-change="handleSelectionChange"
        :show-empty-img="!tableLoading"
        v-loading="tableLoading"
      >
        <el-table-column type="selection" width="55" :selectable="() => true" />
        <el-table-column
          key="name"
          show-overflow-tooltip
          prop="name"
          :label="t('sync_task.name')"
          min-width="170"
        >
          <template #default="scope">
            <div class="task-span-link" @click="edit(scope.row)">
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          prop="lastExecTime"
          :label="t('sync_task.trigger_last_time')"
          width="185"
          sortable="custom"
        >
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.lastExecTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="lastExecStatus"
          key="lastExecStatus"
          :label="t('sync_task.last_execute_result')"
          show-overflow-tooltip
          min-width="180"
        >
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <el-icon
                size="16px"
                v-if="scope.row.lastExecStatus"
                :class="scope.row.lastExecStatus === 1 ? 'is-loading' : ''"
              >
                <Icon
                  ><component
                    :style="'color:' + getLogStatusIcon(scope.row.lastExecStatus).color"
                    :is="getLogStatusIcon(scope.row.lastExecStatus).icon"
                  ></component
                ></Icon>
              </el-icon>
              <span style="padding: 0 8px 0 8px">{{
                getLastStatusLabel(scope.row.lastExecStatus)
              }}</span>
              <el-icon
                @click="showLogDetail(scope.row)"
                class="error-info"
                v-if="scope.row.lastExecStatus === 3"
              >
                <icon name="icon-maybe_outlined"><iconMaybe_outlined class="svg-icon" /></icon>
              </el-icon>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="status"
          key="status"
          :label="t('sync_task.task_status')"
          show-overflow-tooltip
          min-width="170"
        >
          <template #default="scope">
            <div class="status" :class="`status status-${scope.row.status}`">
              <span>{{ getStatusLabel(scope.row.status) }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          prop="nextExecTime"
          :label="t('sync_task.trigger_next_time')"
          min-width="170"
        >
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.nextExecTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" :label="t('report.creator')" min-width="100" />
        <el-table-column prop="createTime" :label="t('report.create_time')" min-width="170">
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          key="_operation"
          :label="t('sync_task.operation')"
          width="120"
        >
          <template #default="scope">
            <div class="operate-icon-container">
              <el-tooltip effect="dark" :content="t('commons.edit')" placement="top">
                <el-button text @click="edit(scope.row)">
                  <template #icon>
                    <Icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <el-tooltip effect="dark" :content="t('sync_task.running_one')" placement="top">
                <el-button text @click="execute(scope.row)">
                  <template #icon>
                    <Icon name="icon_sync-play-round_outlined"
                      ><icon_syncPlayRound_outlined class="svg-icon"
                    /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
              <div class="icon-more">
                <handle-more
                  class="task-handle-more"
                  @handle-command="cmd => moreHandler(cmd, scope.row)"
                  :menu-list="getMoreList(scope.row)"
                />
              </div>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-if="state.multipleSelection.length" class="bottom-bar flex-align-center">
    <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
      {{ t('sync_task.batch_del') }}
    </el-button>
    <span class="bottom-info">{{
      t('sync_task.selection_info', [state.multipleSelection.length])
    }}</span>
    <el-button text @click="clearSelection">{{ t('sync_task.clear_button') }}</el-button>
  </div>
  <report-form
    ref="reportFormDialogRef"
    :task-id="taskId"
    @task-add-visible-close="taskAddVisibleClose"
    @refresh-list="refreshList"
  />
  <log-details
    ref="jobLogDetailRef"
    @job-log-detail-visible-close="jobLogDetailVisibleClose"
    @refresh-list="search"
  />
  <drawer-main
    :filter-options="filterOption"
    @trigger-filter="searchCondition"
    ref="drawerMainRef"
  ></drawer-main>
</template>
<style lang="less">
.popper-max-width {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }
}
</style>

<style lang="less" scoped>
.report-table {
  height: calc(100% - 88px);
  box-sizing: border-box;
  margin-top: 12px;
  background: white;
  padding: 24px;
  border-radius: 4px;

  .report-table__content {
    height: calc(100vh - 274px);
  }

  .is-in-filter {
    height: calc(100vh - 324px);
  }
}

.report-table-selection {
  height: calc(100% - 126px);
  .report-table__content {
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
.operate-icon-container {
  .icon-more {
    color: var(--ed-color-primary) !important;
    border: 0 solid transparent;
    background-color: transparent;
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
    line-height: 26px;
    letter-spacing: 0;
    text-align: center;
    padding: 2px 0;
    display: inline-flex;
    justify-content: center;
    align-items: center;
    height: 32px;
    white-space: nowrap;
    cursor: pointer;
    box-sizing: border-box;
    outline: 0;
    transition: 0.1s;

    user-select: none;
    vertical-align: middle;

    border-radius: var(--ed-border-radius-base);
    .ed-dropdown {
      color: var(--ed-color-primary) !important;
      &:hover {
        i {
          background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
        }
      }
    }
    .task-handle-more {
      .hover-icon {
        color: inherit !important;
      }
    }
  }

  .ed-button {
    align-self: center;
    margin-left: 0px;
    margin-right: 4px;
  }
}

.error-info {
  cursor: pointer;
}

.status {
  display: inline-block;
  margin-left: 4px;
  height: 24px;
  padding: 0 6px;
  font-size: 14px;
  border-radius: 2px;
  span {
    line-height: 24px;
  }
}
.status-0,
.status-2 {
  background: #e8e9e9 !important;
  span {
    color: #646a73 !important;
  }
}
.status-1 {
  background: #d5e2ff !important;
  span {
    color: #2c5fd9 !important;
  }
}
.status-3 {
  background: #d6f4d3 !important;
  span {
    color: #2ca91f !important;
  }
}

.bottom-bar {
  position: absolute;
  bottom: 0;
  height: 64px;
  width: calc(100% - 327px);
  padding-left: 24px;
  background: var(--neutral-00, #fff);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);

  .bottom-info {
    color: #646a73;
    margin: 0 16px 0 24px;
  }

  .batch-delete-button {
    color: var(--ed-button-text-color);
    border-color: var(--ed-button-border-color);

    &:hover {
      color: var(--ed-button-hover-text-color);
      border-color: var(--ed-button-hover-border-color);
      background-color: var(--ed-button-hover-bg-color);
      outline: none;
    }
  }
}
.task-span-link:hover {
  color: var(--ed-color-primary) !important;
  cursor: pointer;
}
</style>
