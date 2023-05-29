<script lang="tsx" setup>
import { ref, reactive, shallowRef, nextTick } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElIcon, ElMessage } from 'element-plus-secondary'
import ApiHttpRequestForm from './ApiHttpRequestForm.vue'
import { Icon } from '@/components/icon-custom'
import type { ApiRequest } from './ApiHttpRequestForm.vue'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { checkApiItem } from '../../../../../api/datasource.ts'
export interface Field {
  name: string
  value: Array<{}>
  checked: boolean
  children?: Array<{}>
}
export interface ApiItem {
  status: string
  name: string
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

const apiItem = reactive<ApiItem>({
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
const api_table_title = ref('')
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
  active.value = 1
}

const showApiData = () => {
  apiItemBasicInfo.value.validate(valid => {
    if (valid) {
      apiItem.showApiStructure = true
      const data = Base64.encode(JSON.stringify(apiItem))
      loading.value = true
      checkApiItem({ data: data }).then(response => {})
      loading.value = false
      // res.data.jsonFields.forEach(((item) => {
      //       item.checked = false
      //     }))
      //     originFieldItem.jsonFields = res.data.jsonFields
      //     loading.value = false
    } else {
      return false
    }
  })
}
// const generateColumns = (arr: Field[]) =>
//   arr.map(ele => ({
//     key: ele.fieldShortName,
//     deType: ele.deType,
//     dataKey: ele.fieldShortName,
//     title: ele.name,
//     width: 150,
//     headerCellRenderer: ({ column }) => (
//       <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
//         <ElIcon>
//           <Icon
//             name={`field_${fieldType(column.deType)}`}
//             className={`field-icon-${fieldType(column.deType)}`}
//           ></Icon>
//         </ElIcon>
//         {column.title}
//       </div>
//     )
//   }))
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
  returnAPIItem('returnItem', apiItem)
  edit_api_item.value = false
}
const before = () => {
  active.value -= 1
}
const next = () => {
  checkApiItem({ data: Base64.encode(JSON.stringify(apiItem)) }).then(response => {
    apiItem.jsonFields = response.jsonFields
  })
  active.value += 1
}
const closeEditItem = () => {
  edit_api_item.value = false
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
    nextTick(() => {
      tableData.value = data
    })
    nextTick(() => {
      columns.value = columnTmp
    })
  }
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
    // $message.error(
    //   [...new Set(errMsg)].join(',') +
    //            ', ' +
    //            i18n.t('datasource.has_repeat_field_name')
    // )
  }
}
const changeId = (val: string) => {
  apiItem.request.body.typeChange = val
}

const bodyChange = val => {
  apiItem.request.body = val
}

const headersChange = val => {
  apiItem.request.headers = val
}

const changeParameters = val => {
  apiItem.request.arguments = val
}
const authConfigChange = val => {
  apiItem.request.authManager = val
}

const kvsChange = val => {
  apiItem.request.body.kvs = val
}

const returnAPIItem = defineEmits(['returnItem'])

defineExpose({
  initApiItem
})
</script>

<template>
  <el-drawer
    :title="api_table_title"
    v-model="edit_api_item"
    custom-class="api-datasource-drawer"
    size="840px"
    :before-close="closeEditItem"
    direction="rtl"
  >
    <div class="flex-center">
      <el-steps :active="active" align-center :space="144">
        <el-step :title="t('datasource.api_step_1')" />
        <el-step :title="t('datasource.api_step_2')" />
      </el-steps>
    </div>

    <el-row v-show="active === 1">
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
              <el-select v-model="apiItem.method" style="width: 100px">
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
              @authConfigChange="authConfigChange"
              @bodyChange="bodyChange"
              @headersChange="headersChange"
              @kvsChange="kvsChange"
              @changeParameters="changeParameters"
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
          <el-table
            ref="apiItemTable"
            :data="originFieldItem.jsonFields"
            style="width: 100%"
            row-key="jsonPath"
          >
            <el-table-column
              class-name="checkbox-table"
              prop="originName"
              :label="t('dataset.parse_filed')"
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
    <el-row v-show="active === 2">
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
          <el-table
            ref="apiItemTable"
            :data="apiItem.jsonFields"
            style="width: 100%"
            row-key="jsonPath"
          >
            <el-table-column
              class-name="checkbox-table"
              prop="originName"
              :label="t('dataset.parse_filed')"
              :show-overflow-tooltip="true"
              width="255"
            >
              <template #default="scope">
                <el-checkbox
                  :key="scope.row.jsonPath"
                  v-model="scope.row.checked"
                  :disabled="scope.row.disabled || apiItem.useJsonPath"
                  @change="handleCheckAllChange(scope.row)"
                >
                  {{ scope.row.originName }}
                </el-checkbox>
              </template>
            </el-table-column>
            <el-table-column prop="name" :label="t('dataset.field_rename')">
              <template #default="scope">
                <el-input
                  v-model="scope.row.name"
                  :disabled="scope.row.children"
                  text
                  @change="previewData()"
                />
              </template>
            </el-table-column>

            <el-table-column prop="deExtractType" :label="t('dataset.field_type')">
              <template #default="scope">
                <el-select
                  v-model="scope.row.deExtractType"
                  :disabled="scope.row.children"
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
        <empty-background
          v-if="showEmpty"
          description="暂无数据，请在数据结构勾选字段"
          img-type="select"
        />

        <div class="info-table" v-else>
          <el-auto-resizer>
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
      <el-button v-show="active === 1" type="primary" :disabled="disabledNext" @click="next"
        >{{ t('common.next') }}
      </el-button>
      <el-button v-show="active === 2" type="primary" @click="before"
        >{{ t('common.prev') }}
      </el-button>
      <el-button v-show="active === 2" type="primary" @click="saveItem"
        >{{ t('common.save') }}
      </el-button>
    </template>
  </el-drawer>
</template>

<style lang="less">
.api-datasource-drawer {
  .input-with-select {
    .el-input-group__prepend {
      background-color: #fff;
      border-color: #bbbfc4;
      .el-select {
        width: 84px !important;
      }
    }
  }
  .table-container {
    padding: 20px;
    border: 1px solid #dee0e3;
  }
}
</style>
