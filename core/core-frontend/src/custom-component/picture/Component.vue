<template>
  <div class="pic-main">
    <img
      draggable="false"
      v-if="propValue['url']"
      :style="imageAdapter"
      :src="imgUrlTrans(propValue['url'])"
    />
    <div v-else class="pic-upload">
      <span
        ><el-button
          @click="uploadImg"
          text
          style="font-family: inherit; color: #646a73"
          icon="Plus"
          >{{ t('visualization.pic_upload_tips') }}</el-button
        ></span
      >
    </div>
  </div>
</template>

<script setup lang="ts">
import { CSSProperties, computed, nextTick, toRefs } from 'vue'
import { imgUrlTrans } from '@/utils/imgUtils'
import eventBus from '@/utils/eventBus'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

const props = defineProps({
  propValue: {
    type: Object,
    required: true
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
