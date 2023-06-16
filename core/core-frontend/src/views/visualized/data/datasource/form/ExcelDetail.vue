<script lang="tsx" setup>
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { Icon } from '@/components/icon-custom'
import { ElIcon, ElDropdown, ElDropdownItem, ElDropdownMenu } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { ref, nextTick, shallowRef, reactive, computed, PropType, toRefs } from 'vue'
import { ElTree, ElMessage, ElMessageBox } from 'element-plus-secondary'
import { save } from '@/api/datasource'
import type { Action } from 'element-plus-secondary'
import { Base64 } from 'js-base64'
import { useUserStoreWithOut } from '@/store/modules/user'

export interface Param {
  editType: number
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
  },
  editDs: {
    required: false,
    default: false,
    type: Boolean
  }
})

const { param, editDs } = toRefs(props)

const dsFormDisabled = ref(false)

const { t } = useI18n()

const token = useUserStoreWithOut().getToken

const baseUrl = ''
const headers = {
  Authorization: token,
  'Accept-Language': 'zh'
}
const defaultProps = {
  label: 'excelLabel',
  children: 'sheets'
}

const num = ref(0)

const nameList = []
const originName = ''
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
const sheetObj = reactive({
  tableName: ' ',
  sheetExcelId: '',
  fields: [],
  jsonArray: [],
  nameExist: false,
  empty: '',
  overLength: false
})
const state = reactive({
  excelData: [],
  defaultExpandedKeys: [],
  defaultCheckedKeys: [],
  fileList: [],
  sheets: []
})

const nameListCopy = computed(() => {
  return nameList.filter(ele => ele !== originName)
})
const uploading = ref(false)
const tree = ref<InstanceType<typeof ElTree>>()
const editeTableField = ref(false)

const handleCheckChange = (data, checked) => {
  if (checked) {
    state.defaultCheckedKeys.push(data.id)
    handleNodeClick(data)
  } else {
    let index = state.defaultCheckedKeys.findIndex(id => {
      if (id === data.id) {
        return
      }
    })
    state.defaultCheckedKeys.splice(index, 1)
  }
  validateName()
  const labelList = tree.value.getCheckedNodes().map(ele => ele.id)
  const excelList = state.excelData.map(ele => ele.id)
  num.value = labelList.filter(ele => !excelList.includes(ele)).length
}
const nameExistValidator = (ele, checkList) => {
  ele.nameExist =
    nameListCopy.value.concat(checkList).filter(name => name === ele.tableName).length > 1
}
const validateName = () => {
  const checkList = tree.value.getCheckedNodes().map(ele => ele.tableName)
  state.excelData
    .reduce((pre, next) => pre.concat(next.sheets), [])
    .forEach(ele => {
      if (checkList.includes(ele.tableName)) {
        nameExistValidator(ele, checkList)
        nameLengthValidator(ele)
      } else {
        Object.assign(ele, {
          nameExist: false,
          empty: false,
          overLength: false
        })
      }
    })
}
const fieldType = {
  TEXT: 'field_text',
  DATETIME: 'field_time',
  LONG: 'field_value',
  DOUBLE: 'field_value'
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
                <ElIcon>
                  <Icon name={fieldType[column.fieldType]}></Icon>
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
                          <Icon name={fieldType[ele.value]}></Icon>
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
const nameLengthValidator = ele => {
  Object.assign(ele, {
    empty: !ele.tableName.length,
    overLength: ele.tableName.length > 50
  })
}
const handleNodeClick = data => {
  console.log(data)
  if (data.sheet) {
    Object.assign(sheetObj, data)
    columns.value = generateColumns(data.fields)
  }
  console.log(columns.value)
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
  validateName()
}

const beforeUpload = () => {
  console.log(param)
  uploading.value = true
}

const uploadFail = response => {
  let myError = response.toString()
  myError = myError.replace('Error: ', '')
}

const excelSave = () => {
  editeTableField.value = false
}

const uploadSuccess = (response, _, fileList) => {
  editeTableField.value = true
  uploading.value = false
  state.excelData.push(response.data)
  console.log(state.excelData)
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

const initEditForm = () => {
  dsFormDisabled.value = true
}

const saveExcelDs = () => {
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
          saveExcelData(sheetFileMd5, table)
        }
      }
    })
  } else {
    saveExcelData(sheetFileMd5, table)
  }
}

