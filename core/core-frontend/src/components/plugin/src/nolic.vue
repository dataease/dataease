<template>
  <div v-if="props.errorTips">
    <h2>当前不是企业版，请上传许可证</h2>
  </div>
  <div v-else></div>
</template>

<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
const props = defineProps({
  errorTips: propTypes.bool.def(false)
})
</script>
