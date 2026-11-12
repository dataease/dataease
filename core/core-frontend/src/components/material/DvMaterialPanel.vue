<script setup lang="ts">
import { ref, watch } from 'vue'
import dvMaterial from '@/assets/svg/dv-material.svg'
import ComponentButtonLabel from '@/components/visualization/ComponentButtonLabel.vue'
import CommonGroup from '@/custom-component/component-group/CommonGroup.vue'
import { useI18n } from '@/hooks/web/useI18n'
import eventBus from '@/utils/eventBus'
import MaterialExtension from './MaterialExtension.vue'
import { isMaterialReference, useMaterialLibrary } from './useMaterialLibrary'

const { t } = useI18n()
const { editing, available, context } = useMaterialLibrary()
const open = ref(false)
const activeTab = ref('components')
const dragging = ref(false)

const openPanel = () => {
  activeTab.value = 'components'
  dragging.value = false
  open.value = true
}

const startDrag = (event: DragEvent) => {
  if (event.dataTransfer?.types.includes('id')) dragging.value = true
}

const insertPicture = (reference: string) => {
  if (!available.value || !isMaterialReference(reference)) return
  eventBus.emit('handleNew', {
    componentName: 'Picture',
    innerType: 'Picture',
    materialReference: reference
  })
  open.value = false
}

watch(available, value => {
  if (!value) activeTab.value = 'components'
})
watch(editing, value => {
  if (!value) open.value = false
})
</script>

<template>
  <ComponentButtonLabel
    :title="t('visualization.source_material')"
    :icon-name="dvMaterial"
    show-split-line
    @custom-click="openPanel"
  />
  <el-dialog
    v-if="open"
    v-model="open"
    :title="t('visualization.source_material')"
    :modal="false"
    :close-on-click-modal="false"
    :lock-scroll="false"
    draggable
    width="1000px"
    class="dv-material-dialog"
    :class="{ 'is-dragging': dragging }"
    modal-class="dv-material-overlay"
    destroy-on-close
    append-to-body
  >
    <div @dragstart="startDrag" @dragend="dragging = false">
      <el-tabs v-model="activeTab" class="dv-material-tabs">
        <el-tab-pane :label="t('material.component_assets')" name="components">
          <CommonGroup />
          <div class="dv-material-footer">
            <el-button @click="open = false">{{ t('commons.close') }}</el-button>
          </div>
        </el-tab-pane>
        <el-tab-pane v-if="available" :label="t('material.image_library')" name="images" lazy>
          <MaterialExtension
            :context="context"
            themes="dark"
            toolbar
            @select="insertPicture"
            @cancel="open = false"
          />
        </el-tab-pane>
      </el-tabs>
    </div>
  </el-dialog>
</template>

<style lang="less" scoped>
/* Let drag events reach the canvas outside the modeless dialog. */
:global(.dv-material-overlay),
:global(.dv-material-overlay .ed-overlay-dialog) {
  pointer-events: none;
}
:global(.dv-material-dialog) {
  pointer-events: auto;
  max-width: calc(100vw - 32px);
  max-height: 86vh;
  margin: 7vh auto 0;
  overflow: auto;
  --ed-dialog-bg-color: #232323;
  --ed-bg-color: #232323;
  --ed-bg-color-overlay: #292929;
  --ed-text-color-primary: #ebebeb;
  --ed-text-color-regular: #ebebeb;
  --ed-text-color-secondary: #a6a6a6;
  --ed-text-color-placeholder: #a6a6a6;
  --ed-border-color: #434343;
  --ed-border-color-light: #363636;
  --ed-border-color-lighter: #363636;
  --ed-fill-color: #363636;
  --ed-fill-color-light: #292929;
  --ed-fill-color-blank: #292929;
  color-scheme: dark;
}
:global(.dv-material-dialog.is-dragging) {
  pointer-events: none;
  opacity: 0.3;
}
.dv-material-tabs {
  :deep(.ed-tabs__header) {
    margin-bottom: 20px;
  }
  :deep(.ed-tabs__nav-wrap::after) {
    display: none;
  }
}
.dv-material-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 24px;

  .ed-button {
    min-width: 80px;
  }
}
</style>
