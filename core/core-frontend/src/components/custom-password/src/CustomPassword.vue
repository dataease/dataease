<script lang="ts" setup>
import { useAttrs, computed, ref } from 'vue'
import SensitiveRevealDialog from './SensitiveRevealDialog.vue'
import icon_visible_outlined from '@/assets/svg/icon_visible_outlined.svg'
import icon_invisible_outlined from '@/assets/svg/icon_invisible_outlined.svg'
const attrs = useAttrs()
defineOptions({ inheritAttrs: false })
const props = defineProps<{
  modelValue?: string
  revealResourceId?: string | number
  revealSecretType?: import('@/api/sensitive').SecretType
}>()
// Respect the original field's visibility control; credentials alone never add a new entry point.
const showPassword = computed(() => {
  const enabled = attrs['show-password'] ?? attrs.showPassword
  return enabled === '' || enabled === true
})
const canReveal = computed(
  () =>
    showPassword.value &&
    props.modelValue === '******' &&
    !!props.revealResourceId &&
    !!props.revealSecretType
)
const revealDialog = ref<InstanceType<typeof SensitiveRevealDialog>>()
const handlePasswordClick = (event: MouseEvent) => {
  // Intercept the input's existing eye icon before it toggles local visibility.
  if (!canReveal.value || !(event.target instanceof Element)) return
  if (!event.target.closest('[class*="-input__password"]')) return
  event.preventDefault()
  event.stopPropagation()
  revealDialog.value?.open()
}
const emits = defineEmits(['update:modelValue'])
const value = computed({
  get() {
    return props.modelValue
  },
  set(value) {
    emits('update:modelValue', value)
  }
})
</script>

<template>
  <div style="display: contents" @click.capture="handlePasswordClick">
    <el-input
      :icon-view-custom="icon_visible_outlined"
      :icon-hide-custom="icon_invisible_outlined"
      v-bind="attrs"
      v-model="value"
      :show-password="showPassword"
    />
    <SensitiveRevealDialog
      v-if="canReveal"
      ref="revealDialog"
      :resource-id="revealResourceId!"
      :secret-type="revealSecretType!"
    />
  </div>
</template>
