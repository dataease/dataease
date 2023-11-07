<template>
  <InfoTemplate :label-tooltips="tooltips" setting-key="basic" @edit="edit" />
  <basic-edit ref="editor" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import InfoTemplate from '../../common/InfoTemplate.vue'
import BasicEdit from './BasicEdit.vue'

const editor = ref()
const tooltips = [
  {
    key: '请求超时时间',
    val: '请求超时时间(单位：秒，注意：保存后刷新浏览器生效)'
  }
]

const edit = () => {
  editor?.value.edit()
}
</script>
