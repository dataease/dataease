<script lang="ts" setup>
import { useAttrs, computed } from 'vue'
import icon_visible_outlined from '@/assets/svg/icon_visible_outlined.svg'
import icon_invisible_outlined from '@/assets/svg/icon_invisible_outlined.svg'
import { hIcon } from '@/components/icon-custom'
const attrs = useAttrs()
const props = defineProps(['modelValue'])
const emits = defineEmits(['update:modelValue'])
const value = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})

const iconView = hIcon(icon_visible_outlined)
const iconHide = hIcon(icon_invisible_outlined)
</script>

<template>
  <el-input
    :icon-view-custom="iconView"
    :icon-hide-custom="iconHide"
    v-bind="attrs"
    v-model="value"
  />
</template>
