<template>
  <Icon class-name="de-svg-main" :name="element.innerType" />
</template>

<script setup lang="ts">
import Icon from '@/components/icon-custom/src/Icon.vue'
import { toRefs } from 'vue'
const props = defineProps({
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        innerType: null
      }
    }
  }
})

const { propValue, element } = toRefs(props)
</script>

<style lang="less" scoped>
.de-svg-main {
  width: 100%;
  height: 100%;
}
</style>
