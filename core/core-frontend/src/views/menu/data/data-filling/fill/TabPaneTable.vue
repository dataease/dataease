<script setup lang="ts">
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import { computed, onMounted, ref } from "vue";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import DfDataListForm from "./DfDataListForm.vue";
import DfDataListFormMobile from "../mobile/DfDataListFormMobile.vue";
import { loadUserFillingTask } from "./fill_api";
import { useI18n } from "@/hooks/web/useI18n";
import { cloneDeep, concat } from "lodash-es";
import dayjs from "dayjs";
import { Icon } from "@/components/icon-custom";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_describe_outlined from "@/assets/svg/icon_describe_outlined.svg";
import DfCell from "../mobile/DfCell.vue";
import { isMobile } from "@/utils/utils";
import VanTabs from "vant/es/tabs";
import VanTab from "vant/es/tab";
import VanList from "vant/es/list";
import { showToast } from "vant";

const rowDataFormRef = ref();
const { t } = useI18n();

const curTypeList = [
  { key: "todo", name: t("data_fill.todo") },
  { key: "finished", name: t("data_fill.finished") },
  { key: "expired", name: t("data_fill.expired") },
];

interface Pagination {
  currentPage: number;
  pageSize: number;
  total: number;
}

const defaultPagination = {
  currentPage: 1,
  pageSize: 10,
  total: 0,
};

const paginationConfig = ref<Pagination>({
  currentPage: 1,
  pageSize: 10,
  total: 0,
});

const inMobile = computed(() => {
  return isMobile();
});

function formatDate(time) {
  if (time) {
    return dayjs(new Date(time)).format("YYYY-MM-DD HH:mm:ss");
  } else {
    return "-";
  }
}

const pageChange = (index: any) => {
  if (typeof index !== "number") {
    return;
  }
  paginationConfig.value.currentPage = index;
  loadTableData();
};

const loadingFinished = ref(false);

const nextPage = (command, currentPage) => {
  loading.value = true;
  if (firstLoadMobile.value) {
    firstLoadMobile.value = false;
    loadTableData();
  } else {
    paginationConfig.value.currentPage = currentPage + 1;
    loadTableData();
  }
};

const sizeChange = (size) => {
  paginationConfig.value.pageSize = size;
  paginationConfig.value.currentPage = 1;
  loadTableData();
};

const activeCommand = ref("todo");

function onActiveCommandChange() {
  paginationConfig.value = cloneDeep(defaultPagination);
  loadTableData();
}

const firstLoadMobile = ref(false);

function onActiveCommandChangeMobile() {
  tableData.value = [];
  paginationConfig.value = cloneDeep(defaultPagination);
  firstLoadMobile.value = true;
  loadingFinished.value = false;
}

function onPanelKeywordChange() {
  paginationConfig.value.currentPage = 1
  loadTableData();
}

const panelKeyword = ref();

const tableData = ref([]);

const loading = ref(false);

const imgType = ref();
const emptyDesc = ref("");

function getRestTime(time) {
  if (!time) {
    return t("data_fill.task.no_time_limit");
  }
  const _time = new Date(time).getTime();
  const current = new Date().getTime();

  if (_time < current) {
    return t("data_fill.task.expired");
  }
  const dateDiff = _time - current;

  const dayDiff = Math.floor(dateDiff / (24 * 3600 * 1000));
  const monthDiff = Math.floor(dayDiff / 30);
  const yearDiff = Math.floor(monthDiff / 12);

  const leave1 = dateDiff % (24 * 3600 * 1000); // 计算天数后剩余的毫秒数
  const hours = Math.floor(leave1 / (3600 * 1000)); // 计算出小时数
  // 计算相差分钟数
  const leave2 = leave1 % (3600 * 1000); // 计算小时数后剩余的毫秒数
  const minutes = Math.floor(leave2 / (60 * 1000)); // 计算相差分钟数
  // 计算相差秒数
  const leave3 = leave2 % (60 * 1000); // 计算分钟数后剩余的毫秒数
  const seconds = Math.round(leave3 / 1000);

  if (yearDiff > 0 || monthDiff > 0) {
    const yearStr = yearDiff > 0 ? yearDiff + t("cron.year") : "";
    const monthStr = monthDiff > 0 ? monthDiff + t("cron.month") : "";
    return yearStr + monthStr;
  }

  const dayStr = dayDiff > 0 ? dayDiff + t("cron.day") : "";
  const hourStr = hours > 0 ? hours + t("cron.hour") : "";

  if (dayDiff > 0) {
    return dayStr + hourStr;
  }

  const minuteStr = minutes > 0 ? minutes + t("cron.minute") : "";

  if (hours > 0) {
    return hourStr + minuteStr;
  }

  const secondsStr = seconds > 0 ? seconds + t("cron.second") : "";

  return minuteStr + secondsStr;
}

