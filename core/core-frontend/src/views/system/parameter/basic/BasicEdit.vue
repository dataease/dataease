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
const pvpOptions = [
  { value: '0', label: '永久' },
  { value: '1', label: '一年' },
  { value: '2', label: '半年' },
  { value: '3', label: '三个月' },
  { value: '4', label: '一个月' }
]

const state = reactive({
  form: reactive({
    dsIntervalTime: '30',
    dsExecuteTime: 'minute',
    frontTimeOut: '30'
  }),
  settingList: [],
  orgOptions: [],
  roleOptions: [],
  loginOptions: [
    { value: '0', label: '普通登录' },
    { value: '1', label: 'LDAP' },
    { value: '2', label: 'OIDC' },
    { value: '3', label: 'CAS' }
  ]
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
    let pval = state.form[item.pkey]
    if (Array.isArray(pval)) {
      pval = pval.join(',')
    }
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

const edit = (list, orgOptions, roleOptions, loginOptions) => {
  state.orgOptions = orgOptions || []
  state.roleOptions = roleOptions || []
  state.loginOptions = loginOptions || []
  state.settingList = list.map(item => {
    const pkey = item.pkey
    if (pkey === 'basic.logLiveTime') {
      rule[pkey.split('.')[1]] = [
        {
          required: true,
          message: t('common.require'),
          trigger: ['blur', 'change']
        }
      ]
    }
    if (pkey === 'basic.exportFileLiveTime') {
      rule[pkey.split('.')[1]] = [
        {
          required: true,
          message: t('common.require'),
          trigger: ['blur', 'change']
        }
      ]
    }
    item['label'] = `setting_${pkey}`
    item['pkey'] = pkey.split('.')[1]
    let pval = item.pval
    if (item.pkey.includes('platformRid') && pval?.length) {
      pval = pval.split(',')
      if (!rule['platformRid']) {
        rule['platformRid'] = [
          {
            required: true,
            message: t('common.require'),
            trigger: ['blur', 'change']
          }
        ]
      }
    }
    if (item.pkey.includes('platformOid')) {
      if (!rule['platformOid']) {
        rule['platformOid'] = [
          {
            required: true,
            message: t('common.require'),
            trigger: ['blur', 'change']
          }
        ]
      }
    }
    state.form[item['pkey']] = pval || state.form[item['pkey']]
    return item
  })
  dialogVisible.value = true
}
const loadRoleOptions = async () => {
  const oid = state.form['platformOid']
  if (!oid) {
    return
  }
  const res = await request.get({ url: `/role/queryWithOid/${oid}` })
  const data = res.data
  const map = groupBy(data)
  state.roleOptions[0].children = map.get(false)
  state.roleOptions[1].children = map.get(true)
}
const groupBy = list => {
  const map = new Map()
  list.forEach(item => {
    const readonly = item.readonly
    let arr = map.get(readonly)
    if (!arr) {
      arr = []
    }
    arr.push({ value: item.id, label: item.name, disabled: false })
    map.set(readonly, arr)
  })
  return map
}
const oidChange = () => {
  state.form['platformRid'] = []
  loadRoleOptions()
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
          class="de-basic-switch"
          v-if="
            item.pkey === 'autoCreateUser' || item.pkey === 'pwdStrategy' || item.pkey === 'dip'
          "
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
        <div v-else-if="item.pkey === 'frontTimeOut'">
          <el-input-number
            v-model="state.form.frontTimeOut"
            autocomplete="off"
            step-strictly
            class="text-left edit-all-line"
            :min="1"
            :placeholder="t('common.inputText')"
            controls-position="right"
            type="number"
          />
        </div>
        <div v-else-if="item.pkey === 'logLiveTime'">
          <el-input-number
            v-model="state.form[item.pkey]"
            autocomplete="off"
            step-strictly
            class="text-left edit-all-line"
            :min="1"
            :max="4000"
            :placeholder="t('common.inputText')"
            controls-position="right"
            type="number"
          />
        </div>
        <div v-else-if="item.pkey === 'platformOid'">
          <el-tree-select
            class="edit-all-line"
            v-model="state.form[item.pkey]"
            :data="state.orgOptions"
            check-strictly
            :render-after-expand="false"
            @change="oidChange"
          />
        </div>
        <div v-else-if="item.pkey === 'platformRid'">
          <el-tree-select
            class="edit-all-line"
            v-model="state.form[item.pkey]"
            :data="state.roleOptions"
            :highlight-current="true"
            multiple
            :render-after-expand="false"
            :placeholder="$t('common.please_select') + $t('user.role')"
            show-checkbox
            check-on-click-node
          />
        </div>
        <div v-else-if="item.pkey === 'pvp'">
          <el-select v-model="state.form[item.pkey]" class="edit-all-line">
            <el-option
              v-for="item in pvpOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </div>
        <div v-else-if="item.pkey === 'exportFileLiveTime'">
          <el-input-number
            v-model="state.form[item.pkey]"
            autocomplete="off"
            step-strictly
            class="text-left edit-all-line"
            :min="1"
            :max="4000"
            :placeholder="t('common.inputText')"
            controls-position="right"
            type="number"
          />
        </div>
        <div v-else-if="item.pkey === 'defaultLogin'">
          <el-radio-group v-model="state.form[item.pkey]">
            <el-radio v-for="item in state.loginOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </div>
        <v-else />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button secondary @click="resetForm(basicForm)">{{ t('common.cancel') }}</el-button>
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
    box-shadow: 0 -1px 4px #1f232926 !important;
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
  .edit-all-line {
    width: 552px !important;
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
.de-basic-switch {
  height: 22px;
}
</style>
