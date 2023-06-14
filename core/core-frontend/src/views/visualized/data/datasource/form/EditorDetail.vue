<script lang="ts" setup>
import { ref, reactive, computed, toRefs, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { cloneDeep } from 'lodash-es'
import { useRouter } from 'vue-router'
import ApiHttpRequestDraw from './ApiHttpRequestDraw.vue'
import { Configuration, ApiConfiguration, SyncSetting } from './index.vue'
import { validateById, save, validate } from '@/api/datasource'
import { Base64 } from 'js-base64'
import { ElForm, ElMessage } from 'element-plus-secondary'
import Cron from '@/components/cron/src/Cron.vue'
import { ComponentPublicInstance } from 'vue'
const { t } = useI18n()
const { push } = useRouter()

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
  editDs: {
    required: false,
    default: false,
    type: Boolean
  },
  activeStep: {
    required: false,
    default: 1,
    type: Number
  }
})

const { form, editDs, activeStep } = toRefs(prop)

const dsFormDisabled = ref(false)

const state = reactive({
  itemRef: []
})
const dsForm = ref<FormInstance>()

const cronEdit = ref(true)
const apiRequestDraw = ref(false)

const rule = reactive<FormRules>({
  name: [
    {
      required: true,
      message: t('datasource.input_name'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 25,
      message: t('datasource.input_limit_2_25', [2, 25]),
      trigger: 'blur'
    }
  ],
  description: [
    {
      required: true,
      trigger: 'blur'
    }
  ]
})
const api_table_title = ref('')
const editApiItem = ref()
const defaultApiItem = {
  name: '',
  url: '',
  serialNumber: 0,
  method: 'GET',
  request: {
    headers: [{}],
    arguments: [],
    body: {
      type: '',
      raw: '',
      kvs: []
    }
  },
  fields: []
}

const initForm = type => {
  if (type !== 'API') {
    form.value.configuration = {
      dataBase: '',
      extraParams: '',
      username: '',
      password: '',
      host: '',
      authMethod: '',
      port: ''
    }
    setRules()
  }
  if (type === 'API') {
    form.value.syncSetting = {
      updateType: 'all_scope',
      syncRate: 'CRON',
      simpleCronValue: '',
      simpleCronType: '',
      startTime: '',
      endTime: '',
      endLimit: '',
      cron: ''
    }
  }
  form.value.type = type
  setTimeout(() => {
    dsForm.value.clearValidate()
  }, 0)
}

const initEditForm = () => {
  dsFormDisabled.value = true
}

const notapiexcelconfig = computed(() => form.value.type !== 'API')

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
const setRules = () => {
  const configRules = {
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
        message: t('datasource.please_input_host'),
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
    ]
  }
  Object.assign(rule, configRules)
}

const setItemRef = (ele: ComponentPublicInstance | null | Element) => {
  state.itemRef.push(ele)
}

