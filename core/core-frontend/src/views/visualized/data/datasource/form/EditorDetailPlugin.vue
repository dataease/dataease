<script lang="ts" setup>
import { ref, reactive, toRefs, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { cloneDeep } from 'lodash-es'
import type { Configuration, ApiConfiguration, SyncSetting } from './option'
import { Base64 } from 'js-base64'
import { ElMessage } from 'element-plus-secondary'
const { t } = useI18n()
const prop = defineProps({
  form: {
    required: false,
    default() {
      return reactive<{
        id: number
        name: string
        desc: string
        type: string
        syncSetting?: SyncSetting
        configuration?: Configuration
        apiConfiguration?: ApiConfiguration[]
        paramsConfiguration?: ApiConfiguration[]
      }>({
        id: 0,
        name: '',
        desc: '',
        type: 'API',
        apiConfiguration: []
      })
    },
    type: Object
  },

  activeStep: {
    required: false,
    default: 1,
    type: Number
  }
})

const { form, activeStep } = toRefs(prop)

const state = reactive({
  itemRef: []
})

const schemas = ref([])
const dsForm = ref<FormInstance>()

const defaultRule = {
  name: [
    {
      required: true,
      message: t('datasource.input_name'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 64,
      message: t('datasource.input_limit_2_25', [2, 64]),
      trigger: 'blur'
    }
  ]
}

const rule = ref<FormRules>(cloneDeep(defaultRule))
const api_table_title = ref('')
const editApiItem = ref()
const defaultApiItem = {
  name: '',
  deTableName: '',
  url: '',
  type: '',
  serialNumber: 0,
  method: 'GET',
  request: {
    headers: [{}],
    arguments: [],
    body: {
      type: '',
      raw: '',
      kvs: []
    },
    authManager: {
      verification: '',
      username: '',
      password: ''
    }
  },
  fields: []
}

const initForm = type => {
  form.value.configuration = {
    dataBase: '',
    jdbcUrl: '',
    urlType: 'hostName',
    extraParams: '',
    username: '',
    password: '',
    host: '',
    authMethod: '',
    port: '',
    initialPoolSize: 5,
    minPoolSize: 5,
    maxPoolSize: 5,
    queryTimeout: 30
  }
  schemas.value = []
  rule.value = cloneDeep(defaultRule)
  setRules()

  form.value.type = type
  setTimeout(() => {
    dsForm.value.clearValidate()
  }, 0)
}

const setRules = () => {
  const configRules = {
    'configuration.jdbcUrl': [
      {
        required: true,
        message: t('datasource.please_input_jdbc_url'),
        trigger: 'blur'
      }
    ],
    'configuration.dataBase': [
      {
        required: true,
        message: t('datasource.please_input_data_base'),
        trigger: 'blur'
      }
    ],
    'configuration.authMethod': [
      {
        required: true,
        message: t('datasource.please_select_oracle_type'),
        trigger: 'blur'
      }
    ],
    'configuration.username': [
      {
        required: true,
        message: t('datasource.please_input_user_name'),
        trigger: 'blur'
      }
    ],
    'configuration.password': [
      {
        required: true,
        message: t('datasource.please_input_password'),
        trigger: 'blur'
      }
    ],
    'configuration.host': [
      {
        required: true,
        message: t('datasource._ip_address'),
        trigger: 'blur'
      }
    ],
    'configuration.extraParams': [
      {
        required: false,
        message: t('datasource.please_input_url'),
        trigger: 'blur'
      }
    ],
    'configuration.port': [
      {
        required: true,
        message: t('datasource.please_input_port'),
        trigger: 'blur'
      }
    ],
    'configuration.initialPoolSize': [
      {
        required: true,
        message: t('common.inputText') + t('datasource.initial_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.minPoolSize': [
      {
        required: true,
        message: t('common.inputText') + t('datasource.min_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.maxPoolSize': [
      {
        required: true,
        message: t('common.inputText') + t('datasource.max_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.queryTimeout': [
      {
        required: true,
        message: t('common.inputText') + t('datasource.query_timeout'),
        trigger: 'blur'
      }
    ]
  }
  if (['oracle', 'sqlServer', 'pg', 'redshift', 'db2'].includes(form.value.type)) {
    configRules['configuration.schema'] = [
      {
        required: true,
        message: t('datasource.please_choose_schema'),
        trigger: 'blur'
      }
    ]
  }

  if (form.value.type === 'oracle') {
    configRules['configuration.connectionType'] = [
      {
        required: true,
        message: t('datasource.connection_mode'),
        trigger: 'change'
      }
    ]
  }
  rule.value = { ...cloneDeep(configRules), ...cloneDeep(defaultRule) }
}

watch(
  () => form.value.type,
  val => {
    if (val !== 'API') {
      rule.value = cloneDeep(defaultRule)
      setRules()
    }
  },
  {
    immediate: true
  }
)

const activeName = ref('table')
const showPriority = ref(false)

const submitForm = () => {
  dsForm.value.clearValidate()
  return dsForm.value.validate
}

const clearForm = () => {
  return dsForm.value.clearValidate()
}

const resetForm = () => {
  dsForm.value.resetFields()
}

const showSchema = ref(false)

const validatorSchema = () => {
  dsForm.value.validateField('configuration.schema')
}

const activeParamsName = ref('')
const activeParamsID = ref(0)

const setActiveName = val => {
  gridData.value = val.fields
  activeParamsName.value = val.name
  activeParamsName.value = val.serialNumber
}

const gridData = ref([])

defineExpose({
  submitForm,
  resetForm,
  initForm,
  clearForm
})
</script>

<template>
  <div class="editor-detail">
    <div class="detail-inner create-dialog">
      <el-form
        ref="dsForm"
        :model="form"
        :rules="rule"
        label-width="180px"
        label-position="top"
        require-asterisk-position="right"
      >
        <el-form-item
          :label="t('auth.datasource') + t('chart.name')"
          prop="name"
          v-show="activeStep !== 2"
        >
          <el-input
            v-model="form.name"
            autocomplete="off"
            :placeholder="t('datasource.input_name')"
          />
        </el-form-item>
        <el-form-item :label="t('common.description')" v-show="activeStep !== 2">
          <el-input
            class="description-text"
            type="textarea"
            :placeholder="t('common.inputText')"
            v-model="form.description"
            :row="10"
            :maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item
          :label="t('datasource.host')"
          prop="configuration.host"
          v-if="form.configuration.urlType !== 'jdbcUrl'"
        >
          <el-input
            v-model="form.configuration.host"
            :placeholder="t('datasource._ip_address')"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item
          :label="t('datasource.port')"
          prop="configuration.port"
          v-if="form.configuration.urlType !== 'jdbcUrl'"
        >
          <el-input-number
            v-model="form.configuration.port"
            autocomplete="off"
            step-strictly
            class="text-left"
            :min="0"
            :placeholder="t('common.inputText') + t('datasource.port')"
            controls-position="right"
            type="number"
          />
        </el-form-item>
        <el-form-item
          :label="t('datasource.data_base')"
          prop="configuration.dataBase"
          v-if="form.configuration.urlType !== 'jdbcUrl'"
        >
          <el-input
            v-model="form.configuration.dataBase"
            :placeholder="t('datasource.please_input_data_base')"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="t('datasource.user_name')" v-if="form.type !== 'presto'">
          <el-input
            :placeholder="t('common.inputText') + t('datasource.user_name')"
            v-model="form.configuration.username"
            autocomplete="off"
          />
        </el-form-item>
        <el-form-item :label="t('datasource.password')" v-if="form.type !== 'presto'">
          <CustomPassword
            :placeholder="t('common.inputText') + t('datasource.password')"
            show-password
            type="password"
            v-model="form.configuration.password"
          />
        </el-form-item>
        <el-form-item
          :label="t('datasource.extra_params')"
          v-if="form.configuration.urlType !== 'jdbcUrl'"
        >
          <el-input
            :placeholder="t('common.inputText') + t('datasource.extra_params')"
            v-model="form.configuration.extraParams"
            autocomplete="off"
          />
        </el-form-item>
        <span
          v-if="!['es', 'api'].includes(form.type)"
          class="de-expand"
          @click="showPriority = !showPriority"
          >{{ t('datasource.priority') }}
          <el-icon>
            <Icon :name="showPriority ? 'icon_down_outlined' : 'icon_down_outlined-1'"></Icon>
          </el-icon>
        </span>
        <template v-if="showPriority">
          <el-row :gutter="24" class="mb16">
            <el-col :span="12">
              <el-form-item
                :label="t('datasource.initial_pool_size')"
                prop="configuration.initialPoolSize"
              >
                <el-input-number
                  v-model="form.configuration.initialPoolSize"
                  controls-position="right"
                  autocomplete="off"
                  :placeholder="t('common.inputText') + t('datasource.initial_pool_size')"
                  type="number"
                  :min="0"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="t('datasource.min_pool_size')" prop="configuration.minPoolSize">
                <el-input-number
                  v-model="form.configuration.minPoolSize"
                  controls-position="right"
                  autocomplete="off"
                  :placeholder="t('common.inputText') + t('datasource.min_pool_size')"
                  type="number"
                  :min="0"
                />
              </el-form-item>
            </el-col>
          </el-row>
          <el-row :gutter="24">
            <el-col :span="12">
              <el-form-item :label="t('datasource.max_pool_size')" prop="configuration.maxPoolSize">
                <el-input-number
                  v-model="form.configuration.maxPoolSize"
                  controls-position="right"
                  autocomplete="off"
                  :placeholder="t('common.inputText') + t('datasource.max_pool_size')"
                  type="number"
                  :min="0"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item
                :label="`${t('datasource.query_timeout')}(${t('common.second')})`"
                prop="configuration.queryTimeout"
              >
                <el-input-number
                  v-model="form.configuration.queryTimeout"
                  controls-position="right"
                  autocomplete="off"
                  :placeholder="t('common.inputText') + t('datasource.query_timeout')"
                  type="number"
                  :min="0"
                />
              </el-form-item>
            </el-col>
          </el-row>
        </template>
      </el-form>
    </div>
  </div>
</template>

<style lang="less" scoped>
.editor-detail {
  width: 100%;
  display: flex;
  justify-content: center;
  .ed-radio {
    height: 22px;
  }

  .mb16 {
    :deep(.ed-form-item) {
      margin-bottom: 16px;
    }
  }

  .execute-rate-cont {
    border-radius: 4px;
    margin-top: -8px;
  }

  .de-select {
    width: 100%;
  }
  .ed-input-number {
    width: 100%;
  }

  :deep(.is-controls-right > span) {
    background: #fff;
  }

  .de-expand {
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: var(--ed-color-primary);
    cursor: pointer;
    display: inline-flex;
    align-items: center;

    .ed-icon {
      margin-left: 4px;
    }
  }

  :deep(.ed-date-editor.ed-input) {
    .ed-input__wrapper {
      width: 100%;
    }
    width: 100%;
  }
  .simple-cron {
    height: 32px;
    .ed-select,
    .ed-input-number {
      width: 140px;
      margin: 0 8px;
    }
  }
  .detail-inner {
    width: 800px;
    padding-top: 8px;

    .description-text {
      :deep(.ed-textarea__inner) {
        height: 92px;
      }
    }

    .base-info {
      margin: 24px 0 16px 0;
    }

    .left-api_params {
      border-top-left-radius: 4px;
      border-bottom-left-radius: 4px;
      border: 1px solid #bbbfc4;
      width: 300px;
      padding: 16px;
      .name-copy {
        display: none;
        line-height: 24px;
        margin-left: 4px;
      }

      .list-item_primary:hover {
        .name-copy {
          display: inline;
        }

        .label {
          width: 74% !important;
        }
      }
    }

    .right-api_params {
      border-top-right-radius: 4px;
      border-bottom-right-radius: 4px;
      border: 1px solid #bbbfc4;
      border-left: none;
      width: calc(100% - 200px);
    }

    .table-info-mr {
      margin: 28px 0 12px 0;
      .api-tabs {
        :deep(.ed-tabs__nav-wrap::after) {
          display: none;
        }
      }
    }

    .info-update {
      height: 22px;
      width: 100%;
      display: flex;
      align-items: center;
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
      justify-content: center;

      .update-info-line {
        width: 208px;
        height: 1px;
        background: #bcbdbf;
        margin: 0 8px;
      }

      .info-text,
      .update-text {
        padding-left: 16px;
        position: relative;
        color: #1f2329;
        font-weight: 400;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-style: normal;
        line-height: 22px;
        &::before {
          width: 8px;
          height: 8px;
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          border: 1px solid var(--ed-color-primary);
          border-radius: 50%;
        }

        &.active {
          font-weight: 500;
        }

        &.active::before {
          border: none;
          background: var(--ed-color-primary);
        }
      }
    }

    .detail-operate {
      text-align: right;
      padding: 8px 0;
    }

    .flex-space {
      display: flex;
      align-items: center;
    }
  }
}
</style>
