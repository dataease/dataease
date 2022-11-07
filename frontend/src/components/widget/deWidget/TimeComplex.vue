<template>
  <div>
    <canvas
      :id="'complex_canvas'+element.id"
      width="500"
      height="500"
    />
  </div>
</template>

<script>
var milliseconds = 0
var minutes = 0
var hour = 0
var date = ''
export default {
  name: 'TimeComplex',
  props: {
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      time: '',
      date: '',
      week: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六'],
      canvas: null,
      ctx: null,
      ctxBack: null,
      numBack: null,
      timer: null
    }
  },
  mounted() {
    this.canvas = document.getElementById('complex_canvas' + this.element.id)
    this.initCtx()
    this.initCtxBack()
    this.initNumBack()
    this.currentTime()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer) // 在Vue实例销毁前，清除时间定时器
    }
  },
  methods: {
    currentTime() {
      this.timer = setInterval(this.pageInit, 500)
    },
    initCtx() {
      this.ctx = this.canvas.getContext('2d')
      this.ctx.strokeStyle = '#7FFFD4'
      this.ctx.lineWidth = 3
      this.ctx.shadowBlur = 5
      this.ctx.shadowColor = '#7FFFD4'
    },
    initCtxBack() {
      this.ctxBack = this.canvas.getContext('2d')
      this.ctxBack.lineWidth = 1
      this.ctxBack.shadowBlur = 0
      this.ctxBack.shadowColor = '#F0F8FF'
    },
    initNumBack() {
      this.numBack = this.canvas.getContext('2d')
    },
    pageInit() {
      this.showTime()
      this.showBack()
      this.drawSecPin()
      this.drawMinPin()
      this.drawHouPin()
      this.setPoint()
      this.setNum()
    },

    setNum() {
      this.numBack.save()
      this.numBack.translate(250, 250)
      this.numBack.beginPath()
      this.numBack.fillStyle = '#7FFFD4'
      this.numBack.font = '30px Helvetica'
      for (var i = 0; i < 60; i++) {
        if (i % 5 === 0) {
          this.numBack.lineWidth = 5
          var xPoint = Math.sin(i * 6 * 2 * Math.PI / 360) * 195
          var yPoint = -Math.cos(i * 6 * 2 * Math.PI / 360) * 195
          this.numBack.fillText(i === 0 ? 12 : i / 5, i === 0 ? -15 : xPoint - 10, i === 0 ? -185 : i <= 30 ? yPoint + 5 : yPoint + 10)
        }
      }
      this.numBack.stroke()
      this.numBack.closePath()
      this.numBack.restore()
    },

    drawSecPin() {
      this.ctxBack.save()
      this.ctxBack.translate(250, 250)
      this.ctxBack.rotate(milliseconds / 60 * 2 * Math.PI)
      this.ctxBack.beginPath()
      this.ctxBack.strokeStyle = '#AFEEEE'
      this.ctxBack.lineWidth = 1
      this.ctxBack.lineJoin = 'bevel'
      this.ctxBack.miterLimit = 10
      this.ctxBack.moveTo(0, 30)
      this.ctxBack.lineTo(3, -175)
      this.ctxBack.lineTo(13, -165)
      this.ctxBack.lineTo(0, -210)
      this.ctxBack.lineTo(-13, -165)
      this.ctxBack.lineTo(-3, -175)
      this.ctxBack.lineTo(0, 30)
      this.ctxBack.stroke()
      this.ctxBack.closePath()
      this.ctxBack.restore()
    },

    drawMinPin() {
      this.ctxBack.save()
      this.ctxBack.translate(250, 250)
      this.ctxBack.rotate(minutes * 6 * Math.PI / 180)
      this.ctxBack.beginPath()
      this.ctxBack.strokeStyle = '#20B2AA'
      this.ctxBack.lineWidth = 1
      this.ctxBack.lineJoin = 'bevel'
      this.ctxBack.miterLimit = 10
      this.ctxBack.moveTo(0, 20)
      this.ctxBack.lineTo(3, -145)
      this.ctxBack.lineTo(10, -135)
      this.ctxBack.lineTo(0, -180)
      this.ctxBack.lineTo(-10, -135)
      this.ctxBack.lineTo(-3, -145)
      this.ctxBack.lineTo(0, 20)
      this.ctxBack.stroke()
      this.ctxBack.closePath()
      this.ctxBack.restore()
    },

    drawHouPin() {
      this.ctxBack.save()
      this.ctxBack.translate(250, 250)
      this.ctxBack.rotate(hour * 30 * Math.PI / 180)
      this.ctxBack.beginPath()
      this.ctxBack.strokeStyle = '#87CEFA'
      this.ctxBack.lineWidth = 1
      this.ctxBack.lineJoin = 'bevel'
      this.ctxBack.miterLimit = 10
      this.ctxBack.moveTo(0, 20)
      this.ctxBack.lineTo(3, -110)
      this.ctxBack.lineTo(10, -100)
      this.ctxBack.lineTo(0, -150)
      this.ctxBack.lineTo(-10, -100)
      this.ctxBack.lineTo(-3, -110)
      this.ctxBack.lineTo(0, 20)
      this.ctxBack.stroke()
      this.ctxBack.closePath()
      this.ctxBack.restore()
    },

    setPoint() {
      this.ctxBack.beginPath()
      this.ctxBack.fillStyle = 'black'
      this.ctxBack.arc(250, 250, 3, 0, 2 * Math.PI)
      this.ctxBack.stroke()
    },

    showBack() {
      for (var i = 0; i < 60; i++) {
        this.ctxBack.save()
        this.ctxBack.translate(250, 250)
        this.ctxBack.rotate(i / 60 * 2 * Math.PI)
        this.ctxBack.beginPath()
        this.ctxBack.strokeStyle = '#7FFFD4'
        this.ctxBack.moveTo(0, -250)
        this.ctxBack.lineWidth = i % 5 === 0 ? 5 : 2
        this.ctxBack.lineTo(0, -230)
        this.ctxBack.stroke()
        this.ctxBack.closePath()
        this.ctxBack.restore()
      }
      this.ctxBack.beginPath()
      this.ctxBack.arc(250, 250, 230, 0, 2 * Math.PI)
      this.ctxBack.stroke()
    },

    degToRad(degree) {
      var result
      var factor = Math.PI / 180
      if (degree === 0) {
        result = 270 * factor
      } else {
        result = degree * factor
      }
      return result
    },

    showTime() {
      var now = new Date()
      var today = now.toLocaleDateString()
      var time = now.toLocaleTimeString()
      var day = now.getDay()
      var hrs = now.getHours()
      var min = now.getMinutes()
      var sec = now.getSeconds()
      var mil = now.getMilliseconds()
      var smoothsec = sec + (mil / 1000)
      var smoothmin = min + (smoothsec / 60)
      var hours = hrs + (smoothmin / 60)
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
      const gradient = this.ctx.createRadialGradient(250, 250, 5, 250, 250, 300)
      gradient.addColorStop(0, '#03303a')
      gradient.addColorStop(1, 'black')
      this.ctx.fillStyle = gradient
      this.ctx.fillRect(0, 0, 500, 500)
      this.ctx.beginPath()
      this.ctx.strokeStyle = '#87CEFA'
      this.ctx.arc(250, 250, 215, this.degToRad(0), this.degToRad((hours * 30) - 90))
      this.ctx.stroke()
      this.ctx.beginPath()
      this.ctx.strokeStyle = '#20B2AA'
      this.ctx.arc(250, 250, 220, this.degToRad(0), this.degToRad(smoothmin * 6 - 90))
      this.ctx.stroke()
      this.ctx.beginPath()
      this.ctx.strokeStyle = '#AFEEEE'
      this.ctx.arc(250, 250, 225, this.degToRad(0), this.degToRad(smoothsec * 6 - 90))
      this.ctx.stroke()
      this.ctx.font = '25px Helvetica Bold'
      this.ctx.fillStyle = '#7FFFD4'
      this.ctx.fillText(today + '/星期' + date, 150, 230)
      this.ctx.font = '23px Helvetica Bold'
      this.ctx.fillStyle = '#7FFFD4'
      this.ctx.fillText(time, 190, 280)
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
