<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus-secondary'
import { useI18n } from '@/hooks/web/useI18n'
import {
  aiFailureKey,
  createAiDraft,
  discoverAiModels,
  getAiModel,
  saveAiModel,
  testAiCandidate,
  type AiCandidate,
  type AiModel
} from '@/api/aiService'

const props = defineProps<{ model?: AiModel }>()
const emit = defineEmits<{ close: []; saved: [] }>()
const { t } = useI18n()
const tr = (key: string, args: Record<string, unknown> = {}) => t(`ai_service.${key}`, args)
const visible = ref(true)
const ready = ref(false)
const loadFailed = ref(false)
const loading = ref(false)
const saving = ref(false)
const discovering = ref(false)
const testing = ref(false)
const showKey = ref(false)
const manual = ref(false)
const choices = ref<string[]>([])
const discoveryError = ref('')
const discoveryState = ref('')
const testState = ref('')
const testError = ref('')
const basic = ref(false)
const structured = ref(false)
const baseline = ref('')
const connectionBaseline = ref('')
const qualifiedBaseline = ref(false)
const candidate = reactive<AiCandidate>({
  id: '',
  name: '',
  provider: 'OPENAI_COMPATIBLE',
  baseUrl: '',
  apiKey: '',
  modelIdentifier: '',
  processingLocation: 'UNKNOWN',
  processingNote: ''
})
let generation = 0
let controller: AbortController | undefined
let proofExpires = 0
let proofTimer: ReturnType<typeof setTimeout> | undefined
const serialize = () => JSON.stringify(candidate)
const connection = () =>
  JSON.stringify([
    candidate.provider,
    candidate.baseUrl,
    candidate.apiKey,
    candidate.modelIdentifier
  ])
