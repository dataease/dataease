<template>
  <div
    style="width: 100%; height: 100%; display: flex; align-items: center"
    :style="{ 'justify-content': element.style.textAlign }"
  >
    <p>{{ state.nowDate }}</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, reactive, toRefs } from 'vue'

const props = defineProps({
  element: {
    type: Object
  }
})

const { element } = toRefs(props)
const state = reactive({
  nowDate: '', // 当前日期
  nowWeek: '',
  timer: null
})

const currentTime = () => {
  state.timer = setInterval(formatDate, 500)
}
const formatDate = () => {
  const weekArr = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  let timeFormat = element.value.formatInfo.timeFormat
  const showWeek = element.value.formatInfo.showWeek
  const showDate = element.value.formatInfo.showDate
  const dateFormat = element.value.formatInfo.dateFormat || 'yyyy-MM-dd'
  if (showDate && dateFormat) {
    timeFormat = dateFormat + ' ' + timeFormat
  }

  const date = new Date()

  state.nowDate = date.format(timeFormat)

  if (showWeek) {
    state.nowWeek = weekArr[date.getDay()]
    state.nowDate = state.nowDate + ' ' + state.nowWeek
  }
}

onMounted(() => {
  currentTime()
})

onUnmounted(() => {
  if (state.timer) {
    clearInterval(state.timer) // 在Vue实例销毁前，清除时间定时器
  }
})
</script>

<style lang="less" scoped></style>