const saveExcelData = (sheetFileMd5, table) => {
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

const editForm = () => {
  dsFormDisabled.value = false
}

const cancel = () => {
  dsFormDisabled.value = true
}

defineExpose({
  initEditForm,
  saveExcelDs
})
</script>

<template>
  <div class="excel-detail">
    <div class="detail-inner">
      <div class="detail-operate" v-show="editDs">
        <el-button v-show="!dsFormDisabled" @click="() => cancel()">{{
          t('common.cancel')
        }}</el-button>
        <el-button v-show="dsFormDisabled" type="primary" @click="() => editForm()">{{
          t('common.edit')
        }}</el-button>
        <el-button v-show="!dsFormDisabled" type="primary" @click="() => saveExcelDs()">{{
          t('common.sure')
        }}</el-button>
      </div>
      <el-form :disabled="dsFormDisabled" label-position="top">
        <el-form-item :label="t('common.name')">
          <el-input v-model="param.name" :placeholder="t('datasource.input_name')" />
        </el-form-item>
        <el-form-item class="upload-excel">
          <el-upload
            :action="baseUrl + 'api/datasource/uploadFile'"
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
              <el-input
                :disabled="!!param.id"
                style="width: 100%"
                readonly
                placeholder="点击选择文件"
              >
                <template #prefix>
                  <el-icon>
                    <Icon name="icon_search-outline_outlined"></Icon>
                  </el-icon>
                </template>
              </el-input>
            </template>
          </el-upload>
          <p style="width: 100%">温馨提示: 请上传csv,xlsx,xls格式的文件</p>
          <el-upload
            :action="baseUrl + 'api/datasource/uploadFile'"
            :multiple="false"
            :show-file-list="false"
            v-if="!!param.id"
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
              <el-button
                class="upload-btn upload-replace"
                @click="param.editType = 0"
                type="primary"
                >替换文件</el-button
              >
              <el-button class="upload-btn upload-append" @click="param.editType = 1" type="primary"
                >追加文件</el-button
              >
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <el-dialog
        fullscreen
        class="excel-dialog-fullscreen"
        append-to-body
        v-model="editeTableField"
      >
        <div class="table-checkbox-list">
          <el-tree
            ref="tree"
            class="de-tree"
            :data="state.excelData"
            :default-expanded-keys="state.defaultExpandedKeys"
            :default-checked-keys="state.defaultCheckedKeys"
            node-key="id"
            :props="defaultProps"
            show-checkbox
            highlight-current
            @node-click="handleNodeClick"
            @check-change="handleCheckChange"
          >
            <template #default="{ data }">
              <span :title="data.excelLabel" class="custom-tree-node">
                <span class="label">{{ data.excelLabel }}</span>
                <span class="error-name-exist">
                  <el-icon>
                    <Icon name="ds-icon-scene"></Icon>
                  </el-icon>
                </span>
              </span>
            </template>
          </el-tree>
        </div>
        <div class="table-detail">
          <empty-background
            v-if="!state.excelData.length"
            :description="t('deDataset.excel_data_first')"
            img-type="table"
          />

          <template v-else>
            <div class="dataset">
              <span class="name">{{ t('dataset.name') }}</span>
              <el-input
                v-model="sheetObj.tableName"
                :placeholder="t('common.name')"
                size="small"
                @change="changeDatasetName"
              />
              <div
                v-if="
                  (sheetObj.nameExist && !param.datasourceId) ||
                  sheetObj.empty ||
                  sheetObj.overLength
                "
                style="top: 52px; left: 107px"
                class="el-form-item__error"
              >
                {{
                  t(
                    sheetObj.nameExist
                      ? 'deDataset.already_exists'
                      : sheetObj.overLength
                      ? 'dataset.char_can_not_more_50'
                      : 'dataset.pls_input_name'
                  )
                }}
              </div>
            </div>
            <div class="data">
              <div class="result-num">
                {{ `${t('dataset.preview_show')} 1000 ${t('dataset.preview_item')}` }}
              </div>
              <div class="info-table">
                <el-auto-resizer>
                  <template #default="{ height, width }">
                    <el-table-v2
                      :columns="columns"
                      header-class="header-cell"
                      :data="sheetObj.jsonArray"
                      :width="width"
                      :height="height"
                      fixed
                    />
                  </template>
                </el-auto-resizer>
              </div>
            </div>
          </template>
        </div>
        <div class="footer">
          <span> 已选: {{ num }} 张表 </span>
          &nbsp;
          <el-button type="primary" @click="excelSave"> 确定 </el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<style lang="less">
.excel-detail {
  display: flex;
  justify-content: center;
  .ed-form {
    width: 50%;
    .upload-excel {
      .ed-form-item__content > div,
      .ed-upload {
        width: 100% !important;
      }

      .upload-btn {
        position: absolute;
        top: 65px;
      }

      .upload-replace {
        left: 0;
      }

      .upload-append {
        left: 90px;
      }
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
.excel-dialog-fullscreen {
  .ed-dialog__body::after {
    content: '';
    display: block;
    visibility: hidden;
    clear: both;
  }
  .table-checkbox-list {
    width: 240px;
    float: left;
    height: calc(100vh - 100px);
    overflow-y: auto;
    border-right: 1px solid #cccccc;

    .custom-tree-node {
      position: relative;
      width: 80%;
      display: flex;

      .label {
        overflow: hidden;
        white-space: nowrap;
        text-overflow: ellipsis;
        width: 85%;
      }
    }

    .error-name-exist {
      position: absolute;
      top: 0;
      right: 0;
    }

    .item {
      height: 40px;
      width: 215px;
      border-radius: 4px;
      display: flex;
      align-items: center;
      box-sizing: border-box;
      padding: 12px;

      &:hover {
        background: rgba(31, 35, 41, 0.1);
      }

      &.active {
        background-color: var(--deWhiteHover, #3370ff);
        color: var(--primary, #3370ff);
      }

      .ed-checkbox__label {
        overflow: hidden;
      }
    }
  }
  .table-detail {
    font-family: PingFang SC;
    height: calc(100vh - 100px);
    float: right;
    width: calc(100% - 240px);

    .dataset {
      padding: 21px 24px;
      width: 100%;
      border-bottom: 1px solid rgba(31, 35, 41, 0.15);
      display: flex;
      align-items: center;
      position: relative;

      .name {
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextPrimary, #1f2329);
      }

      .ed-input {
        width: 420px;
        margin-left: 12px;
      }
    }

    .data {
      padding: 16px 24px;
      box-sizing: border-box;
      height: calc(100% - 80px);
      overflow-y: auto;

      .result-num {
        font-family: PingFang SC;
        font-size: 14px;
        font-weight: 400;
        color: var(--deTextSecondary, #646a73);
        margin-bottom: 16px;
      }

      .type-switch {
        padding: 2px 1.5px;
        display: inline-block;
        cursor: pointer;

        i {
          margin-left: 4px;
          font-size: 12px;
        }

        &:hover {
          background: rgba(31, 35, 41, 0.1);
          border-radius: 4px;
        }
      }
    }

    .info-table {
      height: calc(100% - 200px);
    }
  }

  .footer {
    float: right;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: flex-end;
  }
}
</style>
