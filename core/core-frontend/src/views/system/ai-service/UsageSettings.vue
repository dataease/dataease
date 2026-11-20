<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import TaskSchedulingSettings from './TaskSchedulingSettings.vue'
import RecoverySettings from './RecoverySettings.vue'
import RetentionSettings from './RetentionSettings.vue'
import LengthLimitInput from './LengthLimitInput.vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { CaretBottom } from '@element-plus/icons-vue'
import { useI18n } from '@/hooks/web/useI18n'
import {
  getAiOrgs,
  getAiModels,
  getAiModel,
  saveAiOrgPolicies,
  saveAiSettings,
  type AiSettings,
  type AiOrg,
  type AiModel
} from '@/api/aiService'
const props = defineProps<{ settings: AiSettings; models: AiModel[] }>()
const emit = defineEmits<{ saved: [] }>()
const { t } = useI18n()
const tr = (key: string, args: Record<string, unknown> = {}) => t(`ai_service.${key}`, args)
const search = ref('')
const organizations = ref<AiOrg[]>([])
const available = ref(false)
const selected = reactive(new Map<string, AiOrg>())
const loading = ref(false)
const failed = ref(false)
const saving = ref(false)
const drawer = ref<'org' | 'batch' | 'data' | 'limits' | ''>('')
const targets = ref<AiOrg[]>([])
const showSelected = ref(false)
const mode = ref('')
const selectedModel = ref<string | null>(null)
const modelOptions = ref<AiModel[]>([])
const settingsDraft = ref<AiSettings>()
const formBaseline = ref('')
let generation = 0
const allSelected = computed(
  () => organizations.value.length > 0 && organizations.value.every(o => selected.has(o.id))
)
const someSelected = computed(
  () => organizations.value.some(o => selected.has(o.id)) && !allSelected.value
)
const hiddenCount = computed(
  () => [...selected.keys()].filter(id => !organizations.value.some(o => o.id === id)).length
)
const choices = computed(() => [
  ...new Map([...props.models, ...modelOptions.value].map(m => [m.id, m])).values()
])
const effective = computed(() => {
  const id =
    mode.value === 'SPECIFIED'
      ? selectedModel.value
      : mode.value === 'INHERIT'
      ? props.settings.defaultModelId
      : null
  return choices.value.find(m => m.id === id && m.enabled && m.validated)?.name
})
const formValue = () => JSON.stringify([mode.value, selectedModel.value, settingsDraft.value])
const limits = [
  { key: 'requestTimeoutSeconds', min: 5, max: 300 },
  { key: 'maxInputChars', min: 0, max: 2147483647 },
  { key: 'maxOutputTokens', min: 0, max: 2147483647 },
  { key: 'interactiveConcurrency', min: 1, max: 20 },
  { key: 'backgroundConcurrency', min: 1, max: 20 }
] as const
const validTaskScheduling = () => {
  const limits = settingsDraft.value?.taskScheduling
  return (
    !limits ||
    (Number.isInteger(limits.maxRunning) &&
      limits.maxRunning >= 1 &&
      limits.maxRunning <= 20 &&
      Number.isInteger(limits.maxQueued) &&
      limits.maxQueued >= 1 &&
      limits.maxQueued <= 10000 &&
      Number.isInteger(limits.maxQueuedPerUser) &&
      limits.maxQueuedPerUser >= 1 &&
      limits.maxQueuedPerUser <= 1000 &&
      Number.isInteger(limits.maxWaitSeconds) &&
      limits.maxWaitSeconds >= 1 &&
      limits.maxWaitSeconds <= 86400)
  )
}
const valid = computed(() => {
  if (drawer.value === 'org' || drawer.value === 'batch')
    return (
      Boolean(mode.value) &&
      (mode.value !== 'SPECIFIED' ||
        choices.value.some(
          model => model.id === selectedModel.value && model.enabled && model.validated
        ))
    )
  const s = settingsDraft.value
  return Boolean(
    s &&
      limits.every(l => Number.isInteger(s[l.key]) && s[l.key] >= l.min && s[l.key] <= l.max) &&
      s.backgroundConcurrency <= s.interactiveConcurrency &&
      validTaskScheduling() &&
      s.retention &&
      [s.retention.auditDays, s.retention.deliveryDays].every(
        days => Number.isInteger(days) && days >= 1 && days <= 3650
      ) &&
      s.recovery &&
      Number.isInteger(s.recovery.stepRetries) &&
      s.recovery.stepRetries >= 0 &&
      s.recovery.stepRetries <= 5 &&
      Number.isInteger(s.recovery.attemptRetries) &&
      s.recovery.attemptRetries >= 0 &&
      s.recovery.attemptRetries <= 5 &&
      Number.isInteger(s.recovery.initialDelayMillis) &&
      s.recovery.initialDelayMillis >= 100 &&
      s.recovery.initialDelayMillis <= 10000
  )
})
const load = async () => {
  if (!props.settings.organizationPolicyAvailable) return
  const current = ++generation
  loading.value = true
  failed.value = false
  try {
    const result = await getAiOrgs(search.value)
    if (current !== generation) return
    if (!result.available) throw new Error('AI capabilities changed')
    organizations.value = result.items
    available.value = result.available
  } catch {
    if (current === generation) failed.value = true
  } finally {
    if (current === generation) loading.value = false
  }
}
const toggle = (org: AiOrg, checked: boolean) => {
  if (checked && !selected.has(org.id) && selected.size >= 2000) {
    ElMessage.warning(tr('selection_limit'))
    return
  }
  if (checked) selected.set(org.id, org)
  else selected.delete(org.id)
}
const toggleAll = (checked: boolean) => {
  const combined = new Set([...selected.keys(), ...organizations.value.map(org => org.id)])
  if (checked && combined.size > 2000) {
    ElMessage.warning(tr('selection_limit'))
    return
  }
  organizations.value.forEach(org => toggle(org, checked))
}
const searchModels = async (value: string) => {
  modelOptions.value = (await getAiModels(value, 1, 50)).items
}
const openOrg = async (org?: AiOrg) => {
  showSelected.value = false
  targets.value = org ? [org] : [...selected.values()]
  if (!targets.value.length) return
  mode.value = org?.mode ?? ''
  selectedModel.value = org?.modelId ?? null
  settingsDraft.value = undefined
  if (selectedModel.value && !choices.value.some(m => m.id === selectedModel.value))
    modelOptions.value.push(await getAiModel(selectedModel.value))
  formBaseline.value = formValue()
  drawer.value = org ? 'org' : 'batch'
}
const openSettings = (kind: 'data' | 'limits') => {
  settingsDraft.value = {
    ...props.settings,
    taskScheduling: props.settings.taskScheduling
      ? { ...props.settings.taskScheduling }
      : undefined,
    retention: { ...(props.settings.retention ?? { auditDays: 30, deliveryDays: 7 }) },
    recovery: {
      ...(props.settings.recovery ?? {
        stepRetries: 2,
        attemptRetries: 3,
        initialDelayMillis: 1000
      })
    }
  }
  mode.value = ''
  selectedModel.value = null
  formBaseline.value = formValue()
  drawer.value = kind
}
const close = async (done?: () => void) => {
  if (saving.value) return
  if (formValue() !== formBaseline.value) {
    try {
      await ElMessageBox.confirm(tr('discard_text'), tr('discard_title'), {
        confirmButtonText: tr('discard'),
        cancelButtonText: tr('keep_editing')
      })
    } catch {
      return
    }
  }
  drawer.value = ''
  done?.()
}
const save = async () => {
  if (!valid.value || saving.value) return
  saving.value = true
  try {
    if (drawer.value === 'org' || drawer.value === 'batch') {
      await saveAiOrgPolicies(targets.value, mode.value, selectedModel.value)
      selected.clear()
      await load()
      ElMessage.success(tr('save_success'))
    } else if (settingsDraft.value) {
      const saved = await saveAiSettings(settingsDraft.value)
      if (saved.runtimeState !== 'ACTIVE') ElMessage.warning(tr('runtime_pending'))
      else ElMessage.success(tr('save_success'))
    }
    drawer.value = ''
    emit('saved')
  } finally {
    saving.value = false
  }
}
watch(
  () => props.settings.organizationPolicyAvailable,
  enabled => {
    generation++
    organizations.value = []
    selected.clear()
    targets.value = []
    available.value = false
    loading.value = false
    failed.value = false
    if (drawer.value === 'org' || drawer.value === 'batch') drawer.value = ''
    if (enabled) load()
  },
  { immediate: true }
)
</script>
<template>
  <div class="ai-usage">
    <section v-if="settings.organizationPolicyAvailable" v-loading="loading" class="ai-org-card">
      <h2>{{ tr('org_title') }}</h2>
      <div class="ai-org-toolbar">
        <div>
          <el-button :disabled="!selected.size || !available" @click="openOrg()">{{
            tr('batch')
          }}</el-button
          ><span class="ai-secondary">{{
            selected.size
              ? tr('selected', { count: selected.size })
              : tr('org_count', { count: organizations.length })
          }}</span
          ><span v-if="hiddenCount" class="ai-secondary">{{
            tr('hidden_selected', { count: hiddenCount })
          }}</span
          ><el-button v-if="selected.size" link type="primary" @click="selected.clear()">{{
            tr('clear_selection')
          }}</el-button>
        </div>
        <el-input
          v-model="search"
          clearable
          :placeholder="tr('org_search')"
          :aria-label="tr('org_search')"
          @input="load"
        />
      </div>
      <div v-if="failed" role="alert">
        {{ tr('load_failed') }}<el-button @click="load">{{ tr('refresh') }}</el-button>
      </div>
      <el-table v-else :data="organizations" height="100%" class="ai-org-table">
        <el-table-column width="44"
          ><template #header
            ><el-checkbox
              :model-value="allSelected"
              :indeterminate="someSelected"
              :aria-label="tr('select_all')"
              @change="toggleAll" /></template
          ><template #default="{ row }"
            ><el-checkbox
              :model-value="selected.has(row.id)"
              :aria-label="tr('select_org', { name: row.name })"
              @change="value => toggle(row, value)" /></template
        ></el-table-column>
        <el-table-column :label="tr('org_name')" min-width="150"
          ><template #default="{ row }"
            ><strong>{{ row.name }}</strong>
            <div class="ai-secondary">{{ row.parentName }}</div></template
          ></el-table-column
        >
        <el-table-column :label="tr('policy')" min-width="190"
          ><template #default="{ row }">{{ tr(row.mode) }}</template></el-table-column
        >
        <el-table-column :label="tr('effective_model')" min-width="220"
          ><template #default="{ row }"
            ><template v-if="row.effectiveModel">{{ row.effectiveModel }}</template
            ><template v-else
              ><span class="ai-secondary">● {{ tr('unavailable') }}</span>
              <div class="ai-secondary">
                {{ tr(row.unavailableReason || 'model_unavailable') }}
              </div></template
            ></template
          ></el-table-column
        >
        <el-table-column :label="tr('operations')" min-width="100"
          ><template #default="{ row }"
            ><el-button link type="primary" @click="openOrg(row)">{{
              tr('configure')
            }}</el-button></template
          ></el-table-column
        >
      </el-table>
    </section>
    <div class="ai-settings-cards">
      <section>
        <div>
          <h2>{{ tr('data_title') }}</h2>
          <p>{{ tr(settings.allowSamples ? 'samples_summary' : 'metadata_summary') }}</p>
        </div>
        <el-button
          link
          type="primary"
          :aria-label="tr('data_title')"
          @click="openSettings('data')"
          >{{ tr('configure_settings') }}</el-button
        >
      </section>
      <section>
        <div>
          <h2>{{ tr('limits') }}</h2>
          <p>
            {{
              tr('limits_summary', {
                timeout: settings.requestTimeoutSeconds,
                interactive: settings.interactiveConcurrency,
                background: settings.backgroundConcurrency
              })
            }}
          </p>
        </div>
        <el-button link type="primary" :aria-label="tr('limits')" @click="openSettings('limits')">{{
          tr('configure_settings')
        }}</el-button>
      </section>
    </div>
    <el-drawer
      :model-value="Boolean(drawer)"
      size="600px"
      :title="
        tr(
          drawer === 'org'
            ? 'org_title'
            : drawer === 'batch'
            ? 'batch_title'
            : drawer === 'data'
            ? 'data_title'
            : 'limits'
        )
      "
      :before-close="close"
      :close-on-click-modal="false"
      :close-on-press-escape="!saving"
      class="ai-usage-drawer"
      :class="{ 'ai-org-drawer': drawer === 'org' || drawer === 'batch' }"
    >
      <el-form
        v-if="drawer === 'org' || drawer === 'batch'"
        label-position="top"
        :disabled="saving"
        class="ai-org-form"
      >
        <el-form-item v-if="drawer === 'org'" :label="tr('organization')" class="ai-org-identity"
          ><strong>{{ targets[0]?.name }}</strong>
          <p v-if="targets[0]?.parentName" class="ai-secondary">
            {{ tr('parent', { name: targets[0].parentName }) }}
          </p></el-form-item
        >
        <section v-else class="ai-batch-scope">
          <div class="ai-batch-scope-heading">
            <strong>{{ tr('selected', { count: targets.length }) }}</strong>
            <el-button
              link
              type="primary"
              :aria-expanded="showSelected"
              aria-controls="ai-selected-org-list"
              @click="showSelected = !showSelected"
            >
              {{ tr(showSelected ? 'hide_list' : 'view_list') }}
              <el-icon :class="{ expanded: showSelected }" aria-hidden="true"
                ><CaretBottom
              /></el-icon>
            </el-button>
          </div>
          <p class="ai-secondary ai-current-policy">
            {{
              new Set(targets.map(o => o.mode + o.modelId)).size > 1
                ? tr('mixed')
                : tr('current_policy', { policy: tr(targets[0]?.mode || 'INHERIT') })
            }}
          </p>
          <ul v-if="showSelected" id="ai-selected-org-list" class="ai-selected-list">
            <li v-for="org in targets" :key="org.id">
              <span>{{ org.name }}</span
              ><small
                >{{ tr(org.mode)
                }}<template v-if="org.mode === 'SPECIFIED'">
                  · {{ org.effectiveModel || tr('unavailable') }}</template
                ></small
              >
            </li>
          </ul>
          <p class="ai-secondary ai-batch-scope-note">{{ tr('batch_hint') }}</p>
        </section>
        <el-form-item :label="tr('policy')" required
          ><el-radio-group v-model="mode" class="ai-policy-options"
            ><el-radio
              v-for="policy in ['INHERIT', 'SPECIFIED', 'DISABLED']"
              :key="policy"
              :value="policy"
              border
              >{{ tr(policy) }}</el-radio
            ></el-radio-group
          ></el-form-item
        >
        <el-form-item v-if="mode === 'SPECIFIED'" :label="tr('SPECIFIED')" required
          ><el-select
            v-model="selectedModel"
            filterable
            remote
            :remote-method="searchModels"
            :placeholder="tr('choose_model')"
            ><el-option
              v-for="model in choices"
              :key="model.id"
              :value="model.id"
              :label="model.name"
              :disabled="!model.enabled || !model.validated" /></el-select
        ></el-form-item>
        <div
          v-if="mode === 'INHERIT' || (mode === 'SPECIFIED' && selectedModel)"
          class="ai-policy-preview"
        >
          <span>{{ tr('effective_model') }}</span
          ><strong>{{ effective || tr('unavailable') }}</strong>
        </div>
        <p v-if="mode === 'DISABLED'" class="ai-policy-notice">
          {{ tr(drawer === 'batch' ? 'batch_disabled_hint' : 'org_disabled_hint') }}
        </p>
        <details class="ai-rules">
          <summary>
            {{ tr('rules') }}<el-icon aria-hidden="true"><CaretBottom /></el-icon>
          </summary>
          <ul>
            <li v-for="rule in ['rules_identity', 'rules_task', 'rules_policy']" :key="rule">
              {{ tr(rule) }}
            </li>
          </ul>
        </details>
      </el-form>
      <div v-else-if="drawer === 'data' && settingsDraft">
        <p class="ai-data-hint">{{ tr('data_hint') }}</p>
        <div class="ai-data-row">
          <div>
            <strong>{{ tr('metadata') }}</strong>
            <p class="ai-secondary">{{ tr('metadata_hint') }}</p>
          </div>
          <span class="ai-secondary">{{ tr('fixed') }}</span>
        </div>
        <div class="ai-data-row">
          <div>
            <strong>{{ tr('samples') }}</strong>
            <p class="ai-secondary">{{ tr('samples_hint') }}</p>
          </div>
          <el-switch
            v-model="settingsDraft.allowSamples"
            :disabled="saving"
            :aria-label="tr('samples')"
          />
        </div>
      </div>
      <el-form
        v-else-if="settingsDraft"
        label-position="top"
        :disabled="saving"
        class="ai-limits-form"
        ><p class="ai-secondary">{{ tr('length_hint') }}</p>
        <el-form-item v-for="limit in limits" :key="limit.key" :label="tr(limit.key)"
          ><LengthLimitInput
            v-if="limit.key === 'maxInputChars' || limit.key === 'maxOutputTokens'"
            v-model="settingsDraft[limit.key]"
            :label="tr(limit.key)" />
          <el-input-number
            v-else
            v-model="settingsDraft[limit.key]"
            :min="limit.min"
            :max="limit.max"
            :precision="0"
            controls-position="right"
        /></el-form-item>
        <p class="ai-secondary">{{ tr('background_hint') }}</p>
        <TaskSchedulingSettings
          v-if="settingsDraft.taskScheduling"
          v-model="settingsDraft.taskScheduling"
        />
        <RecoverySettings v-if="settingsDraft.recovery" v-model="settingsDraft.recovery" />
        <RetentionSettings v-if="settingsDraft.retention" v-model="settingsDraft.retention" />
      </el-form>
      <template #footer
        ><el-button :disabled="saving" @click="close()">{{ tr('cancel') }}</el-button
        ><el-button
          type="primary"
          :disabled="!valid || (drawer === 'org' && formValue() === formBaseline)"
          :loading="saving"
          @click="save"
          >{{
            drawer === 'batch' ? tr('apply_orgs', { count: targets.length }) : tr('save')
          }}</el-button
        ></template
      >
    </el-drawer>
  </div>
