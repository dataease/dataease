<template>
  <div class="pic-main">
    <img v-if="propValue['url']" :style="imageAdapter" :src="imgUrlTrans(propValue['url'])" />
    <div v-else class="pic-upload">
      <span
        ><el-button @click="uploadImg" text style="color: #646a73" icon="Plus"
          >请上传图片...</el-button
        ></span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, toRefs, watch } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import eventBus from '@/utils/eventBus'
const props = defineProps({
  propValue: {
    type: String,
    required: true,
    default: ''
  },
  element: {
    type: Object,
    default() {
      return {
        propValue: null
      }
    }
  }
})

const { propValue, element } = toRefs(props)

const imageAdapter = computed(() => {
  const style = {
    position: 'relative',
    width: '100%',
    height: '100%'
  }
  return style
})

const uploadImg = () => {
  nextTick(() => {
    eventBus.emit('uploadImg')
  })
}
</script>

<style lang="less" scoped>
.pic-main {
  overflow: hidden;
  width: 100%;
  height: 100%;
}
.pic-upload {
  display: flex;
  width: 100%;
  height: 100%;
  color: #5370af;
  align-items: center;
  justify-content: center;
}
</style>
