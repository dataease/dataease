<script setup lang="ts">
import { computed, inject, type Ref } from 'vue'
import { Expand, Fold } from '@element-plus/icons-vue'
import { ref } from 'vue'
import FilterStyleEditor from './FilterStyleEditor.vue'
import type { SpreadsheetFilterConfig } from '../../../../types/plugin'
import {
  dispatchSpreadsheetFilterConfigChange,
  getSpreadsheetFilterConfig
} from '../../utils/events'

const pluginConfig = inject<Ref<SpreadsheetFilterConfig>>('pluginConfig')
if (!pluginConfig) {
  throw new Error('FilterEditor requires pluginConfig to be provided')
}

const collapsed = ref(false)
const currentConfig = computed(() => pluginConfig.value)

const updatePluginConfig = (key: string, value: any) => {
  const latestConfig = getSpreadsheetFilterConfig()
  if (latestConfig && latestConfig.id === pluginConfig.value.id) {
    // 配置弹窗可能在样式面板打开期间替换配置对象，写样式前必须合并到最新条件列表。
    pluginConfig.value = latestConfig
  }
  const keys = key.split('.')
  let target: any = pluginConfig.value
  for (let i = 0; i < keys.length - 1; i++) {
    if (!target[keys[i]]) {
      target[keys[i]] = {}
    }
    target = target[keys[i]]
  }
  target[keys[keys.length - 1]] = value
  dispatchSpreadsheetFilterConfigChange(pluginConfig.value)
}

const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}
</script>

<template>
  <div class="filter-editor" :class="{ 'is-collapsed': collapsed }">
    <div class="filter-editor__header">
      <div v-if="!collapsed" class="filter-editor__title">
        <span>查询组件</span>
      </div>
      <div v-else class="filter-editor__title is-empty"></div>
      <el-icon class="filter-editor__collapse" @click="toggleCollapsed">
        <Fold v-if="collapsed" />
        <Expand v-else />
      </el-icon>
    </div>

    <div v-if="!collapsed" class="filter-editor__content">
      <FilterStyleEditor :config="currentConfig" @updateConfig="updatePluginConfig" />
    </div>

    <div v-else class="filter-editor__collapsed-title">查询组件</div>
  </div>
</template>

<style scoped lang="less">
.filter-editor {
  width: 240px;
  min-width: 240px;
  max-width: 240px;
  height: 100%;
  background: #fff;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.2s ease;

  &.is-collapsed {
    width: 48px;
    min-width: 48px;
    max-width: 48px;
  }

  &__header {
    height: 48px;
    padding: 0 8px;
    border-bottom: 1px solid #e8eaef;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;
  }

  &__title {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 6px;
    color: #1d2129;
    font-size: 14px;
    font-weight: 500;

    &.is-empty {
      flex: 1;
    }
  }

  &__collapse {
    color: #6b7280;
    cursor: pointer;
  }

  &__content {
    flex: 1;
    min-height: 0;
    width: 100%;
    min-width: 0;
    overflow: hidden auto;
  }

  &__collapsed-title {
    writing-mode: vertical-rl;
    text-orientation: upright;
    padding: 12px 0 0 10px;
    color: #1d2129;
    font-size: 15px;
    font-weight: 700;
    letter-spacing: 2px;
  }
}
</style>
