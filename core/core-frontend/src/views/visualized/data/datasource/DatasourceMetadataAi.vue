<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { listDatasourceTables } from '@/api/datasource'
import {
  getMetadataAiTask,
  submitMetadataAiTask,
  type MetadataAiKind,
  type MetadataAiTask
} from '@/api/datasourceMetadataAi'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{
  datasourceId: string
  datasourceName: string
  kind: MetadataAiKind
  semanticTables?: { tableName: string; name?: string | null }[]
}>()
const emit = defineEmits<{ refresh: [] }>()
const { t: tr } = useI18n('datasourceMetadata')
const open = ref(false)
const busy = ref(false)
const failed = ref(false)
const task = ref<MetadataAiTask | null>(null)
const selected = ref<string[]>([])
const options = ref<{ tableName: string; name?: string | null }[]>([])
const title = computed(() => tr(props.kind === 'RELATION' ? 'discover' : 'complete'))
const selectionLimit = 10
const selectionTarget = computed(() => Math.min(selectionLimit, options.value.length))
const selectionFull = computed(() => selected.value.length === selectionTarget.value)
const toggleAll = () => {
  if (selectionFull.value) {
    selected.value = []
    return
  }
  selected.value = Array.from(
    new Set([...selected.value, ...options.value.map(table => table.tableName)])
  ).slice(0, selectionLimit)
}
const min = computed(() => (props.kind === 'RELATION' ? 2 : 1))
const running = computed(() => task.value?.status === 'QUEUED' || task.value?.status === 'RUNNING')
const refreshKey = ref(0)
let generation = 0
let timer: ReturnType<typeof setTimeout> | undefined

const poll = (key: number) => {
  timer = setTimeout(async () => {
    const id = task.value?.id
    try {
      const { data } = await getMetadataAiTask(props.datasourceId, props.kind, id)
      if (key !== generation) return
      task.value = data
      if (running.value) poll(key)
      else {
        emit('refresh')
        if (!data) refreshKey.value++
      }
    } catch {
      if (key === generation) failed.value = true
    }
  }, 1800)
}

watch([open, () => props.datasourceId, refreshKey], async ([visible], _, onCleanup) => {
  const key = ++generation
  onCleanup(() => {
    ++generation
    clearTimeout(timer)
  })
  if (!visible) return
  const previous = task.value
  task.value = null
  selected.value = []
  options.value = []
  failed.value = false
  busy.value = true
  try {
    const { data } = await getMetadataAiTask(props.datasourceId, props.kind)
    if (key !== generation) return
    task.value = data
    if (data) poll(key)
    else if (props.kind === 'RELATION') {
      const { data: tables } = await listDatasourceTables({ datasourceId: props.datasourceId })
      if (key === generation) options.value = tables
    } else options.value = props.semanticTables ?? []
    if (!data && previous && key === generation) emit('refresh')
  } catch {
    if (key === generation) failed.value = true
  } finally {
    if (key === generation) busy.value = false
  }
})

const submit = async () => {
  if (busy.value || selected.value.length < min.value || selected.value.length > selectionLimit)
    return
  const key = generation
  busy.value = true
  try {
    const { data } = await submitMetadataAiTask(props.datasourceId, props.kind, selected.value)
    if (key !== generation) return
    task.value = data
    if (running.value) poll(key)
  } catch {
    // The shared interceptor displays request errors.
  } finally {
    if (key === generation) busy.value = false
  }
}
</script>

