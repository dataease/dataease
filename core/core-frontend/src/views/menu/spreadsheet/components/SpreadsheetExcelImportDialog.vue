<script lang="ts" setup>
import { computed, onBeforeUnmount, ref, shallowRef, watch } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import { DateSystem, Tools } from '@univerjs/core'
import type { Column } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import uploadIcon from '@/assets/svg/icon_upload_outlined.svg'
import excelIcon from '@/assets/svg/icon_file-excel_colorful.svg'
import {
  FORMULA_MODES,
  IMPORT_LIMITS,
  suggestSheetNames,
  validateSheetNames,
  type FormulaMode,
  type ImportAnalysis,
  type ImportOptions,
  type ImportedSheet,
  type ImportRequest,
  type ImportResponse,
  type ImportSheetInfo
} from '../excel-import/protocol'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    mode: 'create' | 'append'
    targetName: string
    existingNames: string[]
    dateSystem?: DateSystem
    apply: (sheets: ImportedSheet[], name: string) => void | Promise<void>
  }>(),
  { dateSystem: DateSystem.Date1900 }
)
const emit = defineEmits<{ (event: 'update:modelValue', value: boolean): void }>()
const { t } = useI18n()
const { t: tr } = useI18n('spreadsheet.excel_import')
// 与新建电子表格的首次保存表单使用相同的资源名长度。
const maxBookNameLength = 50
const limits = {
  mb: IMPORT_LIMITS.fileBytes / (1024 * 1024),
  rows: IMPORT_LIMITS.sheetRows,
  cells: IMPORT_LIMITS.workbookCells,
  length: IMPORT_LIMITS.sheetNameLength
}
const step = ref(1)
const stepPanel = ref<HTMLElement>()
const stepHeight = ref<number>()
// 测量内层内容的自然高度，让外层过渡也能跟上异步返回的公式检查结果。
useResizeObserver(stepPanel, ([entry]) => {
  if (props.modelValue && entry.target === stepPanel.value)
    stepHeight.value = entry.contentRect.height
})
const loading = ref(false)
const submitting = ref(false)
const applying = ref(false)
const error = ref('')
const file = shallowRef<File>()
const sheets = shallowRef<ImportSheetInfo[]>([])
const selectedIds = ref<number[]>([])
const showHidden = ref(false)
const previewId = ref<number>()
type PreviewRow = { rowNumber: number; cells: (string | null)[] }
const previewRows = shallowRef<PreviewRow[]>([])
const previewPending = ref(false)
const previewError = ref('')
const bookName = ref('')
const overrides = ref<Record<number, string>>({})
const keepFormat = ref(true)
const formulaMode = ref<FormulaMode>('compatible')
const confirmedFallback = ref(false)
const analysis = shallowRef<ImportAnalysis>()
const analysisError = ref('')
const issuePage = ref(1)
const issuePageSize = 20
const fileSize = computed(() => {
  const kb = (file.value?.size || 0) / 1024
  return kb < 1024 ? kb.toFixed(1) + ' KB' : (kb / 1024).toFixed(1) + ' MB'
})
const steps = ['upload', 'select', 'confirm']
const hasHidden = computed(() => sheets.value.some(sheet => sheet.hidden))
const visibleSheets = computed(() =>
  sheets.value.filter(sheet => showHidden.value || !sheet.hidden)
)
const selected = computed(() => {
  const ids = new Set(selectedIds.value)
  return sheets.value.filter(sheet => ids.has(sheet.id))
})
const totalCells = computed(() =>
  selected.value.reduce((n, sheet) => n + sheet.rows * sheet.columns, 0)
)
const mappings = computed(() => {
  const names = suggestSheetNames(
    selected.value.map(sheet => sheet.name),
    props.existingNames
  )
  return selected.value.map((sheet, index) => ({
    id: sheet.id,
    name: overrides.value[sheet.id] ?? names[index]
  }))
})
const nameErrors = computed(() =>
  validateSheetNames(
    mappings.value.map(sheet => sheet.name),
    props.existingNames
  )
)
// 分析使用同一份名称映射，保证预检与最终写入的跨表引用一致。
const options = computed<ImportOptions>(() => ({
  sheets: mappings.value.map(sheet => ({ id: sheet.id, name: sheet.name.trim() })),
  dateSystem: props.dateSystem,
  keepFormat: keepFormat.value,
  formulaMode: formulaMode.value,
  confirmedFallback: false
}))
const issueGroups = computed(() => {
  if (!analysis.value) return []
  const { errors, fallbacks, warnings } = analysis.value
  return [
    { kind: 'error', issues: errors },
    { kind: 'fallback', issues: fallbacks },
    { kind: 'warning', issues: warnings }
  ]
})
const issueCount = computed(() =>
  issueGroups.value.reduce((sum, group) => sum + group.issues.length, 0)
)
const issuesOnPage = computed(() => {
  // 优先展示阻断项，只为当前页创建展示数据。
  let offset = (issuePage.value - 1) * issuePageSize
  return issueGroups.value.flatMap(({ kind, issues }) => {
    const visible = issues.slice(Math.max(0, offset), Math.max(0, offset + issuePageSize))
    offset -= issues.length
    return visible.map(issue => ({ ...issue, kind }))
  })
})
const previewSheet = computed(() => sheets.value.find(sheet => sheet.id === previewId.value))
const previewColumnCount = computed(() =>
  Math.min(previewSheet.value?.columns || 0, IMPORT_LIMITS.previewColumns)
)
const previewColumns = computed<Column[]>(() => [
  { dataKey: 'rowNumber', width: 44, fixed: true, class: 'preview-row-number' },
  ...Array.from({ length: previewColumnCount.value }, (_, index) => ({
    key: index,
    title: Tools.chatAtABC(index),
    width: 112,
    dataGetter: ({ rowData }: { rowData: PreviewRow }) => rowData.cells[index] ?? tr('noCache')
  }))
])
const bookNameValid = computed(() => props.mode === 'append' || !!bookName.value.trim())
const canNext = computed(() => selected.value.length > 0 && bookNameValid.value)
const canConfirm = computed(
  () =>
    canNext.value &&
    analysis.value?.errors.length === 0 &&
    !nameErrors.value.some(Boolean) &&
    (formulaMode.value !== 'compatible' ||
      !analysis.value.fallbacks.length ||
      confirmedFallback.value) &&
    !submitting.value
)

