<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import { searchExternalUserApi, mountExternalUserApi } from '@/api/user'
interface RoleForm {
  id?: string | number
}
interface ExternalUser {
  uid: string | number
  account: string
  name: string
  email: string
  phonePrefix: string
  phone: string
}

const { t } = useI18n()
const loadingInstance = ref(null)
const dialogVisible = ref(false)
const loading = ref(false)
const roleId = ref(null)
const roleForm = ref<FormInstance>()
const searchMsg = ref('')
const searchExist = ref(false)
const externalUser = reactive<ExternalUser>({})
const form = reactive<RoleForm>({
  id: null
})

const rule = reactive<FormRules>({
  id: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
})

const init = rid => {
  roleId.value = rid
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])

const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      if (searchExist.value && externalUser?.uid) {
        const param = { rid: roleId.value, uid: externalUser.uid }
        showLoading()
        mountExternalUserApi(param).then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            emits('saved')
            reset()
          }
          closeLoading()
        })
      }
    } else {
      console.log('error submit!', fields)
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  roleId.value = null
  searchMsg.value = null
  searchExist.value = false
  Object.assign(externalUser, {
    uid: '',
    account: '',
    name: '',
    email: '',
    phone: '',
    phonePrefix: ''
  })
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
const searchUser = () => {
  if (!form.id) {
    return
  }
  searchExternalUserApi(form.id).then(res => {
    if (res.data) {
      searchMsg.value = t('role.search_one')
      searchExist.value = true
      Object.assign(externalUser, res.data)
    } else {
      searchMsg.value = t('role.search_no')
      searchExist.value = false
    }
    closeLoading()
  })
}
defineExpose({
  init
})
</script>

<template>
  <el-dialog
    custom-class="role-form-dialog"
    v-loading="loading"
    :before-close="reset"
    v-model="dialogVisible"
    :title="t('role.out_user_title')"
    width="540px"
  >
    <el-form
      ref="roleForm"
      require-asterisk-position="right"
      :model="form"
      :rules="rule"
      label-width="80px"
    >
      <el-form-item :label="$t('user.user_id')" prop="id">
        <el-col :span="19">
          <el-input v-model="form.id" :placeholder="$t('user.user_id_empty')" />
        </el-col>
        <el-col :span="1" class="text-center">
          <span class="text-gray-500"></span>
        </el-col>
        <el-col :span="4">
          <el-button :disabled="!form.id" text @click="searchUser">{{
            t('role.search_user')
          }}</el-button>
        </el-col>
      </el-form-item>
      <div class="search-result-container">
        <div :class="'search-result-' + searchExist">{{ searchMsg }}</div>
        <div v-if="searchExist && externalUser">
          <el-row>
            <el-col :span="6">{{ t('user.account') + ':' }}</el-col>
            <el-col :span="18">{{ externalUser.account }}</el-col>
          </el-row>

          <el-row>
            <el-col :span="6">{{ t('user.name') + ':' }}</el-col>
            <el-col :span="18">{{ externalUser.name }}</el-col>
          </el-row>

          <el-row>
            <el-col :span="6">{{ t('common.phone') + ':' }}</el-col>
            <el-col :span="18">{{ externalUser.phone }}</el-col>
          </el-row>
          <el-row>
            <el-col :span="6">{{ t('common.email') + ':' }}</el-col>
            <el-col :span="18">{{ externalUser.email }}</el-col>
          </el-row>
        </div>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(roleForm)">{{ t('common.cancel') }}</el-button>
        <el-button
          :disabled="!searchExist || !externalUser"
          type="primary"
          @click="submitForm(roleForm)"
        >
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
.search-result-container {
  padding-left: 80px;
  height: 100px;
  .search-result-true {
    color: #039d12;
  }
  .search-result-false {
    color: #d9001b;
  }
}
</style>
