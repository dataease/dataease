<script lang="tsx" setup>
import { Icon } from '@/components/icon-custom'
import { ElIcon, ElDropdown, ElDropdownItem, ElDropdownMenu } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, nextTick, shallowRef, reactive, computed, toRefs } from 'vue'
import { ElTree, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { save } from '@/api/datasource'
import type { Action } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
import { useUserStoreWithOut } from '@/store/modules/user'
import ExcelInfo from '../ExcelInfo.vue'
import SheetTabs from '../SheetTabs.vue'
import { cloneDeep } from 'lodash-es'

export interface Param {
  editType: number
  pid?: string
  type?: string
  id: number
  table: {
    name: string
  }
}

export interface Field {
  accuracy: number
  originName: string
  fieldSize: number
  fieldType: string
  name: string
}
const props = defineProps({
  param: {
    required: false,
    default() {
      return reactive<{
        id: number
        name: string
        desc: string
        type: string
        editType: number
      }>({
        id: 0,
        name: '',
        desc: '',
        type: 'Excel',
        editType: 0
      })
    },
    type: Object
  }
})

const { param } = toRefs(props)

const { t } = useI18n()

const token = useUserStoreWithOut().getToken

const baseUrl = ''
const headers = {
  'X-DE-TOKEN': token,
  'Accept-Language': 'zh'
}

const loading = ref(false)
const columns = shallowRef([])
const fieldOptions = [
  { label: t('dataset.text'), value: 'TEXT' },
  { label: t('dataset.time'), value: 'DATETIME' },
  { label: t('dataset.value'), value: 'LONG' },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 'DOUBLE'
  }
]

const defaultSheetObj = {
  tableName: ' ',
  sheetExcelId: '',
  fields: [],
  jsonArray: [],
  nameExist: false,
  empty: '',
  overLength: false
}
const sheetObj = reactive(cloneDeep(defaultSheetObj))
const state = reactive({
  excelData: [],
  defaultExpandedKeys: [],
  defaultCheckedKeys: [],
  fileList: [],
  sheets: []
})

const sheetFile = computed(() => {
  const [sheet = {}] = state.excelData
  return {
    name: sheet.excelLabel,
    size: sheet.excelLabel
  }
})

const uploading = ref(false)
const tree = ref<InstanceType<typeof ElTree>>()

const fieldType = {
  TEXT: 'text',
  DATETIME: 'time',
  LONG: 'value',
  DOUBLE: 'value'
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    fieldType: ele.fieldType,
    dataKey: ele.originName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElDropdown
          placement="bottom-start"
          trigger="click"
          key={column.fieldName}
          onCommand={type => handleCommand(type, column)}
        >
          {{
            default: () => {
              return (
                <ElIcon style={{ marginRight: '6px' }}>
                  <Icon
                    name={`field_${fieldType[column.fieldType]}`}
                    className={`field-icon-${fieldType[column.fieldType]}`}
                  ></Icon>
                </ElIcon>
              )
            },
            dropdown: () => {
              return (
                <ElDropdownMenu>
                  {fieldOptions.map(ele => {
                    return (
                      <ElDropdownItem key={ele.value} command={ele.value}>
                        <ElIcon>
                          <Icon
                            name={`field_${fieldType[ele.value]}`}
                            className={`field-icon-${fieldType[ele.value]}`}
                          ></Icon>
                        </ElIcon>
                        <span>{ele.label}</span>
                      </ElDropdownItem>
                    )
                  })}
                </ElDropdownMenu>
              )
            }
          }}
        </ElDropdown>
        {column.title}
      </div>
    )
  }))

const handleNodeClick = data => {
  if (data.sheet) {
    Object.assign(sheetObj, data)
    columns.value = generateColumns(data.fields)
  }
}
const handleCommand = (type, field) => {
  sheetObj.fields.some(ele => {
    if (ele.fieldName === field.dataKey) {
      ele.fieldType = type
      return true
    }
  })
  columns.value.some(ele => {
    if (ele.dataKey === field.dataKey) {
      ele.fieldType = type
      return true
    }
  })
  changeDatasetName()
}

const changeDatasetName = () => {
  for (let i = 0; i < state.excelData.length; i++) {
    if (state.excelData[i].excelId === sheetObj.sheetExcelId) {
      for (let j = 0; j < state.excelData[i].sheets.length; j++) {
        if (state.excelData[i].sheets[j].excelId === sheetObj.sheetId) {
          state.excelData[i].sheets[j] = sheetObj
        }
      }
    }
  }
}