type WithoutId<T> = T extends unknown ? Omit<T, 'id'> : never
let worker: Worker | undefined
let sequence = 0
const pending = new Map<
  number,
  { resolve: (result: ImportResponse) => void; reject: (reason: Error) => void }
>()

function releaseWorker() {
  worker?.terminate()
  worker = undefined
  for (const request of pending.values()) request.reject(new Error('workerUnavailable'))
  pending.clear()
}

function startWorker() {
  const active = new Worker(new URL('../excel-import/worker.ts', import.meta.url), {
    type: 'module'
  })
  worker = active
  active.onmessage = ({ data }: MessageEvent<ImportResponse>) => {
    const task = pending.get(data.id)
    if (!task || worker !== active) return
    pending.delete(data.id)
    if (data.type === 'error') task.reject(new Error(data.code))
    else task.resolve(data)
  }
  active.onerror = active.onmessageerror = () => {
    if (worker !== active) return
    // Worker 已退出，回到上传页让用户直接重选文件，避免停留在等待分析的状态。
    reset()
    error.value = tr('workerUnavailable')
  }
}

function request(message: WithoutId<ImportRequest>): Promise<ImportResponse> {
  return new Promise((resolve, reject) => {
    if (!worker) throw new Error('workerUnavailable')
    const id = ++sequence
    pending.set(id, { resolve, reject })
    try {
      worker.postMessage({ ...message, id })
    } catch (cause) {
      pending.delete(id)
      reject(cause)
    }
  })
}

function reset() {
  releaseWorker()
  step.value = 1
  file.value = undefined
  sheets.value = []
  selectedIds.value = []
  previewId.value = undefined
  previewRows.value = []
  error.value = ''
  analysisError.value = ''
  analysis.value = undefined
  showHidden.value = false
  keepFormat.value = true
  formulaMode.value = 'compatible'
  overrides.value = {}
  bookName.value = ''
  loading.value = false
  submitting.value = false
  applying.value = false
  confirmedFallback.value = false
}

