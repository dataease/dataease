<template>
  <InfoTemplate
    ref="infoTemplate"
    :label-tooltips="tooltips"
    setting-key="basic"
    setting-title="基础设置"
    :setting-data="state.templateList"
    @edit="edit"
  />
  <basic-edit ref="editor" @saved="refresh" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import InfoTemplate from '../../common/InfoTemplate.vue'
import BasicEdit from './BasicEdit.vue'
import request from '@/config/axios'
import { SettingRecord } from '@/views/system/common/SettingTemplate'
import { reactive } from 'vue'
import { cloneDeep } from 'lodash-es'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
const editor = ref()
const infoTemplate = ref()
const pvpOptions = [
  { value: '0', label: '永久' },
  { value: '1', label: '一年' },
  { value: '2', label: '半年' },
  { value: '3', label: '三个月' },
  { value: '4', label: '一个月' }
]
const tooltips = [
  {
    key: 'setting_basic.frontTimeOut',
    val: '请求超时时间(单位：秒，注意：保存后刷新浏览器生效)'
  },
  {
    key: 'setting_basic.platformOid',
    val: '作用域包括认证设置和平台对接'
  },
  {
    key: 'setting_basic.platformRid',
    val: '作用域包括认证设置和平台对接'
  }
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
  ]
})
let originData = []
const selectedOid = ref('')
const selectedOName = ref('')
const selectedRid = ref<string[]>([])
const selectedRName = ref<string[]>([])
const selectedPvp = ref('0')
const search = cb => {
  const url = '/sysParameter/basic/query'
  originData = []
  state.templateList = []
  request.get({ url }).then(async res => {
    originData = cloneDeep(res.data)
    const data = res.data
    for (let index = 0; index < data.length; index++) {
      const item = data[index]
      if (
        item.pkey === 'basic.autoCreateUser' ||
        item.pkey === 'basic.dip' ||
        item.pkey === 'basic.pwdStrategy'
      ) {
        item.pval = item.pval === 'true' ? '开启' : '未开启'
      } else if (item.pkey === 'basic.platformOid') {
        selectedOid.value = item.pval
        await loadOrgOptions()
        item.pval = selectedOName.value || '默认组织'
      } else if (item.pkey === 'basic.platformRid') {
        const pval = item.pval
        if (pval?.length) {
          const pvalArray = pval.split(',')
          selectedRid.value = pvalArray
          await loadRoleOptions()
          if (selectedRName.value.length) {
            item.pval = selectedRName.value.join(',')
          } else {
            item.pval = '普通角色'
          }
        } else {
          selectedRid.value = []
          item.pval = '普通角色'
        }
      } else if (item.pkey === 'basic.pvp') {
        selectedPvp.value = item.pval || '0'
        item.pval = pvpOptions.filter(cur => cur.value === selectedPvp.value)[0].label
      } else {
        item.pval = item.pval
      }
      item.pkey = 'setting_' + item.pkey
      state.templateList.push(item)
    }
    cb && cb()
  })
}
const refresh = () => {
  search(() => {
    infoTemplate?.value.init()
  })
}
refresh()

const edit = () => {
  editor?.value.edit(
    cloneDeep(originData),
    cloneDeep(state.orgOptions),
    cloneDeep(state.roleOptions)
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
</script>
