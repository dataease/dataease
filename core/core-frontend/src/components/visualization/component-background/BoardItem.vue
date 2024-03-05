<template>
  <div class="icon-option">
    <div
      class="icon-area"
      :class="{ 'selected-active': active, 'icon-area-dark': themes === 'dark' }"
    >
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

const props = withDefaults(
  defineProps<{
    item: any
    active: boolean
    themes?: EditorTheme
    innerImageColor: string
  }>(),
  {
    themes: 'dark'
  }
)

const { item, innerImageColor, active, themes } = toRefs(props)

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
    border: 1px solid var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
  }
  .icon-area {
    &:hover {
      border: 1px dashed var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
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

.icon-area-dark {
  background-color: #1a1a1a !important;
}
</style>
