<template>
  <el-dialog
      v-model="dialogVisible"
      :show-close="true"
      :close-on-click-modal="false"
      :title="title"
      @close="closeHandler"
      width="605"
  >
    <div class="threshold-dialog-container">
      <div class="threshold-operate">
        <div class="target-chart">
          <span>{{ t('threshold.selected_view') }}</span>
          <span class="custom-option">
            <Icon class-name="view-type-icon"
            ><component :is="iconMap[chart['chartType']]" class="svg-icon view-type-icon"></component
            ></Icon>
            <span :title="chart['chartName']"> {{ viewTitle || chart["chartName"] }} </span>
          </span>
        </div>
        <div class="threshold-add">
          <el-button @click.stop="addRow" text>
            <template #icon>
              <icon name="icon_add_outlined">
                <icon_add_outlined class="svg-icon"/>
              </icon>
            </template>
            {{ t("common.add") }}
          </el-button>
        </div>
      </div>
      <div class="threshold-table">
        <GridTable
            ref="thresholdTableRef"
            :showPagination="state.paginationConfig.total > 10"
            :pagination="state.paginationConfig"
            :table-data="state.tableData"
            class="popper-max-width"
            @current-change="pageChange"
            @size-change="sizeChange"
            :emptyDesc="t('data_set.no_data')"
        >
          <el-table-column
              key="name"
              show-overflow-tooltip
              prop="name"
              :label="t('threshold.table_name')"
              width="324"
          />

          <el-table-column
              key="status"
              prop="status"
              :label="t('threshold.status')"
              width="105"
          >
            <template #default="scope">
              <span v-if="scope.row.status">
                <el-icon>
                  <Icon name="threshold_full"><threshold_full class="svg-icon"/></Icon>
                </el-icon>
              </span>
              <span v-else/>
            </template>
          </el-table-column>

          <el-table-column
              width="128"
              fixed="right"
              key="_operation"
              :label="t('common.operate')"
          >
            <template #default="scope">
              <div
                  style="display: flex; align-items: center"
                  class="threshold-table-operate"
              >
                <el-switch
                    v-model="scope.row.enable"
                    @change="enableChange(scope.row)"
                    size="small"
                />
                <el-divider style="margin: 0 12px" direction="vertical"/>

                <el-tooltip
                    effect="dark"
                    :content="t('common.edit')"
                    placement="top"
                >
                  <button
                      class="circle-button_icon"
                      @click="editHandler(scope.row)"
                  >
                    <Icon name="icon_edit_outlined"
                    >
                      <icon_edit_outlined class="svg-icon"
                      />
                    </Icon>
                  </button>
                </el-tooltip>

                <el-tooltip
                    effect="dark"
                    :content="t('common.delete')"
                    placement="top"
                >
                  <button
                      style="margin-left: 8px"
                      class="circle-button_icon"
                      @click="delHandler(scope.row)"
                  >
                    <Icon name="icon_delete-trash_outlined"
                    >
                      <icon_deleteTrash_outlined class="svg-icon"
                      />
                    </Icon>
                  </button>
                </el-tooltip>
              </div>
            </template>
          </el-table-column>
        </GridTable>
      </div>
    </div>
  </el-dialog>
  <threshold-drawer
      v-if="drawerVisible"
      ref="thresholdDrawer"
      :resource-table="state.resourceTable"
      @reset="drawerReset"
      @refresh-list="refreshList"
  />
</template>

<script lang="ts" setup>
import areaStack from "@/assets/svg/area-stack.svg";
import area from "@/assets/svg/area.svg";
import barGroupStack from "@/assets/svg/bar-group-stack.svg";
import barGroup from "@/assets/svg/bar-group.svg";
import barHorizontal from "@/assets/svg/bar-horizontal.svg";
import barRange from "@/assets/svg/bar-range.svg";
import barStackHorizontal from "@/assets/svg/bar-stack-horizontal.svg";
import barStack from "@/assets/svg/bar-stack.svg";
import bar from "@/assets/svg/bar.svg";
import bidirectionalBar from "@/assets/svg/bidirectional-bar.svg";
import bubbleMap from "@/assets/svg/bubble-map.svg";
import chartMixGroup from "@/assets/svg/chart-mix-group.svg";
import chartMixStack from "@/assets/svg/chart-mix-stack.svg";
import chartMix from "@/assets/svg/chart-mix.svg";
import flowMap from "@/assets/svg/flow-map.svg";
import funnel from "@/assets/svg/funnel.svg";
import gauge from "@/assets/svg/gauge.svg";
import heatMap from "@/assets/svg/heat-map.svg";
import indicator from "@/assets/svg/indicator.svg";
import line from "@/assets/svg/line.svg";
import liquid from "@/assets/svg/liquid.svg";
import map from "@/assets/svg/map.svg";
import percentageBarStackHorizontal from "@/assets/svg/percentage-bar-stack-horizontal.svg";
import percentageBarStack from "@/assets/svg/percentage-bar-stack.svg";
import pieDonutRose from "@/assets/svg/pie-donut-rose.svg";
import pieDonut from "@/assets/svg/pie-donut.svg";
import pieRose from "@/assets/svg/pie-rose.svg";
import pie from "@/assets/svg/pie.svg";
import progressBar from "@/assets/svg/progress-bar.svg";
import quadrant from "@/assets/svg/quadrant.svg";
import radar from "@/assets/svg/radar.svg";
import richText from "@/assets/svg/rich-text.svg";
import sankey from "@/assets/svg/sankey.svg";
import scatter from "@/assets/svg/scatter.svg";
import stockLine from "@/assets/svg/stock-line.svg";
import symbolicMap from "@/assets/svg/symbolic-map.svg";
import tableInfo from "@/assets/svg/table-info.svg";
import tableNormal from "@/assets/svg/table-normal.svg";
import tablePivot from "@/assets/svg/table-pivot.svg";
import treemap from "@/assets/svg/treemap.svg";
import waterfall from "@/assets/svg/waterfall.svg";
import wordCloud from "@/assets/svg/word-cloud.svg";
import tHeatmap from "@/assets/svg/t-heatmap.svg";

