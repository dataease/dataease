<script lang="tsx" setup>
import icon_upload_outlined from '@/assets/svg/icon_upload_outlined.svg'
import { Icon } from '@/components/icon-custom'
import { ElIcon } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  ref,
  shallowRef,
  reactive,
  h,
  computed,
  toRefs,
  onMounted,
  onBeforeUnmount,
  nextTick
} from 'vue'
import { fieldType as fieldTypeLowercase } from '@/utils/attr'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { save, update } from '@/api/datasource'
import type { Action } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
import ExcelInfo from '../ExcelInfo.vue'
import SheetTabs from '../SheetTabs.vue'
import { cloneDeep, debounce } from 'lodash-es'
import { uploadFile } from '@/api/datasource'
import { useEmitt } from '@/hooks/web/useEmitt'
import { iconFieldMap } from '@/components/icon-group/field-list'
import { boolean } from 'mathjs'

export interface Param {
  editType: number
  pid?: string
  type?: string
  id?: string
  name?: string
  creator?: string
  isPlugin?: boolean
  staticMap?: any
  configuration?: {}
}

export interface Field {
  accuracy: number
  originName: string
  fieldSize: number
  fieldType: string
  name: string
  deExtractType: number
  checked: boolean
  primaryKey: boolean
  length: number
}
const props = defineProps({
  param: {
    required: false,
    default() {
      return reactive<{
        id: string
        name: string
        desc: string
        type: string
        editType: number
      }>({
        id: '0',
        name: '',
        desc: '',
        type: 'Excel',
        editType: 0
      })
    },
    type: Object
  },
  isSupportSetKey: {
    type: boolean,
    required: true
  }
})

const { param, isSupportSetKey } = toRefs(props)

const { t } = useI18n()
const { emitter } = useEmitt()

const loading = ref(false)
const columns = shallowRef([])
const multipleSelection = shallowRef([])
const multipleTable = ref()

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
  fileList: null,
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

const fieldType = {
  TEXT: 'text',
  DATETIME: 'time',
  LONG: 'value',
  DOUBLE: 'value'
}

const fieldTypeToStr = {
  0: 'TEXT',
  2: 'LONG',
  3: 'DOUBLE'
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    fieldType: ele.fieldType,
    deExtractType: ele.deExtractType,
    dataKey: ele.originName,
    title: ele.name,
    checked: ele.checked,
    primaryKey: ele.primaryKey,
    length: ele.length,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center icon">
        <ElIcon>
          <Icon>
            {h(iconFieldMap[fieldType[column.fieldType]], {
              class: `svg-icon field-icon-${fieldType[column.fieldType]}`
            })}
          </Icon>
        </ElIcon>
        <span class="ellipsis" title={column.title} style={{ width: '100px' }}>
          {column.title}
        </span>
      </div>
    )
  }))

const handleNodeClick = data => {
  if (data.sheet) {
    Object.assign(sheetObj, data)
    columns.value = generateColumns(data.fields)
    multipleSelection.value = columns.value.filter(item => item.checked)
    currentMode.value = 'preview'
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
  state.excelData = []
  activeTab.value = ''
  tabList.value = []
  Object.assign(sheetObj, cloneDeep(defaultSheetObj))
  let myError = response.toString()
  myError.replace('Error: ', '')
}
const tabList = shallowRef([])
const activeTab = ref('')

const handleExcelDel = () => {
  state.excelData = []
  activeTab.value = ''
  tabList.value = []
  Object.assign(sheetObj, cloneDeep(defaultSheetObj))
}

const uploadSuccess = response => {
  if (!response) {
    return
  }
  if (response?.code !== 0) {
    state.excelData = []
    activeTab.value = ''
    tabList.value = []
    Object.assign(sheetObj, cloneDeep(defaultSheetObj))
    ElMessage.warning(response.msg)
    return
  }
  columns.value = []
  Object.assign(sheetObj, cloneDeep(defaultSheetObj))
  multipleSelection.value = []
  uploading.value = false
  if (!param.value.name) {
    param.value.name = response.data.excelLabel
  }
  tabList.value = response.data.sheets.map(ele => {
    const { sheetId, tableName, newSheet } = ele
    return {
      value: sheetId,
      label: tableName,
      newSheet: newSheet
    }
  })
  state.excelData = [response.data]
  const [sheet] = tabList.value

  sheet && handleTabClick(sheet)
}
const saveExcelDs = (params, successCb, finallyCb) => {
  let validate = true
  let selectedSheet = []
  let sheetFileMd5 = []
  let effectExtField = false
  let changeFiled = false
  let selectNode = state.excelData[0]?.sheets
  for (let i = 0; i < selectNode.length; i++) {
    if (selectNode[i].sheet) {
      if (selectNode[i].effectExtField) {
        effectExtField = true
      }
      if (selectNode[i].changeFiled) {
        changeFiled = true
      }
      if (selectNode[i].fields.filter(field => field.checked).length == 0) {
        ElMessage({
          message: selectNode[i].excelLabel + t('datasource.api_field_not_empty'),
          type: 'error'
        })
        finallyCb?.()
        return
      }
      for (let j = 0; j < selectNode[i].fields.length; j++) {
        if (
          selectNode[i].fields[j].checked &&
          selectNode[i].fields[j].primaryKey &&
          !selectNode[i].fields[j].length &&
          selectNode[i].fields[j].deExtractType === 0
        ) {
          ElMessage({
            message:
              t('datasource.primary_key_length') +
              selectNode[i].excelLabel +
              ': ' +
              selectNode[i].fields[j].name,
            type: 'error'
          })
          finallyCb?.()
          return
        }
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
    finallyCb?.()
    return
  }
  if (!validate) {
    finallyCb?.()
    return
  }

  let table = {}
  if (params) {
    param.value.name = params.name
  }
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
      confirmButtonText: t('dataset.confirm'),
      tip: t('data_source.to_replace_it'),
      cancelButtonText: 'Cancel',
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false,
      callback: (action: Action) => {
        if (action === 'confirm') {
          saveExcelData(sheetFileMd5, table, params, successCb, finallyCb)
        }
      }
    })
  } else {
    saveExcelData(sheetFileMd5, table, params, successCb, finallyCb)
  }
}

