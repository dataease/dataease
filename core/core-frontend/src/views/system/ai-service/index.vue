<script setup lang="ts">
import { computed, h, onMounted, ref, watch, type ComponentPublicInstance, type VNode } from 'vue'
import { useResizeObserver } from '@vueuse/core'
import { ArrowDown, CaretBottom, Operation } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import { useUserStoreWithOut } from '@/store/modules/user'
import ModelEditor from './ModelEditor.vue'
import UsageSettings from './UsageSettings.vue'
import {
  aiFailureKey,
  getAiSettings,
  getAiModels,
  getAiModel,
  saveAiSettings,
  getAiSettingsImpact,
  getAiImpact,
  setAiModelEnabled,
  deleteAiModel,
  testSavedAiModel,
  type AiSettings,
  type AiModel,
  type AiImpact
} from '@/api/aiService'

const { t } = useI18n()
const tr = (key: string, args: Record<string, unknown> = {}) => t(`ai_service.${key}`, args)
const user = useUserStoreWithOut()
const tab = ref('models')
const settings = ref<AiSettings>()
const models = ref<AiModel[]>([])
const options = ref<AiModel[]>([])
const total = ref(0)
const search = ref('')
const page = ref(1)
const size = ref(10)
const loading = ref(false)
const failed = ref(false)
const busy = ref(false)
const editor = ref(false)
const editing = ref<AiModel>()
const testing = ref(new Set<string>())
const actionsRef = ref<HTMLElement>()
const actionsWidth = ref<number>()
const setActionsRef = (element: Element | ComponentPublicInstance | null) => {
  actionsRef.value = element instanceof HTMLElement ? element : undefined
}
useResizeObserver(actionsRef, entries => {
  const entry = entries[0]
  if (entry) actionsWidth.value = Math.ceil(entry.contentRect.width + 48)
})
let queryGeneration = 0
let optionsGeneration = 0
const usable = computed(() => options.value.filter(model => model.enabled && model.validated))
const formatTime = (time: number | null) =>
  time ? new Date(time).toLocaleString() : tr('never_tested')
const loadOptions = async (query = '') => {
  const current = ++optionsGeneration
  const context = queryGeneration
  const result = await getAiModels(query, 1, 50)
  const list = result.items
  if (
    settings.value?.defaultModelId &&
    !list.some(model => model.id === settings.value?.defaultModelId)
  ) {
    list.push(await getAiModel(settings.value.defaultModelId))
  }
  if (current === optionsGeneration && context === queryGeneration) options.value = list
}
const load = async () => {
  const current = ++queryGeneration
  loading.value = true
  failed.value = false
  try {
    const [config, list] = await Promise.all([
      getAiSettings(),
      getAiModels(search.value, page.value, size.value)
    ])
    if (current !== queryGeneration) return
    if (typeof config.organizationPolicyAvailable !== 'boolean')
      throw new Error('AI capabilities unavailable')
    settings.value = config
    models.value = list.items
    total.value = list.total
    await loadOptions()
  } catch {
    if (current === queryGeneration) failed.value = true
  } finally {
    if (current === queryGeneration) loading.value = false
  }
}
const query = () => {
  page.value = 1
  return load()
}
const edit = (model?: AiModel) => {
  editing.value = model
  editor.value = true
}
const affected = (impact: AiImpact) =>
  [
    ...(impact.systemDefault ? [tr('default_model'), tr('system_scope')] : []),
    ...impact.organizations
  ].join('、')
