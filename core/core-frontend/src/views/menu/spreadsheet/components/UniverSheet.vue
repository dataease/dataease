<script lang="ts" setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import {
  createUniverInstance,
  createDefaultWorkbookData,
  serializeSheetData
} from '../utils/univerConfig'
import type { UniverInstance } from '../utils/univerConfig'
import { ICommandService, type IWorkbookData } from '@univerjs/core'
import { IRenderManagerService } from '@univerjs/engine-render'
import type { SpreadsheetMode } from '../types/mode'

const props = withDefaults(
  defineProps<{
    modelValue?: Partial<IWorkbookData>
    dataKey?: number | string
    locale?: string
    mode?: SpreadsheetMode
  }>(),
  {
    mode: 'edit'
  }
)

const emit = defineEmits<{
  (e: 'update:modelValue', value: Partial<IWorkbookData>): void
  (e: 'change'): void
  (e: 'ready'): void
}>()

const containerRef = ref<HTMLElement | null>(null)
let univerInstance: UniverInstance | null
const isReady = ref(false)
const isInitializing = ref(false)
let rafId: number | null = null
let commandExecutedDisposable: { dispose: () => void } | undefined

// Internal data tracking
let workbookData: Partial<IWorkbookData> = props.modelValue || createDefaultWorkbookData()

/**
 * Initialize Univer instance
 */
const initUniver = async () => {
  // Prevent duplicate initialization
  if (isInitializing.value || univerInstance) {
    return
  }

  if (!containerRef.value) {
    return
  }

  // Wait for container to have proper dimensions
  const container = containerRef.value
  if (container.offsetWidth === 0 || container.offsetHeight === 0) {
    rafId = requestAnimationFrame(() => {
      initUniver()
    })
    return
  }

  isInitializing.value = true

  // Create Univer instance
  try {
    univerInstance = createUniverInstance(
      container,
      props.locale || 'zh-CN',
      workbookData,
      { mode: props.mode }
    )
    const commandService = univerInstance.univer.__getInjector().get(ICommandService)
    // Univer 的原生编辑、插件命令最终都会经过命令服务，由上层统一比较持久化快照。
    commandExecutedDisposable = commandService.onCommandExecuted(() => emit('change'))
    isReady.value = true
    emit('ready')
  } catch (error) {
  } finally {
    isInitializing.value = false
  }
}

/**
 * Get current snapshot data
 */
const getSheetData = (): string => {
  if (!univerInstance?.univer) {
    return serializeSheetData(createDefaultWorkbookData())
  }

  // Get snapshot from univer instance
  try {
    const univerApi = univerInstance.univerApi
    const workbook = univerApi.getActiveWorkbook()
    if (!workbook) return serializeSheetData(createDefaultWorkbookData())
    const snapshot = workbook.save()
    return serializeSheetData(snapshot)
  } catch (e) {
  }

  return serializeSheetData(createDefaultWorkbookData())
}

/**
 * Load snapshot data - recreate instance with new data
 */
const loadSheetData = (data: Partial<IWorkbookData>) => {
  if (!data) return

  workbookData = data

  // Dispose current instance
  if (univerInstance) {
    disposeInstance()
  }

  nextTick(() => {
    initUniver()
  })
}

const resize = () => {
  const workbook = univerInstance?.univerApi.getActiveWorkbook()
  if (!workbook || !univerInstance?.univer) {
    return
  }

  const renderManager = univerInstance.univer.__getInjector().get(IRenderManagerService)
  renderManager.getRenderById(workbook.getId())?.engine.resize()
}

/**
 * Dispose instance
 */
const disposeInstance = () => {
  commandExecutedDisposable?.dispose()
  commandExecutedDisposable = undefined
  if (rafId !== null) {
    cancelAnimationFrame(rafId)
    rafId = null
  }
  if (univerInstance) {
    try {
      univerInstance.dispose()
    } catch (e) {
    }
    univerInstance = null
  }
  isReady.value = false
  isInitializing.value = false
  if (containerRef.value) {
    containerRef.value.innerHTML = ''
  }
}

// Watch for external changes to modelValue
watch(
  () => props.modelValue,
  newVal => {
    workbookData = newVal || createDefaultWorkbookData()
    if (!univerInstance && !isInitializing.value && containerRef.value) {
      nextTick(() => {
        initUniver()
      })
    }
  }
)

watch(
  () => props.mode,
  () => {
    if (!univerInstance) {
      return
    }
    disposeInstance()
    nextTick(() => initUniver())
  }
)

const spreadsheetColorSchemeClass = 'spreadsheet-light-scheme'
onMounted(() => {
  document.documentElement.classList.add(spreadsheetColorSchemeClass)
  nextTick(() => {
    initUniver()
  })
})

onBeforeUnmount(() => {
  document.documentElement.classList.remove(spreadsheetColorSchemeClass)
  disposeInstance()
})

// Expose methods
defineExpose({
  getSheetData,
  loadSheetData,
  resize,
  getUniverApi: () => univerInstance?.univerApi,
  getUniverInstance: () => univerInstance
})
</script>

<template>
  <div
    class="univer-sheet-container"
    :class="{ 'dataease-univer-preview': props.mode === 'preview' }"
  >
    <div ref="containerRef" class="univer-container"></div>
  </div>
</template>

<style lang="less" scoped>
.univer-sheet-container {
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;

  .univer-container {
    width: 100%;
    height: 100%;
    position: relative;
  }
}
</style>

<style lang="less">
// Global styles for Univer integration (not scoped)
.univer-sheet-container {
  // Ensure Univer app fills container
  .univer-app {
    height: 100% !important;
    width: 100% !important;
  }

  // Ensure the sheets container fills
  .univer-sheets {
    height: 100% !important;
    width: 100% !important;
  }

  // Sheet container
  .univer-sheet-container {
    height: 100% !important;
    width: 100% !important;
  }

  // Canvas container - ensure it's interactive
  .univer-canvas-container {
    position: relative !important;
  }

  // Toolbar styles
  .univer-toolbar {
    border-bottom: 1px solid #e5e5e5;
  }

  // Ensure editor overlay works
  .univer-editor-container {
    z-index: 100;
  }
}
html.spreadsheet-light-scheme {
  color-scheme: light !important;
}
</style>
