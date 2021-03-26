<template>
  <div class="rect-shape">
    <chart-component v-if="showCard" :ref="element.propValue.id" class="chart-class" :chart-id="element.propValue.id" :chart="chart" />
  </div>
</template>

<script>

import { post } from '@/api/panel/panel'
import ChartComponent from '@/views/chart/components/ChartComponent.vue'
export default {
  name: 'UserView',
  components: { ChartComponent },
  props: {
    element: {
      type: Object
    }
  },
  data() {
    return {
      chart: {},
      showCard: false

    }
  },
  created() {
    const id = this.element.propValue.viewId
    debugger
    this.$nextTick(() => {
      // 获取eChar数据
      this.getData(id)
    })
  },
  mounted() {

  },
  methods: {
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, null).then(response => {
          // 将视图传入echart组件
          this.chart = response.data
          this.showCard = true
        })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.rect-shape {
    width: 100%;
    height: 100%;
    overflow: auto;
}
.chart-class{
  height: 100%;
}
</style>