const beforeUpload = () => {
  uploading.value = true
}

const handleTabClick = tab => {
  activeTab.value = tab.value
  const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === tab.value)
  handleNodeClick(sheet)
}

const uploadFail = response => {
  let myError = response.toString()
  myError = myError.replace('Error: ', '')
}
const tabList = shallowRef([])
const activeTab = ref('')

const handleExcelDel = () => {
  state.excelData = []
  activeTab.value = ''
  tabList.value = []
  Object.assign(sheetObj, cloneDeep(defaultSheetObj))
}

const uploadSuccess = (response, _, fileList) => {
  if (response.code !== 0) {
    ElMessage.warning(response.msg)
    return
  }
  uploading.value = false
  if (!param.value.name) {
    param.value.name = response.data.excelLabel
  }
  tabList.value = response.data.sheets.map(ele => {
    const { sheetId, tableName } = ele
    return {
      value: sheetId,
      label: tableName
    }
  })
  state.excelData.push(response.data)
  const [sheet] = tabList.value

  sheet && handleTabClick(sheet)
  // state.defaultExpandedKeys.push(response.data.id)
  // state.defaultCheckedKeys.push(response.data.sheets[0].id)
  nextTick(() => {
    // tree.value.setCheckedKeys(state.defaultCheckedKeys)
  })
  state.fileList = fileList
  // if (response.headers[RefreshTokenKey]) {
  // const refreshToken = response.headers[RefreshTokenKey]
  // setToken(refreshToken)
  // store.dispatch('user/refreshToken', refreshToken)
  // }
}
// uploadSuccess({ data: mockData, headers: {} }, '', [])

const saveExcelDs = (params, cb) => {
  let validate = true
  let selectedSheet = []
  let sheetFileMd5 = []
  let effectExtField = false
  let changeFiled = false
  let selectNode = tree.value.getCheckedNodes()
  if (!props.param.id && selectNode.some(ele => ele.nameExist)) {
    ElMessage({
      message: t('deDataset.cannot_be_duplicate'),
      type: 'error'
    })
    return
  }
  for (let i = 0; i < selectNode.length; i++) {
    if (selectNode[i].sheet) {
      if (!selectNode[i].tableName || selectNode[i].tableName === '') {
        validate = false
        ElMessage({
          message: t('dataset.pls_input_name'),
          type: 'error'
        })
        return
      }
      if (selectNode[i].tableName.length > 50 && !props.param.id) {
        validate = false
        ElMessage({
          message: t('dataset.char_can_not_more_50'),
          type: 'error'
        })
        return
      }
      if (selectNode[i].effectExtField) {
        effectExtField = true
      }
      if (selectNode[i].changeFiled) {
        changeFiled = true
      }
      selectedSheet.push(selectNode[i])
      sheetFileMd5.push(selectNode[i].fieldsMd5)
    }
  }
  if (!selectedSheet.length) {
    ElMessage({
      message: t('dataset.ple_select_excel'),
      type: 'error'
    })
    return
  }
  if (!validate) {
    return
  }

  let table = {}
  if (!props.param.id) {
    table = {
      id: props.param.id,
      name: props.param.name,
      type: 'Excel',
      sheets: selectedSheet,
      editType: 0
    }
  } else {
    table = {
      id: props.param.id,
      name: props.param.name,
      type: 'Excel',
      sheets: selectedSheet,
      editType: props.param.editType ? props.param.editType : 0
    }
  }

  if (props.param.editType === 0 && props.param.id && (effectExtField || changeFiled)) {
    ElMessageBox.confirm(t('deDataset.replace_the_data'), {
      confirmButtonText: t('common.confirm'),
      tip: '替换可能会影响自定义数据集、关联数据集、仪表板等，是否替换？',
      cancelButtonText: 'Cancel',
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false,
      callback: (action: Action) => {
        if (action === 'confirm') {
          saveExcelData(sheetFileMd5, table, params, cb)
        }
      }
    })
  } else {
    saveExcelData(sheetFileMd5, table, params, cb)
  }
}

