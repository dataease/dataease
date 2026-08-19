<script lang="ts" setup>
import icon_info_outlined from '@/assets/svg/icon_info_outlined.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import {reactive, ref} from 'vue'
import {
  deleteDfSubTask, getTaskUserList,
  subTaskPager
} from './task_api'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import {ElMessage, ElMessageBox} from 'element-plus-secondary'
import {find} from 'lodash-es'
import {useI18n} from '@/hooks/web/useI18n'
import {Icon} from '@/components/icon-custom'
import dayjs from 'dayjs'
import {useCache} from '@/hooks/web/useCache'

interface FieldSort {
  field: string,
  type: boolean
}

const {t} = useI18n()
const {wsCache} = useCache()
const keyword = ref(null)
const multipleTableRef = ref()
const tableLoading = ref<boolean>(false)
const imgType = ref()
const emptyDesc = ref('')
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


const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return dayjs(new Date(value)).format('YYYY-MM-DD HH:mm:ss')
}


const taskStatus = [
  {label: t('data_fill.task.expired'), value: 0},
  {label: t('data_fill.task.running'), value: 1}
]

const getStatusLabel = row => {
  const execStatus = row?.execStatus
  if (execStatus != undefined && execStatus === 3) {
    return t('report.last_status_fail')
  }
  const value = row?.status
  if (value !== null && value !== undefined) {
    const status = find(taskStatus, ['value', value])
    if (status) {
      return status.label
    }
  }
  return '-'
}


const search = () => {
  subTaskPager(
      {taskId: taskId.value},
      state.paginationConfig.currentPage,
      state.paginationConfig.pageSize
  ).then(res => {
    state.taskList = res.data.records
    if (state.paginationConfig.currentPage > 1 && state.taskList.length === 0) {
      state.paginationConfig.currentPage--
      search()
    }
    state.paginationConfig.total = res.data.total
  })
}


