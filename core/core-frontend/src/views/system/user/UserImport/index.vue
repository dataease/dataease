<script lang="ts" setup>
import { ref, reactive, h } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  ElLoading,
  UploadRequestOptions,
  UploadProps,
  ElButton
} from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { downExcelTemplateApi, importUserApi, downErrorRecordApi, clearErrorApi } from '@/api/user'
const { t } = useI18n()
const defaultTip = t('userimport.defaultTip')
const loadingInstance = ref(null)
const dialogShow = ref(false)
const form = ref({})
const file = ref(null)
const fileName = ref('')
const errorFileKey = ref(null)
const emits = defineEmits(['refresh-grid'])
const state = reactive({
  errList: [],
  filesTmp: []
})

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.user-import-class' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
const showDialog = () => {
  file.value = null
  fileName.value = null
  errorFileKey.value = null
  dialogShow.value = true
}
const closeDialog = () => {
  dialogShow.value = false
}
const handleExceed: UploadProps['onExceed'] = () => {
  ElMessage.warning(t('userimport.exceedMsg'))
}
const handleError = () => {
  ElMessage.warning('执行失败请联系管理员')
}
const uploadValidate = file => {
  const suffix = file.name.substring(file.name.lastIndexOf('.') + 1)
  if (suffix !== 'xlsx' && suffix !== 'xls') {
    ElMessage.warning(t('userimport.suffixMsg'))
    return false
  }

  if (file.size / 1024 / 1024 > 10) {
    ElMessage.warning(t('userimport.limitMsg'))
    return false
  }
  state.errList = []
  return true
}
const setFile = (options: UploadRequestOptions) => {
  file.value = options.file
  fileName.value = options.file.name
}

const buildFormData = (file, files, param) => {
  const formData = new FormData()
  if (file) {
    formData.append('file', file)
  }
  if (files) {
    files.forEach(f => {
      formData.append('files', f)
    })
  }
  if (param) {
    formData.append('request', new Blob([JSON.stringify(param)], { type: 'application/json' }))
  }

  return formData
}
const downExcel = () => {
  showLoading()
  downExcelTemplateApi().then(res => {
    const blobData = res.data
    const blob = new Blob([blobData], { type: 'application/vnd.ms-excel' })
    const link = document.createElement('a')
    link.style.display = 'none'
    link.href = URL.createObjectURL(blob)
    link.download = 'user.xlsx' // 下载的文件名
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    closeLoading()
  })
}

