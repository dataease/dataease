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
const state = reactive({
  form: reactive({
    dsIntervalTime: '30',
    dsExecuteTime: 'minute'
  }),
  settingList: []
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

const buildSettingList = () => {
  return state.settingList.map(item => {
    const pkey = item.pkey.startsWith('basic.') ? item.pkey : `basic.${item.pkey}`
    const sort = item.sort
    const type = item.type
    const pval = state.form[item.pkey]
    return { pkey, pval, type, sort }
  })
}
const emits = defineEmits(['saved'])
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await formEl.validate(valid => {
    if (valid) {
      if (
        state.form.dsExecuteTime === 'minute' &&
        (Number(state.form.dsIntervalTime) < 1 || Number(state.form.dsIntervalTime) > 59)
      ) {
        ElMessage.error('分钟超出范围【1-59】')
        return
      }
      if (
        state.form.dsExecuteTime === 'hour' &&
        (Number(state.form.dsIntervalTime) < 1 || Number(state.form.dsIntervalTime) > 23)
      ) {
        ElMessage.error('小时超出范围【1-23】')
        return
      }
      const param = buildSettingList()
      if (param.length < 2) {
        return
      }
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
    }
  })
}

const resetForm = (formEl: FormInstance | undefined) => {
  state.settingList = []
  if (!formEl) return
  formEl.resetFields()
  dialogVisible.value = false
}

const reset = () => {
  resetForm(basicForm.value)
}

const showLoading = () => {
  loadingInstance.value = ElLoading.service({ target: '.basic-param-drawer' })
}
const closeLoading = () => {
  loadingInstance.value?.close()
}

const edit = list => {
  state.settingList = list.map(item => {
    const pkey = item.pkey
    item['label'] = `setting_${pkey}`
    item['pkey'] = pkey.split('.')[1]
    let pval = item.pval
    state.form[item['pkey']] = pval || state.form[item['pkey']]
    return item
  })
  dialogVisible.value = true
}
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    title="基础设置"
    v-model="dialogVisible"
    custom-class="basic-param-drawer"
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
      <el-form-item
        v-for="item in state.settingList"
        :key="item.pkey"
        :prop="item.pkey"
        :class="{ 'setting-hidden-item': item.pkey === 'dsExecuteTime' }"
        :label="t(item.label)"
      >
        <el-switch
          v-if="item.pkey === 'autoCreateUser'"
          active-value="true"
          inactive-value="false"
          v-model="state.form[item.pkey]"
        />
        <div v-else-if="item.pkey === 'dsIntervalTime'" class="ds-task-form-inline">
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
        <div v-else />
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
<style lang="less">
.basic-param-drawer {
  .ed-drawer__footer {
    height: 64px !important;
    padding: 16px 24px !important;
    .dialog-footer {
      height: 32px;
      line-height: 32px;
    }
  }
  .ed-form-item__label {
    line-height: 22px !important;
    height: 22px !important;
  }
}
</style>
<style scoped lang="less">
.basic-param-drawer {
  .ed-form-item {
    margin-bottom: 16px;
  }
  .is-error {
    margin-bottom: 40px !important;
  }
}
.setting-hidden-item {
  display: none !important;
}
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