const saveExcelData = (sheetFileMd5, table, params, successCb, finallyCb) => {
  for (let i = 0; i < table.sheets.length; i++) {
    table.sheets[i].data = []
    table.sheets[i].jsonArray = []
  }
  table.configuration = Base64.encode(JSON.stringify(table.sheets))
  let method = save
  if (!table.id || table.id === '0') {
    delete table.id
    table.pid = params.pid
  } else {
    method = update
  }
  if (loading.value) return
  loading.value = true
  method(table)
    .then(res => {
      emitter.emit('showFinishPage', res)
      successCb?.()
      ElMessage({
        message: t('commons.save_success'),
        type: 'success'
      })
    })
    .finally(() => {
      finallyCb?.()
      loading.value = false
    })
}

const onChange = file => {
  state.fileList = file
}
const isResize = ref(true)

const handleResize = debounce(() => {
  isResize.value = false
  nextTick(() => {
    isResize.value = true
  })
}, 500)
onMounted(() => {
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

const upload = ref()
const uploadAgain = ref()

const uploadExcel = () => {
  const formData = new FormData()
  formData.append('file', state.fileList.raw)
  formData.append('type', '')
  formData.append('editType', param.value.editType)
  formData.append('id', param.value.id || 0)
  loading.value = true
  return uploadFile(formData)
    .then(res => {
      upload.value?.clearFiles()
      uploadAgain.value?.clearFiles()
      uploadSuccess(res)
      loading.value = false
    })
    .catch(error => {
      state.excelData = []
      activeTab.value = ''
      tabList.value = []
      Object.assign(sheetObj, cloneDeep(defaultSheetObj))
      if (error.code === 'ECONNABORTED') {
        ElMessage({
          type: 'error',
          message: error.message,
          showClose: true
        })
      }
      loading.value = false
    })
}
const excelForm = ref()
const submitForm = () => {
  return excelForm.value.validate
}

const showName = ref(true)

const appendReplaceExcel = response => {
  showName.value = false
  uploadSuccess(response)
}

const status = ref(false)
const initMultipleTable = ref(false)
const currentMode = ref('preview')

const deExtractTypeChange = item => {
  item.deType = item.deExtractType
  const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === activeTab.value)
  sheet.fields.forEach(row => {
    if (row.originName === item.dataKey) {
      row.deExtractType = item.deExtractType
      row.deType = item.deExtractType
      row.fieldType = fieldTypeToStr[item.deExtractType]
    }
  })
}

const lengthChange = val => {
  const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === activeTab.value)
  sheet.fields.forEach(row => {
    if (row.originName === val.dataKey) {
      row.length = val.length
    }
  })
}
const primaryKeyChange = val => {
  const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === activeTab.value)
  sheet.fields.forEach(row => {
    if (row.originName === val.dataKey) {
      row.primaryKey = val.primaryKey
    }
  })
}

const fieldOptions = [
  { label: t('dataset.text'), value: 0 },
  { label: t('dataset.value'), value: 2 },
  {
    label: t('dataset.value') + '(' + t('dataset.float') + ')',
    value: 3
  }
]

