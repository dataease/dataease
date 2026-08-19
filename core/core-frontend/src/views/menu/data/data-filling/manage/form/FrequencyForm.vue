<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{
        t("data_fill.task.distribute_setting")
      }}</span>
    </div>
    <el-form
      ref="rateForm"
      class="report-form"
      :model="formState"
      :rules="rateRules"
      label-width="180px"
      label-position="top"
      :scroll-to-error="true"
    >
      <el-form-item
        :label="t('data_fill.task.distribute_frequency')"
        prop="rateType"
      >
        <el-radio-group v-model="formState.rateType">
          <el-radio :label="0">{{ t("data_fill.task.one_time") }}</el-radio>
          <el-radio :label="1">{{ t("data_fill.task.interval") }}</el-radio>
        </el-radio-group>

        <div class="setting-bg" v-if="formState.rateType === 1">
          <div class="rate-type-time">
            <el-select v-model="timeTypeValue" class="w140">
              <el-option
                v-for="item in timeType"
                :key="item.value"
                :label="t(item.label)"
                :value="item.value"
              />
            </el-select>
            <el-select
              v-if="timeTypeValue === 2"
              v-model="weekTypeValue"
              class="w140"
            >
              <el-option
                v-for="item in weekType"
                :key="item.value"
                :label="t(item.label)"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <el-select
              v-if="timeTypeValue === 3"
              v-model="monthTypeValue"
              class="w140"
            >
              <el-option
                v-for="item in monthType"
                :key="item.value"
                :label="item.label + t('report.date')"
                :value="item.value"
              >
              </el-option>
            </el-select>
            <el-time-picker
              class="w140"
              v-model="timePicker"
              :clearable="false"
              :picker-options="{
                selectableRange: '00:00:00 - 23:59:59',
              }"
            >
            </el-time-picker>
            <el-button link class="tail" style="margin-left: 8px">{{
              t("report.every_exec")
            }}</el-button>
          </div>
          <div class="rate-type-time second-row">
            <span class="prefix">{{ t("data_fill.task.task_finish_in") }}</span>
            <el-input-number
              v-model.number="formState.publishRangeTime"
              class="w140"
              :min="1"
              :max="100"
              :step="1"
              :precision="0"
              @change="onPublishRangeTimeChange(formState)"
              @blur="onPublishRangeTimeChange(formState)"
              @keyup.enter.native="onPublishRangeTimeChange(formState)"
            />
            <el-select v-model="formState.publishRangeTimeType" class="w140">
              <el-option
                v-for="item in simpleTimeType"
                :key="item.value"
                :label="t(item.label)"
                :value="item.value"
              />
            </el-select>

            <span class="tail">{{
              t("data_fill.task.task_finish_in_suffix")
            }}</span>
          </div>
        </div>
      </el-form-item>

      <div :class="{ 'setting-bg': formState.rateType === 0 }">
        <div v-if="formState.rateType === 0">
          <label style="font-size: 14px"
            >{{ t("data_fill.task.task_distribute_time")
            }}<span style="color: #f54a45; margin-left: 2px">*</span>
          </label>
        </div>

        <el-radio-group
          v-model="formState.oneTimeType"
          style="margin: 8px 0"
          v-if="formState.rateType !== 1"
        >
          <el-radio :label="0">{{ t("data_fill.task.deliver_now") }}</el-radio>
          <el-radio :label="1">{{
            t("data_fill.task.deliver_scheduled")
          }}</el-radio>
        </el-radio-group>

        <el-form-item
          prop="startTime"
          v-if="formState.oneTimeType === 1 || formState.rateType === 1"
        >
          <template #label>
            {{ t("dataset.start_time") }}
          </template>
          <el-date-picker
            v-model="formState.startTime"
            popper-class="df-task-top-time-select"
            class="de-time-range"
            type="datetime"
            :picker-options="startPickerOptions"
            :placeholder="t('commons.please_select') + t('report.start_time')"
            value-format="x"
          />
        </el-form-item>

        <el-form-item
          :label="
            formState.rateType
              ? t('datasource.end_time')
              : t('data_fill.task.end_time')
          "
          prop="endTime"
        >
          <el-date-picker
            v-model="formState.endTime"
            class="de-time-range"
            popper-class="df-task-top-time-select"
            type="datetime"
            :picker-options="startPickerOptions"
            :placeholder="t('data_fill.task.please_select_end_time')"
            value-format="x"
          />
        </el-form-item>
      </div>
    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { nextTick, onMounted, reactive, ref } from "vue";
