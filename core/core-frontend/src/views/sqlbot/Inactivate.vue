<template>
  <div class="inactivate-div">
    <el-dropdown @command="handleCommand">
      <div class="inactivate-button el-dropdown-link" :style="{ color: fontColor }">
        API 功能
        <img class="api-img" src="/api.png" alt="inactivate" />
        <el-icon class="el-icon--right">
          <arrow-down />
        </el-icon>
      </div>
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="showHistory">历史记录(显/隐)</el-dropdown-item>
          <el-dropdown-item command="newChat" divided>新建对话</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </div>
</template>

<script setup lang="ts">
import { ElDropdown, ElDropdownMenu, ElDropdownItem, ElIcon } from 'element-plus-secondary'
import { ArrowDown } from '@element-plus/icons-vue'

defineProps({
  fontColor: {
    type: String,
    default: '#000000'
  }
})

const emit = defineEmits(['showHistory', 'newChat'])
const handleCommand = (command: string) => {
  if (command === 'showHistory') {
    emit('showHistory')
  } else if (command === 'newChat') {
    emit('newChat')
  }
}
</script>

<style scoped>
.inactivate-div {
  position: absolute;
  right: 32px;
  top: 92px;
  height: auto;
  padding: 8px 16px;
  display: flex;
  border-radius: 4px;
}
.inactivate-button {
  display: flex;
  align-items: center;
  cursor: pointer;
  font-size: 14px;
}
.api-img {
  width: 24px;
  height: 24px;
  margin: 0 4px;
}
</style>
