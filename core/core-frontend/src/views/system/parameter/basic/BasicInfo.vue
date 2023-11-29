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
const editor = ref()
const infoTemplate = ref()
const tooltips = [
  {
    key: '请求超时时间',
    val: '请求超时时间(单位：秒，注意：保存后刷新浏览器生效)'
  }
]
const state = reactive({
  templateList: [] as SettingRecord[]
})
let originData = []
const search = cb => {
  const url = '/sysParameter/basic/query'
  originData = []
  state.templateList = []
  request.get({ url }).then(res => {
    originData = cloneDeep(res.data)
    const data = res.data
    for (let index = 0; index < data.length; index++) {
      const item = data[index]
      if (item.pkey === 'basic.autoCreateUser') {
        item.pval = item.pval === 'true' ? '开启' : '未开启'
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
  editor?.value.edit(cloneDeep(originData))
}
</script>
