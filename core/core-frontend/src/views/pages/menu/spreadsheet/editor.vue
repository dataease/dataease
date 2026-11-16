<script lang="ts" setup>
import { presentError } from '@/config/axios/errorPresenter'

import {
  computed,
  reactive,
  ref,
  shallowRef,
  provide,
  nextTick,
  watch,
  onBeforeMount,
  onMounted,
  onBeforeUnmount,
  type Component
} from 'vue'
import router from '@/router'
import { onBeforeRouteLeave, onBeforeRouteUpdate } from 'vue-router_2'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  create,
  update,
  updateStatus,
  findEditById,
  recoverToPublished,
  SpreadsheetPublishStatus
} from '@/views/menu/spreadsheet/api/index'
import type {
  DatasetBindDTO,
  SpreadsheetEditor,
  SpreadsheetVO
} from '@/views/menu/spreadsheet/api/index'
import UniverSheet from '@/views/menu/spreadsheet/components/UniverSheet.vue'
import SpreadsheetToolbar from '@/views/menu/spreadsheet/components/SpreadsheetToolbar.vue'
import SpreadsheetResourceGroupOpt from '@/views/menu/spreadsheet/components/SpreadsheetResourceGroupOpt.vue'
import PluginActionToolbar from '@/views/menu/spreadsheet/components/PluginActionToolbar.vue'
import PluginRenderIndicator from '@/views/menu/spreadsheet/components/PluginRenderIndicator.vue'
import DatasetReplacementDialog from '@/views/menu/spreadsheet/components/dataset-replacement/DatasetReplacementDialog.vue'
import SpreadsheetPreviewOverlay from '@/views/menu/spreadsheet/components/SpreadsheetPreviewOverlay.vue'
import SpreadsheetOuterParamsDialog from '@/views/menu/spreadsheet/components/SpreadsheetOuterParamsDialog.vue'
import { useLocaleStoreWithOut } from '@/store/modules/locale'
import { useAppStoreWithOut } from '@/store/modules/app'
import { useEmbedded } from '@/store/modules/embedded'
import { useResizeObserver } from '@vueuse/core'
import { onInitReady } from '@/utils/canvasUtils'
import type { IWorkbookData } from '@univerjs/core'
import { ISidebarService } from '@univerjs/ui'
import {
  createDefaultWorkbookData,
  parseSheetData,
  serializeSheetData
} from '@/views/menu/spreadsheet/utils/univerConfig'
import { loadSpreadsheetOuterParamsEditorContext } from '@/views/menu/spreadsheet/utils/outer-params-editor'
import { SPREADSHEET_EVENTS } from '@/views/menu/spreadsheet/utils/events'
import { useEmitt } from '@/hooks/web/useEmitt'
import type { PluginEditPayload } from '@/views/menu/spreadsheet/types/editor'
import type { PluginConfig } from '@/views/menu/spreadsheet/types/plugin'
import type {
  SpreadsheetOuterParamsConfig,
  SpreadsheetOuterParamsContext
} from '@/views/menu/spreadsheet/types/outer-params'
import type { DatasetReplacementScope } from '@/views/menu/spreadsheet/plugins/DataEaseDatasetReplacementPlugin/types'
import { clearUniverProtectionResources } from '@/views/menu/spreadsheet/plugins/DataEaseToolbarUIPlugin/config/protection-config'
import { pluginSnapshotCleaningService } from '@/views/menu/spreadsheet/services/plugin-snapshot-cleaning.service'
import { pluginRuntimeRegistry } from '@/views/menu/spreadsheet/services/plugin-runtime.service'
import { PluginRenderStatusService } from '@/views/menu/spreadsheet/plugins/DataEaseRuntimePlugin/services/table'
import { PluginAdapterManager } from '@/views/menu/spreadsheet/types/adapter'
import {
  deleteSpreadsheetDraftCache,
  getSpreadsheetDraftCache,
  setSpreadsheetDraftCache,
  type SpreadsheetDraftCache
} from '@/views/menu/spreadsheet/utils/draft-cache'

const { t } = useI18n()
const localeStore = useLocaleStoreWithOut()
const appStore = useAppStoreWithOut()
const embeddedStore = useEmbedded()
const { emitter } = useEmitt()
const isEmbedded = appStore.getIsDataEaseBi || appStore.getIsIframe

