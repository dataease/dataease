<template>
  <div class="sidebar-container">
    <!-- 第一个左侧边栏 -->
    <div class="left-sidebar first-sidebar" :class="{ collapsed: firstSidebarCollapsed }">
      <div class="sidebar-header">
        <span :class="{'collapsed-header': firstSidebarCollapsed}">数据集</span>
        <button
          class="toggle-btn first-toggle" 
          @click="toggleFirstSidebar"
          :title="firstSidebarCollapsed ? '展开组件面板' : '收起组件面板'"
        >
          <svg 
            class="toggle-icon" 
            :class="{ rotated: firstSidebarCollapsed }" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor"
          >
            <polyline points="15,18 9,12 15,6"></polyline>
          </svg>
        </button>
      </div>
      
      <div class="sidebar-content" v-if="!firstSidebarCollapsed">
        内容
      </div>
    </div>

    <!-- 第二个左侧边栏 -->
    <div class="left-sidebar second-sidebar" :class="{ collapsed: secondSidebarCollapsed }">
      <div class="sidebar-header">
        <span :class="{'collapsed-header': secondSidebarCollapsed}">图表</span>
        <button
          class="toggle-btn second-toggle" 
          @click="toggleSecondSidebar"
          :title="secondSidebarCollapsed ? '展开属性面板' : '收起属性面板'"
        >
          <svg
            class="toggle-icon" 
            :class="{ rotated: secondSidebarCollapsed }" 
            viewBox="0 0 24 24" 
            fill="none" 
            stroke="currentColor"
          >
            <polyline points="15,18 9,12 15,6"></polyline>
          </svg>
        </button>
      </div>
      
      <div class="sidebar-content" v-if="!secondSidebarCollapsed">
       内容
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const firstSidebarCollapsed = ref(false)
const secondSidebarCollapsed = ref(false)

const toggleFirstSidebar = () => {
  firstSidebarCollapsed.value = !firstSidebarCollapsed.value
}
const toggleSecondSidebar = () => {
  secondSidebarCollapsed.value = !secondSidebarCollapsed.value
}
</script>

<style lang="less" scoped>
.sidebar-container {
  display: flex;
  height: 100vh;
  background-color: #f5f5f5;
}

.left-sidebar,
.second-sidebar {
  background-color: #ffffff;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}
.left-sidebar {
  width: 200px;
  border-right: 1px solid #e0e0e0;
  
  &.collapsed {
    width: 50px;
  }
}

.first-sidebar {
  order: 1;
}
.second-sidebar {
  order: 2;
}

.sidebar-header {
  display: flex;
  align-items: center;
  justify-content: flex-start;
  padding: 12px 16px;
  border-bottom: 1px solid #e0e0e0;
  background-color: #fafafa;
  min-height: 50px;
}

.toggle-btn {
  position: absolute;
  top: 15px;
  background: #ffffff;
  border: 1px solid #d9d9d9;
  cursor: pointer;
  width: 18px;
  height: 18px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s ease;
  z-index: 10;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  
  &:hover {
    background-color: #e6f7ff;
    border-color: #1890ff;
    color: #1890ff;
    box-shadow: 0 2px 6px rgba(24, 144, 255, 0.2);
  }
  
  &:active {
    transform: scale(0.95);
  }
}

.first-toggle,.second-toggle {
  right: -9px;
}

.toggle-icon {
  width: 12px;
  height: 12px;
  transition: transform 0.3s ease;
  
  &.rotated {
    transform: rotate(180deg);
  }
}

</style>