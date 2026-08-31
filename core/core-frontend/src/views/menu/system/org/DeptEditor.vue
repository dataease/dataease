<script lang="ts" setup>
import { ref, reactive, computed, onBeforeUnmount } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { saveApi, updateApi, searchApi } from '@/api/org'
import { sysOrgMemberCandidatesApi } from './api'
import { useUserStoreWithOut } from '@/store/modules/user'
const userStore = useUserStoreWithOut()
const isAdmin = computed(() => userStore.getUid === '1')
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
  adminIds?: number[]
}
const state = reactive({
  treeData: [] as Org[]
})

const treeProps = {
  value: 'id',
  label: 'name',
  disabled: 'readOnly'
}
const { t } = useI18n()

const orgForm = ref<FormInstance>()
const loadingInstance = ref(null)
const formType = ref('add')
const searchNodeLoading = ref(false)
const dialogTableVisible = ref(false)

const form = reactive<OrgForm>({
  pid: '',
  name: ''
})

const adminIds = ref<number[]>([])
const candidateAdmins = ref<any[]>([])

const rule = reactive<FormRules>({
  name: [
    { required: true, trigger: 'blur', message: t('common.required') },
    {
      min: 1,
      max: 50,
      message: t('common.input_limit', [1, 50]),
      trigger: 'blur'
    }
  ]
})

const createOrg = (pid?: number) => {
  formType.value = 'add'
  form.name = ''
  form.pid = pid
  adminIds.value = []
  dialogTableVisible.value = true

  searchApi({}).then(res => {
    state.treeData = res.data
  })

  // Load candidate admins (users not in any org)
  sysOrgMemberCandidatesApi({ keyword: '' }).then(res => {
    candidateAdmins.value = res.data || []
  })
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
  adminIds.value = []
  candidateAdmins.value = []
  orgForm.value.resetFields()
  dialogTableVisible.value = false
}
const emits = defineEmits(['saved'])
const save = () => {
  orgForm.value.validate(valid => {
    if (valid) {
      const param = { ...form }
      if (formType.value === 'add') {
        param.adminIds = adminIds.value
      }
      const method = formType.value === 'modify' ? updateApi : saveApi
      showLoading()
      method(param)
        .then(() => {
          ElMessage.success(t('common.save_success'))
          param['formType'] = formType.value
          emits('saved', param)
          reset()
          closeLoading()
        })
        .finally(() => {
          closeLoading()
        })
    } else {
      return false
    }
  })
}
const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.org-form-dialog' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
const keyFunction = (e: any) => {
  if (e?.keyCode === 13) {
    save()
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
  createOrg,
  editOrg
})
</script>
<template>
  <el-dialog
    modal-class="org-form-dialog"
    :title="formType == 'add' ? t('org.add') : t('org.edit')"
    v-model="dialogTableVisible"
    append-to-body
    width="600px"
    :z-index="2000"
    v-loading="searchNodeLoading"
    :before-close="reset"
    @open="addKeyDown"
    @close="removeKeyDown"
  >
    <el-form
      require-asterisk-position="right"
      label-position="top"
      ref="orgForm"
      class="org-editor-form"
      :model="form"
      @submit.prevent
      :rules="rule"
    >
      <el-row>
        <el-col :span="24">
          <el-form-item :label="t('org.name')" prop="name">
            <el-input
              v-model="form.name"
              :placeholder="`${t('common.please_input')} ${t('org.name')}`"
            />
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
              :data="state.treeData"
              check-strictly
              :render-after-expand="false"
              :placeholder="`${t('common.please_select')}${t('org.parent')}${
                isAdmin ? t('org.admin_parent_tips') : t('org.default_parent_tips')
              }`"
            />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row v-if="formType === 'add'">
        <el-col :span="24">
          <el-form-item :label="t('role.org_admin')">
            <el-select
              v-model="adminIds"
              multiple
              filterable
              :placeholder="`${t('common.please_select')}${t('role.org_admin')}`"
            >
              <el-option
                v-for="user in candidateAdmins"
                :key="user.id"
                :value="user.id"
                :label="user.name"
              >
                <span>{{ user.name }}</span>
                <span style="color: #8f959e; margin-left: 4px">({{ user.account }})</span>
              </el-option>
            </el-select>
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
.org-form-dialog {
  .ed-dialog__body {
    padding-bottom: 8px !important;
  }
}
</style>
<style scoped lang="less">
.org-tree-select {
  width: 100%;
}
.org-editor-form {
  :deep(.ed-form-item) {
    margin-bottom: 16px;
  }
  :deep(.is-error) {
    margin-bottom: 40px !important;
  }
}
</style>
