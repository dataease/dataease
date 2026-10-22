<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import materialStoreIcon from '@/assets/svg/dv-material-store.svg'
import MaterialExtension from './MaterialExtension.vue'
import { materialCapabilities, materialContext } from '@/api/material'
import extensionEnabled from 'virtual:dataease-material-enabled'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import { MATERIAL_LIBRARY_ENTRY_ENABLED } from '@/config/material'

const props = withDefaults(
  defineProps<{ disabled?: boolean; themes?: EditorTheme; toolbar?: boolean }>(),
  {
    disabled: false,
    themes: 'dark',
    toolbar: false
  }
)
const emit = defineEmits<{ (event: 'select', reference: string): void }>()
const { t } = useI18n()
const open = ref(false)
const canvas = dvMainStoreWithOut()
const available = ref(false)
let availabilitySequence = 0
const visible = computed(
  () => MATERIAL_LIBRARY_ENTRY_ENABLED && available.value && canvas.editMode === 'edit'
)
watch(
  () => canvas.editMode,
  async mode => {
    const current = ++availabilitySequence
    available.value = false
    if (!MATERIAL_LIBRARY_ENTRY_ENABLED || !extensionEnabled || mode !== 'edit') return
    try {
      const result = await materialCapabilities()
      if (current === availabilitySequence) available.value = result.data.available === true
    } catch {
      if (current === availabilitySequence) available.value = false
    }
  },
  { immediate: true }
)
const context = computed(() => materialContext())
const select = (reference: string) => {
  if (!visible.value || props.disabled || !/^material:\/\/[1-9][0-9]{0,18}$/.test(reference)) return
  emit('select', reference)
  open.value = false
}
watch(visible, value => {
  if (!value) open.value = false
})
</script>

<template>
  <el-button
    v-if="visible"
    text
    :disabled="disabled"
    class="material-picker-trigger"
    :class="[themes, { 'is-toolbar': toolbar }]"
    :aria-label="t(toolbar ? 'material.library' : 'material.choose')"
    @click="open = true"
  >
    <el-icon v-if="toolbar"><material-store-icon /></el-icon>
    <span>{{ t(toolbar ? 'material.library' : 'material.choose') }}</span>
  </el-button>
  <el-dialog
    v-if="open"
    :modal="false"
    draggable
    v-model="open"
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
      :toolbar="toolbar"
      @select="select"
      @cancel="open = false"
    />
  </el-dialog>
</template>

<style lang="less" scoped>
.material-picker-trigger.is-toolbar {
  height: auto;
  padding: 8px;
  min-width: 56px;
  color: #a6a6a6;
  background: transparent;
  :deep(> span) {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    line-height: 12px;
  }
  .ed-icon {
    width: 20px;
    height: 20px;
    font-size: 20px;
    fill: currentColor;
  }
  &:hover {
    color: #fff;
    background: #ebebeb1a;
  }
}
</style>
