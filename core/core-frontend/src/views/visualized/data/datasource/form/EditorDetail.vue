<script lang="ts" setup>
import icon_calendar_outlined from '@/assets/svg/icon_calendar_outlined.svg'
import icon_rename_outlined from '@/assets/svg/icon_rename_outlined.svg'
import icon_down_outlined from '@/assets/svg/icon_down_outlined.svg'
import icon_down_outlined1 from '@/assets/svg/icon_down_outlined-1.svg'
import icon_add_outlined from '@/assets/svg/icon_add_outlined.svg'
import deCopy from '@/assets/svg/de-copy.svg'
import deDelete from '@/assets/svg/de-delete.svg'
import icon_warning_filled from '@/assets/svg/icon_warning_filled.svg'
import icon_deleteTrash_outlined from '@/assets/svg/icon_delete-trash_outlined.svg'
import icon_edit_outlined from '@/assets/svg/icon_edit_outlined.svg'
import { ref, reactive, computed, toRefs, nextTick, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { cloneDeep } from 'lodash-es'
import ApiHttpRequestDraw from './ApiHttpRequestDraw.vue'
import type { Configuration, ApiConfiguration, SyncSetting } from './option'
import { fieldType, fieldTypeText } from '@/utils/attr'
import { Icon } from '@/components/icon-custom'
import { getSchema } from '@/api/datasource'
import { Base64 } from 'js-base64'
import { CustomPassword } from '@/components/custom-password'
import { ElForm, ElMessage, ElMessageBox } from 'element-plus-secondary'
import Cron from '@/components/cron/src/Cron.vue'
import { ComponentPublicInstance } from 'vue'
import { XpackComponent } from '@/components/plugin'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { boolean } from 'mathjs'
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
        enableDataFill?: boolean
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
  },
  isSupportSetKey: {
    type: boolean,
    required: true
  },
  pluginDs: {
    type: [],
    required: true
  },
  pluginIndex: {
    type: String,
    required: true
  },
  isPlugin: {
    type: boolean,
    required: true
  }
})

const { form, activeStep, isSupportSetKey, pluginDs, pluginIndex, isPlugin } = toRefs(prop)

const state = reactive({
  itemRef: []
})

const schemas = ref([])
const targetCharset = ref(['GBK', 'UTF-8'])
const charset = ref(['GBK', 'BIG5', 'ISO-8859-1', 'UTF-8', 'UTF-16', 'CP850', 'EUC_JP', 'EUC_KR'])

const loading = ref(false)
const dsForm = ref<FormInstance>()

const cronEdit = ref(true)

