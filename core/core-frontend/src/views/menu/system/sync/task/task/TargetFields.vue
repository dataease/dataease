<script setup lang="ts">
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { Icon } from '@/components/icon-custom'
import iconEdit from '@/assets/svg/icon_edit_outlined.svg'
import iconDelete from '@/assets/svg/icon_delete-trash_outlined.svg'
import type { ITableField } from '@/api/sync/syncTask'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
/**
 * 原任务配置和独立字段调整共用字段表格，差异、保护和错误由调用方提供，组件不决定数据库能力
 */
const props = withDefaults(
  defineProps<{
    rows: ITableField[]
    columns: { model: string; label: string; type: string }[]
    pagination: { currentPage: number; pageSize: number; total: number }
    indexLabel: string
    optionDisabled: (row: ITableField, option: 'fieldPk' | 'fieldIndex') => boolean
    optionHint: (row: ITableField, option: 'fieldPk' | 'fieldIndex') => string
    editable?: (row: ITableField) => boolean
    status?: (row: ITableField) => string
    deleted?: (row: ITableField) => boolean
    showActions?: boolean
    compact?: boolean
    sourceType?: (row: ITableField) => string
    targetType?: (row: ITableField) => string
    actualTargetType?: (row: ITableField) => string
    protectedHint?: (row: ITableField) => string
    deletable?: (row: ITableField) => boolean
    fieldErrors?: (row: ITableField) => string[]
  }>(),
  { showActions: true }
)
const emit = defineEmits<{
  (event: 'edit' | 'delete' | 'undo', row: ITableField): void
  (event: 'selection', rows: ITableField[]): void
  (event: 'page' | 'size', value: number): void
}>()
const selectable = (row: ITableField) =>
  (!props.editable || props.editable(row)) && !props.deleted?.(row)
const value = (row: ITableField, model: string) => row[model as keyof ITableField]
const setOption = (row: ITableField, option: string, checked: boolean) => {
  if (option === 'fieldPk' || option === 'fieldIndex') row[option] = checked
}
/**
 * 字段问题同时通过整行背景和原因文字展示，翻页后仍按字段标识定位
 */
const rowClassName = ({ row }: { row: ITableField }) =>
  props.fieldErrors?.(row).length ? 'field-validation-error' : ''
</script>

<template>
  <GridTable
    :table-data="rows"
    :pagination="pagination"
    :show-pagination="pagination.total > 10"
    class="popper-max-width target-fields"
    :class="{ 'target-fields-compact': compact }"
    :row-class-name="rowClassName"
    @selection-change="emit('selection', $event)"
    @current-change="emit('page', $event)"
    @size-change="emit('size', $event)"
  >
    <el-table-column
      v-if="showActions !== false && !compact"
      type="selection"
      width="30"
      :selectable="selectable"
    />
    <el-table-column
      v-if="compact"
      :label="t('sync_maintenance.source_field_type')"
      min-width="180"
    >
      <template #default="{ row }">
        <div :class="{ 'pending-delete': deleted?.(row) }">
          {{ row.fieldSource || t('sync_maintenance.generated_field') }}
        </div>
        <small v-if="row.fieldSource" class="field-type">{{ sourceType(row) }}</small>
      </template>
    </el-table-column>
    <el-table-column
      v-if="compact"
      :label="t('sync_maintenance.target_field_type')"
      min-width="200"
    >
      <template #default="{ row }">
        <div :class="{ 'pending-delete': deleted?.(row) }">
          {{ row.fieldName }}
          <el-tag v-if="status?.(row)" size="small" type="warning" class="change-tag">{{
            status(row)
          }}</el-tag>
        </div>
        <small class="field-type"
          >{{ actualTargetType ? t('sync_maintenance.saved_definition') + '：' : ''
          }}{{ targetType(row) }}{{ row.fieldIndex ? ` · ${indexLabel}` : '' }}</small
        >
        <small v-if="actualTargetType" class="field-type"
          >{{ t('sync_maintenance.actual_definition') }}：{{ actualTargetType(row) }}</small
        >
        <small v-if="protectedHint(row)" class="field-type">{{ protectedHint(row) }}</small>
        <small v-for="message in fieldErrors?.(row)" :key="message" class="field-error">{{
          message
        }}</small>
      </template>
    </el-table-column>
    <el-table-column
      v-if="compact"
      prop="remarks"
      :label="t('sync_task.field_comment')"
      min-width="130"
    />
    <template v-if="!compact">
      <el-table-column
        v-for="column in columns"
        :key="column.model"
        :label="column.model === 'fieldIndex' ? indexLabel : column.label"
        :prop="column.model"
        show-overflow-tooltip
        min-width="90"
      >
        <template #default="{ row }">
          <el-checkbox
            v-if="column.model === 'fieldPk' || column.model === 'fieldIndex'"
            :model-value="!!value(row, column.model)"
            :disabled="optionDisabled(row, column.model) || !selectable(row)"
            :title="optionHint(row, column.model)"
            @change="setOption(row, column.model, !!$event)"
          />
          <span v-else :class="{ 'pending-delete': deleted?.(row) }">
            {{ value(row, column.model) === 0 ? '-' : value(row, column.model) }}
          </span>
        </template>
      </el-table-column>
    </template>
    <el-table-column
      v-if="status && !compact"
      :label="t('sync_maintenance.change')"
      min-width="110"
    >
      <template #default="{ row }">{{ status(row) }}</template>
    </el-table-column>
    <el-table-column
      v-if="showActions !== false"
      :label="t('common.operate')"
      width="120"
      fixed="right"
    >
      <template #default="{ row }">
        <el-button v-if="deleted?.(row)" link type="primary" @click="emit('undo', row)">
          {{ t('sync_maintenance.undo') }}
        </el-button>
        <div v-else class="operate-icon-container">
          <el-button
            text
            :disabled="!selectable(row)"
            :title="t('common.edit')"
            :aria-label="t('common.edit')"
            @click="emit('edit', row)"
          >
            <template #icon>
              <Icon name="icon_edit_outlined"><icon-edit class="svg-icon" /></Icon>
            </template>
          </el-button>
          <el-button
            text
            :disabled="deletable ? !deletable(row) : !selectable(row)"
            :title="t('common.delete')"
            :aria-label="t('common.delete')"
            @click="emit('delete', row)"
          >
            <template #icon>
              <Icon name="icon_delete-trash_outlined"><icon-delete class="svg-icon" /></Icon>
            </template>
          </el-button>
        </div>
      </template>
    </el-table-column>
  </GridTable>
</template>

<style scoped lang="less">
.target-fields {
  :deep(.cell) {
    overflow-wrap: anywhere;
    line-height: 22px;
  }
}
.target-fields-compact {
  height: auto;
}
.field-type {
  display: block;
  margin-top: 4px;
  color: var(--ed-text-color-secondary);
  font-size: 12px;
  white-space: normal;
  overflow-wrap: anywhere;
  line-height: 20px;
}
.change-tag {
  margin-left: 8px;
}
.pending-delete {
  text-decoration: line-through;
  opacity: 0.6;
}
.field-error {
  display: block;
  margin-top: 6px;
  color: var(--ed-color-danger);
  font-size: 12px;
  line-height: 20px;
}
.target-fields :deep(.field-validation-error) {
  --ed-table-tr-bg-color: var(--ed-color-danger-light-9);
}
</style>
