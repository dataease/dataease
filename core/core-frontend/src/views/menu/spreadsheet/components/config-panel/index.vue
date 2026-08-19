<script lang="ts" setup>
import { computed, ref, inject, type Ref } from 'vue'
import { InfoFilled, Expand, Fold } from '@element-plus/icons-vue'
import DataTab from './data-tab.vue'
import StyleTab from './style-tab.vue'
import type { PluginConfig, TablePluginConfig } from '../../types/plugin'
import { PluginAdapterManager, TablePluginAdapter } from '../../types/adapter'

const emit = defineEmits<{
  'queryData': [config: PluginConfig]
  'updateConfig': [key: string, value: any]
}>()
const pluginConfig = inject<Ref<TablePluginConfig>>('pluginConfig')
// 当前激活的 Tab
const activeTab = ref('data')
const collapsed = ref(false)
const panelTitle = computed(() => {
  const type = pluginConfig?.value?.type as string
  if (!type) {
    return ''
  }
  const adapter = PluginAdapterManager.getAdapter(type) as TablePluginAdapter
  if (!adapter) {
    return ''
  }
  return adapter.getPanelTitle(pluginConfig?.value)
})
const toggleCollapsed = () => {
  collapsed.value = !collapsed.value
}
const queryData = config => {
  emit('queryData', config)
}
const updateConfig = (key: string, value: any) => {
  emit('updateConfig', key, value)
}
</script>

<script lang="ts">
export default {
  name: 'ConfigPanel'
}
</script>

<template>
  <div class="config-panel" :class="{ 'is-collapsed': collapsed }">
    <div class="panel-header">
      <div v-if="!collapsed" class="header-left">
        <el-tooltip :content="panelTitle" placement="top">
          <span class="title">{{ panelTitle }}</span>
        </el-tooltip>
        <el-tooltip :content="`${pluginConfig?.placement?.sheetName || ''}!${pluginConfig?.placement?.startCell || ''}`">
          <el-icon class="info-icon"><InfoFilled /></el-icon>
        </el-tooltip>
      </div>
      <div v-else class="header-left header-left-collapsed"></div>
      <el-icon class="menu-icon" @click="toggleCollapsed">
        <Fold v-if="collapsed" />
        <Expand v-else />
      </el-icon>
    </div>

    <template v-if="!collapsed">
      <el-tabs v-model="activeTab" class="config-tabs">
        <el-tab-pane label="数据" name="data">
          <DataTab
            @queryData="queryData"
            @updateConfig="updateConfig"
          />
        </el-tab-pane>
        <el-tab-pane label="样式" name="style">
          <StyleTab @updateConfig="updateConfig" />
        </el-tab-pane>
        <el-tab-pane label="高级" name="senior">
          <div class="placeholder-panel">
            <div class="placeholder-text">高级配置面板待完善</div>
            <div class="placeholder-desc">功能设置、滚动设置等高级选项会在这里配置</div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </template>
    <div v-else class="collapsed-placeholder">
      <span class="collapsed-title">{{ panelTitle }}</span>
    </div>
  </div>
</template>

<style lang="less" scoped>
.config-panel {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 280px;
  background: #fff;
  flex-shrink: 0;
  overflow: hidden;
  transition: width 0.2s ease;

  &.is-collapsed {
    width: 48px;
  }

  .panel-header {
    padding: 12px 16px;
    border-bottom: 1px solid #e8eaef;
    display: flex;
    align-items: center;
    justify-content: space-between;
    box-sizing: border-box;

    .header-left {
      display: flex;
      align-items: center;
      gap: 6px;
      flex: 1;
      min-width: 0;
    }


    .title {
      display: block;
      max-width: 50%;
      flex-shrink: 1;
      min-width: 0;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      font-size: 16px;
      font-weight: 700;
      color: #1d2129;
    }

    .info-icon {
      flex-shrink: 0;
      font-size: 15px;
      color: #9aa0aa;
      cursor: help;
    }

    .menu-icon {
      flex-shrink: 0;
      font-size: 18px;
      color: #6b7280;
      cursor: pointer;
    }
  }

  .collapsed-placeholder {
    flex: 1;
    display: flex;
    align-items: flex-start;
    justify-content: flex-start;
    padding: 12px 0 12px 10px;
  }

  .collapsed-title {
    writing-mode: vertical-rl;
    text-orientation: upright;
    font-size: 16px;
    line-height: 1;
    letter-spacing: 2px;
    font-weight: 700;
    color: #1d2129;
  }

  .config-tabs {
    flex: 1;
    display: flex;
    overflow: hidden;

    :deep(.ed-tabs__header) {
      margin: 0;
      border-bottom: 1px solid #e8eaef;
    }

    :deep(.ed-tabs__nav-wrap) {
      padding: 0 14px;
    }

    :deep(.ed-tabs__nav-scroll) {
      display: flex;
      justify-content: flex-start;
    }

    :deep(.ed-tabs__item) {
      height: 56px;
      line-height: 56px;
      padding: 0 12px;
      font-size: 16px;
      font-weight: 600;
      color: #4b5563;
      transition: color 0.2s;

      &:hover {
        color: #3370ff;
      }

      &.is-active {
        color: #3370ff;
        font-weight: 700;
      }
    }

    :deep(.ed-tabs__active-bar) {
      height: 4px;
      background-color: #3370ff;
      border-radius: 999px;
    }

    :deep(.ed-tabs__content) {
      flex: 1;
      overflow: hidden;
      padding: 0;
    }

    :deep(.ed-tab-pane) {
      height: 100%;
      overflow: hidden;
    }
  }

  .placeholder-panel {
    height: 100%;
    padding: 32px 24px;
  }

  .placeholder-text {
    font-size: 15px;
    color: #4b5563;
    text-align: center;
    padding: 40px 0 12px;
    font-weight: 600;
  }

  .placeholder-desc {
    font-size: 13px;
    line-height: 20px;
    color: #9ca3af;
    text-align: center;
    padding: 0 8px;
  }
}
</style>
