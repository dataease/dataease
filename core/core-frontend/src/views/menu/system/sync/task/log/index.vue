<script setup lang="ts">
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_close_filled from "@/assets/svg/icon_close_filled.svg";
import icon_replace_outlined from "@/assets/svg/icon_replace_outlined.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import iconFilter from "@/assets/svg/icon-filter.svg";
import icon_sync_logs_outlined from "@/assets/svg/icon_sync_logs_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import { ElIcon, ElMessage, ElMessageBox } from "element-plus-secondary";
import { Icon } from "@/components/icon-custom";
import { onMounted, onUnmounted, reactive, ref } from "vue";
import { clear, getTaskLogListApi, removeApi } from "@/api/sync/syncTaskLog";
import { some, find } from "lodash-es";
import dayjs from "dayjs";
import LogDetails from "./LogDetails.vue";
import ClearJobLogForm from "./ClearJobLogForm.vue";
import { useI18n } from "@/hooks/web/useI18n";
import { propTypes } from "@/utils/propTypes";
import { convertFilterText, FilterText } from "@/components/filter-text";
import { deepCopy } from "@/utils/utils";

const { t } = useI18n();
const keyword = ref(null);
const imgType = ref();
const emptyDesc = ref("");
const tableLoading = ref(false);
const getEmptyImg = (): string => {
  if (keyword.value) {
    return "tree";
  }
  return "noneWhite";
};

const getEmptyDesc = (): string => {
  if (keyword.value) {
    return t("work_branch.relevant_content_found");
  }

  return "";
};
const drawerMainRef = ref();
const props = defineProps({
  jobId: propTypes.string.def(""),
});
const state = reactive({
  taskLogList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  conditions: [],
  orders: [],
  multipleSelection: [],
});
const timeInterval = reactive({
  timeId: null,
});
const startInterval = () => {
  if (!timeInterval.timeId) {
    timeInterval.timeId = window.setInterval(() => {
      partDataUpdate();
    }, 3000);
  }
};
const stopInterval = () => {
  timeInterval.timeId !== null && window.clearInterval(timeInterval.timeId);
  timeInterval.timeId = null;
};
const buildParam = () => {
  const param = {};
  if (state.conditions?.length) {
    state.conditions.forEach((condition) => {
      if (condition["value"]) {
        param[condition["field"]] = condition["value"];
      }
    });
  }
  if (keyword.value) {
    param["keyword"] = keyword.value;
  }
  if (props.jobId !== "") {
    param['taskId'] = props.jobId;
  }
  return param;
};
const search = () => {
  tableLoading.value = true;
  getTaskLogListApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    buildParam()
  ).then((res) => {
    tableLoading.value = false;
    state.taskLogList = res.data.records;
    if (some(state.taskLogList, ["status", "RUNNING"])) {
      startInterval();
    }
    if (
      state.paginationConfig.currentPage > 1 &&
      state.taskLogList.length === 0
    ) {
      state.paginationConfig.currentPage--;
      search();
    }
    state.paginationConfig.total = res.data.total;
    imgType.value = getEmptyImg();
    emptyDesc.value = getEmptyDesc();
  }).catch(() => {
    tableLoading.value = false;
  });;
};
const partDataUpdate = () => {
  const param = {
    keyword: keyword.value || undefined,
    conditions: deepCopy(state.conditions),
  };
  if (props.jobId !== "") {
    const taskIdOption = {
      field: "taskId",
      value: props.jobId,
      operator: "eq",
    };
    param.conditions.push(taskIdOption);
  }
  getTaskLogListApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    param
  ).then((res) => {
    let isRunning = false;
    const resDataMap = res.data.records.reduce((acc, cur) => {
      acc[cur.id] = cur;
      return acc;
    }, {});
    state.taskLogList.forEach(item => {
      const resItem = resDataMap[item.id];
      if (resItem) {
        item.status = resItem.status;
        if (resItem.status === "RUNNING") {
          isRunning = true;
        }
      }
    });
    if (isRunning) {
      startInterval();
    } else {
      stopInterval();
    }
  });
}
onMounted(() => {
  search();
});
onUnmounted(() => {
  stopInterval();
});
const pageChange = (index: any) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  search();
};
const sizeChange = (size) => {
  state.paginationConfig.pageSize = size;
  search();
};
const timestampFormatDate = (value) => {
  if (!value) {
    return "-";
  }
  return dayjs(new Date(value)).format("YYYY-MM-DD HH:mm");
};

