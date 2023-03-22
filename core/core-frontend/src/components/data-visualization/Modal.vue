<script setup lang="ts">
import { defineEmits } from 'vue'
const props = defineProps({
  show: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['change'])
const hide = () => {
  emit('change')
}

const stopPropagation = e => {
  e.stopPropagation()
}
</script>

<template>
  <div v-if="show" class="modal-bg" @click="hide">
    <div class="fadeInLeft animated modal" @click="stopPropagation">
      <slot></slot>
    </div>
  </div>
</template>

<style lang="less" scoped>
.modal-bg {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1001;

  .modal {
    width: 400px;
    background: #fff;
    height: 100%;
  }
}
</style>
