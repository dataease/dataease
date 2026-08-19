<template>
  <el-drawer
    :title="t('threshold.drawer_title')"
    v-model="drawerVisible"
    modal-class="threshold-info-drawer"
    size="896px"
    @close="beforeClose"
    direction="rtl"
  >
    <div class="threshold-step flex-center">
      <el-steps
        custom
        style="max-width: 370px; flex: 1"
        :active="activeStep"
        align-center
      >
        <el-step>
          <template #title>
            {{ t("threshold.base_setting") }}
          </template>
        </el-step>
        <el-step>
          <template #title>
            {{ t("threshold.threshold_setting") }}
          </template>
        </el-step>
      </el-steps>
    </div>
    <div class="thread-drawer-content">
      <threshold-base-form
        ref="baseForm"
        :is-edit="isEdit"
        :chart-info="chartInfo"
        :field-list="fieldList"
        :base-form-data="thresholdInfo"
        :resource-table="resourceTable"
        v-show="activeStep === 0"
      />
      <threshold-warn-form
        ref="warnForm"
        :is-edit="isEdit"
        :field-list="fieldList"
        :warn-form-data="thresholdInfo"
        :resource-table="resourceTable"
        :resource-id="chartInfo.resourceId"
        :resource-type="chartInfo.resourceType"
        v-show="activeStep === 1"
      />
    </div>

    <template #footer>
      <span class="threshold-footer">
        <el-button secondary @click="cancelClick">{{
          t("common.cancel")
        }}</el-button>
        <el-button
          element-loading-spinner=""
          v-if="activeStep === 0"
          type="primary"
          @click="next"
          >{{ t("common.next") }}</el-button
        >
        <el-button
          element-loading-spinner=""
          v-if="activeStep === 1"
          secondary
          @click="prev"
          >{{ t("common.prev") }}</el-button
        >
        <el-button type="primary" v-if="activeStep === 1" @click="save">
          {{ t("commons.save") }}</el-button
        >
      </span>
    </template>
  </el-drawer>
</template>

<script lang="ts" setup>
import { ref, computed } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import ThresholdBaseForm from "./form/ThresholdBaseForm.vue";
import ThresholdWarnForm from "./form/ThresholdWarnForm.vue";
import { ElMessage } from "element-plus-secondary";
import {
  thresholdSaveApi,
  thresholdInfoApi,
  thresholdEditApi,
  chartInfoApi,
} from "./api";
import { ChartBaseInfo, convertChart2FieldList } from "./ThresholdPage";
import { useEmitt } from "@/hooks/web/useEmitt";
import { propTypes } from "@/utils/propTypes";

const { t } = useI18n();

const props = defineProps({
  resourceTable: propTypes.string.def("core"),
});

const drawerVisible = ref(false);
const thresholdId = ref("");
const baseForm = ref();
const baseFormData = ref({});
const warnForm = ref();
const warnFormData = ref({});
const thresholdInfo = ref({});
const chartInfo = ref<ChartBaseInfo>({
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
const fieldList = ref<object[]>([]);
const activeStep = ref(0);

const isEdit = computed<boolean>(() => !!thresholdId.value);

// method area
const cancelClick = () => {
  activeStep.value = 0;
  drawerVisible.value = false;
};

const beforeClose = () => {
  useEmitt().emitter.emit("moreBarElementClick");
  drawerVisible.value = false;
  emit("reset");
};
const prev = () => {
  useEmitt().emitter.emit("moreBarElementClick");
  activeStep.value = 0;
};
const next = async () => {
  const baseData = await baseForm?.value?.getFormData();
  if (baseData) {
    baseFormData.value = Object.assign(baseFormData.value, baseData);
    activeStep.value = 1;
    warnForm.value.setDefaultContent(baseData.thresholdRules);
  }
};
const save = async () => {
  const warnData = await warnForm?.value?.getFormData();
  if (warnData) {
    warnFormData.value = Object.assign(warnFormData.value, warnData);
    saveHandler();
  }
};
const saveHandler = () => {
  const param = {
    resourceTable: props.resourceTable,
    ...baseFormData.value,
    ...warnFormData.value,
  };
  if (isEdit.value) {
    param["id"] = thresholdId.value;
  }
  const method = isEdit.value ? thresholdEditApi : thresholdSaveApi;
  method(param).then((res) => {
    if (!res?.code) {
      ElMessage.success(t("common.save_success"));
      emit("refreshList");
      cancelClick();
    }
  });
};
const emit = defineEmits(["refreshList", "reset"]);
const open = (param: ChartBaseInfo) => {
  chartInfo.value = Object.assign(chartInfo.value, param);
  if (param?.id) {
    thresholdId.value = param.id;
  }
  if (thresholdId.value) {
    thresholdInfoApi(thresholdId.value, props.resourceTable).then((res) => {
      thresholdInfo.value = Object.assign(thresholdInfo.value, res.data);
      loadFieldOption();
    });
  } else {
    thresholdInfo.value = { chartId: param?.chartId };
    loadFieldOption();
  }
};

const loadFieldOption = async () => {
  let tableId = chartInfo?.value?.tableId;
  if (!tableId) {
    const res = await chartInfoApi(
      thresholdInfo.value["chartId"],
      props.resourceTable
    );
    if (!res.data) {
      ElMessage.error(
        t("threshold.no_view_tip") +
          (chartInfo.value.resourceType === "dashboard"
            ? t("auth.panel")
            : t("auth.screen")) +
          "!"
      );
      cancelClick();
      return;
    }
    chartInfo.value = res.data;
    tableId = res.data.tableId;
  }
  fieldList.value = convertChart2FieldList(chartInfo.value);
  drawerVisible.value = true;
};

defineExpose({ open });
</script>

<style lang="less">
.threshold-info-drawer {
  height: 100%;
  .ed-drawer__footer {
    height: 64px !important;
    padding: 16px 24px !important;
    .threshold-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-drawer__body {
    padding: 24px 24px 64px;
    .threshold-step {
      margin-bottom: 24px;
      margin-right: 50px;
      position: relative;
    }
  }
}
</style>

<style lang="less" scoped>
.thread-drawer-content {
  height: calc(100% - 48px);
}
</style>

<style lang="less">
.thread-drawer-content {
  .status-switch {
    height: 20px;
  }
}
</style>
