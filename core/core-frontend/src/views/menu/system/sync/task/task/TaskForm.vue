<script lang="ts" setup>
import icon_close_outlined from '@/assets/svg/icon_close_outlined.svg'
import { computed, defineEmits, reactive, ref, watch } from 'vue'
import {
  addApi,
  findTaskInfoByIdApi,
  ISource,
  ITarget,
  ITargetProperty,
  ITaskInfoInsertReq,
  ITaskInfoRes,
  modifyApi
} from '@/api/sync/syncTask'
import type { FormInstance } from 'element-plus-secondary'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import SourceForm from './SourceForm.vue'
import TargetForm from './TargetForm.vue'
import cron from 'cron-validate'
import { Icon } from '@/components/icon-custom'
import dayjs from 'dayjs'
import { deepCopy } from '@/utils/utils'
import { loadSyncPlugin, validateByIdApi } from '@/api/sync/syncDatasource'
import { useI18n } from '@/hooks/web/useI18n'
import { dsTypes } from '../../ds/form/option'

const { t } = useI18n()
const props = defineProps({
  taskId: {
    type: String,
    default: ''
  }
})
const formLoading = ref(false)
const activeStep = ref(0)
const taskFormVisible = ref<boolean>(false)
/**
 * 编辑状态
 */
const isEdit = computed(() => !!props.taskId)
const checkingTarget = ref(false)
let taskRequestSequence = 0
const targetCheckTitle = computed(() => {
  if (formState.value.targetDatasourceMissing) return t('sync_task.target_connection_missing')
  if (formState.value.targetTableCheckStatus === 'TABLE_MISSING')
    return t('sync_task.target_table_missing')
  return t('sync_task.target_table_check_failed')
})

const getPeriod = value => {
  if (value.endsWith('m')) {
    return { unit: 'm', interval: parseInt(value.substring(0)) }
  } else if (value.endsWith('h')) {
    return { unit: 'h', interval: parseInt(value.substring(0)) }
  } else if (value.endsWith('d')) {
    return { unit: 'd', interval: parseInt(value.substring(0)) }
  } else {
    return { unit: 'm', interval: 0 }
  }
}

/**
 * 获取任务详情
 */
const getTaskInfo = (recheck = false) => {
  const taskId = props.taskId
  const sequence = ++taskRequestSequence
  if (recheck) checkingTarget.value = true
  else {
    formLoading.value = true
    formState.value = deepCopy(defaultFormState)
    activeStep.value = 0
  }
  findTaskInfoByIdApi(taskId)
    .then(res => {
      // 切换任务或关闭窗口后丢弃旧响应，重新检查只合并状态和名称，保留当前编辑内容
      if (sequence !== taskRequestSequence || taskId !== props.taskId) return
      if (recheck) {
        const {
          editable,
          editableCheckFailed,
          sourceDatasourceMissing,
          targetDatasourceMissing,
          targetTableCheckStatus,
          targetTableCheckError
        } = res.data
        Object.assign(formState.value, {
          editable,
          editableCheckFailed,
          sourceDatasourceMissing,
          targetDatasourceMissing,
          targetTableCheckStatus,
          targetTableCheckError
        })
        for (const side of ['source', 'target'] as const) {
          const name = res.data[side].datasource?.name
          if (formState.value[side].datasource) formState.value[side].datasource.name = name
          const selected = formState.value[side].dsList?.find(
            item => item.id === formState.value[side].datasourceId
          )
          if (selected) selected.name = name
        }
        return
      }
      formState.value = {
        ...deepCopy(defaultFormState),
        ...res.data
      }
      if (formState.value.stopTimeString != null) {
        formState.value.stopTimeString = dayjs(formState.value.stopTimeString).format(
          'YYYY-MM-DD HH:mm'
        )
      }
      if (formState.value.startTimeString != null) {
        formState.value.startTimeString = dayjs(formState.value.startTimeString).format(
          'YYYY-MM-DD HH:mm'
        )
      }
      formState.value.target.property = JSON.parse(formState.value.target.targetProperty)
      if (formState.value.schedulerType === 'FIX_RATE' && formState.value.schedulerConf) {
        formState.value.schedulerOption = getPeriod(formState.value.schedulerConf)
      }
    })
    .catch(() => {
      // 请求层已展示错误信息，初次加载失败保持空白只读，不能继续保存上一任务的数据
      if (!recheck && sequence === taskRequestSequence) {
        formState.value = deepCopy(defaultFormState)
      }
    })
    .finally(() => {
      if (sequence !== taskRequestSequence) return
      checkingTarget.value = false
      formLoading.value = false
    })
}

