<script lang="ts" setup>
import { computed, onUnmounted, ref, watch } from 'vue'
import { ElMessage } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import dayjs from 'dayjs'
import { useEmitt } from '@/hooks/web/useEmitt'
import SuccessIcon from '@/assets/svg/icon_succeed_filled.svg'
import NoticeIcon from '@/assets/svg/icon_info_colorful.svg'
import { uuid } from 'vue-uuid'
import { reportDeliveryApi, reportResendApi } from './api'
import { registerDeliveryLocale } from './deliveryLocale'
import RecipientAddress from './RecipientAddress.vue'

import ReportIcon from '@/assets/svg/toolbox-report.svg'
import CopyIcon from '@/assets/svg/icon_copy_outlined.svg'
import DownloadIcon from '@/assets/svg/icon_download_outlined.svg'
import RefreshIcon from '@/assets/svg/icon_refresh_outlined.svg'
import SearchIcon from '@/assets/svg/icon_search-outline_outlined.svg'
import EmailIcon from '@/assets/svg/read-msg.svg'
import WecomIcon from '@/assets/svg/logo_wechat-work.svg'
import DingtalkIcon from '@/assets/svg/logo_dingtalk.svg'
import LarkIcon from '@/assets/svg/logo_lark.svg'

registerDeliveryLocale()
const { t } = useI18n()
const tr = (key: string) => t(`reportDelivery.${key}`)
interface Attempt {
  id: string
  triggerType: number
  attemptNo: number
  artifactId?: string
  status: number
  stage?: string
  errorCode?: string
  errorMessage?: string
  operatorName?: string
  createTime: number
}
interface Recipient {
  id: string
  recipientName: string
  recipientAddress: string
  channel: number
  initialStatus: number
  currentStatus: number
  executionAction: number
  initialStage?: string
  initialErrorCode?: string
  initialErrorMessage?: string
  retryBlock: string
  artifactId?: string
  title?: string
  filter?: string
  generatedTime?: number
  files?: string[]
  attempts: Attempt[]
}
interface Delivery {
  instanceId: string
  taskName: string
  resourceName?: string
  format?: number
  execStatus: number
  triggerType: number
  startTime: number
  endTime: number
  legacy: boolean
  current: boolean
  snapshotState?: string
  log: string
  recipients: Recipient[]
}
const jobLogDetailVisible = ref(false)
const loading = ref(false)
const detail = ref<Delivery>()
const keyword = ref('')
const channel = ref<number | ''>('')
const selectedStatus = ref(-1)
const request = ref<{ instanceId?: string; taskId?: string }>({})
const confirmVisible = ref(false)
const confirmedRisk = ref(false)
const submitting = ref(false)
const selectedRows = ref<Recipient[]>([])
const requestKey = ref('')
const channelIcons = {
  1: EmailIcon,
  2: WecomIcon,
  3: DingtalkIcon,
  4: LarkIcon,
  5: LarkIcon,
  6: LarkIcon,
  8: LarkIcon,
  9: DingtalkIcon
}
const statuses = ['pending', 'sending', 'success', 'failed', 'unknown', 'notSent']
const triggers = ['scheduled', 'immediate', 'automatic', 'manual']
const channels: Record<number, string> = {
  1: 'email',
  2: 'wecom',
  3: 'dingtalk',
  4: 'lark',
  5: 'larksuite',
  6: 'larkGroup',
  7: 'webhook',
  8: 'larksuiteGroup',
  9: 'dingtalkGroup'
}
const stages: Record<string, string> = {
  GENERATE: 'generating',
  PREPARE: 'prepare',
  AUTHORIZE: 'authorize',
  CONNECT: 'connect',
  SUBMIT: 'submit'
}
const blocks: Record<string, string> = {
  EXPIRED: 'expired',
  SENDING: 'sending',
  SUCCESS: 'done',
  UNAVAILABLE: 'unavailable',
  NOT_SENT: 'notSent'
}
const rows = computed(() => detail.value?.recipients || [])
// Manual resends have their own list; normal executions and automatic retries are excluded.
const resendRecords = computed(() =>
  rows.value
    .flatMap(recipient =>
      recipient.attempts
        .filter(attempt => attempt.triggerType === 3)
        .sort((a, b) => a.attemptNo - b.attemptNo)
        .map((attempt, index) => ({
          ...attempt,
          resendNo: index + 1,
          recipientName: recipient.recipientName,
          recipientAddress: recipient.recipientAddress,
          channel: recipient.channel,
          artifactId: attempt.artifactId || recipient.artifactId
        }))
    )
    .sort((a, b) => a.createTime - b.createTime || a.attemptNo - b.attemptNo)
)
const resendMessage = (attempt: Attempt) => {
  if (attempt.status === 0 || attempt.status === 1) return tr('awaitingResult')
  if (attempt.status === 2) return tr('channelAccepted')
  return [attempt.errorCode, attempt.errorMessage].filter(Boolean).join(' / ') || tr('emptyError')
}
const snapshotMessages: Record<string, string> = {
  LEGACY: 'snapshotLegacy',
  PREPARING: 'snapshotPreparing',
  CLEARED: 'snapshotCleared',
  EXPIRED_ACTIVE: 'snapshotOccupied',
  EXPIRED: 'snapshotExpired',
  RESOLVED: 'snapshotResolved',
  RELEASED: 'snapshotReleased',
  MISSING: 'snapshotMissing',
  CLEANING: 'snapshotCleaning',
  UNAVAILABLE: 'snapshotUnavailable',
  PARTIAL: 'snapshotPartial'
}
const snapshotNotice = computed(() => {
  if (
    detail.value?.snapshotState === 'RELEASED' &&
    rows.value.length &&
    rows.value.every(row => row.initialStatus === 2)
  )
    return ''
  const key = snapshotMessages[detail.value?.snapshotState || '']
  return key ? tr(key) : ''
})
const manualAttempts = (row: Recipient) => row.attempts.filter(attempt => attempt.triggerType === 3)
const latestResend = (row: Recipient) => manualAttempts(row).at(-1)
const needsResend = (row: Recipient) => [3, 4, 5].includes(row.initialStatus) || !!latestResend(row)
const resendAction = (row: Recipient) => {
  const latest = latestResend(row)
  if (!latest) return tr('resend')
  if (latest.status === 0 || latest.status === 1) return tr('resending')
  return tr(latest.status === 2 ? 'resentSuccess' : 'resendAgain')
}
const resendStatus = (status: number) =>
  status === 0 || status === 1 ? tr('resending') : statusLabel(status)
