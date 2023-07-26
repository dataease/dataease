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
const externalUser = reactive<ExternalUser>({
  uid: '',
  account: '',
  name: '',
  email: '',
  phonePrefix: '',
  phone: ''
})
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
      label-width="0px"
    >
      <el-form-item label="" prop="id">
        <el-input
          v-model="form.id"
          :placeholder="$t('user.user_id_empty')"
          class="input-with-select"
          :class="{ 'disabled-append': !form.id }"
          clearable
        >
          <template #append>
            <el-button
              class="de-input-append-button"
              :disabled="!form.id"
              text
              @click="searchUser"
              >{{ t('role.search_user') }}</el-button
            >
          </template>
        </el-input>
      </el-form-item>
      <div class="search-result-container">
        <div :class="'search-result-' + searchExist">{{ searchMsg }}</div>
        <div v-if="searchExist && externalUser">
          <div class="user-label" :title="externalUser.name + '(' + externalUser.account + ')'">
            <span> {{ externalUser.name }}</span>
            <span>{{ '(' + externalUser.account + ')' }}</span>
          </div>
        </div>
      </div>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(roleForm)">{{ t('common.cancel') }}</el-button>
        <el-button
          :disabled="!searchExist || !externalUser"
          :type="searchExist && externalUser ? 'primary' : 'info'"
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
  ::v-deep .ed-input-group__append {
    width: 70px;
    background: none;
  }
}
.input-with-select {
  &:not(.disabled-append) ::v-deep .ed-input-group__append {
    &:hover {
      border-left: 0;
      border-top-left-radius: 0;
      border-bottom-left-radius: 0;
      box-shadow: 0 1px 0 0 var(--ed-color-primary) inset, 0 -1px 0 0 var(--ed-color-primary) inset,
        1px 0 0 0 var(--ed-color-primary) inset, -1px 0 0 0 var(--ed-color-primary) inset;
    }
  }
}
.search-result-container {
  height: 100px;
  .search-result-true {
    display: none;
    color: #039d12;
  }
  .search-result-false {
    // color: #d9001b;
    color: #8d9199;
    font-size: 14px;
  }

  .user-label {
    // display: flex;
    // flex-direction: row;
    height: 22px;
    font-family: PingFang SC;
    font-weight: 400;
    font-style: normal;
    overflow: hidden;
    text-overflow: ellipsis;
    -o-text-overflow: ellipsis;
    -webkit-text-overflow: ellipsis;
    -moz-text-overflow: ellipsis;
    white-space: nowrap;
    width: 490px;

    :nth-child(1) {
      font-size: 14px;
      line-height: 22px;
    }

    :nth-child(2) {
      color: #8d9199;
      font-size: 12px;
      line-height: 20px;
      margin-top: 1px;
    }
  }
}
.de-input-append-button {
  border-radius: 0px;
  padding: 0px 7px;
  height: 30px;
  line-height: 30px;
  &:not(.is-disabled):hover {
    color: var(--ed-color-primary) !important;
  }
}
</style>
