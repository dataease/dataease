<script lang="tsx" setup>
import { Icon } from '@/components/icon-custom'
import { ElIcon } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  ref,
  shallowRef,
  reactive,
  computed,
  toRefs,
  onMounted,
  onBeforeUnmount,
  nextTick
} from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { save, update } from '@/api/datasource'
import type { Action } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
import ExcelInfo from '../ExcelInfo.vue'
import SheetTabs from '../SheetTabs.vue'
import { cloneDeep, debounce } from 'lodash-es'
import { uploadFile } from '@/api/datasource'
import { useEmitt } from '@/hooks/web/useEmitt'

export interface Param {
  editType: number
  pid?: string
  type?: string
  id?: string
  name?: string
  creator?: string
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
  }
})

const { param } = toRefs(props)

const { t } = useI18n()
const { emitter } = useEmitt()

const loading = ref(false)
const columns = shallowRef([])

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

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.originName,
    fieldType: ele.fieldType,
    dataKey: ele.originName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div class="flex-align-center icon">
        <ElIcon>
          <Icon
            name={`field_${fieldType[column.fieldType]}`}
            className={`field-icon-${fieldType[column.fieldType]}`}
          ></Icon>
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
  if (response?.code !== 0) {
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
      tip: '替换可能会影响自定义数据集、关联数据集、仪表板等，是否替换？',
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

        if (action === 'cancel') {
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
      }
    })
  } else {
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
  return uploadFile(formData).then(res => {
    upload.value?.clearFiles()
    uploadAgain.value?.clearFiles()
    uploadSuccess(res)
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
      >
        <el-form-item
          v-if="sheetFile.name"
          prop="id"
          label="文件"
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
              <el-button text>重新上传</el-button>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item
          v-else
          prop="id"
          key="sheetId"
          label="文件"
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
                  <Icon name="icon_upload_outlined"></Icon>
                </template>
                {{ t('dataset.upload_file') }}
              </el-button>
            </template>
          </el-upload>
          <p class="upload-tip" style="width: 100%">仅支持xlsx、xls、csv格式的文件</p>
          <div class="ed-form-item__error" v-if="status">请上传文件</div>
        </el-form-item>
        <el-form-item
          :class="status && !sheetFile.name && 'error-status'"
          prop="name"
          key="name"
          v-if="showName"
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

        <div class="info-table" v-if="isResize">
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
    padding-top: 16px;
    height: calc(100vh - 280px);

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
      font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
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
      height: calc(100% - 315px);
    }
  }
}
</style>