// 会话固定工作簿的归属；嵌入容器清理共享状态时，仍保留当前资源上下文。
const session = reactive({
  id: router.currentRoute.value.query.id as string | undefined,
  mode: router.currentRoute.value.query.opt as string | undefined,
  pid: router.currentRoute.value.query.pid as string | undefined
})
if (isEmbedded) {
  session.id = embeddedStore.opt === 'create' ? '' : embeddedStore.resourceId
  session.mode = session.id ? 'edit' : 'create'
  session.pid = embeddedStore.pid
}

const showEditor = ref(false)
const currentPluginConfig = ref<PluginConfig | null>(null)

const routeMode = computed(() => {
  if (isEmbedded) return session.mode
  return router.currentRoute.value.query.opt as string | undefined
})
const routeSheetId = computed(() => {
  if (isEmbedded) return session.id
  return router.currentRoute.value.query.id as string | undefined
})
const routePid = computed(() => {
  if (isEmbedded) return session.pid
  return router.currentRoute.value.query.pid as string | undefined
})
const editorRoutePath = router.currentRoute.value.path
const sessionMatchesRoute = computed(
  () =>
    isEmbedded ||
    (router.currentRoute.value.path === editorRoutePath &&
      session.id === routeSheetId.value &&
      session.mode === routeMode.value)
)
let sessionRevision = 0
let loadController: AbortController | undefined
const disposed = ref(false)
const leaving = ref(false)
const editorRootRef = ref<HTMLElement>()
const univerSheetRef = ref<InstanceType<typeof UniverSheet>>()
useResizeObserver(editorRootRef, () => {
  if (isEmbedded) {
    univerSheetRef.value?.resize()
  }
})
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
const mutating = computed(() => saving.value || publishing.value || recovering.value)
const sessionAvailable = computed(
  () => !disposed.value && !loading.value && !leaving.value && sessionMatchesRoute.value
)
const isCurrentSession = (revision: number) =>
  !disposed.value && revision === sessionRevision && sessionMatchesRoute.value
const pendingPublish = ref(false)
const openingOuterParams = ref(false)
const outerParamsVisible = ref(false)
const outerParamsContext = shallowRef<SpreadsheetOuterParamsContext>({
  conditions: [],
  dataAreas: []
})
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

let baselineSheetData = ''
let baselineSpreadsheetName = ''
let draftCacheTimer: ReturnType<typeof setTimeout> | undefined
let draftCheckRevision = 0
let draftCheckPending = false
let discardAndLeave = false

const stopDraftCheck = () => {
  if (draftCacheTimer) {
    clearTimeout(draftCacheTimer)
    draftCacheTimer = undefined
  }
  // 已开始的异步比较也必须失效，避免重建工作簿后重新写入旧草稿。
  draftCheckRevision++
  draftCheckPending = false
}

const getCurrentRawSheetData = () =>
  univerSheetRef.value?.getSheetData() ||
  serializeSheetData(workbookData.value || createLocalizedDefaultWorkbookData())

const cleanSheetData = async (sheetData: string): Promise<string> => {
  const snapshot = parseSheetData(sheetData) ?? createLocalizedDefaultWorkbookData()
  const cleanedSnapshot = await pluginSnapshotCleaningService.clean(snapshot)
  return serializeSheetData(cleanedSnapshot)
}

const updateSavedBaseline = (sheetData: string, name: string) => {
  baselineSheetData = sheetData
  baselineSpreadsheetName = name
}

const buildDraftCache = (sheetData: string): SpreadsheetDraftCache | undefined => {
  if (session.mode !== 'edit' || !session.id) {
    return undefined
  }
  return {
    sheetId: session.id,
    sourceVersion: spreadsheetVersion.value,
    name: spreadsheetName.value,
    remark: spreadsheetRemark.value,
    sheetData,
    cachedAt: Date.now()
  }
}

const cacheCurrentDraft = (sheetData: string) => {
  const cache = buildDraftCache(sheetData)
  if (!cache) {
    return
  }
  try {
    setSpreadsheetDraftCache(cache)
  } catch (error) {
    // 本地存储空间不足不能影响正常编辑和保存。
    console.warn('[Spreadsheet] Failed to cache spreadsheet draft:', error)
  }
}

const syncDraftCache = async () => {
  if (session.mode !== 'edit' || !session.id || !baselineSheetData) {
    return
  }

  const currentRevision = draftCheckRevision
  try {
    const currentSheetData = await cleanSheetData(getCurrentRawSheetData())
    if (currentRevision !== draftCheckRevision) {
      return
    }

    draftCheckPending = false
    const contentChanged = currentSheetData !== baselineSheetData
    const nameChanged = spreadsheetName.value !== baselineSpreadsheetName
    hasChanges.value = contentChanged || nameChanged
    if (hasChanges.value) {
      cacheCurrentDraft(currentSheetData)
    } else {
      deleteSpreadsheetDraftCache(session.id)
    }
  } catch (error) {
    if (currentRevision !== draftCheckRevision) {
      return
    }
    draftCheckPending = false
    hasChanges.value = true
    cacheCurrentDraft(getCurrentRawSheetData())
    console.warn('[Spreadsheet] Failed to compare spreadsheet draft:', error)
  }
}

