<script lang="tsx" setup>
import { nextTick, reactive, ref, shallowRef } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import type { ApiRequest } from './ApiHttpRequestForm.vue'
import ApiHttpRequestForm from './ApiHttpRequestForm.vue'
import { Icon } from '@/components/icon-custom'
import { Base64 } from 'js-base64'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { checkApiItem } from '@/api/datasource'
import { cloneDeep } from 'lodash-es'

export interface Field {
  name: string
  value: Array<{}>
  checked: boolean
  children?: Array<{}>
}

export interface ApiItem {
  status: string
  name: string
  deTableName?: string
  url: string
  method: string
  request: ApiRequest
  fields: Field[]
  jsonFields: JsonField[]
  useJsonPath: boolean
  showApiStructure: boolean
  jsonPath: string
  serialNumber: number
}

export interface JsonField {
  deType: number
  size: number
  children: null
  name: string
  checked: false
  extField: number
  jsonPath: string
  type: string
  originName: string
  deExtractType: number
}
const { t } = useI18n()

const originFieldItem = reactive({
  jsonFields: [],
  fields: []
})

let apiItem = reactive<ApiItem>({
  status: '',
  name: '',
  url: '',
  method: 'GET',
  request: {
    changeId: '',
    rest: [],
    headers: [],
    arguments: [],
    body: {
      typeChange: '',
      raw: '',
      kvs: []
    },
    authManager: {
      verification: '',
      username: '',
      password: ''
    }
  },
  fields: [],
  jsonFields: [],
  useJsonPath: false,
  showApiStructure: false,
  jsonPath: '',
  serialNumber: -1
})
let errMsg = []
const api_table_title = ref('datasource.data_table')
const apiItemForm = ref()
const showEmpty = ref(false)
const edit_api_item = ref(false)
const active = ref(1)
const loading = ref(false)
const columns = shallowRef([])
const tableData = shallowRef([])
const apiItemBasicInfo = ref<FormInstance>()
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
const fieldType = (deType: number) => {
  return ['text', '', 'value', 'value'][deType]
}
const initApiItem = (val: ApiItem) => {
  Object.assign(apiItem, val)
  edit_api_item.value = true
  active.value = 0
}

const showApiData = () => {
  apiItemBasicInfo.value.validate(valid => {
    if (valid) {
      apiItem.showApiStructure = true
      const data = Base64.encode(JSON.stringify(apiItem))
      loading.value = true
      checkApiItem({ data: data }).then(response => {
        originFieldItem.jsonFields = response.data.jsonFields
      })
      loading.value = false
    } else {
      return false
    }
  })
}

const reqOptions = [
  { id: 'GET', label: 'GET' },
  { id: 'POST', label: 'POST' }
]

const isUseJsonPath = [
  { id: true, label: t('common.yes') },
  { id: false, label: t('common.no') }
]

const fieldOptions = [
  { label: t('dataset.text'), value: 0 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  }
]
const disabledNext = ref(false)
const saveItem = () => {
  if (apiItem.fields.length === 0) {
    ElMessage.warning(t('datasource.api_field_not_empty'))
    return
  }
  for (let i = 0; i < apiItem.fields.length - 1; i++) {
    for (let j = i + 1; j < apiItem.fields.length; j++) {
      if (apiItem.fields[i].name === apiItem.fields[j].name) {
        ElMessage.warning(apiItem.fields[i].name + ', ' + t('datasource.has_repeat_field_name'))
        return
      }
    }
  }
  returnAPIItem('returnItem', cloneDeep(apiItem))
  edit_api_item.value = false
}
const before = () => {
  active.value -= 1
}
const next = () => {
  checkApiItem({ data: Base64.encode(JSON.stringify(apiItem)) }).then(response => {
    apiItem.jsonFields = response.data.jsonFields
    apiItem.fields = []
    handleFiledChange(apiItem)
    previewData()
  })
  active.value += 1
}
const closeEditItem = () => {
  edit_api_item.value = false
}

const disabledByChildren = item => {
  if (item.hasOwnProperty('children') && item.children.length > 0) {
    return true
  } else {
    return false
  }
}
const previewData = () => {
  showEmpty.value = false
  const data = []
  const columnTmp = []
  let maxPreviewNum = 0
  for (let j = 0; j < apiItem.fields.length; j++) {
    if (apiItem.fields[j].value && apiItem.fields[j].value.length > maxPreviewNum) {
      maxPreviewNum = apiItem.fields[j].value.length
    }
  }
  for (let i = 0; i < maxPreviewNum; i++) {
    data.push({})
  }
  for (let i = 0; i < apiItem.fields.length; i++) {
    for (let j = 0; j < apiItem.fields[i].value.length; j++) {
      data[j][apiItem.fields[i].name] = apiItem.fields[i].value[j]
      data[j]['id'] = apiItem.fields[i].name
    }
    columnTmp.push({
      key: apiItem.fields[i].name,
      dataKey: apiItem.fields[i].name,
      title: apiItem.fields[i].name,
      width: 150
    })
  }
  tableData.value = data
  columns.value = columnTmp
  showEmpty.value = apiItem.fields.length === 0
}

