<script setup lang="ts">
import { ref, toRefs } from 'vue'
import Icon from '@/components/icon-custom/src/Icon.vue'

const props = defineProps({
  icon: {
    type: String,
    required: false
  },
  name: {
    type: String,
    required: false
  },
  label: {
    type: String,
    required: true,
    default: ''
  },
  dragInfo: {
    type: String,
    required: true,
    default: ''
  },
  themes: {
    type: String,
    default: 'dark'
  }
})

const { icon, name, label, dragInfo, themes } = toRefs(props)
</script>

<template>
  <div class="drag-component" :class="'drag-' + themes">
    <div draggable="true" :data-id="dragInfo" class="icon-content">
      <span v-if="name" class="label-content">{{ name }}</span>
      <Icon v-if="icon" class="drag-icon" :name="icon" />
    </div>
    <div class="label-content">
      <span>{{ label }}</span>
    </div>
  </div>
</template>

<style lang="less" scoped>
.drag-light {
  :deep(.icon-content) {
    background-color: #f5f6f7 !important;
  }
  :deep(.label-content) {
    color: #1a1a1a !important;
  }
}
.drag-component {
  border-radius: 4px;
  width: 88px !important;
  .icon-content {
    width: 88px !important;
    height: 64px !important;
    border-radius: 4px;
    &:hover {
      border: 1px solid rgba(51, 112, 255, 1);
      cursor: pointer;
    }
    display: flex;
    justify-content: center; /* 水平居中 */
    align-items: center; /* 垂直居中 */
    text-align: center;
    background-color: #000;
    width: 100%;
    height: 60px;
    .drag-icon {
      height: 40px;
      width: 40px;
    }
  }
  .label-content {
    color: @canvas-main-font-color;
    text-align: center;
    line-height: 20px;
    font-size: 12px;
    margin-top: 4px;
  }
}
</style>
