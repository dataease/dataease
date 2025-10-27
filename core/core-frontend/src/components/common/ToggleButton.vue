<template>
  <button 
    class="toggle-btn" 
    @click="handleToggle" 
    :title="collapsed ? '展开' : '收起'"
  >
    <svg 
      class="toggle-icon" 
      :class="{ rotated: collapsed }" 
      viewBox="0 0 24 24" 
      fill="none" 
      stroke="currentColor"
    >
      <polyline points="15,18 9,12 15,6"></polyline>
    </svg>
  </button>
</template>

<script setup lang="ts">
interface Props {
  collapsed: boolean
}

interface Emits {
  (e: 'toggle'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const handleToggle = () => {
  emit('toggle')
}
</script>

<style lang="less" scoped>
.toggle-btn {
  position: absolute;
  top: 15px;
  right: -9px;
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

.toggle-icon {
  width: 12px;
  height: 12px;
  transition: transform 0.3s ease;

  &.rotated {
    transform: rotate(180deg);
  }
}
</style>