const getEmptyImg = (): string => {
  if (panelKeyword.value) {
    return "tree";
  }
  return "noneWhite";
};

const getEmptyDesc = (): string => {
  if (panelKeyword.value) {
    return t("data_set.relevant_content_found");
  }
  return t("data_export.no_task");
};

const showDialog = ref(false);
const tempSelectedForm = ref();
const tempSelectedFormEdit = ref(false);

function editForm(data) {
  if (inMobile.value) {
    tempSelectedForm.value = data;
    tempSelectedFormEdit.value = true;
    showDialog.value = true;
  } else {
    rowDataFormRef.value?.init(data.id, data.formId, true);
  }
}

function showForm(data) {
  if (inMobile.value) {
    tempSelectedForm.value = data;
    tempSelectedFormEdit.value = false;
    showDialog.value = true;
  } else {
    rowDataFormRef.value?.init(data.id, data.formId, false);
  }
}

function showNoDataToast(item) {
  showToast({
    duration: 2000,
    message: t("data_fill.data.data_not_exists"),
    position: "bottom",
    className: "de-mobile-checker-error",
  });
}

function onClickMobileItem(item) {
  if (!checkEditBtnDisabled(item)) {
    editForm(item);
  } else {
    onClickMobileItemShow(item);
  }
}

function onClickMobileItemShow(item) {
  if (!checkShowBtnDisabled(item)) {
    showForm(item);
  } else {
    showNoDataToast(item);
  }
}

function onCloseToRefresh() {
  loadTableData(true);
}

function checkEditBtnDisabled(row) {
  if (row.expired) {
    return true;
  }
  if (new Date().getTime() >= row.endTime) {
    return true;
  }
  if (row.finishCount === 0) {
    return true;
  }
  return false;
}

function checkShowBtnDisabled(row) {
  if (row.finishCount === 0) {
    return true;
  }
  return false;
}

const loadTableData = (refresh) => {
  loading.value = true;
  if (!inMobile.value || refresh) {
    tableData.value = [];
    if (inMobile.value) {
      onActiveCommandChangeMobile();
    }
  }
  loadUserFillingTask(
    {
      type: activeCommand.value,
      taskName: panelKeyword.value,
    },
    paginationConfig.value.currentPage,
    paginationConfig.value.pageSize,
  )
    .then((res) => {
      if (res.data) {
        if (!inMobile.value) {
          tableData.value = res.data.records;
        } else {
          tableData.value = concat(tableData.value, res.data.records);
          if (tableData.value.length === res.data.total) {
            loadingFinished.value = true;
          }
        }
        paginationConfig.value.total = res.data.total;
        paginationConfig.value.currentPage = res.data.current;
        paginationConfig.value.pageSize = res.data.size;
      }
    })
    .finally(() => {
      imgType.value = getEmptyImg();
      emptyDesc.value = getEmptyDesc();
      loading.value = false;
    });
};

onMounted(() => {
  loadTableData();
});
</script>