const editing = computed(() => Boolean(props.model))
const canConnect = computed(
  () =>
    ready.value &&
    /^https?:\/\//.test(candidate.baseUrl) &&
    (Boolean(candidate.apiKey) || Boolean(props.model?.keyConfigured))
)
const qualified = computed(
  () =>
    (qualifiedBaseline.value && connection() === connectionBaseline.value) ||
    Boolean(candidate.testProof && proofExpires > Date.now())
)
const canSave = computed(
  () =>
    ready.value &&
    Boolean(candidate.name.trim()) &&
    Boolean(candidate.modelIdentifier.trim()) &&
    qualified.value
)
const dirty = computed(
  () => ready.value && (serialize() !== baseline.value || testState.value === 'PASSED')
)
const cancelWork = () => {
  generation++
  controller?.abort()
  controller = undefined
  discovering.value = false
  testing.value = false
}
const invalidate = (clearIdentifier: boolean, address = false) => {
  cancelWork()
  candidate.testProof = undefined
  clearTimeout(proofTimer)
  proofExpires = 0
  qualifiedBaseline.value = false
  testState.value = ''
  testError.value = ''
  basic.value = false
  structured.value = false
  if (clearIdentifier) {
    choices.value = []
    candidate.modelIdentifier = ''
    discoveryError.value = ''
    discoveryState.value = ''
  }
  if (address) {
    candidate.processingLocation = 'UNKNOWN'
    candidate.processingNote = ''
  }
}
const discover = async () => {
  if (!canConnect.value || discovering.value || testing.value) return
  cancelWork()
  const current = generation
  controller = new AbortController()
  discovering.value = true
  discoveryError.value = ''
  discoveryState.value = ''
  try {
    const result = await discoverAiModels({ ...candidate }, controller.signal)
    if (current !== generation) return
    choices.value = result
    discoveryState.value = result.length ? 'LOADED' : 'EMPTY'
    discoveryError.value = result.length
      ? tr('discovery_loaded', { count: result.length })
      : tr('empty_discovery')
  } catch (error) {
    if (current === generation) {
      discoveryState.value = 'FAILED'
      discoveryError.value = typeof error === 'string' ? error : tr('discover_failed')
    }
  } finally {
    if (current === generation) discovering.value = false
  }
}
const test = async () => {
  if (!canConnect.value || !candidate.modelIdentifier.trim() || testing.value) return
  cancelWork()
  const current = generation
  controller = new AbortController()
  testing.value = true
  candidate.testProof = undefined
  testState.value = ''
  testError.value = ''
  try {
    const result = await testAiCandidate({ ...candidate }, controller.signal)
    if (current !== generation) return
    basic.value = result.basic
    structured.value = result.structured
    if (result.basic && result.structured && result.testProof) {
      candidate.testProof = result.testProof
      proofExpires = Date.now() + 9 * 60 * 1000
      clearTimeout(proofTimer)
      proofTimer = setTimeout(() => {
        candidate.testProof = undefined
        testState.value = ''
        proofExpires = 0
      }, 9 * 60 * 1000)
      testState.value = 'PASSED'
    } else {
      testState.value = 'FAILED'
      testError.value = tr(aiFailureKey(result.errorCode))
    }
  } catch (error) {
    if (current === generation) {
      testState.value = 'FAILED'
      testError.value = typeof error === 'string' ? error : tr('test_failed')
    }
  } finally {
    if (current === generation) testing.value = false
  }
}
const clear = () => {
  cancelWork()
  clearTimeout(proofTimer)
  candidate.apiKey = ''
  candidate.testProof = undefined
  candidate.draftHandle = undefined
  choices.value = []
}
const close = async (done?: () => void) => {
  if (saving.value) return
  if (dirty.value) {
    try {
      await ElMessageBox.confirm(tr('discard_text'), tr('discard_title'), {
        confirmButtonText: tr('discard'),
        cancelButtonText: tr('keep_editing'),
        type: 'warning'
      })
    } catch {
      return
    }
  }
  clear()
  visible.value = false
  done?.()
  emit('close')
}
const save = async () => {
  if (!canSave.value || saving.value || testing.value || discovering.value) return
  saving.value = true
  try {
    await saveAiModel({ ...candidate }, editing.value)
    ElMessage.success(tr('save_success'))
    clear()
    emit('saved')
    emit('close')
  } catch (error) {
    // 维持稳定 ID 和候选内容，不因网络失败创建新的模型。
    if (typeof error !== 'string' && !(error as { response?: unknown })?.response) {
      try {
        await getAiModel(candidate.id)
        ElMessage.warning(tr('save_found'))
        emit('saved')
      } catch {
        ElMessage.warning(tr('save_unknown'))
      }
    }
  } finally {
    saving.value = false
  }
}
const initialize = async () => {
  loadFailed.value = false
  loading.value = true
  const current = generation
  try {
    if (props.model) {
      const m = await getAiModel(props.model.id)
      if (generation !== current) return
      Object.assign(candidate, {
        id: m.id,
        expectedVersion: m.rowVersion,
        expectedConfigRevision: m.configRevision,
        name: m.name,
        provider: m.provider,
        baseUrl: m.baseUrl,
        modelIdentifier: m.modelIdentifier,
        processingLocation: m.processingLocation,
        processingNote: m.processingNote ?? ''
      })
      choices.value = [m.modelIdentifier]
      qualifiedBaseline.value = m.validated
      connectionBaseline.value = connection()
    } else {
      const draft = await createAiDraft()
      if (generation !== current) return
      Object.assign(candidate, draft)
    }
    baseline.value = serialize()
    ready.value = true
  } catch {
    if (current === generation) loadFailed.value = true
  } finally {
    if (current === generation) loading.value = false
  }
}
onMounted(initialize)
onBeforeUnmount(clear)
</script>

