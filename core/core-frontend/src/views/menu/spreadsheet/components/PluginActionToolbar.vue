<script lang="ts" setup>
import { computed, ref } from 'vue'
import { Delete, DocumentCopy, Edit, MoreFilled, Scissor } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus-secondary'
import { useEmitt } from '@/hooks/web/useEmitt'
import { useI18n } from '@/hooks/web/useI18n'
import { SPREADSHEET_EVENTS } from '../utils/events'
import type { PluginActionToolbarPayload } from '../types/editor'

const { t } = useI18n()
const { emitter } = useEmitt()

const payload = ref<PluginActionToolbarPayload>()
const hoveringToolbar = ref(false)
const dropdownVisible = ref(false)
const hideTimer = ref<number>()

const visible = computed(() => !!payload.value)
const disabledActions = computed(() => payload.value?.disabledActions ?? [])
const copyDisabled = computed(() => disabledActions.value.includes('copy'))
const cutDisabled = computed(() => disabledActions.value.includes('cut'))
const toolbarStyle = computed(() => {
  const position = payload.value?.position
  return {
    left: `${position?.left ?? 0}px`,
    top: `${position?.top ?? 0}px`
  }
})

const clearHideTimer = () => {
  if (hideTimer.value) {
    window.clearTimeout(hideTimer.value)
    hideTimer.value = undefined
  }
}

const hide = () => {
  clearHideTimer()
  dropdownVisible.value = false
  payload.value = undefined
}

useEmitt({
  name: SPREADSHEET_EVENTS.SHOW_PLUGIN_ACTION_TOOLBAR,
  callback: (nextPayload: PluginActionToolbarPayload) => {
    clearHideTimer()
    payload.value = nextPayload
  }
})

useEmitt({
  name: SPREADSHEET_EVENTS.HIDE_PLUGIN_ACTION_TOOLBAR,
  callback: () => {
    clearHideTimer()
    hideTimer.value = window.setTimeout(() => {
      if (!hoveringToolbar.value && !dropdownVisible.value) {
        hide()
      }
    }, 120)
  }
})

const handleMouseEnter = () => {
  hoveringToolbar.value = true
  clearHideTimer()
}

const handleMouseLeave = () => {
  hoveringToolbar.value = false
  emitter.emit(SPREADSHEET_EVENTS.HIDE_PLUGIN_ACTION_TOOLBAR)
}

const handleEdit = () => {
  if (!payload.value?.config) {
    return
  }
  emitter.emit(SPREADSHEET_EVENTS.OPEN_PLUGIN_EDITOR, {
    config: payload.value.config,
    isNewSheet: false
  })
}

const handleDelete = async () => {
  const current = payload.value
  if (!current) {
    return
  }

  try {
    await ElMessageBox.confirm(
      '确定删除该组件吗？删除后不可恢复。',
      '删除组件',
      {
        confirmButtonText: t('commons.confirm'),
        cancelButtonText: t('commons.cancel'),
        type: 'warning'
      }
    )
  } catch {
    return
  }

  emitter.emit(SPREADSHEET_EVENTS.DELETE_PLUGIN_RENDER, current)
  hide()
}

const handleDropdownVisibleChange = (visible: boolean) => {
  dropdownVisible.value = visible
  if (visible) {
    clearHideTimer()
    return
  }
  if (!hoveringToolbar.value) {
    emitter.emit(SPREADSHEET_EVENTS.HIDE_PLUGIN_ACTION_TOOLBAR)
  }
}

const handleCopy = () => {
  if (!payload.value || copyDisabled.value) {
    return
  }
  emitter.emit(SPREADSHEET_EVENTS.COPY_PLUGIN_TABLE, payload.value)
  hide()
}

const handleCut = () => {
  if (!payload.value || cutDisabled.value) {
    return
  }
  emitter.emit(SPREADSHEET_EVENTS.CUT_PLUGIN_TABLE, payload.value)
  hide()
}

</script>

<template>
  <div
    v-if="visible"
    class="plugin-action-toolbar"
    :style="toolbarStyle"
    @mouseenter="handleMouseEnter"
    @mouseleave="handleMouseLeave"
    @mousedown.stop
    @click.stop
  >
    <button class="toolbar-button" type="button" title="编辑" @click="handleEdit">
      <el-icon><Edit /></el-icon>
    </button>
    <button class="toolbar-button" type="button" title="删除" @click="handleDelete">
      <el-icon><Delete /></el-icon>
    </button>
    <el-dropdown
      trigger="click"
      placement="right-start"
      popper-class="plugin-action-toolbar-dropdown"
      @visible-change="handleDropdownVisibleChange"
    >
      <button class="toolbar-button" type="button" title="更多">
        <el-icon><MoreFilled /></el-icon>
      </button>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item :disabled="copyDisabled" @click="handleCopy">
            <el-icon><DocumentCopy /></el-icon>
            复制表格
          </el-dropdown-item>
          <el-dropdown-item :disabled="cutDisabled" @click="handleCut">
            <el-icon><Scissor /></el-icon>
            剪切表格
          </el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<style lang="less" scoped>
.plugin-action-toolbar {
  position: absolute;
  z-index: 260;
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 4px;
  border-radius: 6px;
  background: #3370ff;
  box-shadow: 0 4px 12px rgba(31, 35, 41, 0.18);
}

.toolbar-button {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: 0;
  border-radius: 4px;
  color: #fff;
  background: transparent;
  cursor: pointer;

  .el-icon {
    font-size: 16px;
  }

  &:hover {
    background: rgba(31, 35, 41, 0.1);
  }
}
</style>

<style lang="less">
.plugin-action-toolbar-dropdown {
  .el-dropdown-menu__item {
    gap: 8px;

    .el-icon {
      font-size: 14px;
    }
  }
}
</style>