const handleSelectionChange = val => {
  if (!initMultipleTable.value) {
    multipleSelection.value = val
    multipleSelection.value.forEach(row => {
      row.checked = true
    })
    columns.value.forEach(row => {
      let item
      for (let i = 0; i < multipleSelection.value.length; i++) {
        if (row.dataKey === multipleSelection.value[i].dataKey) {
          item = multipleSelection.value[i]
        }
      }
      if (item) {
        row.checked = item.checked
      } else {
        row.checked = false
      }
    })

    const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === activeTab.value)
    sheet.fields.forEach(row => {
      let item
      for (let i = 0; i < multipleSelection.value.length; i++) {
        if (row.originName === multipleSelection.value[i].dataKey) {
          item = multipleSelection.value[i]
        }
      }
      if (item) {
        row.checked = item.checked
      } else {
        row.checked = false
      }
    })
  }
}

const disabledFieldLength = item => {
  if (!item.checked) {
    return true
  }
  if (item.deExtractType !== 0) {
    return true
  }
}

const changeCurrentMode = val => {
  currentMode.value = val
  if (val === 'select') {
    nextTick(() => {
      initMultipleTable.value = true
      for (let i = 0; i < columns.value.length; i++) {
        if (columns.value[i].checked) {
          multipleTable?.value?.toggleRowSelection(columns.value[i], true)
        }
      }
      initMultipleTable.value = false
    })
  } else {
    const sheet = state.excelData[0]?.sheets.find(ele => ele.sheetId === activeTab.value)
    handleNodeClick(sheet)
  }
}

const uploadStatus = val => {
  status.value = val
}
defineExpose({
  saveExcelDs,
  submitForm,
  sheetFile,
  appendReplaceExcel,
  uploadStatus
})
</script>

