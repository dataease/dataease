<script lang="ts" setup>
import { ref, reactive } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  ElLoading,
  UploadRequestOptions,
  UploadProps
} from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { downExcelTemplateApi, importUserApi } from '@/api/user'
const { t } = useI18n()
const defaultTip = t('userimport.defaultTip')
const errorTip = t('userimport.errorTip')
const loadingInstance = ref(null)
const dialogShow = ref(false)
const form = ref({})

const showError = ref(false)

const file = ref(null)
const fileName = ref('')
const errorLink = ref()
const userUpload = ref(null)
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
  if (!showError.value) {
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
  } else {
    if (errorLink.value) {
      errorLink.value.click()
      clearErrorLink()
    } else {
      ElMessage.warning(t('userimport.repeatDown'))
    }
  }
}

const clearErrorLink = () => {
  if (errorLink.value) {
    document.body.removeChild(errorLink.value)
    errorLink.value = null
  }
}
const uploadAgain = () => {
  clearErrorLink()
  userUpload.value['clearFiles']()
  file.value = null
  fileName.value = ''
  // userUpload.value.$refs['upload-inner'].handleClick()
  showError.value = false
}
const toGrid = () => {
  showError.value = false
  file.value = null
  fileName.value = ''
  clearErrorLink()
  dialogShow.value = false
  emits('refresh-grid')
}
const sure = () => {
  const param = buildFormData(file.value, null, null)
  showLoading()
  importUserApi(param).then(res => {
    closeLoading()
    const data = res.data
    if (data.errorCount === 0) {
      showTips('数据导入成功', data.successCount, data.errorCount)
    } else if (data.errorCount > 0 && data.successCount > 0) {
      showTips('部分数据导入失败', data.successCount, data.errorCount)
    } else if (data.successCount === 0) {
      showTips('数据导入失败', data.successCount, data.errorCount)
    }
  })
}
const showTips = (title, successCount, errorCount) => {
  ElMessageBox.confirm(title, {
    confirmButtonType: 'danger',
    type: 'warning',
    autofocus: false,
    dangerouslyUseHTMLString: true,
    message:
      '<strong style="font-size: 16px;">' +
      '成功导入数据 ' +
      successCount +
      ' 条，导入失败 ' +
      errorCount +
      ' 条 \n 可 下载错误报告，修改后重新导入' +
      '</strong>',
    showClose: false
  }).then(e => {
    console.log(e)
  })
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
          ref="userUpload"
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
            <el-link class="color-danger font12" v-if="showError" type="danger" disabled>
              {{ errorTip }}
            </el-link>

            <el-link class="font12" v-else type="info" disabled>
              {{ defaultTip }}
            </el-link>
          </span>
        </span>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer" v-if="showError">
        <el-button type="primary" @click="uploadAgain">{{ t('userimport.uploadAgain') }}</el-button>
        <el-button @click="toGrid">{{ t('userimport.backUserGrid') }}</el-button>
      </span>
      <span class="dialog-footer" v-else>
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
</style>
