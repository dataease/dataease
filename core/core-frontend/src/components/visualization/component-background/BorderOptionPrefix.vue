<template>
  <div class="img-option-prefix">
    <Icon
      ><component
        :style="{ color: innerImageColor, width: '20px', height: '20px' }"
        class="svg-icon svg-background"
        :is="iconBoardMap[mainIconClass(url)]"
      ></component
    ></Icon>
  </div>
</template>

<script setup lang="ts">
import { iconBoardMap } from '@/components/icon-group/board-list'
import { toRefs } from 'vue'
import { Icon } from '@/components/icon-custom'

const props = withDefaults(
  defineProps<{
    url: any
    innerImageColor: string
  }>(),
  {}
)

const { innerImageColor } = toRefs(props)

const mainIconClass = url => {
  return url.replace('board/', '').replace('.svg', '')
}
</script>

<style scoped lang="less">
.img-option-prefix {
  width: 20px;
  height: 20px;
  display: flex;
  flex-direction: column;
  align-content: center;
}
</style>
