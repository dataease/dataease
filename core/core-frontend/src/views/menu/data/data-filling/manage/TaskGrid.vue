<script lang="ts" setup>
import iconMaybe_outlined from "@/assets/svg/icon-maybe_outlined.svg";
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import iconFilter from "@/assets/svg/icon-filter.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_succeed_filled from "@/assets/svg/icon_succeed_filled.svg";
import icon_close_filled from "@/assets/svg/icon_close_filled.svg";
import icon_sync_progress from "@/assets/svg/icon_sync_progress.svg";
import icon_syncPlayRound_outlined from "@/assets/svg/icon_sync-play-round_outlined.svg";
import { onMounted, onUnmounted, reactive, ref } from "vue";
import {
  deleteDfTask,
  executeTaskApi,
  isOrgAdminApi,
  startTaskApi,
  stopTaskApi,
  taskPager,
  userOptionApi,
} from "./task_api";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import { ElIcon, ElMessage, ElMessageBox } from "element-plus-secondary";
import { map, find } from "lodash-es";
import { useI18n } from "@/hooks/web/useI18n";
import { HandleMore } from "@/components/handle-more";
import { Icon } from "@/components/icon-custom";
import dayjs from "dayjs";
import LogDetails from "./LogDetails.vue";
import DrawerMain from "@/components/drawer-main/src/DrawerMain.vue";
import { convertFilterText, FilterText } from "@/components/filter-text";
import { filterOption } from "./options";
import { useEmitt } from "@/hooks/web/useEmitt";
import { useCache } from "@/hooks/web/useCache";
import DataFillingTaskForm from "./form/DataFillingTaskForm.vue";
import SubTaskGrid from "./SubTaskGrid.vue";
import { startPickerOptions } from "../../../system/report/form/formUtil";

const props = withDefaults(
  defineProps<{
    formName?: string;
    formId?: string;
    columns?: Array<any>;
    forms?: Array<any>;
  }>(),
  {
    columns: () => [],
    forms: () => [],
  }
);

interface FieldSort {
  field: string;
  type: boolean;
}

