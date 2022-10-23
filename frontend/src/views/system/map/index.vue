<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <div
      id="maptest"
      style="width: 100%;height:100%;"
    />
  </layout-content>
</template>

<script>
import LayoutContent from '@/components/business/LayoutContent'
import { get } from '@/api/panel/panel'
export default {
  components: { LayoutContent },
  data() {
    return {
      myChart: null,
      defaultPcode: '100000',
      mapurl: '/api/map/resourceFull/'
    }
  },
  mounted() {
    const chartDom = document.getElementById('maptest')
    this.myChart = this.$echarts.init(chartDom)
    this.initMap(this.defaultPcode)
  },
  methods: {
    initMap(pcode) {
      this.myChart.showLoading()
      get(this.mapurl + pcode).then(res => {
        this.myChart.hideLoading()
        const geoJson = res.data
        this.$echarts.registerMap('HK', geoJson)

        this.myChart.setOption({

          series: [
            {
              type: 'map',
              map: 'MAP', // 自定义扩展图表类型
              roam: true,
              label: {
                show: false
              },
              nameProperty: 'name'

            }
          ]
        })
        this.queryAreaCodes(pcode).then(res => {
          const areaEntities = res.data

          this.myChart.on('click', param => {
            const name = param.name
            for (let index = 0; index < areaEntities.length; index++) {
              const element = areaEntities[index]
              if (element.name === name) {
                this.initMap(element.code)
              }
            }
          })
        })
      })
    },
    queryAreaCodes(pcode) {
      return get('/api/map/areaEntitys/' + pcode)
    }

  }
}
</script>
