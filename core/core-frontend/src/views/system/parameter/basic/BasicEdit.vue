<script lang="ts" setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
const { t } = useI18n()
const dialogVisible = ref(false)
const loadingInstance = ref(null)
const basicForm = ref<FormInstance>()
const options = [
  { value: 'minute', label: '分钟（执行时间：0秒）' },
  { value: 'hour', label: '小时（执行时间：0分0秒）' }
]
interface BasicFrom {
  autoCreateUser?: boolean
  dsIntervalTime?: string
  dsExecuteTime?: string
}
const state = reactive({
  form: reactive<BasicFrom>({
    autoCreateUser: false,
    dsIntervalTime: '30',
    dsExecuteTime: 'minute'
  })
})

const rule = reactive<FormRules>({
  dsIntervalTime: [
    {
      required: true,
      message: t('common.require'),
      trigger: 'blur'
    }
  ]
})

const edit = row => {
  state.form = {
    autoCreateUser: row.autoCreateUser === 'true',
    dsIntervalTime: row.dsIntervalTime,
    dsExecuteTime: row.dsExecuteTime
  }
  dialogVisible.value = true
}

const emits = defineEmits(['saved'])

const buildSettingList = () => {
  const param = { ...state.form }
  const item0 = {
    pkey: 'basic.autoCreateUser',
    pval: param.autoCreateUser.toString(),
    type: 'text',
    sort: 1
  }
  const item1 = {
    pkey: 'basic.dsIntervalTime',
    pval: param.dsIntervalTime,
    type: 'text',
    sort: 2
  }
  const item2 = {
    pkey: 'basic.dsExecuteTime',
    pval: param.dsExecuteTime,
    type: 'text',
    sort: 3
  }
  return [item0, item1, item2]
}
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate((valid, fields) => {
    if (valid) {
      const param = buildSettingList()
      showLoading()
      request
        .post({ url: '/sysParameter/basic/save', data: param })
        .then(res => {
          if (!res.msg) {
            ElMessage.success(t('common.save_success'))
            emits('saved')
            reset()
          }
          closeLoading()
        })
        .catch(() => {
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
  resetForm(basicForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.basic-info-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    title="基础设置"
    v-model="dialogVisible"
    custom-class="basic-info-drawer"
    size="600px"
    direction="rtl"
  >
    <el-form
      ref="basicForm"
      require-asterisk-position="right"
      :model="state.form"
      :rules="rule"
      label-width="80px"
      label-position="top"
    >
      <el-form-item label="禁止扫码创建用户" prop="autoCreateUser">
        <el-switch v-model="state.form.autoCreateUser" />
      </el-form-item>

      <el-form-item label="数据源检测时间间隔" prop="dsIntervalTime">
        <div class="ds-task-form-inline">
          <span>每</span>
          <el-input-number
            v-model="state.form.dsIntervalTime"
            autocomplete="off"
            step-strictly
            class="text-left"
            :min="1"
            :placeholder="t('common.inputText')"
            controls-position="right"
            type="number"
          />
          <el-select v-model="state.form.dsExecuteTime">
            <el-option
              v-for="item in options"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
          <span class="ds-span">执行一次</span>
        </div>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="resetForm(basicForm)">{{ t('common.cancel') }}</el-button>
        <el-button type="primary" @click="submitForm(basicForm)">
          {{ t('commons.save') }}
        </el-button>
      </span>
    </template>
  </el-drawer>
</template>

<style scoped lang="less">
.ds-task-form-inline {
  width: 100%;
  display: flex;
  .ed-input-number {
    width: 140px;
    margin: 0 6px;
  }
  .ed-select {
    width: 240px;
    :deep(.ed-input) {
      width: 100% !important;
    }
  }
  span.ds-span {
    margin-left: 6px;
  }
}
</style>
