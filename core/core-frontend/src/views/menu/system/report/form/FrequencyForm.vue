<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag" />
      <span class="form-title">{{ t('report.send_setting') }}</span>
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
      <el-form-item :label="t('report.form.rate')" prop="rateType">
        <el-radio-group v-model="formState.rateType" @change="rateTypeChange">
          <el-radio :label="1">{{ t('datasource.simple_cron') }}</el-radio>
          <el-radio :label="0">{{ t('sync_task.cron_expression') }}</el-radio>
        </el-radio-group>

        <div class="rate-type-time" v-if="formState.rateType === 1">
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
              :label="(item.label + t('report.date'))"
              :value="item.value"
            >
            </el-option>
          </el-select>
          <el-time-picker
            class="w140 send-time"
            v-model="timePicker"
            :clearable="false"
            :picker-options="{
              selectableRange: '00:00:00 - 23:59:59',
            }"
          >
          </el-time-picker>
          <span class="tail">{{ t('report.every_exec')}}</span>
        </div>
      </el-form-item>

      <el-form-item class="cron-form-item" v-if="formState.rateType === 0" label="" prop="rateVal">
        <el-input
          v-model="formState.rateVal"
          style="width: 100%"
        />
      </el-form-item>

      <el-form-item :label="t('report.start_time')" prop="startTime">
        <el-date-picker
          v-model="formState.startTime"
          class="de-time-range"
          type="datetime"
          :picker-options="startPickerOptions"
          :placeholder="
            t('sync_task.please_choose_start_time')
          "
          value-format="x"
        />
      </el-form-item>

      <el-form-item :label="t('report.end_time')" prop="endTime">
        <el-date-picker
          v-model="formState.endTime"
          class="de-time-range"
          type="datetime"
          :picker-options="startPickerOptions"
          :placeholder="
            t('sync_task.please_choose_end_time')
          "
          value-format="x"
        />
      </el-form-item>

      <div class="report-form-title-container">
        <span class="title-flag" />
        <span class="form-title">{{ t('report.retrying_settings') }}</span>
      </div>

      <el-form-item :label="t('report.form.retrying')" prop="retryEnable">
        <el-switch v-model="formState.retryEnable" @change="retryEnableChange"/>
      </el-form-item>

      <el-form-item v-if="formState.retryEnable" :label="t('sync_task.retry_attempts_on_failure')" prop="retryLimit">
        <el-input-number
          style="width: 100%;"
          v-model="formState.retryLimit"
          :min="1"
          :max="30"
          controls-position="right"
        />
      </el-form-item>

      <el-form-item v-if="formState.retryEnable" :label="t('report.form.retrying_rate')" prop="retryInterval">
        <el-input-number
          style="width: 100%;"
          v-model="formState.retryInterval"
          :min="1"
          :max="60"
          controls-position="right"
        />
      </el-form-item>

    </el-form>
  </div>
</template>

<script lang="ts" setup>
import { reactive, ref, onMounted} from 'vue'
import { startPickerOptions, timeType, weekType, monthType } from './formUtil'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
  rateFormData: {
    type: Object,
    default: () => {
    }
  },
  isEdit: {
    type: Boolean,
    default: false
  }
})

const timeTypeValue = ref(1)
const weekTypeValue = ref(1)
const monthTypeValue = ref(1)
const timePicker = ref(new Date(new Date().format('yyyy-MM-dd') + ' 09:00:00'))

const validateMin = (rule, value, callback) => {
  if (props.rateFormData?.startTime === value)
    return callback();
  if (!value) return callback();
  const val = new Date(value);
  if (val.getTime() < Date.now() - 60 * 1000) {
    return callback(new Error(t('data_fill.task.time_check_later_than_current')));
  }
  if (formState?.value?.endTime) {
    if (val.getTime() >= new Date(formState.value.endTime).getTime()) {
      return callback(new Error(t('data_fill.task.time_check_earlier_than_end')));
    }
  }
  return callback();
}
const validateMax = (rule, value, callback) => {
  if (props.rateFormData?.endTime === value)
    return callback();
  if (!value) return callback();
  const val = new Date(value);
  if (val.getTime() < Date.now()) {
    return callback(new Error(t('data_fill.task.time_check_later_than_current')));
  }

  if (formState?.value?.startTime) {
    if (val.getTime() <= new Date(formState.value.startTime).getTime()) {
      return callback(new Error(t('data_fill.task.time_check_later_than_start')));
    }
  }
  return callback();
}