const confirm = async (
  text: string | VNode,
  title: string,
  button: string,
  danger = false,
  structured = false,
  kind = 'disable'
) => {
  try {
    await ElMessageBox.confirm(text, title, {
      confirmButtonText: button,
      cancelButtonText: tr('cancel'),
      confirmButtonType: danger ? 'danger' : 'primary',
      type: structured ? undefined : 'warning',
      dangerouslyUseHTMLString: false,
      modalClass: structured ? 'ai-model-confirm-overlay' : undefined,
      customClass: structured
        ? `ai-impact-confirm ai-model-confirm ai-model-${kind}-confirm`
        : 'ai-impact-confirm'
    })
    return true
  } catch {
    return false
  }
}
const changeSettings = async (next: AiSettings, mode: 'default' | 'enabled') => {
  if (busy.value) return
  busy.value = true
  const clearDefault = mode === 'default' && !next.defaultModelId
  let submitted = false
  try {
    const needsConfirmation = mode === 'default' || !next.enabled
    const impact = needsConfirmation ? await getAiSettingsImpact(next) : undefined
    const name = options.value.find(m => m.id === next.defaultModelId)?.name ?? ''
    const title =
      mode === 'enabled' ? tr('off_title') : tr(clearDefault ? 'clear_title' : 'change_title')
    if ((mode === 'enabled' && !next.enabled) || mode === 'default') {
      if (
        !(await confirm(
          mode === 'default'
            ? h('div', { class: 'ai-disable-content ai-default-content' }, [
                clearDefault
                  ? h('p', tr('clear_text'))
                  : h('p', [
                      tr('change_default_prefix'),
                      h('strong', name),
                      tr('change_default_suffix')
                    ]),
                h(
                  'p',
                  impact?.organizationCount
                    ? tr('default_impact', {
                        count: impact.organizationCount,
                        names: impact.organizations.join('、')
                      })
                    : tr('default_impact_empty')
                ),
                h(
                  'p',
                  { class: 'ai-default-hint' },
                  tr(clearDefault ? 'no_fallback' : 'change_hint')
                )
              ])
            : h('div', { class: 'ai-disable-content' }, [
                h(
                  'p',
                  tr(
                    settings.value?.organizationPolicyAvailable ? 'off_text' : 'off_text_community'
                  )
                )
              ]),
          title,
          tr(
            mode === 'enabled' ? 'confirm_off' : clearDefault ? 'clear_confirm' : 'confirm_change'
          ),
          clearDefault || mode === 'enabled',
          true,
          mode === 'enabled' ? 'off' : 'default'
        ))
      )
        return
    }
    submitted = true
    const saved = await saveAiSettings(next, impact?.token)
    if (saved.runtimeState !== 'ACTIVE') ElMessage.warning(tr('runtime_pending'))
    else if (mode === 'default')
      ElMessage.success(tr(clearDefault ? 'default_cleared' : 'default_updated'))
    await load()
  } catch (error) {
    if (mode !== 'default') throw error
    // 业务错误及 HTTP 失败沿用统一拦截器；补齐无响应的网络失败，避免重复弹出。
    if (mode === 'default' && typeof error !== 'string') {
      const response = (error as { response?: unknown } | null)?.response
      if (response) return
      ElMessage.error(
        tr(
          submitted && !response
            ? 'default_result_unknown'
            : clearDefault
            ? 'default_clear_failed'
            : 'default_update_failed'
        )
      )
    }
  } finally {
    busy.value = false
  }
}
const toggleService = (value: boolean) =>
  settings.value && changeSettings({ ...settings.value, enabled: value }, 'enabled')
