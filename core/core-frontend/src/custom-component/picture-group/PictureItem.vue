<template>
  <div class="img-option">
    <div class="img-area" :class="{ 'selected-active': active }">
      <img draggable="false" :src="imgUrlTrans(urlInfo.url)" />
    </div>
    <span :title="urlInfo.name" class="name-area">{{ urlInfo.name }}</span>
  </div>
</template>

<script setup lang="ts">
import { toRefs } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'

const props = withDefaults(
  defineProps<{
    urlInfo: any
    active: boolean
  }>(),
  {}
)

const { urlInfo, active } = toRefs(props)
</script>

<style scoped lang="less">
.img-option {
  margin: 0 5px !important;
  width: 88px;
  height: 88px;
  display: flex;
  flex-direction: column;
  align-content: center;
  .selected-active {
    border: 1px solid var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
  }
  .img-area {
    &:hover {
      border: 1px dashed var(--ed-color-primary-99, rgba(51, 112, 255, 0.6));
    }
    border-radius: 4px;
    background-color: #f5f6f7;
    width: 80px;
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    img {
      width: 100%;
      height: 100%;
    }
  }
  .name-area {
    margin: 6px 4px;
    width: 80px;
    line-height: 20px;
    font-size: 12px !important;
    font-weight: 400;
    text-align: center;
    white-space: nowrap; /* 不换行 */
    overflow: hidden; /* 隐藏超出的内容 */
    text-overflow: ellipsis; /* 用省略号表示被隐藏的部分 */
    color: rgba(100, 106, 115, 1);
  }
}
</style>
