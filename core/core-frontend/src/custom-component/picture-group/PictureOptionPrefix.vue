<template>
  <div class="img-option-prefix">
    <img draggable="false" :src="imgUrlTrans(url)" />
  </div>
</template>

<script setup lang="ts">
import { imgUrlTrans } from '@/utils/imgUtils'

withDefaults(
  defineProps<{
    url: any
  }>(),
  {}
)
</script>

<style scoped lang="less">
.img-option-prefix {
  width: 20px;
  height: 20px;
  display: flex;
  flex-direction: column;
  align-content: center;
  img {
    border-radius: 4px;
    width: 100%;
    height: 100%;
  }
}
</style>
