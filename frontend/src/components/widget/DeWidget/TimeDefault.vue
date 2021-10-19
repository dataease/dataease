<template>
  <span>{{ nowDate }}</span>
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
      let timeFormat = this.element.formatInfo.timeFormat || 'hh:mm:ss'
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
