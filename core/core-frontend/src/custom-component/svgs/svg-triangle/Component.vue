<template>
  <div class="svg-triangle-container">
    <svg version="1.1" baseProfile="full" xmlns="http://www.w3.org/2000/svg">
      <polygon
        ref="star"
        :points="points"
        :stroke="element.style.borderColor"
        :fill="element.style.backgroundColor"
        :stroke-width="element.style.borderWidth"
      />
    </svg>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, toRefs, watch } from 'vue'

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
const { element } = toRefs(props)
const points = ref('')

watch(
  () => element.value.style.width,
  () => {
    draw()
  }
)

watch(
  () => element.value.style.height,
  () => {
    draw()
  }
)

onMounted(() => {
  draw()
})

const draw = () => {
  const { width, height } = element.value.style
  drawPolygon(width, height)
}

const drawPolygon = (width, height) => {
  // 三角形三个坐标
  const pointsArray = [
    [0.5, 0.05],
    [1, 0.95],
    [0, 0.95]
  ]

  const coordinatePoints = pointsArray.map(point => width * point[0] + ' ' + height * point[1])
  points.value = coordinatePoints.toString()
}
</script>
<style lang="less" scoped>
.svg-triangle-container {
  width: 100%;
  height: 100%;

  svg {
    width: 100%;
    height: 100%;
  }

  .v-text {
    position: absolute;
    top: 72%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 50%;
    height: 40%;
  }
}
</style>