const stageLabel = (row: Recipient) =>
  row.initialErrorCode === 'EMAIL_CONFIGURATION_INVALID'
    ? tr('emailConfiguration')
    : row.initialStage === 'SUBMIT'
    ? tr(row.channel === 1 ? 'submitEmail' : 'submitMessage')
    : row.initialStage
    ? tr(stages[row.initialStage] || 'submit')
    : ''
const reportFormats = computed(() => {
  const extensions = rows.value.flatMap(row =>
    (row.files || []).map(file => file.split(' / SHA-256:')[0].split('.').at(-1)?.toUpperCase())
  )
  return (
    [...new Set(extensions.filter(Boolean))].join(' / ') ||
    (detail.value?.format === 1 ? 'PDF' : detail.value?.format === 0 ? 'JPEG' : '-')
  )
})
const originalReports = computed(() => [
  ...new Map(selectedRows.value.map(row => [row.artifactId || row.id, row])).values()
])
const failures = computed(() => rows.value.filter(row => row.currentStatus === 3))
const resendable = computed(() => failures.value.filter(row => !row.retryBlock))
const filtered = computed(() =>
  rows.value.filter(
    row =>
      (selectedStatus.value === -1 || row.initialStatus === selectedStatus.value) &&
      (channel.value === '' || row.channel === channel.value) &&
      `${row.recipientName} ${row.recipientAddress}`
        .toLowerCase()
        .includes(keyword.value.trim().toLowerCase())
  )
)
const needsRisk = computed(() => selectedRows.value.some(row => row.currentStatus === 4))
const count = (status: number) => rows.value.filter(row => row.initialStatus === status).length
const hasNonSuccess = computed(
  () =>
    detail.value?.legacy ||
    detail.value?.execStatus === 3 ||
    rows.value.some(row => row.initialStatus !== 2)
)
const noEligibleRecipients = computed(
  () =>
    !detail.value?.legacy &&
    detail.value?.execStatus === 3 &&
    !rows.value.length &&
    [
      'No eligible recipients',
      '缺失有效接收者',
      '缺失有效接收者, 或者接收人员没有权限访问该资源'
    ].includes(detail.value?.log || '')
)
const resultNotice = computed(() => {
  if (detail.value?.legacy) return tr('legacyNotice')
  if (noEligibleRecipients.value) return tr('noEligibleRecipients')
  if (
    rows.value.length &&
    rows.value.every(row => row.initialStatus === 5) &&
    rows.value.some(row => row.initialStage === 'GENERATE')
  ) {
    return `${tr('generationFailed')} ${tr('sendingNotStarted')}`
  }
  const counts = { success: count(2), failed: count(3), unknown: count(4) }
  let message = t('reportDelivery.resultCounts', counts)
  if (count(5)) message += t('reportDelivery.unsentCount', { count: count(5) })
  const pending = count(0) + count(1)
  if (pending) message += t('reportDelivery.pendingCount', { count: pending })
  if (resendRecords.value.length) {
    message += t('reportDelivery.resendProgress', {
      failed: rows.value.filter(row => row.currentStatus === 3).length,
      unknown: rows.value.filter(row => row.currentStatus === 4).length,
      notSent: rows.value.filter(row => row.currentStatus === 5).length,
      sending: rows.value.filter(row => row.currentStatus === 0 || row.currentStatus === 1).length
    })
  } else if (count(4)) message += tr('resultNotice')
  return message
})
const toast = ref('')
let toastTimer: ReturnType<typeof setTimeout> | undefined
const toastQueue: string[] = []
const nextToast = () => {
  toast.value = toastQueue.shift() || ''
  if (toast.value) toastTimer = setTimeout(nextToast, 3500)
}
const notify = (message: string) => {
  toastQueue.push(message)
  if (!toast.value) nextToast()
}
let resendBatches: { recipientIds: string[]; previousIds: Set<string> }[] = []
const checkResendFinished = () => {
  resendBatches = resendBatches.filter(batch => {
    const finished = batch.recipientIds.every(id => {
      const attempts =
        rows.value
          .find(row => row.id === id)
          ?.attempts.filter(
            attempt => attempt.triggerType === 3 && !batch.previousIds.has(attempt.id)
          ) || []
      return (
        attempts.length > 0 &&
        attempts.every(attempt => attempt.status !== 0 && attempt.status !== 1)
      )
    })
    if (finished) {
      notify(tr('resendFinished'))
    }
    return !finished
  })
}
const clearNotices = () => {
  clearTimeout(toastTimer)
  toastQueue.length = 0
  toast.value = ''
  resendBatches = []
}
const date = (value?: number) => (value ? dayjs(value).format('YYYY-MM-DD HH:mm:ss') : '-')
const statusLabel = (status: number) => tr(statuses[status] || 'unknown')
let timer: ReturnType<typeof setTimeout> | undefined
let generation = 0
let refreshSequence = 0
let appliedSequence = 0
const refresh = async (initial = false, showLoading = true) => {
  const currentGeneration = generation
  const sequence = ++refreshSequence
  if (showLoading) loading.value = true
  try {
    const response = await reportDeliveryApi(request.value)
    if (currentGeneration !== generation || sequence < appliedSequence) return
    appliedSequence = sequence
    detail.value = response.data
    checkResendFinished()
    if (initial) selectedStatus.value = count(3) ? 3 : count(4) ? 4 : count(5) ? 5 : -1
  } finally {
    if (currentGeneration === generation) loading.value = false
  }
}
const poll = () => {
  timer = setTimeout(async () => {
    if (!jobLogDetailVisible.value) return
    try {
      await refresh(false, false)
    } finally {
      if (jobLogDetailVisible.value) poll()
    }
  }, 4000)
}
const startInterval = async (instanceId?: string, taskId?: string) => {
  generation++
  clearNotices()
  clearTimeout(timer)
  request.value = { instanceId, taskId }
  keyword.value = ''
  channel.value = ''
  detail.value = undefined
  await refresh(true)
  poll()
}
watch(jobLogDetailVisible, visible => {
  if (!visible) {
    generation++
    clearTimeout(timer)
    confirmVisible.value = false
    clearNotices()
  }
})
onUnmounted(() => {
  clearNotices()
  generation++
  clearTimeout(timer)
})
useEmitt({
  name: 'report-notice-call',
  callback: (payload: string) => {
    if (!jobLogDetailVisible.value || !detail.value) return
    try {
      const notice = JSON.parse(payload)
      if (String(notice.instanceId) === detail.value.instanceId) {
        void refresh(false, false).catch(() => {
          /* Polling retries after a transient disconnect. */
        })
      }
    } catch {
      /* Ignore malformed notifications. */
    }
  }
})
const filterDescription = (value?: string | null) => {
  if (value == null) return tr('filterUnrecorded')
  // Older backends return the raw per-chart parameter map in this field.
  try {
    const parsed = JSON.parse(value)
    if (parsed && typeof parsed === 'object') return tr('filterUnrecorded')
  } catch {
    // Current snapshots contain plain display text.
  }
  return value || tr('filterUnset')
}
const confirmResend = (targets: Recipient[]) => {
  selectedRows.value = targets
  confirmedRisk.value = false
  requestKey.value = uuid.v4()
  confirmVisible.value = true
}
const submit = async () => {
  if (submitting.value || (needsRisk.value && !confirmedRisk.value)) return
  submitting.value = true
  const currentGeneration = generation
  const recipientIds = selectedRows.value.map(row => row.id)
  const previousIds = new Set(resendRecords.value.map(row => row.id))
  try {
    await reportResendApi({
      instanceId: detail.value!.instanceId,
      recipientIds: selectedRows.value.map(row => row.id),
      idempotencyKey: requestKey.value,
      confirmUnknown: confirmedRisk.value
    })
    if (currentGeneration !== generation) return
    confirmVisible.value = false
    resendBatches.push({ recipientIds, previousIds })
    notify(t('reportDelivery.resendStarted', { count: recipientIds.length }))
    await refresh(false, false)
  } finally {
    submitting.value = false
  }
}
const copy = async () => {
  await navigator.clipboard.writeText(
    failures.value
      .map(row =>
        [
          row.recipientName,
          row.recipientAddress,
          tr(channels[row.channel]),
          row.attempts[row.attempts.length - 1]?.errorMessage ||
            row.initialErrorMessage ||
            tr('emptyError')
        ].join('\t')
      )
      .join('\n')
  )
  ElMessage.success(tr('copied'))
}
const csv = (value: unknown) => {
  let text = String(value ?? '')
  if (/^[\s]*[=+@-]/.test(text)) text = `'${text}`
  return `"${text.replace(/"/g, '""')}"`
}
const exportRows = () => {
  const data: unknown[][] = [
    [
      tr('name'),
      tr('address'),
      tr('channel'),
      tr('first'),
      tr('reason'),
      tr('current'),
      tr('files'),
      tr('history')
    ]
  ]
  for (const row of rows.value)
    data.push([
      row.recipientName,
      row.recipientAddress,
      tr(channels[row.channel]),
      statusLabel(row.initialStatus),
      [row.initialStage, row.initialErrorCode, row.initialErrorMessage].filter(Boolean).join(' / '),
      statusLabel(row.currentStatus),
      `${row.artifactId || ''}: ${(row.files || []).join('; ')}`,
      JSON.stringify(row.attempts)
    ])
  const url = URL.createObjectURL(
    new Blob(['\ufeff' + data.map(line => line.map(csv).join(',')).join('\r\n')], {
      type: 'text/csv;charset=utf-8'
    })
  )
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `report-${detail.value!.instanceId}.csv`
  anchor.click()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}
