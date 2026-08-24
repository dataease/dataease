<script lang="ts" setup>
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_close_filled from "@/assets/svg/icon_close_filled.svg";
import icon_sync_progress from "@/assets/svg/icon_sync_progress.svg";
import iconMaybe_outlined from "@/assets/svg/icon-maybe_outlined.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import iconFilter from "@/assets/svg/icon-filter.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_syncPlayRound_outlined from "@/assets/svg/icon_sync-play-round_outlined.svg";
import icon_syncPlayRound_filled from "@/assets/svg/icon_sync-play-round_filled.svg";
import {onMounted, onUnmounted, reactive, ref, defineEmits} from "vue";
import {
  batchRemoveApi,
  executeOneApi,
  getTaskInfoListApi,
  ITaskInfoRes,
  removeApi,
  startTaskApi,
  stopTaskApi,
} from "@/api/sync/syncTask";
import {terminationTaskApi} from "@/api/sync/syncTaskLog";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {ElIcon, ElMessage, ElMessageBox} from "element-plus-secondary";
import TaskForm from "./TaskForm.vue";
import {find, some, map} from "lodash-es";
import {useI18n} from "@/hooks/web/useI18n";
import {HandleMore} from "@/components/handle-more";
import {Icon} from "@/components/icon-custom";
import dayjs from "dayjs";
import LogDetails from "../log/LogDetails.vue";
import DrawerMain from "@/components/drawer-main/src/DrawerMain.vue";
import {convertFilterText, FilterText} from "@/components/filter-text";
import useClipboard from "vue-clipboard3";
import icon_copy_outlined from "@/assets/svg/icon_copy_outlined.svg";

const {toClipboard} = useClipboard();