<template>
  <el-drawer
    v-model="visible"
    :title="tr(editing ? 'edit' : 'add')"
    size="600px"
    :before-close="close"
    :close-on-click-modal="false"
    :close-on-press-escape="!saving"
    class="ai-model-drawer"
  >
    <el-skeleton v-if="loading" :rows="8" animated />
    <div v-else-if="loadFailed" role="alert">
      <p>{{ tr('load_failed') }}</p>
      <el-button @click="initialize">{{ tr('refresh') }}</el-button>
    </div>
    <el-form
      v-else
      label-position="top"
      :disabled="saving || !ready"
      class="ai-model-form"
      @submit.prevent="save"
    >
      <el-form-item :label="tr('model_name')" required>
        <el-input v-model="candidate.name" :maxlength="128" :placeholder="tr('name_placeholder')" />
      </el-form-item>
      <el-form-item :label="tr('provider')" required>
        <el-select v-model="candidate.provider" @change="invalidate(true)"
          ><el-option value="OPENAI_COMPATIBLE" :label="tr('openai')"
        /></el-select>
      </el-form-item>
      <el-form-item :label="tr('address')" required>
        <el-input
          v-model="candidate.baseUrl"
          :maxlength="2048"
          :placeholder="tr('address_placeholder')"
          @input="invalidate(true, true)"
        />
      </el-form-item>
      <el-form-item :label="tr('key')" :required="!editing">
        <el-input
          v-model="candidate.apiKey"
          :type="showKey ? 'text' : 'password'"
          autocomplete="new-password"
          :maxlength="4096"
          :placeholder="tr(editing ? 'key_keep' : 'key_placeholder')"
          @input="invalidate(true)"
        >
          <template #suffix
            ><el-button
              link
              type="primary"
              :aria-label="tr(showKey ? 'hide' : 'show')"
              @click="showKey = !showKey"
              >{{ tr(showKey ? 'hide' : 'show') }}</el-button
            ></template
          >
        </el-input>
      </el-form-item>
      <el-button
        class="ai-discover"
        :loading="discovering"
        :disabled="!canConnect || testing"
        @click="discover"
        >{{ tr(discovering ? 'discovering' : 'discover') }}</el-button
      >
      <p
        v-if="discoveryError"
        role="status"
        class="ai-catalog-result"
        :class="{
          'ai-passed': discoveryState === 'LOADED',
          'ai-failed': discoveryState === 'FAILED'
        }"
      >
        {{ discoveryError }}
      </p>
      <el-form-item>
        <template #label
          ><div class="ai-field-label">
            <span>{{ tr('identifier') }} <span class="ai-required" aria-hidden="true">*</span></span
            ><el-button link type="primary" @click="manual = !manual">{{
              tr(manual ? 'from_list' : 'manual')
            }}</el-button>
          </div></template
        >
        <el-input
          v-if="manual"
          v-model="candidate.modelIdentifier"
          :maxlength="256"
          :placeholder="tr('manual_placeholder')"
          @input="invalidate(false)"
        />
        <el-select
          v-else
          v-model="candidate.modelIdentifier"
          :disabled="!choices.length"
          :placeholder="tr(choices.length ? 'choose_model' : 'discover_first')"
          @change="invalidate(false)"
        >
          <el-option v-for="choice in choices" :key="choice" :value="choice" :label="choice" />
        </el-select>
      </el-form-item>
      <el-form-item :label="tr('location')">
        <el-select v-model="candidate.processingLocation"
          ><el-option
            v-for="location in ['UNKNOWN', 'PUBLIC', 'PRIVATE', 'GATEWAY', 'UNVERIFIED']"
            :key="location"
            :value="location"
            :label="tr(location)"
        /></el-select>
        <p class="ai-form-hint">{{ tr('location_hint') }}</p>
      </el-form-item>
      <el-form-item :label="tr('location_note')"
        ><el-input
          v-model="candidate.processingNote"
          :maxlength="512"
          :placeholder="tr('note_placeholder')"
      /></el-form-item>
      <el-alert
        v-if="testState === 'FAILED'"
        type="error"
        :closable="false"
        :title="testError || tr('test_failed')"
      />
      <p v-if="testState" class="ai-form-hint">
        {{ tr('basic') }}：{{ tr(basic ? 'PASSED' : 'FAILED') }} · {{ tr('structured') }}：{{
          tr(structured ? 'PASSED' : 'FAILED')
        }}
      </p>
    </el-form>
    <template #footer
      ><div class="ai-drawer-footer">
        <div class="ai-test-feedback">
          <el-button
            :loading="testing"
            :disabled="!canConnect || !candidate.modelIdentifier || discovering || saving"
            @click="test"
            >{{ tr(testing ? 'testing' : testState === 'FAILED' ? 'retest' : 'test') }}</el-button
          ><span
            v-if="testState"
            :class="testState === 'PASSED' ? 'ai-passed' : 'ai-failed'"
            :title="testState === 'PASSED' ? tr('test_passed_detail') : testError"
            role="status"
            >{{ tr(testState) }}</span
          >
        </div>
        <div>
          <el-button :disabled="saving" @click="close()">{{ tr('cancel') }}</el-button
          ><el-button
            type="primary"
            :loading="saving"
            :disabled="!canSave || testing || discovering"
            @click="save"
            >{{ tr('save') }}</el-button
          >
        </div>
      </div></template
    >
  </el-drawer>
</template>
<style scoped lang="less">
.ai-model-form {
  .ed-form-item {
    margin-bottom: 20px;
  }
  .ed-select {
    width: 100%;
  }
}
.ai-field-label {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}
:deep(.ed-form-item__label) {
  width: 100%;
}
.ai-required {
  color: var(--ed-color-danger);
  margin-left: 4px;
}
.ai-discover {
  margin-bottom: 20px;
}
.ai-form-hint {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  line-height: 20px;
  margin: 6px 0 0;
}
.ai-drawer-footer {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}
.ai-test-feedback {
  display: flex;
  align-items: center;
  gap: 12px;
}
.ai-passed {
  color: var(--ed-color-success);
  font-size: 14px;
}
.ai-failed {
  color: var(--ed-color-danger);
  font-size: 14px;
}
.ai-catalog-result {
  margin: -12px 0 20px;
  font-size: 12px;
  line-height: 20px;
}
</style>
