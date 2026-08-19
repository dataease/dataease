<template>
  <div class="report-form-container">
    <div class="report-form-title-container">
      <span class="title-flag"/>
      <span class="form-title">{{ t('data_fill.task.notification_setting') }}</span>
    </div>
    <el-form
        ref="sendForm"
        class="report-form"
        :model="formState"
        :rules="rateRules"
        label-width="180px"
        label-position="top"
        :scroll-to-error="true"
    >
      <el-form-item
          :label="t('threshold.notification_method')"
          prop="reciFlagList"
      >
        <el-checkbox-group v-model="formState.reciFlagList">
          <el-checkbox
              v-for="item in platformOptions"
              :key="item.value"
              :label="item.value"
              :class="{
              'disabled-platform-option': !platformCategory[item.flag],
            }"
          >
            {{ item.name }}
          </el-checkbox>
        </el-checkbox-group>
      </el-form-item>

      <el-form-item :label="t('threshold.notification_content')" prop="msgType">
        <el-radio-group v-model="formState.msgType" @change="msgTypeChange">
          <el-radio :label="0"> {{ t("threshold.default_msg") }}</el-radio>
          <el-radio :label="1"> {{ t("threshold.custom_msg") }}</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item
          :label="t('threshold.msg_title')"
          prop="msgTitle"
          :rules="[requiredRule, minLengthRule]"
          v-if="formState.msgType"
      >
        <el-input v-model="formState.msgTitle" style="width: 100%"/>
      </el-form-item>
      <el-form-item
          :label="t('threshold.msg_content')"
          prop="msgContent"
          :rules="[requiredRule, minLengthRule]"
          v-if="formState.msgType"
      >
        <de-rich-text
            :fieldList="fieldList"
            v-model="formState.msgContent"
            ref="richRef"
        ></de-rich-text>
      </el-form-item>
      <el-form-item label="">
        <template #label>
          <div
              style="
              display: flex;
              align-items: center;
              justify-content: space-between;
            "
          >
            {{ t("threshold.msg_preview") }}
          </div>
        </template>
        <div class="notification-content">
          <div class="notification-item">
            <el-icon>
              <Icon name="icon_notification_filled"
              >
                <icon_notification_filled class="svg-icon"
                />
              </Icon>
            </el-icon>
            <div class="notification-detail">
              <div class="title">{{ formState.msgTitle }}</div>
              <div
                  class="content"
                  v-html="sanitizeHtml(previewHml || formState.msgContent)"
              />
            </div>
          </div>
        </div>
      </el-form-item>

    </el-form>
  </div>
</template>

<script lang="ts" setup>
import {computed, onMounted, reactive, ref} from 'vue'
import icon_notification_filled from "@/assets/svg/icon_notification_filled.svg";
import {useI18n} from '@/hooks/web/useI18n'
import {PlatformCategory, platformOptions} from "../../../../../component/threshold-warning/form/FormPage";
import {queryCategoryStatusApi} from "../../../../system/report/api";
import {sanitizeHtml} from "@/utils/utils";
import {Icon} from "@/components/icon-custom";
import deRichText from "@/components/rich-text/TinymacEditorAlarm.vue";

const {t} = useI18n()

const props = defineProps({
  sendFormData: {
    type: Object,
    default: () => {
    }
  },
})

const requiredRule = {
  required: true,
  message: t("common.required"),
  trigger: ["blur", "change"],
};
const minLengthRule = (min = 0) => {
  return {
    min: min,
    message: t("data_fill.form.input_limit_min", [min]),
    trigger: ["blur", "change"],
  };
};

const previewHml = ref("");
const richRef = ref();

const platformCategory = ref({
  email: true,
} as PlatformCategory);

const loadPlatformStatus = async () => {
  const res = await queryCategoryStatusApi();
  if (res["data"]) {
    const list: any[] = res["data"] as any[];
    list.forEach((item) => {
      platformCategory.value[item.name] = item.enable;
    });
  }
};


const rateRules = reactive({})


const sendForm = ref(null)

const contentTemplate = computed(() => {
  return '<p><span style="font-family: \'PingFang SC\'; font-size: 14px; background-color: #ffffff;">' + t("data_fill.task.msg_content_1") + ' <span id="changeText-0" style="background: #3370FF33; color: #2b5fd9;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[' + t("data_fill.task.task_name") + ']">[' + t("data_fill.task.task_name") + ']</span></span> <span id="attachValue">&nbsp;</span>, ' + t('data_fill.task.task_end_time') + '： <span id="changeText-1" style="background: #3370FF33; color: #2b5fd9;"><span class="mceNonEditable" contenteditable="false" data-mce-content="[' + t('data_fill.task.task_end_time') + ']">[' + t('data_fill.task.task_end_time') + ']</span></span><span id="attachValue">&nbsp;</span>' + t('data_fill.task.msg_content_2') + '</span></p>'
})

