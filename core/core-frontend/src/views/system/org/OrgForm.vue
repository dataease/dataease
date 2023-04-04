<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { TreeSelect } from '@/components/tree-select'

interface OrgForm {
  resourceName: string
  targetOrg: string | number
  targetDir?: string | number
  moveType: 'resource' | 'resourceAndAuth'
}
const { t } = useI18n()

const dialogVisible = ref(false)
const loading = ref(false)

const createOrgForm = ref<FormInstance>()

const form = reactive<OrgForm>({
  resourceName: '',
  targetOrg: '',
  targetDir: '',
  moveType: 'resource'
})

const validateTargetDir = (_, value, callback) => {
  const pattern = '^[a-zA-Z][a-zA-Z0-9\._-]*$'
  const regep = new RegExp(pattern)
  if (!regep.test(value)) {
    const msg = t('user.user_name_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}

const validateResourceName = (_, value, callback) => {
  const pattern = '^[a-zA-Z][a-zA-Z0-9\._-]*$'
  const regep = new RegExp(pattern)
  if (!regep.test(value)) {
    const msg = t('user.user_name_pattern_error')
    callback(new Error(msg))
  } else {
    callback()
  }
}
const rule = reactive<FormRules>({
  resourceName: [
    {
      required: true,
      message: t('user.id_mandatory'),
      trigger: 'blur'
    },
    { required: true, validator: validateResourceName, trigger: 'blur' }
  ],
  targetOrg: [
    {
      required: true,
      message: t('user.name_mandatory'),
      trigger: 'blur'
    }
  ],
  targetDir: [
    {
      required: true,
      message: t('user.phone_format'),
      trigger: 'blur'
    },
    { required: true, validator: validateTargetDir, trigger: 'blur' }
  ]
})

const init = () => {
  dialogVisible.value = true
}

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      console.log('submit!')
    } else {
      console.log('error submit!', fields)
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(createOrgForm.value)
}
defineExpose({
  init
})
</script>

<template>
  <el-dialog
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    title="资源迁移"
    width="840px"
  >
    <el-form
      ref="createOrgForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="资源名称" prop="resourceName">
        <el-input v-model="form.resourceName" :placeholder="$t('user.input_name')" />
      </el-form-item>
      <el-form-item label="迁移类型">
        <el-radio-group v-model="form.moveType">
          <el-radio label="resource">仅迁移资源</el-radio>
          <el-radio label="resourceAndAuth">迁移资源及授权相关</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="目标组织" prop="targetOrg">
        <tree-select></tree-select>
      </el-form-item>
      <el-form-item label="目标目录" prop="targetDir">
        <tree-select width="500px"></tree-select>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(createOrgForm)">Cancel</el-button>
        <el-button type="primary" @click="submitForm(createOrgForm)"> Confirm </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped></style>