const getLogStatusIcon = (value) => {
  const iconObj = {
    icon: "-",
    color: "",
  };
  if (value === "Success".toUpperCase()) {
    iconObj.icon = icon_succeed_filled;
    iconObj.color = "#34C724";
  }
  if (
      value === "Fail_retry".toUpperCase() ||
      value === "No_process".toUpperCase() ||
    value === "Fail".toUpperCase() ||
    value === "Termination".toUpperCase() ||
    value === "connection_lost".toUpperCase()
  ) {
    iconObj.icon = icon_close_filled;
    iconObj.color = "#F54A45";
  }
  if (value === "Running".toUpperCase()) {
    iconObj.icon = icon_replace_outlined;
    iconObj.color = "#2c5fd9";
  }
  return iconObj;
};
const taskLogStatus = [
  { label: t("sync_task.status_failed"), value: "fail_retry" },
  { label: t("sync_task.status_failed"), value: "no_process" },
  { label: t("sync_task.status_failed"), value: "fail" },
  { label: t("sync_task.status_running"), value: "running" },
  { label: t("sync_task.status_success"), value: "success" },
  { label: t("sync_task.status_connection_lost"), value: "connection_lost" },
  { label: t("sync_task.status_terminated"), value: "termination" },
];
const getStatusLabel = (value) => {
  if (value) {
    const status = find(taskLogStatus, ["value", value.toLowerCase()]);
    if (status) {
      return status.label;
    }
  }
  return "-";
};
const delHandler = (row) => {
  ElMessageBox.confirm(t("sync_task.confirm_delete_msg"), {
    confirmButtonText: t("sync_task.delete"),
    cancelButtonText: t("sync_datasource.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    removeApi(row.id).then(() => {
      ElMessage.success(t("sync_task.op_success"));
      search();
    });
  });
};

const logStatus = ref();
const jobLogDetailRef = ref();
const showLogDetail = (row) => {
  logStatus.value = row.status;
  jobLogDetailRef.value.logId = row.id;
  jobLogDetailRef.value.jobLogDetailVisible = true;
  jobLogDetailRef.value?.startInterval(row.id, row.status);
};

const jobLogDetailVisibleClose = () => {
  jobLogDetailRef.value.jobLogDetailVisible = false;
};

const clearJobLogDialogRef = ref();
const closeClearDialog = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false;
};
const showClearJobLogDialogFormVisible = () => {
  clearJobLogDialogRef.value.clearJobLogDialogFormVisible = true;
};

const clearLogForm = reactive({
  jobId: "",
  clearType: "1",
});
const clearJobLog = (clearTypeLabel: string) => {
  ElMessageBox.confirm(t("sync_task.confirm_clear_msg", [clearTypeLabel]), {
    confirmButtonText: t("sync_task.clear"),
    cancelButtonText: t("sync_datasource.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    clear(clearLogForm).then(() => {
      clearJobLogDialogRef.value.clearJobLogDialogFormVisible = false;
      ElMessage({
        message: t("sync_task.op_success_refresh"),
        type: "success",
      });
      search();
    });
  });
};
const filterOption = [
  {
    type: "enum",
    option: [
      {
        id: "SUCCESS",
        name: t("sync_task.status_success"),
      },
      {
        id: "FAIL",
        name: t("sync_task.status_failed"),
      },
      {
        id: "FAIL_RETRY",
        name: t("sync_task.status_failed"),
      },
      {
        id: "NO_PROCESS",
        name: t("sync_task.status_failed"),
      },
      {
        id: "RUNNING",
        name: t("sync_task.status_running"),
      },
      {
        id: "CONNECTION_LOST",
        name: t("sync_task.status_connection_lost"),
      },
      {
        id: "TERMINATION",
        name: t("sync_task.status_terminated"),
      },
    ],
    field: "status",
    title: t("sync_task.execute_result"),
    operate: "in",
  },
  {
    type: "time",
    option: [],
    property: {
      showType: "datetimerange",
      format: "YYYY-MM-DD HH:mm:ss",
      valueFormat: "YYYY-MM-DD HH:mm:ss",
      rangeSeparator: "-",
      startPlaceholder: t("sync_datasource.start_time"),
      endPlaceholder: t("sync_datasource.end_time"),
    },
    field: "lastExecuteTime",
    title: t("sync_task.execute_time"),
    operate: "between",
  },
];
const searchCondition = (conditions) => {
  state.conditions = conditions;
  search();
  fillFilterText();
  drawerMainClose();
};
const fillFilterText = () => {
  const textArray = state.conditions?.length
    ? convertFilterText(state.conditions, filterOption)
    : [];
  state.filterTexts = [...textArray];
  Object.assign(state.filterTexts, textArray);
};
const clearFilter = (params?: number) => {
  let index = params ? params : 0;
  if (isNaN(index)) {
    state.filterTexts = [];
  } else {
    state.filterTexts.splice(index, 1);
  }
  drawerMainRef.value.clearFilter(index);
};