const markSpreadsheetChanged = () => {
  if (loading.value) {
    return
  }
  if (session.mode !== 'edit' || !session.id) {
    hasChanges.value = true
    return
  }

  draftCheckRevision++
  draftCheckPending = true
  if (draftCacheTimer) {
    clearTimeout(draftCacheTimer)
  }
  // 合并连续输入和样式操作，避免每条 Univer 命令都序列化整个工作簿。
  draftCacheTimer = setTimeout(() => {
    draftCacheTimer = undefined
    void syncDraftCache()
  }, 1000)
}

const flushDraftBeforeLeaving = async () => {
  if (draftCacheTimer) {
    clearTimeout(draftCacheTimer)
    draftCacheTimer = undefined
  }
  while (draftCheckPending) {
    await syncDraftCache()
  }
}

const cacheRawDraftBeforeUnload = () => {
  if (!hasChanges.value && !draftCheckPending) {
    return
  }
  if (!draftCheckPending && session.id && getSpreadsheetDraftCache(session.id)) {
    return
  }
  // 页面卸载阶段不能等待异步清理；恢复时会再次经过快照清理器。
  cacheCurrentDraft(getCurrentRawSheetData())
}

const handleBeforeCreateDataset = async (): Promise<boolean> => {
  if (!sessionAvailable.value || session.mode !== 'edit' || !session.id) {
    ElMessage.warning(t('spreadsheet.save_current_before_create_dataset'))
    return false
  }
  const revision = sessionRevision
  await flushDraftBeforeLeaving()
  return isCurrentSession(revision) && sessionAvailable.value
}

provide('pluginConfig', currentPluginConfig)
provide('beforeCreateSpreadsheetDataset', handleBeforeCreateDataset)

const closeNativeSidebar = () => {
  const injector = univerInstanceRef.value?.univer?.__getInjector?.()
  const sidebarService = injector?.get(ISidebarService)
  if (sidebarService?.visible) {
    sidebarService.close()
  }
}

const getPluginRenderStatusService = (): PluginRenderStatusService | undefined => {
  const injector = univerInstanceRef.value?.univer?.__getInjector?.()
  return injector?.get(PluginRenderStatusService)
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
  name: SPREADSHEET_EVENTS.CONTENT_CHANGED,
  callback: markSpreadsheetChanged
})

let pendingPluginEditorPayload: PluginEditPayload | null | undefined
let pluginEditorTransitionScheduled = false
let pluginEditorTransitionRunning = false

const removeDraftInstance = async (config: PluginConfig): Promise<void> => {
  const runtime = pluginRuntimeRegistry.get(config.type)
  const api = univerApiRef.value
  if (runtime?.removeDraft && api) {
    await runtime.removeDraft({ univerApi: api, config })
  }
}

const applyPluginEditorPayload = (payload: PluginEditPayload | null): void => {
  if (!payload) {
    currentPluginConfig.value = null
    showEditor.value = false
    return
  }

  if (previewVisible.value || !payload.config) {
    return
  }

  const adapter = PluginAdapterManager.getAdapter(payload.config.type)
  if (!adapter) {
    return
  }

  // 自动选区入口会在原生侧边栏打开时提前返回；进入此处的主动请求先关闭原生侧边栏。
  closeNativeSidebar()
  currentPluginConfig.value = payload.config
  editorComponent.value = adapter.getEditor()
  showEditor.value = true
}

const resetWorkbookEditor = () => {
  loading.value = true
  outerParamsVisible.value = false
  openingOuterParams.value = false
  stopDraftCheck()
  pendingPluginEditorPayload = undefined
  applyPluginEditorPayload(null)
  editorComponent.value = null
  datasetReplacementVisible.value = false
  datasetReplacementComponentId.value = undefined
  univerApiRef.value = undefined
  univerInstanceRef.value = undefined
}

