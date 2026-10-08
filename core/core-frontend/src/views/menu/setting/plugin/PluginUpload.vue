<template>
  <el-upload
    class="upload-plugin"
    action=""
    accept=".jar"
    :before-upload="uploadValidate"
    :show-file-list="false"
    :http-request="setFile"
    :on-success="onSuccess"
    :disabled="busy"
  >
    <slot />
  </el-upload>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { UploadRequestOptions, UploadProps } from 'element-plus-secondary'
import request from '@/config/axios'
import { propTypes } from '@/utils/propTypes'

import { confirmDependencies, type InstallCheck } from './dependencyConfirm'

const props = defineProps({
  isEdit: propTypes.bool.def(false),
  id: propTypes.string
})
const { t } = useI18n()
const busy = ref(false)
const cancelled = ref(false)
const emits = defineEmits(['onSuccess'])

/*
 * 统一上传入口，同步插件先检查并确认驱动，其他插件由后端沿用原安装流程
 */
const setFile = async (options: UploadRequestOptions) => {
  busy.value = true
  cancelled.value = false
  try {
    const data = new FormData()
    data.append('file', options.file)
    if (props.isEdit) {
      data.append(
        'request',
        new Blob([JSON.stringify({ id: props.id })], { type: 'application/json' })
      )
    }
    const response = await request.post({
      url: '/plugin/prepare',
      headersType: 'multipart/form-data;',
      data
    })
    const check = response.data as InstallCheck
    if (!check.operation) return
    if (!(await confirmDependencies(check, t))) {
      cancelled.value = true
      return
    }
    ElMessage.success(t('system.driver_restart'))
  } finally {
    busy.value = false
  }
}
const uploadValidate: UploadProps['beforeUpload'] = file => {
  if (!file.name.endsWith('.jar')) {
    ElMessage.warning(t('system.can_be_uploaded'))
    return false
  }
  if (file.size / 1024 / 1024 > 200) {
    ElMessage.warning(t('system.maximum_upload_200m'))
    return false
  }
  return true
}
const onSuccess = () => {
  if (!cancelled.value) emits('onSuccess')
}
</script>
