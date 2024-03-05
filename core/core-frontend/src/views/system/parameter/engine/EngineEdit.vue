<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import request from '@/config/axios'
import { dsTypes, Node } from '@/views/visualized/data/datasource/form/option'
import { cloneDeep } from 'lodash-es'
import { getDeEngine } from '@/api/datasource'
import { CustomPassword } from '@/components/custom-password'
import { Base64 } from 'js-base64'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)

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
const configRules = {
  'configuration.dataBase': [
    {
      required: true,
      message: t('datasource.please_input_data_base'),
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
const rule = { ...cloneDeep(configRules), ...cloneDeep(defaultRule) }

const emits = defineEmits(['saved'])
const resetForm = () => {
  dialogVisible.value = false
  Object.assign(nodeInfo, cloneDeep(defaultInfo))
}

const reset = () => {
  resetForm()
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.basic-param-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
const showPriority = ref(false)
const defaultInfo = {
  name: '',
  createBy: '',
  creator: '',
  createTime: '',
  description: '',
  id: 0,
  size: 0,
  nodeType: '',
  type: '',
  fileName: '',
  configuration: {
    host: '',
    port: 8081,
    dataBase: '',
    username: '',
    password: '',
    extraParams: '',
    initialPoolSize: 5,
    minPoolSize: 5,
    maxPoolSize: 5,
    queryTimeout: 30
  },
  syncSetting: null,
  apiConfiguration: [],
  weight: 0
}
const nodeInfo = reactive(cloneDeep(defaultInfo))
const edit = () => {
  getDeEngine()
    .then(res => {
      let {
        name,
        createBy,
        id,
        createTime,
        creator,
        type,
        pid,
        configuration,
        syncSetting,
        fileName,
        size,
        description,
        lastSyncTime
      } = res.data
      if (configuration) {
        configuration = JSON.parse(configuration)
      }
      Object.assign(nodeInfo, {
        name,
        pid,
        description,
        fileName,
        size,
        createTime,
        creator,
        createBy,
        id,
        type,
        configuration,
        syncSetting,
        lastSyncTime
      })
    })
    .finally(() => {
      dialogVisible.value = true
    })
}
const basicForm = ref()

const submitForm = async () => {
  let data = JSON.parse(JSON.stringify(nodeInfo)) as unknown as Omit<
    Node,
    'configuration' | 'apiConfiguration'
  > & {
    configuration: string
    apiConfiguration: string
  }
  data.configuration = Base64.encode(JSON.stringify(data.configuration))
  basicForm.value.validate(result => {
    if (result) {
      showLoading()
      request
        .post({ url: '/engine/save', data: data })
        .then(res => {
          if (res !== undefined) {
            console.log(res)
            ElMessage.success(t('common.save_success'))
            emits('saved')
            reset()
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
    }
  })
}

const validate = async () => {
  let data = JSON.parse(JSON.stringify(nodeInfo)) as unknown as Omit<
    Node,
    'configuration' | 'apiConfiguration'
  > & {
    configuration: string
    apiConfiguration: string
  }
  data.configuration = Base64.encode(JSON.stringify(data.configuration))
  basicForm.value.validate(result => {
    if (result) {
      showLoading()
      request
        .post({ url: '/engine/validate', data: data })
        .then(res => {
          if (res !== undefined) {
            ElMessage.success(t('datasource.validate_success'))
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
    }
  })
}

defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    title="引擎设置"
    v-model="dialogVisible"
    custom-class="basic-param-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="basicForm"
      require-asterisk-position="right"
      :model="nodeInfo"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="t('datasource.type')">
        <el-select v-model="nodeInfo.type" class="de-select" disabled>
          <el-option
            v-for="item in dsTypes"
            :key="item.type"
            :label="item.name"
            :value="item.type"
          />
        </el-select>
      </el-form-item>
      <el-form-item :label="t('datasource.host')" prop="configuration.host">
        <el-input
          v-model="nodeInfo.configuration.host"
          :placeholder="t('datasource._ip_address')"
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item :label="t('datasource.port')" prop="configuration.port">
        <el-input-number
          v-model="nodeInfo.configuration.port"
          autocomplete="off"
          step-strictly
          class="text-left"
          :min="0"
          :placeholder="t('common.inputText') + t('datasource.port')"
          controls-position="right"
          type="number"
        />
      </el-form-item>
      <el-form-item :label="t('datasource.data_base')" prop="configuration.dataBase">
        <el-input
          v-model="nodeInfo.configuration.dataBase"
          :placeholder="t('datasource.please_input_data_base')"
          autocomplete="off"
        />
      </el-form-item>

      <el-form-item :label="t('datasource.user_name')">
        <el-input
          :placeholder="t('common.inputText') + t('datasource.user_name')"
          v-model="nodeInfo.configuration.username"
          autocomplete="off"
        />
      </el-form-item>
      <el-form-item :label="t('datasource.password')">
        <CustomPassword
          :placeholder="t('common.inputText') + t('datasource.password')"
          show-password
          type="password"
          v-model="nodeInfo.configuration.password"
        />
      </el-form-item>
      <el-form-item :label="t('datasource.extra_params')">
        <el-input
          :placeholder="t('common.inputText') + t('datasource.extra_params')"
          v-model="nodeInfo.configuration.extraParams"
          autocomplete="off"
        />
      </el-form-item>
      <span class="de-expand" @click="showPriority = !showPriority"
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
                v-model="nodeInfo.configuration.initialPoolSize"
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
                v-model="nodeInfo.configuration.minPoolSize"
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
                v-model="nodeInfo.configuration.maxPoolSize"
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
                v-model="nodeInfo.configuration.queryTimeout"
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
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm()">{{ t('common.cancel') }}</el-button>
        <el-button secondary @click="validate()">{{ t('datasource.validate') }}</el-button>
        <el-button type="primary" @click="submitForm()">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>
<style lang="less">
.basic-param-drawer {
  .ed-drawer__footer {
    height: 64px !important;
    padding: 16px 24px !important;
    .dialog-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;
  }
}
</style>
<style scoped lang="less">
.basic-param-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
  .edit-all-line {
    width: 552px !important;
  }
}
.setting-hidden-item {
  display: none !important;
}
.ds-task-form-inline {
  width: 100%;
  display: flex;
  .ed-input-number {
    width: 140px;
    margin: 0 6px;
  }
  .ed-select {
    width: 240px;
    :deep(.ed-input) {
      width: 100% !important;
    }
  }
  span.ds-span {
    margin-left: 6px;
  }
}
</style>
