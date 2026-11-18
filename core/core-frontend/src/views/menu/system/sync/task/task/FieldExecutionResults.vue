<script setup lang="ts">
import { computed } from 'vue'
import type { IMaintenancePlan } from '@/api/sync/syncTask'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{
  steps: IMaintenancePlan['steps']
  summarizeError: (message: string) => string
}>()
const { t } = useI18n()
/**
 * 同一字段改名前后的步骤归为一组，结果仍按步骤显示，避免将部分完成的字段误报为全部成功
 */
const groups = computed(() => {
  const result: { id: string; name: string; steps: IMaintenancePlan['steps'] }[] = []
  const aliases = new Map<string, (typeof result)[number]>()
  for (const step of props.steps) {
    // 历史步骤没有字段快照时按步骤编号单列，不丢弃实际执行结果
    const name = step.before?.fieldName || step.after?.fieldName || step.id
    let group = step.action === 'ADD' ? undefined : aliases.get(name)
    if (!group) {
      group = { id: step.id, name, steps: [] }
      result.push(group)
    }
    group.steps.push(step)
    aliases.set(name, group)
    if (step.action === 'RENAME' && step.after?.fieldName) {
      aliases.delete(name)
      aliases.set(step.after.fieldName, group)
      group.name = `${group.name} → ${step.after.fieldName}`
    } else if (step.action === 'DROP') aliases.delete(name)
  }
  return result.map(group => {
    const states = group.steps.map(step => step.state)
    const state = states.includes('UNKNOWN')
      ? 'UNKNOWN'
      : states.includes('FAILED')
      ? 'FAILED'
      : states.some(value => ['SUBMITTING', 'WAITING'].includes(value))
      ? 'WAITING'
      : states.every(value => value === 'DONE')
      ? 'DONE'
      : states.every(value => value === 'SKIPPED')
      ? 'SKIPPED'
      : states.includes('DONE')
      ? 'PARTIAL'
      : 'PLANNED'
    return { ...group, state }
  })
})
const summary = computed(() =>
  [
    ['completed_count', props.steps.filter(step => step.state === 'DONE').length],
    ['failed_count', props.steps.filter(step => step.state === 'FAILED').length],
    [
      'not_executed_count',
      props.steps.filter(step => ['PLANNED', 'SKIPPED'].includes(step.state)).length
    ],
    [
      'unconfirmed_count',
      props.steps.filter(step => ['SUBMITTING', 'WAITING', 'UNKNOWN'].includes(step.state)).length
    ]
  ]
    .filter(([, count]) => count)
    .map(([key, count]) => t(`sync_maintenance.${key}`, [count]))
    .join(' · ')
)
/**
 * 类型步骤展示实际前值和计划后值，执行状态单独标明，不把计划值当作当前已生效配置
 */
const stepDetail = (step: IMaintenancePlan['steps'][number]) => {
  if (!step.before || !step.after) return ''
  if (step.action === 'RENAME') return `${step.before.fieldName} → ${step.after.fieldName}`
  if (step.action !== 'TYPE') return ''
  return [step.before, step.after]
    .map(field => {
      if (['CHAR', 'VARCHAR', 'DECIMAL', 'DECIMALV3'].includes(field.fieldType)) {
        return `${field.fieldType}(${field.fieldSize}${
          field.fieldType.startsWith('DECIMAL') ? `, ${field.fieldPrecision}` : ''
        })`
      }
      if (['DATETIME', 'DATETIMEV2'].includes(field.fieldType))
        return `${field.fieldType}(${field.fieldPrecision || 0})`
      return field.fieldType
    })
    .join(' → ')
}
</script>

<template>
  <section class="field-execution-results">
    <div class="execution-heading">
      <span>{{ t('sync_maintenance.execution_result') }}</span
      ><span class="secondary">{{ summary }}</span>
    </div>
    <details
      v-for="group in groups"
      :key="group.id"
      class="execution-field"
      :open="['FAILED', 'UNKNOWN'].includes(group.state)"
    >
      <summary>
        <span class="execution-name">{{ group.name }}</span
        ><span class="execution-state" :class="group.state.toLowerCase()">{{
          t(`sync_maintenance.step_${group.state.toLowerCase()}`)
        }}</span>
      </summary>
      <div class="execution-steps">
        <div v-for="step in group.steps" :key="step.id" class="execution-step">
          <div class="execution-step-heading">
            <div>
              <span>{{ t(`sync_maintenance.action_${step.action.toLowerCase()}`) }}</span
              ><span v-if="stepDetail(step)" class="step-detail secondary">{{
                stepDetail(step)
              }}</span>
            </div>
            <span class="execution-state" :class="step.state.toLowerCase()">{{
              t(`sync_maintenance.step_${step.state.toLowerCase()}`)
            }}</span>
          </div>
          <p v-if="step.error" class="execution-error">{{ summarizeError(step.error) }}</p>
        </div>
      </div>
    </details>
  </section>
</template>

<style scoped lang="less">
.field-execution-results {
  line-height: 22px;
  overflow-wrap: anywhere;
}
.execution-heading {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px 16px;
  margin-bottom: 12px;
}
.secondary {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
}
.execution-field {
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
  & + & {
    margin-top: 12px;
  }
}
.execution-field > summary {
  display: flex;
  align-items: baseline;
  gap: 10px;
  padding: 12px 14px;
  cursor: pointer;
  list-style: none;
  &::-webkit-details-marker {
    display: none;
  }
  &::before {
    content: '▸';
    color: var(--ed-text-color-secondary);
    flex-shrink: 0;
  }
}
.execution-field[open] > summary {
  background: var(--ed-fill-color-light);
  border-radius: 6px 6px 0 0;
  &::before {
    transform: rotate(90deg);
  }
}
.execution-name {
  flex: 1;
  min-width: 0;
}
.execution-state {
  flex-shrink: 0;
  font-size: 12px;
  color: var(--ed-text-color-secondary);
  &.done {
    color: var(--ed-color-success);
  }
  &.failed {
    color: var(--ed-color-danger);
  }
  &.unknown,
  &.partial {
    color: var(--ed-color-warning);
  }
}
.execution-steps {
  padding: 0 14px;
  border-top: 1px solid var(--ed-border-color);
}
.execution-step {
  padding: 12px 0;
  & + & {
    border-top: 1px solid var(--ed-border-color);
  }
}
.execution-step-heading {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  > div {
    min-width: 0;
  }
}
.step-detail {
  display: block;
  margin-top: 4px;
}
.execution-error {
  margin: 10px 0 0;
  padding: 10px 12px;
  color: var(--ed-color-danger);
  background: var(--ed-color-danger-light-9);
  border-radius: 4px;
}
</style>
