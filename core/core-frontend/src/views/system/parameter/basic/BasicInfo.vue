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
const editor = ref()
const infoTemplate = ref()
const tooltips = [
  {
    key: '请求超时时间',
    val: '请求超时时间(单位：秒，注意：保存后刷新浏览器生效)'
  }
]
const state = reactive({
  templateList: [
    {
      pkey: '禁止扫码创建用户',
      pval: '未开启',
      type: 'text',
      sort: 1
    },
    {
      pkey: '数据源检测时间间隔',
      pval: '100',
      type: 'text',
      sort: 2
    },
    {
      pkey: '数据源检测频率',
      pval: 'minute',
      type: 'text',
      sort: 3
    }
  ] as SettingRecord[]
})

const search = cb => {
  const url = '/sysParameter/basic/query'
  request.get({ url }).then(res => {
    const data = res.data
    for (let index = 0; index < data.length; index++) {
      const item = data[index]
      if (index === 0) {
        state.templateList[index].pval = item.pval === 'true' ? '开启' : '未开启'
      } else {
        state.templateList[index].pval = item.pval
      }
    }
    cb && cb()
  })
}
const refresh = () => {
  search(() => {
    infoTemplate?.value.init()
  })
}
search(null)

const edit = () => {
  const param = {
    autoCreateUser: state.templateList[0].pval === '开启' ? 'true' : 'false',
    dsIntervalTime: state.templateList[1].pval,
    dsExecuteTime: state.templateList[2].pval
  }
  editor?.value.edit(param)
}
</script>