const drawerMainOpen = async () => {
  drawerMainRef.value.init();
};
const drawerMainClose = () => {
  drawerMainRef.value.close();
};
</script>

<template>
  <div class="source-ds-table de-search-table">
    <div
      :class="!!state.multipleSelection.length && 'source-ds-table-selection'"
      class="source-ds de-search-table"
    >
      <el-row class="ds-table__filter top-operate">
        <el-col :span="12">
          <el-button @click="showClearJobLogDialogFormVisible" type="primary">{{
            t("sync_task.clear_log")
          }}</el-button>
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
            :class="
              state.conditions.length
                ? 'filter-condition-button'
                : 'filter-button'
            "
          >
            <template #icon>
              <Icon name="icon-filter"><iconFilter class="svg-icon" /></Icon>
            </template>
            {{
              t("common.filter") +
              (state.conditions.length ? `(${state.conditions?.length})` : "")
            }}
          </el-button>
        </el-col>
      </el-row>
      <filter-text
        @clear-filter="clearFilter"
        :total="state.paginationConfig.total"
        :filter-texts="state.filterTexts"
      ></filter-text>
      <div
        :class="[
          state.filterTexts.length
            ? 'is-in-filter'
            : 'source-ds-table__content',
        ]"
      >
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
          <el-table-column
            key="id"
            show-overflow-tooltip
            prop="id"
            :label="t('sync_task.log_id')"
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
              <span>{{ scope.row.jobName }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="executorStartTime"
            :label="t('sync_task.execute_time')"
            min-width="170"
          >
            <template #default="scope">
              <span>{{
                timestampFormatDate(scope.row.executorStartTime)
              }}</span>
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
                  <Icon
                    :style="'color:' + getLogStatusIcon(scope.row.status).color"
                    ><component
                      :is="getLogStatusIcon(scope.row.status).icon"
                      :style="
                        'color:' + getLogStatusIcon(scope.row.status).color
                      "
                      class="svg-icon"
                    ></component
                  ></Icon>
                </el-icon>
                <span style="padding: 0 8px 0 8px">{{
                  getStatusLabel(scope.row.status)
                }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            fixed="right"
            key="_operation"
            :label="t('sync_task.op')"
            width="100"
          >
            <template #default="scope">
              <div class="operate-icon-container">
                <el-tooltip
                  effect="dark"
                  :content="t('sync_task.view_execute_log')"
                  placement="top"
                >
                  <template #default>
                    <el-button
                      text
                      @click="showLogDetail(scope.row)"
                      class="detail-button"
                    >
                      <template #icon>
                        <Icon name="icon_sync_logs_outlined"
                          ><icon_sync_logs_outlined class="svg-icon"
                        /></Icon>
                      </template>
                    </el-button>
                  </template>
                </el-tooltip>
                <el-tooltip
                  effect="dark"
                  :content="t('common.delete')"
                  placement="top"
                >
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
  </div>
  <log-details
    ref="jobLogDetailRef"
    :job-log-status="logStatus"
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
  .detail-button {
    margin: 0 4px 0 0;
  }
}
.source-ds-table {
  border-radius: 4px;
}
</style>