const handlePluginEditorTransition = async (
  requestedPayload: PluginEditPayload | null
): Promise<void> => {
  const config = currentPluginConfig.value
  if (requestedPayload?.config.id === config?.id) {
    applyPluginEditorPayload(requestedPayload)
    return
  }

  if (!config || !showEditor.value) {
    applyPluginEditorPayload(requestedPayload)
    return
  }

  // 已成功渲染的实例直接切换；draft 实例切换前二次确认并清除实例。
  if (!getPluginRenderStatusService()?.needsCloseConfirm(config.id)) {
    applyPluginEditorPayload(requestedPayload)
    return
  }

  const univerApi = univerApiRef.value
  try {
    await ElMessageBox.confirm(t('commons.prompt'), {
      tip: t('spreadsheet.draft_close_message'),
      confirmButtonText: t('spreadsheet.draft_close_confirm'),
      cancelButtonText: t('spreadsheet.draft_close_cancel'),
      type: 'warning',
      autofocus: false
    })
    // 恢复已发布版本会重建实例，旧确认回调不能再删除插件或打开配置面板。
    if (univerApi !== univerApiRef.value) {
      return
    }
    await removeDraftInstance(config)
    if (univerApi !== univerApiRef.value) {
      return
    }

    // 确认期间可能收到多个选区事件，最终打开最新命中的实例。
    let targetPayload = requestedPayload
    if (pendingPluginEditorPayload !== undefined) {
      targetPayload = pendingPluginEditorPayload
    }
    pendingPluginEditorPayload = undefined
    applyPluginEditorPayload(targetPayload)
  } catch {
    if (univerApi !== univerApiRef.value) {
      return
    }
    // 用户取消时必须保留 draft 面板，避免实例失去配置入口后持续禁用插入。
    pendingPluginEditorPayload = undefined
  }
}

const processPluginEditorTransition = async (): Promise<void> => {
  if (pluginEditorTransitionRunning || pendingPluginEditorPayload === undefined) {
    return
  }

  pluginEditorTransitionRunning = true
  const requestedPayload = pendingPluginEditorPayload
  pendingPluginEditorPayload = undefined
  try {
    await handlePluginEditorTransition(requestedPayload)
  } finally {
    pluginEditorTransitionRunning = false
    if (pendingPluginEditorPayload !== undefined) {
      schedulePluginEditorTransition()
    }
  }
}

const schedulePluginEditorTransition = (): void => {
  if (pluginEditorTransitionScheduled || pluginEditorTransitionRunning) {
    return
  }

  pluginEditorTransitionScheduled = true
  queueMicrotask(() => {
    pluginEditorTransitionScheduled = false
    void processPluginEditorTransition()
  })
}

const requestPluginEditorTransition = (payload: PluginEditPayload | null): void => {
  if (loading.value) {
    return
  }
  // 同一次选区变化中，非命中插件可能先发出关闭事件；实际命中的打开请求应优先。
  if (payload || pendingPluginEditorPayload === undefined) {
    pendingPluginEditorPayload = payload
  }
  schedulePluginEditorTransition()
}

useEmitt({
  name: SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR,
  callback: (payload: PluginEditPayload) => {
    if (!payload?.config) {
      return
    }
    requestPluginEditorTransition(payload)
  }
})

useEmitt({
  name: SPREADSHEET_EVENTS.CLOSE_PLUGIN_EDITOR,
  callback: (discarded?: { unitId: string; pluginIds: string[] }) => {
    if (discarded) {
      if (univerApiRef.value?.getActiveWorkbook()?.getId() !== discarded.unitId) return
      // 删表可能发生在非活动 Sheet，仅撤销被丢弃草稿的打开请求或编辑面板。
      if (
        pendingPluginEditorPayload &&
        discarded.pluginIds.includes(pendingPluginEditorPayload.config.id)
      ) {
        pendingPluginEditorPayload = undefined
      }
      if (!currentPluginConfig.value || !discarded.pluginIds.includes(currentPluginConfig.value.id))
        return
    }
    requestPluginEditorTransition(null)
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
    markSpreadsheetChanged()
  }
})

type SaveDestination = { name: string; pid: string | number }

