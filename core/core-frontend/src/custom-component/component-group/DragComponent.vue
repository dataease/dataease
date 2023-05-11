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
  }
})

const { icon, name, label, dragInfo } = toRefs(props)
</script>

<template>
  <div class="drag-component">
    <div draggable="true" :data-id="dragInfo" class="icon-content">
      <span v-if="name">{{ name }}</span>
      <Icon v-if="icon" class="drag-icon" :name="icon" />
    </div>
    <div class="label-content">
      <span>{{ label }}</span>
    </div>
  </div>
</template>

<style lang="less" scoped>
.drag-component {
  width: 120px;
  height: 80px;
  .icon-content {
    &:hover {
      border: 1px solid #ddd;
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
      height: 50px;
      width: 50px;
    }
  }
  .label-content {
    text-align: center;
  }
}
</style>
