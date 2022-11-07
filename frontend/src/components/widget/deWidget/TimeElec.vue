<template>
  <div
    id="clock"
    :style="{'--varBg': varBg}"
  >
    <p class="date">{{ date }}</p>
    <p
      v-if="time"
      class="time"
      :style="{'fontSize': (parseInt(element.style.fontSize) * 3) + 'px'}"
    >{{ time }}</p>
  </div>
</template>

<script>
import { getThemeCluster } from '@/utils/style'
export default {
  name: 'TimeElec',
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
      week: ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
    }
  },
  computed: {
    varBg() {
      const color = this.element.style.color
      const styleCluster = getThemeCluster(color.replace('#', ''))
      if (styleCluster.length > 2) {
        const len = styleCluster.length
        const val = styleCluster[len - 2]
        return val
      }
      return ''
    },
    timeMargin() {
      return this.element.style.time_margin
    },
    containerHeight() {
      return 'calc(100% - ' + this.element.style.time_margin * 2 + 'px)'
    }
  },
  created() {
    // this.bigFontSize = (parseInt(this.element.style.fontSize) * 3) + 'px'
  },
  mounted() {
    // this.bigFontSize = this.element.style.fontSize + ''
    this.currentTime()
  },

  beforeDestroy() {
    if (this.updateTime) {
      clearInterval(this.updateTime) // 在Vue实例销毁前，清除时间定时器
    }
  },
  methods: {
    currentTime() {
      setInterval(this.updateTime, 500)
    },
    zeroPadding(num, digit) {
      var zero = ''
      for (var i = 0; i < digit; i++) {
        zero += '0'
      }
      return (zero + num).slice(-digit)
    },
    updateTime() {
      var cd = new Date()
      const timeFormat = this.element.formatInfo.timeFormat
      const showWeek = this.element.formatInfo.showWeek
      const showDate = this.element.formatInfo.showDate
      const dateFormat = this.element.formatInfo.dateFormat || 'yyyy-MM-dd'
      let nowDate = ''
      if (showDate && dateFormat) {
        nowDate = cd.format(dateFormat)
      }
      const nowWeek = this.week[cd.getDay()]
      if (timeFormat) {
        this.time = cd.format(timeFormat)
      } else {
        this.time = null
      }

      this.date = showWeek ? (nowDate + ' ' + nowWeek) : nowDate
    }
  }
}
</script>

<style lang="scss" scoped>
p {
  margin: 0;
  padding: 0;
}

#clock {
  font-family: "Share Tech Mono", monospace;
  // color: #ffffff;
  // text-align: center;
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  // color: #daf6ff;
  // text-shadow: 0 0 20px #0aafe6, 0 0 20px rgba(10, 175, 230, 0);
  text-shadow: 0 0 20px var(--varBg), 0 0 20px rgba(10, 175, 230, 0);
}
#clock .time {
  letter-spacing: 0.05em;
  font-size: 80px;
  padding: 5px 0;
}
#clock .date {
  letter-spacing: 0.1em;
  //font-size: 24px;
}
#clock .text {
  letter-spacing: 0.1em;
  font-size: 12px;
  padding: 20px 0 0;
}
</style>