import {
  monthType,
  simpleTimeType,
  startPickerOptions,
  timeType,
  weekType,
} from "../../../../system/report/form/formUtil";
import { useI18n } from "@/hooks/web/useI18n";
import dayjs from "dayjs";

const { t } = useI18n();

const props = defineProps({
  rateFormData: {
    type: Object,
    default: () => {},
  },
  isEdit: {
    type: Boolean,
    default: false,
  },
});

const timeTypeValue = ref(1);
const weekTypeValue = ref(1);
const monthTypeValue = ref(1);
const timePicker = ref(
  new Date(dayjs(new Date()).format("YYYY-MM-DD") + " 09:00:00"),
);

const validateMin = (rule, value, callback) => {
  if (
    props.rateFormData?.startTime === value &&
    props.rateFormData?.rateType === formState.value.rateType
  )
    return callback();
  if (!value) return callback();
  const val = new Date(value);
  if (formState.value.rateType !== 1) {
    if (formState.value.oneTimeType !== 0) {
      if (val.getTime() < Date.now() + 5 * 60 * 1000) {
        return callback(
          new Error(t("data_fill.task.time_check_5_minute_later_than_current")),
        );
      }
    }
  } else {
    if (val.getTime() < Date.now() - 60 * 1000) {
      return callback(
        new Error(t("data_fill.task.time_check_later_than_current")),
      );
    }
  }
  if (formState.value.rateType !== 1) {
    if (formState.value.oneTimeType !== 1) {
      return callback();
    }
  }
  if (formState?.value?.endTime) {
    if (val.getTime() >= new Date(formState.value.endTime).getTime()) {
      return callback(
        new Error(t("data_fill.task.time_check_earlier_than_end")),
      );
    }
  }
  return callback();
};
const validateMax = (rule, value, callback) => {
  if (
    props.rateFormData?.endTime === value &&
    props.rateFormData?.rateType === formState.value.rateType
  )
    return callback();
  if (!value) return callback();
  const val = new Date(value);
  if (val.getTime() < Date.now()) {
    return callback(
      new Error(t("data_fill.task.time_check_later_than_current")),
    );
  }

  if (formState.value.rateType !== 1) {
    if (formState.value.oneTimeType !== 1) {
      return callback();
    }
  }

  if (formState?.value?.startTime) {
    if (val.getTime() <= new Date(formState.value.startTime).getTime()) {
      return callback(
        new Error(t("data_fill.task.time_check_later_than_start")),
      );
    }
  }
  return callback();
};

const rateRules = reactive({
  rateType: [
    {
      required: true,
      trigger: ["change"],
      message: t("commons.cannot_be_null"),
    },
  ],
  rateVal: [
    {
      required: true,
      trigger: ["blur", "change"],
      message: t("commons.cannot_be_null"),
    },
  ],
  startTime: [
    {
      required: true,
      trigger: ["blur", "change"],
      message: t("commons.cannot_be_null"),
    },
    {
      required: true,
      validator: validateMin,
      trigger: "blur",
    },
  ],
  endTime: [
    {
      required: true,
      trigger: ["blur", "change"],
      message: t("commons.cannot_be_null"),
    },
    {
      required: false,
      validator: validateMax,
      trigger: "blur",
    },
  ],
});

const rateForm = ref(null);

const defaultFormData = ref({
  rateType: 0,
  oneTimeType: 0,
  rateVal: "",
  startTime: "",
  endTime: "",
  publishRangeTime: 1,
  publishRangeTimeType: 0,
});
const formState = ref({
  ...defaultFormData.value,
});

