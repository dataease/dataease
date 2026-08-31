<script lang="ts" setup>
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import { computed, defineEmits, nextTick, ref } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import BaseForm from './BaseForm.vue'
import RecipientForm from './RecipientForm.vue'
import FrequencyForm from './FrequencyForm.vue'
import { reportInfoApi, reportCreateApi, reportUpdateApi } from '../api'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const props = defineProps({
  taskId: {
    type: String,
    default: ''
  }
})

const reportInfo = ref({})
const formLoading = ref(false)
const activeStep = ref(0)
const reportFormVisible = ref<boolean>(false)

const baseForm = ref(null)
const baseFormData = ref({})

const recipientForm = ref(null)
const recipientFormData = ref({})

const frequencyForm = ref(null)
const frequencyFormData = ref({})

const isEdit = computed<boolean>(() => {
  if (props.taskId && props.taskId !== '') {
    return true
  }
  return false
})

const currentResourceId = computed(() => baseFormData.value?.rid || reportInfo.value?.['rid'] || '')
const currentResourceFlag = computed(() => {
  const rtid = baseFormData.value?.rtid ?? reportInfo.value?.['rtid']
  return rtid === 1 ? 2 : 1 // 0=dashboard→1(PANEL), 1=dataV→2(SCREEN)
})

const emit = defineEmits(['taskAddVisibleClose', 'refreshList'])

const prev = () => {
  activeStep.value = activeStep.value - 1
}

const next = async () => {
  formLoading.value = true
  if (activeStep.value === 0) {
    const baseData = await baseForm?.value?.getFormData()
    if (baseData) {
      baseFormData.value = Object.assign(baseFormData.value, baseData)
      activeStep.value = activeStep.value + 1
      nextTick(() => {
        recipientForm?.value?.setOwnSelectHeight()
        recipientForm?.value?.setDataPermission(baseData?.dataPermission)
      })
    }
    formLoading.value = false
  } else if (activeStep.value === 1) {
    const recipientData = await recipientForm?.value?.getFormData()
    if (recipientData) {
      recipientFormData.value = Object.assign(recipientFormData.value, recipientData)
      activeStep.value = activeStep.value + 1
    }
    formLoading.value = false
  } else if (activeStep.value === 2) {
    const frequencyData = await frequencyForm?.value?.getFormData()
    if (frequencyData) {
      frequencyFormData.value = Object.assign(frequencyFormData.value, frequencyData)
      saveHandler()
    }
  }
}

const saveHandler = () => {
  const param = {
    ...baseFormData.value,
    ...recipientFormData.value,
    ...frequencyFormData.value
  }
  if (isEdit.value) {
    param['taskId'] = reportInfo.value['taskId']
  }
  const method = isEdit.value ? reportUpdateApi : reportCreateApi
  method(param)
    .then(res => {
      if (!res?.code) {
        ElMessage.success(t('common.save_success'))
        emit('refreshList')
        cancelClick()
      }
    })
    .finally(() => {
      formLoading.value = false
    })
}

const cancelClick = () => {
  activeStep.value = 0
  formLoading.value = false
  baseFormData.value = {}
  recipientFormData.value = {}
  frequencyFormData.value = {}
  reportInfo.value = {}
  emit('taskAddVisibleClose')
}

const edit = (taskId: string) => {
  if (isEdit.value || taskId) {
    reportInfoApi(props.taskId || taskId).then(res => {
      reportInfo.value = res.data
      reportFormVisible.value = true
    })
  }
}

defineExpose({ reportFormVisible, edit })
</script>
<template>
  <el-drawer
    v-model="reportFormVisible"
    :close-on-click-modal="false"
    modal-class="report-form-drawer-fullscreen"
    direction="btt"
    :show-close="false"
    :before-close="cancelClick"
    :z-index="11"
  >
    <template #header>
      <span class="head-title">{{
        isEdit ? t('dataset.task_edit_title') : t('dataset.task_add_title')
      }}</span>
      <div class="flex-center" style="width: 100%">
        <el-steps custom style="max-width: 550px; flex: 1" :active="activeStep" align-center>
          <el-step>
            <template #title>
              {{ t('datasource.base_info') }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t('threshold.recipient') }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t('report.send_setting') }}
            </template>
          </el-step>
        </el-steps>
      </div>
      <el-icon @click="cancelClick" class="report-close">
        <Icon name="icon_close_outlined"><icon_close_outlined class="svg-icon" /></Icon>
      </el-icon>
    </template>
    <div class="task-form-container">
      <div class="task-form-class" v-loading="formLoading" v-if="reportFormVisible">
        <base-form
          ref="baseForm"
          :is-edit="isEdit"
          :base-form-data="reportInfo"
          v-show="activeStep === 0"
        />
        <recipient-form
          ref="recipientForm"
          :is-edit="isEdit"
          :reci-form-data="reportInfo"
          :resource-id="currentResourceId"
          :resource-flag="currentResourceFlag"
          v-show="activeStep === 1"
        />
        <FrequencyForm
          ref="frequencyForm"
          :is-edit="isEdit"
          :rate-form-data="reportInfo"
          v-show="activeStep === 2"
        />
      </div>
    </div>
    <div class="task-form-footer">
      <div class="editor-footer" style="flex: auto">
        <el-button @click="cancelClick">{{ t('common.cancel') }}</el-button>
        <el-button :loading="formLoading" v-show="activeStep > 0" secondary @click="prev">
          {{ t('common.prev') }}
        </el-button>
        <el-button
          :loading="formLoading"
          v-show="activeStep === 0 || activeStep < 2"
          type="primary"
          @click="next()"
        >
          {{ t('common.next') }}
        </el-button>
        <el-button :loading="formLoading" v-show="activeStep >= 2" type="primary" @click="next()">
          {{ t('chart.confirm') }}
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>
<style lang="less">
body:has(.report-form-drawer-fullscreen:not([style*='display: none'])) {
  height: 100vh !important;
}

.report-form-drawer-fullscreen {
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
            display: inline-flex;
            align-items: center;
            justify-content: center;
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
      .task-form-container {
        height: 100%;
        .task-form-class {
          height: calc(100% - 64px);
          width: 100%;
          display: flex;
          align-items: center;
          overflow-y: auto;
        }
      }
      .task-form-footer {
        position: absolute;
        width: 100%;
        height: 64px;
        bottom: 0px;
        left: 0;
        .editor-footer {
          height: 64px;
          display: flex;
          align-items: center;
          justify-content: flex-end;
          padding-right: 24px;
          border-top: 1px solid rgba(31, 35, 41, 0.15);
        }
      }
    }
  }
}
</style>
