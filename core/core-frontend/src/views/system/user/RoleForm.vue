<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { roleCreateApi, roleEditApi, roleDetailApi } from '@/api/user'
interface RoleForm {
  id?: string | number
  name: string
  typeCode: string | number
  desc: string
}

const { t } = useI18n()
const loadingInstance = ref(null)
const dialogVisible = ref(false)
const loading = ref(false)
const formType = ref('add')

const roleForm = ref<FormInstance>()

const form = reactive<RoleForm>({
  id: null,
  name: null,
  typeCode: 0,
  desc: null
})

const rule = reactive<FormRules>({
  name: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 50,
      message: t('commons.input_limit', [2, 50]),
      trigger: 'blur'
    }
  ]
})

const init = () => {
  formType.value = 'add'
  form.typeCode = 0
  dialogVisible.value = true
}
const edit = rid => {
  formType.value = 'modify'
  dialogVisible.value = true
  queryForm(rid)
}
const queryForm = rid => {
  showLoading()
  roleDetailApi(rid).then(res => {
    Object.assign(form, res.data)
    closeLoading()
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      const param = { ...form }
      const method = formType.value === 'add' ? roleCreateApi : roleEditApi
      showLoading()
      method(param).then(res => {
        if (!res.msg) {
          ElMessage.success(t('common.save_success'))
          emits('saved')
          reset()
        }
        closeLoading()
      })
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
  resetForm(roleForm.value)
}
const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.role-form-dialog' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

defineExpose({
  init,
  edit
})
</script>

<template>
  <el-dialog
    custom-class="role-form-dialog"
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    :title="formType === 'add' ? t('role.add_title') : t('role.edit_title')"
    width="540px"
  >
    <el-form
      ref="roleForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item :label="$t('role.name')" prop="name">
        <el-input v-model="form.name" :placeholder="$t('common.please_input') + $t('role.name')" />
      </el-form-item>

      <el-form-item :label="$t('role.type')" prop="type">
        <el-radio-group v-model="form.typeCode" :disabled="formType === 'modify'">
          <el-radio v-if="formType === 'add' || form.typeCode === 0" :label="0">{{
            t('role.average_role')
          }}</el-radio>
          <el-radio v-if="formType === 'add' || form.typeCode === 1" :label="1">{{
            t('role.org_admin')
          }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <el-form-item :label="$t('role.desc')" prop="desc">
        <el-input v-model="form.desc" type="textarea" />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(roleForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(roleForm)">
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.editer-form-title {
  width: 100%;
  border-radius: 4px;
  background: #e1eaff;
  padding: 9px 16px;
  display: flex;
  align-items: center;
  margin: -8px 0 16px 0;

  i {
    color: #3370ff;
    font-size: 14.666666030883789px;
  }

  .pwd,
  .btn-text {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    text-align: left;
  }

  .pwd {
    margin: 0 8px;
    color: #1f2329;
  }

  .btn-text {
    padding: 0;
    border: none;
    height: 22px;
  }
}
.input-with-select {
  :deep(.ed-input-group__prepend) {
    background-color: #fff;
  }
  .ed-select {
    :deep(.ed-input__wrapper) {
      width: 72px;
    }
  }
}
</style>