const selectDefault = (id: string) => {
  if (!settings.value || (id || null) === settings.value.defaultModelId) return
  return changeSettings({ ...settings.value, defaultModelId: id || null }, 'default')
}
const toggleModel = async (model: AiModel, enabled: boolean) => {
  if (busy.value) return
  busy.value = true
  try {
    let token: string | undefined
    if (!enabled) {
      const impact = await getAiImpact(model.id, 'DISABLE')
      token = impact.token
      const scopes = [
        ...(impact.systemDefault ? [tr('default_model')] : []),
        ...impact.organizations,
        ...(impact.systemDefault ? [tr('system_scope')] : [])
      ]
      if (
        !(await confirm(
          h('div', { class: 'ai-disable-content' }, [
            h('p', tr('disable_text')),
            ...(scopes.length
              ? [
                  h('p', { class: 'ai-impact-heading' }, tr('affected')),
                  h(
                    'ul',
                    { class: 'ai-impact-list' },
                    scopes.map(scope => h('li', scope))
                  )
                ]
              : [])
          ]),
          tr('disable_title', { name: model.name }),
          tr('confirm_disable'),
          true,
          true
        ))
      )
        return
    }
    await setAiModelEnabled(model, enabled, token)
    await load()
  } finally {
    busy.value = false
  }
}
const remove = async (model: AiModel) => {
  if (busy.value) return
  busy.value = true
  try {
    const impact = await getAiImpact(model.id, 'DELETE')
    if (
      impact.systemDefault ||
      model.organizationReferences ||
      model.historicalOrganizationReferences
    ) {
      await ElMessageBox.alert(
        tr(
          model.historicalOrganizationReferences ? 'historical_reference_text' : 'referenced_text',
          { name: model.name }
        ) +
          '\n' +
          affected(impact) +
          '\n' +
          tr('reference_hint'),
        tr('referenced_title'),
        { confirmButtonText: tr('understood'), customClass: 'ai-impact-confirm' }
      )
      return
    }
    if (
      !(await confirm(
        tr('delete_text'),
        tr('delete_title', { name: model.name }),
        tr('delete_model'),
        true
      ))
    )
      return
    await deleteAiModel(model)
    if (models.value.length === 1 && page.value > 1) page.value--
    await load()
  } finally {
    busy.value = false
  }
}
const test = async (model: AiModel) => {
  if (testing.value.has(model.id)) return
  testing.value.add(model.id)
  try {
    const result = await testSavedAiModel(model.id)
    if (result.basic && result.structured)
      ElMessage.success(tr('model_test_passed', { name: model.name }))
    else ElMessage.error(tr(aiFailureKey(result.errorCode)))
    await load()
  } finally {
    testing.value.delete(model.id)
  }
}
const more = (command: string, model: AiModel) => {
  if (command === 'delete') return remove(model)
  return selectDefault(model.systemDefault ? '' : model.id)
}
watch(
  () => [user.uid, user.oid, user.proxyInfo?.proxyOid, user.proxyInfo?.proxy],
  () => {
    queryGeneration++
    optionsGeneration++
    loading.value = false
    editor.value = false
    settings.value = undefined
    models.value = []
    options.value = []
  }
)
onMounted(load)
</script>