const emit = defineEmits(['taskAddVisibleClose', 'refreshList'])
/**
 * 任务表单对象
 */
const taskFormRef = ref<FormInstance>()
const sourceConfigRef = ref()
const targetConfigRef = ref()
const defaultFormState = {
  id: '',
  name: '',
  schedulerType: 'NONE',
  schedulerConf: '',
  taskKey: '',
  desc: '',
  executorTimeout: undefined,
  executorFailRetryCount: undefined,
  source: {} as ISource,
  target: {
    property: {} as ITargetProperty
  } as ITarget,
  status: false,
  editableCheckFailed: false,
  editable: undefined as boolean | undefined,
  sourceDatasourceMissing: false,
  targetDatasourceMissing: false,
  targetTableCheckStatus: undefined as ITaskInfoRes['targetTableCheckStatus'],
  targetTableCheckError: '',
  startTimeString: '', //dayjs(new Date()).format('YYYY-MM-DD HH:mm'),
  stopTimeString: '',
  schedulerOption: {
    interval: 30,
    unit: 'h'
  }
}
/**
 * 表单数据
 */
const formState = ref({
  ...defaultFormState
})
watch([taskFormVisible, () => props.taskId], ([visible]) => {
  if (visible && props.taskId) getTaskInfo()
  else {
    taskRequestSequence++
    checkingTarget.value = false
  }
})
/**
 * 存储数据库类型
 */
const dsTypeListData = ref([])

const getTypeList = () => {
  dsTypeListData.value = deepCopy(dsTypes)
}
getTypeList()
const listSyncPlugin = () => {
  loadSyncPlugin().then(res => {
    res.data?.forEach(item => {
      const { name, category, type, icon, extraParams, staticMap, datasourceRole } = item
      const node = {
        name,
        catalog: category,
        type,
        icon,
        extraParams,
        isPlugin: true,
        staticMap,
        datasourceRole
      }
      dsTypeListData.value.push(node)
    })
  })
}
listSyncPlugin()
const options = [
  { value: 'm', label: t('sync_task.minute') },
  { value: 'h', label: t('sync_task.hour') }
]

/**
 * 表单验证
 */