const validateRetryLimit = (rule, value, callback) => {
  if (value === '' || Number.isInteger(value) && value > 0 && value < 31) {
    callback()
  } else {
    callback(new Error(t('report.form.please_input_positive_int', ['1-30'])));
  }
}

const validateRetryInterval = (rule, value, callback) => {
  if (value === '' || Number.isInteger(value) && value > 0 && value < 61) {
    callback()
  } else {
    callback(new Error(t('report.form.please_input_positive_int', ['1-60'])));
  }
}

const rateRules = reactive({
  rateType: [
    {
      required: true,
      trigger: ['change'],
      message: t("commons.cannot_be_null"),
    }
  ],
  rateVal: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    }
  ],
  startTime: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null"),
    },
    {
      required: true,
      validator: validateMin,
      trigger: "blur",
    }
  ],
  endTime: [
    {
      required: false,
      validator: validateMax,
      trigger: "blur",
    }
  ],
  retryLimit: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null")
    },
    {
      required: true,
      validator: validateRetryLimit,
      trigger: ['blur', 'change']
    }
  ],
  retryInterval: [
    {
      required: true,
      trigger: ['blur', 'change'],
      message: t("commons.cannot_be_null")
    },
    {
      required: true,
      validator: validateRetryInterval,
      trigger: ['blur', 'change']
    }
  ]
})


const rateForm = ref(null)

const defaultFormData = ref({
  rateType: 1,
  rateVal: '',
  startTime: +new Date(),
  endTime: '',
  retryEnable: false,
  retryLimit: 3,
  retryInterval: 5
})
const formState = ref({
  ...defaultFormData.value
})


// method area
const retryEnableChange = () => {
  if (!formState.value.retryLimit) {
    formState.value.retryLimit = 3
  }
  if (!formState.value.retryInterval) {
    formState.value.retryInterval = 5
  }
}
const rateTypeChange = val => {
  if (!val) {
    formState.value.rateVal = '0 0 9 * * ?'
  }
}
const formatRate2Form = () => {
  if (!props.isEdit || !props.rateFormData) {
    return
  }
  for (const key in formState.value) {
    formState.value[key] = props.rateFormData[key]
  }
  
  const { rateType, rateVal, startTime, endTime, retryEnable, retryLimit, retryInterval } = props.rateFormData
  formState.value.retryEnable = retryEnable
  formState.value.retryLimit = retryLimit
  formState.value.retryInterval = retryInterval
  if(!rateType) {
    formState.value.rateType = rateType
    formState.value.rateVal = rateVal
    formState.value.startTime = startTime
    formState.value.endTime = endTime
    return
  }
  timeTypeValue.value = rateType
  let dayTime = new Date(rateVal).getDate()
  timePicker.value = new Date(rateVal)
  if (rateType === 2) {
    weekTypeValue.value = dayTime
  }
  if (rateType === 3) {
    monthTypeValue.value = dayTime
  }
  formState.value.rateType = 1
}

const formatRate2Data = () => {
  const { rateType, startTime, endTime, retryEnable, retryLimit, retryInterval } = formState.value
  if (!rateType) {
    return formState.value
  }
  let dayTime = '01'
  if (timeTypeValue.value === 2) {
    dayTime = `0${weekTypeValue.value}`
  }
  if (timeTypeValue.value === 3) {
    dayTime = monthTypeValue.value < 10 ? `0${monthTypeValue.value}` : monthTypeValue.value.toString()
  }
  const hms = timePicker.value ? new Date(timePicker.value).format("hh:mm:ss") : "00:00:01"
  const tmText = `2021-08-${dayTime} ${hms}`
  return { rateType: timeTypeValue.value, rateVal: tmText, startTime, endTime, retryEnable, retryLimit, retryInterval }
}


const getFormData = async () => {
  const p = new Promise((r, e) => {
    rateForm?.value?.validate(valid => {
      r(valid && formatRate2Data())
    })
  })
  return await p
  
}
defineExpose({
  getFormData
})
onMounted(() => {
  formatRate2Form()
})

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
      color: #1F2329;
      font-weight: 500;
      font-family: var(--de-custom_font, 'PingFang');
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

  .tail {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 400;
  }
}
:deep(.send-time) {
  width: 200px !important;
}

:deep(.ed-form-item__label) {
  line-height: 22px;
  height: 22px;
}
</style>