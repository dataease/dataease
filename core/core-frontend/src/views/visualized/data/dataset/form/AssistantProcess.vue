<script setup lang="ts">
import { computed } from 'vue'
import AssistantToolResult from './AssistantToolResult.vue'
import { useI18n } from '@/hooks/web/useI18n'
import type { AiConversationTool } from '@/api/aiConversation'
import { aiFailureKey } from '@/api/aiService'
import {
  parseToolResult,
  toolOutcome,
  type AnswerProcess,
  type ProcessEntry
} from './assistantProcess'

const props = defineProps<{
  process: AnswerProcess
  elapsed?: string
  status: string
  sessionId?: string
  executionId?: string
}>()
const emit = defineEmits<{
  (event: 'detail-error', error: unknown): void
  (event: 'update:open', open: boolean): void
}>()
const { t } = useI18n()
const tr = (key: string) => t(`ai_conversation.${key}`)
const active = (tool: AiConversationTool) => ['STARTED', 'RUNNING'].includes(tool.status)
const expanded = computed(
  () =>
    props.process.open ??
    (props.status === 'RUNNING' ||
      props.status === 'UNREACHABLE' ||
      props.process.entries.some(item => item.kind === 'tool' && item.open))
)
const format = (value: unknown) =>
  typeof value === 'string' ? value : JSON.stringify(value, null, 2)
const verdict = (tool: AiConversationTool) => {
  if (active(tool)) return tr('tool_running')
  if (tool.status === 'CANCELLED') return tr('tool_cancelled')
  if (tool.status === 'FAILED') return tr('tool_failed')
  switch (toolOutcome(tool)) {
    case 'INVALID':
    case 'PREVIEW_FAILED':
      return tr('validation_rejected')
    case 'PASS':
    case 'DETAIL_PASS':
      return tr('validation_passed')
    case 'AGGREGATE_PLACEHOLDER':
      return tr('aggregate_placeholder')
    case 'NOT_RUN':
      return tr('unrun_sql')
    case 'CLARIFY_ROLE':
    case 'CLARIFY_FIELD':
      return tr('validation_clarify')
    default:
      return tr('tool_succeeded')
  }
}
const actionLabel = (tool: AiConversationTool) => {
  const params = tool.parameters
  const labels: Record<string, [string, string | undefined]> = {
    get_table_fields: ['read_table_fields', params.table],
    get_named_table_fields: ['read_named_fields', params.table],
    find_historical_step: ['find_historical_step', params.subject === '*' ? '' : params.subject],
    revalidate_current_formula: ['validate_formula', undefined],
    propose_calc_field: ['validate_formula_object', params.name || params.expression],
    search_semantics: ['search_registered_tables', params.terms],
    search_registered_tables: ['search_registered_tables', params.terms],
    search_physical_tables: ['search_physical_tables', params.terms],
    read_sql_guide: ['read_sql_guide', params.topic],
    read_relations: [
      'read_relations',
      [params.fromTable, params.toTable].filter(Boolean).join(' → ')
    ],
    propose_sql: ['propose_sql', params.sql]
  }
  const label = labels[tool.name]
  const object = (label?.[1] ?? '').replace(/\s+/g, ' ').trim()
  return label
    ? t(`ai_conversation.${label[0]}`, {
        object: object.length > 64 ? object.slice(0, 63) + '…' : object
      }).trim()
    : tool.name
}
const outcomeLabel = (tool: AiConversationTool) =>
  tool.status === 'SUCCEEDED' && tool.fieldCount != null
    ? t('ai_conversation.fields_count', { count: tool.fieldCount })
    : verdict(tool)
const toggleTool = (entry: ProcessEntry) => {
  if (entry.kind === 'tool') entry.open = !entry.open
}
</script>