const rules = reactive({
  name: [
    {
      required: true,
      message: t('sync_task.please_enter_task_name'),
      trigger: 'change'
    },
    {
      max: 255,
      message: t('sync_task.input_limit_255'),
      trigger: 'change'
    }
  ],
  desc: [
    {
      max: 255,
      message: t('sync_task.input_limit_255'),
      trigger: 'change'
    }
  ],
  schedulerConf: [
    { required: true, message: t('sync_task.please_enter'), trigger: 'blur' },
    {
      validator: (rule: any, value: any, callback: any) => {
        if (formState.value.schedulerType === 'CRON') {
          const cronResult = cron(value, {
            preset: 'npm-cron-schedule',
            override: {
              useSeconds: true,
              useBlankDay: true,
              useLastDayOfWeek: true,
              useLastDayOfMonth: true,
              useNearestWeekday: true,
              useNthWeekdayOfMonth: true
            }
          })
          if (!cronResult.isValid()) {
            console.log(cronResult.getError())
            callback(new Error(t('sync_task.please_cron') + cronResult.getError()))
            return false
          }
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  schedulerType: [{ required: true, message: t('sync_task.please_choose'), trigger: 'blur' }],
  schedulerOption: {
    interval: [{ required: true, message: t('sync_task.please_enter'), trigger: 'blur' }]
  },
  startTimeString: [
    {
      required: true,
      message: t('sync_task.please_choose_start_time'),
      trigger: 'change'
    },
    {
      required: true,
      validator: (rule: any, value: any, callback: any) => {
        if (!value) {
          callback(new Error(t('sync_task.please_choose_start_time')))
        } else {
          if (formState.value.stopTimeString) {
            taskFormRef.value?.validateField('stopTime')
          }
          callback()
        }
      },
      trigger: 'change'
    }
  ],
  stopTime: [
    {
      validator: (rule: any, value: any, callback: any) => {
        if (value !== '') {
          if (Date.parse(formState.value.startTimeString) >= Date.parse(value)) {
            callback(new Error(t('sync_task.end_time_must_be_later_than_start_time')))
          } else {
            callback()
          }
        }
        callback()
      },
      trigger: 'change'
    }
  ],
  source: {
    type: [
      {
        required: true,
        message: t('sync_task.please_choose_database_type'),
        trigger: 'blur'
      }
    ],
    datasourceId: [
      {
        required: true,
        message: t('sync_task.please_choose_database'),
        trigger: 'blur'
      }
    ],
    tables: [
      {
        required: true,
        message: t('sync_task.please_choose_table'),
        trigger: 'blur'
      }
    ],
    query: [
      {
        required: true,
        message: t('sync_task.please_enter_sql'),
        trigger: 'blur'
      }
    ],
    incrementField: [
      {
        required: true,
        message: t('sync_task.please_choose_incremental_field'),
        trigger: 'blur'
      }
    ],
    esQuery: [
      {
        validator: (rule: any, value: any, callback: any) => {
          if (value) {
            try {
              JSON.parse(value)
            } catch (e) {
              callback(new Error(t('sync_task.es_query_param_formatter_error')))
            }
          }
          callback()
        },
        trigger: 'change'
      }
    ]
  },
  target: {
    type: [
      {
        required: true,
        message: t('sync_task.please_choose_database_type'),
        trigger: 'blur'
      }
    ],
    datasourceId: [
      {
        required: true,
        message: t('sync_task.please_choose_database'),
        trigger: 'blur'
      }
    ],
    tableName: [
      {
        required: true,
        message: t('sync_task.please_enter_table_name'),
        trigger: 'change'
      },
      {
        max: 64,
        message: t('sync_task.input_limit_64'),
        trigger: 'change'
      },
      {
        validator: (rule: any, value: any, callback: any) => {
          const regex = new RegExp(/^[a-zA-Z][a-zA-Z0-9_]*$/)
          if (!regex.test(value)) {
            callback(new Error(t('sync_task.must_be_met_the_table_name')))
            return false
          }
          callback()
        },
        trigger: 'change'
      }
    ],
    incrementField: [
      {
        required: true,
        message: t('sync_task.please_choose_incremental_field'),
        trigger: 'blur'
      }
    ],
    remarks: [
      {
        max: 255,
        message: t('sync_task.input_limit_255'),
        trigger: 'change'
      }
    ]
  }
})

const onSubmit = () => {
  if (
    isEdit.value &&
    (formState.value.id !== props.taskId ||
      formState.value.editableCheckFailed ||
      formState.value.sourceDatasourceMissing ||
      checkingTarget.value)
  )
    return
  formLoading.value = true
  const id = props.taskId
  const sequence = taskRequestSequence
  // 保存跨越连接校验和提交两个请求，关闭或切换任务后不能继续提交或重置新表单
  const isCurrentRequest = () => sequence === taskRequestSequence && id === props.taskId
  const requestData: ITaskInfoInsertReq = {
    id: id,
    name: formState.value.name,
    schedulerType: formState.value.schedulerType,
    schedulerConf: formState.value.schedulerConf,
    desc: formState.value.desc,
    executorTimeout: formState.value.executorTimeout,
    executorFailRetryCount: formState.value.executorFailRetryCount,
    startTimeString: formState.value.startTimeString,
    stopTimeString: formState.value.stopTimeString,
    source: deepCopy(formState.value.source),
    target: deepCopy(formState.value.target),
    editable: formState.value.editable
  }
  if (requestData.schedulerType === 'FIX_RATE') {
    requestData.schedulerConf =
      formState.value.schedulerOption.interval + formState.value.schedulerOption.unit
  }
  if (requestData.target.datasourceId) {
    validateByIdApi(requestData.target.datasourceId)
      .then(data => {
        if (!isCurrentRequest()) return
        if (data.data?.status !== 'Success') {
          ElMessage.error(t('sync_task.target_database_status_is_abnormal'))
          return
        }
        const submitRequest = id ? modifyApi(requestData) : addApi(requestData)
        return submitRequest.then(() => {
          emit('refreshList')
          if (!isCurrentRequest()) return
          ElMessage.success(t(id ? 'sync_task.edit_success' : 'sync_task.add_success'))
          cancelClick()
        })
      })
      .catch(() => {
        // 请求错误提示由公共请求层统一处理
      })
      .finally(() => {
        if (!isCurrentRequest()) return
        // 数据源校验和任务提交任一环节结束后都必须关闭 loading
        formLoading.value = false
        targetConfigRef.value?.closeLoading()
      })
  } else {
    formLoading.value = false
    targetConfigRef.value?.closeLoading()
  }
}

const prev = () => {
  activeStep.value = activeStep.value - 1
}

const setNextStep = () => {
  formLoading.value = false
  activeStep.value = activeStep.value + 1
}

const next = (btn: string) => {
  const sequence = taskRequestSequence
  formLoading.value = true
  if (activeStep.value === 0) {
    taskFormRef.value?.validate((valid: boolean) => {
      if (sequence !== taskRequestSequence) return
      if (valid) {
        if (btn === 'save') {
          onSubmit()
        } else {
          setNextStep()
        }
      } else {
        formLoading.value = false
      }
    })
  }
  if (activeStep.value === 1) {
    taskFormRef.value?.validate((valid: boolean) => {
      if (sequence !== taskRequestSequence) return
      if (valid) {
        if (btn === 'save') {
          onSubmit()
        } else {
          formLoading.value = true
          setNextStep()
        }
      } else {
        formLoading.value = false
      }
    })
  }
  if (activeStep.value === 2) {
    taskFormRef.value?.validate(async (valid: boolean) => {
      if (sequence !== taskRequestSequence) return
      // 父表单统一管理加载状态，不向旧组件的异步校验传递共享引用
      const targetValid = await targetConfigRef.value?.validate()
      if (sequence !== taskRequestSequence) return
      if (valid && targetValid) {
        onSubmit()
      } else {
        formLoading.value = false
      }
    })
  }
}

const cancelClick = () => {
  taskRequestSequence++
  checkingTarget.value = false
  taskFormRef.value?.resetFields()
  formState.value = deepCopy(defaultFormState)
  formState.value.source = {}
  formState.value.target = { property: {} }
  activeStep.value = 0
  formLoading.value = false
  emit('taskAddVisibleClose')
}
const changeLoading = (loading: boolean) => {
  if (loading) {
    formLoading.value = true
  } else {
    formLoading.value = false
  }
}
defineExpose({ taskFormVisible })
</script>
<template>
  <el-drawer
    v-model="taskFormVisible"
    :close-on-click-modal="false"
    size="calc(100% - 64px)"
    modal-class="task-form-drawer-fullscreen"
    direction="btt"
    :show-close="false"
    :z-index="2000"
  >
    <template #header>
      <span>{{ isEdit ? t('sync_task.edit_task') : t('sync_task.add_task') }}</span>
      <div class="flex-center" style="width: 100%; margin-right: 50px">
        <el-steps style="max-width: 550px; flex: 1" :active="activeStep" align-center custom>
          <el-step>
            <template #title>
              {{ t('sync_task.basic_information') }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t('sync_task.source_database') }}
            </template>
          </el-step>
          <el-step>
            <template #title>
              {{ t('sync_task.target_database') }}
            </template>
          </el-step>
        </el-steps>
      </div>
      <el-icon @click="cancelClick" class="datasource-close">
        <Icon name="icon_close_outlined">
          <icon_close_outlined class="svg-icon" />
        </Icon>
      </el-icon>
    </template>
    <div class="task-form-container">
      <div class="task-form-class">
        <div
          v-if="isEdit && (formState.editableCheckFailed || formState.sourceDatasourceMissing)"
          class="target-check-notice"
        >
          <el-alert type="warning" :closable="false" show-icon>
            <template #title>{{
              formState.editableCheckFailed
                ? targetCheckTitle
                : t('sync_task.source_connection_missing')
            }}</template>
            <div class="target-check-context">
              {{
                t('sync_task.original_target', [
                  formState.target.datasource?.name ||
                    formState.target.datasourceId ||
                    t('sync_task.connection_not_recorded'),
                  formState.target.tableName
                ])
              }}
            </div>
            <div>{{ t('sync_task.target_check_readonly') }}</div>
            <details v-if="formState.targetTableCheckError" class="target-check-details">
              <summary>{{ t('sync_task.check_error_details') }}</summary>
              <pre>{{ formState.targetTableCheckError }}</pre>
            </details>
          </el-alert>
          <el-button
            :loading="checkingTarget"
            :disabled="formLoading || (isEdit && formState.id !== props.taskId)"
            @click="getTaskInfo(true)"
            >{{ t('sync_task.recheck_target') }}</el-button
          >
        </div>
        <el-form
          ref="taskFormRef"
          :model="formState"
          :rules="rules"
          label-width="180px"
          label-position="top"
          :disabled="
            isEdit &&
            activeStep != 0 &&
            (formState.editable === false || formState.sourceDatasourceMissing || checkingTarget)
          "
          v-loading="formLoading"
          :scroll-to-error="true"
        >
          <!--基本信息开始-->
          <div class="base-info" v-if="activeStep === 0">
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <el-form-item :label="t('sync_task.name')" prop="name">
                  <el-input
                    v-model="formState.name"
                    :placeholder="t('sync_task.please_enter_task_name')"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <el-form-item :label="t('sync_task.desc')" prop="desc">
                  <el-input v-model="formState.desc" :placeholder="t('sync_task.please_enter')" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <el-form-item :label="t('sync_task.task_time_out_time')" prop="executorTimeout">
                  <el-input-number
                    v-model="formState.executorTimeout"
                    :placeholder="t('sync_task.effective_if_greater_than_0')"
                    controls-position="right"
                    autocomplete="off"
                    type="number"
                    @mousewheel.prevent
                    :min="0"
                    :max="86400"
                    :precision="0"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_task.retry_attempts_on_failure')"
                  prop="executorFailRetryCount"
                >
                  <el-input-number
                    v-model="formState.executorFailRetryCount"
                    :placeholder="t('sync_task.retry_attempts_on_failure')"
                    controls-position="right"
                    autocomplete="off"
                    type="number"
                    @mousewheel.prevent
                    :min="0"
                    :max="10"
                    :precision="0"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <el-form-item
                  :label="t('sync_task.sync_frequency')"
                  prop="schedulerType"
                  style="margin-bottom: 8px"
                >
                  <el-radio-group v-model="formState.schedulerType" size="small">
                    <el-radio label="NONE">{{ t('sync_task.sync_immediately') }} </el-radio>
                    <el-radio label="CRON">{{ t('sync_task.sync_cron') }} </el-radio>
                    <el-radio label="FIX_RATE">{{ t('sync_task.sync_fixed_frequency') }} </el-radio>
                  </el-radio-group>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24" :justify="'center'">
              <el-col :span="12">
                <div class="scheduler" v-if="formState.schedulerType != 'NONE'">
                  <div v-if="formState.schedulerType === 'CRON'">
                    <el-form-item prop="schedulerConf">
                      <el-input
                        v-model="formState.schedulerConf"
                        :placeholder="t('sync_task.cron_expression')"
                      />
                    </el-form-item>
                  </div>
                  <div v-if="formState.schedulerType === 'FIX_RATE'">
                    <el-form-item prop="schedulerOption.interval">
                      <div class="ds-task-form-inline">
                        <span>{{ t('sync_task.each') }}</span>
                        <el-input-number
                          v-model="formState.schedulerOption.interval"
                          step-strictly
                          class="text-left"
                          :min="1"
                          :max="formState.schedulerOption.unit === 'm' ? 59 : 23"
                          controls-position="right"
                          type="number"
                        />
                        <el-select v-model="formState.schedulerOption.unit">
                          <el-option
                            v-for="item in options"
                            :key="item.value"
                            :label="item.label"
                            :value="item.value"
                          />
                        </el-select>
                        <span class="ds-span">{{ t('sync_task.sync_once') }}</span>
                      </div>
                    </el-form-item>
                  </div>
                  <div class="date-picker-box">
                    <el-form-item :label="t('sync_datasource.start_time')" prop="startTimeString">
                      <el-date-picker
                        v-model="formState.startTimeString"
                        type="datetime"
                        :placeholder="t('sync_datasource.start_time')"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm"
                        key="start-time-filt"
                        size="default"
                      />
                    </el-form-item>
                    <el-form-item :label="t('sync_datasource.end_time')" prop="stopTime">
                      <el-date-picker
                        v-model="formState.stopTime"
                        type="datetime"
                        :placeholder="t('sync_datasource.end_time')"
                        format="YYYY-MM-DD HH:mm"
                        value-format="YYYY-MM-DD HH:mm"
                        key="stop-time-filt"
                        size="default"
                      />
                    </el-form-item>
                  </div>
                </div>
              </el-col>
            </el-row>
          </div>
          <!--基本信息结束-->
          <div class="base-info">
            <!--source 配置开始-->
            <source-form
              :key="`${formState.editable}:${formState.sourceDatasourceMissing}`"
              ref="sourceConfigRef"
              :model-value="formState"
              :ds-type-list-data="dsTypeListData.filter(vo => vo.datasourceRole === 1)"
              :is-edit="isEdit"
              v-if="activeStep === 1"
              @change-loading="changeLoading"
            />
            <!--source 配置结束-->
            <!--target 配置开始-->
            <target-form
              :key="`${formState.editable}:${formState.sourceDatasourceMissing}`"
              ref="targetConfigRef"
              :model-value="formState"
              :ds-type-list-data="dsTypeListData.filter(vo => vo.datasourceRole === 2)"
              :is-edit="isEdit"
              v-if="activeStep >= 2"
              @change-loading="changeLoading"
            />
            <!--target 配置结束-->
          </div>
        </el-form>
      </div>
    </div>
    <div class="task-form-footer">
      <div class="editor-footer" style="flex: auto">
        <el-button @click="cancelClick">{{ t('sync_datasource.cancel') }} </el-button>
        <el-button
          :disabled="formLoading || (isEdit && formState.id !== props.taskId)"
          v-show="activeStep > 0"
          secondary
          @click="prev"
        >
          {{ t('sync_datasource.prev') }}
        </el-button>
        <el-button
          :disabled="formLoading || (isEdit && formState.id !== props.taskId)"
          element-loading-spinner=""
          v-show="activeStep === 0 || activeStep < 2"
          type="primary"
          @click="next('next')"
        >
          {{ t('sync_datasource.next') }}
        </el-button>
        <el-button
          :disabled="formLoading || (isEdit && formState.id !== props.taskId)"
          v-show="activeStep >= 2 && !isEdit"
          type="primary"
          @click="next('next')"
        >
          {{ t('sync_task.confirm') }}
        </el-button>
        <el-button
          :disabled="
            formLoading ||
            checkingTarget ||
            formState.id !== props.taskId ||
            formState.editableCheckFailed ||
            formState.sourceDatasourceMissing
          "
          v-show="isEdit"
          type="primary"
          @click="next('save')"
        >
          {{ t('sync_datasource.save') }}
        </el-button>
      </div>
    </div>
  </el-drawer>
</template>
<style lang="less">
.task-form-drawer-fullscreen {
  .target-check-notice {
    display: flex;
    align-items: flex-start;
    gap: 16px;
    margin: 24px 0;

    .ed-alert {
      flex: 1;
      min-width: 0;
    }
    .ed-button {
      flex-shrink: 0;
    }
  }
  .target-check-context {
    margin: 8px 0;
    overflow-wrap: anywhere;
  }
  .target-check-details {
    margin-top: 12px;
    summary {
      cursor: pointer;
    }
    pre {
      margin: 8px 0 0;
      max-height: 160px;
      overflow: auto;
      white-space: pre-wrap;
      overflow-wrap: anywhere;
    }
  }
  .ed-drawer__header > :first-child {
    flex: none;
    width: auto;
  }

  .ed-checkbox__label:hover {
    color: var(--ed-checkbox-text-color);
  }

  .ed-drawer__header {
    border-color: rgba(31, 35, 41, 0.15);
    justify-content: space-between;
  }

  .datasource-close {
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

  .ed-drawer__body {
    padding: 0 24px;

    .task-form-container {
      height: 100%;

      .task-form-class {
        height: calc(100% - 64px);
        width: 100%;
        align-items: center;
        overflow-y: auto;
        overflow-x: hidden;

        .ed-input__wrapper {
          padding-right: 12px;
          padding-left: 12px;
        }

        .el-input-number .el-input__inner {
          text-align: left !important;
        }

        .base-info {
          padding: 24px 0 0 0;

          .ed-form-item {
            margin-bottom: 16px;

            .item-label-class {
              display: inline-flex;
              align-items: center;

              i {
                margin-left: 4.67px;
              }
            }

            .ed-radio__label {
              padding-left: 8px;
            }

            .ed-form-item__label,
            .ed-radio__label,
            .ed-checkbox__label {
              font-size: 14px;
              font-family: var(--de-custom_font, 'PingFang');
              font-weight: 400;
            }

            .ed-form-item__error {
              position: relative;
            }

            .ed-input-number {
              width: 100%;
            }

            .ed-select {
              width: 100%;
            }
          }

          .scheduler {
            border-radius: 4px;
            margin-bottom: 20px;

            .ds-task-form-inline {
              width: 100%;
              display: flex;
              align-items: center;

              .ed-input-number {
                width: 140px;
                margin: 0 6px;
              }

              .ed-select {
                width: 140px;

                :deep(.ed-input) {
                  width: 100% !important;
                }
              }

              span.ds-span {
                margin-left: 6px;
              }
            }

            .date-picker-box {
              .ed-date-editor.ed-input {
                width: 100%;

                .ed-input__wrapper {
                  width: 100%;

                  .clear-icon {
                    position: absolute !important;
                    transform: translateX(-24px);
                  }
                }
              }

              .ed-form-item__content {
                display: inline;
              }
            }

            :deep(.ed-radio) {
              font-size: 14px;
              font-weight: 100;
            }
          }
        }
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
</style>
