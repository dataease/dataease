import { computed, ref, watch } from 'vue'
import { materialCapabilities, materialContext } from '@/api/material'
import { MATERIAL_LIBRARY_ENTRY_ENABLED } from '@/config/material'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import extensionEnabled from 'virtual:dataease-material-enabled'

export const useMaterialLibrary = () => {
  const canvas = dvMainStoreWithOut()
  const capable = ref(false)
  const editing = computed(() => canvas.editMode === 'edit' && !canvas.fullscreenFlag)
  const available = computed(() => capable.value && editing.value)
  watch(
    () => canvas.editMode,
    async (mode, _previousMode, onCleanup) => {
      let active = true
      onCleanup(() => {
        active = false
      })
      capable.value = false
      if (!MATERIAL_LIBRARY_ENTRY_ENABLED || !extensionEnabled || mode !== 'edit') return
      try {
        const result = await materialCapabilities()
        if (active) capable.value = result.data.available === true
      } catch {
        if (active) capable.value = false
      }
    },
    { immediate: true }
  )
  return { editing, available, context: computed(materialContext) }
}

export const isMaterialReference = (reference: string) =>
  /^material:\/\/[1-9][0-9]{0,18}$/.test(reference)