<template>
  <div
    class="data-filling-dashboard-type"
    :class="{ 'in-mobile': inMobile }"
    v-loading="loading && !inMobile"
  >
    <el-row v-if="!inMobile">
      <el-col :span="12">
        <el-select
          popper-class="menu-panel-select_popper_fill"
          class="select-type-list"
          v-model="activeCommand"
          @change="onActiveCommandChange"
        >
          <el-option
            v-for="item in curTypeList"
            :key="item.key"
            :label="item.name"
            :value="item.key"
          />
        </el-select>
      </el-col>
      <el-col class="search" :span="12">
        <el-input
          v-model="panelKeyword"
          clearable
          @change="onPanelKeywordChange"
          :placeholder="t('report.task_name')"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined">
                <icon_searchOutline_outlined class="svg-icon" />
              </Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>

    <van-tabs
      v-else
      @click-tab="onActiveCommandChangeMobile"
      v-model:active="activeCommand"
    >
      <van-tab
        v-for="item in curTypeList"
        :key="item.key"
        :name="item.key"
        :title="item.name"
      />
    </van-tabs>

    <div class="panel-table" :class="{ 'in-mobile': inMobile }">
      <template v-if="activeCommand === 'todo'">
        <GridTable
          v-if="!inMobile"
          :pagination="paginationConfig"
          @current-change="pageChange"
          @size-change="sizeChange"
          :table-data="tableData"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="workbranch-grid"
          :show-pagination="!inMobile"
        >
          <el-table-column
            key="taskName"
            prop="taskName"
            :label="t('data_fill.task.task_name')"
          />
          <el-table-column
            key="restTime"
            prop="restTime"
            :label="t('data_fill.task.task_remain_time')"
          >
            <template #default="scope">
              {{ getRestTime(scope.row.endTime) }}
            </template>
          </el-table-column>
          <el-table-column
            key="finishCount"
            prop="finishCount"
            :label="t('data_fill.task.task_progress')"
          >
            <template #default="scope">
              {{ scope.row.finishCount }} /
              <template v-if="scope.row.fillType == 1">{{
                scope.row.totalCount
              }}</template>
              <template v-else>-</template>
            </template>
          </el-table-column>
          <el-table-column
            key="endTime"
            prop="endTime"
            :label="t('data_fill.task.task_end_time')"
          >
            <template #default="scope">
              {{ scope.row.endTime ? formatDate(scope.row.endTime) : "-" }}
            </template>
          </el-table-column>
          <el-table-column
            key="assigner"
            prop="assigner"
            :label="t('data_fill.task.task_sender')"
          />
          <el-table-column
            key="startTime"
            prop="startTime"
            :label="t('data_fill.task.task_distribute_time')"
          >
            <template #default="scope">
              {{ formatDate(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column
            key="id"
            :label="t('data_fill.form.operation')"
            prop="id"
            fixed="right"
            width="96"
          >
            <template #default="scope">
              <div style="display: flex; flex-direction: row">
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('data_fill.task.start_filling')"
                >
                  <el-button text @click="editForm(scope.row)">
                    <template #icon>
                      <Icon name="icon_edit_outlined">
                        <icon_edit_outlined />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>

        <el-main style="height: 100%; padding: 0" v-else>
          <van-list
            v-model:loading="loading"
            :finished="loadingFinished"
            @load="nextPage('todo', paginationConfig.currentPage)"
          >
            <DfCell
              v-for="item in tableData"
              :key="item.id"
              :label="item.taskName"
              @click="editForm(item)"
            >
              <template #subInfo>
                <div>
                  {{
                    t("data_fill.task.task_remain_time") +
                    ": " +
                    getRestTime(item.endTime)
                  }}
                </div>
                <div>
                  {{
                    t("data_fill.task.task_expiration_time") +
                    ": " +
                    (item.endTime ? formatDate(item.endTime) : "-")
                  }}
                </div>
              </template>
            </DfCell>
          </van-list>
        </el-main>
      </template>
      <template v-if="activeCommand === 'finished'">
        <GridTable
          v-if="!inMobile"
          :pagination="paginationConfig"
          @current-change="pageChange"
          @size-change="sizeChange"
          :table-data="tableData"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="workbranch-grid"
          :show-pagination="!inMobile"
        >
          <el-table-column
            key="taskName"
            prop="taskName"
            :label="t('data_fill.task.task_name')"
          />
          <el-table-column
            key="finishTime"
            prop="finishTime"
            :label="t('data_fill.task.task_finished_time')"
          >
            <template #default="scope">
              {{ formatDate(scope.row.finishTime) }}
            </template>
          </el-table-column>
          <el-table-column
            key="endTime"
            prop="endTime"
            :label="t('data_fill.task.task_expiration_time')"
          >
            <template #default="scope">
              {{ scope.row.endTime ? formatDate(scope.row.endTime) : "-" }}
            </template>
          </el-table-column>
          <el-table-column
            key="assigner"
            prop="assigner"
            :label="t('data_fill.task.task_sender')"
          />
          <el-table-column
            key="startTime"
            prop="startTime"
            :label="t('data_fill.task.task_distribute_time')"
          >
            <template #default="scope">
              {{ formatDate(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column
            key="id"
            :label="t('data_fill.form.operation')"
            prop="id"
            fixed="right"
            width="100"
          >
            <template #default="scope">
              <div style="display: flex; flex-direction: row">
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('data_fill.task.edit_data')"
                >
                  <el-button
                    text
                    @click="editForm(scope.row)"
                    :disabled="checkEditBtnDisabled(scope.row)"
                  >
                    <template #icon>
                      <Icon name="icon_edit_outlined">
                        <icon_edit_outlined />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('data_fill.task.show_data')"
                >
                  <el-button
                    text
                    @click="showForm(scope.row)"
                    :disabled="checkShowBtnDisabled(scope.row)"
                  >
                    <template #icon>
                      <Icon name="icon_describe_outlined">
                        <icon_describe_outlined />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
        <el-main style="height: 100%; padding: 0" v-else>
          <van-list
            v-model:loading="loading"
            :finished="loadingFinished"
            @load="nextPage('finished', paginationConfig.currentPage)"
          >
            <DfCell
              v-for="item in tableData"
              :key="item.id"
              :label="item.taskName"
              @click="onClickMobileItem(item)"
            >
              <template #subInfo>
                <div>
                  {{
                    t("data_fill.task.task_finished_time") +
                    ": " +
                    formatDate(item.finishTime)
                  }}
                </div>
                <div>
                  {{
                    t("data_fill.task.task_expiration_time") +
                    ": " +
                    (item.endTime ? formatDate(item.endTime) : "-")
                  }}
                </div>
              </template>
              <template #btn> </template>
            </DfCell>
          </van-list>
        </el-main>
      </template>
      <template v-if="activeCommand === 'expired'">
        <GridTable
          v-if="!inMobile"
          :pagination="paginationConfig"
          @current-change="pageChange"
          @size-change="sizeChange"
          :table-data="tableData"
          :empty-desc="emptyDesc"
          :empty-img="imgType"
          class="workbranch-grid"
          :show-pagination="!inMobile"
        >
          <el-table-column
            key="taskName"
            prop="taskName"
            :label="t('data_fill.task.task_name')"
          />
          <el-table-column
            key="endTime"
            prop="endTime"
            :label="t('data_fill.task.task_expiration_time')"
          >
            <template #default="scope">
              {{ scope.row.endTime ? formatDate(scope.row.endTime) : "-" }}
            </template>
          </el-table-column>
          <el-table-column
            key="assigner"
            prop="assigner"
            :label="t('data_fill.task.task_sender')"
          />
          <el-table-column
            key="startTime"
            prop="startTime"
            :label="t('data_fill.task.task_distribute_time')"
          >
            <template #default="scope">
              {{ formatDate(scope.row.startTime) }}
            </template>
          </el-table-column>
          <el-table-column
            key="id"
            :label="t('data_fill.form.operation')"
            prop="id"
            fixed="right"
            width="96"
          >
            <template #default="scope">
              <div style="display: flex; flex-direction: row">
                <el-tooltip
                  class="item"
                  effect="dark"
                  :content="t('data_fill.task.show_data')"
                >
                  <el-button
                    text
                    @click="showForm(scope.row)"
                    :disabled="checkShowBtnDisabled(scope.row)"
                  >
                    <template #icon>
                      <Icon name="icon_describe_outlined">
                        <icon_describe_outlined />
                      </Icon>
                    </template>
                  </el-button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
        <el-main style="height: 100%; padding: 0" v-else>
          <van-list
            v-model:loading="loading"
            :finished="loadingFinished"
            @load="nextPage('expired', paginationConfig.currentPage)"
          >
            <DfCell
              v-for="item in tableData"
              :key="item.id"
              :label="item.taskName"
              @click="onClickMobileItemShow(item)"
            >
              <template #subInfo>
                <div>
                  {{
                    t("data_fill.task.task_expiration_time") +
                    ": " +
                    (item.endTime ? formatDate(item.endTime) : "-")
                  }}
                </div>
              </template>
              <template #btn> </template>
            </DfCell>
          </van-list>
        </el-main>
      </template>
    </div>

    <DfDataListForm
      v-if="!inMobile"
      ref="rowDataFormRef"
      @finish="onCloseToRefresh"
    />
    <DfDataListFormMobile
      v-else-if="inMobile && showDialog"
      v-model="showDialog"
      ref="rowDataFormRef"
      :sub-task-id="tempSelectedForm.id"
      :form-id="tempSelectedForm.formId"
      :edit="tempSelectedFormEdit"
      @close="
        () => {
          tempSelectedForm = {};
        }
      "
      @finish="onCloseToRefresh"
    />
  </div>
</template>

<style lang="less">
.de-mobile-checker-error {
  z-index: 4003 !important;
}
</style>
<style scoped lang="less">
.data-filling-dashboard-type {
  height: calc(100% - 61px);

  .select-type-list {
    width: 104px;

    :deep(.ed-input__wrapper) {
      padding-left: 11px;
      padding-right: 11px;
    }
  }

  &.expand {
    height: calc(100% - 89px);
  }

  .type-button {
    background-color: #fff;

    &:hover,
    &:active,
    &:focus {
      border-color: var(--ed-color-primary);
      background-color: #fff;
    }
  }

  .dashboard-type-tabs {
    margin-bottom: 16px;
  }

  .search {
    text-align: right;

    .ed-input {
      width: 240px;
    }
  }

  .panel-table {
    margin-top: 16px;
    height: calc(100% - 74px);

    :deep(.ed-table__row):hover {
      //cursor: pointer;
    }

    .name-content {
      display: flex;
      align-items: center;

      .custom-icon {
        display: none;
      }

      &:hover .custom-icon {
        cursor: pointer;
        margin-left: 8px;
        display: inherit !important;
      }
    }

    .main-color {
      font-size: 18px;
      padding: 3px;
      margin-right: 12px;
      border-radius: 4px;
      color: #fff;
    }

    .name-star {
      font-size: 15px;
      padding-left: 5px;
    }

    &.in-mobile {
      height: calc(100% - 40px);
      margin-top: 0;
    }
  }

  &.in-mobile {
    height: 100%;

    :deep(.van-tabs__line) {
      display: none;
    }
  }
}
</style>
