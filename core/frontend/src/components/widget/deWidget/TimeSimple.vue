<template>
  <div :style="{ 'height': containerHeight}">
    <div
      ref="canvasContainer"
      class="time-s-class"
      style="height: calc(100% - 50px);"
      :style="{'margin':timeMargin +'px'}"
    >
      <canvas
        :id="'simple-canvas'+element.id"
        class="de-canvas"
        :width="canvas_width"
        :height="canvas_height"
      />
    </div>
    <div style="height: 50px;display: flex;align-items: center;">
      <p
        :id="element.id + '_fulltime'"
        :style="{'fontSize': (parseInt(element.style.fontSize) * 1) + 'px', 'color':element.style.color}"
        style="width:100%;margin:auto;"
      />
    </div>
  </div>
</template>

<script>
export default {
  name: 'TimeSimple',
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
      draw: null,
      timer: null,
      canvas_width: null,
      canvas_height: null,
      padding: 0

    }
  },
  computed: {
    timeMargin() {
      return this.element.style.time_margin
    },
    containerHeight() {
      return 'calc(100% - ' + this.element.style.time_margin * 2 + 'px)'
    }
  },
  mounted() {
    this.canvas = document.getElementById('simple-canvas' + this.element.id)
    this.draw = this.canvas.getContext('2d')
    this.canvas_width = this.element.style.width
    this.canvas_height = this.element.style.height
    this.currentTime()
  },
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer) // 在Vue实例销毁前，清除时间定时器
    }
  },
  methods: {
    resize() {
      this.$forceUpdate()
    },
    currentTime() {
      this.timer = setInterval(this.canvass, 500)
    },
    canvass() {
      const timeFormat = this.element.formatInfo.timeFormat
      const dateFormat = this.element.formatInfo.dateFormat || 'yyyy-MM-dd'
      const showWeek = this.element.formatInfo.showWeek
      const showDate = this.element.formatInfo.showDate

      const canvas_w = this.$refs.canvasContainer.clientWidth || this.element.style.width
      if (canvas_w !== this.canvas_width) {
        this.canvas_width = canvas_w
      }

      const canvas_h = this.$refs.canvasContainer.clientHeight || (this.element.style.height - 50)

      if (canvas_h !== this.canvas_height) {
        this.canvas_height = canvas_h
      }

      const side_length = Math.min(canvas_w, canvas_h)

      const draws = this.draw
      // 获取时间
      var time = new Date()

      var w = time.getDay()
      var englishWeek = ''
      var h = time.getHours()

      var min = time.getMinutes()

      var s = time.getSeconds()

      if (w === 0) {
        englishWeek = 'Sunday'
      } else if (w === 1) {
        englishWeek = 'Monday'
      } else if (w === 2) {
        englishWeek = 'Tuesday'
      } else if (w === 3) {
        englishWeek = 'Wednesday'
      } else if (w === 4) {
        englishWeek = 'Thursday'
      } else if (w === 5) {
        englishWeek = 'Friday'
      } else {
        englishWeek = 'Saturday'
      }
      let simpleTime = ''
      if (timeFormat) {
        simpleTime = time.format(timeFormat)
      }
      let nowDate = ''
      if (showDate && dateFormat) {
        nowDate = time.format(dateFormat)
      }
      const nowWeek = this.week[time.getDay()]
      var fullTime = showWeek ? (nowDate + ' ' + nowWeek) : nowDate
      var fullDoc = document.getElementById(this.element.id + '_fulltime')
      fullDoc.innerHTML = fullTime

      const draw = draws
      draw.clearRect(0, 0, canvas_w, canvas_h)

      draw.fillStyle = this.element.style.color
      draw.font = this.element.style.fontSize + 'px 黑体'
      draw.fillText(simpleTime, canvas_w / 2 - 33, canvas_h / 2 + 50)
      showWeek && draw.fillText(englishWeek, canvas_w / 2 - 25, canvas_h / 2 + 70)
      for (var i = 0; i < 12; i++) {
        draw.save()

        draw.lineWidth = 4
        draw.strokeStyle = this.element.style.color
        draw.translate(canvas_w / 2, canvas_h / 2)
        draw.rotate(i * 30 * Math.PI / 180)

        draw.beginPath()

        draw.moveTo(0, side_length / 2 - 0)
        draw.lineTo(0, side_length / 2 - 20)
        draw.closePath()
        draw.stroke()
        draw.restore()
      }
      // 分刻度
      for (let i = 0; i < 60; i++) {
        draw.save()
        draw.translate(canvas_w / 2, canvas_h / 2)
        draw.rotate(i * 6 * Math.PI / 180)
        draw.lineWidth = 2
        draw.strokeStyle = this.element.style.color
        draw.beginPath()

        draw.moveTo(0, side_length / 2 - 0)
        draw.lineTo(0, side_length / 2 - 10)
        draw.closePath()
        draw.stroke()

        draw.restore()
      }

      // 画时阵
      draw.save()
      draw.strokeStyle = this.element.style.color
      draw.translate(canvas_w / 2, canvas_h / 2)
      const hourzs = h + min / 60// 获取浮点类型的小时
      draw.rotate(hourzs * 30 * Math.PI / 180)
      draw.lineWidth = 6
      draw.beginPath()
      draw.moveTo(0, 0)
      draw.lineTo(0, -(side_length / 2) * 0.5)
      draw.closePath()
      draw.stroke()
      draw.restore()
      // 画分针
      draw.save()
      draw.translate(canvas_w / 2, canvas_h / 2)

      draw.rotate(min * 6 * Math.PI / 180)
      draw.strokeStyle = this.element.style.color
      draw.lineWidth = 3
      draw.beginPath()
      draw.moveTo(0, 0)
      draw.lineTo(0, -(side_length / 2) * 0.7)
      draw.closePath()
      draw.stroke()
      draw.restore()
      // 画秒针
      draw.save()
      draw.translate(canvas_w / 2, canvas_h / 2)
      draw.rotate(s * 6 * Math.PI / 180)
      draw.strokeStyle = this.element.style.color
      draw.lineWidth = 1
      draw.beginPath()
      draw.moveTo(0, 15)
      draw.lineTo(0, -(side_length / 2) * 0.9)
      draw.closePath()
      draw.stroke()
      draw.restore()

      // 画中心原点
      draw.lineWidth = 2
      draw.beginPath()
      draw.arc(canvas_w / 2, canvas_h / 2, 4, 0, 360, false)
      draw.closePath()
      draw.fill()
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
