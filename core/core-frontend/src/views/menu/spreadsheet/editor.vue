<script lang="ts" setup>
import { computed, ref, shallowRef, provide, type Component } from 'vue'
import router from '@/router'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  create,
  update,
  updateStatus,
  findEditById,
  recoverToPublished,
  SpreadsheetPublishStatus
} from './api/index'
import type { DatasetBindDTO, SpreadsheetEditor, SpreadsheetVO } from './api/index'
import UniverSheet from './components/UniverSheet.vue'
import SpreadsheetToolbar from './components/SpreadsheetToolbar.vue'
import SpreadsheetResourceGroupOpt from './components/SpreadsheetResourceGroupOpt.vue'
import PluginActionToolbar from './components/PluginActionToolbar.vue'
import PluginRenderIndicator from './components/PluginRenderIndicator.vue'
import DatasetReplacementDialog from './components/dataset-replacement/DatasetReplacementDialog.vue'
import SpreadsheetPreviewOverlay from './components/SpreadsheetPreviewOverlay.vue'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { nextTick, onBeforeMount } from 'vue'
import type { IWorkbookData } from '@univerjs/core'
import { ISidebarService } from '@univerjs/ui'
import { createDefaultWorkbookData, parseSheetData, serializeSheetData } from './utils/univerConfig'
import { SPREADSHEET_EVENTS } from './utils/events'
import { useEmitt } from '@/hooks/web/useEmitt'
import type { PluginEditPayload } from './types/editor'
import type { PluginConfig } from './types/plugin'
import type { DatasetReplacementScope } from './plugins/DataEaseDatasetReplacementPlugin/types'
import { clearUniverProtectionResources } from './plugins/DataEaseToolbarUIPlugin/config/protection-config'
import { pluginSnapshotCleaningService } from './services/plugin-snapshot-cleaning.service'
import { pluginRuntimeRegistry } from './services/plugin-runtime.service'
import { pluginRenderStatusService } from './services/plugin-render-status.service'
import { PluginAdapterManager } from './types/adapter'

const { t } = useI18n()
const localeStore = useLocaleStoreWithOut()

const showEditor = ref(false)
const currentPluginConfig = ref<PluginConfig | null>(null)

const routeMode = computed(() => router.currentRoute.value.query.opt as string | undefined)
const routeSheetId = computed(() => router.currentRoute.value.query.id as string | undefined)
const routePid = computed(() => router.currentRoute.value.query.pid as string | undefined)
const editorRootRef = ref<HTMLElement>()
const univerSheetRef = ref<InstanceType<typeof UniverSheet>>()
const univerApiRef = shallowRef<any>()
const univerInstanceRef = shallowRef<any>()
const loading = ref(true)
const workbookData = shallowRef<Partial<IWorkbookData>>()
const hasChanges = ref(false)
const spreadsheetStatus = ref(SpreadsheetPublishStatus.Unpublished)
const spreadsheetRemark = ref('')
const spreadsheetVersion = ref(1)
const spreadsheetName = ref(t('spreadsheet.new_spreadsheet'))
const saving = ref(false)
const publishing = ref(false)
const recovering = ref(false)
const pendingPublish = ref(false)
const previewVisible = ref(false)
const previewWorkbookData = shallowRef<Partial<IWorkbookData>>()
const closePreviewOnFullscreenExit = ref(false)
const datasetReplacementVisible = ref(false)
const datasetReplacementScope = ref<DatasetReplacementScope>('workbook')
const datasetReplacementComponentId = ref<string>()

const resourceGroupOpt = ref()
const resourceGroupOptShow = ref(false)
const editorComponent = shallowRef<Component | null>(null)

// 默认工作表名称跟随 DataEase 当前语言，已有工作簿仍使用快照中保存的名称。
const createLocalizedDefaultWorkbookData = () =>
  createDefaultWorkbookData(t('spreadsheet.default_sheet_name'))

provide('pluginConfig', currentPluginConfig)

const closeNativeSidebar = () => {
  const injector = univerInstanceRef.value?.univer?.__getInjector?.()
  const sidebarService = injector?.get(ISidebarService)
  if (sidebarService?.visible) {
    sidebarService.close()
  }
}