const handleCheckChange = (apiItem, node) => {
  if (node.children?.length) {
    node.children.forEach(item => {
      item.checked = node.checked
      handleCheckChange(apiItem, item)
    })
  }
}

const handleFiledChange = apiItem => {
  for (var i = 0; i < apiItem.jsonFields.length; i++) {
    if (apiItem.jsonFields[i].checked && apiItem.jsonFields[i].children === undefined) {
      apiItem.fields.push(apiItem.jsonFields[i])
    }
    if (apiItem.jsonFields[i].children !== undefined) {
      handleFiledChange2(apiItem, apiItem.jsonFields[i].children)
    }
  }
}
const handleFiledChange2 = (apiItem, jsonFields) => {
  for (var i = 0; i < jsonFields.length; i++) {
    if (jsonFields[i].checked && jsonFields[i].children === undefined) {
      for (var j = 0; j < apiItem.fields.length; j++) {
        if (apiItem.fields[j].name === jsonFields[i].name) {
          jsonFields[i].checked = false
          nextTick(() => {
            jsonFields[i].checked = false
          })
          errMsg.push(jsonFields[i].name)
        }
      }
      apiItem.fields.push(jsonFields[i])
    }
    if (jsonFields[i].children?.length) {
      handleFiledChange2(apiItem, jsonFields[i].children)
    }
  }
}

const handleCheckAllChange = row => {
  errMsg = []
  handleCheckChange(apiItem, row)
  apiItem.fields = []
  handleFiledChange(apiItem)
  previewData()

  if (errMsg.length) {
    ElMessage.error([...new Set(errMsg)].join(',') + ', ' + t('datasource.has_repeat_field_name'))
  }
}
const changeId = (val: string) => {
  apiItem.request.body.typeChange = val
}

const returnAPIItem = defineEmits(['returnItem'])

defineExpose({
  initApiItem
})
</script>

