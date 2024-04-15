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
  width: 32px;
  height: 32px;
  border-radius: 4px;
  border-width: 1px;
  border-color: #dee0e3;
  cursor: pointer;
  border-style: solid;
  padding-left: 5px;
  padding-top: 5px;
  &:hover {
    padding-left: 5px;
    padding-top: 5px;
    border-width: 1px;
    border-color: var(--ed-color-primary-99, #3370ff99);
  }
}
.color-button-active {
  padding-left: 5px;
  padding-top: 5px;
  border-width: 1px;
  border-color: var(--ed-color-primary) !important;
}

.color-button-inner {
  width: 20px;
  height: 20px;
  border-radius: 4px;
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
  size: 12px;
  color: #1f2329;
  line-height: 20px;
  margin-top: 4px;
}
</style>