useEmitt({
  name: SPREADSHEET_EVENTS.OPEN_COMPONENT_DATASET_REPLACEMENT,
  callback: (payload: { componentId?: string }) => {
    const componentId = payload?.componentId || currentPluginConfig.value?.id
    if (!componentId) {
      return
    }
    datasetReplacementScope.value = 'component'
    datasetReplacementComponentId.value = String(componentId)
    datasetReplacementVisible.value = true
  }
})

useEmitt({
  name: SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR,
  callback: (payload: PluginEditPayload) => {
    if (previewVisible.value || !payload?.config) {
      return
    }
    currentPluginConfig.value = payload.config
    const adapter = PluginAdapterManager.getAdapter(payload.config.type)
    if (!adapter) {
      return
    }
    // 自动选区入口会在原生侧边栏打开时提前返回；进入此处的主动请求先关闭原生侧边栏。
    closeNativeSidebar()
    editorComponent.value = adapter.getEditor()
    showEditor.value = true
  }
})

const closePromptVisible = ref(false)

const removeDraftInstance = async (config: PluginConfig): Promise<void> => {
  const runtime = pluginRuntimeRegistry.get(config.type)
  const api = univerApiRef.value
  if (runtime?.removeDraft && api) {
    await runtime.removeDraft({ univerApi: api, config })
  }
}

const handleClosePluginEditor = async (): Promise<void> => {
  const config = currentPluginConfig.value
  if (!config || !showEditor.value) {
    currentPluginConfig.value = null
    showEditor.value = false
    return
  }

  // 已成功渲染的实例直接关闭；draft / error 实例关闭前二次确认并清除实例。
  if (!pluginRenderStatusService.needsCloseConfirm(config.id)) {
    currentPluginConfig.value = null
    showEditor.value = false
    return
  }

  if (closePromptVisible.value) {
    return
  }
  closePromptVisible.value = true
  try {
    await ElMessageBox.confirm(
      t('spreadsheet.draft_close_message'),
      t('spreadsheet.draft_close_title'),
      {
        confirmButtonText: t('spreadsheet.draft_close_confirm'),
        cancelButtonText: t('spreadsheet.draft_close_cancel'),
        type: 'warning',
        autofocus: false
      }
    )
    await removeDraftInstance(config)
    currentPluginConfig.value = null
    showEditor.value = false
  } catch {
    // 用户取消，保持面板打开。
  } finally {
    closePromptVisible.value = false
  }
}

useEmitt({
  name: SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR,
  callback: () => {
    void handleClosePluginEditor()
  }
})

useEmitt({
  name: SPREADSHEET_EVENTS.DATASET_REPLACEMENT_COMPLETED,
  callback: (payload: { configs?: PluginConfig[] }) => {
    const currentId = currentPluginConfig.value?.id
    const nextConfig = payload?.configs?.find(config => config.id === currentId)
    if (nextConfig) {
      currentPluginConfig.value = nextConfig
    }
    hasChanges.value = true
  }
})

type SaveDestination = { name: string; pid: string | number }

