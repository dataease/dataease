<script setup lang="ts">
import DashboardCanvas from '@/components/dashboard/canvas/index.vue'
import { nextTick, onMounted, ref } from 'vue'
import $ from 'jquery'

const myList = ref([
  { id: 450, x: 2, y: 1, sizeX: 1, sizeY: 1 },
  { id: 472, x: 3, y: 1, sizeX: 3, sizeY: 1 },
  { id: 491, x: 6, y: 1, sizeX: 2, sizeY: 3 },
  { id: 496, x: 8, y: 1, sizeX: 2, sizeY: 3 },
  { id: 492, x: 4, y: 2, sizeX: 2, sizeY: 2 }
])
//屏幕适配，
let screenWidth = window.innerWidth
let screenHeight = window.innerHeight
const baseWidth = ref(90.8333 * (screenWidth / 1366))
const baseHeight = ref(100 * (screenHeight / 638))
const renderState = ref('loading...')
const baseMarginLeft = ref(20 * (screenWidth / 1366))
const baseMarginTop = ref(20 * (screenHeight / 638))
const cyGridster = ref(null)

nextTick(function () {
  $('.dragAndResize').css('width', 'calc(100% - ' + baseMarginLeft.value + 'px)')
})

onMounted(() => {
  cyGridster.value.canvasInit() //在适当的时候初始化布局组件
  cyGridster.value.afterInitOk(function () {
    renderState.value = 'done...'
  })
})
</script>

<template>
  <div id="demo">
    <div class="head">
      <span>仪表板画布测试</span>
      <span style="color: green">{{ renderState }}</span>
    </div>
    <DashboardCanvas
      ref="cyGridster"
      :your-list="myList"
      :base-margin-left="baseMarginLeft"
      :base-margin-top="baseMarginTop"
      :base-width="baseWidth"
      :base-height="baseHeight"
    >
    </DashboardCanvas>
  </div>
</template>

<style lang="less" scoped>
body {
  overflow-x: hidden;
  & * {
    box-sizing: border-box;
  }
}

#demo {
  width: 100%;
  padding: 1.5em 0 1.5em 0;

  .head {
    width: 100%;

    padding-left: 20px;

    height: 50px;

    a {
      text-decoration: none;
      color: black;
    }
  }

  .arrow {
    font-size: 20px;

    position: relative;
    margin-right: 10px;
    top: 2px;
  }
}
</style>