<template>
  <details v-if="process.entries.length" class="assistant-process" :open="expanded">
    <summary @click.prevent="emit('update:open', !expanded)">
      {{ tr('answer_process') }}<span v-if="elapsed"> · {{ elapsed }}</span>
    </summary>
    <template v-for="entry in process.entries" :key="entry.sequence + '-' + entry.kind">
      <details v-if="entry.kind === 'tool'" class="tool-step" :open="entry.open">
        <summary @click.prevent="toggleTool(entry)">
          <span>{{ t('ai_conversation.step_number', { number: entry.tool.stepNumber }) }} · </span>
          <strong>{{ actionLabel(entry.tool) }}</strong>
          <span> · {{ outcomeLabel(entry.tool) }}</span>
        </summary>
        <dl class="tool-detail">
          <div class="tool-name">
            <dt>{{ tr('tool') }}</dt>
            <dd>
              <code>{{ entry.tool.name }}</code>
            </dd>
          </div>
          <div>
            <dt>{{ tr('tool_parameters') }}</dt>
            <dd class="tool-value">
              <pre>{{ format(entry.tool.parameters) }}</pre>
            </dd>
          </div>
          <div v-if="entry.tool.result">
            <dt>{{ tr('tool_result') }}</dt>
            <dd class="tool-value">
              <pre>{{ format(parseToolResult(entry.tool.result)) }}</pre>
            </dd>
          </div>
          <div
            v-if="entry.tool.resultSequence && sessionId && executionId && expanded && entry.open"
          >
            <dt>{{ tr('tool_result') }}</dt>
            <dd class="tool-value">
              <AssistantToolResult
                :session-id="sessionId"
                :execution-id="executionId"
                :sequence="entry.tool.resultSequence"
                @detail-error="emit('detail-error', $event)"
              />
            </dd>
          </div>
          <div v-if="entry.error">
            <dt>{{ tr('tool_end_reason') }}</dt>
            <dd>
              {{
                entry.error === 'connection_interrupted'
                  ? tr(entry.error)
                  : t(`ai_service.${aiFailureKey(entry.error)}`)
              }}
              ({{ entry.error }})
            </dd>
          </div>
        </dl>
      </details>
      <p v-else-if="entry.text.trim()" class="process-note">{{ entry.text.trim() }}</p>
    </template>
  </details>
</template>

<style scoped>
.assistant-process {
  overflow-wrap: anywhere;
}

summary {
  cursor: pointer;
}

.process-note {
  margin: 4px 0;
  white-space: pre-wrap;
}

.tool-step {
  margin: 2px 0;
  border-radius: 6px;
}

.tool-step[open],
.tool-step > summary:hover {
  background: var(--ed-fill-color-light);
}

.tool-step > summary {
  position: relative;
  display: block;
  padding: 2px 8px 2px 22px;
  font-size: 12px;
  line-height: 20px;
  color: var(--ed-text-color-secondary);
  border-radius: inherit;
}

.tool-step > summary::-webkit-details-marker {
  display: none;
}

.tool-step > summary::before {
  position: absolute;
  top: 9px;
  width: 5px;
  height: 5px;
  border: solid currentcolor;
  border-width: 0 1.5px 1.5px 0;
  content: '';
  transform: rotate(-45deg);
  inset-inline-start: 8px;
}

.tool-step[open] > summary::before {
  transform: rotate(45deg);
}

.tool-step strong {
  font-size: 13px;
  font-weight: 500;
  color: var(--ed-text-color-primary);
}

.tool-detail {
  display: grid;
  padding: 4px 10px 10px 22px;
  margin: 0;
  gap: 8px;
}

.tool-detail dt {
  font-size: 12px;
  line-height: 18px;
  color: var(--ed-text-color-regular);
}

.tool-detail dd {
  min-width: 0;
  margin: 0;
}

.tool-name {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  align-items: baseline;
  gap: 8px;
}

.tool-detail .tool-value {
  max-height: 160px;
  padding: 5px 8px;
  margin-top: 2px;
  overflow: auto;
  background: var(--ed-bg-color);
  border: 1px solid var(--ed-border-color);
  border-radius: 4px;
  box-sizing: border-box;
}

.tool-value :deep(pre) {
  margin: 0;
  white-space: pre-wrap;
}
</style>