const persistSpreadsheet = async (
  destination?: SaveDestination,
  silentSuccess = false
): Promise<SpreadsheetVO | undefined> => {
  if (routeMode.value !== 'edit' && !routeSheetId.value && !destination) {
    resourceGroupOptShow.value = true
    nextTick(() => {
      resourceGroupOpt.value.optInit(spreadsheetName.value, routePid.value)
    })
    return undefined
  }

  saving.value = true
  try {
    const rawSheetDataStr =
      univerSheetRef.value?.getSheetData() ||
      JSON.stringify(workbookData.value || createLocalizedDefaultWorkbookData())
    const cleanedSheetData = await pluginSnapshotCleaningService.clean(
      JSON.parse(rawSheetDataStr)
    )
    const currentSheetDataStr = JSON.stringify(cleanedSheetData)

    const editor: SpreadsheetEditor = {
      name: destination ? destination.name : spreadsheetName.value,
      sheetData: currentSheetDataStr,
      remark: spreadsheetRemark.value,
      version: spreadsheetVersion.value,
      id: routeSheetId.value ? routeSheetId.value : undefined,
      pid: destination ? destination.pid : (routePid.value || 0),
      nodeType: 'sheet'
    }

    const result = routeSheetId.value ? await update(editor) : await create(editor)

    if (!silentSuccess) {
      ElMessage.success(t('spreadsheet.save_success'))
    }

    if (routeMode.value !== 'edit') {
      await router.replace({
        query: { id: String(result.id), opt: 'edit', pid: String(result.pid) }
      })
    }
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Unpublished
    spreadsheetRemark.value = result.remark ?? spreadsheetRemark.value
    spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
    hasChanges.value = false
    if (destination) {
      resourceGroupOptShow.value = false
      spreadsheetName.value = destination.name
    }
    return result
  } catch (e) {
    console.error(e)
    ElMessage.error(t('spreadsheet.save_error'))
    return undefined
  } finally {
    saving.value = false
  }
}

const handleSave = () => persistSpreadsheet()

const publishSavedSpreadsheet = async (id: string | number) => {
  const result = await updateStatus({
    id,
    status: SpreadsheetPublishStatus.Published
  })
  spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Published
  spreadsheetRemark.value = result.remark ?? spreadsheetRemark.value
  spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
  ElMessage.success(t('spreadsheet.publish_success'))
}

const handlePublish = async () => {
  if (!routeSheetId.value) {
    pendingPublish.value = true
    await persistSpreadsheet(undefined, true)
    return
  }

  publishing.value = true
  try {
    const saved = await persistSpreadsheet(undefined, true)
    if (!saved) {
      return
    }
    await publishSavedSpreadsheet(saved.id)
  } catch (error) {
    console.error(error)
    ElMessage.error(t('spreadsheet.publish_error'))
  } finally {
    publishing.value = false
  }
}

const resourceOptFinish = async (destination: SaveDestination) => {
  const shouldPublish = pendingPublish.value
  const result = await persistSpreadsheet(destination, shouldPublish)
  if (!result || !shouldPublish) {
    pendingPublish.value = false
    return
  }

  publishing.value = true
  try {
    await publishSavedSpreadsheet(result.id)
  } catch (error) {
    console.error(error)
    ElMessage.error(t('spreadsheet.publish_error'))
  } finally {
    publishing.value = false
    pendingPublish.value = false
  }
}

const handleRecoverPublished = async () => {
  if (
    spreadsheetStatus.value !== SpreadsheetPublishStatus.SavedUnpublished ||
    !routeSheetId.value
  ) {
    return
  }

  try {
    await ElMessageBox.confirm(t('spreadsheet.recover_publish_confirm'), {
      confirmButtonText: t('commons.confirm'),
      cancelButtonText: t('commons.cancel'),
      type: 'warning',
      autofocus: false
    })
  } catch {
    return
  }

  recovering.value = true
  try {
    const result = await recoverToPublished(routeSheetId.value)
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Published
    spreadsheetRemark.value = result.remark ?? ''
    spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
    loading.value = true
    await nextTick()
    workbookData.value = clearUniverProtectionResources(
      parseSheetData(result.sheetData) ?? createLocalizedDefaultWorkbookData()
    )
    hasChanges.value = false
    loading.value = false
    ElMessage.success(t('spreadsheet.recover_publish_success'))
  } catch (error) {
    console.error(error)
    loading.value = false
    ElMessage.error(t('spreadsheet.recover_publish_error'))
  } finally {
    recovering.value = false
  }
}

const handleCancelPublish = async () => {
  if (
    spreadsheetStatus.value === SpreadsheetPublishStatus.Unpublished ||
    !routeSheetId.value
  ) {
    return
  }

  publishing.value = true
  try {
    const result = await updateStatus({
      id: routeSheetId.value,
      status: SpreadsheetPublishStatus.Unpublished
    })
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Unpublished
    ElMessage.success(t('spreadsheet.cancel_publish_success'))
  } catch (error) {
    console.error(error)
    ElMessage.error(t('spreadsheet.cancel_publish_error'))
  } finally {
    publishing.value = false
  }
}