const persistSpreadsheet = async (
  destination?: SaveDestination,
  silentSuccess = false
): Promise<SpreadsheetVO | undefined> => {
  if (!sessionAvailable.value || saving.value || recovering.value) {
    return undefined
  }
  if (session.mode !== 'edit' && !session.id && !destination) {
    resourceGroupOptShow.value = true
    nextTick(() => {
      resourceGroupOpt.value?.optInit(spreadsheetName.value, session.pid)
    })
    return undefined
  }

  const revision = sessionRevision
  saving.value = true
  try {
    await flushDraftBeforeLeaving()
    if (!isCurrentSession(revision)) return undefined
    const rawSheetDataStr =
      univerSheetRef.value?.getSheetData() ||
      JSON.stringify(workbookData.value || createLocalizedDefaultWorkbookData())
    const cleanedSheetData = await pluginSnapshotCleaningService.clean(JSON.parse(rawSheetDataStr))
    if (!isCurrentSession(revision)) return undefined
    const currentSheetDataStr = serializeSheetData(cleanedSheetData)

    const editor: SpreadsheetEditor = {
      name: destination ? destination.name : spreadsheetName.value,
      sheetData: currentSheetDataStr,
      remark: spreadsheetRemark.value,
      version: spreadsheetVersion.value,
      id: session.id || undefined,
      pid: destination ? destination.pid : session.pid || 0,
      nodeType: 'sheet'
    }

    const result = session.id ? await update(editor) : await create(editor)
    if (!isCurrentSession(revision)) return undefined

    if (!silentSuccess) {
      ElMessage.success(t('spreadsheet.save_success'))
    }

    const isNewResource = !session.id
    // 新建成功后接纳资源 ID，随后更新路由时保留保存期间继续编辑的工作簿。
    session.id = String(result.id)
    session.pid = String(result.pid ?? 0)
    session.mode = 'edit'
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Unpublished
    spreadsheetRemark.value = result.remark ?? spreadsheetRemark.value
    spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
    hasChanges.value = false
    if (destination) {
      resourceGroupOptShow.value = false
      spreadsheetName.value = destination.name
    }
    updateSavedBaseline(currentSheetDataStr, editor.name || spreadsheetName.value)
    deleteSpreadsheetDraftCache(result.id)
    // 先保留保存期间的后续输入，即使新建后的路由更新失败也不会丢失草稿。
    draftCheckRevision++
    draftCheckPending = true
    await syncDraftCache()
    if (disposed.value || revision !== sessionRevision) return undefined
    if (isEmbedded) {
      embeddedStore.setResourceId(session.id)
      embeddedStore.setPid(session.pid)
      embeddedStore.setOpt('edit')
    } else if (isNewResource || routeMode.value !== 'edit') {
      await router.replace({
        query: { id: String(result.id), opt: 'edit', pid: String(result.pid) }
      })
    }
    if (!isCurrentSession(revision)) return undefined
    return result
  } catch (e) {
    if (!isCurrentSession(revision)) return undefined
    console.error(e)
    presentError(e, t('spreadsheet.save_error'))
    return undefined
  } finally {
    saving.value = false
  }
}

const handleSave = () => persistSpreadsheet()

const persistAndOpenOuterParams = async () => {
  const revision = sessionRevision
  openingOuterParams.value = true
  try {
    // 先保存工作簿，使外参绑定校验使用的编辑稿与当前条件、数据区域一致。
    const saved = await persistSpreadsheet(undefined, true)
    if (!saved || !isCurrentSession(revision)) {
      return
    }
    const savedWorkbook = parseSheetData(saved.sheetData)
    if (!savedWorkbook) {
      throw new Error(t('spreadsheet.outer_params_load_error'))
    }
    const context = await loadSpreadsheetOuterParamsEditorContext(savedWorkbook)
    if (!isCurrentSession(revision)) {
      return
    }

    outerParamsContext.value = context
    outerParamsVisible.value = true
  } catch (error) {
    if (isCurrentSession(revision)) {
      presentError(error, t('spreadsheet.outer_params_load_error'))
    }
  } finally {
    if (isCurrentSession(revision)) {
      openingOuterParams.value = false
    }
  }
}

const handleOpenOuterParams = () => {
  if (
    !sessionAvailable.value ||
    mutating.value ||
    openingOuterParams.value ||
    previewVisible.value
  ) {
    return
  }
  if (!session.id) {
    ElMessage.warning(t('spreadsheet.save_first'))
    return
  }
  pendingPublish.value = false
  void persistAndOpenOuterParams()
}

const handleOuterParamsSaved = (config: SpreadsheetOuterParamsConfig) => {
  if (config.spreadsheetId !== session.id || !sessionAvailable.value) {
    return
  }
  if (spreadsheetStatus.value === SpreadsheetPublishStatus.Published) {
    spreadsheetStatus.value = SpreadsheetPublishStatus.SavedUnpublished
  }
}

const publishSavedSpreadsheet = async (id: string | number) => {
  if (!sessionAvailable.value || session.id !== String(id)) return
  const revision = sessionRevision
  const result = await updateStatus({
    id,
    status: SpreadsheetPublishStatus.Published
  })
  if (!isCurrentSession(revision)) return
  spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Published
  spreadsheetRemark.value = result.remark ?? spreadsheetRemark.value
  spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
  ElMessage.success(t('spreadsheet.publish_success'))
}

