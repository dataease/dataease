<template>
  <div class="icon-option">
    <div class="icon-area" :class="{ 'selected-active': active }">
      <Icon
        :style="{ color: innerImageColor }"
        class-name="svg-background"
        :name="mainIconClass(item)"
      />
    </div>
    <span>{{ item.name }}</span>
  </div>
</template>

<script setup lang="ts">
import { toRefs } from 'vue'

const props = defineProps({
  item: Object,
  active: Boolean,
  innerImageColor: String
})

const { item, innerImageColor, active } = toRefs(props)

const mainIconClass = itemUrl => {
  return itemUrl.url.replace('board/', '').replace('.svg', '')
}
</script>

<style scoped lang="less">
.icon-option {
  width: 88px;
  height: 88px;
  display: flex;
  flex-direction: column;
  align-content: center;
  .selected-active {
    border: 1px solid rgba(51, 112, 255, 0.6);
  }
  .icon-area {
    &:hover {
      border: 1px dashed rgba(51, 112, 255, 0.6);
    }
    border-radius: 4px;
    background-color: #f5f6f7;
    width: 88px;
    height: 64px;
    display: flex;
    align-items: center;
    justify-content: center;
    .svg-background {
      width: 70px;
      height: 50px;
    }
  }
}
</style>
