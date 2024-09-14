<template>
  <div class="pic-main" @click="onPictureClick">
    <img
      draggable="false"
      v-if="propValue['url']"
      :style="imageAdapter"
      :src="imgUrlTrans(propValue['url'])"
    />
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
import { CSSProperties, computed, nextTick, toRefs } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import eventBus from '@/utils/eventBus'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
const dvMainStore = dvMainStoreWithOut()

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
  if (element.value.style.adaptation === 'original') {
    style.width = 'auto'
    style.height = 'auto'
  } else if (element.value.style.adaptation === 'equiratio') {
    style.height = 'auto'
  }
  return style as CSSProperties
})
const onPictureClick = e => {
  if (element.value.events && element.value.events.checked) {
    if (element.value.events.type === 'displayChange') {
      // 打开弹框区域
      nextTick(() => {
        dvMainStore.popAreaActiveSwitch()
      })
    }
  }
}
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
  cursor: pointer;
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
