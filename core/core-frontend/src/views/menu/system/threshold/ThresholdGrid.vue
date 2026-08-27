<script lang="ts" setup>
import icon_dashboard_outlined from '@/assets/svg/icon_dashboard_outlined.svg'
import icon_operationAnalysis_outlined from '@/assets/svg/icon_operation-analysis_outlined.svg'
import icon_searchOutline_outlined from '@/assets/svg/icon_search-outline_outlined.svg'
import iconFilter from '@/assets/svg/icon-filter.svg'
import threshold_full from '@/assets/svg/threshold_full.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import clock from '@/assets/svg/clock.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import { onMounted, reactive, ref, defineEmits, nextTick } from 'vue'
import { thresholdGridApi, thresholdDelApi, thresholdSwitchApi } from './api'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { ElIcon, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { map } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
import { Icon } from '@/components/icon-custom'
import dayjs from 'dayjs'
import DrawerMain from '@/components/drawer-main/src/DrawerMain.vue'
import { convertFilterText, FilterText } from '@/components/filter-text'
import { filterOption } from './options'
// import BatchReciDialog from "../../../component/threshold-warning/form/BatchReciDialog.vue";
import ThresholdDrawer from '../../../component/threshold-warning/ThresholdDrawer.vue'

interface FieldSort {
  field: string
  type: boolean
}
const { t } = useI18n()
const drawerVisible = ref(false)
const drawerMainRef = ref()
const keyword = ref(null)
const multipleTableRef = ref()
const tableLoading = ref<boolean>(true)
const thresholdDrawer = ref()
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
// const reciDialog = ref();

const statusFilterList = [
  { value: -1, text: t('threshold_warn.all') },
  { value: 0, text: t('threshold_warn.normal') },
  { value: 1, text: t('threshold_warn.abnormal') }
]
const selectedStatusFilter = ref<number>(-1)
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

const drawerReset = () => {
  drawerVisible.value = false
}
const emits = defineEmits(['openTaskLog'])
const openTaskLogList = row => {
  emits('openTaskLog', { taskId: row.id, taskName: row.name })
}

const timestampFormatDate = value => {
  if (!value) {
    return '-'
  }
  return dayjs(new Date(value)).format('YYYY-MM-DD HH:mm:ss')
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
  if (selectedStatusFilter.value === -1) {
    delete param['statusList']
  } else {
    param['statusList'] = [selectedStatusFilter.value]
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
  thresholdGridApi(state.paginationConfig.currentPage, state.paginationConfig.pageSize, param)
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

onMounted(async () => {
  search()
})

const batchDelHandler = () => {
  ElMessageBox.confirm(t('threshold_warn.batch_del_confirm', [state.multipleSelection.length]), {
    confirmButtonText: t('common.delete'),
    cancelButtonText: t('commons.cancel'),
    showCancelButton: true,
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    tableLoading.value = true
    thresholdDelApi(map(state.multipleSelection, 'id')).then(() => {
      tableLoading.value = false
      ElMessage({
        message: t('common.delete_success'),
        type: 'success'
      })
      search()
    })
  })
}
/*const batcReciHandler = () => {
  reciDialog.value.init(state.multipleSelection.map((item) => item.id));
};*/

const handleSelectionChange = (rows: any) => {
  state.multipleSelection = rows
}
const clearSelection = () => {
  multipleTableRef.value?.clearSelection()
}
const allSelection = () => {
  if (
    Math.min(state.paginationConfig.pageSize, state.paginationConfig.total) ==
    state.multipleSelection.length
  ) {
    return
  }
  multipleTableRef.value?.toggleAllSelection()
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
    thresholdDelApi([row.id]).then(() => {
      tableLoading.value = false
      ElMessage.success(t('commons.delete_success'))
      search()
    })
  })
}

const enableChange = row => {
  const param = { id: row.id, enable: row.enable }
  thresholdSwitchApi(param).then(() => {
    ElMessage.success(t('user.switch_success'))
  })
}
const editHandler = row => {
  drawerVisible.value = true
  nextTick(() => {
    thresholdDrawer.value?.open({ id: row.id })
  })
}
const editChartHandler = row => {
  const baseUrl = row.resourceType === 'dataV' ? '#/dvCanvas?dvId=' : '#/dashboard?resourceId='
  const thresholdToken = `${row.id}-fit2cloud-de-v2-${row.chartId}`
  window.open(baseUrl + row.resourceId + `&thresholdToken=${thresholdToken}`, '_blank')
}
const statusFilterChange = (value: number) => {
  if (selectedStatusFilter.value === value) {
    return
  }
  selectedStatusFilter.value = value
  search()
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
    :class="!!state.multipleSelection.length && 'report-table-selection'"
    class="report-table de-search-table threshold-table"
  >
    <el-row class="report-table__filter top-operate">
      <el-col :span="12">
        <div class="top-operate-left">
          <div
            v-for="item in statusFilterList"
            :key="item.value"
            @click="statusFilterChange(item.value)"
            :class="{ 'is-active': selectedStatusFilter === item.value }"
          >
            {{ item.text }}
          </div>
        </div>
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('threshold_warn.search_placeholder')"
          @change="search"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"><icon_searchOutline_outlined /></Icon>
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
        current-row-key="id"
        class="popper-max-width"
        @current-change="pageChange"
        @size-change="sizeChange"
        @sort-change="sortChange"
        @selection-change="handleSelectionChange"
        :show-empty-img="!tableLoading"
        v-loading="tableLoading"
      >
        <el-table-column type="selection" width="38" :selectable="() => true" />
        <el-table-column
          key="name"
          show-overflow-tooltip
          prop="name"
          :label="t('threshold.table_name')"
          min-width="220"
        >
          <template #default="scope">
            <div class="task-span-link" @click="editChartHandler(scope.row)">
              <span>{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          key="chartName"
          show-overflow-tooltip
          prop="chartName"
          :label="t('threshold_warn.chart_name')"
          width="160"
        />
        <el-table-column
          key="resourceName"
          show-overflow-tooltip
          prop="resourceName"
          :label="t('auth.resource_name')"
          width="220"
        >
          <template #default="scope">
            <div class="threshold-resource-name">
              <el-icon :class="`main-color color-${scope.row.resourceType}`">
                <Icon
                  ><component
                    :is="
                      scope.row.resourceType === 'dataV'
                        ? icon_operationAnalysis_outlined
                        : icon_dashboard_outlined
                    "
                  ></component
                ></Icon>
              </el-icon>
              <span>{{ scope.row.resourceName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column
          key="resourceType"
          show-overflow-tooltip
          prop="resourceType"
          :label="t('org.resource_type')"
          width="125"
        >
          <template #default="scope">
            <span>{{
              scope.row.resourceType === 'dataV'
                ? t('work_branch.big_data_screen')
                : t('work_branch.dashboard')
            }}</span>
          </template>
        </el-table-column>
        <el-table-column key="status" prop="status" :label="t('threshold.status')" width="105">
          <template #default="scope">
            <span v-if="scope.row.status">
              <el-icon>
                <Icon name="threshold_full"><threshold_full class="svg-icon" /></Icon>
              </el-icon>
            </span>
            <span v-else />
          </template>
        </el-table-column>
        <el-table-column
          key="enable"
          prop="enable"
          :label="t('threshold_warn.warn_status')"
          width="105"
        >
          <template #default="scope">
            <div class="threshold_enable" :class="`threshold_enable_${!!scope.row.enable}`">
              <span>{{ scope.row.enable ? t('chart.open') : t('commons.close') }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column
          show-overflow-tooltip
          prop="createName"
          :label="t('report.creator')"
          width="120"
        />
        <el-table-column prop="createTime" :label="t('report.create_time')" width="175">
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          fixed="right"
          key="_operation"
          :label="t('sync_task.operation')"
          width="152"
        >
          <template #default="scope">
            <div class="threshold-table-operate">
              <el-switch
                v-model="scope.row.enable"
                @change="enableChange(scope.row)"
                size="small"
              />
              <el-divider style="margin: 0 12px" direction="vertical" />
              <el-tooltip effect="dark" :content="t('common.edit')" placement="top">
                <el-button text @click="editHandler(scope.row)" size="small">
                  <template #icon>
                    <Icon name="icon_edit_outlined"><icon_edit_outlined /></Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <el-tooltip effect="dark" :content="t('threshold.record')" placement="top">
                <el-button text @click="openTaskLogList(scope.row)" size="small">
                  <template #icon>
                    <Icon name="clock"><clock class="svg-icon" /></Icon>
                  </template>
                </el-button>
              </el-tooltip>

              <el-tooltip effect="dark" :content="t('common.delete')" placement="top">
                <el-button text @click="delHandler(scope.row)" size="small">
                  <template #icon>
                    <Icon name="icon_delete-trash_outlined"><icon_deleteTrash_outlined /></Icon>
                  </template>
                </el-button>
              </el-tooltip>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
  <div v-if="state.multipleSelection.length" class="bottom-bar flex-align-center">
    <!--
    <el-button
      type="primary"
      class="batch-delete-button"
      plain
      @click="batcReciHandler"
    >
      {{ t("threshold.recipient_setting") }}
    </el-button>
    -->
    <el-button type="danger" class="batch-delete-button" plain @click="batchDelHandler">
      {{ t('sync_task.batch_del') }}
    </el-button>
    <span class="bottom-info">{{
      t('sync_task.selection_info', [state.multipleSelection.length])
    }}</span>
    <el-button text @click="allSelection">{{
      `${t('dataset.check_all')} ${Math.min(
        state.paginationConfig.pageSize,
        state.paginationConfig.total
      )} ${t('deDataset.item')}`
    }}</el-button>
    <el-button text @click="clearSelection">{{ t('sync_task.clear_button') }}</el-button>
  </div>
  <drawer-main
    :filter-options="filterOption"
    @trigger-filter="searchCondition"
    ref="drawerMainRef"
  ></drawer-main>
  <!-- <batch-reci-dialog ref="reciDialog" /> -->
  <threshold-drawer
    v-if="drawerVisible"
    ref="thresholdDrawer"
    @reset="drawerReset"
    @refresh-list="search"
  />
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
.threshold-resource-name {
  display: flex;
  align-items: center;
  .main-color {
    color: #fff;
    border-radius: 4px;
    font-size: 18px;
    padding: 3px;
    margin-right: 12px;
  }
  span {
    max-width: 160px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.threshold_enable {
  width: 40px;
  height: 24px;
  line-height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 2px;
}
.threshold_enable_true {
  background-color: var(--ed-color-primary-33, #3370ff33);
  color: var(--ed-color-primary, #3370ff);
  line-height: 22px;
}
.threshold_enable_false {
  background-color: #1f23291a;
  color: #646a73;
  line-height: 22px;
}
.threshold-table-operate {
  button {
    width: 16px;
    height: 16px;
    line-height: 16px;
    i {
      font-size: 14px;
    }
  }
  .ed-divider {
    border-color: #1f232926;
  }
}
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
  :deep(.top-operate-left) {
    display: flex;
    width: fit-content;
    height: 32px;
    line-height: 32px;
    align-items: center;
    column-gap: 4px;
    border: 1px solid #bbbfc4;
    border-radius: 4px;
    padding: 0 4px;
    div {
      padding: 0 8px;
      height: 24px;
      font-size: 14px;
      line-height: 24px;
      display: flex;
      align-items: center;
      justify-content: center;
      cursor: pointer;
      border-radius: 4px;
      &:hover {
        color: var(--ed-color-primary, #3370ff);
      }
    }
    .is-active {
      background-color: var(--ed-color-primary-1a, #3370ff1a);
      color: var(--ed-color-primary, #3370ff);
    }
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