defineExpose({ jobLogDetailVisible, startInterval })
</script>

<template>
  <el-drawer
    v-model="jobLogDetailVisible"
    :title="tr('title')"
    class="report-delivery-drawer"
    size="min(1200px, 100vw)"
    direction="rtl"
    :close-on-click-modal="false"
  >
    <template #header
      ><div class="delivery-heading">
        <h2>{{ tr('title') }}</h2>
        <span
          v-if="detail?.execStatus"
          class="execution-badge"
          :class="`status-${detail.execStatus}`"
          ><span class="status-dot" />{{
            tr(
              noEligibleRecipients
                ? 'notSent'
                : detail.execStatus === 2
                ? 'executionSuccess'
                : detail.execStatus === 3
                ? 'executionFailed'
                : 'executionRunning'
            )
          }}</span
        >
      </div></template
    >
    <div v-if="toast" class="delivery-toast" role="status" aria-live="polite">
      <SuccessIcon class="notice-icon" />{{ toast }}
    </div>
    <div v-loading="loading" class="delivery">
      <template v-if="detail">
        <section class="task-meta">
          <div class="task-identity">
            <ReportIcon class="report-icon" />
            <div>
              <h3>{{ detail.taskName }}</h3>
              <p class="resource-caption">
                {{ tr('resource') }}: {{ detail.resourceName || tr('notRecorded') }} ·
                {{ reportFormats }}
              </p>
            </div>
          </div>
          <dl>
            <div>
              <dt>{{ tr('time') }}</dt>
              <dd>{{ date(detail.startTime) }}</dd>
            </div>
            <div>
              <dt>{{ tr('duration') }}</dt>
              <dd>
                {{
                  detail.endTime
                    ? `${Math.max(0, (detail.endTime - detail.startTime) / 1000).toFixed(1)} s`
                    : '-'
                }}
              </dd>
            </div>
            <div>
              <dt>{{ tr('trigger') }}</dt>
              <dd>{{ tr(triggers[detail.triggerType] || 'scheduled') }}</dd>
            </div>
          </dl>
        </section>
        <template v-if="detail">
          <section class="result-section">
            <div class="section-title">
              <h3>{{ tr('firstSummary') }}</h3>
              <span class="muted">{{ tr('summaryHint') }}</span>
            </div>
            <div class="summary">
              <div v-for="status in [-1, 2, 3, 4, 5]" :key="status" class="metric">
                <div class="metric-label">
                  <span v-if="status !== -1" class="status-dot" :class="`status-${status}`" />{{
                    status === -1 ? tr('total') : statusLabel(status)
                  }}
                </div>
                <div class="metric-value" :class="`status-${status}`">
                  {{
                    detail.legacy ? tr('unknownCount') : status === -1 ? rows.length : count(status)
                  }}
                </div>
              </div>
            </div>
            <div
              class="result-notice"
              :class="detail.legacy || hasNonSuccess ? 'has-warning' : 'is-success'"
            >
              <component
                :is="detail.legacy || hasNonSuccess ? NoticeIcon : SuccessIcon"
                class="notice-icon"
              />
              <span>{{ resultNotice }}</span>
            </div>
          </section>
          <div class="recipient-heading">
            <h3>{{ tr('recipients') }}</h3>
            <div class="recipient-actions">
              <el-button :disabled="!failures.length" @click="copy"
                ><CopyIcon class="action-icon" />{{ tr('copy') }}</el-button
              >
              <el-button :disabled="detail.legacy || !rows.length" @click="exportRows"
                ><DownloadIcon class="action-icon" />{{ tr('export') }}</el-button
              >
              <el-button
                type="primary"
                :title="tr('batchHint')"
                :disabled="!resendable.length"
                @click="confirmResend(resendable)"
                ><RefreshIcon class="action-icon" />{{ tr('batch')
                }}<span v-if="resendable.length"> ({{ resendable.length }})</span></el-button
              >
            </div>
          </div>
          <div v-if="snapshotNotice" class="snapshot-notice" role="status">
            <NoticeIcon class="notice-icon" /><span>{{ snapshotNotice }}</span>
          </div>
          <div class="recipient-toolbar">
            <div class="result-tabs" role="group" :aria-label="tr('first')">
              <button
                v-for="status in [3, 4, -1, 2, 5]"
                :key="status"
                type="button"
                :aria-pressed="selectedStatus === status"
                :class="['result-tab', { active: selectedStatus === status }, `tab-${status}`]"
                @click="selectedStatus = status"
              >
                {{ status === -1 ? tr('all') : statusLabel(status) }}
                <span>{{ status === -1 ? rows.length : count(status) }}</span>
              </button>
            </div>
            <div class="filter-controls">
              <el-select
                v-model="channel"
                class="channel-select"
                :placeholder="tr('allChannels')"
                :aria-label="tr('channel')"
                value-on-clear=""
                clearable
              >
                <el-option
                  v-for="(label, value) in channels"
                  :key="value"
                  :value="Number(value)"
                  :label="tr(label)"
                />
              </el-select>
              <el-input
                v-model="keyword"
                class="recipient-search"
                :placeholder="tr('search')"
                :aria-label="tr('search')"
                clearable
                ><template #prefix><SearchIcon class="search-icon" /></template
              ></el-input>
            </div>
          </div>
          <el-table :data="filtered" row-key="id" class="recipient-table">
            <template #empty
              ><div class="delivery-empty">
                <strong>{{ tr(detail.legacy ? 'legacyEmpty' : 'emptyRecords') }}</strong>
                <p>
                  {{ tr(detail.legacy ? 'legacyEmptyHint' : 'emptyRecordsHint') }}
                </p>
              </div></template
            >
            <el-table-column
              prop="recipientName"
              :label="tr('name')"
              min-width="120"
              show-overflow-tooltip
            />
            <el-table-column
              prop="recipientAddress"
              :label="tr('address')"
              min-width="190"
              show-overflow-tooltip
            >
              <template #default="{ row }">
                <RecipientAddress v-bind="row" />
              </template>
            </el-table-column>
            <el-table-column :label="tr('channel')" width="110"
              ><template #default="{ row }"
                ><span class="channel-name"
                  ><component :is="channelIcons[row.channel]" v-if="channelIcons[row.channel]" />{{
                    tr(channels[row.channel])
                  }}</span
                ></template
              ></el-table-column
            >
            <el-table-column :label="tr('first')" min-width="145"
              ><template #default="{ row }">
                <span class="status-label" :class="`status-${row.initialStatus}`"
                  ><span class="status-dot" />{{ statusLabel(row.initialStatus) }}</span
                ><small v-if="row.executionAction === 1">{{ tr('inherited') }}</small>
              </template></el-table-column
            >
            <el-table-column :label="tr('reason')" min-width="240"
              ><template #default="{ row }">
                <template v-if="row.initialStatus !== 2">
                  <div class="phase-label">
                    {{ stageLabel(row) }}
                  </div>
                  <div class="reason-preview">
                    {{
                      row.initialErrorMessage || (row.initialStatus === 3 ? tr('emptyError') : '-')
                    }}
                  </div>
                  <details
                    v-if="row.initialErrorCode || row.initialErrorMessage"
                    class="technical-detail"
                  >
                    <summary>{{ tr('technical') }}</summary>
                    <pre>{{ row.initialErrorCode }} {{ row.initialErrorMessage }}</pre>
                  </details> </template
                ><span v-else class="muted">-</span>
              </template></el-table-column
            >
            <el-table-column :label="tr('resendColumn')" width="174" fixed="right">
              <template #default="{ row }">
                <template v-if="needsResend(row)">
                  <el-button
                    link
                    type="primary"
                    :disabled="!!row.retryBlock"
                    :title="
                      row.retryBlock
                        ? tr(blocks[row.retryBlock] || 'unavailable')
                        : tr('reuseOriginal')
                    "
                    @click="confirmResend([row])"
                    >{{ resendAction(row) }}</el-button
                  >
                  <small v-if="latestResend(row)"
                    >{{
                      t('reportDelivery.attemptNumber', {
                        count: manualAttempts(row).length
                      })
                    }}
                    · {{ resendStatus(latestResend(row)?.status ?? 4) }}</small
                  >
                </template>
                <span v-else class="muted">{{ tr('noResendNeeded') }}</span>
              </template>
            </el-table-column>
          </el-table>
          <div class="recipient-footer">
            <span>{{
              detail.legacy
                ? tr('legacy')
                : t('reportDelivery.recipientCount', {
                    count: filtered.length,
                    total: rows.length
                  })
            }}</span>
            <span v-if="!detail.legacy">{{ tr('accepted') }}</span>
          </div>
          <section v-if="resendRecords.length" class="resend-section">
            <div class="section-title">
              <h3>{{ tr('resendHistory') }}</h3>
              <span class="muted"
                >{{
                  t('reportDelivery.resendRecordCount', {
                    count: resendRecords.length
                  })
                }}
                · {{ tr('initialUnchanged') }}</span
              >
            </div>
            <el-table :data="resendRecords" row-key="id" class="recipient-table resend-table">
              <el-table-column :label="tr('recipientChannel')" min-width="250">
                <template #default="{ row }">
                  <div>{{ row.recipientName }}</div>
                  <small>{{ tr(channels[row.channel]) }} · {{ row.recipientAddress }}</small>
                </template>
              </el-table-column>
              <el-table-column :label="tr('resendCount')" width="100"
                ><template #default="{ row }">{{
                  t('reportDelivery.attemptNumber', { count: row.resendNo })
                }}</template></el-table-column
              >
              <el-table-column :label="tr('resendResult')" min-width="140">
                <template #default="{ row }"
                  ><span class="status-label" :class="`status-${row.status}`"
                    ><span class="status-dot" />{{ statusLabel(row.status) }}</span
                  ></template
                >
              </el-table-column>
              <el-table-column :label="tr('response')" min-width="270">
                <template #default="{ row }">
                  <div class="reason-preview">{{ resendMessage(row) }}</div>
                  <details v-if="row.errorCode || row.errorMessage" class="technical-detail">
                    <summary>{{ tr('technical') }}</summary>
                    <pre>{{ resendMessage(row) }}</pre>
                  </details>
                </template>
              </el-table-column>
              <el-table-column :label="tr('artifactReference')" min-width="190">
                <template #default="{ row }"
                  ><span class="artifact-reference">{{ row.artifactId || '-' }}</span></template
                >
              </el-table-column>
            </el-table>
          </section>
        </template>
        <details class="technical-section">
          <summary>{{ tr('raw') }}</summary>
          <pre>{{ detail.log || '-' }}</pre>
        </details>
      </template>
    </div>
    <template #footer
      ><div class="delivery-footer">
        <span>{{ tr('logId') }}: {{ detail?.instanceId || '-' }}</span>
        <div>
          <el-button @click="refresh()">{{ tr('refresh') }}</el-button
          ><el-button type="primary" @click="jobLogDetailVisible = false">{{
            tr('close')
          }}</el-button>
        </div>
      </div></template
    >
  </el-drawer>
  <el-dialog
    v-model="confirmVisible"
    :title="tr('resend')"
    class="report-resend-dialog"
    width="min(640px, 95vw)"
    :close-on-click-modal="false"
  >
    <p class="resend-description">
      {{ t('reportDelivery.originalDescription', { count: selectedRows.length }) }}
    </p>
    <div class="confirmation">
      <dl v-for="row in originalReports" :key="row.artifactId || row.id" class="retry-source">
        <dt>{{ tr('reportTitle') }}</dt>
        <dd>{{ row.title || '-' }}</dd>
        <dt>{{ tr('generated') }}</dt>
        <dd>{{ date(row.generatedTime) }}</dd>
        <dt>{{ tr('filter') }}</dt>
        <dd>{{ filterDescription(row.filter) }}</dd>
        <dt>{{ tr('originalFiles') }}</dt>
        <dd>
          <div class="artifact-reference">{{ row.artifactId || '-' }}</div>
          <div v-for="file in row.files" :key="file" class="muted">
            {{ file.split(' / SHA-256:')[0] }}
          </div>
        </dd>
      </dl>
      <el-table :data="selectedRows" row-key="id" class="recipient-table confirmation-table">
        <el-table-column prop="recipientName" :label="tr('name')" min-width="140" />
        <el-table-column prop="recipientAddress" :label="tr('address')" min-width="190">
          <template #default="{ row }">
            <RecipientAddress v-bind="row" />
          </template>
        </el-table-column>
        <el-table-column :label="tr('channel')" width="110"
          ><template #default="{ row }">{{ tr(channels[row.channel]) }}</template></el-table-column
        >
      </el-table>
    </div>
    <div v-if="needsRisk" class="retry-warning">{{ tr('riskWarning') }}</div>
    <el-checkbox v-if="needsRisk" class="risk-confirmation" v-model="confirmedRisk">{{
      tr('riskAcknowledgement')
    }}</el-checkbox>
    <template #footer>
      <el-button :disabled="submitting" @click="confirmVisible = false">{{
        tr('cancel')
      }}</el-button>
      <el-button
        type="primary"
        :loading="submitting"
        :disabled="needsRisk && !confirmedRisk"
        @click="submit"
        >{{ tr('confirm') }}</el-button
      >
    </template>
  </el-dialog>
