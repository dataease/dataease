<template>
  <el-scrollbar class="threshold-form-container">
    <div class="threshold-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{ t('threshold.base_setting') }}</span>
    </div>
    <el-form
      ref="thresholdBaseForm"
      @submit.prevent
      class="threshold-form"
      :model="formState"
      :rules="baseRules"
      label-width="180px"
      label-position="top"
      :scroll-to-error="true"
    >
      <el-form-item :label="t('threshold.name')" prop="name">
        <el-input v-model="formState.name" :placeholder="t('threshold.please_enter_name')" />
      </el-form-item>

      <el-form-item :label="t('visualization.is_enable')" prop="enable">
        <el-switch class="status-switch" v-model="formState.enable" />
      </el-form-item>

      <el-form-item :label="t('threshold.detection_time')" prop="rateType">
        <div class="rate-type">
          <el-select v-model="formState.rateType" class="w140">
            <el-option
              v-for="item in rateTypeOptions"
              :key="item.value"
              :label="t(item.label)"
              :value="item.value"
            />
          </el-select>
          <el-select
            v-if="formState.rateType === 2"
            v-model="weekTypeValue"
            class="w140"
          >
            <el-option
              v-for="item in weekTypeOptions"
              :key="item.value"
              :label="t(item.label)"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-select
            v-if="formState.rateType === 3"
            v-model="monthTypeValue"
            class="w140"
          >
            <el-option
              v-for="item in dateTypeOptions"
              :key="item.value"
              :label="item.label + t('report.date')"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-time-picker
            v-if="formState.rateType"
            class="w140 send-time"
            v-model="timePicker"
            :clearable="false"
            :picker-options="{
              selectableRange: '00:00:00 - 23:59:59',
            }"
          >
          </el-time-picker>
          <el-select v-else v-model="timeSelected" class="w140">
            <el-option
              v-for="i in 60"
              :key="i"
              :label="(i - 1) + ' ' + t('chart.minute')"
              :value="(i - 1)"
            >
            </el-option>
          </el-select>
        </div>
      </el-form-item>

      <el-form-item
        class="threshold-rules-item is-required"
        :label="t('threshold.rules')"
        prop="thresholdRules"
      >
        <div
          class="threshold-rules-tree"
          :class="{ 'threshold-rules-error': rulesError }"
        >
          <RulerTree @save="fillThresholdRules" ref="rulerTreeRef"></RulerTree>
        </div>
        <div v-if="rulesError" class="ed-form-item__error">{{ t('commons.cannot_be_null') }}</div>
      </el-form-item>
    </el-form>
  </el-scrollbar>
</template>

<script lang="ts" setup>
import { ref, onMounted, provide, toRefs } from "vue";
import { useI18n } from "@/hooks/web/useI18n";
import RulerTree from "../ruler-tree/index.vue";
import '@/utils/DateUtil'
import {
  rateTypeOptions,
  weekTypeOptions,
  dateTypeOptions,
  baseRules,
} from "./FormPage";
const { t } = useI18n();