const handleBack = async () => {
  if (hasChanges.value) {
    try {
      await ElMessageBox.confirm(
        t('spreadsheet.save_before_leave'),
        t('spreadsheet.unsaved_changes'),
        {
          confirmButtonText: t('spreadsheet.save'),
          cancelButtonText: t('spreadsheet.discard'),
          distinguishCancelAndClose: true
        }
      )
      await handleSave()
    } catch (e) {
      // User chose to discard or closed dialog
    }
  }
  router.push('/spreadsheet/index')
}

const handleDatasetBinding = async (binding: DatasetBindDTO) => {
  //
}

const handleDatasetUnbind = async (datasetId: number) => {
  //
}

const handleRename = (name: string) => {
  if (spreadsheetName.value === name) return
  spreadsheetName.value = name
}

const handleExport = () => {
  ElMessage.info(t('spreadsheet.coming_soon'))
}

const handleOpenWorkbookDatasetReplacement = () => {
  datasetReplacementScope.value = 'workbook'
  datasetReplacementComponentId.value = undefined
  datasetReplacementVisible.value = true
}

const handlePreview = async (fullscreen = false) => {
  let enteredFullscreen = false
  closePreviewOnFullscreenExit.value = false

  if (fullscreen && editorRootRef.value && document.fullscreenElement !== editorRootRef.value) {
    try {
      await editorRootRef.value.requestFullscreen()
      enteredFullscreen = document.fullscreenElement === editorRootRef.value
    } catch (error) {
      console.error('[Spreadsheet] Failed to enter fullscreen preview:', error)
      ElMessage.warning('无法进入全屏预览')
    }
  }

  try {
    const rawSnapshot = univerSheetRef.value?.getSheetData()
    const snapshot = parseSheetData(rawSnapshot) ?? createLocalizedDefaultWorkbookData()
    const cleanedSnapshot = await pluginSnapshotCleaningService.clean(snapshot)
    const cleanedSnapshotData = serializeSheetData(cleanedSnapshot)
    workbookData.value = parseSheetData(cleanedSnapshotData) ?? createLocalizedDefaultWorkbookData()
    previewWorkbookData.value =
      parseSheetData(cleanedSnapshotData) ?? createLocalizedDefaultWorkbookData()
    currentPluginConfig.value = null
    showEditor.value = false
    // 从编辑态直接进入全屏时，退出全屏应回到编辑态；普通预览内的全屏仍回到普通预览。
    closePreviewOnFullscreenExit.value = enteredFullscreen
    previewVisible.value = true
  } catch (error) {
    if (enteredFullscreen && document.fullscreenElement === editorRootRef.value) {
      await document.exitFullscreen()
    }
    throw error
  }
}

const handleFullscreenPreview = () => handlePreview(true)

const handleClosePreview = () => {
  closePreviewOnFullscreenExit.value = false
  previewVisible.value = false
}

const handleUniverReady = () => {
  univerApiRef.value = univerSheetRef.value?.getUniverApi()
  univerInstanceRef.value = univerSheetRef.value?.getUniverInstance()
}

// 提供 Univer API 给子组件
provide('univerApi', univerApiRef)

onBeforeMount(async () => {
  if (routeMode.value === 'edit' && routeSheetId.value) {
    try {
      const spreadsheetInfo = await findEditById(routeSheetId.value)
      if (spreadsheetInfo) {
        const loadedWorkbookData =
          parseSheetData(spreadsheetInfo.sheetData) ?? createLocalizedDefaultWorkbookData()
        // 在 createUnit 加载资源前删除旧版 Univer 保护数据，
        // 避免历史工作表或区域保护规则在打开时直接锁定工作簿。
        const cleanedWorkbookData = clearUniverProtectionResources(loadedWorkbookData)
        spreadsheetName.value = spreadsheetInfo.name
        spreadsheetStatus.value =
          spreadsheetInfo.status ?? SpreadsheetPublishStatus.Unpublished
        spreadsheetRemark.value = spreadsheetInfo.remark ?? ''
        spreadsheetVersion.value = spreadsheetInfo.version ?? 1
        workbookData.value = cleanedWorkbookData
        loading.value = false
      }
    } catch (e) {
      console.error(e)
      ElMessage.error(t('spreadsheet.load_error'))
      loading.value = true
    }
  } else {
    workbookData.value = createLocalizedDefaultWorkbookData()
    loading.value = false
  }
})