const defaultRule = {
  name: [
    {
      required: true,
      message: t('datasource.input_name'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 64,
      message: t('datasource.input_limit_1_64', [1, 64]),
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
    page: {
      pageType: 'empty',
      requestData: [],
      responseData: []
    },
    authManager: {
      verification: '',
      username: '',
      password: ''
    }
  },
  fields: [],
  useJsonPath: false,
  jsonPath: ''
}
let time
const initForm = (type, pluginDsList, indexPlugin, isPluginDs) => {
  pluginDs.value = pluginDsList
  pluginIndex.value = indexPlugin
  isPlugin.value = isPluginDs
  if (!type.startsWith('API')) {
    form.value.configuration = {
      dataBase: '',
      jdbcUrl: '',
      urlType: 'hostName',
      sshType: 'password',
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
  }
  if (type.startsWith('API')) {
    form.value.syncSetting = {
      updateType: 'all_scope',
      syncRate: 'SIMPLE_CRON',
      simpleCronValue: '1',
      simpleCronType: 'minute',
      startTime: '',
      endTime: '',
      endLimit: '0',
      cron: '0 0/1 * * * ? *'
    }
  }
  if (type === 'oracle') {
    form.value.configuration.connectionType = 'sid'
  }
  form.value.type = type

  time = setTimeout(() => {
    clearTimeout(time)
    dsApiForm.value && dsApiForm.value.clearValidate()
    dsForm.value && dsForm.value.clearValidate()
  }, 0)
}

const notapiexcelconfig = computed(() => form.value && !form.value.type.startsWith('API'))

const authMethodList = [
  {
    id: 'passwd',
    label: t('datasource.passwd')
  },
  {
    id: 'kerberos',
    label: 'Kerberos'
  }
]

const validateSshHost = (_: any, value: any, callback: any) => {
  if ((value === undefined || value === null || value === '') && form.value.configuration.useSSH) {
    callback(new Error(t('data_source.cannot_be_empty')))
  }
  return callback()
}

const validateSshPort = (_: any, value: any, callback: any) => {
  if ((value === undefined || value === null || value === '') && form.value.configuration.useSSH) {
    callback(new Error(t('data_source.cannot_be_empty_de')))
  }
  return callback()
}

const validateSshUserName = (_: any, value: any, callback: any) => {
  if ((value === undefined || value === null || value === '') && form.value.configuration.useSSH) {
    callback(new Error(t('data_source.cannot_be_empty_de_name')))
  }
  return callback()
}

const validateSshPassword = (_: any, value: any, callback: any) => {
  if (
    (value === undefined || value === null || value === '') &&
    form.value.configuration.useSSH &&
    form.value.configuration.sshType === 'password'
  ) {
    callback(new Error(t('data_source.cannot_be_empty_de_pwd')))
  }
  return callback()
}

const validateSshkey = (_: any, value: any, callback: any) => {
  if (
    (value === null || value === '' || value === undefined) &&
    form.value.configuration.useSSH &&
    form.value.configuration.sshType === 'sshkey'
  ) {
    callback(new Error(t('data_source.cannot_be_empty_de_key')))
  }
  return callback()
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
        message: t('common.inputText') + ' ' + t('datasource.initial_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.minPoolSize': [
      {
        required: true,
        message: t('common.inputText') + ' ' + t('datasource.min_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.maxPoolSize': [
      {
        required: true,
        message: t('common.inputText') + ' ' + t('datasource.max_pool_size'),
        trigger: 'blur'
      }
    ],
    'configuration.queryTimeout': [
      {
        required: true,
        message: t('common.inputText') + ' ' + t('datasource.query_timeout'),
        trigger: 'blur'
      }
    ],
    'configuration.sshHost': [{ validator: validateSshHost, trigger: 'blur' }],
    'configuration.sshPort': [{ validator: validateSshPort, trigger: 'blur' }],
    'configuration.sshUserName': [{ validator: validateSshUserName, trigger: 'blur' }],
    'configuration.sshPassword': [{ validator: validateSshPassword, trigger: 'blur' }],
    'configuration.sshKey': [{ validator: validateSshkey, trigger: 'blur' }]
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

  if (form.value.type === 'es') {
    configRules['configuration.url'] = [
      {
        required: true,
        message: t('datasource.please_input_datasource_url'),
        trigger: 'change'
      }
    ]
  }
  rule.value = { ...cloneDeep(configRules), ...cloneDeep(defaultRule) }
}

watch(
  () => form.value.type,
  val => {
    if (!val.startsWith('API')) {
      rule.value = cloneDeep(defaultRule)
      setRules()
    }
  },
  {
    immediate: true
  }
)

watch(
  () => activeStep.value,
  () => {
    showCron.value = form.value.syncSetting?.syncRate === 'CRON'
  }
)

const setItemRef = (ele: ComponentPublicInstance | null | Element) => {
  state.itemRef.push(ele)
}

const copyItem = (item?: ApiConfiguration) => {
  const newItem = JSON.parse(JSON.stringify(item))
  newItem.deTableName = ''
  newItem.serialNumber =
    form.value.apiConfiguration[form.value.apiConfiguration.length - 1].serialNumber + 1
  newItem.copy = true
  const reg = new RegExp(item.name + '_copy_' + '([0-9]*)', 'gim')
  let number = 0
  for (let i = 1; i < form.value.apiConfiguration.length; i++) {
    const match = form.value.apiConfiguration[i].name.match(reg)
    if (match !== null) {
      const num = match[0].substring(
        form.value.apiConfiguration[i].name.length + 5,
        match[0].length - 1
      )
      if (!isNaN(parseInt(num)) && parseInt(num) > number) {
        number = parseInt(num)
      }
    }
  }
  number = number + 1
  newItem.name = item.name + '_copy_' + number
  state.itemRef = []
  form.value.apiConfiguration.push(newItem)
  ElMessage.success(t('datasource.success_copy'))
}
const addApiItem = item => {
  let apiItem = null
  let editItem = false
  api_table_title.value = t('datasource.data_table')
  if (item) {
    apiItem = cloneDeep(item)
    editItem = true
  } else {
    apiItem = cloneDeep(defaultApiItem)
    apiItem.type = activeName.value
    let serialNumber1 =
      form.value.apiConfiguration.length > 0
        ? form.value.apiConfiguration[form.value.apiConfiguration.length - 1].serialNumber + 1
        : 0
    let serialNumber2 =
      form.value.paramsConfiguration && form.value.paramsConfiguration.length > 0
        ? form.value.paramsConfiguration[form.value.paramsConfiguration.length - 1].serialNumber + 1
        : 0
    apiItem.serialNumber = serialNumber1 + serialNumber2
  }
  nextTick(() => {
    editApiItem.value.initApiItem(
      apiItem,
      form.value,
      activeName.value,
      editItem,
      isSupportSetKey.value,
      pluginDs.value,
      pluginIndex.value,
      isPlugin.value
    )
  })
}

const activeName = ref('table')
const showPriority = ref(false)
const showSSH = ref(false)

const deleteItem = (item, idx) => {
  form.value.apiConfiguration.splice(form.value.apiConfiguration.indexOf(item), 1)
  cancelItem(idx)
}
const cancelItem = (index: number) => {
  state.itemRef[index].hide()
}
const submitForm = () => {
  dsForm.value.clearValidate()
  return dsForm.value.validate
}

const submitApiForm = () => {
  dsApiForm.value.clearValidate()
  return dsApiForm.value.validate
}

const clearForm = () => {
  return dsForm.value.clearValidate()
}

const resetForm = () => {
  dsForm.value.resetFields()
}

const returnItem = apiItem => {
  let find = false
  if (apiItem.type !== 'params') {
    apiItem.status = 'Success'
    for (let i = 0; i < form.value.apiConfiguration.length; i++) {
      if (form.value.apiConfiguration[i].serialNumber === apiItem.serialNumber) {
        find = true
        form.value.apiConfiguration[i] = apiItem
      }
    }
    if (!find) {
      state.itemRef = []
      form.value.apiConfiguration.push(apiItem)
    }
  } else {
    if (form.value.paramsConfiguration) {
      for (let i = 0; i < form.value.paramsConfiguration.length; i++) {
        if (form.value.paramsConfiguration[i].serialNumber === apiItem.serialNumber) {
          find = true
          form.value.paramsConfiguration[i] = apiItem
          if (apiItem.serialNumber === activeParamsID.value) {
            setActiveName(apiItem)
          }
        }
      }
    } else {
      form.value.paramsConfiguration = []
    }
    if (!find) {
      state.itemRef = []
      form.value.paramsConfiguration.push(apiItem)
    }
  }
}

const showCron = ref(false)

const onRateChange = () => {
  if (form.value.syncSetting.syncRate === 'SIMPLE') {
    form.value.syncSetting.endLimit = 0
    form.value.syncSetting.endTime = 0
    form.value.syncSetting.cron = ''
  }
  if (form.value.syncSetting.syncRate === 'SIMPLE_CRON') {
    form.value.syncSetting.cron = '0 0/1 * * * ? *'
    form.value.syncSetting.simpleCronType = 'minute'
  }
  if (form.value.syncSetting.syncRate === 'CRON') {
    form.value.syncSetting.cron = '00 00 * ? * * *'
  }
  nextTick(() => {
    showCron.value = form.value.syncSetting.syncRate === 'CRON'
  })
}

const onSimpleCronChange = () => {
  if (form.value.syncSetting.simpleCronType === 'minute') {
    if (form.value.syncSetting.simpleCronValue < 1 || form.value.syncSetting.simpleCronValue > 59) {
      ElMessage.warning(t('cron.minute_limit'))
      form.value.syncSetting.simpleCronValue = 59
    }
    form.value.syncSetting.cron = '0 0/' + form.value.syncSetting.simpleCronValue + ' * * * ? *'
    return
  }
  if (form.value.syncSetting.simpleCronType === 'hour') {
    if (form.value.syncSetting.simpleCronValue < 1 || form.value.syncSetting.simpleCronValue > 23) {
      ElMessage.warning(t('cron.hour_limit'))
      form.value.syncSetting.simpleCronValue = 23
    }
    form.value.syncSetting.cron = '0 0 0/' + form.value.syncSetting.simpleCronValue + ' * * ? *'
    return
  }
  if (form.value.syncSetting.simpleCronType === 'day') {
    if (form.value.syncSetting.simpleCronValue < 1 || form.value.syncSetting.simpleCronValue > 31) {
      ElMessage.warning(t('cron.day_limit'))
      form.value.syncSetting.simpleCronValue = 31
    }
    form.value.syncSetting.cron = '0 0 0 1/' + form.value.syncSetting.simpleCronValue + ' * ? *'
    return
  }
}

const showSchema = ref(false)

const getDsSchema = () => {
  showSchema.value = true
  const validateFrom = dsForm.value.validate
  validateFrom(val => {
    showSchema.value = false
    if (val) {
      const request = JSON.parse(JSON.stringify(form.value))
      request.configuration = Base64.encode(JSON.stringify(request.configuration))
      loading.value = true
      getSchema(request)
        .then(res => {
          schemas.value = (res.data || []).map(ele => ({
            value: ele,
            label: ele
          }))
          ElMessage.success(t('commons.success'))
        })
        .finally(() => {
          loading.value = false
        })
    }
  })
}

const validatorSchema = () => {
  dsForm.value.validateField('configuration.schema')
}

const dsApiForm = ref()
const apiRule = {
  'syncSetting.updateType': [
    {
      required: true,
      message: t('datasource.update_type'),
      trigger: 'change'
    }
  ],
  'syncSetting.syncRate': [
    {
      required: true,
      message: t('datasource.sync_rate'),
      trigger: 'change'
    }
  ],
  'syncSetting.simpleCronValue': [
    {
      required: true,
      message: t('auth.set_rules'),
      trigger: 'change'
    }
  ],
  'syncSetting.cron': [
    {
      required: true,
      message: t('common.cron_exp'),
      trigger: 'change'
    }
  ],
  'syncSetting.startTime': [
    {
      required: true,
      message: t('sync_task.please_choose_start_time'),
      trigger: 'change'
    }
  ]
}
const dialogEditParams = ref(false)
const dialogRenameApi = ref(false)
const activeParamsName = ref('')
const activeParamsID = ref(0)
const paramsObj = ref({
  name: '',
  id: 1,
  deType: 0
})

const apiObj = ref({
  name: '',
  serialNumber: 1
})
const paramsObjRules = {
  name: [
    {
      required: true,
      message: t('data_source.enter_parameter_name'),
      trigger: 'change'
    },
    {
      min: 2,
      max: 64,
      message: t('data_source.to_64_characters'),
      trigger: 'blur'
    }
  ]
}

const apiObjRules = {
  name: [
    {
      required: true,
      message: t('data_source.the_interface_name'),
      trigger: 'change'
    },
    {
      min: 2,
      max: 64,
      message: t('data_source.to_64_characters_de'),
      trigger: 'blur'
    }
  ]
}
const setActiveName = val => {
  gridData.value = val.fields
  activeParamsName.value = val.name
  activeParamsID.value = val.serialNumber
}

const paramsObjRef = ref()
const apiObjRef = ref()

const saveParamsObj = () => {
  paramsObjRef.value.validate(result => {
    if (result) {
      gridData.value.forEach(ele => {
        if (ele.id === paramsObj.value.id) {
          ele.name = paramsObj.value.name
        }
      })
    }
  })
}

const saveApiObj = () => {
  apiObjRef.value.validate(result => {
    if (result) {
      form.value.paramsConfiguration.forEach(ele => {
        if (ele.serialNumber === apiObj.value.serialNumber) {
          ele.name = apiObj.value.name
        }
      })
    }
    dialogRenameApi.value = false
  })
}

const paramsResetForm = () => {
  dialogEditParams.value = false
}

const apiResetForm = () => {
  dialogRenameApi.value = false
}

const gridData = ref([])
const handleApiParams = (cmd: string, data) => {
  if (cmd === 'rename') {
    dialogRenameApi.value = true
    apiObj.value.name = data.name
    apiObj.value.serialNumber = data.serialNumber
  }
  if (cmd === 'delete') {
    ElMessageBox.confirm(t('data_source.sure_to_delete'), {
      confirmButtonType: 'danger',
      type: 'warning',
      autofocus: false,
      showClose: false
    }).then(() => {
      let index = 0
      for (let i = 0; i < form.value.paramsConfiguration.length; i++) {
        if (form.value.paramsConfiguration[i].serialNumber === data.serialNumber) {
          index = i
        }
      }
      form.value.paramsConfiguration.splice(index, 1)
      if (activeParamsName.value === data.name) {
        gridData.value = []
      }
    })
  }
  if (cmd === 'edit') {
    addApiItem(data)
  }
}

const delParams = data => {
  ElMessageBox.confirm(t('data_source.sure_to_delete'), {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    showClose: false
  }).then(() => {
    gridData.value.splice(gridData.value.indexOf(data), 1)
  })
}
const datasetTypeList = [
  {
    label: t('data_source.rename'),
    svgName: icon_rename_outlined,
    command: 'rename'
  },
  {
    label: t('data_source.delete'),
    svgName: icon_deleteTrash_outlined,
    command: 'delete'
  }
]
defineExpose({
  submitForm,
  submitApiForm,
  resetForm,
  initForm,
  clearForm
})
</script>

<template>
  <div class="editor-detail">
    <div class="detail-inner create-dialog">
      <div v-show="form.type.startsWith('API')" class="info-update">
        <div :class="activeStep === 1 && 'active'" class="info-text">
          {{ t('data_source.source_configuration_information') }}
        </div>
        <div class="update-info-line"></div>
        <div :class="activeStep === 2 && 'active'" class="update-text">
          {{ t('data_source.data_update_settings') }}
        </div>
      </div>
      <div
        class="title-form_primary base-info"
        v-show="activeStep !== 2 && form.type.startsWith('API')"
      >
        {{ t('datasource.basic_info') }}
      </div>
      <el-form
        @submit.prevent
        ref="dsForm"
        :model="form"
        :rules="rule"
        label-width="180px"
        label-position="top"
        require-asterisk-position="right"
        v-loading="loading"
      >
        <el-form-item
          :label="t('data_source.data_source_name')"
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
        <template v-if="form.type.startsWith('API')">
          <div class="title-form_primary flex-space table-info-mr" v-show="activeStep !== 2">
            <el-tabs v-model="activeName" class="api-tabs">
              <el-tab-pane :label="t('datasource.data_table')" name="table"></el-tab-pane>
              <el-tab-pane
                v-if="form.type === 'API'"
                :label="t('data_source.interface_parameters')"
                name="params"
              ></el-tab-pane>
            </el-tabs>
            <el-button type="primary" style="margin-left: auto" @click="() => addApiItem(null)">
              <template #icon>
                <Icon name="icon_add_outlined">
                  <icon_add_outlined class="svg-icon" />
                </Icon>
              </template>
              {{ t('common.add') }}
            </el-button>
          </div>
          <empty-background
            v-show="activeStep !== 2"
            v-if="!form.apiConfiguration.length && activeName === 'table'"
            :description="t('datasource.no_data_table')"
            img-type="noneWhite"
          />
          <template
            v-if="form.type.startsWith('API') && activeStep === 1 && activeName === 'table'"
          >
            <div class="api-card-content">
              <div
                v-for="(api, idx) in form.apiConfiguration"
                :key="api.id"
                class="api-card"
                @click="addApiItem(api)"
              >
                <el-row>
                  <el-col style="display: flex" :span="19">
                    <span class="name ellipsis">{{ api.name }}</span>
                    <span v-if="api.status === 'Error'" class="de-tag invalid">{{
                      t('datasource.invalid')
                    }}</span>
                    <span v-if="api.status === 'Success'" class="de-tag valid">{{
                      t('datasource.valid')
                    }}</span>
                  </el-col>
                  <el-col style="text-align: right" :span="5">
                    <el-icon class="de-copy-icon hover-icon" @click.stop="copyItem(api)">
                      <Icon name="de-copy">
                        <deCopy class="svg-icon" />
                      </Icon>
                    </el-icon>

                    <span @click.stop>
                      <el-popover
                        placement="top"
                        width="200"
                        :ref="setItemRef"
                        show-arrow
                        popper-class="api-table-delete"
                        trigger="click"
                      >
                        <template #reference>
                          <el-icon class="de-delete-icon hover-icon">
                            <Icon name="de-delete"><deDelete class="svg-icon" /></Icon>
                          </el-icon>
                        </template>
                        <template #default>
                          <el-icon class="de-copy-icon icon-warning">
                            <Icon name="icon_warning_filled"
                              ><icon_warning_filled class="svg-icon"
                            /></Icon>
                          </el-icon>
                          <div class="tips">
                            {{ t('datasource.delete_this_item') }}
                          </div>
                          <div class="foot">
                            <el-button style="min-width: 48px" secondary @click="cancelItem(idx)">{{
                              t('common.cancel')
                            }}</el-button>
                            <el-button
                              style="min-width: 48px"
                              type="primary"
                              @click="deleteItem(api, idx)"
                              >{{ t('common.sure') }}</el-button
                            >
                          </div>
                        </template>
                      </el-popover>
                    </span>
                  </el-col>
                </el-row>
                <div class="req-title">
                  <span>{{ t('datasource.method') }}</span>
                  <span>{{ t('datasource.url') }}</span>
                </div>
                <div class="req-value">
                  <span>{{ api.method }}</span>
                  <el-tooltip effect="dark" :content="api.url" placement="top">
                    <span>{{ api.url }}</span>
                  </el-tooltip>
                </div>
              </div>
            </div>
          </template>
          <div
            style="display: flex"
            v-if="form.type === 'API' && activeStep === 1 && activeName === 'params'"
          >
            <div class="left-api_params">
              <div
                v-for="ele in form.paramsConfiguration"
                :class="[{ active: activeParamsName === ele.name }]"
                class="list-item_primary"
                :title="ele.name"
                :key="ele.id"
                @click="setActiveName(ele)"
              >
                <span class="label">{{ ele.name }}</span>
                <span class="name-copy">
                  <el-icon class="hover-icon" @click.stop="handleApiParams('edit', ele)">
                    <icon name="icon_edit_outlined"><icon_edit_outlined class="svg-icon" /></icon>
                  </el-icon>
                  <handle-more
                    icon-size="24px"
                    @handle-command="cmd => handleApiParams(cmd, ele)"
                    :menu-list="datasetTypeList"
                    placement="bottom-start"
                  ></handle-more>
                </span>
              </div>
            </div>
            <div class="right-api_params">
              <el-table
                height="300"
                style="width: 100%"
                header-cell-class-name="header-cell"
                :data="gridData"
              >
                <el-table-column :label="t('visualization.param_name')">
                  <template #default="scope">
                    {{ scope.row.name || '-' }}
                  </template>
                </el-table-column>
                <el-table-column :label="t('deDataset.parameter_type')">
                  <template #default="scope">
                    <div class="flex-align-center icon">
                      <el-icon>
                        <Icon>
                          <component
                            class="svg-icon"
                            :class="`field-icon-${fieldType[scope.row.deType]}`"
                            :is="iconFieldMap[fieldType[scope.row.deType]]"
                          ></component>
                        </Icon>
                      </el-icon>
                      {{ fieldTypeText[scope.row.deType] }}
                    </div>
                  </template>
                </el-table-column>

                <el-table-column :label="t('common.operate')">
                  <template #default="scope">
                    <el-button text @click.stop="delParams(scope.row)">
                      <template #icon>
                        <Icon name="icon_delete-trash_outlined">
                          <icon_deleteTrash_outlined class="svg-icon" />
                        </Icon>
                      </template>
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </div>
        </template>
        <template v-if="notapiexcelconfig">
          <el-form-item
            :label="t('data_source.connection_method')"
            prop="type"
            v-if="form.type !== 'es'"
          >
            <el-radio-group v-model="form.configuration.urlType">
              <el-radio label="hostName">{{ t('data_source.hostname') }}</el-radio>
              <el-radio label="jdbcUrl">{{ t('data_source.jdbc_connection') }}</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-form-item
            :label="t('data_source.jdbc_connection_string')"
            prop="configuration.jdbcUrl"
            v-if="form.configuration.urlType === 'jdbcUrl'"
          >
            <el-input
              v-model="form.configuration.jdbcUrl"
              :placeholder="t('data_source.jdbc_connection_string')"
              autocomplete="off"
            />
          </el-form-item>

          <el-form-item
            :label="t('datasource.host')"
            prop="configuration.host"
            v-if="form.configuration.urlType !== 'jdbcUrl' && form.type !== 'es'"
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
            v-if="form.configuration.urlType !== 'jdbcUrl' && form.type !== 'es'"
          >
            <el-input-number
              v-model="form.configuration.port"
              autocomplete="off"
              step-strictly
              class="text-left"
              :min="0"
              :placeholder="t('common.inputText') + ' ' + t('datasource.port')"
              controls-position="right"
              type="number"
            />
          </el-form-item>
          <el-form-item
            :label="t('datasource.data_base')"
            prop="configuration.dataBase"
            v-if="form.configuration.urlType !== 'jdbcUrl' && form.type !== 'es'"
          >
            <el-input
              v-model="form.configuration.dataBase"
              :placeholder="t('datasource.please_input_data_base')"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item
            :label="t('datasource.auth_method')"
            prop="configuration.authMethod"
            v-if="form.type === 'presto'"
          >
            <el-select
              :placeholder="t('common.inputText') + ' ' + t('datasource.auth_method')"
              v-model="form.configuration.authMethod"
              class="de-select"
            >
              <el-option
                v-for="item in authMethodList"
                :key="item.id"
                :label="item.label"
                :value="item.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="t('datasource.client_principal')"
            prop="configuration.username"
            v-if="form.type === 'presto'"
          >
            <el-input
              :placeholder="t('common.inputText') + ' ' + t('datasource.client_principal')"
              v-model="form.configuration.username"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item
            :label="t('datasource.keytab_Key_path')"
            prop="configuration.password"
            v-if="form.type === 'presto'"
          >
            <CustomPassword
              :placeholder="t('common.inputText') + ' ' + t('datasource.keytab_Key_path')"
              show-password
              type="password"
              v-model="form.configuration.password"
            />
            <p>
              {{ t('datasource.kerbers_info') }}
            </p>
          </el-form-item>
          <el-form-item
            v-if="form.type == 'es'"
            :label="$t('datasource.datasource_url')"
            prop="configuration.url"
          >
            <el-input
              v-model="form.configuration.url"
              :placeholder="$t('datasource.please_input_datasource_url')"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item :label="t('datasource.user_name')" v-if="form.type !== 'presto'">
            <el-input
              :placeholder="t('common.inputText') + ' ' + t('datasource.user_name')"
              v-model="form.configuration.username"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item :label="t('datasource.password')" v-if="form.type !== 'presto'">
            <CustomPassword
              :placeholder="t('common.inputText') + ' ' + t('datasource.password')"
              show-password
              type="password"
              v-model="form.configuration.password"
            />
          </el-form-item>
          <el-form-item
            v-if="form.type == 'oracle' && form.configuration.urlType !== 'jdbcUrl'"
            :label="t('datasource.connection_mode')"
            prop="configuration.connectionType"
          >
            <el-radio v-model="form.configuration.connectionType" label="sid"
              >{{ t('datasource.oracle_sid') }}
            </el-radio>
            <el-radio v-model="form.configuration.connectionType" label="serviceName">
              {{ t('datasource.oracle_service_name') }}
            </el-radio>
          </el-form-item>
          <el-form-item
            v-if="['oracle', 'sqlServer', 'pg', 'redshift', 'db2'].includes(form.type)"
            class="schema-label"
            :prop="showSchema ? '' : 'configuration.schema'"
          >
            <template v-slot:label>
              <span class="name">{{ t('datasource.schema') }}<i class="required" /></span>
              <el-button text size="small" @click="getDsSchema()">
                <template #icon>
                  <Icon name="icon_add_outlined">
                    <icon_add_outlined class="svg-icon" />
                  </Icon>
                </template>
                {{ t('datasource.get_schema') }}
              </el-button>
            </template>
            <el-select-v2
              v-model="form.configuration.schema"
              :options="schemas"
              filterable
              :placeholder="t('common.please_select')"
              class="de-select"
              @change="validatorSchema"
              @blur="validatorSchema"
            />
          </el-form-item>
          <el-form-item v-if="form.type == 'oracle'" :label="$t('datasource.charset')">
            <el-select
              v-model="form.configuration.charset"
              filterable
              :placeholder="$t('datasource.please_choose_charset')"
              class="de-select"
            >
              <el-option v-for="item in charset" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="form.type == 'oracle'" :label="$t('datasource.targetCharset')">
            <el-select
              v-model="form.configuration.targetCharset"
              filterable
              :placeholder="$t('datasource.please_choose_targetCharset')"
              class="de-select"
            >
              <el-option v-for="item in targetCharset" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
          <el-form-item
            :label="t('datasource.extra_params')"
            v-if="form.configuration.urlType !== 'jdbcUrl' && form.type !== 'es'"
          >
            <el-input
              :placeholder="t('common.inputText') + ' ' + t('datasource.extra_params')"
              v-model="form.configuration.extraParams"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item>
            <span
              v-if="!['es', 'api'].includes(form.type) && form.configuration.urlType !== 'jdbcUrl'"
              class="de-expand"
              @click="showSSH = !showSSH"
              >{{ t('data_source.ssh_settings') }}
              <el-icon>
                <Icon
                  ><component
                    class="svg-icon"
                    :is="showSSH ? icon_down_outlined : icon_down_outlined1"
                  ></component
                ></Icon>
              </el-icon>
            </span>
          </el-form-item>
          <template v-if="showSSH">
            <el-form-item>
              <el-checkbox v-model="form.configuration.useSSH"
                >{{ t('data_source.enable_ssh') }}
              </el-checkbox>
            </el-form-item>
            <el-form-item :label="t('data_source.host')" prop="configuration.sshHost">
              <el-input
                v-model="form.configuration.sshHost"
                :placeholder="t('data_source.please_enter_hostname')"
                autocomplete="off"
              />
            </el-form-item>
            <el-form-item :label="t('data_source.port')" prop="configuration.sshPort">
              <el-input-number
                v-model="form.configuration.sshPort"
                autocomplete="off"
                step-strictly
                class="text-left"
                :min="0"
                :max="65535"
                :placeholder="t('common.inputText') + ' ' + t('datasource.port')"
                controls-position="right"
              />
            </el-form-item>
            <el-form-item :label="t('datasource.user_name')" prop="configuration.sshUserName">
              <el-input
                :placeholder="t('common.inputText') + ' ' + t('datasource.user_name')"
                v-model="form.configuration.sshUserName"
                autocomplete="off"
                :maxlength="255"
              />
            </el-form-item>
            <el-form-item :label="t('data_source.connection_method')">
              <el-radio-group v-model="form.configuration.sshType">
                <el-radio label="password">{{ t('data_source.password') }}</el-radio>
                <el-radio label="sshkey">ssh key</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item
              :label="t('datasource.password')"
              v-if="form.configuration.sshType === 'password'"
              prop="configuration.sshPassword"
            >
              <CustomPassword
                :placeholder="t('common.inputText') + ' ' + t('datasource.password')"
                show-password
                type="password"
                v-model="form.configuration.sshPassword"
              />
            </el-form-item>

            <el-form-item
              label="ssh key"
              prop="configuration.sshKey"
              v-if="form.configuration.sshType === 'sshkey'"
            >
              <el-input
                type="textarea"
                :rows="6"
                v-model="form.configuration.sshKey"
                :placeholder="t('data_source.enter_ssh_key')"
                autocomplete="off"
              />
            </el-form-item>

            <el-form-item
              :label="t('data_source.ssh_key_password')"
              v-if="form.configuration.sshType === 'sshkey'"
            >
              <CustomPassword
                :placeholder="t('common.inputText') + ' ' + t('datasource.password')"
                show-password
                type="password"
                v-model="form.configuration.sshKeyPassword"
              />
            </el-form-item>
          </template>
          <el-form-item>
            <span
              v-if="!['es', 'api'].includes(form.type)"
              class="de-expand"
              @click="showPriority = !showPriority"
              >{{ t('datasource.priority') }}
              <el-icon>
                <Icon
                  ><component
                    :is="showPriority ? icon_down_outlined : icon_down_outlined1"
                  ></component
                ></Icon>
              </el-icon>
            </span>
          </el-form-item>

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
                    :placeholder="t('common.inputText') + ' ' + t('datasource.initial_pool_size')"
                    type="number"
                    :min="0"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item
                  :label="t('datasource.min_pool_size')"
                  prop="configuration.minPoolSize"
                >
                  <el-input-number
                    v-model="form.configuration.minPoolSize"
                    controls-position="right"
                    autocomplete="off"
                    :placeholder="t('common.inputText') + ' ' + t('datasource.min_pool_size')"
                    type="number"
                    :min="0"
                  />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="24">
              <el-col :span="12">
                <el-form-item
                  :label="t('datasource.max_pool_size')"
                  prop="configuration.maxPoolSize"
                >
                  <el-input-number
                    v-model="form.configuration.maxPoolSize"
                    controls-position="right"
                    autocomplete="off"
                    :placeholder="t('common.inputText') + ' ' + t('datasource.max_pool_size')"
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
                    :placeholder="t('common.inputText') + ' ' + t('datasource.query_timeout')"
                    type="number"
                    :min="0"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </template>

          <!--    数据填报      -->
          <XpackComponent
            :form="form"
            jsname="L2NvbXBvbmVudC9kYXRhLWZpbGxpbmcvRGF0YXNvdXJjZUVuYWJsZURhdGFGaWxsaW5n"
          />
        </template>
      </el-form>
      <el-form
        ref="dsApiForm"
        :model="form"
        style="margin-top: 24px"
        :rules="apiRule"
        label-width="180px"
        label-position="top"
        require-asterisk-position="right"
      >
        <el-form-item
          :label="t('datasource.update_type')"
          prop="syncSetting.updateType"
          v-if="activeStep === 2 && form.type.startsWith('API')"
        >
          <el-radio-group v-model="form.syncSetting.updateType">
            <el-radio label="all_scope">{{ t('datasource.all_scope') }}</el-radio>
            <el-radio label="add_scope"> {{ t('datasource.add_scope') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="t('datasource.sync_rate')"
          prop="syncSetting.syncRate"
          v-if="activeStep === 2 && form.type.startsWith('API')"
        >
          <el-radio-group v-model="form.syncSetting.syncRate" @change="onRateChange">
            <el-radio label="RIGHTNOW">{{ t('data_source.update_now') }}</el-radio>
            <el-radio label="CRON">{{ t('datasource.cron_config') }}</el-radio>
            <el-radio label="SIMPLE_CRON">{{ t('datasource.simple_cron') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div
          v-if="
            activeStep === 2 &&
            form.type.startsWith('API') &&
            form.syncSetting.syncRate !== 'RIGHTNOW'
          "
          class="execute-rate-cont"
        >
          <el-form-item
            :label="t('auth.set_rules')"
            v-if="form.syncSetting.syncRate === 'SIMPLE_CRON'"
            prop="syncSetting.simpleCronValue"
          >
            <div class="simple-cron">
              {{ t('common.every') }}
              <el-input-number
                v-model="form.syncSetting.simpleCronValue"
                controls-position="right"
                :min="1"
                @change="onSimpleCronChange()"
              />
              <el-select
                v-model="form.syncSetting.simpleCronType"
                filterable
                @change="onSimpleCronChange()"
              >
                <el-option :label="t('common.minute')" value="minute" />
                <el-option :label="t('common.hour')" value="hour" />
                <el-option :label="t('common.day')" value="day" />
              </el-select>
              {{ t('data_source.update_once') }}
            </div>
          </el-form-item>
          <el-form-item v-if="form.syncSetting.syncRate === 'CRON'" prop="syncSetting.cron">
            <el-popover :width="834" v-model="cronEdit" trigger="click">
              <template #default>
                <div style="width: 814px; height: 450px; overflow-y: auto">
                  <cron
                    v-if="showCron"
                    v-model="form.syncSetting.cron"
                    :is-rate="form.syncRate === 'CRON'"
                    @close="cronEdit = false"
                  />
                </div>
              </template>
              <template #reference>
                <el-input v-model="form.syncSetting.cron" @click="cronEdit = true" />
              </template>
            </el-popover>
          </el-form-item>
          <el-form-item
            v-if="form.syncSetting.syncRate !== 'RIGHTNOW'"
            :label="t('datasource.start_time')"
            prop="syncSetting.startTime"
          >
            <el-date-picker
              v-model="form.syncSetting.startTime"
              class="de-date-picker"
              :prefix-icon="icon_calendar_outlined"
              type="datetime"
              :placeholder="t('datasource.start_time')"
            />
          </el-form-item>
          <el-form-item
            v-if="form.syncSetting.syncRate !== 'RIGHTNOW'"
            :label="t('datasource.end_time')"
            prop="syncSetting.endLimit"
          >
            <div style="width: 100%">
              <el-date-picker
                v-model="form.syncSetting.endTime"
                class="de-date-picker"
                :prefix-icon="icon_calendar_outlined"
                type="datetime"
                :placeholder="t('datasource.end_time')"
              />
            </div>
          </el-form-item>
        </div>
      </el-form>
      <el-dialog
        :title="t('data_source.edit_parameters')"
        v-model="dialogEditParams"
        width="420px"
        class="create-dialog"
        :before-close="paramsResetForm"
      >
        <el-form
          label-position="top"
          require-asterisk-position="right"
          ref="paramsObjRef"
          @keydown.stop.prevent.enter
          :model="paramsObj"
          :rules="paramsObjRules"
        >
          <el-form-item :label="t('visualization.param_name')" prop="name">
            <el-input
              :placeholder="t('data_source.enter_parameter_name')"
              v-model="paramsObj.name"
            />
          </el-form-item>
          <el-form-item :label="t('deDataset.parameter_type')" prop="deType">
            <el-radio-group v-model="paramsObj.deType">
              <el-radio :value="0" :label="t('data_source.text')"></el-radio>
              <el-radio :value="2" :label="t('data_source.numerical_value')"></el-radio>
              <el-radio :value="3" :label="t('data_source.numeric_value_decimal')"></el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button secondary @click="paramsResetForm">{{ t('dataset.cancel') }}</el-button>
          <el-button type="primary" @click="saveParamsObj">{{ t('dataset.confirm') }}</el-button>
        </template>
      </el-dialog>
      <el-dialog
        :title="t('data_source.rename')"
        v-model="dialogRenameApi"
        width="420px"
        class="create-dialog"
        :before-close="apiResetForm"
      >
        <el-form
          label-position="top"
          require-asterisk-position="right"
          ref="apiObjRef"
          @keydown.stop.prevent.enter
          :model="apiObj"
          :rules="apiObjRules"
        >
          <el-form-item :label="t('data_source.interface_name')" prop="name">
            <el-input :placeholder="t('data_source.the_interface_name')" v-model="apiObj.name" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button secondary @click="apiResetForm">{{ t('dataset.cancel') }}</el-button>
          <el-button type="primary" @click="saveApiObj">{{ t('dataset.confirm') }}</el-button>
        </template>
      </el-dialog>
      <api-http-request-draw @return-item="returnItem" ref="editApiItem"></api-http-request-draw>
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
    font-family: var(--de-custom_font, 'PingFang');
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
      font-family: var(--de-custom_font, 'PingFang');
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
        font-family: var(--de-custom_font, 'PingFang');
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

.api-card-content {
  display: flex;
  flex-wrap: wrap;
  margin-left: -16px;
}

.api-card {
  height: 120px;
  width: 392px;
  border-radius: 4px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin: 0 0 16px 16px;
  padding: 16px;
  font-family: var(--de-custom_font, 'PingFang');
  cursor: pointer;

  &:hover {
    border-color: var(--ed-color-primary);
  }

  .name {
    font-size: 16px;
    font-weight: 500;
    margin-right: 8px;
    max-width: 70%;
  }

  .req-title,
  .req-value {
    display: flex;
    font-size: 14px;
    font-weight: 400;

    :nth-child(1) {
      width: 120px;
    }

    :nth-child(2) {
      margin-left: 24px;
      max-width: 230px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .req-title {
    color: var(--deTextSecondary, #646a73);
    margin: 16px 0 4px 0;
  }

  .req-value {
    color: var(--deTextPrimary, #1f2329);
  }

  .de-copy-icon {
    margin-right: 16px;
    color: var(--deTextSecondary, #646a73);
  }

  .de-delete-icon {
    cursor: pointer;
  }

  .de-tag {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    border-radius: 2px;
    padding: 1px 6px;
    height: 24px;
    font-size: 14px;

    &.invalid {
      color: #646a73;
      background: rgba(31, 35, 41, 0.1);
    }

    &.valid {
      color: green;
      background: rgba(52, 199, 36, 0.2);
    }
  }
}
</style>

<style lang="less">
.api-table-delete {
  padding: 20px 24px !important;
  display: flex;
  flex-wrap: wrap;

  .small {
    height: 28px;
    min-width: 48px !important;
  }

  .icon-warning {
    transform: translateY(3px);
  }

  .tips {
    font-family: var(--de-custom_font, 'PingFang');
    font-size: 14px;
    font-weight: 500;
    line-height: 22px;
    margin-left: 8.67px;
    color: var(--deTextPrimary, #1f2329);
  }

  i {
    font-size: 14.666666030883789px;
    color: var(--deWarning, #ff8800);
    line-height: 22px;
  }

  .foot {
    text-align: right;
    width: 100%;
    margin-top: 16px;
  }
}

.schema-label {
  .ed-form-item__label {
    display: flex !important;
    justify-content: space-between;
    padding-right: 0;

    &::after {
      display: none;
    }

    .name {
      .required::after {
        content: '*';
        color: #f54a45;
        margin-left: 2px;
      }
    }
  }
}
</style>
