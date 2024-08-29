<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { uploadFontFile } from '@/api/font'
import FontInfo from './FontInfo.vue'
import { ElMessage } from 'element-plus-secondary'
import { edit } from '@/api/font'
import { cloneDeep } from 'lodash-es'
const state = reactive({
  fileList: null
})
const loading = ref(false)
const upload = ref()
const uploadExcel = () => {
  const formData = new FormData()
  formData.append('file', state.fileList.raw)
  loading.value = true
  return uploadFontFile(formData)
    .then(res => {
      ruleForm.size = res.data.size
      ruleForm.sizeType = res.data.sizeType
      ruleForm.fileTransName = res.data.fileTransName
      ruleForm.fileName = state.fileList.raw.name
      upload.value?.clearFiles()
      loading.value = false
    })
    .catch(error => {
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
const dialogTitle = ref('')
const dialogVisible = ref(false)
const action = ref('')
const defaultForm = {
  id: null,
  name: '',
  fileName: '',
  fileTransName: '',
  size: 0,
  sizeType: '',
  isDefault: 0,
  isBuiltin: 0,
  updateTime: 0
}
const ruleForm = reactive(cloneDeep(defaultForm))

const init = (val, type, item) => {
  dialogTitle.value = val || '添加字体'
  action.value = type
  dialogVisible.value = true
  Object.assign(ruleForm, cloneDeep(defaultForm))
  Object.assign(ruleForm, JSON.parse(JSON.stringify(item)))
}

const fontDel = () => {
  state.fileList = null
}

const ruleFormRef = ref()
const rules = {
  name: [
    { required: true, message: '请输入字体名称', trigger: 'blur' },
    { min: 1, max: 50, message: '字符长度1-50', trigger: 'blur' }
  ]
}
defineExpose({
  init
})

const onChange = file => {
  state.fileList = file
}

const uploadFail = response => {
  let myError = response.toString()
  myError.replace('Error: ', '')
}

const emits = defineEmits(['finish'])

const confirm = () => {
  ruleFormRef.value.validate(val => {
    if (val) {
      if (action.value === 'uploadFile') {
        if (ruleForm.fileTransName === '') {
          ElMessage.error('请上传字库文件')
          return
        }
      }
      edit(ruleForm).then(res => {
        ElMessage.success(dialogTitle.value + '成功')
        dialogVisible.value = false
        Object.assign(ruleForm, cloneDeep(defaultForm))
        state.fileList = null
        emits('finish')
      })
    }
  })
}
</script>

<template>
  <el-dialog v-model="dialogVisible" :title="dialogTitle" width="420">
    <el-form
      ref="ruleFormRef"
      :model="ruleForm"
      label-position="top"
      :rules="rules"
      label-width="auto"
      class="demo-ruleForm"
    >
      <el-form-item v-if="action !== 'uploadFile'" label="字体名称" prop="name">
        <el-input placeholder="请输入字体名称" v-model="ruleForm.name" />
      </el-form-item>
      <el-form-item v-if="action !== 'rename'" label="字库文件">
        <el-upload
          action=""
          :multiple="false"
          ref="uploadAgain"
          :show-file-list="false"
          accept=".ttf"
          :on-change="onChange"
          :http-request="uploadExcel"
          :on-error="uploadFail"
          name="file"
          v-show="!state.fileList"
        >
          <template #trigger>
            <el-button v-loading="loading" secondary>
              <template #icon>
                <Icon name="icon_upload_outlined"></Icon>
              </template>
              上传字库文件
            </el-button>
          </template>
        </el-upload>
        <FontInfo
          @del="fontDel"
          v-show="state.fileList"
          :size="ruleForm.size + ' ' + ruleForm.sizeType"
          :name="ruleForm.fileName"
        ></FontInfo>
        <el-upload
          action=""
          :multiple="false"
          ref="uploadAgain"
          :show-file-list="false"
          accept=".ttf"
          :on-change="onChange"
          :http-request="uploadExcel"
          :on-error="uploadFail"
          name="file"
          v-show="state.fileList"
        >
          <template #trigger>
            <el-button text> 重新上传 </el-button>
          </template>
        </el-upload>
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirm"> 确定 </el-button>
      </div>
    </template>
  </el-dialog>
</template>
