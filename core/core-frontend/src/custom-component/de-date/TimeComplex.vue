<template>
  <div>
    <canvas :id="'complex_canvas' + element.id" width="500" height="500" />
  </div>
</template>

<script lang="ts" setup>
import { toRefs } from 'vue/dist/vue'
import { getCurrentInstance, onMounted, reactive } from 'vue'

let milliseconds = 0
let minutes = 0
let hour = 0
let date = ''

const props = defineProps({
  element: {
    type: Object
  }
})
const { element } = toRefs(props)

const state = reactive({
  time: '',
  date: '',
  week: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
  canvas: null,
  ctx: null,
  ctxBack: null,
  numBack: null,
  timer: null
})

const currentTime = () => {
  state.timer = setInterval(pageInit, 500)
}
const initCtx = () => {
  state.ctx = state.canvas.getContext('2d')
  state.ctx.strokeStyle = '#7FFFD4'
  state.ctx.lineWidth = 3
  state.ctx.shadowBlur = 5
  state.ctx.shadowColor = '#7FFFD4'
}
const initCtxBack = () => {
  state.ctxBack = state.canvas.getContext('2d')
  state.ctxBack.lineWidth = 1
  state.ctxBack.shadowBlur = 0
  state.ctxBack.shadowColor = '#F0F8FF'
}
const initNumBack = () => {
  state.numBack = state.canvas.getContext('2d')
}
const pageInit = () => {
    showTime()
    showBack()
    drawSecPin()
    drawMinPin()
    drawHouPin()
    setPoint()
    setNum()
  },
  setNum = () => {
    state.numBack.save()
    state.numBack.translate(250, 250)
    state.numBack.beginPath()
    state.numBack.fillStyle = '#7FFFD4'
    state.numBack.font = '30px Helvetica'
    for (let i = 0; i < 60; i++) {
      if (i % 5 === 0) {
        state.numBack.lineWidth = 5
        let xPoint = Math.sin((i * 6 * 2 * Math.PI) / 360) * 195
        let yPoint = -Math.cos((i * 6 * 2 * Math.PI) / 360) * 195
        state.numBack.fillText(
          i === 0 ? 12 : i / 5,
          i === 0 ? -15 : xPoint - 10,
          i === 0 ? -185 : i <= 30 ? yPoint + 5 : yPoint + 10
        )
      }
    }
    state.numBack.stroke()
    state.numBack.closePath()
    state.numBack.restore()
  }

const drawSecPin = () => {
  state.ctxBack.save()
  state.ctxBack.translate(250, 250)
  state.ctxBack.rotate((milliseconds / 60) * 2 * Math.PI)
  state.ctxBack.beginPath()
  state.ctxBack.strokeStyle = '#AFEEEE'
  state.ctxBack.lineWidth = 1
  state.ctxBack.lineJoin = 'bevel'
  state.ctxBack.miterLimit = 10
  state.ctxBack.moveTo(0, 30)
  state.ctxBack.lineTo(3, -175)
  state.ctxBack.lineTo(13, -165)
  state.ctxBack.lineTo(0, -210)
  state.ctxBack.lineTo(-13, -165)
  state.ctxBack.lineTo(-3, -175)
  state.ctxBack.lineTo(0, 30)
  state.ctxBack.stroke()
  state.ctxBack.closePath()
  state.ctxBack.restore()
}

const drawMinPin = () => {
  state.ctxBack.save()
  state.ctxBack.translate(250, 250)
  state.ctxBack.rotate((minutes * 6 * Math.PI) / 180)
  state.ctxBack.beginPath()
  state.ctxBack.strokeStyle = '#20B2AA'
  state.ctxBack.lineWidth = 1
  state.ctxBack.lineJoin = 'bevel'
  state.ctxBack.miterLimit = 10
  state.ctxBack.moveTo(0, 20)
  state.ctxBack.lineTo(3, -145)
  state.ctxBack.lineTo(10, -135)
  state.ctxBack.lineTo(0, -180)
  state.ctxBack.lineTo(-10, -135)
  state.ctxBack.lineTo(-3, -145)
  state.ctxBack.lineTo(0, 20)
  state.ctxBack.stroke()
  state.ctxBack.closePath()
  state.ctxBack.restore()
}