const handlePublish = async () => {
  if (!sessionAvailable.value || mutating.value) return
  if (!session.id) {
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
    presentError(error, t('spreadsheet.publish_error'))
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
    presentError(error, t('spreadsheet.publish_error'))
  } finally {
    publishing.value = false
    pendingPublish.value = false
  }
}

const handleRecoverPublished = async () => {
  if (
    !sessionAvailable.value ||
    mutating.value ||
    spreadsheetStatus.value !== SpreadsheetPublishStatus.SavedUnpublished ||
    !session.id
  ) {
    return
  }

  const revision = sessionRevision
  const sheetId = session.id
  recovering.value = true
  try {
    await ElMessageBox.confirm(t('commons.prompt'), {
      tip: t('spreadsheet.recover_publish_confirm'),
      confirmButtonText: t('commons.confirm'),
      cancelButtonText: t('commons.cancel'),
      type: 'warning',
      autofocus: false
    })
  } catch {
    recovering.value = false
    return
  }

  try {
    if (!isCurrentSession(revision)) return
    const result = await recoverToPublished(sheetId)
    if (!isCurrentSession(revision)) return
    const publishedWorkbookData = parseSheetData(result.sheetData)
    if (!publishedWorkbookData) {
      throw new Error('Invalid published spreadsheet snapshot')
    }
    // 服务端已返回持久化快照，不能再按旧实例的表格范围清理，否则会误删普通单元格。
    const recoveredWorkbookData = clearUniverProtectionResources(publishedWorkbookData)
    const recoveredSheetData = serializeSheetData(recoveredWorkbookData)

    resetWorkbookEditor()
    await nextTick()
    if (!isCurrentSession(revision)) return

    workbookData.value = recoveredWorkbookData
    spreadsheetName.value = result.name
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Published
    spreadsheetRemark.value = result.remark ?? ''
    spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
    updateSavedBaseline(recoveredSheetData, spreadsheetName.value)
    hasChanges.value = false
    deleteSpreadsheetDraftCache(sheetId)
    loading.value = false
    ElMessage.success(t('spreadsheet.recover_publish_success'))
  } catch (error) {
    if (!isCurrentSession(revision)) return
    console.error(error)
    loading.value = false
    presentError(error, t('spreadsheet.recover_publish_error'))
  } finally {
    recovering.value = false
  }
}

const handleCancelPublish = async () => {
  if (
    !sessionAvailable.value ||
    mutating.value ||
    spreadsheetStatus.value === SpreadsheetPublishStatus.Unpublished ||
    !session.id
  ) {
    return
  }

  const revision = sessionRevision
  publishing.value = true
  try {
    const result = await updateStatus({
      id: session.id,
      status: SpreadsheetPublishStatus.Unpublished
    })
    if (!isCurrentSession(revision)) return
    spreadsheetStatus.value = result.status ?? SpreadsheetPublishStatus.Unpublished
    spreadsheetVersion.value = result.version ?? spreadsheetVersion.value
    ElMessage.success(t('spreadsheet.cancel_publish_success'))
  } catch (error) {
    console.error(error)
    presentError(error, t('spreadsheet.cancel_publish_error'))
  } finally {
    publishing.value = false
  }
}

const confirmLeave = async (): Promise<boolean> => {
  if (mutating.value || leaving.value) return false
  leaving.value = true
  const revision = sessionRevision
  try {
    await flushDraftBeforeLeaving()
    if (disposed.value || revision !== sessionRevision) return false
    const isCreateMode = session.mode !== 'edit' || !session.id
    if (!loading.value && (isCreateMode || hasChanges.value)) {
      await ElMessageBox.confirm(t('commons.prompt'), {
        tip: t('spreadsheet.confirm_exit_without_save'),
        confirmButtonText: t('commons.confirm'),
        cancelButtonText: t('commons.cancel'),
        confirmButtonType: 'primary',
        type: 'warning',
        autofocus: false,
        showClose: false
      })
    }
    return !disposed.value && revision === sessionRevision
  } catch {
    return false
  } finally {
    leaving.value = false
  }
}

