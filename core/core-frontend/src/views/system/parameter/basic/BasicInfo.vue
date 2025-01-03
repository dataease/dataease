<template>
  <InfoTemplate
    ref="infoTemplate"
    :label-tooltips="tooltips"
    setting-key="basic"
    :setting-title="t('system.basic_settings')"
    :setting-data="baseInfoSettings"
    @edit="
      edit(
        t('system.basic_settings'),
        baseInfoSettings.map(ele => ele.pkey.split('.')[1])
      )
    "
  />
  <InfoTemplate
    v-if="loginInoSettings?.length"
    ref="loginTemplate"
    class="login-setting-template"
    :label-tooltips="tooltips"
    setting-key="basic"
    :setting-title="t('system.login_settings')"
    :setting-data="loginInoSettings"
    @edit="
      edit(
        t('system.login_settings'),
        loginInoSettings.map(ele => ele.pkey.split('.')[1])
      )
    "
  />

  <InfoTemplate
    v-if="thirdInfoSettings?.length"
    ref="thirdTemplate"
    class="login-setting-template"
    :label-tooltips="tooltips"
    setting-key="basic"
    :setting-title="t('setting_basic.third_platform_settings')"
    :setting-data="thirdInfoSettings"
    @edit="
      edit(
        t('setting_basic.third_platform_settings'),
        thirdInfoSettings.map(ele => ele.pkey.split('.')[1])
      )
    "
  />
  <basic-edit ref="editor" :label-tooltips="tooltips" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref, computed, nextTick } from 'vue'
import InfoTemplate from '../../common/InfoTemplate.vue'
import BasicEdit from './BasicEdit.vue'
import request from '@/config/axios'
import { SettingRecord } from '@/views/system/common/SettingTemplate'
import { reactive } from 'vue'
import { isDesktop } from '@/utils/ModelUtil'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const editor = ref()
const infoTemplate = ref()
const loginTemplate = ref()
const thirdTemplate = ref()
const showDefaultLogin = ref(false)
const desktop = isDesktop()
const pvpOptions = [
  { value: '0', label: t('commons.date.permanent') },
  { value: '1', label: t('commons.date.one_year') },
  { value: '2', label: t('commons.date.six_months') },
  { value: '3', label: t('commons.date.three_months') },
  { value: '4', label: t('commons.date.one_month') }
]
const tooltips = [
  {
    key: 'setting_basic.defaultOpen',
    val: t('setting_basic.default_open_tips')
  },
  {
    key: 'setting_basic.frontTimeOut',
    val: t('system.to_take_effect')
  },
  {
    key: 'setting_basic.platformOid',
    val: t('system.and_platform_docking')
  },
  {
    key: 'setting_basic.platformRid',
    val: t('system.and_platform_docking')
  },
  {
    key: 'setting_basic.shareDisable',
    val: t('setting_basic.share_disable_tips')
  }
]
const loginSettings = [
  'setting_basic.dip',
  'setting_basic.pvp',
  'setting_basic.defaultLogin',
  'setting_basic.loginLimit',
  'setting_basic.loginLimitRate',
  'setting_basic.loginLimitTime'
]