import icon_add_outlined from "@/assets/svg/icon_add_outlined.svg";
import threshold_full from "@/assets/svg/threshold_full.svg";
import icon_edit_outlined from "@/assets/svg/icon_edit_outlined.svg";
import icon_deleteTrash_outlined from "@/assets/svg/icon_delete-trash_outlined.svg";
import {ref, onMounted, reactive, nextTick, computed} from "vue";
import {useEmitt} from "@/hooks/web/useEmitt";
import {useI18n} from "@/hooks/web/useI18n";
import GridTable from "@/components/grid-table/src/GridTable.vue";
import {DialogRow, ChartBaseInfo} from "./ThresholdPage";
import ThresholdDrawer from "./ThresholdDrawer.vue";
import {
  thresholdGridApi,
  chartInfoApi,
  thresholdDelApi,
  thresholdSwitchApi,
  thresholdRestoreApi,
} from "./api";
import {ElMessage, ElMessageBox} from "element-plus-secondary";
import router from "@/router";
import {dvMainStoreWithOut} from '@/store/modules/data-visualization/dvMain'

const dvMainStore = dvMainStoreWithOut()

const viewTitle = computed(() => {
  if (chartId.value) {
    const canvasInfo = dvMainStore.canvasViewInfo
    if (canvasInfo && canvasInfo[chartId.value]) {
      return canvasInfo[chartId.value].title
    }
  }
  return null
})
const {t} = useI18n();
const drawerVisible = ref(false);
const title = ref(t('threshold.module_name'));
const dialogVisible = ref(false);
const chartId = ref("");
const autoDrawerId = ref("");
const chart = ref<ChartBaseInfo>({
  chartId: "",
  chartType: "",
  chartName: "",
  tableId: "",
  resourceId: "",
  resourceType: "",
  resourceName: "",
  xAxis: [],
  xAxisExt: [],
  yAxis: [],
  yAxisExt: [],
  extStack: [],
  extBubble: [],
  extLabel: [],
  extTooltip: [],
  extColor: [],
  flowMapStartName: [],
  flowMapEndName: [],
});
const thresholdDrawer = ref();

const iconMap = {
  "area-stack": areaStack,
  area: area,
  "bar-group-stack": barGroupStack,
  "bar-group": barGroup,
  "bar-horizontal": barHorizontal,
  "bar-range": barRange,
  "bar-stack-horizontal": barStackHorizontal,
  "bar-stack": barStack,
  bar: bar,
  "bidirectional-bar": bidirectionalBar,
  "bubble-map": bubbleMap,
  "chart-mix-group": chartMixGroup,
  "chart-mix-stack": chartMixStack,
  "chart-mix": chartMix,
  "flow-map": flowMap,
  funnel: funnel,
  gauge: gauge,
  "heat-map": heatMap,
  indicator: indicator,
  line: line,
  liquid: liquid,
  map: map,
  "percentage-bar-stack-horizontal": percentageBarStackHorizontal,
  "percentage-bar-stack": percentageBarStack,
  "pie-donut-rose": pieDonutRose,
  "pie-donut": pieDonut,
  "pie-rose": pieRose,
  pie: pie,
  "progress-bar": progressBar,
  quadrant: quadrant,
  radar: radar,
  "rich-text": richText,
  sankey: sankey,
  scatter: scatter,
  "stock-line": stockLine,
  "symbolic-map": symbolicMap,
  "table-info": tableInfo,
  "table-normal": tableNormal,
  "table-pivot": tablePivot,
  treemap: treemap,
  waterfall: waterfall,
  "word-cloud": wordCloud,
  "t-heatmap": tHeatmap,
};
const state = reactive({
  tableData: [] as DialogRow[],
  resourceTable: 'core',
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0,
  },
});