<template>
  <section class="ai-service-page">
    <header class="ai-page-header">
      <h1>{{ tr('title') }}</h1>
      <div v-if="tab === 'models' && settings" class="ai-master-switch">
        <span>{{ tr('service') }}</span
        ><el-switch
          :model-value="settings.enabled"
          :disabled="busy"
          :aria-label="tr('service')"
          @change="toggleService"
        />
      </div>
    </header>
    <el-tabs v-model="tab" class="ai-tabs"
      ><el-tab-pane :label="tr('models')" name="models" /><el-tab-pane
        :label="tr('usage')"
        name="usage"
    /></el-tabs>
    <el-alert
      v-if="settings && settings.runtimeState !== 'ACTIVE'"
      :title="tr('runtime_pending')"
      type="warning"
      :closable="false"
    />
    <div v-if="failed" class="ai-error" role="alert">
      <p>{{ tr('load_failed') }}</p>
      <el-button @click="load">{{ tr('refresh') }}</el-button>
    </div>
    <div v-else-if="tab === 'models'" v-loading="loading" class="ai-model-card">
      <div class="ai-toolbar">
        <el-button type="primary" :disabled="!settings" @click="edit()"
          ><span class="ai-plus" aria-hidden="true">＋</span>{{ tr('add') }}</el-button
        >
        <div class="ai-toolbar-right">
          <el-input
            v-model="search"
            clearable
            :placeholder="tr('search_model')"
            :aria-label="tr('search_model')"
            @keyup.enter="query"
            @clear="query"
          />
          <el-select
            class="ai-default-model-select"
            :model-value="settings?.defaultModelId ?? ''"
            :placeholder="tr('default_model')"
            :aria-label="tr('default_model')"
            :title="
              tr('current_default', {
                name:
                  options.find(model => model.id === settings?.defaultModelId)?.name ||
                  tr('no_default_model')
              })
            "
            :suffix-icon="CaretBottom"
            :fit-input-width="false"
            popper-class="ai-default-model-options"
            placement="bottom-end"
            :fallback-placements="['bottom-end', 'top-end']"
            :offset="6"
            filterable
            remote
            remote-show-suffix
            :remote-method="loadOptions"
            :disabled="busy || !settings"
            @change="selectDefault"
          >
            <template #prefix
              ><el-icon aria-hidden="true"><Operation /></el-icon
            ></template>
            <template #label>{{ tr('default_model') }}</template>
            <el-option value="" :label="tr('no_default_model')" />
            <el-option
              v-for="model in usable"
              :key="model.id"
              :value="model.id"
              :label="model.name"
              :disabled="!model.enabled || !model.validated"
            />
          </el-select>
        </div>
      </div>
      <el-table :data="models" class="ai-model-table" :empty-text="tr('no_models')" height="100%">
        <el-table-column :label="tr('model_name')" min-width="200"
          ><template #default="{ row }"
            ><div class="ai-model-name">
              <strong>{{ row.name }}</strong
              ><span v-if="row.systemDefault" class="ai-default-badge">{{
                tr('default_badge')
              }}</span>
            </div>
            <div class="ai-secondary">{{ tr('openai') }}</div></template
          ></el-table-column
        >
        <el-table-column :label="tr('identifier')" min-width="150"
          ><template #default="{ row }"
            ><span class="ai-identifier">{{ row.modelIdentifier }}</span></template
          ></el-table-column
        >
        <el-table-column :label="tr('status')" min-width="125"
          ><template #default="{ row }"
            ><div class="ai-status">
              <el-tooltip :disabled="row.validated" :content="tr('enable_test')"
                ><el-switch
                  :model-value="row.enabled"
                  :disabled="busy || !row.validated"
                  :aria-label="row.name"
                  @change="value => toggleModel(row, value)" /></el-tooltip
              ><span>{{ tr(row.enabled ? 'enabled' : 'disabled') }}</span>
            </div></template
          ></el-table-column
        >
        <el-table-column :label="tr('scope')" min-width="155"
          ><template #default="{ row }"
            ><div v-if="row.systemDefault">{{ tr('default_model') }}</div>
            <div v-if="row.organizationReferences">
              {{
                row.organizationNames?.length === 1 && row.organizationReferences === 1
                  ? tr('specified_name', { name: row.organizationNames[0] })
                  : row.organizationNames?.length
                  ? tr('specified_many', {
                      name: row.organizationNames[0],
                      count: row.organizationReferences
                    })
                  : tr('specified_reference', { count: row.organizationReferences })
              }}
            </div>
            <span v-if="!row.systemDefault && !row.organizationReferences">{{
              tr('unassigned')
            }}</span></template
          ></el-table-column
        >
        <el-table-column :label="tr('test_result')" min-width="160"
          ><template #default="{ row }"
            ><span
              :class="{
                'ai-passed': row.lastTestStatus === 'PASSED',
                'ai-failed': row.lastTestStatus === 'FAILED'
              }"
              >{{ testing.has(row.id) ? tr('testing') : tr(row.lastTestStatus) }}</span
            >
            <div class="ai-secondary">{{ formatTime(row.lastTestTime) }}</div></template
          ></el-table-column
        >
        <el-table-column
          :label="tr('operations')"
          :width="actionsWidth"
          fixed="right"
          class-name="ai-operation-cell"
          ><template #default="{ row, $index }"
            ><div :ref="$index === 0 ? setActionsRef : undefined" class="ai-row-actions">
              <el-button link type="primary" @click="edit(row)">{{ tr('edit_action') }}</el-button
              ><el-button link type="primary" :disabled="testing.has(row.id)" @click="test(row)">{{
                tr('test_action')
              }}</el-button
              ><el-dropdown trigger="click" @command="command => more(command, row)"
                ><el-button link type="primary"
                  >{{ tr('more')
                  }}<el-icon class="ai-more-icon" aria-hidden="true"
                    ><ArrowDown /></el-icon></el-button
                ><template #dropdown
                  ><el-dropdown-menu
                    ><el-dropdown-item
                      command="default"
                      :disabled="!row.systemDefault && (!row.enabled || !row.validated)"
                      >{{
                        tr(row.systemDefault ? 'clear_default' : 'set_default')
                      }}</el-dropdown-item
                    ><el-dropdown-item command="delete">{{
                      tr('delete')
                    }}</el-dropdown-item></el-dropdown-menu
                  ></template
                ></el-dropdown
              >
            </div></template
          ></el-table-column
        >
      </el-table>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, prev, pager, next, sizes"
        @current-change="load"
        @size-change="query"
      />
    </div>
    <UsageSettings v-else-if="settings" :settings="settings" :models="usable" @saved="load" />
    <ModelEditor v-if="editor" :model="editing" @close="editor = false" @saved="load" />
  </section>