const defaultFormData = ref({
  reciFlagList: [0],
  msgType: 0,
  msgTitle: t("data_fill.task.msg_title"),
  msgContent: contentTemplate.value,
})
const formState = ref({
  ...defaultFormData.value
})
const msgTypeChange = (val) => {
  if (!val) {
    formState.value.msgTitle = t("data_fill.task.msg_title")
    formState.value.msgContent = contentTemplate.value
  }
}


const fieldList = computed(() => {
  const defaultList = [
    {
      id: "0",
      name: t("data_fill.task.task_name"),
      deType: 2,
      groupType: "d",
    },
    {
      id: "1",
      name: t("data_fill.task.task_end_time"),
      deType: 1,
      groupType: "d",
    },
  ];
  return [...defaultList];
});

const formatRate2Form = () => {
  if (!props.sendFormData) {
    return
  }
  for (const key in formState.value) {
    if (props.sendFormData[key]) {
      formState.value[key] = props.sendFormData[key]
    }
  }
}

const formatRate2Data = () => {
  if (!formState.value.reciFlagList.includes(0)) {
    formState.value.reciFlagList.push(0)
  }
  return formState.value;
}

const getFormData = async () => {
  const p = new Promise((r, e) => {
    sendForm?.value?.validate(valid => {
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
  loadPlatformStatus();
})

</script>
<style scoped lang="less">

.report-form-container {
  height: 100%;
  margin: 0 auto;
  width: 680px;

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
    width: 100%;
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

    :deep(.disabled-platform-option) {
      display: none;
    }

    .cron-form-item {
      margin-top: -8px;
    }
  }

  .notification-content {
    width: 848px;
    min-height: 166px;
    height: auto;
    padding: 16px;
    border-radius: 6px;
    background: #f5f6f7;

    .notification-item {
      display: flex;

      .ed-icon {
        font-size: 16px;
        width: 32px;
        height: 32px;
        color: #ffffff;
        background-color: var(--ed-color-primary, #3370ff);
        margin-right: 8px;
        border-radius: 50%;
      }

      .notification-detail {
        width: 303px;
        min-height: 134px;
        height: auto;
        padding: 16px;
        border-radius: 6px;
        border: 1px solid #dee0e3;
        background-color: #ffffff;

        .title {
          font-size: 16px;
          font-weight: 500;
          line-height: 24px;
        }

        .content {
          font-family: var(--de-custom_font, "PingFang");
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          word-break: break-word;
        }
      }
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
  border-radius: 6px;
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

  .prefix {
    font-family: var(--de-custom_font, 'PingFang');
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

.custom-option {
  font-size: 14px;
  display: flex;
  align-items: center;
}

.view-type-icon {
  color: var(--ed-color-primary);
  width: 22px;
  height: 16px;
}

.threshold-form-container {
  height: 100%;
  margin: 0 auto;

  .threshold-form-title-container {
    display: flex;
    align-items: center;
    height: 24px;
    line-height: 24px;
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

  .threshold-form {
    width: 100%;
    padding-bottom: 16px;

    .ed-form-item {
      margin-bottom: 16px;
    }

    .is-error {
      margin-bottom: 40px !important;
    }


  }

  :deep(.ed-form-item__label) {
    line-height: 22px;
    height: 22px;
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

.reci-custom-email-select {
  width: 100%;

  :deep(.ed-input__suffix) {
    display: none;
  }

  :deep(.ed-select-tags-wrapper) {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }

  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}

.ed-select__tags {
  .ed-select-tags-wrapper {
    display: flex;
    flex-wrap: wrap;
    grid-row-gap: 4px;
  }

  :deep(.ed-tag) {
    margin: 0px 4px 0 0;
  }
}

.custom-clear-class {
  :deep(.ed-input__suffix-inner) {
    column-gap: 4px;

    i:first-child {
      &:hover {
        color: var(--ed-color-primary, #3370ff);
      }
    }
  }
}

</style>

<style lang="less">
.df-task-top-time-select {
  margin-top: -30px !important;
}
</style>
