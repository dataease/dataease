<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { clearLog, commitLogPager } from './task_api'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { useI18n } from '@/hooks/web/useI18n'
import dayjs from 'dayjs'
import { useCache } from '@/hooks/web/useCache'
import RowDataForm from './form/RowDataForm.vue'
import { find } from 'lodash-es'
import ClearJobLogForm from '../../../system/sync/task/log/ClearJobLogForm.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'

const props = withDefaults(
  defineProps<{
    formId?: string
    hasManagePermission?: boolean
  }>(),
  {
    hasManagePermission: false
  }
)

const { t } = useI18n()
const { wsCache } = useCache()
const taskId = ref<string>('')
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

const activeCommand = ref(-1)

const search = () => {
  const param = {
    formId: props.formId
  }
  if (activeCommand.value > -1) {
    param.operate = activeCommand.value
  }
  commitLogPager(param, state.paginationConfig.currentPage, state.paginationConfig.pageSize).then(
    res => {
      state.taskList = res.data.records
      if (state.paginationConfig.currentPage > 1 && state.taskList.length === 0) {
        state.paginationConfig.currentPage--
        search()
      }
      state.paginationConfig.total = res.data.total
    }
  )
}

const curTypeList = [
  { key: -1, name: t('commons.all') },
  { key: 1, name: t('data_fill.data.insert_data') },
  { key: 2, name: t('data_fill.data.update_data') },
  { key: 0, name: t('data_fill.data.delete_data') },
  { key: 3, name: t('data_fill.data.batch_insert_data') }
]

function getType(type, count) {
  if (type === 3) {
    if (count) {
      return t('data_fill.data.batch_insert_data_with_count', [count])
    }
  }
  const obj = find(curTypeList, c => c.key === type)
  if (obj) {
    return obj.name
  }
  return '-'
}

onMounted(async () => {
  search()
})

const rowDataFormRef = ref()

function showForm(data) {
  rowDataFormRef.value?.init(data.formId, true, false, [{ rowDataId: data.dataId }])
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

const clearJobLogDialogRef = ref()
const closeClearDialog = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
}
const showClearJobLogDialogFormVisible = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = true
}

const clearLogForm = reactive({
  formId: props.formId,
  clearType: '1'
})

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
    clearLog(clearLogForm).then(() => {
      clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false
      ElMessage({
        message: t('sync_task.op_success_refresh'),
        type: 'success'
      })
      search()
    })
  })
}
</script>
<template>
  <div class="df-base-container">
    <div
      class="df-table-container report-table"
      :class="{
        'df-table-container-no-bottom': state.multipleSelection.length
      }"
    >
      <div class="search-operate">
        <el-button
          @click="showClearJobLogDialogFormVisible"
          type="primary"
          v-if="hasManagePermission"
        >
          {{ t('sync_task.clear_log') }}
        </el-button>
        <div style="width: 185px">
          <el-select
            popper-class="menu-panel-select_popper_fill"
            class="select-type-list"
            v-model="activeCommand"
            @change="search"
          >
            <template #prefix>
              <span style="font-size: 14px; color: #646a73; font-weight: normal">
                {{ t('data_fill.task.commit_operate_type') }}:
              </span>
            </template>
            <el-option
              v-for="item in curTypeList"
              :key="item.key"
              :label="item.name"
              :value="item.key"
            />
          </el-select>
        </div>
      </div>
      <div
        :class="[state.filterTexts.length ? 'is-in-filter' : 'report-table__content']"
        class="info-table"
      >
        <GridTable
          ref="multipleTableRef"
          :pagination="state.paginationConfig"
          :table-data="state.taskList"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="popper-max-width"
          @current-change="pageChange"
          @size-change="sizeChange"
          :show-empty-img="!tableLoading"
          v-loading="tableLoading"
        >
          <el-table-column
            key="name"
            show-overflow-tooltip
            prop="operate"
            :label="t('data_fill.task.commit_operate_type')"
            min-width="170"
          >
            <template #default="scope">
              <span>{{ getType(scope.row.operate, scope.row.count) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            key="name"
            prop="committer"
            :label="t('data_fill.task.committer')"
            min-width="170"
          >
            <template #default="scope">
              <span>{{ scope.row.committer }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="commitTime"
            :label="t('data_fill.data.commit_time')"
            min-width="170"
          >
            <template #default="scope">
              <span>{{ timestampFormatDate(scope.row.commitTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
            fixed="right"
            key="_operation"
            :label="t('sync_task.operation')"
            width="120"
          >
            <template #default="scope">
              <el-button text @click="showForm(scope.row)" v-if="scope.row.operate !== 3">
                {{ t('data_fill.task.show_data') }}
              </el-button>
            </template>
          </el-table-column>
        </GridTable>
      </div>

      <RowDataForm ref="rowDataFormRef" />
    </div>

    <clear-job-log-form
      :model-value="clearLogForm"
      ref="clearJobLogDialogRef"
      @clear-job-log="clearJobLog"
      @close-clear-dialog="closeClearDialog"
    />
  </div>
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
.df-base-container {
  display: flex;
  height: 100%;
  width: 100%;
  flex-direction: column;
}

.df-table-container {
  display: flex;
  height: 100%;
  width: 100%;
  flex-direction: column;

  .select-type-list {
    :deep(.ed-input__wrapper) {
      padding: 0 11px;
    }

    :deep(.ed-select__prefix::after) {
      display: none;
    }
  }
}

.df-table-container-no-bottom {
  padding: 24px 24px 0;
}

.report-table {
  padding: 24px;
  background: #fff;
  flex: 1;

  .search-operate {
    display: flex;
    align-items: center;
    justify-content: space-between;
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
  height: 64px;
  width: 100%;
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
  border-bottom: 1px solid var(--ed-color-primary) !important;
}
</style>