const toGrid = () => {
  file.value = null
  fileName.value = ''
  dialogShow.value = false
  emits('refresh-grid')
}
const sure = () => {
  const param = buildFormData(file.value, null, null)
  showLoading()
  importUserApi(param).then(res => {
    closeLoading()
    const data = res.data
    errorFileKey.value = data.dataKey
    closeDialog()
    showTips(data.successCount, data.errorCount)
  })
}
const downErrorExcel = () => {
  if (errorFileKey.value) {
    showLoading()
    downErrorRecordApi(errorFileKey.value).then(res => {
      const blobData = res.data
      const blob = new Blob([blobData], { type: 'application/vnd.ms-excel' })
      const link = document.createElement('a')
      link.style.display = 'none'
      link.href = URL.createObjectURL(blob)
      link.download = 'error.xlsx' // 下载的文件名
      document.body.appendChild(link)
      link.click()
      document.body.removeChild(link)
      closeLoading()
      // closeDialog()
    })
  }
}
const showTips = (successCount, errorCount) => {
  let title = !errorCount ? '导入数据成功' : successCount ? '部分数据导入失败' : '数据导入失败'
  const childrenDomList = [
    h('strong', null, title),
    h('br', null, null),
    h('span', null, `成功导入数据 ${successCount} 条`)
  ]
  if (errorCount) {
    const errorCountDom = h('span', null, `，导入失败 ${errorCount} 条 `)
    const errorDom = h('div', { class: 'error-record-tip' }, [
      h('span', null, '可'),
      h(
        ElButton,
        {
          onClick: downErrorExcel,
          type: 'primary',
          text: true,
          class: 'down-button'
        },
        '下载错误报告'
      ),
      h('span', null, '，修改后重新导入')
    ])

    childrenDomList.push(errorCountDom)
    childrenDomList.push(errorDom)
  }
  ElMessageBox.confirm('', {
    confirmButtonType: 'primary',
    type: !errorCount ? 'success' : successCount ? 'warning' : 'error',
    autofocus: false,
    dangerouslyUseHTMLString: true,
    message: h('div', { class: 'import-tip-box' }, childrenDomList),
    showClose: false,
    cancelButtonText: '返回查看',
    confirmButtonText: '继续导入'
  })
    .then(() => {
      clearErrorRecord()
      showDialog()
      toGrid()
    })
    .catch(() => {
      clearErrorRecord()
      toGrid()
    })
}
const clearErrorRecord = () => {
  if (errorFileKey.value) {
    clearErrorApi(errorFileKey.value)
  }
}
</script>
<template>
  <div style="display: inline-block; margin-left: 15px">
    <el-button @click="showDialog">{{ t('userimport.dialogTitle') }}</el-button>
  </div>
  <el-dialog
    v-model="dialogShow"
    :title="t('userimport.buttonText')"
    width="600px"
    class="user-import-class"
    v-if="dialogShow"
    @close="closeDialog"
  >
    <div class="down-template">
      <span class="icon-span">
        <el-icon>
          <Icon name="icon_warning_filled"></Icon>
        </el-icon>
      </span>
      <div class="down-template-content">
        <span>{{ t('userimport.first_please') }}</span>
        <el-button type="primary" text class="down-button" @click="downExcel">{{
          t('userimport.downTip')
        }}</el-button>
        <span>{{ t('userimport.fill_and_upload') }}</span>
      </div>
    </div>
    <el-form ref="form" class="import-form" :model="form" label-width="0px">
      <el-form-item label="" style="margin-bottom: 36px">
        <el-upload
          class="upload-user"
          action=""
          accept=".xlsx,.xls"
          :on-exceed="handleExceed"
          :before-upload="uploadValidate"
          :on-error="handleError"
          :show-file-list="false"
          :file-list="state.filesTmp"
          :http-request="setFile"
        >
          <el-input :placeholder="t('userimport.placeholder')" readonly v-model="fileName">
            <template #suffix>
              <el-icon>
                <Icon name="icon_upload_outlined" />
              </el-icon>
            </template>
            <template #prefix>
              <el-icon v-if="!!fileName">
                <Icon name="icon_file-excel_colorful" />
              </el-icon>
            </template>
          </el-input>
        </el-upload>

        <span style="float: left">
          <span>
            <el-link class="font12" type="info" disabled>
              {{ defaultTip }}
            </el-link>
          </span>
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="closeDialog">{{ t('common.cancel') }}</el-button>
        <el-button
          :type="file && fileName ? 'primary' : 'info'"
          @click="sure"
          :disabled="!file || !fileName"
          >{{ t('userimport.import') }}</el-button
        >
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less">
.upload-user {
  width: 100%;
  height: 32px;
  .ed-upload {
    width: 100% !important;
  }
}
.color-danger {
  ::v-deep .el-link--inner {
    color: var(--deDanger, #f54a45) !important;
  }
}
.font12 {
  ::v-deep .el-link--inner {
    font-size: 12px !important;
  }
}
.down-template {
  display: flex;
  flex-direction: row;
  width: 100%;
  height: 40px;
  line-height: 40px;
  background: var(--ed-color-primary-light-7);

  padding-left: 10px;
  .icon-span {
    color: var(--ed-color-primary);
    font-size: 18px;
    i {
      top: 3px;
    }
  }
  .down-template-content {
    font-size: 14px;
    display: flex;
    flex-direction: row;
    margin-left: 10px;
    .down-button {
      height: 40px;
    }
  }
}
.import-form {
  margin-top: 20px;
}
.import-tip-box {
  strong {
    font-size: 16px;
  }
  span {
    font-size: 13px;
  }
  .error-record-tip {
    font-size: 13px;
  }
}
</style>
