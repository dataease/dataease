<script lang="tsx" setup>
import { toRefs } from 'vue'
const props = defineProps({
  viewIcon: {
    type: String,
    required: true,
    default: ''
  },
  themes: {
    type: String,
    required: true,
    default: ''
  }
})

const { viewIcon, themes } = toRefs(props)
</script>

<template>
  <div class="canvas-content error-info">
    <Icon class-name="item-icon" :name="viewIcon + (themes === 'dark' ? '-dark' : '')" />
  </div>
</template>

<style lang="less" scoped>
.error-info {
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  color: #9ea6b2;
  width: 100%;
  height: 100%;
  flex-direction: column;
}

.item-icon {
  width: 50%;
  height: 50%;
  opacity: 0.3;
}
</style>