const pageChange = (index: any) => {
  if (typeof index !== 'number') {
    return
  }
  state.paginationConfig.currentPage = index
  search()
}
const sizeChange = size => {
  state.paginationConfig.pageSize = size
  state.paginationConfig.currentPage = 1
  search()
}
const sortChange = (param) => {
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


const deleteSubTask = (row) => {
  ElMessageBox.confirm(t('commons.confirm') + t('commons.delete'), {
    confirmButtonText: t('commons.delete'),
    cancelButtonText: t('commons.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    tableLoading.value = true
    deleteDfSubTask(formId.value, [row.id]).then(res => {
      if (res.code === 0) {
        ElMessage.success(t('commons.delete_success'))
        search()
      }
    }).finally(() => {
      tableLoading.value = false
    })
  })
}

function getPercent(value) {
  if (value === undefined) {
    return '-'
  }
  return new Intl.NumberFormat('en-IN', {style: 'percent', maximumFractionDigits: 2, minimumFractionDigits: 2})
      .format(value)
}

const show = ref(false)
const taskId = ref<string | undefined>()
const formId = ref<string | undefined>()

const open = (_taskId: string, _formId: string) => {
  taskId.value = _taskId
  formId.value = _formId
  state.taskList = []
  state.paginationConfig = {
    currentPage: 1,
    pageSize: 10,
    total: 0
  }
  show.value = true
  search()
}

const users = ref<Array<any>>([])
const userLoading = ref(false)

function showUsers(id, _type) {
  userLoading.value = true
  tableLoading.value = true
  users.value = []
  getTaskUserList(id, _type).then(res => {
    if (res.data.length > 0) {
      users.value = res.data
      switch (_type) {
        case 'finished':
          break
        case 'unfinished':
          break
        default:
          break
      }

    }
  }).finally(() => {
    userLoading.value = false
    tableLoading.value = false
  })

}


defineExpose({open})

</script>
<template>
  <el-drawer v-model="show" direction="rtl" close-on-click-modal destroy-on-close size="950"
             :title="t('data_fill.task.assigned_task')">
    <GridTable
        v-if="show"
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.taskList"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="df-sub-task-table"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        :show-empty-img="!tableLoading"
        v-loading="tableLoading"
    >

      <el-table-column prop="status" key="status" :label="t('data_fill.task.df_task_status')" show-overflow-tooltip
                       min-width="110">
        <template #default="scope">
          <div class="status"
               :class="scope.row.execStatus != undefined && scope.row.execStatus === 3 ? 'status-fail': `status-${scope.row.status}`">
            <span>{{ getStatusLabel(scope.row) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" :label="t('data_fill.task.task_distribute_time')" min-width="180">
        <template #default="scope">
          <span>{{ timestampFormatDate(scope.row.startTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="endTime" :label="t('data_fill.task.task_end_time')" min-width="180">
        <template #default="scope">
          <span>{{ timestampFormatDate(scope.row.endTime) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="totalUserCount" :label="t('data_fill.task.assign_num')" min-width="84">
        <template #default="scope">
          <el-popover :width="200" trigger="click" placement="bottom-start">
            <template #reference>
              <span class="task-span-link" @click="showUsers(scope.row.id, 'total')">{{
                  scope.row.totalUserCount ?? 0
                }}</span>
            </template>
            <el-main v-loading="userLoading" class="user-list-div">
              <span v-for="user in users" :key="user.id">
                {{ user.name }}({{ user.account }})
              </span>
            </el-main>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="totalUserCount" :label="t('data_fill.task.finished_user_num')" min-width="94">
        <template #default="scope">
          <el-popover :width="200" trigger="click" placement="bottom-start">
            <template #reference>
              <span class="task-span-link" @click="showUsers(scope.row.id, 'finished')">{{
                  scope.row.totalUserCount ? (scope.row.totalUserCount - scope.row.unfinishedUserCount) : 0
                }}</span>
            </template>
            <el-main v-loading="userLoading" class="user-list-div">
              <span v-for="user in users" :key="user.id">
                {{ user.name }}({{ user.account }})
              </span>
            </el-main>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="totalUserCount" :label="t('data_fill.task.unfinished_user_num')" min-width="94">
        <template #default="scope">
          <el-popover :width="200" trigger="click" placement="bottom-start">
            <template #reference>
              <span class="task-span-link"
                    @click="showUsers(scope.row.id, 'unfinished')">{{ scope.row.unfinishedUserCount ?? 0 }}</span>
            </template>
            <el-main v-loading="userLoading" class="user-list-div">
              <span v-for="user in users" :key="user.id">
                {{ user.name }}({{ user.account }})
              </span>
            </el-main>
          </el-popover>
        </template>
      </el-table-column>
      <el-table-column prop="finishedRate" :label="t('data_fill.task.finished_rate')" min-width="100">
        <template #header>
          {{ t('data_fill.task.finished_rate') }}
          <el-tooltip placement="bottom">
            <template #content>
              <div>
                {{ t('data_fill.task.finish_rate_hint') }}
              </div>
            </template>
            <el-icon style="cursor: pointer">
              <Icon name="icon_info_outlined">
                <icon_info_outlined/>
              </Icon>
            </el-icon>
          </el-tooltip>
        </template>
        <template #default="scope">
          <span>{{ getPercent(scope.row.finishedRate) }}</span>
        </template>
      </el-table-column>

      <el-table-column fixed="right" key="_operation" :label="t('sync_task.operation')" width="60">
        <template #default="scope">
          <div class="operate-icon-container">
            <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
              <el-button text @click="deleteSubTask(scope.row)">
                <template #icon>
                  <Icon name="icon_delete-trash_outlined">
                    <icon_deleteTrash_outlined/>
                  </Icon>
                </template>
              </el-button>
            </el-tooltip>

          </div>
        </template>
      </el-table-column>
    </GridTable>
  </el-drawer>

</template>
<style lang="less">

.df-sub-task-table {
  .ed-popper.is-dark {
    white-space: pre-wrap;
    max-width: 300px;
  }

  .ed-table .cell {
    font-family: var(--de-custom_font, 'PingFang');
  }

}

</style>

<style lang="less" scoped>
.datasource-table-container {
  padding: 24px;
  height: calc(100vh - 148px);
  display: flex;
  flex-direction: column;
}

.datasource-table-container-no-bottom {
  padding: 24px 24px 0;
}

.report-table {
  padding: 24px;
  background: #fff;
  flex: 1;

  .search-operate {
    margin-bottom: 16px;
  }
}

.info-table {
  height: calc(100% - 49px);
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
  display: flex;

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

.status-0, .status-2 {
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
    color: #2CA91F !important;
  }
}

.status-fail {
  background: #F54A45 !important;

  span {
    color: #FFF !important;
  }
}

.bottom-bar {
  height: 64px;
  width: 100%;
  padding-left: 24px;
  background: var(--neutral-00, #FFF);
  box-shadow: 0px -2px 4px 0px rgba(31, 35, 41, 0.08);

  .bottom-info {
    color: #646A73;
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

.task-span-link {
  color: var(--ed-color-primary) !important;
  cursor: pointer;
  border-bottom: 1px solid var(--ed-color-primary) !important;;
}

.user-list-div {
  display: flex;
  flex-direction: column;
  padding: 0;
  max-height: 480px;
}

</style>