</template>
<style scoped lang="less">
.ai-usage {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
h2 {
  font-size: 16px;
  line-height: 24px;
  margin: 0 0 20px;
  font-weight: 600;
}
.ai-org-card {
  flex: 1;
  min-height: 240px;
  padding: 24px;
  background: var(--ed-bg-color);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}
.ai-org-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
  > div {
    display: flex;
    align-items: center;
    gap: 12px;
    flex-wrap: wrap;
  }
  .ed-input {
    width: 240px;
    flex: none;
  }
}
.ai-secondary {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
}
.ai-org-table {
  flex: 1;
  :deep(th.ed-table__cell) {
    height: 44px;
    background: var(--ed-fill-color-light);
    font-weight: 400;
  }
  :deep(td.ed-table__cell) {
    padding: 16px 0;
  }
  strong {
    font-weight: 500;
  }
}
.ai-settings-cards {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  > section {
    background: var(--ed-bg-color);
    border-radius: 12px;
    padding: 20px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 12px;
    h2 {
      font-size: 14px;
      margin: 0 0 4px;
    }
    p {
      margin: 0;
      font-size: 12px;
      line-height: 20px;
      color: var(--ed-text-color-regular);
    }
  }
}
.ai-org-form {
  .ed-form-item {
    margin-bottom: 20px;
  }
  :deep(.ed-form-item__label) {
    height: auto;
    margin-bottom: 8px;
    line-height: 22px;
  }
}
.ai-org-identity {
  :deep(.ed-form-item__content) {
    display: block;
    line-height: 24px;
  }
  strong {
    font-weight: 600;
  }
  p {
    margin: 4px 0 0;
  }
}
.ai-batch-scope {
  border: 1px solid var(--ai-org-border);
  border-radius: 6px;
  background: var(--ai-org-surface);
  padding: 16px;
  margin-bottom: 24px;
  p {
    margin: 0;
  }
  .ai-batch-scope-note {
    margin-top: 10px;
    color: var(--ai-org-muted);
  }
}
.ai-batch-scope-heading {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 6px;
  min-height: 28px;
  strong {
    font-size: 14px;
    font-weight: 500;
  }
  .ed-button {
    min-width: 0;
    font-size: 12px;
    .ed-icon {
      margin-left: 6px;
    }
  }
  .expanded {
    transform: rotate(180deg);
  }
}
.ai-selected-list {
  max-height: 240px;
  overflow: auto;
  list-style: none;
  padding: 0;
  margin: 12px 0 0;
  border-top: 1px solid var(--ai-org-border);
  border-bottom: 1px solid var(--ai-org-border);
  li {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    padding: 8px 0;
    font-size: 13px;
    line-height: 20px;
  }
  small {
    color: var(--ed-text-color-secondary);
    font-size: 12px;
    text-align: right;
  }
}
.ai-policy-options {
  display: flex;
  flex-direction: column;
  width: 100%;
  gap: 12px;
  .ed-radio {
    margin: 0;
    width: 100%;
    // 覆盖产品全局 .ed-radio 的 22px !important，仅作用于策略卡片。
    height: 52px !important;
    padding: 14px 16px;
    border-radius: 6px;
    border-color: var(--ai-org-border);
    &.is-checked {
      background: var(--ed-color-primary-light-9);
      border-color: var(--ed-color-primary);
    }
    :deep(.ed-radio__label) {
      padding-left: 10px;
      font-weight: 500;
      color: var(--ed-text-color-primary);
    }
  }
}
.ed-select {
  width: 100%;
}
.ai-rules {
  margin-top: 16px;
  color: var(--ai-org-muted);
  font-size: 12px;
  summary {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 24px;
    cursor: pointer;
    list-style: none;
    &::-webkit-details-marker {
      display: none;
    }
  }
  &[open] .ed-icon {
    transform: rotate(180deg);
  }
  ul {
    margin: 8px 0 0;
    padding-left: 18px;
    line-height: 22px;
    list-style: disc;
  }
}
.ai-policy-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 14px;
  margin-bottom: 16px;
  border-radius: 6px;
  background: var(--ai-org-surface);
  font-size: 13px;
  line-height: 20px;
  span {
    color: var(--ai-org-muted);
  }
  strong {
    font-weight: 500;
    overflow-wrap: anywhere;
    text-align: right;
  }
}
.ai-policy-notice {
  margin: 0 0 16px;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 22px;
}
.ai-data-hint {
  margin: 0 0 20px;
  color: var(--ed-text-color-regular);
  font-size: 14px;
}
.ai-data-row {
  padding: 20px 0;
  border-bottom: 1px solid var(--ed-border-color-lighter);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  strong {
    font-size: 14px;
    font-weight: 500;
  }
  p {
    margin: 4px 0 0;
  }
}
.ai-limits-form {
  .ed-form-item {
    margin-bottom: 24px;
  }
  .ed-input-number {
    width: 100%;
  }
}
@media (max-width: 1100px) {
  .ai-settings-cards {
    grid-template-columns: 1fr;
  }
}
</style>

<style lang="less">
.ai-usage-drawer.ai-org-drawer {
  --ai-org-surface: #f5f6f7;
  --ai-org-border: #dee0e3;
  --ai-org-muted: #646a73;

  .ed-drawer__body {
    padding: 20px 24px;
  }
  .ed-drawer__footer .ed-button {
    min-width: 0;
    height: 32px;
    padding: 4px 14px;
    border-radius: 6px;
    font-weight: 400;
  }
}

.dark .ai-usage-drawer.ai-org-drawer {
  --ai-org-surface: var(--ed-fill-color-light);
  --ai-org-border: var(--ed-border-color);
  --ai-org-muted: var(--ed-text-color-secondary);
}
</style>
