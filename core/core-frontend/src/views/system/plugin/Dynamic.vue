<script lang="ts" setup>
import { propTypes } from '@/utils/propTypes'
defineProps({
  jsname: propTypes.string.def(''),
  menuid: propTypes.number.def(null),
  noLayout: propTypes.bool.def(false)
})
</script>

<template>
  <div>
    {{ jsname }}
  </div>
</template>

<style lang="less" scoped></style>
