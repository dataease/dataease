<template>
  <div style="overflow: hidden">
    <!--    <canvas ref="canvasImg"></canvas>-->
    <Icon class="upload" name="other_media" />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, toRefs, watch } from 'vue'
import Icon from '@/components/icon-custom/src/Icon.vue'
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
const canvasImg = ref(null)

let img = null
let canvas = {}
let ctx = null
let isFirst = true

const drawImage = () => {
  const { width, height } = element.value.style
  canvas['width'] = width
  canvas['height'] = height
  if (isFirst) {
    isFirst = false
    img = document.createElement('img')
    img.src = propValue.value['url']
    img.onload = () => {
      ctx.drawImage(img, 0, 0, width, height)
      mirrorFlip()
    }
  } else {
    mirrorFlip()
  }
}

const mirrorFlip = () => {
  const { vertical, horizontal } = propValue.value.flip
  const { width, height } = element.value.style
  const hValue = horizontal ? -1 : 1
  const vValue = vertical ? -1 : 1

  // 清除图片
  ctx.clearRect(0, 0, width, height)
  // 平移图片
  ctx.translate(width / 2 - (width * hValue) / 2, height / 2 - (height * vValue) / 2)
  // 对称镜像
  ctx.scale(hValue, vValue)
  ctx.drawImage(img, 0, 0, width, height)
  // 还原坐标点
  ctx.setTransform(1, 0, 0, 1, 0, 0)
}

onMounted(() => {
  ctx = canvasImg.value.getContext('2d')
  drawImage()
})

watch(
  [element.value.style.width, element.value.style.height],
  () => {
    drawImage()
  },
  { deep: true }
)

watch(
  [propValue.value.flip.vertical, propValue.value.flip.horizontal],
  () => {
    mirrorFlip()
  },
  { deep: true }
)
</script>

<style lang="less" scoped>
.upload {
  width: 50px;
  height: 50px;
}
</style>
