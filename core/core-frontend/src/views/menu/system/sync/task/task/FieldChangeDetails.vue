<script setup lang="ts">
import { computed } from 'vue'
import type { ITableField } from '@/api/sync/syncTask'
import { useI18n } from '@/hooks/web/useI18n'

const props = defineProps<{
  changes: { before?: ITableField; after?: ITableField }[]
  mappingNotes?: Record<string, string>
  afterLabel?: string
}>()
const { t } = useI18n()
const attributes = [
  ['fieldSource', 'source_field'],
  ['fieldName', 'name'],
  ['fieldType', 'field_type'],
  ['fieldSize', 'field_length'],
  ['fieldPrecision', 'field_precision'],
  ['fieldIndex', 'field_index'],
  ['remarks', 'field_comment']
] as const
/**
 * 按字段类型展示有意义的属性，缺失值仅用于展示，不参与配置恢复
 */
const value = (field: ITableField | undefined, key: (typeof attributes)[number][0]) => {
  if (!field) return '-'
  if (key === 'fieldSize' && ['DECIMAL', 'DECIMALV3'].includes(field.fieldType) && !field.fieldSize)
    return t('sync_maintenance.precision_unspecified')
  if (key === 'fieldIndex')
    return t(field.fieldIndex ? 'sync_maintenance.index_enabled' : 'sync_maintenance.index_none')
  if (key === 'fieldSize' && !['CHAR', 'VARCHAR', 'DECIMAL', 'DECIMALV3'].includes(field.fieldType))
    return '-'
  if (
    key === 'fieldPrecision' &&
    !['DECIMAL', 'DECIMALV3', 'DATETIME', 'DATETIMEV2'].includes(field.fieldType)
  )
    return '-'
  return String(field[key] ?? '') || '-'
}
/**
 * 新增和删除各作为一次操作展示字段定义，只有修改操作逐项比较，避免将删除注释标成修改
 */
const groups = computed(() =>
  props.changes.map(change => {
    const action = !change.before ? 'add' : !change.after ? 'delete' : 'update'
    const field = change.after || change.before
    const rows =
      action === 'update'
        ? attributes.flatMap(([key, label]) => {
            const before = value(change.before, key)
            const after = value(change.after, key)
            if (
              key === 'remarks'
                ? (change.before?.remarks || '') === (change.after?.remarks || '')
                : before === after
            )
              return []
            return [
              {
                key,
                attribute: t(`sync_task.${label}`),
                before,
                after:
                  key === 'fieldIndex'
                    ? t(
                        change.after.fieldIndex
                          ? 'sync_maintenance.index_add'
                          : 'sync_maintenance.index_remove'
                      )
                    : after
              }
            ]
          })
        : []
    return {
      name: change.before?.fieldName || change.after.fieldName,
      targetName: change.after?.fieldName,
      type: field.fieldType,
      action,
      rows: rows.filter(row => row.key !== 'remarks'),
      comment: rows.find(row => row.key === 'remarks'),
      definition:
        action === 'update'
          ? []
          : attributes.flatMap(([key, label]) => {
              const content = value(field, key)
              return key === 'fieldName' ||
                (['fieldSize', 'fieldPrecision'].includes(key) && content === '-') ||
                (key === 'remarks' && !field.remarks) ||
                (key === 'fieldSource' && !field.fieldSource)
                ? []
                : [{ key, label: t(`sync_task.${label}`), content }]
            }),
      count: rows.length
    }
  })
)
const changeCount = computed(() =>
  groups.value.reduce((count, group) => count + (group.action === 'update' ? group.count : 1), 0)
)
</script>