const rulerTreeRef = ref();
const thresholdRules = ref({});
const props = defineProps({
  baseFormData: {
    type: Object,
    default: () => {},
  },
  chartInfo: {
    type: Object,
    default: () => {},
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
  resourceTable: {
    type: String,
    default: 'core',
  },
  fieldList: {
    type: Array,
    default: () => [],
  },
});
const { chartInfo, fieldList } = toRefs(props);
provide("filedList", fieldList);
const weekTypeValue = ref(1);
const monthTypeValue = ref(1);
const timePicker = ref(new Date(new Date().format("yyyy-MM-dd") + " 09:00:00"));
const timeSelected = ref(30);
const rulesError = ref(false);
const fillThresholdRules = (args) => {
  thresholdRules.value = args;
  rulerTreeRef.value.init(args);
};
const thresholdBaseForm = ref({});

const defaultFormData = ref({
  name: null,
  enable: true,
  rateType: 1,
  rateValue: "",
  thresholdRules: "",
  resourceId: "",
  resourceType: "",
  chartId: "",
  chartType: "",
});
const formState = ref({
  ...defaultFormData.value,
});

// method area

const getFormData = async () => {
  getThresholdRules();
  const p = new Promise((r, e) => {
    if (validateThresholdRules()) {
      thresholdBaseForm.value["thresholdRules"] = JSON.stringify(
        thresholdRules.value
      );
    } else {
      e(t('threshold.rules_invalid'));
      return;
    }
    thresholdBaseForm?.value?.validate((valid) => {
      r(valid && formatRate2Data());
    });
  });
  return await p;
};

const formatBase2Form = () => {
  if (!props.isEdit || !props.baseFormData) {
    return;
  }
  for (const key in formState.value) {
    if (props.baseFormData.hasOwnProperty(key)) {
      formState.value[key] = props.baseFormData[key];
    }
  }
  if (formState.value["thresholdRules"]) {
    thresholdRules.value = JSON.parse(formState.value["thresholdRules"]);
    fillThresholdRules(JSON.parse(formState.value["thresholdRules"]));
  }
  const { rateType, rateValue } = props.baseFormData;

  if (!rateType) {
    formState.value.rateType = rateType;
    formState.value.rateValue = rateValue;
    timeSelected.value = parseInt(rateValue);
    return;
  }

  let dayTime = new Date(rateValue).getDate();
  timePicker.value = new Date(rateValue);
  if (rateType === 2) {
    weekTypeValue.value = dayTime;
  }
  if (rateType === 3) {
    monthTypeValue.value = dayTime;
  }
};

const formatRate2Data = () => {
  const { rateType } = formState.value;
  const tempResult = {
    rateType: formState.value.rateType,
    name: formState.value.name,
    enable: formState.value.enable,
    thresholdRules: JSON.stringify(thresholdRules.value),
    resourceId: chartInfo.value.resourceId,
    resourceType: chartInfo.value.resourceType,
    chartId: chartInfo.value.chartId,
    chartType: chartInfo.value.chartType,
  }
  if (!rateType) {
    formState.value.rateValue = timeSelected.value + "";
    tempResult['rateValue'] = timeSelected.value + ""
    return tempResult
  }
  let dayTime = "01";
  if (formState.value.rateType === 2) {
    dayTime = `0${weekTypeValue.value}`;
  }
  if (formState.value.rateType === 3) {
    dayTime =
      monthTypeValue.value < 10
        ? `0${monthTypeValue.value}`
        : monthTypeValue.value.toString();
  }
  const hms = timePicker.value
    ? new Date(timePicker.value).format("hh:mm:ss")
    : "00:00:01";
  const tmText = `2021-08-${dayTime} ${hms}`;
  tempResult['rateValue'] = tmText
  return tempResult
};

const getThresholdRules = () => {
  rulerTreeRef.value.submit();
};

const validateThresholdRules = () => {
  if (!thresholdRules.value?.items?.length) {
    rulesError.value = true;
    return false;
  }
  // 如何校验json内部结构？
  rulesError.value = false;
  return true;
};

defineExpose({
  getFormData,
});
onMounted(async () => {
  formatBase2Form();
});
</script>

<style scoped lang="less">
.threshold-form-container {
  height: 100%;
  margin: 0 auto;
  .threshold-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
    margin-top: 24px;
    margin-bottom: 16px;
    .title-flag {
      height: 16px;
      line-height: 16px;
      border-left: 2px solid var(--ed-color-primary, #3370ff);
    }
    .form-title {
      color: #1f2329;
      font-weight: 500;
      font-family: var(--de-custom_font, 'PingFang');
      line-height: 24px;
      font-size: 16px;
      padding-left: 8px;
    }
  }

  .threshold-form {
    width: 100%;
    padding-bottom: 16px;
    .ed-form-item {
      margin-bottom: 16px;
    }
    .is-error {
      margin-bottom: 40px !important;
    }
    .threshold-rules-item {
      .ed-form-item__label::after {
        content: "*";
        color: var(--ed-color-danger);
        margin-left: 2px;
        font-family: var(--de-custom_font, 'PingFang');
        font-size: 14px;
        font-style: normal;
        font-weight: 400;
      }
      :deep(.threshold-rules-tree) {
        width: 100%;
        border: 1px solid #e2e4e7;
        border-radius: 4px !important;
        max-height: 60vh !important;
        padding: 16px !important;
        overflow-x: auto !important;
        .rowAuth {
          width: 100%;
        }
      }
      :deep(.threshold-rules-error) {
        border-color: red !important;
      }
    }
  }
  :deep(.ed-form-item__label) {
    line-height: 22px;
    height: 22px;
  }
}

.rate-type {
  height: 32px;
  width: 100%;
  border-radius: 4px;
  display: flex;
  align-items: center;
  margin-top: 8px;
  .w140 {
    width: 140px !important;
    margin-right: 8px;
  }

  .tail {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
  }
}
:deep(.send-time) {
  width: 200px !important;
}
</style>