</script>

<template>
  <div ref="editorRootRef" class="spreadsheet-editor" v-loading="loading">
    <SpreadsheetToolbar
      :name="spreadsheetName"
      :has-changes="hasChanges"
      :saving="saving"
      :publishing="publishing"
      :recovering="recovering"
      :status="spreadsheetStatus"
      @back="handleBack"
      @save="handleSave"
      @export="handleExport"
      @preview="handlePreview"
      @fullscreen-preview="handleFullscreenPreview"
      @publish="handlePublish"
      @recover-published="handleRecoverPublished"
      @cancel-publish="handleCancelPublish"
      @replace-dataset="handleOpenWorkbookDatasetReplacement"
      @rename="handleRename"
    />

    <div class="editor-main">
      <div class="editor-canvas">
        <UniverSheet
          v-if="!loading && !previewVisible"
          ref="univerSheetRef"
          :model-value="workbookData || undefined"
          mode="edit"
          @ready="handleUniverReady"
        />

        <PluginActionToolbar v-if="!previewVisible" />
        <PluginRenderIndicator v-if="!previewVisible" />

        <!-- 右侧配置面板 - 悬浮在画布上 -->
        <component
          :is="editorComponent"
          v-if="showEditor && !previewVisible"
          class="editor-overlay"
        />
      </div>

    </div>
    <SpreadsheetResourceGroupOpt
      v-if="resourceGroupOptShow"
      ref="resourceGroupOpt"
      @finish="resourceOptFinish"
      @cancel="pendingPublish = false"
    />
    <SpreadsheetPreviewOverlay
      v-if="previewVisible && previewWorkbookData"
      :model-value="previewWorkbookData"
      :locale="localeStore.getCurrentLocale.lang"
      :close-on-fullscreen-exit="closePreviewOnFullscreenExit"
      @close="handleClosePreview"
    />
    <DatasetReplacementDialog
      v-model="datasetReplacementVisible"
      :scope="datasetReplacementScope"
      :component-id="datasetReplacementComponentId"
      :univer-instance="univerInstanceRef"
      :univer-api="univerApiRef"
    />
  </div>
</template>

<style lang="less">
.spreadsheet-editor {
  height: 100vh;
  width: 100vw;
  display: flex;
  flex-direction: column;
  background: var(--de-bg-color, #f5f7fa);
  overflow: hidden;

  .editor-main {
    flex: 1;
    display: flex;
    overflow: hidden;
    height: calc(100vh - 56px);
    min-height: 0;

    .editor-canvas {
      flex: 1;
      overflow: hidden;
      background: #fff;
      position: relative;
      min-height: 0;

      .editor-overlay {
        position: absolute;
        right: 0;
        top: 0;
        bottom: 0;
        width: auto;
        background: #fff;
        border-left: 1px solid var(--de-border-color, #e5e5e5);
        z-index: 200;
        box-shadow: -2px 0 8px rgba(0, 0, 0, 0.1);
      }
    }

    .editor-sidebar {
      width: 320px;
      background: #fff;
      border-left: 1px solid var(--de-border-color, #e5e5e5);
      overflow: auto;
      flex-shrink: 0;

      :deep(.el-tabs) {
        height: 100%;
        display: flex;
        flex-direction: column;

        .el-tabs__header {
          margin: 0;
          padding: 0 16px;
          flex-shrink: 0;
        }

        .el-tabs__content {
          flex: 1;
          overflow: auto;
        }

        .el-tab-pane {
          padding: 16px;
        }
      }
    }
  }
}

.slide-enter-active,
.slide-leave-active {
  transition: all 0.3s ease;
}

.slide-enter-from,
.slide-leave-to {
  transform: translateX(100%);
  opacity: 0;
}
</style>