watch(
  () => props.modelValue,
  visible => {
    if (!visible) reset()
  },
  { flush: 'sync' }
)
onBeforeUnmount(reset)

async function loadFile(next: File) {
  if (!props.modelValue || applying.value) return
  reset()
  if (!/\.xlsx$/i.test(next.name) || next.size > IMPORT_LIMITS.fileBytes) {
    error.value = tr(next.size > IMPORT_LIMITS.fileBytes ? 'fileSize' : 'fileType', limits)
    return
  }
  file.value = next
  bookName.value = next.name
    .replace(/\.xlsx$/i, '')
    .trim()
    .slice(0, maxBookNameLength)
  loading.value = true
  try {
    startWorker()
    const result = await request({ type: 'load', file: next })
    if (file.value !== next || result.type !== 'loaded') return
    sheets.value = result.sheets
    selectedIds.value = visibleSheets.value.map(sheet => sheet.id)
    previewId.value = selectedIds.value[0]
    step.value = 2
  } catch (cause) {
    if (file.value === next) {
      releaseWorker()
      error.value = tr(workerError(cause), limits)
    }
  } finally {
    if (file.value === next) loading.value = false
  }
}

function workerError(cause: unknown) {
  return cause instanceof Error && /^[a-zA-Z]+$/.test(cause.message) ? cause.message : 'fileInvalid'
}

function selectFile(file: File) {
  // 文件交给本地 Worker 解析，阻止上传组件发送网络请求。
  void loadFile(file)
  return false
}

function toggleSheet(id: number) {
  const ids = new Set(selectedIds.value)
  if (ids.has(id)) ids.delete(id)
  else ids.add(id)
  selectedIds.value = [...ids]
}

function toggleAll() {
  selectedIds.value =
    selected.value.length === visibleSheets.value.length
      ? []
      : visibleSheets.value.map(sheet => sheet.id)
}

watch(showHidden, value => {
  if (value) {
    if (previewId.value === undefined) previewId.value = visibleSheets.value[0]?.id
    return
  }
  const visible = new Set(visibleSheets.value.map(sheet => sheet.id))
  selectedIds.value = selectedIds.value.filter(id => visible.has(id))
  if (previewId.value === undefined || !visible.has(previewId.value))
    previewId.value = visibleSheets.value[0]?.id
})

watch([previewId, step], async ([sheetId, currentStep], _, cleanup) => {
  let current = true
  cleanup(() => {
    current = false
  })
  previewRows.value = []
  previewError.value = ''
  previewPending.value = false
  if (!worker || sheetId === undefined || currentStep !== 2) return
  previewPending.value = true
  try {
    const result = await request({ type: 'preview', sheetId })
    if (current && result.type === 'preview') {
      previewRows.value = result.rows.map((cells, index) => ({ rowNumber: index + 1, cells }))
    }
  } catch (cause) {
    if (current) previewError.value = tr(workerError(cause), limits)
  } finally {
    if (current) previewPending.value = false
  }
})

watch(options, (value, _, cleanup) => {
  analysis.value = undefined
  analysisError.value = ''
  confirmedFallback.value = false
  issuePage.value = 1
  if (!worker || !value.sheets.length) return
  let current = true
  // 连续修改名称或勾选时，只分析最后一组选择；已发出的旧回包同时失效。
  const timer = window.setTimeout(async () => {
    try {
      const result = await request({ type: 'analyze', options: value })
      if (current && result.type === 'analysis') analysis.value = result.analysis
    } catch (cause) {
      if (current) analysisError.value = tr(workerError(cause), limits)
    }
  }, 100)
  cleanup(() => {
    current = false
    window.clearTimeout(timer)
  })
})

function previous() {
  error.value = ''
  if (step.value === 2) reset()
  else step.value = 2
}

function cancel() {
  if (applying.value) return
  // 在关闭动画之前终止转换，避免取消后仍向编辑器写入工作表。
  releaseWorker()
  emit('update:modelValue', false)
}

