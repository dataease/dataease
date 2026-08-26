<script lang="ts" setup>
import { Delete, Edit } from '@element-plus/icons-vue'
import type { FieldItemData } from '../../types/plugin'

const props = defineProps<{
  field: FieldItemData
  index: number
}>()

const emit = defineEmits<{
  remove: [index: number]
  rename: [field: FieldItemData, index: number]
}>()

const handleCommand = (command: string) => {
  if (command === 'rename') {
    emit('rename', props.field, props.index)
  } else if (command === 'remove') {
    emit('remove', props.index)
  }
}

const getFieldIcon = (groupType: string) => {
  return groupType === 'd' ? 'T' : '#'
}

const getFieldColor = (groupType: string) => {
  return groupType === 'd' ? 'var(--ed-color-primary, #3370ff)' : '#34c724'
}
</script>

<template>
  <el-dropdown trigger="click" @command="handleCommand">
    <span class="drop-zone-field-item">
      <div class="field-content">
        <span class="field-icon" :style="{ color: getFieldColor(field.groupType) }">
          {{ getFieldIcon(field.groupType) }}
        </span>
        <el-tooltip placement="top">
          <template #content>
            <div>字段名: {{ field.name }}</div>
            <div>显示名称: {{ field.chartShowName || field.name }}</div>
          </template>
          <span class="field-name" :title="field.chartShowName || field.name">
            {{ field.chartShowName || field.name }}
          </span>
        </el-tooltip>
      </div>
      <div class="field-actions child">
        <span class="action-btn delete-btn" @click.stop="emit('remove', index)">
          <el-icon><Delete /></el-icon>
        </span>
      </div>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item command="rename">
          <el-icon><Edit /></el-icon>
          <span>编辑显示名称</span>
        </el-dropdown-item>
        <el-dropdown-item command="remove" divided>
          <el-icon class="delete-icon"><Delete /></el-icon>
          <span class="delete-text">删除</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style scoped lang="less">
.drop-zone-field-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 8px;
  cursor: pointer;
  width: 100%;
  box-sizing: border-box;

  .field-content {
    display: flex;
    align-items: center;
    gap: 8px;
    flex: 1;
    overflow: hidden;

    .field-icon {
      font-size: 14px;
      font-weight: 500;
      flex-shrink: 0;
      width: 16px;
      text-align: center;
    }

    .field-name {
      font-size: 13px;
      color: #1f2329;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .field-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    visibility: hidden;

    .action-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 20px;
      height: 20px;
      cursor: pointer;
      color: #8f959e;
      border-radius: 4px;
      transition: all 0.2s;

      &:hover {
        background: rgba(0, 0, 0, 0.05);
        color: #f54a45;
      }

      .ed-icon {
        font-size: 14px;
      }
    }
  }

  &:hover .field-actions {
    visibility: visible;
  }
}
</style>