const { t } = useI18n();
const isOrgAdmin = ref(false);
const { wsCache } = useCache();
const drawerMainRef = ref();
const taskId = ref<string>("");
const keyword = ref(null);
const multipleTableRef = ref();
const tableLoading = ref<boolean>(false);
const imgType = ref();
const emptyDesc = ref("");
const state = reactive({
  taskList: [],
  filterTexts: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  conditions: [],
  orders: [] as FieldSort[],
  multipleSelection: [],
});
state.filterTexts = [];
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
  const stopMenu = {
    label: t("sync_task.stop"),
    divided: false,
    command: "stop",
  };
  const startMenu = {
    label: t("sync_task.start"),
    divided: false,
    command: "start",
  };
  const moreList = [
    {
      label: t("data_fill.task.open_sub_task"),
      divided: false,
      command: "showLog",
    },
    {
      label: t("commons.delete"),
      divided: false,
      command: "delete",
    },
  ];
  if (row.status === 2) {
    moreList.splice(0, 0, startMenu);
  } else if (row.status < 2) {
    moreList.splice(0, 0, stopMenu);
  }
  return moreList;
};
const emits = defineEmits(["openTaskLog"]);
const openTaskLogList = (row) => {
  subTaskRef.value?.open(row.id, props.formId);
};
const moreHandler = (cmd: string, row) => {
  if (cmd === "stop" || cmd === "start") {
    changeStatus(row);
    return;
  }
  if (cmd === "showLog") {
    openTaskLogList(row);
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
  return dayjs(new Date(value)).format("YYYY-MM-DD HH:mm:ss");
};

const lastStatus = [
  { label: t("report.last_status_running"), value: 1 },
  { label: t("report.last_status_success"), value: 2 },
  { label: t("report.last_status_fail"), value: 3 },
];

const taskStatus = [
  { label: t("report.status_wait"), value: 0 },
  { label: t("report.status_send"), value: 1 },
  { label: t("report.status_stop"), value: 2 },
  { label: t("report.status_finish"), value: 3 },
];

const getLogStatusIcon = (value) => {
  const iconObj = {
    icon: "-",
    color: "",
  };
  if (value === 2) {
    iconObj.icon = icon_succeed_filled;
    iconObj.color = "#34C724";
  }
  if (value === 3) {
    iconObj.icon = icon_close_filled;
    iconObj.color = "#F54A45";
  }
  if (value === 1) {
    iconObj.icon = icon_sync_progress;
    iconObj.color = "#2c5fd9";
  }
  return iconObj;
};
const getStatusLabel = (value) => {
  if (value !== null && value !== undefined) {
    const status = find(taskStatus, ["value", value]);
    if (status) {
      return status.label;
    }
  }
  return "-";
};

const getLastStatusLabel = (value) => {
  if (value !== null && value !== undefined) {
    const status = find(lastStatus, ["value", value]);
    if (status) {
      return status.label;
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
  const param = buildParam();
  taskPager(
    props.formId,
    param,
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize
  ).then((res) => {
    state.taskList = res.data.records;
    if (state.paginationConfig.currentPage > 1 && state.taskList.length === 0) {
      state.paginationConfig.currentPage--;
      search();
    }
    state.paginationConfig.total = res.data.total;
  });
};

const noticeCall = (args) => {
  const argObj = JSON.parse(args);
  if (!state.taskList?.length || !argObj?.taskId) {
    return;
  }
  if (state.taskList.some((item) => item["id"] === argObj.taskId)) {
    search();
  }
};

let timer;

function hasPassedTask() {
  if (state?.taskList) {
    for (let i = 0; i < state.taskList.length; i++) {
      const s = state.taskList[i];
      if (s.nextExecTime) {
        if (new Date().getTime() > s.nextExecTime) {
          search();
        }
      } else {
        if (s.status == 1) {
          search();
        }
      }
    }
  }
}

onMounted(async () => {
  useEmitt({ name: "report-notice-call", callback: noticeCall });
  const res = await isOrgAdminApi();
  isOrgAdmin.value = res.data;
  search();
  timer = setInterval(() => {
    hasPassedTask();
  }, 10000);
});

onUnmounted(() => {
  if (timer) {
    clearInterval(timer);
  }
});

const batchDelHandler = () => {
  ElMessageBox.confirm(t("data_fill.task.confirm_batch_delete"), {
    confirmButtonText: t("common.delete"),
    cancelButtonText: t("commons.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    tableLoading.value = true;
    deleteDfTask(props.formId, map(state.multipleSelection, "id"))
      .then((res) => {
        if (res.code === 0) {
          ElMessage.success(t("commons.delete_success"));
          search();
        }
      })
      .finally(() => {
        tableLoading.value = false;
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
  state.paginationConfig.currentPage = 1
  search();
};
const sortChange = (param) => {
  state.orders = [];
  if (param.order && param.prop === "lastExecTime") {
    const type = param.order.substring(0, param.order.indexOf("ending"));
    state.orders.push({
      field: "timeDesc",
      type: type !== "asc",
    });
    search();
  }
};

const changeStatus = (row) => {
  ElMessageBox.confirm(
    (row.status === 2 ? t("sync_task.start") : t("sync_task.stop")) +
      t("sync_task.task_text"),
    {
      confirmButtonText: "",
      cancelButtonText: t("commons.cancel"),
      showCancelButton: true,
      confirmButtonType: "primary",
      type: "warning",
      autofocus: false,
      showClose: false,
    }
  ).then(() => {
    if (row.status === 2) {
      tableLoading.value = true;
      startTaskApi(props.formId, row.id)
        .then((res) => {
          if (res.code === 0) {
            ElMessage.success(
              t("sync_task.start") + t("sync_task.status_success")
            );
            search();
          }
        })
        .finally(() => {
          tableLoading.value = false;
        });
    } else {
      tableLoading.value = true;
      stopTaskApi(props.formId, row.id)
        .then((res) => {
          if (res.code === 0) {
            ElMessage.success(
              t("sync_task.stop") + t("sync_task.status_success")
            );
            search();
          }
        })
        .finally(() => {
          tableLoading.value = false;
        });
    }
  });
};

const delHandler = (row) => {
  ElMessageBox.confirm(t("data_source.sure_to_delete"), {
    confirmButtonText: t("commons.delete"),
    cancelButtonText: t("commons.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
  }).then(() => {
    tableLoading.value = true;
    deleteDfTask(props.formId, [row.id])
      .then((res) => {
        if (res.code === 0) {
          ElMessage.success(t("commons.delete_success"));
          search();
        }
      })
      .finally(() => {
        tableLoading.value = false;
      });
  });
};

const jobLogDetailRef = ref();
const showLogDetail = (row) => {
  jobLogDetailRef.value.jobLogDetailVisible = true;
  jobLogDetailRef.value?.startInterval(null, row.id);
};

const jobLogDetailVisibleClose = () => {
  jobLogDetailRef.value.jobLogDetailVisible = false;
};

const drawerMainOpen = async () => {
  if (!isOrgAdmin.value && filterOption.length === 4) {
    filterOption.splice(0, 1);
  } else if (isOrgAdmin.value && filterOption.length === 3) {
    filterOption.splice(0, 0, {
      type: "select",
      option: [],
      field: "uidList",
      title: t("dataset.create_by"),
      operate: "in",
      property: {
        placeholder: t("commons.please_select"),
      },
    });
    const res = await userOptionApi();
    let options = res.data;
    if (!options) {
      options = [];
    }
    options.splice(0, 0, {
      id: "1",
      name: t("role.manager"),
    });
    filterOption[0].option = options;
  }

  drawerMainRef.value.init();
};
const drawerMainClose = () => {
  drawerMainRef.value.close();
};

//task
const taskFormRef = ref();

function addTask() {
  taskFormRef.value?.open();
}

function editTask(item) {
  taskFormRef.value?.open(item.id);
}

function onCloseToRefreshTask() {
  search();
}

const quickExecuteTaskItem = ref();

function executeNow(item) {
  endTime.value = undefined;
  quickExecuteTaskItem.value = item;
  showQuickExecuteRef.value = true;
}

function closeShowQuickExecuteRef() {
  showQuickExecuteRef.value = false;
}

function confirmQuickExecuteRef() {
  if (endTime.value === undefined) {
    return;
  }
  if (endTime.value <= new Date().getTime()) {
    ElMessage.error(t("data_fill.task.end_time_error"));
    return;
  }
  executeTaskApi(
    quickExecuteTaskItem.value.id,
    props.formId,
    endTime.value
  ).then((res) => {
    closeShowQuickExecuteRef();
    search();
  });
}

const showQuickExecuteRef = ref(false);
const endTime = ref(undefined);

const subTaskRef = ref();
</script>
<template>
  <div
    class="df-base-container"
    :class="{
      'df-table-container-no-bottom': state.multipleSelection.length,
    }"
  >
    <div
      class="df-table-container df-table"
      :class="{
        'df-table-bottom': state.multipleSelection.length,
      }"
    >
      <el-row class="search-operate">
        <el-col :span="12">
          <el-button @click="addTask" type="primary"
            >{{ t("sync_task.add_task") }}
          </el-button>
        </el-col>
        <el-col :span="12" class="right-filter">
          <el-input
            v-model="keyword"
            clearable
            :placeholder="t('commons.search') + t('data_fill.task.name')"
            class="df-search"
            @change="search"
          >
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined">
                  <icon_searchOutline_outlined />
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
                <iconFilter class="svg-icon" />
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
      />
      <div
        :class="[
          state.filterTexts.length ? 'is-in-filter' : 'report-table__content',
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
          :show-empty-img="!tableLoading"
          v-loading="tableLoading"
        >
          <el-table-column
            type="selection"
            width="55"
            :selectable="() => true"
          />
          <el-table-column
            key="name"
            show-overflow-tooltip
            prop="name"
            :label="t('sync_task.name')"
            min-width="170"
          >
            <template #default="scope">
              <span
                class="task-span-link"
                @click="openTaskLogList(scope.row)"
                >{{ scope.row.name }}</span
              >
            </template>
          </el-table-column>

          <el-table-column
            prop="lastExecTime"
            :label="t('sync_task.trigger_last_time')"
            min-width="170"
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
                  <Icon>
                    <component
                      :style="
                        'color:' +
                        getLogStatusIcon(scope.row.lastExecStatus).color
                      "
                      :is="getLogStatusIcon(scope.row.lastExecStatus).icon"
                    ></component>
                  </Icon>
                </el-icon>
                <span style="padding: 0 8px 0 8px">{{
                  getLastStatusLabel(scope.row.lastExecStatus)
                }}</span>
                <el-icon
                  @click="showLogDetail(scope.row)"
                  class="error-info"
                  v-if="scope.row.lastExecStatus === 3"
                >
                  <icon name="icon-maybe_outlined">
                    <iconMaybe_outlined class="svg-icon" />
                  </icon>
                </el-icon>
              </div>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            key="status"
            :label="t('data_fill.task.send_status')"
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
          <el-table-column
            prop="creator"
            :label="t('report.creator')"
            min-width="100"
          />
          <el-table-column
            prop="createTime"
            :label="t('report.create_time')"
            min-width="170"
          >
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
                <el-tooltip
                  effect="dark"
                  :content="t('commons.edit')"
                  placement="top"
                >
                  <el-button text @click="editTask(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined">
                        <icon_edit_outlined />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  effect="dark"
                  :content="t('data_fill.task.execute_now')"
                  placement="top"
                >
                  <el-button text @click="executeNow(scope.row)">
                    <template #icon>
                      <Icon name="icon_sync-play-round_outlined">
                        <icon_syncPlayRound_outlined class="svg-icon" />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <div class="icon-more">
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
    <div
      v-if="state.multipleSelection.length"
      class="df-bottom-bar flex-align-center"
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
      <el-button text @click="clearSelection"
        >{{ t("sync_task.clear_button") }}
      </el-button>
    </div>

    <DataFillingTaskForm
      ref="taskFormRef"
      :form-id="formId"
      :form-name="formName"
      :columns="columns"
      :forms="forms"
      @finish="onCloseToRefreshTask"
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
    />
    <SubTaskGrid ref="subTaskRef" />

    <el-dialog
      :title="t('data_fill.task.distribute_setting')"
      v-model="showQuickExecuteRef"
      width="420px"
      :close-on-click-modal="false"
      @submit.prevent
    >
      <div @keydown.stop @keyup.stop>
        <el-form-item :label="t('data_fill.task.end_time')" prop="endTime">
          <el-date-picker
            v-model="endTime"
            class="de-time-range"
            popper-class="df-task-top-time-select"
            type="datetime"
            :picker-options="startPickerOptions"
            :placeholder="t('data_fill.task.please_select_end_time')"
            value-format="x"
          />
        </el-form-item>
      </div>
      <template #footer>
        <el-button secondary @click="closeShowQuickExecuteRef()">{{
          t("dataset.cancel")
        }}</el-button>
        <el-button type="primary" @click="confirmQuickExecuteRef()">{{
          t("dataset.confirm")
        }}</el-button>
      </template>
    </el-dialog>
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
  padding: 24px;
  display: flex;
  height: 100%;
  width: 100%;
  flex-direction: column;
}

.df-table-container-no-bottom {
  padding: 24px 24px 0;
}

.df-table-container {
  padding: 24px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.df-table {
  padding: 24px;
  background: #fff;
  height: 100%;

  .search-operate {
    margin-bottom: 16px;
  }
}

.df-table-bottom {
  height: calc(100% - 64px);
}

.report-table__content {
  height: calc(100% - 32px - 16px);
}

.is-in-filter {
  height: calc(100% - 32px - 16px - 17px - 24px - 17px);
}

.right-filter {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;

  .df-search {
    width: 240px;
    margin-right: 12px;
  }

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
    font-family: var(--de-custom_font, "PingFang");
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

.df-bottom-bar {
  z-index: 800;
  position: unset !important;
  min-height: 64px;
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

:deep(.de-time-range) {
  width: 100% !important;

  .ed-input__wrapper {
    width: 100%;
  }
}
</style>
