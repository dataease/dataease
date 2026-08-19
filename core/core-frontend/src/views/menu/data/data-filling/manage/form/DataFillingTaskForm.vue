<script setup lang="ts">
import icon_close_outlined from "@/assets/svg/icon_close_outlined.svg";
import {ElIcon, ElMessage} from "element-plus-secondary";
import {Icon} from "@/components/icon-custom";
import {computed, ref} from "vue";
import {useI18n} from "@/hooks/web/useI18n";
import BaseRecipientForm from "./BaseRecipientForm.vue";
import FrequencyForm from "./FrequencyForm.vue";
import SendForm from "./SendForm.vue";
import {getTaskInfo, saveTask} from "../../data-filling";

const emit = defineEmits(["finish"]);

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

const taskId = ref<string | undefined>(undefined);

const {t} = useI18n();

const reportInfo = ref({});

const baseFormData = ref({});
const frequencyFormData = ref({});
const sendFormData = ref({});

const show = ref(false);
const isEdit = computed<boolean>(() => {
  return taskId.value !== undefined;
});

const activeStep = ref<0 | 1 | 2>(0);
const formLoading = ref(false);

const baseFormRef = ref();
const frequencyFormRef = ref();
const sendFormRef = ref();

const prev = () => {
  switch (activeStep.value) {
    case 2:
      activeStep.value = 1
      break
    default:
      activeStep.value = 0
  }
};
const next = async () => {
  formLoading.value = true;
  if (activeStep.value === 0) {
    const baseData = await baseFormRef?.value?.getFormData();
    if (baseData) {
      baseFormData.value = Object.assign(baseFormData.value, baseData);
      activeStep.value = 1;
    }
    formLoading.value = false;
  } else if (activeStep.value === 1) {
    const frequencyData = await frequencyFormRef?.value?.getFormData();
    if (frequencyData) {
      frequencyFormData.value = Object.assign(
          frequencyFormData.value,
          frequencyData
      );
      activeStep.value = 2;
    }
    formLoading.value = false;
  } else {
    const sendData = await sendFormRef?.value?.getFormData();
    if (sendData) {
      sendFormData.value = Object.assign(
          sendFormData.value,
          sendData
      );
      await saveHandler();
    } else {
      formLoading.value = false;
    }
  }
};

const saveHandler = async () => {
  formLoading.value = true;
  const param = {
    ...baseFormData.value,
    ...frequencyFormData.value,
    ...sendFormData.value,
    formId: props.formId,
  };
  if (isEdit.value) {
    param["id"] = taskId.value;
  }
  saveTask(param)
      .then((res) => {
        if (res.data) {
          ElMessage.success(t("common.save_success"));

          cancelClick();
          emit("finish");
        }
      })
      .finally(() => {
        formLoading.value = false;
      });
};

const cancelClick = () => {
  activeStep.value = 0;
  formLoading.value = false;
  show.value = false;
  reportInfo.value = {};
};

const open = (id?: string) => {
  formLoading.value = true;
  taskId.value = id;
  if (id) {
    // 查询信息
    getTaskInfo(taskId.value)
        .then((res) => {
          reportInfo.value = res.data;
          show.value = true;
        })
        .finally(() => {
          formLoading.value = false;
        });
  } else {
    formLoading.value = false;
    show.value = true;
  }
};

defineExpose({open});
</script>