function onPublishRangeTimeChange(form) {
  if (!form.publishRangeTime) {
    nextTick(() => {
      form.publishRangeTime = 1;
    });
  }
}

// method area

const formatRate2Form = () => {
  if (!props.isEdit || !props.rateFormData) {
    return;
  }
  for (const key in formState.value) {
    formState.value[key] = props.rateFormData[key];
  }

  const {
    rateType,
    oneTimeType,
    rateVal,
    startTime,
    endTime,
    publishRangeTime,
    publishRangeTimeType,
  } = props.rateFormData;
  if (!rateType) {
    formState.value.rateType = rateType;
    formState.value.oneTimeType = oneTimeType;
    formState.value.rateVal = rateVal;
    formState.value.startTime = startTime;
    formState.value.endTime = endTime;
    formState.value.publishRangeTime = publishRangeTime ?? 1;
    formState.value.publishRangeTimeType = publishRangeTimeType ?? 0;
    return;
  }
  timeTypeValue.value = rateType;
  let dayTime = new Date(rateVal).getDate();
  timePicker.value = new Date(rateVal);
  if (rateType === 2) {
    weekTypeValue.value = dayTime;
  }
  if (rateType === 3) {
    monthTypeValue.value = dayTime;
  }
  formState.value.rateType = 1;
};

const formatRate2Data = () => {
  const {
    rateType,
    oneTimeType,
    startTime,
    endTime,
    publishRangeTime,
    publishRangeTimeType,
  } = formState.value;
  if (!rateType) {
    return formState.value;
  }
  let dayTime = "01";
  if (timeTypeValue.value === 2) {
    dayTime = `0${weekTypeValue.value}`;
  }
  if (timeTypeValue.value === 3) {
    dayTime =
      monthTypeValue.value < 10
        ? `0${monthTypeValue.value}`
        : monthTypeValue.value.toString();
  }
  const hms = timePicker.value
    ? dayjs(new Date(timePicker.value)).format("HH:mm:ss")
    : "00:00:01";
  const tmText = `2021-08-${dayTime} ${hms}`;
  return {
    rateType: timeTypeValue.value,
    rateVal: tmText,
    startTime,
    endTime,
    publishRangeTime,
    publishRangeTimeType,
    oneTimeType,
  };
};

const getFormData = async () => {
  const p = new Promise((r, e) => {
    rateForm?.value?.validate((valid) => {
      r(valid && formatRate2Data());
    });
  });
  return await p;
};
defineExpose({
  getFormData,
});
onMounted(() => {
  formatRate2Form();
});
</script>
<style scoped lang="less">
.report-form-container {
  height: 100%;
  margin: 0 auto;
  width: 600px;

  .report-form-title-container {
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
      font-family: var(--de-custom_font, "PingFang");
      line-height: 24px;
      font-size: 16px;
      padding-left: 8px;
    }
  }

  .report-form {
    width: 600px;
    padding-bottom: 16px;

    .ed-form-item {
      margin-bottom: 16px;
    }

    .is-error {
      margin-bottom: 40px !important;
    }

    :deep(.de-time-range) {
      width: 100% !important;

      .ed-input__wrapper {
        width: 100%;
      }
    }

    .cron-form-item {
      margin-top: -8px;
    }
  }
}

.dv-selector {
  width: 100%;
}

.label-content-details {
  width: 100%;
  display: flex;
  align-items: center;
}

.rate-type-time {
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
  .w100 {
    width: 100px !important;
  }

  .tail {
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
  }

  .prefix {
    font-family: var(--de-custom_font, "PingFang");
    font-size: 14px;
    font-weight: 400;
    margin-right: 8px;
  }
}

.setting-bg {
  padding: 10px 20px;
  background-color: #f5f6f7;
}

:deep(.ed-form-item__label) {
  line-height: 22px;
  height: 22px;
}
</style>

<style lang="less">
.df-task-top-time-select {
  margin-top: -30px !important;
}
</style>