// method area
const drawerReset = () => {
  drawerVisible.value = false;
};

interface InitParam {
  viewId: string
  isScreen: boolean
  resourceTable: string
}

const init = (param: InitParam) => {
  const {viewId, isScreen, resourceTable} = param
  chartId.value = viewId;
  state.resourceTable = resourceTable
  loadChartInfo(isScreen);
  dialogVisible.value = true;
  search();
};
const loadChartInfo = (isScreen: boolean) => {
  chartInfoApi(chartId.value, state.resourceTable)
      .then((res) => {
        if (!res.data) {
          ElMessage.error(t('threshold.no_view_tip') + (isScreen ? t('auth.screen') : t('auth.panel')) + '!');
          closeHandler();
          return;
        }
        chart.value = res.data;
      })
      .catch(() => {
        ElMessage.error(t('threshold.no_view_tip') + (isScreen ? t('auth.screen') : t('auth.panel')) + '!');
      });
};

const closeHandler = () => {
  chartId.value = "";
  dialogVisible.value = false;
};

const addRow = () => {
  drawerVisible.value = true;
  nextTick(() => {
    thresholdDrawer?.value?.open(chart.value);
  });
};

const enableChange = (row) => {
  const param = {id: row.id, enable: row.enable, resourceTable:state.resourceTable};
  thresholdSwitchApi(param).then(() => {
    ElMessage.success(t("user.switch_success"));
  });
};

const editHandler = (row) => {
  drawerVisible.value = true;
  nextTick(() => {
    thresholdDrawer?.value?.open({...chart.value, ...{id: row.id}});
  });
};
const delHandler = (row) => {
  const idList = [row.id];
  ElMessageBox.confirm(t("data_source.sure_to_delete"), {
    confirmButtonText: t("commons.delete"),
    cancelButtonText: t("commons.cancel"),
    showCancelButton: true,
    confirmButtonType: "danger",
    type: "warning",
    autofocus: false,
    showClose: false,
    callback: (action) => {
      if (action === 'confirm') {
        thresholdDelApi(idList, state.resourceTable).then(() => {
          ElMessage.success(t("common.delete_success"));
          search();
        });
      }
    }
  })
};

const pageChange = (index) => {
  if (typeof index !== "number") {
    return;
  }
  state.paginationConfig.currentPage = index;
  search();
};
const sizeChange = (size) => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = size;
  search();
};
const buildParam = () => {
  return {
    chartId: chartId.value,
    resourceTable: state.resourceTable,
  };
};
const search = () => {
  const param = buildParam();
  thresholdGridApi(
      state.paginationConfig.currentPage,
      state.paginationConfig.pageSize,
      param
  ).then((res) => {
    state.tableData = res.data.records;
    if (
        state.paginationConfig.currentPage > 1 &&
        state.tableData.length === 0
    ) {
      state.paginationConfig.currentPage--;
      search();
    }
    state.paginationConfig.total = res.data.total;
    if (autoDrawerId.value) {
      editHandler({id: autoDrawerId.value});
      autoDrawerId.value = "";
    }
  });
};
const refreshList = () => {
  search();
  useEmitt().emitter.emit("refresh-threshold-status", chartId.value);
};
const autoThresholdDialog = () => {
  const thresholdToken = router.currentRoute.value.query.thresholdToken;
  if (thresholdToken) {
    const array = thresholdToken.split("-fit2cloud-de-v2-");
    if (!array || array.length !== 2) {
      return;
    }
    const thresholdId = array[0];
    const chartId = array[1];
    autoDrawerId.value = thresholdId;
    thresholdRestoreApi({ chartId: chartId }).then(() => {
      init({
        viewId: chartId,
        isScreen: false,
        resourceTable: 'snapshot'
      });
    })
  }
};

onMounted(() => {
  useEmitt({name: "open-threshold-dialog", callback: init});
  autoThresholdDialog();
});
</script>

<style lang="less" scoped>
.threshold-dialog-container {
  width: 100%;
  height: auto;

  .threshold-operate {
    display: flex;
    justify-content: space-between;
    width: 100%;
    height: 22px;
    line-height: 22px;

    .target-chart {
      height: 22px;
      display: flex;

      span:first-child {
        color: #646a73;
      }

      .custom-option {
        display: flex;
        align-items: center;
        height: 22px;

        svg {
          width: 20px;
          height: 20px;
          margin-right: 4px;
        }

        span {
          color: #1f2329;
          max-width: 360px;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }

    .threshold-add {
      line-height: 22px;
      display: flex;
      align-items: center;

      .button {
        line-height: 22px;
        height: 22px;
        padding: 0;
      }
    }
  }

  .threshold-table {
    margin-top: 16px;

    :deep(td) {
      padding: 6px 0 !important;
    }

    :deep(.pagination-cont) {
      margin-top: 16px !important;
    }
  }
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
</style>
