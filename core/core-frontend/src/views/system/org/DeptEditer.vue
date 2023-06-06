<script lang="ts" setup>
import { ref, reactive, PropType } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { saveApi, updateApi } from '@/api/org'
export interface Org {
  id: string
  name: string
  children?: []
  readOnly: boolean
  subCount: number
  createTime: number
}

interface OrgForm {
  pid: number | string
  name: string
  id?: number | string
}

const props = defineProps({
  treeData: {
    type: Array as PropType<Org[]>,
    default: () => []
  }
})
const treeProps = {
  value: 'id',
  label: 'name',
  disabled: 'readOnly'
}
const { t } = useI18n()

const orgForm = ref<FormInstance>()

const formType = ref('add')
const searchNodeLoading = ref(false)
const dialogTableVisible = ref(false)

const form = reactive<OrgForm>({
  pid: '',
  name: ''
})

const rule = reactive<FormRules>({
  name: [
    { required: true, trigger: 'blur', message: t('common.required') },
    {
      min: 1,
      max: 10,
      message: t('common.input_limit', [1, 10]),
      trigger: 'blur'
    }
  ]
})

const createOrg = (pid?: number) => {
  formType.value = 'add'
  form.name = ''
  form.pid = pid
  dialogTableVisible.value = true
}
const editOrg = ({ id, name }) => {
  formType.value = 'modify'
  form.name = name
  form.id = id
  dialogTableVisible.value = true
}
const reset = () => {
  form.name = ''
  form.pid = ''
  form.id = ''
  orgForm.value.resetFields()
  dialogTableVisible.value = false
}
const emits = defineEmits(['saved'])
const save = () => {
  orgForm.value.validate(valid => {
    if (valid) {
      const param = { ...form }
      const method = formType.value === 'modify' ? updateApi : saveApi
      method(param).then(() => {
        ElMessage.success(t('common.save_success'))
        emits('saved')
        reset()
      })
    } else {
      return false
    }
  })
}

defineExpose({
  createOrg,
  editOrg
})
</script>
<template>
  <el-dialog
    :title="formType == 'add' ? t('org.add') : t('org.edit')"
    v-model="dialogTableVisible"
    append-to-body
    class="org-editer-form"
    width="600px"
    v-loading="searchNodeLoading"
    :before-close="reset"
  >
    <el-form
      require-asterisk-position="right"
      label-position="top"
      ref="orgForm"
      :model="form"
      :rules="rule"
    >
      <el-row>
        <el-col :span="24">
          <el-form-item :label="t('org.name')" prop="name">
            <el-input v-model="form.name" :placeholder="t('common.please_input') + t('org.name')" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="formType === 'add'">
        <el-col :span="24">
          <el-form-item :label="t('org.parent')" prop="description">
            <el-tree-select
              class="org-tree-select"
              clearable
              v-model="form.pid"
              :props="treeProps"
              :data="props.treeData"
              check-strictly
              :render-after-expand="false"
              :placeholder="t('common.please_select') + t('org.parent')"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <template #footer>
      <el-button secondary @click="reset">{{ t('common.cancel') }}</el-button>
      <el-button type="primary" @click="save">{{ t('common.sure') }}</el-button>
    </template>
  </el-dialog>
</template>

<style lang="less">
.org-tree-select {
  width: 100%;
}
</style>