const saveExcelData = (sheetFileMd5, table, params, cb) => {
  table.configuration = Base64.encode(JSON.stringify(table.sheets))
  if (new Set(sheetFileMd5).size !== sheetFileMd5.length && !props.param.id) {
    ElMessageBox.confirm(t('dataset.merge_title'), {
      confirmButtonText: t('dataset.merge'),
      tip: t('dataset.task.excel_replace_msg'),
      cancelButtonText: t('dataset.no_merge'),
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      callback: (action: Action) => {
        if (action === 'close') return
        loading.value = true
        table.mergeSheet = action === 'confirm'
        if (action === 'confirm') {
          save(table)
            .then(() => {
              ElMessage({
                message: t('dataset.set_saved_successfully'),
                type: 'success'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }

        if (action === 'cancel') {
          save(table)
            .then(() => {
              ElMessage({
                message: t('dataset.set_saved_successfully'),
                type: 'success'
              })
            })
            .finally(() => {
              loading.value = false
            })
        }
      }
    })
  } else {
    if (loading.value) return
    loading.value = true
    save(table)
      .then(() => {
        ElMessage({
          message: t('dataset.set_saved_successfully'),
          type: 'success'
        })
      })
      .finally(() => {
        loading.value = false
      })
  }
}

defineExpose({
  saveExcelDs
})
</script>

<template>
  <div class="excel-detail">
    <div class="detail-operate">文件</div>
    <div class="detail-inner">
      <el-form require-asterisk-position="right" :model="param" label-position="top">
        <el-form-item
          v-if="sheetFile.name"
          prop="name"
          label="文件"
          :rules="[
            {
              required: true
            }
          ]"
        >
          <ExcelInfo
            @del="handleExcelDel"
            show-del
            :name="sheetFile.name"
            :size="sheetFile.size"
          ></ExcelInfo>
          <el-upload
            :action="baseUrl + 'api/datasource/uploadFile'"
            :multiple="false"
            :show-file-list="false"
            :file-list="state.fileList"
            :data="param"
            accept=".xls,.xlsx,.csv"
            :before-upload="beforeUpload"
            :on-success="uploadSuccess"
            :on-error="uploadFail"
            name="file"
            :headers="headers"
          >
            <template #trigger>
              <el-button text>重新上传</el-button>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item
          v-else
          prop="name"
          label="文件"
          :rules="[
            {
              required: true
            }
          ]"
        >
          <el-upload
            :action="baseUrl + 'de2api/datasource/uploadFile'"
            :multiple="false"
            :disabled="!!param.id"
            :show-file-list="false"
            :file-list="state.fileList"
            :data="param"
            accept=".xls,.xlsx,.csv"
            :before-upload="beforeUpload"
            :on-success="uploadSuccess"
            :on-error="uploadFail"
            name="file"
            :headers="headers"
          >
            <template #trigger>
              <el-button secondary>
                <template #icon>
                  <Icon name="icon_upload_outlined"></Icon>
                </template>
                {{ t('dataset.upload_file') }}
              </el-button>
            </template>
          </el-upload>
          <p style="width: 100%">仅支持xlsx、xls、csv格式的文件</p>
        </el-form-item>
        <el-form-item
          prop="name"
          :rules="[
            {
              required: true,
              message: t('common.please_input') + t('datasource.datasource') + t('common.name')
            }
          ]"
          :label="t('visualization.custom') + t('datasource.datasource') + t('common.name')"
        >
          <el-input
            v-model="param.name"
            :placeholder="t('common.please_input') + t('datasource.datasource') + t('common.name')"
          />
        </el-form-item>
      </el-form>
      <template v-if="activeTab">
        <div class="title-form_primary">
          {{ t('chart.data_preview') }}
        </div>
        <SheetTabs
          :activeTab="activeTab"
          @tab-click="handleTabClick"
          :tab-list="tabList"
        ></SheetTabs>

        <div class="info-table">
          <el-auto-resizer>
            <template #default="{ height, width }">
              <el-table-v2
                :columns="columns"
                header-class="excel-header-cell"
                :data="sheetObj.jsonArray"
                :width="width"
                :height="height"
                fixed
              />
            </template>
          </el-auto-resizer>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="less">
.excel-detail {
  display: flex;
  justify-content: center;
  flex-direction: column;
  align-items: center;
  width: calc(100% + 48px);
  margin: -8px -24px 0 -24px;
  .ed-form-item {
    margin-bottom: 16px;
  }

  .detail-operate {
    height: 56px;
    padding: 16px 24px;
    font-size: 16px;
    font-weight: 500;
    width: 100%;
    border-bottom: 1px solid rgba(31, 35, 41, 0.15);
  }
  .detail-inner {
    width: 800px;
    padding-top: 24px;

    .title-form_primary {
      margin: 16px 0;
    }

    .info-table {
      height: 400px;
    }
  }
}
</style>
