<script setup lang="ts">
import { ref, watch } from 'vue'
import MaterialExtension from './MaterialExtension.vue'
import { isMaterialReference, useMaterialLibrary } from './useMaterialLibrary'
import { useI18n } from '@/hooks/web/useI18n'

const props = withDefaults(defineProps<{ disabled?: boolean; themes?: EditorTheme }>(), {
  disabled: false,
  themes: 'dark'
})
const emit = defineEmits<{ (event: 'select', reference: string): void }>()
const { t } = useI18n()
const open = ref(false)
const { available, context } = useMaterialLibrary()
const select = (reference: string) => {
  if (!available.value || props.disabled || !isMaterialReference(reference)) return
  emit('select', reference)
  open.value = false
}
watch(available, value => {
  if (!value) open.value = false
})
</script>

<template>
  <el-button
    v-if="available"
    text
    type="primary"
    size="small"
    :disabled="disabled"
    :effect="themes"
    class="material-picker-trigger"
    @click="open = true"
  >
    {{ t('material.choose') }}
  </el-button>
  <el-dialog
    v-if="open"
    v-model="open"
    :modal="false"
    draggable
    :title="t('material.choose')"
    width="1000px"
    class="material-picker-dialog"
    :class="{ 'is-dark': themes === 'dark' }"
    :effect="themes"
    destroy-on-close
    append-to-body
  >
    <MaterialExtension
      :context="context"
      :themes="themes"
      @select="select"
      @cancel="open = false"
    />
  </el-dialog>
</template>

<style lang="less" scoped>
.material-picker-trigger {
  padding-inline: 0;
}
</style>