<template>
  <el-drawer
      v-model="show"
      :close-on-click-modal="false"
      modal-class="df-form-drawer-fullscreen"
      size="calc(100% - 64px)"
      direction="btt"
      :show-close="false"
      :z-index="11"
  >
    <template #header>
      <span class="head-title">{{
          isEdit ? t("data_fill.task.edit_task") : t("data_fill.task.create_task")
        }}</span>
      <div class="flex-center" style="width: 100%; margin-right: 50px">
        <el-steps
            style="max-width: 504px; flex: 1"
            :active="activeStep"
            align-center
            custom
        >
          <el-step>
            <template #title>
              {{ t("datasource.base_info") }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t("data_fill.task.task_distribute_setting") }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t("data_fill.task.notification_setting") }}
            </template>
          </el-step>
        </el-steps>
      </div>
      <el-icon @click="cancelClick" class="report-close">
        <Icon name="icon_close_outlined">
          <icon_close_outlined class="svg-icon"/>
        </Icon>
      </el-icon>
    </template>
    <el-main v-loading="formLoading">
      <template v-if="show">
        <BaseRecipientForm
            ref="baseFormRef"
            :is-edit="isEdit"
            :form-name="formName"
            :forms="forms"
            :columns="columns"
            :reci-form-data="reportInfo"
            :form-id="formId"
            v-show="activeStep === 0"
        />
        <FrequencyForm
            ref="frequencyFormRef"
            :is-edit="isEdit"
            :rate-form-data="reportInfo"
            v-show="activeStep === 1"
        />
        <SendForm
            ref="sendFormRef"
            :send-form-data="reportInfo"
            v-show="activeStep === 2"
        />
      </template>
    </el-main>
    <template #footer>
      <div class="editor-footer" style="flex: auto">
        <el-button @click="cancelClick">{{ t("common.cancel") }}</el-button>
        <el-button
            :disabled="formLoading"
            v-show="activeStep > 0"
            secondary
            @click="prev"
        >
          {{ t("common.prev") }}
        </el-button>
        <el-button
            :disabled="formLoading"
            element-loading-spinner=""
            v-show="activeStep < 2"
            type="primary"
            @click="next()"
        >
          {{ t("common.next") }}
        </el-button>
        <el-button
            :disabled="formLoading"
            v-show="activeStep >= 2"
            type="primary"
            @click="next()"
        >
          {{ t("commons.confirm") }}
        </el-button>
      </div>
    </template>
  </el-drawer>
</template>

<style lang="less">
.df-form-drawer-fullscreen {
  .ed-drawer {
    height: calc(100% - 100px) !important;

    .ed-drawer__header {
      border-color: rgba(31, 35, 41, 0.15);
      justify-content: space-between;

      .head-title {
        flex: none;
        width: auto;
      }

      .report-close {
        cursor: pointer;
      }

      .editor-step {
        margin-right: 50px;
        position: relative;

        .ed-step.is-center .ed-step__line {
          width: 80px;
          right: 40px;
          z-index: 5;
          left: calc(100% - 40px);
        }

        .ed-step__icon.is-icon {
          width: auto;
          position: relative;
          z-index: 0;
        }

        .ed-step__head {
          line-height: 0;
        }

        .ed-step__head.is-finish::after {
          right: calc(100% - 66px);
          top: 44%;
        }

        .ed-step__head.is-process .ed-step__icon {
          background-color: transparent;

          .step-icon {
            .icon {
              background: var(--ed-color-primary);
            }

            .title {
              color: #1f2329;
              font-weight: 500 !important;
            }
          }
        }

        .ed-step__head.is-finish .ed-step__icon {
          background-color: transparent;

          .step-icon {
            .icon {
              border: 1px solid var(--ed-color-primary);
            }
          }
        }

        .ed-step__head.is-wait .ed-step__icon {
          background-color: transparent;

          .step-icon {
            .icon {
              color: #8f959e;
              border: 1px solid #8f959e;
            }

            .title {
              color: #8f959e;
            }
          }
        }

        .step-icon {
          display: flex;
          padding: 0 48px;
          align-items: center;

          .icon {
            width: 28px;
            height: 28px;
            line-height: 27px;
            border-radius: 50%;
          }

          .title {
            margin-left: 8px;
            color: #1f2329;
            font-size: 14px;
            font-weight: 400;
            line-height: 22px;
          }
        }
      }
    }

    .ed-drawer__body {
      padding: 0 24px;

      .ed-main {
        padding: 0;
      }
    }

    .ed-drawer__footer {
      height: 64px;
      padding: 0 24px;
      display: flex;
      flex-direction: row;
      align-items: center;
      position: unset;
    }

    .editor-footer {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      padding-right: 24px;
    }
  }
}
</style>
