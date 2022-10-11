<template>
  <div
    style="display: flex;align-items: center;"
    :style="{ 'height': containerHeight, 'margin':timeMargin +'px'}"
  >

    <p style="width:100%;margin:auto;">{{ nowDate }}</p>

  </div>
</template>

<script>
export default {
  props: {
    element: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      nowDate: '', // 当前日期
      nowWeek: '',
      timer: null
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
    this.currentTime()
  },
  // 销毁定时器
  beforeDestroy() {
    if (this.timer) {
      clearInterval(this.timer) // 在Vue实例销毁前，清除时间定时器
    }
  },
  methods: {
    currentTime() {
      this.timer = setInterval(this.formatDate, 500)
    },
    formatDate() {
      const weekArr = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
      let timeFormat = this.element.formatInfo.timeFormat
      const showWeek = this.element.formatInfo.showWeek
      const showDate = this.element.formatInfo.showDate
      const dateFormat = this.element.formatInfo.dateFormat || 'yyyy-MM-dd'
      if (showDate && dateFormat) {
        timeFormat = dateFormat + ' ' + timeFormat
      }

      const date = new Date()

      this.nowDate = date.format(timeFormat)

      if (showWeek) {
        this.nowWeek = weekArr[date.getDay()]
        this.nowDate = this.nowDate + ' ' + this.nowWeek
      }
    }
  }
}
</script>

<style lang="scss" scoped>

</style>