const handleBack = async () => {
  if (!(await confirmLeave())) return
  const sheetId = session.id
  discardAndLeave = true
  if (isEmbedded) {
    deleteSpreadsheetDraftCache(sheetId)
    embeddedStore.clearState()
    embeddedStore.setResourceId(session.id ?? '')
    embeddedStore.setPid(session.pid ?? '')
    emitter.emit('changeCurrentComponent', 'SpreadsheetPanel')
    return
  }
  try {
    const failure = await router.push('/spreadsheet/index')
    if (failure) {
      discardAndLeave = false
      return
    }
    deleteSpreadsheetDraftCache(sheetId)
  } catch (error) {
    discardAndLeave = false
    throw error
  }
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
  markSpreadsheetChanged()
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
  if (!sessionAvailable.value) return
  const revision = sessionRevision
  let enteredFullscreen = false
  let previewOpened = false
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
    if (!isCurrentSession(revision)) return
    const rawSnapshot = univerSheetRef.value?.getSheetData()
    const snapshot = parseSheetData(rawSnapshot) ?? createLocalizedDefaultWorkbookData()
    const cleanedSnapshot = await pluginSnapshotCleaningService.clean(snapshot)
    if (!isCurrentSession(revision)) return
    const cleanedSnapshotData = serializeSheetData(cleanedSnapshot)
    workbookData.value = parseSheetData(cleanedSnapshotData) ?? createLocalizedDefaultWorkbookData()
    previewWorkbookData.value =
      parseSheetData(cleanedSnapshotData) ?? createLocalizedDefaultWorkbookData()
    currentPluginConfig.value = null
    showEditor.value = false
    // 从编辑态直接进入全屏时，退出全屏应回到编辑态；普通预览内的全屏仍回到普通预览。
    closePreviewOnFullscreenExit.value = enteredFullscreen
    previewVisible.value = true
    previewOpened = true
  } finally {
    if (!previewOpened && enteredFullscreen && document.fullscreenElement === editorRootRef.value) {
      await document.exitFullscreen()
    }
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
  if (isEmbedded) {
    onInitReady({ resourceId: session.id })
  }
}

// 提供 Univer API 给子组件
provide('univerApi', univerApiRef)

const restoreCachedDraft = async (sheetId: string, revision: number) => {
  const cachedDraft = getSpreadsheetDraftCache(sheetId)
  if (!cachedDraft) return
  if (cachedDraft.sourceVersion !== spreadsheetVersion.value) {
    deleteSpreadsheetDraftCache(sheetId)
    return
  }

  try {
    const cachedWorkbookData = clearUniverProtectionResources(
      parseSheetData(cachedDraft.sheetData) ?? createLocalizedDefaultWorkbookData()
    )
    const cachedSheetData = serializeSheetData(cachedWorkbookData)
    const cacheHasChanges =
      cachedSheetData !== baselineSheetData || cachedDraft.name !== baselineSpreadsheetName
    if (!cacheHasChanges) {
      deleteSpreadsheetDraftCache(sheetId)
      return
    }

    await ElMessageBox.confirm(t('commons.prompt'), {
      tip: t('spreadsheet.cache_use_tips'),
      confirmButtonText: t('visualization.yes'),
      cancelButtonText: t('visualization.no'),
      confirmButtonType: 'primary',
      type: 'warning',
      autofocus: false,
      showClose: false
    })
    if (!isCurrentSession(revision)) return
    workbookData.value = cachedWorkbookData
    spreadsheetName.value = cachedDraft.name
    spreadsheetRemark.value = cachedDraft.remark
    hasChanges.value = true
    cacheCurrentDraft(cachedSheetData)
  } catch {
    if (!isCurrentSession(revision)) return
    // 用户拒绝恢复或缓存损坏时，继续使用已加载的服务端工作簿。
    deleteSpreadsheetDraftCache(sheetId)
  }
}