const copyItem = (item?: ApiConfiguration) => {
  if (dsFormDisabled.value) {
    return
  }
  var newItem = JSON.parse(JSON.stringify(item))
  newItem.serialNumber =
    form.value.apiConfiguration[form.value.apiConfiguration.length - 1].serialNumber + 1
  var reg = new RegExp(item.name + '_copy_' + '([0-9]*)', 'gim')
  var number = 0
  for (var i = 1; i < form.value.apiConfiguration.length; i++) {
    var match = form.value.apiConfiguration[i].name.match(reg)
    if (match !== null) {
      var num = match[0].substring(
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
  form.value.apiConfiguration.push(newItem)
  ElMessage.success(t('datasource.success_copy'))
}
const addApiItem = item => {
  if (dsFormDisabled.value) return
  apiRequestDraw.value = true
  let apiItem = null
  api_table_title.value = t('datasource.data_table')
  if (item) {
    apiItem = cloneDeep(item)
  } else {
    apiItem = cloneDeep(defaultApiItem)
    apiItem.serialNumber =
      form.value.apiConfiguration.length > 0
        ? form.value.apiConfiguration[form.value.apiConfiguration.length - 1].serialNumber + 1
        : 0
  }
  nextTick(() => {
    editApiItem.value.initApiItem(apiItem)
  })
}

const deleteItem = item => {
  form.value.apiConfiguration.splice(form.value.apiConfiguration.indexOf(item), 1)
}
const cancelItem = (index: number) => {
  state.itemRef[index].hide()
}
const submitForm = () => {
  dsForm.value.validate(valid => {
    if (valid) {
    }
  })
  return form
}

const resetForm = () => {
  dsForm.value.resetFields()
}

const returnItem = apiItem => {
  var find = false
  for (let i = 0; i < form.value.apiConfiguration.length; i++) {
    if (form.value.apiConfiguration[i].serialNumber === apiItem.serialNumber) {
      find = true
      form.value.apiConfiguration[i] = JSON.parse(JSON.stringify(apiItem))
    }
  }
  if (!find) {
    form.value.apiConfiguration.push(apiItem)
  }
  apiRequestDraw.value = false
}

const onRateChange = () => {
  if (form.value.syncSetting.syncRate === 'SIMPLE') {
    form.value.syncSetting.endLimit = 0
    form.value.syncSetting.endTime = 0
    form.value.syncSetting.cron = ''
  }
  if (form.value.syncSetting.syncRate === 'SIMPLE_CRON') {
    form.value.syncSetting.cron = '0 0 0/1 *  * ? *'
  }
  if (form.value.syncSetting.syncRate === 'CRON') {
    form.value.syncSetting.cron = '00 00 * ? * * *'
  }
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

const editForm = () => {
  dsFormDisabled.value = false
}

const cancel = () => {
  dsFormDisabled.value = true
}

const saveDs = () => {
  const request = JSON.parse(JSON.stringify(form.value))
  if (form.value.type === 'API') {
    if (form.value.apiConfiguration.length == 0) {
      return
    }
    request.syncSetting.startTime = new Date(request.syncSetting.startTime).getTime()
    request.syncSetting.endTime = new Date(request.syncSetting.endTime).getTime()
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }
  save(request).then(() => {
    push('/data/datasource')
    ElMessage.success(t('common.save_success'))
    dsFormDisabled.value = true
  })
}

const validateDS = () => {
  const request = JSON.parse(JSON.stringify(form.value))
  if (form.value.type === 'API') {
    if (form.value.apiConfiguration.length == 0) {
      return
    }
    request.configuration = Base64.encode(JSON.stringify(request.apiConfiguration))
  } else {
    request.configuration = Base64.encode(JSON.stringify(request.configuration))
  }

  if (editDs.value && dsFormDisabled.value) {
    validateById(form.value.id).then(() => {
      ElMessage.success(t('datasource.validate_succCorness'))
    })
  } else {
    validate(request).then(() => {
      ElMessage.success(t('datasource.validate_success'))
    })
  }
}

defineExpose({
  submitForm,
  resetForm,
  initForm,
  initEditForm
})
</script>

<template>
  <div class="editor-detail">
    <div class="detail-inner">
      <div class="detail-operate" v-show="editDs">
        <el-button v-show="!dsFormDisabled" @click="() => cancel()">{{
          t('common.cancel')
        }}</el-button>
        <el-button @click="() => validateDS()">{{ t('datasource.validate') }}</el-button>
        <el-button v-show="dsFormDisabled" type="primary" @click="() => editForm()">{{
          t('common.edit')
        }}</el-button>
        <el-button v-show="!dsFormDisabled" type="primary" @click="() => saveDs()">{{
          t('common.sure')
        }}</el-button>
      </div>
      <div class="title-form_primary" v-show="activeStep !== 2">
        {{ t('datasource.basic_info') }}
      </div>
      <el-form
        ref="dsForm"
        :model="form"
        :rules="rule"
        :disabled="dsFormDisabled"
        label-width="180px"
        label-position="top"
        require-asterisk-position="right"
      >
        <el-form-item :label="t('datasource.display_name')" prop="name" v-show="activeStep !== 2">
          <el-input
            v-model="form.name"
            autocomplete="off"
            :placeholder="t('datasource.input_name')"
          />
        </el-form-item>
        <el-form-item :label="t('common.description')" prop="description" v-show="activeStep !== 2">
          <el-input
            type="textarea"
            v-model="form.description"
            :row="5"
            :maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <template v-if="form.type === 'API'">
          <div class="title-form_primary flex-space" v-show="activeStep !== 2">
            <span>{{ t('datasource.data_table') }}</span>
            <el-button type="primary" style="margin-left: auto" @click="() => addApiItem(null)">
              <template #icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </template>
              {{ t('common.add') }}
            </el-button>
          </div>
          <empty-background
            v-show="activeStep !== 2"
            v-if="!form.apiConfiguration.length"
            :description="t('datasource.no_data_table')"
            img-type="table"
          />
          <template v-if="form.type === 'API' && activeStep !== 2">
            <div
              v-for="(api, idx) in form.apiConfiguration"
              :key="api.id"
              :style="{ cursor: dsFormDisabled ? 'not-allowed' : 'pointer' }"
              class="api-card"
              @click="addApiItem(api)"
            >
              <el-row>
                <el-col style="display: flex" :span="19">
                  <span class="name">{{ api.name }}</span>
                  <span v-if="api.status === 'Error'" class="de-tag invalid">{{
                    t('datasource.invalid')
                  }}</span>
                  <span v-if="api.status === 'Success'" class="de-tag valid">{{
                    t('datasource.valid')
                  }}</span>
                </el-col>
                <el-col style="text-align: right" :span="5">
                  <el-icon
                    class="de-copy-icon"
                    :disabled="dsFormDisabled"
                    @click.stop="copyItem(api)"
                  >
                    <Icon name="de-copy"></Icon>
                  </el-icon>

                  <span @click.stop>
                    <el-popover
                      placement="top"
                      width="200"
                      :ref="setItemRef"
                      :disabled="dsFormDisabled"
                      popper-class="api-table-delete"
                      trigger="click"
                    >
                      <template #reference>
                        <el-icon
                          class="de-delete-icon"
                          :class="[dsFormDisabled ? 'not-allow' : '']"
                          :disabled="dsFormDisabled"
                        >
                          <Icon name="de-delete"></Icon>
                        </el-icon>
                      </template>
                      <template #default>
                        <el-icon class="de-copy-icon icon-warning" :disabled="dsFormDisabled">
                          <Icon name="icon_warning_filled"></Icon>
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
                            @click="deleteItem(api)"
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
                <span :title="api.url">{{ api.url }}</span>
              </div>
            </div>
          </template>
        </template>
        <template v-if="notapiexcelconfig">
          <el-form-item :label="t('datasource.host')" prop="configuration.host">
            <el-input
              v-model="form.configuration.host"
              :placeholder="t('datasource._ip_address')"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item :label="t('datasource.data_base')" prop="configuration.dataBase">
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
              style="width: 100%"
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
            <el-input v-model="form.configuration.username" autocomplete="off" />
          </el-form-item>
          <el-form-item
            :label="t('datasource.keytab_Key_path')"
            prop="configuration.password"
            v-if="form.type === 'presto'"
          >
            <el-input type="password" v-model="form.configuration.password" />
            <p>
              {{ t('datasource.kerbers_info') }}
            </p>
          </el-form-item>
          <el-form-item
            :label="t('datasource.user_name')"
            prop="configuration.username"
            v-if="form.type !== 'presto'"
          >
            <el-input v-model="form.configuration.username" autocomplete="off" />
          </el-form-item>
          <el-form-item
            :label="t('datasource.password')"
            prop="configuration.password"
            v-if="form.type !== 'presto'"
          >
            <el-input type="password" v-model="form.configuration.password" />
          </el-form-item>
          <el-form-item :label="t('datasource.extra_params')">
            <el-input v-model="form.configuration.extraParams" autocomplete="off" />
          </el-form-item>
          <el-form-item :label="t('datasource.port')" prop="configuration.port">
            <el-input v-model="form.configuration.port" autocomplete="off" type="number" min="0" />
          </el-form-item>
        </template>
        <!--        API update setting -->
        <el-form-item
          :label="$t('datasource.update_type')"
          prop="type"
          v-if="activeStep === 2 || (editDs && form.type === 'API')"
        >
          <el-radio-group v-model="form.syncSetting.updateType">
            <el-radio label="all_scope">{{ $t('datasource.all_scope') }}</el-radio>
            <el-radio label="add_scope"> {{ $t('datasource.add_scope') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item
          :label="$t('datasource.execute_rate')"
          prop="rate"
          v-if="activeStep === 2 || (editDs && form.type === 'API')"
        >
          <el-radio-group v-model="form.syncSetting.syncRate" @change="onRateChange">
            <el-radio label="RIGHTNOW">{{ $t('datasource.execute_once') }}</el-radio>
            <el-radio label="CRON">{{ $t('datasource.cron_config') }}</el-radio>
            <el-radio label="SIMPLE_CRON">{{ $t('datasource.simple_cron') }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <div
          v-if="
            (activeStep === 2 || (editDs && form.type === 'API')) &&
            form.syncSetting.syncRate !== 'RIGHTNOW'
          "
          class="execute-rate-cont"
        >
          <el-form-item
            v-if="form.syncSetting.syncRate === 'SIMPLE_CRON'"
            :label="$t('datasource.execute_rate')"
            prop="rate"
          >
            <div class="simple-cron">
              {{ $t('common.every') }}
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
                <el-option :label="$t('common.minute')" value="minute" />
                <el-option :label="$t('common.hour')" value="hour" />
                <el-option :label="$t('common.day')" value="day" />
              </el-select>
              {{ $t('common.every_exec') }}
            </div>
          </el-form-item>
          <el-form-item
            v-if="form.syncSetting.syncRate === 'CRON'"
            prop="cron"
            :label="$t('common.cron_exp')"
          >
            <el-popover :width="834" v-model="cronEdit" trigger="click">
              <template #default>
                <div style="width: 814px">
                  <cron
                    v-model="form.syncSetting.cron"
                    :is-rate="form.syncRate === 'CRON'"
                    @close="cronEdit = false"
                  />
                </div>
              </template>
              <template #reference>
                <el-input
                  v-model="form.syncSetting.cron"
                  style="width: 50%"
                  @click="cronEdit = true"
                />
              </template>
            </el-popover>
          </el-form-item>
          <el-form-item
            v-if="form.syncSetting.syncRate !== 'RIGHTNOW'"
            :label="$t('datasource.start_time')"
            prop="startTime"
          >
            <el-date-picker
              v-model="form.syncSetting.startTime"
              class="de-date-picker"
              type="datetime"
              :placeholder="$t('datasource.start_time')"
            />
          </el-form-item>
          <el-form-item
            v-if="form.syncSetting.syncRate !== 'RIGHTNOW'"
            :label="$t('datasource.end_time')"
            prop="end"
          >
            <el-radio-group v-model="form.syncSetting.endLimit">
              <el-radio label="0">{{ $t('datasource.no_limit') }}</el-radio>
              <el-radio label="1"> {{ $t('datasource.set_end_time') }}</el-radio>
            </el-radio-group>
            <div style="width: 100%">
              <el-date-picker
                v-if="form.syncSetting.endLimit === '1'"
                v-model="form.syncSetting.endTime"
                class="de-date-picker"
                type="datetime"
                :placeholder="$t('datasource.end_time')"
              />
            </div>
          </el-form-item>
        </div>
      </el-form>
      <api-http-request-draw
        v-if="apiRequestDraw"
        @return-item="returnItem"
        ref="editApiItem"
      ></api-http-request-draw>
    </div>
  </div>
</template>

<style lang="less" scoped>
.editor-detail {
  width: 100%;
  display: flex;
  justify-content: center;

  :deep(.ed-date-editor.ed-input) {
    .ed-input__wrapper {
      width: 100%;
    }
    width: 100%;
  }
  .simple-cron {
    .ed-select,
    .ed-input-number {
      width: 140px;
      margin: 0 8px;
    }
  }
  .detail-inner {
    width: 600px;

    .title-form_primary {
      margin: 16px 0;
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
.api-card {
  height: 128px;
  width: 600px;
  border-radius: 4px;
  border: 1px solid var(--deCardStrokeColor, #dee0e3);
  border-radius: 4px;
  margin-bottom: 12px;
  padding: 20px 16px;
  font-family: PingFang SC;
  .name {
    font-size: 16px;
    font-weight: 500;
    margin-right: 8px;
    max-width: 80%;
    text-overflow: ellipsis;
    overflow: hidden;
    white-space: nowrap;
  }
  .req-title,
  .req-value {
    display: flex;
    font-size: 14px;
    font-weight: 400;
    :nth-child(1) {
      width: 56px;
    }
    :nth-child(2) {
      margin-left: 84px;
      max-width: 415px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }
  .req-title {
    color: var(--deTextPrimary, #1f2329);
    margin: 16px 0 4px 0;
  }
  .req-value {
    color: var(--deTextSecondary, #646a73);
  }
  .de-copy-icon {
    cursor: pointer;
    margin-right: 20px;
    color: var(--deTextSecondary, #646a73);
  }
  .de-delete-icon:not(.not-allow) {
    cursor: pointer;
    &:hover {
      color: var(--deDanger, #f54a45);
    }
  }
  .de-tag {
    display: inline-flex;
    justify-content: center;
    align-items: center;
    border-radius: 2px;
    padding: 1px 6px;
    height: 24px;
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
    font-family: PingFang SC;
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
</style>