const thirdSettings = [
  'setting_basic.autoCreateUser',
  'setting_basic.platformOid',
  'setting_basic.platformRid'
]
const state = reactive({
  templateList: [] as SettingRecord[],
  orgOptions: [],
  roleOptions: [
    {
      value: 'admin',
      label: t('role.org_admin'),
      children: null,
      disabled: false
    },
    {
      value: 'readonly',
      label: t('role.average_role'),
      children: null,
      disabled: false
    }
  ],
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
let originData = []
const selectedOid = ref('')
const selectedOName = ref('')
const selectedRid = ref<string[]>([])
const selectedRName = ref<string[]>([])
const selectedPvp = ref('0')

const baseInfoSettings = computed(() =>
  state.templateList.filter(
    item =>
      !loginSettings.concat(thirdSettings).includes(item.pkey) &&
      (!desktop || item.pkey !== 'setting_basic.defaultOpen')
  )
)

const thirdInfoSettings = computed(() =>
  state.templateList.filter(item => thirdSettings.includes(item.pkey))
)
const loginInoSettings = computed(() => {
  const list = state.templateList.filter(item => loginSettings.includes(item.pkey))
  return list
})

const search = cb => {
  const url = '/sysParameter/basic/query'
  originData = []
  state.templateList = []
  const resultList = []
  request.get({ url }).then(async res => {
    originData = cloneDeep(res.data)
    const data = res.data
    for (let index = 0; index < data.length; index++) {
      const item = data[index]
      if (
        item.pkey === 'basic.autoCreateUser' ||
        item.pkey === 'basic.dip' ||
        item.pkey === 'basic.pwdStrategy' ||
        item.pkey === 'basic.shareDisable' ||
        item.pkey === 'basic.sharePeRequire' ||
        item.pkey === 'basic.loginLimit'
      ) {
        item.pval = item.pval === 'true' ? t('chart.open') : t('system.not_enabled')
      } else if (item.pkey === 'basic.platformOid') {
        selectedOid.value = item.pval
        await loadOrgOptions()
        item.pval = selectedOName.value || t('system.default_organization')
      } else if (item.pkey === 'basic.platformRid') {
        const pval = item.pval
        if (pval?.length) {
          const pvalArray = pval.split(',')
          selectedRid.value = pvalArray
          await loadRoleOptions()
          if (selectedRName.value.length) {
            item.pval = selectedRName.value.join(',')
          } else {
            item.pval = t('system.normal_role')
          }
        } else {
          selectedRid.value = []
          item.pval = t('system.normal_role')
        }
      } else if (item.pkey === 'basic.pvp') {
        selectedPvp.value = item.pval || '0'
        item.pval = pvpOptions.filter(cur => cur.value === selectedPvp.value)[0].label
      } else if (item.pkey === 'basic.defaultLogin') {
        await queryCategoryStatus()
        if (showDefaultLogin.value) {
          if (item.pval) {
            const r = state.loginOptions.filter(cur => cur.value === item.pval)
            if (r?.length) {
              item.pval = r[0].label
            } else {
              item.pval = state.loginOptions[0].label
              resetDefaultLogin()
            }
          } else {
            item.pval = state.loginOptions[0].label
          }
        }
      } else if (item.pkey === 'basic.defaultSort') {
        if (item.pval) {
          const r = state.sortOptions.filter(cur => cur.value === item.pval)
          if (r?.length) {
            item.pval = r[0].label
          } else {
            item.pval = state.sortOptions[1].label
          }
        } else {
          item.pval = state.sortOptions[1].label
        }
      } else if (item.pkey === 'basic.defaultOpen') {
        if (item.pval) {
          const r = state.openOptions.filter(cur => cur.value === item.pval)
          if (r?.length) {
            item.pval = r[0].label
          } else {
            item.pval = state.openOptions[0].label
          }
        } else {
          item.pval = state.openOptions[0].label
        }
      } else {
        item.pval = item.pval
      }
      item.pkey = 'setting_' + item.pkey
      if (!item.pkey.includes('defaultLogin') || showDefaultLogin.value) {
        resultList.push(item)
      }
    }
    state.templateList.splice(0, resultList.length, ...resultList)
    cb && cb()
  })
}
const refresh = () => {
  search(() => {
    nextTick(() => {
      infoTemplate?.value?.init()
      loginTemplate?.value?.init()
      thirdTemplate?.value?.init()
    })
  })
}
refresh()

const edit = (val, arr) => {
  editor?.value.edit(
    cloneDeep(originData),
    cloneDeep(state.orgOptions),
    cloneDeep(state.roleOptions),
    cloneDeep(state.loginOptions),
    cloneDeep(state.sortOptions),
    cloneDeep(state.openOptions),
    val,
    arr
  )
}
const loadOrgOptions = async () => {
  const res = await request.post({ url: '/org/mounted', data: {} })
  const data = res.data
  formatOrg(data)
  state.orgOptions = data
}
const loadRoleOptions = async () => {
  const res = await request.get({ url: `/role/queryWithOid/${selectedOid.value}` })
  const data = res.data
  const map = groupBy(data)
  state.roleOptions[0].children = map.get(false)
  state.roleOptions[1].children = map.get(true)
}
const formatOrg = list => {
  const stack = [...list]
  while (stack.length) {
    const item = stack.pop()
    if (item.id === selectedOid.value) {
      selectedOName.value = item.name
    }
    item.value = item.id
    item.label = item.name
    item.disabled = item.readOnly
    if (item.children?.length) {
      item.children.forEach(kid => stack.push(kid))
    }
  }
}

const groupBy = list => {
  const map = new Map()
  selectedRName.value = []
  list.forEach(item => {
    if (selectedRid.value.includes(item.id)) {
      selectedRName.value.push(item.name)
    }
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
const queryCategoryStatus = async () => {
  const url = `/setting/authentication/status`
  const res = await request.get({ url })
  const data = res.data
  const map = data.reduce((acc, { name, enable }) => {
    acc[name] = enable
    return acc
  }, {})
  let len = state.loginOptions.length
  while (len--) {
    const item = state.loginOptions[len]
    if (item.value !== '0' && !map[item.label.toLocaleLowerCase()]) {
      state.loginOptions.splice(len, 1)
    }
  }
  showDefaultLogin.value = state.loginOptions.length > 1
  if (!showDefaultLogin.value) {
    let len = originData.length
    while (len--) {
      const item = originData[len]
      if (item.pkey === 'basic.defaultLogin') {
        originData.splice(len, 1)
      }
    }
  }
}
const resetDefaultLogin = () => {
  let len = originData.length
  while (len--) {
    const item = originData[len]
    if (item.pkey === 'basic.defaultLogin') {
      item.pval = '0'
    }
  }
}
</script>
<style lang="less" scoped>
.login-setting-template {
  margin-top: 16px;
}
</style>