const loadSpreadsheet = async () => {
  cacheRawDraftBeforeUnload()
  const revision = ++sessionRevision
  loadController?.abort()
  const controller = new AbortController()
  loadController = controller
  resetWorkbookEditor()
  discardAndLeave = false
  updateSavedBaseline('', '')
  hasChanges.value = false
  workbookData.value = undefined
  spreadsheetName.value = t('spreadsheet.new_spreadsheet')
  spreadsheetRemark.value = ''
  spreadsheetVersion.value = 1
  spreadsheetStatus.value = SpreadsheetPublishStatus.Unpublished
  pendingPublish.value = false
  resourceGroupOptShow.value = false
  previewVisible.value = false
  previewWorkbookData.value = undefined
  closePreviewOnFullscreenExit.value = false
  session.id = routeSheetId.value
  session.mode = routeMode.value
  session.pid = routePid.value
  if (editorRootRef.value && document.fullscreenElement === editorRootRef.value) {
    await document.exitFullscreen().catch(error => console.warn(error))
  }
  // 先卸载旧 Univer 实例，再接纳新资源，避免实例和插件状态跨资源复用。
  await nextTick()
  if (!isCurrentSession(revision)) return

  const sheetId = session.id
  if (!sheetId) {
    workbookData.value = createLocalizedDefaultWorkbookData()
    loading.value = false
    return
  }

  try {
    // 带资源 ID 的页面必须加载编辑稿，不能把新建空白内容保存到已有资源。
    if (session.mode !== 'edit') {
      throw new Error('Invalid spreadsheet edit mode')
    }
    const spreadsheetInfo = await findEditById(sheetId, controller.signal)
    if (!isCurrentSession(revision)) return
    if (!spreadsheetInfo) {
      throw new Error('Spreadsheet not found')
    }
    session.pid = String(spreadsheetInfo.pid ?? 0)
    const loadedWorkbookData = parseSheetData(spreadsheetInfo.sheetData)
    if (!loadedWorkbookData) {
      throw new Error('Invalid spreadsheet snapshot')
    }
    // 在 createUnit 加载资源前删除旧版 Univer 保护数据，
    // 避免历史工作表或区域保护规则在打开时直接锁定工作簿。
    workbookData.value = clearUniverProtectionResources(loadedWorkbookData)
    spreadsheetName.value = spreadsheetInfo.name
    spreadsheetStatus.value = spreadsheetInfo.status ?? SpreadsheetPublishStatus.Unpublished
    spreadsheetRemark.value = spreadsheetInfo.remark ?? ''
    spreadsheetVersion.value = spreadsheetInfo.version ?? 1
    updateSavedBaseline(serializeSheetData(workbookData.value), spreadsheetInfo.name)

    await restoreCachedDraft(sheetId, revision)
    if (!isCurrentSession(revision)) return
    loading.value = false
  } catch (e) {
    if (!isCurrentSession(revision) || controller.signal.aborted) return
    console.error(e)
    presentError(e, t('spreadsheet.load_error'))
    loading.value = true
  }
}

if (!isEmbedded) {
  onBeforeRouteUpdate((to, from) => {
    if (to.query.id === from.query.id && to.query.opt === from.query.opt) return true
    // 新建保存只补充当前会话的 ID，不属于切换资源。
    if (!loading.value && to.query.id === session.id && to.query.opt === session.mode) return true
    return confirmLeave()
  })
  onBeforeRouteLeave(() => discardAndLeave || confirmLeave())
  watch([routeSheetId, routeMode], () => {
    if (router.currentRoute.value.path !== editorRoutePath) return
    if (!loading.value && sessionMatchesRoute.value) return
    void loadSpreadsheet()
  })
}

onBeforeMount(() => {
  void loadSpreadsheet()
})

onMounted(() => {
  window.addEventListener('pagehide', cacheRawDraftBeforeUnload)
})

onBeforeUnmount(() => {
  window.removeEventListener('pagehide', cacheRawDraftBeforeUnload)
  if (!discardAndLeave) {
    cacheRawDraftBeforeUnload()
  }
  disposed.value = true
  sessionRevision++
  stopDraftCheck()
  loadController?.abort()
})
</script>

<template>
  <div
    ref="editorRootRef"
    class="spreadsheet-editor"
    :class="{ embedded: isEmbedded }"
    v-loading="loading || openingOuterParams"
  >
    <SpreadsheetToolbar
      :key="sessionRevision"
      :name="spreadsheetName"
      :saving="saving"
      :publishing="publishing"
      :recovering="recovering"
      :disabled="!sessionAvailable || openingOuterParams || outerParamsVisible"
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
      @outer-params="handleOpenOuterParams"
      @rename="handleRename"
    />

    <div class="editor-main">
      <div class="editor-canvas">
        <UniverSheet
          v-if="!loading && !previewVisible"
          ref="univerSheetRef"
          :model-value="workbookData || undefined"
          :locale="localeStore.getCurrentLocale.lang"
          mode="edit"
          @change="markSpreadsheetChanged"
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
    <SpreadsheetOuterParamsDialog
      v-if="outerParamsVisible && session.id"
      v-model="outerParamsVisible"
      :spreadsheet-id="session.id"
      :context="outerParamsContext"
      @saved="handleOuterParamsSaved"
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

  &.embedded {
    position: relative;
    width: 100%;
    height: 100%;

    .editor-main {
      height: auto;
    }

    .spreadsheet-preview-overlay {
      position: absolute;
    }
  }

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
