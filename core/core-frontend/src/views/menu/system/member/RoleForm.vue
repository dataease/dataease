<script lang="ts" setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
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
      message: t('common.please_input') + t('common.empty') + t('role.name'),
      trigger: 'blur'
    },
    {
      min: 2,
      max: 64,
      message: t('commons.input_limit', [2, 64]),
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
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...form }
      const method = formType.value === 'add' ? roleCreateApi : roleEditApi
      showLoading()
      method(param)
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            emits('saved', formType.value)
            reset()
          }
          closeLoading()
        })
        .catch(() => {
          closeLoading()
        })
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
const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    submitForm(roleForm.value)
  }
}
const removeKeyDown = () => {
  window.removeEventListener('keydown', keyFunction)
}
const addKeyDown = () => {
  window.addEventListener('keydown', keyFunction)
}
onBeforeUnmount(() => {
  removeKeyDown()
})
defineExpose({
  init,
  edit
})
</script>

<template>
  <el-dialog
    modal-class="role-form-dialog"
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    :title="formType === 'add' ? t('role.add_title') : t('role.edit_title')"
    width="420px"
    height="282px"
    @open="addKeyDown"
    @close="removeKeyDown"
  >
    <el-form
      ref="roleForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
      label-position="top"
      @submit.prevent
    >
      <el-form-item :label="$t('role.name')" prop="name">
        <el-input
          v-model="form.name"
          :placeholder="$t('common.please_input') + ' ' + $t('role.name')"
        />
      </el-form-item>

      <el-form-item :label="$t('role.type')" prop="type">
        <el-radio-group v-model="form.typeCode" :disabled="formType === 'modify'">
          <el-radio v-if="formType === 'add' || form.typeCode === 0" :label="0">{{
            t('role.average_role')
          }}</el-radio>
          <el-radio v-if="formType === 'add' || form.typeCode === 7" :label="7">{{
            t('role.org_analyst')
          }}</el-radio>
          <el-radio v-if="formType === 'add' || form.typeCode === 9" :label="9">{{
            t('role.org_admin')
          }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- <el-form-item :label="$t('role.desc')" prop="desc">
        <el-input v-model="form.desc" type="textarea" />
      </el-form-item> -->
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