const drawHouPin = () => {
  state.ctxBack.save()
  state.ctxBack.translate(250, 250)
  state.ctxBack.rotate((hour * 30 * Math.PI) / 180)
  state.ctxBack.beginPath()
  state.ctxBack.strokeStyle = '#87CEFA'
  state.ctxBack.lineWidth = 1
  state.ctxBack.lineJoin = 'bevel'
  state.ctxBack.miterLimit = 10
  state.ctxBack.moveTo(0, 20)
  state.ctxBack.lineTo(3, -110)
  state.ctxBack.lineTo(10, -100)
  state.ctxBack.lineTo(0, -150)
  state.ctxBack.lineTo(-10, -100)
  state.ctxBack.lineTo(-3, -110)
  state.ctxBack.lineTo(0, 20)
  state.ctxBack.stroke()
  state.ctxBack.closePath()
  state.ctxBack.restore()
}

const setPoint = () => {
  state.ctxBack.beginPath()
  state.ctxBack.fillStyle = 'black'
  state.ctxBack.arc(250, 250, 3, 0, 2 * Math.PI)
  state.ctxBack.stroke()
}

const showBack = () => {
  for (let i = 0; i < 60; i++) {
    state.ctxBack.save()
    state.ctxBack.translate(250, 250)
    state.ctxBack.rotate((i / 60) * 2 * Math.PI)
    state.ctxBack.beginPath()
    state.ctxBack.strokeStyle = '#7FFFD4'
    state.ctxBack.moveTo(0, -250)
    state.ctxBack.lineWidth = i % 5 === 0 ? 5 : 2
    state.ctxBack.lineTo(0, -230)
    state.ctxBack.stroke()
    state.ctxBack.closePath()
    state.ctxBack.restore()
  }
  state.ctxBack.beginPath()
  state.ctxBack.arc(250, 250, 230, 0, 2 * Math.PI)
  state.ctxBack.stroke()
}

const degToRad = degree => {
  let result
  let factor = Math.PI / 180
  if (degree === 0) {
    result = 270 * factor
  } else {
    result = degree * factor
  }
  return result
}

const showTime = () => {
  let now = new Date()
  let today = now.toLocaleDateString()
  let time = now.toLocaleTimeString()
  let day = now.getDay()
  let hrs = now.getHours()
  let min = now.getMinutes()
  let sec = now.getSeconds()
  let mil = now.getMilliseconds()
  let smoothsec = sec + mil / 1000
  let smoothmin = min + smoothsec / 60
  let hours = hrs + smoothmin / 60
  milliseconds = smoothsec
  minutes = smoothmin
  hour = hours
  switch (day) {
    case 1:
      date = '一'
      break
    case 2:
      date = '二'
      break
    case 3:
      date = '三'
      break
    case 4:
      date = '四'
      break
    case 5:
      date = '五'
      break
    case 6:
      date = '六'
      break
    case 0:
      date = '日'
      break
  }
  const gradient = state.ctx.createRadialGradient(250, 250, 5, 250, 250, 300)
  gradient.addColorStop(0, '#03303a')
  gradient.addColorStop(1, 'black')
  state.ctx.fillStyle = gradient
  state.ctx.fillRect(0, 0, 500, 500)
  state.ctx.beginPath()
  state.ctx.strokeStyle = '#87CEFA'
  state.ctx.arc(250, 250, 215, degToRad(0), degToRad(hours * 30 - 90))
  state.ctx.stroke()
  state.ctx.beginPath()
  state.ctx.strokeStyle = '#20B2AA'
  state.ctx.arc(250, 250, 220, degToRad(0), degToRad(smoothmin * 6 - 90))
  state.ctx.stroke()
  state.ctx.beginPath()
  state.ctx.strokeStyle = '#AFEEEE'
  state.ctx.arc(250, 250, 225, degToRad(0), degToRad(smoothsec * 6 - 90))
  state.ctx.stroke()
  state.ctx.font = '25px Helvetica Bold'
  state.ctx.fillStyle = '#7FFFD4'
  state.ctx.fillText(today + '/星期' + date, 150, 230)
  state.ctx.font = '23px Helvetica Bold'
  state.ctx.fillStyle = '#7FFFD4'
  state.ctx.fillText(time, 190, 280)
}

onMounted(() => {
  state.canvas = document.getElementById('complex_canvas' + element.value.id)
  initCtx()
  initCtxBack()
  initNumBack()
  currentTime()
})
</script>

<style lang="less" scoped></style>