<template>
  <div class="field-change-details">
    <div class="changes-heading">
      <span>{{ t('sync_maintenance.changes_title') }}</span>
      <span class="secondary">{{
        t('sync_maintenance.change_summary', [groups.length, changeCount])
      }}</span>
    </div>
    <details v-for="group in groups" :key="group.name" class="field-change-group">
      <summary class="field-change-heading">
        <span class="field-change-name">
          <span
            >{{ group.name }} <span class="secondary">{{ group.type }}</span></span
          >
          <span v-if="group.action === 'delete'" class="delete-note">{{
            t('sync_maintenance.deleted_field_effect')
          }}</span>
        </span>
        <span class="change-tag" :class="group.action">{{
          group.action === 'update'
            ? t('sync_maintenance.field_change_count', [group.count])
            : t(`sync_maintenance.action_${group.action}`)
        }}</span>
      </summary>
      <div class="field-change-content">
        <dl v-if="group.action !== 'update'" class="field-definition">
          <template v-for="item in group.definition" :key="item.key">
            <dt>{{ item.label }}</dt>
            <dd>{{ item.content }}</dd>
          </template>
        </dl>
        <table v-if="group.rows.length" class="field-change-table" :aria-label="group.name">
          <colgroup>
            <col class="attribute-column" />
            <col />
            <col />
          </colgroup>
          <thead>
            <tr>
              <th>{{ t('sync_maintenance.changed_attribute') }}</th>
              <th>{{ t('sync_maintenance.before') }}</th>
              <th>{{ afterLabel || t('sync_maintenance.after') }}</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="row in group.rows" :key="row.key">
              <td class="secondary">{{ row.attribute }}</td>
              <td class="secondary">{{ row.before }}</td>
              <td>{{ row.after }}</td>
            </tr>
          </tbody>
        </table>
        <details v-if="group.comment" class="comment-change">
          <summary>
            <span>{{ t('sync_task.field_comment') }}</span>
            <span class="secondary">{{ t('sync_maintenance.comment_modified') }}</span>
            <span class="comment-toggle"
              ><span class="comment-open">{{ t('sync_maintenance.view_comparison') }}</span
              ><span class="comment-close">{{ t('sync_maintenance.hide_comparison') }}</span></span
            >
          </summary>
          <div class="comment-comparison">
            <div>
              <span class="secondary">{{ t('sync_maintenance.before') }}</span>
              <p>{{ group.comment.before }}</p>
            </div>
            <div>
              <span class="secondary">{{ afterLabel || t('sync_maintenance.after') }}</span>
              <p>{{ group.comment.after }}</p>
            </div>
          </div>
        </details>
        <p v-if="group.targetName && mappingNotes?.[group.targetName]" class="mapping-note">
          {{ mappingNotes[group.targetName] }}
        </p>
      </div>
    </details>
  </div>
</template>

<style scoped lang="less">
.field-change-details {
  line-height: 22px;
  overflow-wrap: anywhere;
}
.secondary {
  color: var(--ed-text-color-secondary);
  font-size: 12px;
}
.changes-heading {
  display: flex;
  flex-wrap: wrap;
  justify-content: space-between;
  gap: 8px 12px;
  margin-bottom: 12px;
}
.field-change-group {
  border: 1px solid var(--ed-border-color);
  border-radius: 6px;
  & + & {
    margin-top: 12px;
  }
}
.field-change-heading {
  display: flex;
  align-items: flex-start;
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
.field-change-group[open] > .field-change-heading {
  background: var(--ed-fill-color-light);
  border-radius: 6px 6px 0 0;
  &::before {
    transform: rotate(90deg);
  }
}
.field-change-name {
  flex: 1;
  min-width: 0;
  .secondary {
    margin-left: 8px;
  }
}
.delete-note {
  display: block;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  margin-top: 4px;
}
.change-tag {
  flex-shrink: 0;
  padding: 0 8px;
  border-radius: 4px;
  font-size: 12px;
  color: var(--ed-color-primary);
  background: var(--ed-color-primary-light-9);
  &.delete {
    color: var(--ed-color-danger);
    background: var(--ed-color-danger-light-9);
  }
  &.add {
    color: var(--ed-color-success);
    background: var(--ed-color-success-light-9);
  }
}
.field-change-content {
  border-top: 1px solid var(--ed-border-color);
}
.field-definition {
  display: grid;
  grid-template-columns: 80px minmax(0, 1fr);
  gap: 10px 16px;
  margin: 0;
  padding: 14px;
  dt {
    color: var(--ed-text-color-secondary);
    font-size: 12px;
  }
  dd {
    margin: 0;
    white-space: pre-wrap;
  }
}
.field-change-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
  .attribute-column {
    width: 90px;
  }
  th,
  td {
    padding: 8px 14px;
    text-align: left;
    vertical-align: top;
    font-weight: normal;
  }
  th {
    font-size: 12px;
    color: var(--ed-text-color-secondary);
  }
  td {
    font-size: 14px;
  }
  tbody tr {
    border-top: 1px solid var(--ed-border-color);
  }
}
.comment-change {
  summary {
    display: flex;
    flex-wrap: wrap;
    align-items: baseline;
    gap: 8px 16px;
    padding: 12px 14px;
    cursor: pointer;
    list-style: none;
    &::-webkit-details-marker {
      display: none;
    }
  }
  .comment-toggle {
    margin-left: auto;
    color: var(--ed-color-primary);
    font-size: 12px;
  }
  .comment-close {
    display: none;
  }
  &[open] {
    .comment-open {
      display: none;
    }
    .comment-close {
      display: inline;
    }
  }
}
.comment-comparison {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(0, 1fr);
  gap: 16px;
  padding: 0 14px 14px;
  p {
    margin: 4px 0 0;
    white-space: pre-wrap;
  }
}
.mapping-note {
  margin: 0;
  padding: 12px 14px;
  border-top: 1px solid var(--ed-border-color);
  font-size: 12px;
  color: var(--ed-text-color-secondary);
}
@media (max-width: 480px) {
  .field-change-table {
    .attribute-column {
      width: 64px;
    }
    th,
    td {
      padding-left: 8px;
      padding-right: 8px;
    }
  }
  .comment-comparison {
    grid-template-columns: 1fr;
  }
  .field-change-name .secondary {
    display: block;
    margin-left: 0;
  }
}
</style>
