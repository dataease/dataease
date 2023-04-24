<script lang="ts" setup>
import { ref, reactive, computed } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { clone } from 'lodash-es'
import ApiHttpRequestDraw from './ApiHttpRequestDraw.vue'
const { t } = useI18n()
interface Configuration {
  dataBase: string
  extraParams: string
  username: string
  password: string
  host: string
  authMethod: string
  port: string
}

interface ApiConfiguration {
  id: string
  name: string
  method: string
  url: string
  status: string
  useJsonPath: boolean
  serialNumber: number
}

const form = reactive<{
  name: string
  desc: string
  type: string
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
}>({
  name: '',
  desc: '',
  type: 'API',
  apiConfiguration: []
})

const state = reactive({
  apiItem: {
    serialNumber: 0
  }
})
const dsForm = ref<FormInstance>()

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
  desc: [
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
    form.configuration = {
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
  form.type = type

  setTimeout(() => {
    dsForm.value.clearValidate()
  }, 0)
}

const notapiexcelconfig = computed(() => form.type !== 'API')
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
        required: true,
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

const dsFormDisabled = ref(false)

const copyItem = (item?: ApiConfiguration) => {
  if (dsFormDisabled.value) {
    return
  }
  //   var newItem = JSON.parse(JSON.stringify(item))
  //   newItem.serialNumber =
  //     this.form.apiConfiguration[this.form.apiConfiguration.length - 1]
  //       .serialNumber + 1
  //   var reg = new RegExp(item.name + '_copy_' + '([0-9]*)', 'gim')
  //   var number = 0
  //   for (var i = 1; i < this.form.apiConfiguration.length; i++) {
  //     var match = this.form.apiConfiguration[i].name.match(reg)
  //     if (match !== null) {
  //       var num = match[0].substring(
  //         this.form.apiConfiguration[i].name.length + 5,
  //         match[0].length - 1
  //       )
  //       if (!isNaN(parseInt(num)) && parseInt(num) > number) {
  //         number = parseInt(num)
  //       }
  //     }
  //   }
  //   number = number + 1
  //   newItem.name = item.name + '_copy_' + number
  //   this.form.apiConfiguration.push(newItem)
  //   this.openMessageSuccess('datasource.success_copy')
}
const addApiItem = (item?: ApiConfiguration) => {
  if (dsFormDisabled.value) {
    return
  }
  api_table_title.value = t('datasource.data_table')
  if (item) {
    state.apiItem = clone(item)
  } else {
    state.apiItem = clone(defaultApiItem)
    state.apiItem.serialNumber =
      form.apiConfiguration.length > 0
        ? form.apiConfiguration[form.apiConfiguration.length - 1].serialNumber + 1
        : 0
  }
  editApiItem.value.initApiItem(state.apiItem)
}
const deleteItem = item => {
  form.apiConfiguration.splice(form.apiConfiguration.indexOf(item), 1)
}
const cancelItem = ({ name }) => {
  //   this.$refs[`apiTable${name}`][0].doClose()
}
const submitForm = () => {
  dsForm.value.validate((valid, fields) => {
    if (valid) {
      console.log('submit!')
    } else {
      console.log('error submit!', fields)
    }
  })
}

const resetForm = () => {
  dsForm.value.resetFields()
}

defineExpose({
  submitForm,
  resetForm,
  initForm
})
</script>

<template>
  <div class="editor-detail">
    <div class="detail-inner">
      <div class="title-form_primary">
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
        <el-form-item :label="t('datasource.display_name')" prop="name">
          <el-input
            v-model="form.name"
            autocomplete="off"
            :placeholder="t('datasource.input_name')"
          />
        </el-form-item>
        <el-form-item :label="t('commons.description')" prop="desc">
          <el-input type="textarea" v-model="form.desc" :row="5" :maxlength="50" show-word-limit />
        </el-form-item>
        <template v-if="form.type == 'API'">
          <div class="title-form_primary flex-space">
            <span>{{ t('datasource.data_table') }}</span>
            <el-button type="primary" style="margin-left: auto" @click="() => addApiItem()">
              <template #icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </template>
              {{ t('commons.add') }}
            </el-button>
          </div>
          <empty-background
            v-if="!form.apiConfiguration.length"
            :description="t('datasource.no_data_table')"
            img-type="table"
          />
          <template v-else>
            <div
              v-for="api in form.apiConfiguration"
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
                        <el-icon class="de-copy-icon" :disabled="dsFormDisabled">
                          <Icon name="icon_info_filled"></Icon>
                        </el-icon>
                        <div class="tips">
                          {{ t('datasource.delete_this_item') }}
                        </div>
                        <div class="foot">
                          <el-button secondary @click="cancelItem(api)">{{
                            t('fu.search_bar.cancel')
                          }}</el-button>
                          <el-button type="primary" @click="deleteItem(api)">{{
                            t('fu.search_bar.ok')
                          }}</el-button>
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

          <el-form-item :label="t('datasource.datasource_url')" prop="configuration.host">
            <el-input
              v-model="form.configuration.host"
              :placeholder="t('datasource.please_input_datasource_url')"
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

          <el-form-item :label="t('datasource.auth_method')" prop="configuration.authMethod">
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

          <el-form-item :label="t('datasource.client_principal')" prop="configuration.username">
            <el-input v-model="form.configuration.username" autocomplete="off" />
          </el-form-item>

          <el-form-item :label="t('datasource.keytab_Key_path')" prop="configuration.password">
            <el-input type="password" v-model="form.configuration.password" />
            <p>
              {{ t('datasource.kerbers_info') }}
            </p>
          </el-form-item>
          <el-form-item prop="configuration.extraParams" :label="t('datasource.extra_params')">
            <el-input
              v-model="form.configuration.extraParams"
              :placeholder="t('fu.search_bar.please_input') + t('datasource.extra_params')"
              autocomplete="off"
            />
          </el-form-item>
          <el-form-item :label="t('datasource.port')" prop="configuration.port">
            <el-input
              v-model="form.configuration.port"
              autocomplete="off"
              type="number"
              :placeholder="t('components.enter_the_port')"
              min="0"
            />
          </el-form-item>
        </template>
      </el-form>
      <api-http-request-draw ref="editApiItem"></api-http-request-draw>
    </div>
  </div>
</template>

<style lang="less" scoped>
.editor-detail {
  width: 100%;
  display: flex;
  justify-content: center;
  .detail-inner {
    width: 600px;

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