const {t} = useI18n();
const taskFormDialogRef = ref();
const drawerMainRef = ref();
const taskId = ref<string>("");
const activeName = ref("ds");
const keyword = ref(null);
const multipleTableRef = ref();
const tableLoading = ref<boolean>(false);
const imgType = ref();
const emptyDesc = ref("");
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
const state = reactive({
  taskList: [],
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

state.filterTexts = [];
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
    field: "logStatus",
    title: t("sync_task.recent_execute_status"),
    operate: "in",
  },
  {
    type: "enum",
    option: [
      {
        id: "waiting",
        name: t("sync_task.status_waiting"),
      },
      {
        id: "running",
        name: t("sync_task.status_running"),
      },
      {
        id: "suspend",
        name: t("sync_task.status_stopped"),
      },
      {
        id: "done",
        name: t("sync_task.status_done"),
      },
    ],
    field: "status",
    title: t("sync_task.task_status"),
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
    title: t("sync_task.trigger_last_time"),
    operate: "between",
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
    field: "nextExecuteTime",
    title: t("sync_task.trigger_next_time"),
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

const getMoreList = (row) => {
  const moreList = [
    {
      label: t("sync_task.terminated"),
      divided: false,
      command: "terminationTask",
      disabled: row.logStatus !== "RUNNING" || !row.logId,
    },
    {
      label: t("sync_task.stop"),
      divided: false,
      command: "stop",
      disabled:
        row.status === "running" ||
        row.status === "suspend" ||
        row.status === "done" ||
        row.schedulerType === "NONE"
    },
    {
      label: t("sync_task.start"),
      divided: false,
      command: "start",
      disabled: row.status != "suspend" || row.schedulerType === "NONE"
    },
    {
      label: t("sync_task.show_log"),
      divided: false,
      command: "showLog",
    },
    {
      label: t("sync_task.show_task_id"),
      divided: false,
      command: "showTaskId"
    },
    {
      label: t("sync_task.delete"),
      divided: false,
      command: "delete",
      disabled: row.status === "running",
    },
  ];
  return moreList;
};
const emits = defineEmits(["openTaskLog"]);
const openTaskLogList = (row) => {
  emits("openTaskLog", row.id);
};
const taskIdDialogVisible = ref(false);
const dialogTaskId = ref("");
const showTaskId = (row) => {
  taskIdDialogVisible.value = true;
  dialogTaskId.value = row.id;
};
const moreHandler = (cmd: string, row) => {
  if (cmd === "terminationTask") {
    terminationTask(row);
    return;
  }
  if (cmd === "stop" || cmd === "start") {
    changeStatus(row);
    return;
  }
  if (cmd === "showLog") {
    openTaskLogList(row);
    return;
  }
  if (cmd === "showTaskId") {
    showTaskId(row);
    return;
  }
  if (cmd === "delete") {
    delHandler(row);
    return;
  }
};

const timestampFormatDate = (value) => {
  if (!value) {
    return "-";
  }
  return dayjs(new Date(value)).format("YYYY-MM-DD HH:mm");
};

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
/**
 * 任务状态
 */
const taskStatus = [
  {label: t("sync_task.status_done"), value: "unexecuted"},
  {label: t("sync_task.status_done"), value: "done"},
  {label: t("sync_task.status_waiting"), value: "waiting"},
  {label: t("sync_task.status_stopped"), value: "suspend"},
  {label: t("sync_task.status_failed"), value: "fail"},
  {label: t("sync_task.status_failed"), value: "fail_retry"},
  {label: t("sync_task.status_failed"), value: "no_process"},
  {label: t("sync_task.status_connection_lost"), value: "connection_lost"},
  {label: t("sync_task.status_running"), value: "running"},
  {label: t("sync_task.status_success"), value: "success"},
  {label: t("sync_task.status_terminated"), value: "termination"},
];
const getLogStatusIcon = (value) => {
  const iconObj = {
    icon: "-",
    color: "",
  };
  if (value === "Success".toUpperCase()
      || value === "unexecuted".toUpperCase()) {
    iconObj.icon = icon_succeed_filled;
    iconObj.color = "#34C724";
  }
  if (
      value === "Fail_retry".toUpperCase() ||
      value === "No_process".toUpperCase() ||
      value === "Fail".toUpperCase() ||
      value === "Connection_lost".toUpperCase() ||
      value === "termination".toUpperCase()
  ) {
    iconObj.icon = icon_close_filled;
    iconObj.color = "#F54A45";
  }
  if (value === "Running".toUpperCase()) {
    iconObj.icon = icon_sync_progress;
    iconObj.color = "#2c5fd9";
  }

  return iconObj;
};
const getStatusLabel = (value) => {
  if (value !== null && value !== undefined) {
    const status = find(taskStatus, ["value", value.toLowerCase()]);
    if (status) {
      return status.label;
    }
  }
  return "-";
};

const statusClass = (value) => {
  if (value !== null && value !== undefined) {
    const status = find(taskStatus, ["value", value.toLowerCase()]);
    if (status) {
      return status.value;
    }
  }
  return "-";
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
  if (state.orders?.length) {
    state.orders.forEach((item) => {
      param[item["field"]] = item.type;
    });
  }
  return param;
};
const search = () => {
  tableLoading.value = true;
  getTaskInfoListApi(
      state.paginationConfig.currentPage,
      state.paginationConfig.pageSize,
      buildParam()
  )
      .then((res) => {
        tableLoading.value = false;
        state.taskList = res.data.records;
        if (some(state.taskList, ["status", "running"])) {
          startInterval();
        }
        if (
            state.paginationConfig.currentPage > 1 &&
            state.taskList.length === 0
        ) {
          state.paginationConfig.currentPage--;
          search();
        }
        state.paginationConfig.total = res.data.total;
        imgType.value = getEmptyImg();
        emptyDesc.value = getEmptyDesc();
      })
      .catch(() => {
        tableLoading.value = false;
      });
};

onMounted(() => {
  search();
});
onUnmounted(() => {
  stopInterval();
});

const batchDelHandler = () => {
  ElMessageBox.confirm(t("sync_task.confirm_batch_delete"), {
    confirmButtonText: t("sync_task.delete"),
    cancelButtonText: t("sync_datasource.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    tableLoading.value = true;
    batchRemoveApi(map(state.multipleSelection, "id")).then(() => {
      tableLoading.value = false;
      ElMessage({
        message: t("sync_task.batch_del") + t("sync_task.status_success"),
        type: "success",
      });
      search();
    });
  });
};

const handleSelectionChange = (rows: any) => {
  state.multipleSelection = rows;
};
const clearSelection = () => {
  multipleTableRef.value?.clearSelection();
};

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
const sortChange = (param: any) => {
  state.orders = [];
  if (param.order && param.prop === "createTime") {
    const type = param.order.substring(0, param.order.indexOf("ending"));
    state.orders.push("create_time " + type);
    search();
  }
};

const rowCheckStatus = (row: any) => {
  return row.id !== taskId.value && row.id !== "1";
};
const handleAddTask = () => {
  taskFormDialogRef.value.taskFormVisible = true;
};

const taskAddVisibleClose = () => {
  taskId.value = "";
  taskFormDialogRef.value.taskFormVisible = false;
};

const refreshList = () => {
  search();
};

const edit = (row: ITaskInfoRes) => {
  taskFormDialogRef.value.taskFormVisible = true;
  taskId.value = row.id;
};

const changeStatus = (row: ITaskInfoRes) => {
  ElMessageBox.confirm(
      (row.status === "suspend" ? t("sync_task.start") : t("sync_task.stop")) +
      t("sync_task.task_text"),
      {
        confirmButtonText: "",
        cancelButtonText: t("sync_datasource.cancel"),
        showCancelButton: true,
        confirmButtonType: "primary",
        type: "warning",
        autofocus: false,
        showClose: false,
      }
  ).then(() => {
    if (row.status === "suspend") {
      startTaskApi(row.id).then(() => {
        ElMessage.success(t("sync_task.start") + t("sync_task.status_success"));
        search();
      });
    } else {
      stopTaskApi(row.id).then(() => {
        ElMessage.success(t("sync_task.stop") + t("sync_task.status_success"));
        search();
      });
    }
  });
};

const execute = (row: ITaskInfoRes) => {
  ElMessageBox.confirm(
      t("sync_task.confirm") + " " + t("sync_task.running_one"),
      {
        confirmButtonText: t("sync_task.confirm"),
        cancelButtonText: t("sync_datasource.cancel"),
        showCancelButton: true,
        confirmButtonType: "primary",
        type: "warning",
        autofocus: false,
        showClose: false,
      }
  ).then(() => {
    executeOneApi(row.id).then(() => {
      ElMessage.success(t("sync_task.op_success"));
      state.taskList.forEach((item) => {
        item["currentTime"] = new Date().getTime();
      });
      partDataUpdate();
    });
  });
};

const terminationTask = (row) => {
  ElMessageBox.confirm(t("sync_task.confirm") + t("sync_task.terminated"), {
    confirmButtonText: t("sync_task.confirm"),
    cancelButtonText: t("sync_datasource.cancel"),
    showCancelButton: true,
    confirmButtonType: "primary",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    terminationTaskApi(row.logId).then(() => {
      ElMessage.success(t("sync_task.terminated") + t("commons.success"));
      search();
    });
  });
};

const delHandler = (row: ITaskInfoRes) => {
  ElMessageBox.confirm(t("sync_task.confirm_delete_msg"), {
    confirmButtonText: t("sync_task.delete"),
    cancelButtonText: t("sync_datasource.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    tableLoading.value = true;
    removeApi(row.id).then(() => {
      tableLoading.value = false;
      ElMessage.success(
          t("sync_task.delete") + " " + t("sync_task.op_success")
      );
      search();
    });
  });
};

const logStatus = ref();
const jobLogDetailRef = ref();
const showLogDetail = (row) => {
  logStatus.value = row.logStatus;
  jobLogDetailRef.value.logId = row.logId;
  jobLogDetailRef.value.jobLogDetailVisible = true;
  jobLogDetailRef.value?.startInterval(row.logId, logStatus.value);
  stopInterval();
};

const jobLogDetailVisibleClose = () => {
  jobLogDetailRef.value.jobLogDetailVisible = false;
  partDataUpdate();
};

const drawerMainOpen = async () => {
  drawerMainRef.value.init();
};
const drawerMainClose = () => {
  drawerMainRef.value.close();
};
const partDataUpdate = () => {
  const param = buildParam();
  getTaskInfoListApi(
      state.paginationConfig.currentPage,
      state.paginationConfig.pageSize,
      param
  ).then((res) => {
    let isRunning = false;
    const resDataMap = res.data.records.reduce((acc, cur) => {
      acc[cur.id] = cur;
      return acc;
    }, {});
    state.taskList.forEach((item) => {
      const resItem = resDataMap[item.id];
      if (resItem) {
        item.logId = resItem.logId;
        item.status = resItem.status;
        item.logStatus = resItem.logStatus;
        item.lastExecuteStatus = resItem.lastExecuteStatus;
        if (item.currentTime) {
          if (
              item.currentTime > resItem.executorStartTime ||
              resItem.status === "running"
          ) {
            isRunning = true;
          }
          item.currentTime = undefined;
        } else if (
            resItem.status === "running" ||
            resItem.logStatus === "RUNNING"
        ) {
          isRunning = true;
        } else {
          item.triggerLastTime = resItem.triggerLastTime;
          item.triggerNextTime = resItem.triggerNextTime;
        }
      }
    });
    if (isRunning) {
      startInterval();
    } else {
      stopInterval();
    }
  });
};
const getLastStatus = (row) => {
  // 已经执行过
  if (row.logId) {
    return row.lastExecuteStatus ?? row.logStatus;
  }
  if (row.schedulerType === "NONE") {
    row.status = "done";
  }
  return row.lastExecuteStatus ? row.lastExecuteStatus : false;
};
const copyInfo = async (val) => {
  try {
    await toClipboard(val);
    ElMessage.success(t("common.copy_success"));
    taskIdDialogVisible.value = false;
  } catch (e) {
    ElMessage.warning(t("common.copy_unsupported"));
  }
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
          <el-button @click="handleAddTask" type="primary">{{
              t("sync_task.add_task")
            }}
          </el-button>
        </el-col>
        <el-col :span="12" class="right-filter">
          <el-input
              v-model="keyword"
              clearable
              :placeholder="t('sync_task.search_input_name_desc_placeholder')"
              @change="search"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"
                >
                  <icon_searchOutline_outlined class="svg-icon"
                  />
                </Icon>
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
              <Icon name="icon-filter">
                <iconFilter class="svg-icon"/>
              </Icon>
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
            :table-data="state.taskList"
            :empty-desc="emptyDesc"
            :empty-img="imgType"
            class="popper-max-width"
            @current-change="pageChange"
            @size-change="sizeChange"
            @sort-change="sortChange"
            @selection-change="handleSelectionChange"
            :data-loading="tableLoading"
            :show-empty-img="!tableLoading"
        >
          <el-table-column
              type="selection"
              width="55"
              :selectable="rowCheckStatus"
          />
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
              key="desc"
              show-overflow-tooltip
              prop="desc"
              :label="t('sync_task.desc')"
              min-width="120"
          >
            <template #default="scope">
              <span>{{ scope.row.desc ? scope.row.desc : "-" }}</span>
            </template>
          </el-table-column>
          <el-table-column
              prop="triggerLastTime"
              :label="t('sync_task.trigger_last_time')"
              min-width="170"
          >
            <template #default="scope">
              <span>{{ timestampFormatDate(scope.row.triggerLastTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
              prop="lastExecuteStatus"
              key="lastExecuteStatus"
              :label="t('sync_task.recent_execute_status')"
              show-overflow-tooltip
              min-width="180"
          >
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <el-icon
                    v-if="scope.row.status === 'running'"
                    size="16px"
                    :class="'is-loading'"
                >
                  <Icon :style="'color:#2c5fd9'">
                    <component
                        :style="'color:#2c5fd9'"
                        :is="icon_sync_progress"
                        class="svg-icon"
                    ></component>
                  </Icon>
                </el-icon>
                <el-icon size="16px" v-else>
                  <Icon
                      :style="
                      'color:' +
                      getLogStatusIcon(getLastStatus(scope.row)).color
                    "
                  >
                    <component
                        v-if="getLastStatus(scope.row)"
                        :style="
                        'color:' +
                        getLogStatusIcon(getLastStatus(scope.row)).color
                      "
                        :is="getLogStatusIcon(getLastStatus(scope.row)).icon"
                        class="svg-icon"
                    >
                    </component>
                  </Icon>
                </el-icon>
                <span style="padding: 0 8px 0 8px">
                  {{
                    getStatusLabel(
                        scope.row.status === "running"
                            ? "running"
                            : scope.row.lastExecuteStatus ?? scope.row.logStatus
                    )
                  }}
                </span>
                <el-icon
                    @click="showLogDetail(scope.row)"
                    class="error-info"
                    v-if="
                    ((scope.row.lastExecuteStatus !== null &&
                      scope.row.lastExecuteStatus !== 'SUCCESS') ||
                      scope.row.status === 'running') &&
                    scope.row.logId
                  "
                >
                  <icon name="icon-maybe_outlined"
                  >
                    <iconMaybe_outlined class="svg-icon"
                    />
                  </icon>
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column
              prop="currentExecutedStatus"
              key="currentExecutedStatus"
              :label="t('sync_task.task_status')"
              show-overflow-tooltip
              min-width="170"
          >
            <template #default="scope">
              <div style="display: flex; align-items: center">
                <div
                    class="status"
                    :class="statusClass(scope.row.status)"
                    style="display: flex; align-items: center"
                >
                  <span style="padding: 0 8px 0 8px">{{
                      getStatusLabel(scope.row.status)
                    }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          <el-table-column
              prop="triggerNextTime"
              :label="t('sync_task.trigger_next_time')"
              min-width="170"
          >
            <template #default="scope">
              <span>{{ timestampFormatDate(scope.row.triggerNextTime) }}</span>
            </template>
          </el-table-column>
          <el-table-column
              key="sourceDsName"
              show-overflow-tooltip
              prop="sourceDsName"
              :label="t('sync_datasource.source_ds')"
              min-width="120"
          >
            <template #default="scope">
              <span>{{ scope.row.source.datasource.name }}</span>
            </template>
          </el-table-column>
          <el-table-column
              key="targetDsName"
              show-overflow-tooltip
              prop="targetDsName"
              :label="t('sync_datasource.target_ds')"
              min-width="120"
          >
            <template #default="scope">
              <span>{{ scope.row.target.datasource.name }}</span>
            </template>
          </el-table-column>
          <el-table-column
              key="target.tableName"
              show-overflow-tooltip
              prop="target.tableName"
              :label="t('sync_task.target_table')"
              min-width="170"
          >
            <template #default="scope">
              <span>{{ scope.row.target.tableName }}</span>
            </template>
          </el-table-column>
          <el-table-column
              fixed="right"
              key="_operation"
              :label="t('sync_task.operation')"
              width="100"
          >
            <template #default="scope">
              <div class="operate-icon-container" v-if="scope.row.id !== '1'">
                <el-tooltip
                    effect="dark"
                    :content="t('sync_task.edit')"
                    placement="top"
                >
                  <el-button text @click="edit(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined"
                      >
                        <icon_edit_outlined class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                    effect="dark"
                    :content="t('sync_task.running_one')"
                    placement="top"
                >
                  <el-button
                      text
                      @click="execute(scope.row)"
                      :disabled="scope.row.status === 'running'"
                  >
                    <template #icon>
                      <Icon
                          name="icon_sync-play-round_outlined"
                          v-if="scope.row.status !== 'running'"
                      >
                        <icon_syncPlayRound_outlined class="svg-icon"
                        />
                      </Icon>
                      <Icon
                          name="icon_sync-play-round_filled"
                          v-if="scope.row.status === 'running'"
                      >
                        <icon_syncPlayRound_filled class="svg-icon"
                        />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <div class="icon-more" v-if="scope.row.id !== '1'">
                  <handle-more
                      class="task-handle-more"
                      @handle-command="(cmd) => moreHandler(cmd, scope.row)"
                      :menu-list="getMoreList(scope.row)"
                  />
                </div>
              </div>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
  </div>

  <div
      v-if="state.multipleSelection.length && activeName === 'ds'"
      class="bottom-bar flex-align-center"
  >
    <el-button
        type="danger"
        class="batch-delete-button"
        plain
        @click="batchDelHandler"
    >
      {{ t("sync_task.batch_del") }}
    </el-button>
    <span class="bottom-info">{{
        t("sync_task.selection_info", [state.multipleSelection.length])
      }}</span>
    <el-button text @click="clearSelection">{{
        t("sync_task.clear_button")
      }}
    </el-button>
  </div>
  <task-form
      ref="taskFormDialogRef"
      :task-id="taskId"
      @task-add-visible-close="taskAddVisibleClose"
      @refresh-list="refreshList"
  />
  <log-details
      ref="jobLogDetailRef"
      :job-log-status="logStatus"
      @job-log-detail-visible-close="jobLogDetailVisibleClose"
  />
  <drawer-main
      :filter-options="filterOption"
      @trigger-filter="searchCondition"
      ref="drawerMainRef"
  ></drawer-main>
  <el-dialog v-model="taskIdDialogVisible" :show-close="false" :title="t('dataset.task_id')" width="420" align-center>
    <template #default>
      <div style="text-align: center">
        <span>
          {{ dialogTaskId }}
        </span>
        <el-tooltip
            effect="dark"
            :content="t('common.copy')"
            placement="top"
        >
          <el-icon
              @click="copyInfo(dialogTaskId)"
              class="hover-icon hover-icon-in-table"
          >
            <Icon name="icon_copy_outlined">
              <icon_copy_outlined class="svg-icon"/>
            </Icon>
          </el-icon>
        </el-tooltip>
      </div>
    </template>
    <template #footer>
        <span class="dialog-footer">
          <el-button @click="taskIdDialogVisible = false">{{ t('commons.close') }}</el-button>
        </span>
    </template>
  </el-dialog>
</template>
<style scoped lang="less">
.source-ds-table {
  border-radius: 4px;
}
</style>
<style lang="less">
.right-filter {
  .ed-input__wrapper {
    padding-left: 12px;
    padding-right: 12px;
  }
}

.operate-icon-container {
  .icon-more {
    color: var(--ed-color-primary) !important;
    border: 0 solid transparent;
    background-color: transparent;
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
    line-height: 26px;
    letter-spacing: 0;
    text-align: center;
    display: inline-flex;
    justify-content: center;
    align-items: center;
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

.done {
  background: #d6f4d3 !important;

  span {
    color: #2ca91f !important;
  }
}

.unexecuted {
  background: #e8e9e9 !important;

  span {
    color: #646a73 !important;
  }
}

.waiting {
  background: #e8e9e9 !important;

  span {
    color: #646a73 !important;
  }
}

.suspend {
  background: #e8e9e9 !important;

  span {
    color: #646a73 !important;
  }
}

.running {
  background: #d5e2ff !important;

  span {
    color: #2c5fd9 !important;
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
