<template>
  <div class="color-button-main">
    <div
      class="color-button-outer"
      :class="active ? 'color-button-active' : ''"
      @click.stop="colorButtonClick"
    >
      <div class="color-button-inner" :class="'color-button-inner-' + colorType" />
    </div>
    <span class="text-area">
      <slot />
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed, toRefs } from 'vue'

const emits = defineEmits(['onClick'])
const props = defineProps({
  colorType: {
    type: String,
    default: 'light'
  },
  label: {
    type: String,
    default: null
  }
})

const { colorType, label } = toRefs(props)
const active = computed(() => label.value === colorType.value)

const colorButtonClick = () => {
  emits('onClick', colorType.value)
}
</script>

<style lang="less" scoped>
.color-button-main {
  float: left;
}

.color-button-outer {
  width: 24px;
  height: 24px;
  border-radius: 2px;
  border-width: 1px;
  border-color: #dee0e3;
  cursor: pointer;
  border-style: solid;
  padding-left: 3px;
  padding-top: 3px;
  &:hover {
    padding-left: 2px;
    padding-top: 2px;
    border-width: 2px;
    border-color: #dee0e3;
  }
}
.color-button-active {
  padding-left: 2px;
  padding-top: 2px;
  border-width: 2px;
  border-color: #3370ff !important;
}

.color-button-inner {
  width: 16px;
  height: 16px;
  border-radius: 2px;
  border: 1px solid #dee0e3;
}
.color-button-inner-light {
  background-color: #ffffff;
}

.color-button-inner-dark {
  background-color: #0c296e;
}
.text-area {
  font-weight: 400;
  size: 14px;
  line-height: 22px;
  margin-top: 4px;
}
</style>
