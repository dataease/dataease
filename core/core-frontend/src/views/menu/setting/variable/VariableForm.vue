<script lang="ts" setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { ElMessage, ElLoading, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import {
  variableCreateApi,
  variableEditApi,
  variableDetailApi,
  variableDeletelApi
} from '@/api/variable'
interface VariableForm {
  id?: string | number
  name: string
  type: string
  desc: string
}

const { t } = useI18n()
const loadingInstance = ref(null)
const dialogVisible = ref(false)
const loading = ref(false)
const formType = ref('add')

const variableForm = ref<FormInstance>()

const form = reactive<VariableForm>({
  id: null,
  name: null,
  type: 'text',
  desc: null
})

const originForm = reactive<VariableForm>({
  id: null,
  name: null,
  type: 'text',
  desc: null
})

const rule = reactive<FormRules>({
  name: [
    {
      required: true,
      message: t('common.please_input') + t('common.empty') + t('system.variable_name'),
      trigger: 'blur'
    },
    {
      min: 1,
      max: 50,
      message: t('commons.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ]
})

const init = () => {
  formType.value = 'add'
  form.type = 'text'
  dialogVisible.value = true
}
const edit = rid => {
  formType.value = 'modify'
  dialogVisible.value = true
  queryForm(rid)
}
const queryForm = rid => {
  showLoading()
  variableDetailApi(rid).then(res => {
    Object.assign(form, res.data)
    Object.assign(originForm, res.data)
    closeLoading()
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      const param = { ...form }
      const method = formType.value === 'add' ? variableCreateApi : variableEditApi
      showLoading()
      if (param.type !== originForm.type && formType.value !== 'add') {
        ElMessageBox.confirm(t('chart.confirm'), {
          confirmButtonType: 'danger',
          type: 'warning',
          autofocus: false,
          confirmButtonText: t('dataset.confirm'),
          cancelButtonText: t('dataset.cancel'),
          dangerouslyUseHTMLString: true,
          message:
            '<strong style="font-size: 16px;">' + t('system.to_change_it') + '</strong></br>',
          showClose: false
        })
          .then(() => {
            method(param)
              .then(res => {
                if (!res.msg) {
                  ElMessage.success(t('common.save_success'))
                  emits('saved', formType.value, res.data)
                  reset()
                }
                closeLoading()
              })
              .catch(() => {
                closeLoading()
              })
          })
          .catch(() => {
            closeLoading()
          })
      } else {
        method(param)
          .then(res => {
            if (!res.msg) {
              ElMessage.success(t('common.save_success'))
              emits('saved', formType.value, res.data)
              reset()
            }
            closeLoading()
          })
          .catch(() => {
            closeLoading()
          })
      }
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  form.id = null
  closeLoading()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(variableForm.value)
}
const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.role-form-dialog' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    submitForm(variableForm.value)
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
    modal-class="role-form-dialog create-dialog"
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    :title="formType === 'add' ? t('system.add_variable') : t('system.edit_variable')"
    width="420px"
    height="282px"
    @open="addKeyDown"
    @close="removeKeyDown"
  >
    <el-form
      ref="variableForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
      label-position="top"
      @submit.prevent
      @keydown.stop.prevent.enter
    >
      <el-form-item :label="t('system.variable_name')" prop="name">
        <el-input v-model="form.name" :placeholder="t('data_set.enter_1_50_characters')" />
      </el-form-item>

      <el-form-item :label="t('system.variable_type')" prop="type">
        <el-radio-group v-model="form.type">
          <el-radio label="text">{{ t('data_set.text') }}</el-radio>
          <el-radio label="num">{{ t('data_set.numerical_value') }}</el-radio>
          <el-radio label="time">{{ t('common.component.date') }}</el-radio>
        </el-radio-group>
      </el-form-item>

      <!-- <el-form-item :label="$t('role.desc')" prop="desc">
        <el-input v-model="form.desc" type="textarea" />
      </el-form-item> -->
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(variableForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(variableForm)">
          {{ t('common.sure') }}
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>
