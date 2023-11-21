<template>
  <InfoTemplate
    :label-tooltips="tooltips"
    setting-title="邮件设置"
    setting-key="email"
    @edit="edit"
  />
  <email-edit ref="editor" />
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import InfoTemplate from '../../common/InfoTemplate.vue'
import EmailEdit from './EmailEdit.vue'
const tooltips = [
  {
    key: '测试收件人',
    val: '测试收件人xxxx'
  },
  {
    key: 'SSL',
    val: 'SSLxxxx'
  },
  {
    key: 'TSL',
    val: 'TSLxxxx'
  }
]

const editor = ref()

const edit = () => {
  editor?.value.edit()
}
</script>