</template>

<style scoped lang="less">
.ai-service-page {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding: 24px;
  background: var(--ed-bg-color-page, #f5f6f7);
  color: var(--ed-text-color-primary);
}
.ai-page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 28px;
  margin-bottom: 12px;
  h1 {
    font-size: 18px;
    line-height: 28px;
    font-weight: 600;
    margin: 0;
  }
}
.ai-master-switch {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
}
.ai-tabs {
  flex: none;
  :deep(.ed-tabs__header) {
    margin-bottom: 16px;
  }
  :deep(.ed-tabs__item) {
    height: 44px;
  }
  :deep(.ed-tabs__nav-wrap::after) {
    height: 1px;
  }
  :deep(.ed-tabs__content) {
    display: none;
  }
}
.ai-model-card {
  flex: 1;
  min-height: 0;
  padding: 24px;
  background: var(--ed-bg-color);
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}
.ai-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  gap: 16px;
  flex-wrap: wrap;
}
.ai-toolbar-right {
  display: flex;
  gap: 12px;
  .ed-input {
    width: 240px;
  }
  .ai-default-model-select {
    width: 146px;
    :deep(.ed-select__wrapper) {
      height: 32px;
      padding: 0 10px;
      gap: 6px;
    }
    :deep(.ed-select__prefix),
    :deep(.ed-select__placeholder),
    :deep(.ed-select__caret) {
      color: var(--ed-text-color-primary);
    }
    :deep(.ed-select__prefix) {
      padding-right: 0;
      &::after {
        display: none;
      }
    }
  }
}
.ai-plus {
  font-size: 20px;
  margin-right: 4px;
  line-height: 20px;
}
.ai-model-table {
  flex: 1;
  :deep(.ai-operation-cell .cell) {
    padding-left: 24px;
    padding-right: 24px;
  }
  :deep(th.ed-table__cell) {
    background: var(--ed-fill-color-light);
    color: var(--ed-text-color-regular);
    font-weight: 400;
    height: 44px;
  }
  :deep(td.ed-table__cell) {
    padding: 16px 0;
  }
}
.ai-model-name {
  display: flex;
  align-items: center;
  gap: 8px;
  strong {
    font-weight: 500;
    overflow-wrap: anywhere;
  }
}
.ai-secondary {
  font-size: 12px;
  color: var(--ed-text-color-secondary);
  line-height: 20px;
  margin-top: 2px;
}
.ai-default-badge {
  color: var(--ed-color-primary);
  background: var(--ed-color-primary-light-9);
  border-radius: 4px;
  padding: 0 6px;
  font-size: 12px;
  white-space: nowrap;
}
.ai-identifier {
  font-family: monospace;
  font-size: 12px;
  overflow-wrap: anywhere;
}
.ai-status {
  display: flex;
  align-items: center;
  gap: 8px;
}
.ai-passed {
  color: var(--ed-color-success);
}
.ai-failed {
  color: var(--ed-color-danger);
}
.ai-row-actions {
  display: inline-flex;
  width: max-content;
  gap: 16px;
  align-items: center;
  white-space: nowrap;
  > * {
    flex-shrink: 0;
  }
  .ed-button + .ed-button {
    margin-left: 0;
  }
  :deep(.ed-button.is-link) {
    min-width: 0;
    padding: 0;
  }
}
.ai-more-icon {
  margin-left: 4px;
}
.ed-pagination {
  justify-content: flex-end;
  margin-top: 16px;
}
.ai-error {
  padding: 32px;
  text-align: center;
}
@media (max-width: 1280px) {
  .ai-toolbar-right .ed-input {
    width: 200px;
  }
}
</style>

