<template>
  <div class="svg-star-container">
    <svg version="1.1" baseProfile="full" xmlns="http://www.w3.org/2000/svg">
      <polygon
        ref="star"
        :points="points"
        :stroke="element.style.borderColor"
        :fill="element.style.backgroundColor"
        :stroke-width="element.style.borderWidth"
      />
    </svg>
    <v-text :prop-value="element.propValue" :element="element" />
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
const { propValue, element } = toRefs(props)
const points = ref('')

watch(
  () => element.value.style.width,
  val => {
    draw()
  }
)

watch(
  () => element.value.style.height,
  val => {
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
  // 五角星十个坐标点的比例集合
  const pointsArray = [
    [0.5, 0],
    [0.625, 0.375],
    [1, 0.375],
    [0.75, 0.625],
    [0.875, 1],
    [0.5, 0.75],
    [0.125, 1],
    [0.25, 0.625],
    [0, 0.375],
    [0.375, 0.375]
  ]

  const coordinatePoints = pointsArray.map(point => width * point[0] + ' ' + height * point[1])
  points.value = coordinatePoints.toString()
}
</script>

<style lang="less" scoped>
.svg-star-container {
  width: 100%;
  height: 100%;

  svg {
    width: 100%;
    height: 100%;
  }

  .v-text {
    position: absolute;
    top: 58%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 50%;
    height: 40%;
  }
}
</style>