async function confirm() {
  if (!canConfirm.value) return
  error.value = ''
  submitting.value = true
  const active = worker
  try {
    const result = await request({
      type: 'convert',
      options: { ...options.value, confirmedFallback: confirmedFallback.value }
    })
    if (worker !== active || result.type !== 'converted') return
    applying.value = true
    await props.apply(result.sheets, bookName.value.trim())
    if (worker === active) emit('update:modelValue', false)
  } catch (cause) {
    if (worker === active) {
      error.value = applying.value
        ? (cause instanceof Error && cause.message) || tr('applyFailed')
        : tr(workerError(cause), limits)
    }
  } finally {
    if (worker === active) {
      applying.value = false
      submitting.value = false
    }
  }
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    :title="t('spreadsheet.import_excel')"
    width="min(900px, 94vw)"
    top="6vh"
    append-to-body
    destroy-on-close
    :close-on-click-modal="false"
    :close-on-press-escape="!applying"
    :show-close="!applying"
    :before-close="cancel"
    @closed="stepHeight = undefined"
  >
    <el-steps class="import-steps" :active="step - 1" custom align-center>
      <el-step
        v-for="(key, i) in steps"
        :key="key"
        :title="tr(key)"
        :aria-current="step === i + 1 ? 'step' : undefined"
      />
    </el-steps>
    <div class="step-frame" :style="{ height: stepHeight ? `${stepHeight}px` : undefined }">
      <div :key="step" ref="stepPanel" class="import-content" :aria-busy="loading || submitting">
        <div v-if="step <= 2" :class="{ 'file-card': step === 2 }">
          <template v-if="step === 2">
            <excelIcon class="file-icon" aria-hidden="true" />
            <div class="file-info">
              <strong>{{ file?.name }}</strong
              ><small>{{ tr('fileInfo', { size: fileSize, count: sheets.length }) }}</small>
            </div>
          </template>
          <el-upload
            class="import-upload"
            :drag="step === 1"
            accept=".xlsx"
            :show-file-list="false"
            :before-upload="selectFile"
          >
            <template v-if="step === 1">
              <svg
                width="46"
                height="46"
                viewBox="0 0 24 24"
                fill="none"
                stroke="var(--ed-color-primary)"
                stroke-width="1.2"
                stroke-linecap="round"
                stroke-linejoin="round"
                aria-hidden="true"
              >
                <path
                  d="M15 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7ZM14 2v4a2 2 0 0 0 2 2h4M8 13h2m4 0h2M8 17h2m4 0h2"
                />
              </svg>
              <strong>{{ loading ? tr('reading') : tr('drop') }}</strong>
              <small>{{ loading ? file?.name : tr('fileHint', limits) }}</small>
              <el-button v-if="!loading" tag="span" type="primary" :icon="uploadIcon">
                {{ tr('choose') }}
              </el-button>
            </template>
            <el-button v-else text>{{ t('visualization.re_upload') }}</el-button>
          </el-upload>
        </div>

        <template v-if="step === 2">
          <div v-if="mode === 'create'" class="resource">
            <label for="excel-import-name">{{ t('spreadsheet.spreadsheet_name') }}</label>
            <el-input
              id="excel-import-name"
              v-model="bookName"
              :maxlength="maxBookNameLength"
              :placeholder="t('spreadsheet.spreadsheet_name_placeholder')"
            />
          </div>
          <div class="sheet-panels">
            <section class="sheet-panel">
              <header>
                <el-checkbox
                  :model-value="!!visibleSheets.length && selected.length === visibleSheets.length"
                  :indeterminate="selected.length > 0 && selected.length < visibleSheets.length"
                  :disabled="!visibleSheets.length"
                  @change="toggleAll"
                  >{{ tr('sheetList') }}</el-checkbox
                >
              </header>
              <div class="sheet-list">
                <div
                  v-for="sheet in visibleSheets"
                  :key="sheet.id"
                  class="sheet-row"
                  :class="{ active: previewId === sheet.id }"
                >
                  <el-checkbox
                    :model-value="selectedIds.includes(sheet.id)"
                    :aria-label="sheet.name"
                    @change="toggleSheet(sheet.id)"
                  />
                  <button
                    type="button"
                    class="sheet-button"
                    :aria-pressed="previewId === sheet.id"
                    @click="previewId = sheet.id"
                  >
                    <span :title="sheet.name">{{ sheet.name }}</span>
                    <small
                      >{{ tr('dimensions', { rows: sheet.rows, columns: sheet.columns })
                      }}<template v-if="sheet.hidden"> · {{ tr('hidden') }}</template></small
                    >
                  </button>
                </div>
              </div>
              <footer v-if="hasHidden">
                <el-checkbox v-model="showHidden">{{ tr('showHidden') }}</el-checkbox>
              </footer>
            </section>
            <section class="preview-panel">
              <header>
                <strong>{{ previewSheet?.name || tr('preview') }}</strong
                ><small v-if="previewSheet">{{
                  tr('previewHint', {
                    rows: Math.min(previewSheet.rows, IMPORT_LIMITS.previewRows),
                    columns: previewColumnCount
                  })
                }}</small>
              </header>
              <div v-if="previewPending" class="panel-message">{{ tr('reading') }}</div>
              <div v-else-if="previewError || !previewRows.length" class="panel-message">
                {{ previewError || tr('emptyPreview') }}
              </div>
              <el-auto-resizer v-else v-slot="{ width, height }" class="preview-grid">
                <el-table-v2
                  v-if="width && height"
                  :columns="previewColumns"
                  :data="previewRows"
                  :width="width"
                  :height="height"
                  :row-height="28"
                  :header-height="28"
                  :aria-label="tr('preview')"
                  row-key="rowNumber"
                  fixed
                />
              </el-auto-resizer>
            </section>
          </div>
          <div class="settings">
            <span>{{ tr('importContent') }}</span>
            <el-radio-group v-model="keepFormat" :aria-label="tr('importContent')">
              <el-radio :value="true">{{ tr('keepFormat') }}</el-radio>
              <el-radio :value="false">{{ tr('omitFormat') }}</el-radio>
            </el-radio-group>
            <span>{{ tr('formulaMode') }}</span>
            <el-radio-group v-model="formulaMode" :aria-label="tr('formulaMode')">
              <el-radio v-for="mode in FORMULA_MODES" :key="mode" :value="mode">{{
                tr(mode)
              }}</el-radio>
            </el-radio-group>
          </div>
        </template>

        <template v-if="step === 3">
          <dl class="summary">
            <dt>{{ tr('source') }}</dt>
            <dd>{{ file?.name }}</dd>
            <dt>{{ tr('destination') }}</dt>
            <dd v-if="mode === 'append'">{{ tr('appendTarget', { name: targetName }) }}</dd>
            <dd v-else>{{ tr('createTarget', { name: bookName.trim(), folder: targetName }) }}</dd>
            <dt>{{ tr('importOptions') }}</dt>
            <dd>
              {{ keepFormat ? tr('keepFormat') : tr('omitFormat') }} ·
              {{ tr(formulaMode) }}
            </dd>
          </dl>
          <div class="mapping-scroll">
            <table class="mapping-table">
              <thead>
                <tr>
                  <th>{{ tr('sourceSheet') }}</th>
                  <th>{{ tr('targetSheet') }}</th>
                  <th>{{ tr('size') }}</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(sheet, i) in selected" :key="sheet.id">
                  <td>{{ sheet.name }}</td>
                  <td>
                    <el-input
                      :model-value="mappings[i].name"
                      :model-modifiers="{ lazy: true }"
                      :disabled="submitting"
                      :aria-label="tr('targetSheet')"
                      @change="overrides[sheet.id] = $event"
                    />
                    <span v-if="nameErrors[i]" class="field-error">{{
                      tr(nameErrors[i], limits)
                    }}</span>
                  </td>
                  <td>{{ tr('dimensions', { rows: sheet.rows, columns: sheet.columns }) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </template>

        <template v-if="step > 1 && selected.length">
          <div v-if="analysis" class="formula-summary">
            <span>{{
              tr('formulaSummary', {
                kept: analysis.kept,
                cached: analysis.fallbacks.length,
                blocked: analysis.errors.length
              })
            }}</span>
            <span v-if="analysis.warnings.length" class="formula-warning" role="status">{{
              tr('riskHint')
            }}</span>
            <details
              v-if="issueCount"
              :open="!!(analysis.errors.length || analysis.warnings.length)"
              class="issue-details"
            >
              <summary>{{ tr('details') }}</summary>
              <div class="issue-list">
                <div
                  v-for="issue in issuesOnPage"
                  :key="issue.sheetName + '!' + issue.address"
                  :class="{ 'field-error': issue.kind === 'error' }"
                >
                  <b>{{ issue.sheetName }}!{{ issue.address }}</b> ·
                  {{ tr(issue.reason, { sheet: issue.referencedSheet }) }}
                  <template v-if="issue.missingCache"> · {{ tr('noCache') }}</template>
                  <template v-if="issue.kind === 'fallback'"> · {{ tr('useCache') }}</template>
                  <template v-else-if="issue.kind === 'warning'">
                    · {{ tr('keptForEditing') }}</template
                  >
                </div>
              </div>
              <el-pagination
                v-if="issueCount > issuePageSize"
                v-model:current-page="issuePage"
                layout="prev, pager, next"
                :page-size="issuePageSize"
                :total="issueCount"
              />
            </details>
            <div v-if="analysis.errors.length" class="field-error" role="alert">
              {{ tr(formulaMode === 'all' ? 'blockedAllHint' : 'blockedHint') }}
            </div>
            <el-checkbox
              v-if="step === 3 && formulaMode === 'compatible' && analysis.fallbacks.length"
              v-model="confirmedFallback"
              :disabled="submitting"
              >{{ tr('acceptCache') }}</el-checkbox
            >
          </div>
          <p v-else-if="analysisError" role="alert" class="field-error">{{ analysisError }}</p>
          <p v-else class="muted" role="status">{{ tr('analyzing') }}</p>
        </template>
        <p v-if="step === 3" class="muted">{{ tr('saveHint') }}</p>
        <p v-if="error" class="field-error" role="alert">{{ error }}</p>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <span v-if="step > 1" class="selection-count">{{
          tr('selectedCount', { sheets: selected.length, cells: totalCells.toLocaleString() })
        }}</span>
        <el-button :disabled="applying" @click="cancel">{{ t('common.cancel') }}</el-button>
        <el-button v-if="step > 1" :disabled="submitting" @click="previous">{{
          t('common.prev')
        }}</el-button>
        <el-button v-if="step === 2" type="primary" :disabled="!canNext" @click="step = 3">{{
          t('common.next')
        }}</el-button>
        <el-button
          v-if="step === 3"
          type="primary"
          :disabled="!canConfirm"
          :loading="submitting"
          @click="confirm"
          >{{ tr('confirm') }}</el-button
        >
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
.import-content {
  display: flow-root;
  color: var(--ed-text-color-primary);
  overflow-wrap: anywhere;
  animation: import-step-in 180ms ease-out;
  p {
    margin: 12px 0 0;
  }
}
.import-steps {
  max-width: 500px;
  margin: 0 auto 20px;
}
.step-frame {
  overflow: clip;
  overflow-clip-margin: 3px;
  transition: height 220ms ease-in-out;
}
@keyframes import-step-in {
  from {
    opacity: 0;
    transform: translateY(4px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.import-upload :deep(.ed-upload-dragger) {
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;
  justify-content: center;
  min-height: 254px;
  padding: 16px;
  background: #fafcff;
  border: 1px dashed #b7c1d1;
  border-radius: 6px;

  strong {
    font-size: 16px;
    font-weight: 500;
    color: #1f2329;
  }

  small {
    font-size: 12px;
    color: #8f959e;
  }

  .ed-button {
    height: 32px;
    padding: 5px 14px;
    margin-top: 4px;
    border-radius: 4px;
  }
}
.import-upload :deep(.ed-upload:focus-visible .ed-upload-dragger) {
  outline: 2px solid var(--ed-color-primary);
  outline-offset: -2px;
}
.muted,
small,
.selection-count,
.summary dt {
  color: var(--ed-text-color-secondary);
}
.file-card,
.resource,
.dialog-footer {
  display: flex;
  align-items: center;
  gap: 12px;
}
.file-card,
.summary,
.formula-summary {
  padding: 12px 16px;
  border-radius: 6px;
  background: var(--ed-fill-color-light);
}
.file-icon {
  width: 28px;
  height: 28px;
  flex: none;
}
.file-info {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 4px;
}
.resource {
  margin-top: 12px;
  label {
    white-space: nowrap;
  }
  .ed-input {
    max-width: 340px;
  }
}
.sheet-panels {
  display: grid;
  grid-template-columns: minmax(168px, 24%) minmax(0, 1fr);
  margin-top: 16px;
  border: 1px solid var(--ed-border-color);
  border-radius: 4px;
  overflow: hidden;
}
.sheet-panel {
  border-right: 1px solid var(--ed-border-color);
}
.sheet-panel,
.preview-panel {
  display: flex;
  flex-direction: column;
  height: 272px;
  overflow: hidden;
  header,
  footer {
    padding: 0 12px;
    flex: none;
    border-bottom: 1px solid var(--ed-border-color);
    min-height: 36px;
    display: flex;
    align-items: center;
    gap: 8px;
  }
  footer {
    border-top: 1px solid var(--ed-border-color);
    border-bottom: 0;
  }
}
.sheet-list {
  overflow: auto;
  flex: 1;
}
.sheet-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 3px 12px;
  &.active {
    background: var(--ed-color-primary-light-9);
  }
}
.sheet-button {
  flex: 1;
  min-width: 0;
  display: grid;
  gap: 2px;
  padding: 5px 0;
  text-align: start;
  color: inherit;
  font: inherit;
  background: transparent;
  border: 0;
  cursor: pointer;
  span {
    white-space: nowrap;
    text-overflow: ellipsis;
    overflow: hidden;
  }
}
.preview-panel header {
  flex-wrap: wrap;
}
.panel-message {
  margin: auto;
  color: var(--ed-text-color-secondary);
}
.mapping-scroll,
.issue-list {
  overflow: auto;
}
.preview-grid {
  flex: 1;
  min-height: 0;
  min-width: 0;

  :deep(.ed-table-v2__header-cell),
  :deep(.preview-row-number) {
    background: var(--ed-fill-color-light);
  }

  :deep(.ed-table-v2__row-cell),
  :deep(.ed-table-v2__header-cell) {
    border-right: 1px solid var(--ed-border-color-lighter);
  }
}
.settings {
  display: grid;
  grid-template-columns: auto 1fr;
  align-items: center;
  gap: 4px 16px;
  margin: 12px 0;
}
.formula-summary {
  display: grid;
  gap: 6px;
  margin-top: 12px;
}
.issue-details summary {
  cursor: pointer;
  color: var(--ed-color-primary);
}
.issue-list {
  max-height: 130px;
  line-height: 1.7;
}
.summary {
  display: grid;
  grid-template-columns: auto 1fr;
  gap: 6px 14px;
  margin: 0 0 12px;
  dd {
    margin: 0;
  }
}
.mapping-scroll {
  max-height: 245px;
}
.mapping-table {
  width: 100%;
  border-spacing: 0;
  font-size: 13px;
  th,
  td {
    text-align: start;
    padding: 6px 9px;
    border-bottom: 1px solid var(--ed-border-color-lighter);
  }
  th {
    background: var(--ed-fill-color-light);
    position: sticky;
    top: 0;
    z-index: 1;
  }
  td:nth-child(2) {
    width: 46%;
  }
}
.field-error {
  color: var(--ed-color-danger);
}
.formula-warning {
  color: var(--ed-color-warning);
}
.dialog-footer {
  justify-content: flex-end;
  .ed-button + .ed-button {
    margin-left: 0;
  }
}
.selection-count {
  margin-right: auto;
  font-size: 13px;
}
@media (max-width: 600px) {
  .sheet-panels {
    grid-template-columns: 1fr;
  }
  .sheet-panel {
    height: 160px;
  }
  .dialog-footer {
    flex-wrap: wrap;
  }
}
</style>
