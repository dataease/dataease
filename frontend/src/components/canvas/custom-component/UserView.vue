<template>
  <div class="rect-shape">
    <chart-component :ref="element.propValue.id" class="chart-class" :chart="chart" />
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
      chart: {}
    }
  },
  created() {
    this.getData(this.element.propValue.viewId)
  },
  mounted() {

  },
  methods: {
    getData(id) {
      if (id) {
        post('/chart/view/getData/' + id, {}).then(response => {
          // 将视图传入echart组件
          this.chart = response.data
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