</template>

<style scoped lang="less">
.delivery-heading {
  display: flex;
  align-items: center;
  gap: 16px;
}
.delivery-heading h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}
.execution-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 13px;
  background: #f5f6f7;
}
.execution-badge.status-3 {
  background: #fff0ee;
}
.execution-badge.status-2 {
  background: #edf8ee;
}
.delivery {
  min-height: 200px;
  color: #1f2329;
  font-size: 13px;
}
h3,
h4,
p {
  margin: 0;
}
h3 {
  font-size: 14px;
  font-weight: 600;
}
.task-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 24px;
  padding: 12px 0 16px;
  border-bottom: 1px solid #dee0e3;
}
.task-identity {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}
.task-identity h3 {
  font-size: 16px;
  overflow-wrap: anywhere;
}
.report-icon {
  width: 32px;
  height: 32px;
  flex: none;
  color: #3370ff;
}
.task-meta dl {
  display: flex;
  gap: 24px;
  margin: 0;
  flex: none;
}
dt {
  color: #646a73;
  font-size: 12px;
  margin-bottom: 6px;
}
dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}
.result-section {
  padding-top: 16px;
}
.section-title {
  display: flex;
  align-items: center;
  gap: 16px;
}
.summary {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  padding: 16px 0;
}
.metric {
  padding-left: 24px;
  border-left: 1px solid #dee0e3;
}
.metric:first-child {
  padding-left: 0;
  border: 0;
}
.metric-label {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #646a73;
  font-size: 12px;
}
.metric-value {
  margin-top: 6px;
  font-size: 26px;
  line-height: 1.3;
  font-weight: 600;
}
.status-2 {
  color: #2ca13a;
}
.status-3 {
  color: #d83931;
}
.status-4 {
  color: #a46614;
}
.status-5 {
  color: #8f959e;
}
.status-0,
.status-1 {
  color: #3370ff;
}
.status-dot {
  display: inline-block;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: currentColor;
  flex: none;
}
.status-label {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}
.result-notice {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 14px;
  background: #f5f7fa;
  border: 1px solid #dee4ef;
  border-radius: 4px;
  line-height: 1.6;
}
.result-notice.has-warning {
  background: #fff3f2;
  border-color: #f8d9d5;
}
.result-notice.is-success {
  background: #f1f9f1;
  border-color: #d2ebd4;
}
.notice-icon {
  width: 16px;
  height: 16px;
  flex: none;
  margin-top: 2px;
}
.result-notice.is-success .notice-icon,
.delivery-toast .notice-icon {
  color: #2ca13a;
}
.delivery-toast {
  position: absolute;
  z-index: 10;
  top: 64px;
  right: 24px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  max-width: calc(100% - 48px);
  padding: 10px 16px;
  background: white;
  border: 1px solid #d5e8d8;
  border-radius: 6px;
  box-shadow: 0 4px 16px #1f23291a;
  font-size: 13px;
  line-height: 1.6;
}
.recipient-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
  margin: 20px 0 12px;
}
.recipient-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.recipient-actions :deep(.ed-button) {
  margin-left: 0;
}
.action-icon {
  width: 14px;
  height: 14px;
  margin-right: 6px;
  fill: currentColor;
}
.snapshot-notice {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 12px;
  padding: 10px 12px;
  border: 1px solid #dee0e3;
  border-radius: 4px;
  background: #f5f6f7;
  color: #646a73;
  font-size: 13px;
  line-height: 1.6;
}
.recipient-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 12px;
}
.result-tabs {
  display: flex;
  gap: 2px;
  flex-wrap: wrap;
}
.result-tab {
  border: 0;
  background: transparent;
  border-radius: 4px;
  padding: 7px 10px;
  font: inherit;
  color: #646a73;
  cursor: pointer;
}
.result-tab span {
  margin-left: 4px;
  font-variant-numeric: tabular-nums;
}
.result-tab.active {
  background: #edf3ff;
  color: #3370ff;
  font-weight: 500;
}
.result-tab.tab-3.active {
  background: #fff0ee;
  color: #d83931;
}
.result-tab.tab-4.active {
  background: #fff6e7;
  color: #a46614;
}
.result-tab:focus-visible {
  outline: 2px solid #3370ff;
  outline-offset: 2px;
}
.filter-controls {
  display: flex;
  gap: 12px;
}
.channel-select {
  width: 140px;
}
.recipient-search {
  width: 240px;
}
.search-icon {
  width: 16px;
  height: 16px;
}
.recipient-table {
  width: 100%;
  font-size: 13px;
}
.recipient-table :deep(th.ed-table__cell) {
  background: #f5f6f7;
  font-size: 12px;
}
.recipient-table :deep(td.ed-table__cell) {
  vertical-align: top;
  padding: 12px 0;
}
.recipient-table :deep(.cell) {
  overflow-wrap: anywhere;
  line-height: 1.65;
}
.channel-name {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  white-space: nowrap;
}
.channel-name svg {
  width: 16px;
  height: 16px;
  flex: none;
}
.phase-label,
.current-result {
  font-size: 12px;
  margin-bottom: 4px;
}
.phase-label {
  color: #646a73;
}
.reason-preview {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
.technical-detail {
  margin-top: 6px;
}
.technical-detail summary {
  color: #646a73;
  font-size: 12px;
  cursor: pointer;
}
pre {
  padding: 12px;
  margin: 8px 0 0;
  background: #f5f6f7;
  border-radius: 4px;
  font: 12px/1.7 Menlo, monospace;
  white-space: pre-wrap;
  overflow-wrap: anywhere;
}
small {
  display: block;
  color: #8f959e;
  font-size: 12px;
  line-height: 1.6;
  margin-top: 4px;
}
.muted {
  color: #646a73;
  font-size: 12px;
}
.recipient-footer {
  display: flex;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  color: #8f959e;
  font-size: 12px;
}
.technical-section {
  margin: 24px 0;
  padding-top: 16px;
  border-top: 1px solid #dee0e3;
}
.technical-section > summary {
  cursor: pointer;
  font-weight: 500;
}
.resend-section {
  margin-top: 20px;
}
.resend-section .section-title {
  margin-bottom: 12px;
}
.artifact-reference {
  font: 12px/1.7 Menlo, Consolas, monospace;
  overflow-wrap: anywhere;
}
.delivery-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}
.delivery-footer > div {
  display: flex;
  flex: none;
  gap: 8px;
}
.delivery-footer > div :deep(.ed-button) {
  margin-left: 0;
}
.delivery-footer > span {
  color: #8f959e;
  font-size: 12px;
  overflow-wrap: anywhere;
}
.resource-caption {
  margin-top: 4px;
  color: #646a73;
  font-size: 12px;
}
.delivery-empty {
  padding: 28px 12px;
  line-height: 1.7;
}
.delivery-empty strong {
  display: block;
  color: #4e5663;
  margin-bottom: 6px;
}
.retry-warning {
  margin-top: 16px;
  padding: 10px 12px;
  background: #fff6e7;
  border: 1px solid #f0d6a8;
  border-radius: 4px;
  color: #9b6014;
  line-height: 1.6;
}
.confirmation .retry-source {
  padding: 14px 0;
  border-top: 1px solid #dee0e3;
  border-bottom: 1px solid #dee0e3;
}
.resend-description {
  line-height: 1.7;
}
.confirmation {
  max-height: 50vh;
  overflow: auto;
}
.confirmation section {
  padding: 16px 0;
  border-bottom: 1px solid #dee0e3;
}
.confirmation-target {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  overflow-wrap: anywhere;
}
.confirmation-target span {
  color: #646a73;
}
.retry-source {
  display: grid;
  grid-template-columns: 110px minmax(0, 1fr);
  gap: 10px 12px;
  margin: 16px 0 0;
}
.retry-source dt,
.retry-source dd {
  margin: 0;
  font-size: 13px;
  overflow-wrap: anywhere;
}
.risk-confirmation {
  margin-top: 16px;
  height: auto;
}
.risk-confirmation :deep(.ed-checkbox__label) {
  white-space: normal;
  line-height: 1.6;
}
@media (max-width: 900px) {
  .task-meta {
    flex-wrap: wrap;
  }
  .task-meta dl {
    flex-wrap: wrap;
  }
  .section-title {
    flex-wrap: wrap;
    gap: 6px;
  }
}
@media (max-width: 600px) {
  .summary {
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 16px 0;
  }
  .metric {
    padding-left: 12px;
  }
  .metric:nth-child(4) {
    border: 0;
    padding-left: 0;
  }
  .filter-controls {
    width: 100%;
  }
  .channel-select {
    width: 130px;
    flex: none;
  }
  .recipient-search {
    flex: 1;
    min-width: 0;
  }
}
</style>