<style lang="less">
.ai-model-drawer,
.ai-usage-drawer {
  max-width: 100vw;
  color: var(--ed-text-color-primary);
  background: var(--ed-bg-color);

  .ed-drawer__header {
    flex: none;
    height: 56px;
    padding: 16px 24px;
    margin: 0;
    color: var(--ed-text-color-primary);
    border-bottom: 1px solid var(--ed-border-color);
  }

  .ed-drawer__body {
    flex: 1;
    min-height: 0;
    padding: 24px;
    overflow: auto;
  }

  .ed-drawer__footer {
    flex: none;
    padding: 16px 24px;
    color: var(--ed-text-color-primary);
    background: var(--ed-bg-color);
    border-top: 1px solid var(--ed-border-color);
  }
}

.ai-impact-confirm .ed-message-box__message {
  max-height: min(360px, 55vh);
  overflow: auto;
  white-space: pre-line;
  overflow-wrap: anywhere;
}

.ai-model-confirm {
  width: 480px;
  max-width: calc(100vw - 32px);
  padding: 0;
  border: none;
  border-radius: 12px;
  box-shadow: 0 6px 24px rgb(31 35 41 / 10%);

  .ed-message-box__header {
    padding: 16px 56px 16px 24px;
    border-bottom: 1px solid var(--ed-border-color);
  }

  .ed-message-box__title {
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: var(--ed-text-color-primary);
    overflow-wrap: anywhere;
  }

  .ed-message-box__headerbtn {
    top: 20px;
    right: 24px;
    width: 16px;
    height: 16px;
  }

  .ed-message-box__content {
    padding: 20px 24px;
  }

  .ed-message-box__message {
    white-space: normal;
    font-size: 14px;
    font-weight: 400;
    color: var(--ed-text-color-primary);
  }

  .ai-disable-content > p {
    margin: 0;
    line-height: 24px;
    font-weight: 400;
    color: #646a73;
  }

  .ai-disable-content > .ai-impact-heading {
    margin-top: 12px;
    font-size: 13px;
    font-weight: 500;
  }

  .ai-impact-list {
    margin: 14px 0;
    padding-left: 20px;
    list-style: disc;
    line-height: 23px;
  }

  .ed-message-box__btns {
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    padding: 16px 24px;
    border-top: 1px solid var(--ed-border-color);

    .ed-button {
      min-width: 0;
      height: 32px;
      margin: 0;
      padding: 4px 14px;
      border-radius: 6px;
      font-weight: 400;
      line-height: 22px;
    }
  }
}

.ai-model-confirm-overlay {
  background-color: rgb(0 0 0 / 30%);
}

.dark .ai-model-confirm .ai-disable-content > p {
  color: var(--ed-text-color-secondary);
}

.ai-model-default-confirm .ai-default-content > p + p {
  margin-top: 12px;
}
.ai-model-default-confirm .ai-default-content > .ai-default-hint {
  font-size: 12px;
  line-height: 20px;
}
.ai-default-model-options .ed-select-dropdown {
  min-width: 280px !important;
}
</style>
