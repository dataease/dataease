<script lang="ts" setup>
import { ref, reactive, PropType } from 'vue'
import { ElMessage, ElLoading } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import type { FormInstance, FormRules } from 'element-plus-secondary'
import request from '@/config/axios'
import dvInfo from '@/assets/svg/dv-info.svg'
const { t } = useI18n()

const props = defineProps({
  labelTooltips: {
    type: Array as PropType<any[]>,
    default: () => []
  }
})

const dialogVisible = ref(false)
const loadingInstance = ref(null)
const basicForm = ref<FormInstance>()
const options = [
  { value: 'minute', label: t('system.time_0_seconds') },
  { value: 'hour', label: t('system.and_0_seconds_de') }
]
const pvpOptions = [
  { value: '0', label: t('commons.date.permanent') },
  { value: '1', label: t('commons.date.one_year') },
  { value: '2', label: t('commons.date.six_months') },
  { value: '3', label: t('commons.date.three_months') },
  { value: '4', label: t('commons.date.one_month') }
]
const requireKeys = [
  'logLiveTime',
  'thresholdLogLiveTime',
  'exportFileLiveTime',
  'frontTimeOut',
  'loginLimitTime',
  'loginLimitRate'
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
    { value: '0', label: t('system.normal_login') },
    { value: '1', label: 'LDAP' },
    { value: '2', label: 'OIDC' },
    { value: '3', label: 'CAS' },
    { value: '9', label: 'OAuth2' }
  ],
  sortOptions: [
    { value: '0', label: t('resource_sort.time_asc') },
    { value: '1', label: t('resource_sort.time_desc') },
    { value: '2', label: t('resource_sort.name_asc') },
    { value: '3', label: t('resource_sort.name_desc') }
  ],
  openOptions: [
    { value: '0', label: t('open_opt.new_page') },
    { value: '1', label: t('open_opt.local_page') }
  ]
})

const tooltipItem = ref({})
const formatLabel = () => {
  props.labelTooltips?.length &&
    props.labelTooltips.forEach(tooltip => {
      tooltipItem.value[tooltip.key] = tooltip.val
    })
}

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
        ElMessage.error(t('commons.date.of_range_1_59'))
        return
      }
      if (
        state.form.dsExecuteTime === 'hour' &&
        (Number(state.form.dsIntervalTime) < 1 || Number(state.form.dsIntervalTime) > 23)
      ) {
        ElMessage.error(t('commons.date.of_range_1_23'))
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
  settingList.value = []
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
const title = ref()
const settingList = ref([])
const edit = (
  list,
  orgOptions,
  roleOptions,
  loginOptions,
  sortOptions,
  openOptions,
  titleVal,
  settingListVal
) => {
  title.value = titleVal
  state.orgOptions = orgOptions || []
  state.roleOptions = roleOptions || []
  state.loginOptions = loginOptions || []
  state.sortOptions = sortOptions || []
  state.openOptions = openOptions || []
  state.settingList = list.map(item => {
    const pkey = item.pkey
    if (requireKeys.some(requireKey => `basic.${requireKey}` === pkey)) {
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

  settingList.value = state.settingList.filter(ele => settingListVal.includes(ele.pkey))
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
formatLabel()
defineExpose({
  edit
})
</script>

<template>
  <el-drawer
    :title="title"
    v-model="dialogVisible"
    modal-class="basic-param-drawer"
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
        v-for="item in settingList"
        :key="item.pkey"
        :prop="item.pkey"
        :class="{ 'setting-hidden-item': item.pkey === 'dsExecuteTime' }"
      >
        <template v-slot:label>
          <div class="basic-form-info-tips">
            <span class="custom-form-item__label">{{ t(item.label) }}</span>
            <el-tooltip
              v-if="tooltipItem[`setting_basic.${item.pkey}`]"
              effect="dark"
              :content="tooltipItem[`setting_basic.${item.pkey}`]"
              placement="top"
            >
              <el-icon
                ><Icon name="dv-info"><dvInfo class="svg-icon" /></Icon
              ></el-icon>
            </el-tooltip>
          </div>
        </template>
        <el-switch
          class="de-basic-switch"
          v-if="
            item.pkey === 'autoCreateUser' ||
            item.pkey === 'pwdStrategy' ||
            item.pkey === 'dip' ||
            item.pkey === 'shareDisable' ||
            item.pkey === 'sharePeRequire' ||
            item.pkey === 'loginLimit'
          "
          active-value="true"
          inactive-value="false"
          v-model="state.form[item.pkey]"
        />
        <div v-else-if="item.pkey === 'dsIntervalTime'" class="ds-task-form-inline">
          <span>{{ t('cron.every') }}</span>
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
          <span class="ds-span">{{ t('cron.every_exec') }}</span>
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
        <div
          v-else-if="
            item.pkey === 'logLiveTime' ||
            item.pkey === 'thresholdLogLiveTime' ||
            item.pkey === 'loginLimitRate' ||
            item.pkey === 'loginLimitTime'
          "
        >
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
        <div v-else-if="item.pkey === 'defaultSort'">
          <el-radio-group v-model="state.form[item.pkey]">
            <el-radio v-for="item in state.sortOptions" :key="item.value" :label="item.value">
              {{ item.label }}
            </el-radio>
          </el-radio-group>
        </div>
        <div v-else-if="item.pkey === 'defaultOpen'">
          <el-radio-group v-model="state.form[item.pkey]">
            <el-radio v-for="item in state.openOptions" :key="item.value" :label="item.value">
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

    .basic-form-info-tips {
      width: fit-content;
      display: inline-flex;
      align-items: center;
      column-gap: 4px;
    }
  }

  .ed-form-item {
    &.is-required.asterisk-right {
      .ed-form-item__label:after {
        display: none;
      }
      .basic-form-info-tips {
        .custom-form-item__label:after {
          content: '*';
          color: var(--ed-color-danger);
          margin-left: 2px;
          font-family: var(--de-custom_font, 'PingFang');
          font-size: 14px;
          font-style: normal;
          font-weight: 400;
        }
      }
    }
  }
  .ed-radio__label {
    font-weight: 400;
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