<template>
  <el-button type="primary" @click="open = true">{{ title }}</el-button>
  <el-drawer
    v-model="open"
    :title="title"
    size="min(780px, 100vw)"
    header-class="metadata-ai-drawer-header"
    body-class="metadata-ai-drawer-body"
    footer-class="metadata-ai-drawer-footer"
    append-to-body
  >
    <template #header="{ titleId, titleClass }">
      <div>
        <div :id="titleId" :class="titleClass">{{ title }}</div>
        <div v-if="datasourceName" class="metadata-ai-subtitle">{{ datasourceName }}</div>
      </div>
    </template>
    <div v-loading="busy" class="metadata-ai-content">
      <template v-if="failed">
        <p>{{ tr('loadFailed') }}</p>
        <el-button @click="refreshKey++">{{ tr('retry') }}</el-button>
      </template>
      <template v-else-if="task">
        <p>{{ tr('aiStatus' + task.status) }}</p>
        <p v-if="!running">
          {{
            task.status === 'SUCCESS' && task.written === 0 && task.skipped === 0
              ? tr('aiNoResults')
              : tr(kind === 'RELATION' ? 'aiRelationResult' : 'aiSemanticResult', { ...task })
          }}
        </p>
        <el-button v-if="!running" @click="open = false">{{ tr('aiBackToList') }}</el-button>
      </template>
      <section v-else-if="!busy" class="metadata-ai-selection">
        <h3 class="metadata-ai-heading">{{ tr('aiSelectTables') }}</h3>
        <div class="metadata-ai-selection-bar">
          <p>{{ tr(kind === 'RELATION' ? 'aiRelationScope' : 'aiSemanticScope') }}</p>
          <div v-if="options.length" class="metadata-ai-selection-actions">
            <span aria-live="polite">
              {{ tr('aiSelectedCount', { count: selected.length, limit: selectionTarget }) }}
            </span>
            <el-button link type="primary" @click="toggleAll">
              {{
                tr(
                  selectionFull
                    ? 'aiClearSelection'
                    : options.length > selectionLimit
                    ? 'aiSelectUpToLimit'
                    : 'aiSelectAll',
                  { limit: selectionLimit }
                )
              }}
            </el-button>
          </div>
        </div>
        <div v-if="options.length" class="metadata-ai-table-list">
          <div class="metadata-ai-table-grid">
            <label v-for="table in options" :key="table.tableName" class="metadata-ai-table">
              <input
                v-model="selected"
                type="checkbox"
                :value="table.tableName"
                :disabled="selected.length >= selectionLimit && !selected.includes(table.tableName)"
              />
              <span class="metadata-ai-table-copy">
                <span class="metadata-ai-table-name" :title="table.tableName">
                  {{ table.tableName }}
                </span>
                <span
                  v-if="table.name && table.name !== table.tableName"
                  class="metadata-ai-table-comment"
                  :title="table.name"
                >
                  {{ table.name }}
                </span>
              </span>
            </label>
          </div>
        </div>
        <el-empty v-else :description="tr('aiNoTables')" />
      </section>
    </div>
    <template #footer>
      <el-button v-if="!task" @click="open = false">{{ tr('aiClose') }}</el-button>
      <el-button
        v-if="!task && !failed"
        type="primary"
        :loading="busy"
        :disabled="selected.length < min"
        @click="submit"
      >
        {{ tr(kind === 'RELATION' ? 'aiDiscoverRelations' : 'aiStart') }}
      </el-button>
    </template>
  </el-drawer>
</template>

<style scoped>
.metadata-ai-content,
.metadata-ai-selection {
  height: 100%;
}

.metadata-ai-selection {
  display: flex;
  flex-direction: column;
}

.metadata-ai-heading,
.metadata-ai-selection-bar {
  flex-shrink: 0;
}

.metadata-ai-table-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior-y: contain;
}

.metadata-ai-subtitle,
.metadata-ai-table-comment,
.metadata-ai-selection-bar {
  font-size: 12px;
  color: var(--ed-text-color-secondary);
}

.metadata-ai-subtitle {
  margin-top: 8px;
}

.metadata-ai-heading {
  margin: 0 0 8px;
  font-size: 14px;
}

.metadata-ai-selection-bar,
.metadata-ai-selection-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 16px;
}

.metadata-ai-selection-bar {
  justify-content: space-between;
  margin-bottom: 12px;

  p {
    margin: 0;
  }
}

.metadata-ai-table-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(100%, 280px), 1fr));
  gap: 8px;
}

.metadata-ai-table {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-height: 52px;
  padding: 8px 10px;
  cursor: pointer;
  border: 1px solid var(--ed-border-color);
  border-radius: 4px;

  &:has(input:checked) {
    border-color: var(--ed-color-primary);
  }

  &:has(input:disabled) {
    cursor: not-allowed;
  }

  input {
    flex: none;
    margin: 3px 0 0;
    accent-color: var(--ed-color-primary);
  }
}

.metadata-ai-table-copy {
  display: grid;
  gap: 2px;
  min-width: 0;
}

.metadata-ai-table-name,
.metadata-ai-table-comment {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metadata-ai-table-name {
  font-family: var(--ed-font-family-monospace, monospace);
  font-size: 13px;
}

:global(.metadata-ai-drawer-header) {
  padding-bottom: 16px;
  margin-bottom: 0;
  border-bottom: 1px solid var(--ed-border-color);
}

:global(.metadata-ai-drawer-body) {
  min-height: 0;
  overflow: hidden;
}

:global(.metadata-ai-drawer-footer) {
  border-top: 1px solid var(--ed-border-color);
}
</style>
