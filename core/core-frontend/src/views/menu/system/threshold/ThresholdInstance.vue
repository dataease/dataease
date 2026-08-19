<script setup lang="ts">
import icon_searchOutline_outlined from "@/assets/svg/icon_search-outline_outlined.svg";
import threshold_full from "@/assets/svg/threshold_full.svg";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import { ElIcon } from "element-plus-secondary";
import { Icon } from "@/components/icon-custom";
import { onMounted, reactive, ref } from "vue";
import { thresholdInstanceGridApi } from "./api";
import dayjs from "dayjs";
import { useI18n } from "@/hooks/web/useI18n";
import { sanitizeHtml } from "@/utils/utils";
import { TaskParam } from "./options";

const { t } = useI18n();
const keyword = ref();
const imgType = ref();
const tableLoading = ref(true);
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
const first = ref(true);

const props = defineProps<{
  task: TaskParam;
}>();

const state = reactive({
  taskLogList: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
  conditions: [],
  multipleSelection: [],
});

const buildParam = () => {
  const param = {};
  if (props?.task?.taskId && first.value) {
    param["thresholdId"] = props.task.taskId;
    keyword.value = props.task.taskName;
  }
  if (keyword.value) {
    param["keyword"] = keyword.value;
  }
  return param;
};
const search = () => {
  const param = buildParam();
  tableLoading.value = true;
  thresholdInstanceGridApi(
    state.paginationConfig.currentPage,
    state.paginationConfig.pageSize,
    param
  )
    .then((res) => {
      state.taskLogList = res.data.records;
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
    })
    .finally(() => {
      tableLoading.value = false;
    });
};
const keywordChange = () => {
  first.value = false;
  search();
};
onMounted(() => {
  search();
});

const pageChange = (index: any) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  search();
};
const sizeChange = (size) => {
  state.paginationConfig.currentPage = 1;
  state.paginationConfig.pageSize = size;
  search();
};
const timestampFormatDate = (value) => {
  if (!value) {
    return "-";
  }
  return dayjs(new Date(value)).format("YYYY-MM-DD HH:mm:ss");
};
</script>

<template>
  <div
    :class="
      !!state.multipleSelection.length && 'report-instance-table-selection'
    "
    class="report-instance-table de-search-table"
  >
    <el-row class="report-instance-table__filter top-operate">
      <el-col :span="12">
        <!-- <el-button secondary>告警记录设置</el-button> -->
      </el-col>
      <el-col :span="12" class="right-filter">
        <el-input
          v-model="keyword"
          clearable
          :placeholder="t('threshold_warn.search_placeholder')"
          @change="keywordChange"
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"
                ><icon_searchOutline_outlined class="svg-icon"
              /></Icon>
            </el-icon>
          </template>
        </el-input>
      </el-col>
    </el-row>

    <div class="report-instance-table__content">
      <GridTable
        ref="multipleTableRef"
        :pagination="state.paginationConfig"
        :table-data="state.taskLogList"
        :empty-desc="emptyDesc"
        :empty-img="imgType"
        class="popper-max-width"
        :show-empty-img="!tableLoading"
        @current-change="pageChange"
        @size-change="sizeChange"
      >
        <el-table-column
          key="id"
          show-overflow-tooltip
          prop="name"
          :label="t('threshold.table_name')"
          width="220"
        />

        <el-table-column
          prop="execTime"
          :label="t('threshold.detection_time')"
          width="175"
        >
          <template #default="scope">
            <span>{{ timestampFormatDate(scope.row.execTime) }}</span>
          </template>
        </el-table-column>

        <el-table-column
          key="status"
          prop="status"
          :label="t('threshold.status')"
          width="105"
        >
          <template #default="scope">
            <span v-if="scope.row.status">
              <el-icon>
                <Icon name="threshold_full"
                  ><threshold_full class="svg-icon"
                /></Icon>
              </el-icon>
            </span>
            <span v-else />
          </template>
        </el-table-column>

        <el-table-column
          key="content"
          show-overflow-tooltip
          prop="content"
          :label="t('threshold.notification_content')"
        >
          <template #default="scope">
            <div class="threshold-content-col">
              <div v-html="sanitizeHtml(scope.row.content)"></div>
            </div>
          </template>
        </el-table-column>
      </GridTable>
    </div>
  </div>
</template>

<style scoped lang="less">
.threshold-content-col {
  display: inline-flex;
  overflow: hidden;
  line-height: 24px;
  height: 24px;
  text-overflow: ellipsis;
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
}

.report-instance-table-selection {
  height: calc(100% - 126px);
  .report-instance-table__content {
    height: calc(100vh - 313px);
  }
}

.right-filter {
  .ed-input__wrapper {
    padding-left: 12px;
    padding-right: 12px;
  }
}
</style>