<template>
  <el-drawer
    :title="t(api_table_title)"
    v-model="edit_api_item"
    custom-class="api-datasource-drawer"
    size="840px"
    :before-close="closeEditItem"
    direction="rtl"
  >
    <div class="flex-center">
      <el-steps :active="active" align-center>
        <el-step>
          <template #icon>
            <div class="step-icon">
              <span class="icon">
                {{ active <= 0 ? '1' : '' }}
              </span>
              <span class="title">{{ t('datasource.api_step_1') }}</span>
            </div>
          </template>
        </el-step>
        <el-step>
          <template #icon>
            <div class="step-icon">
              <span class="icon">
                {{ active <= 1 ? '2' : '' }}
              </span>
              <span class="title">{{ t('datasource.api_step_2') }}</span>
            </div>
          </template>
        </el-step>
      </el-steps>
    </div>

    <el-row v-show="active === 0">
      <el-form
        ref="apiItemBasicInfo"
        :model="apiItem"
        label-position="top"
        label-width="100px"
        require-asterisk-position="right"
        :rules="rule"
      >
        <div class="title-form_primary">
          <span>{{ t('datasource.base_info') }}</span>
        </div>
        <el-form-item :label="t('common.name')" prop="name">
          <el-input
            v-model="apiItem.name"
            :placeholder="t('common.input_name')"
            autocomplete="off"
          />
        </el-form-item>

        <el-form-item :label="t('datasource.request')" prop="url">
          <el-input
            v-model="apiItem.url"
            :placeholder="t('datasource.path_all_info')"
            class="input-with-select"
          >
            <template #prepend>
              <el-select v-model="apiItem.method">
                <el-option
                  v-for="item in reqOptions"
                  :key="item.id"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </template>
          </el-input>
        </el-form-item>

        <div v-loading="loading">
          <div class="title-form_primary mr40">
            <span>{{ t('datasource.req_param') }}</span>
          </div>
          <!-- HTTP 请求参数 -->
          <el-form-item>
            <api-http-request-form
              v-if="edit_api_item"
              :request="apiItem.request"
              @changeId="changeId"
            />
          </el-form-item>
        </div>

        <el-form-item :label="t('datasource.isUseJsonPath')">
          <el-input
            :disabled="!apiItem.useJsonPath"
            v-model="apiItem.jsonPath"
            :placeholder="t('datasource.jsonpath_info')"
            class="input-with-select"
          >
            <template #prepend>
              <el-select v-model="apiItem.useJsonPath" style="width: 100px">
                <el-option
                  v-for="item in isUseJsonPath"
                  :key="item.label"
                  :label="item.label"
                  :value="item.id"
                />
              </el-select>
            </template>
            <template #append>
              <el-button :disabled="!apiItem.useJsonPath" @click="showApiData"
                >{{ t('datasource.show_api_data') }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>

        <div class="title-form_primary" v-show="apiItem.useJsonPath">
          <span>{{ t('datasource.column_info') }}</span>
        </div>
        <div class="table-container de-svg-in-table" v-show="apiItem.useJsonPath">
          <el-table :data="originFieldItem.jsonFields" style="width: 100%" row-key="jsonPath">
            <el-table-column
              class-name="checkbox-table"
              prop="originName"
              :label="t('datasource.parse_filed')"
              show-overflow-tooltip
            >
              <template #default="scope">
                {{ scope.row.originName }}
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-form>
    </el-row>
    <el-row v-show="active === 1">
      <el-form
        style="width: 100%"
        ref="apiItemForm"
        :model="apiItem"
        label-position="top"
        label-width="100px"
        :rules="rule"
      >
        <div class="title-form_primary">
          <span>{{ t('datasource.column_info') }}</span>
        </div>
        <div class="table-container de-svg-in-table">
          <el-table :data="apiItem.jsonFields" style="width: 100%" row-key="jsonPath">
            <el-table-column
              class-name="checkbox-table"
              prop="originName"
              :label="t('datasource.parse_filed')"
              :show-overflow-tooltip="true"
              width="255"
            >
              <template #default="scope">
                <el-checkbox
                  :key="scope.row.jsonPath"
                  v-model="scope.row.checked"
                  :disabled="apiItem.useJsonPath"
                  @change="handleCheckAllChange(scope.row)"
                >
                  {{ scope.row.originName }}
                </el-checkbox>
              </template>
            </el-table-column>
            <el-table-column prop="name" :label="t('datasource.field_rename')">
              <template #default="scope">
                <el-input
                  v-model="scope.row.name"
                  :disabled="disabledByChildren(scope.row)"
                  text
                  @change="previewData()"
                />
              </template>
            </el-table-column>

            <el-table-column prop="deExtractType" :label="t('datasource.field_type')">
              <template #default="scope">
                <el-select
                  v-model="scope.row.deExtractType"
                  :disabled="disabledByChildren(scope.row)"
                  class="select-type"
                  style="display: inline-block; width: 120px"
                >
                  <template #prefix>
                    <el-icon>
                      <Icon
                        :name="`field_${fieldType(scope.row.deType)}`"
                        :className="`field-icon-${fieldType(scope.row.deType)}`"
                      ></Icon>
                    </el-icon>
                  </template>
                  <el-option
                    v-for="item in fieldOptions"
                    :key="item.value"
                    :label="item.label"
                    :value="item.value"
                  >
                    <span style="float: left">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType(scope.row.deType)}`"
                          :className="`field-icon-${fieldType(scope.row.deType)}`"
                        ></Icon>
                      </el-icon>
                    </span>
                    <span style="float: left; font-size: 12px; color: #8492a6">{{
                      item.label
                    }}</span>
                  </el-option>
                </el-select>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="title-form_primary">
          <span>{{ t('datasource.data_preview') }}</span>
        </div>
        <div class="info-table">
          <empty-background
            v-if="showEmpty"
            description="暂无数据，请在数据结构勾选字段"
            img-type="select"
          />
          <el-auto-resizer v-else>
            <template #default="{ height, width }">
              <el-table-v2
                :columns="columns"
                header-class="header-cell"
                :data="tableData"
                :width="width"
                :height="height"
                fixed
              />
            </template>
          </el-auto-resizer>
        </div>
      </el-form>
    </el-row>
    <template #footer>
      <el-button @click="closeEditItem">{{ t('common.cancel') }}</el-button>
      <el-button v-show="active === 0" type="primary" :disabled="disabledNext" @click="next"
        >{{ t('common.next') }}
      </el-button>
      <el-button v-show="active === 1" type="primary" @click="before"
        >{{ t('common.prev') }}
      </el-button>
      <el-button v-show="active === 1" type="primary" @click="saveItem"
        >{{ t('common.save') }}
      </el-button>
    </template>
  </el-drawer>
</template>

<style lang="less">
.api-datasource-drawer {
  .select-type {
    .ed-select__prefix--light {
      border-right: none;
      padding: 0;
      font-size: 16px;
    }
  }
  .ed-drawer__body {
    padding: 24px 24px 80px 24px !important;
  }
  .flex-center {
    .ed-steps {
      width: 630px;
    }
    .ed-step.is-center .ed-step__line {
      width: 208px;
      right: 104px;
      z-index: 5;
      left: calc(100% - 104px);
    }

    .ed-step__icon.is-icon {
      width: auto;
      position: relative;
      z-index: 0;
    }

    .ed-step__head.is-finish::after {
      right: calc(100% - 133px);
    }

    .ed-step__head.is-process .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          background: #3370ff;
        }
      }
    }

    .ed-step__head.is-finish .ed-step__icon {
      background-color: transparent;
      .step-icon {
        .icon {
          border: 1px solid #3370ff;
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
        border-radius: 50%;
        line-height: 28px;
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

  .ed-form {
    width: 100%;
  }

  .title-form_primary {
    margin: 16px 0;
  }
  .input-with-select {
    .ed-input-group__prepend {
      background-color: #fff;
      padding: 0 20px;
      .ed-select {
        width: 84px !important;
      }
    }
  }
  .table-container {
    padding: 20px;
    border: 1px solid #dee0e3;
  }

  .info-table {
    max-height: 300px;
    height: 200px;
    .ed-table-v2__header-cell {
      background-color: #f5f6f7;
    }
  }
}
</style>