<template>
  <div class="excel-detail">
    <div class="detail-inner">
      <el-form
        ref="excelForm"
        require-asterisk-position="right"
        :model="param"
        label-position="top"
        v-loading="loading"
      >
        <el-form-item
          v-if="sheetFile.name"
          prop="id"
          :label="t('data_source.document')"
          key="sheetFile"
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
            action=""
            :multiple="false"
            ref="uploadAgain"
            :show-file-list="false"
            accept=".xls,.xlsx,.csv"
            :before-upload="beforeUpload"
            :on-change="onChange"
            :http-request="uploadExcel"
            :on-error="uploadFail"
            name="file"
          >
            <template #trigger>
              <el-button text>{{ t('data_source.reupload') }}</el-button>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item
          v-else
          prop="id"
          key="sheetId"
          :label="t('data_source.document')"
          :rules="[
            {
              required: true
            }
          ]"
        >
          <el-upload
            :multiple="false"
            action=""
            ref="upload"
            :show-file-list="false"
            accept=".xls,.xlsx,.csv"
            :before-upload="beforeUpload"
            :on-change="onChange"
            :http-request="uploadExcel"
            :on-error="uploadFail"
            name="file"
          >
            <template #trigger>
              <el-button secondary>
                <template #icon>
                  <Icon name="icon_upload_outlined"><icon_upload_outlined class="svg-icon" /></Icon>
                </template>
                {{ t('dataset.upload_file') }}
              </el-button>
            </template>
          </el-upload>
          <p class="upload-tip" style="width: 100%">{{ t('data_source.and_csv_formats') }}</p>
          <div class="ed-form-item__error" v-if="status">
            {{ t('data_source.please_upload_files') }}
          </div>
        </el-form-item>
        <el-form-item
          :class="status && !sheetFile.name && 'error-status'"
          prop="name"
          key="name"
          v-if="showName"
          :rules="[
            {
              required: true,
              message:
                t('common.please_input') +
                t('common.empty') +
                t('datasource.datasource') +
                t('common.empty') +
                t('common.name')
            }
          ]"
          :label="
            t('visualization.custom') +
            t('common.empty') +
            t('datasource.datasource') +
            t('common.empty') +
            t('common.name')
          "
        >
          <el-input
            v-model="param.name"
            :placeholder="
              t('common.please_input') +
              t('common.empty') +
              t('datasource.datasource') +
              t('common.empty') +
              t('common.name')
            "
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

        <div class="table-select_mode" v-if="param.editType === 0">
          <div class="btn-select">
            <el-button
              @click="changeCurrentMode('preview')"
              :class="[currentMode === 'preview' && 'is-active']"
              text
            >
              {{ t('chart.data_preview') }}
            </el-button>
            <el-button
              @click="changeCurrentMode('select')"
              :class="[currentMode === 'select' && 'is-active']"
              text
            >
              {{ t('data_set.field_selection') }}
            </el-button>
          </div>
        </div>
        <div
          class="info-table"
          :class="param.editType === 0 && 'info-table_height'"
          v-if="isResize"
        >
          <el-auto-resizer v-if="currentMode === 'preview'">
            <template #default="{ height, width }">
              <el-table-v2
                :columns="multipleSelection"
                header-class="excel-header-cell"
                :data="sheetObj.jsonArray"
                :width="width"
                :height="height"
                fixed
              />
            </template>
          </el-auto-resizer>
          <el-table
            header-class="header-cell"
            v-else
            ref="multipleTable"
            :data="columns"
            style="width: 100%"
            @selection-change="handleSelectionChange"
          >
            <el-table-column type="selection" width="55" />
            <el-table-column :label="t('data_set.field_name')">
              <template #default="scope">{{ scope.row.title }}</template>
            </el-table-column>

            <el-table-column prop="deExtractType" :label="t('datasource.field_type')">
              <template #default="scope">
                <el-select
                  v-model="scope.row.deExtractType"
                  class="select-type"
                  style="display: inline-block; width: 120px"
                  @change="deExtractTypeChange(scope.row)"
                >
                  <template #prefix>
                    <el-icon>
                      <Icon :className="`field-icon-${fieldTypeLowercase[scope.row.deExtractType]}`"
                        ><component
                          class="svg-icon"
                          :class="`field-icon-${fieldTypeLowercase[scope.row.deExtractType]}`"
                          :is="iconFieldMap[fieldTypeLowercase[scope.row.deExtractType]]"
                        ></component
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
                        <Icon :className="`field-icon-${fieldTypeLowercase[item.value]}`"
                          ><component
                            class="svg-icon"
                            :class="`field-icon-${fieldTypeLowercase[item.value]}`"
                            :is="iconFieldMap[fieldTypeLowercase[item.value]]"
                          ></component
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
            <el-table-column
              prop="length"
              :label="t('datasource.length')"
              v-if="param.editType === 0"
            >
              <template #default="scope">
                <el-input-number
                  :disabled="disabledFieldLength(scope.row)"
                  v-model="scope.row.length"
                  autocomplete="off"
                  step-strictly
                  class="text-left edit-all-line"
                  :min="1"
                  :max="512"
                  :placeholder="t('common.inputText')"
                  controls-position="right"
                  type="number"
                  @change="lengthChange(scope.row)"
                />
              </template>
            </el-table-column>
            <el-table-column
              prop="primaryKey"
              class-name="checkbox-table"
              :label="t('datasource.set_key')"
              width="100"
              v-if="param.editType === 0 && isSupportSetKey"
            >
              <template #default="scope">
                <el-checkbox
                  :key="scope.row.dataKey"
                  v-model="scope.row.primaryKey"
                  :disabled="!scope.row.checked"
                  @change="primaryKeyChange(scope.row)"
                >
                </el-checkbox>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </template>
    </div>
  </div>
</template>

<style lang="less">
.excel-detail {
  display: flex;
  justify-content: center;
  width: calc(100% + 48px);
  margin: -8px -24px 0 -24px;
  .ed-form-item {
    margin-bottom: 16px;
  }

  .table-select_mode {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: #f5f6f7;
    padding: 16px;
    .btn-select {
      min-width: 164px;
      padding: 0 6px;
      height: 32px;
      display: flex;
      align-items: center;
      justify-content: center;
      background: #ffffff;
      border: 1px solid #bbbfc4;
      border-radius: 4px;

      .is-active {
        background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
      }

      .ed-button:not(.is-active) {
        color: #1f2329;
      }
      .ed-button.is-text {
        height: 24px;
        min-width: 74px;
        line-height: 24px;
      }
      .ed-button + .ed-button {
        margin-left: 4px;
      }
    }
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
    padding-top: 16px;
    height: calc(100vh - 280px);
    min-height: 700px;

    .dropdown-icon {
      .down-outlined {
        transform: rotate(180deg);
      }
      &[aria-expanded='true'] {
        .down-outlined {
          transform: rotate(0);
        }
      }
      cursor: pointer;
    }

    .error-status {
      margin-top: 32px;
    }

    .upload-tip {
      color: #8f959e;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-style: normal;
      font-weight: 400;
      line-height: 22px;
    }

    .title-form_primary {
      margin: 16px 0;
      margin-top: 32px;
    }

    .info-table {
      width: 100%;
      height: calc(100% - 200px);
      &.info-table_height {
        height: calc(100% - 379px);
      }
    }
  }
}
</style>
