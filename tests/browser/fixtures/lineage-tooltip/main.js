import { createApp, h, onMounted, ref } from 'vue'
import Chart from '@/views/menu/system/association/Chart.vue'

const info = {
  queryType: 'datasource',
  num: 'ds1',
  label: 'SQLServer',
  nodeData: { source: '0' }
}

const res = {
  data: {
    list: [
      {
        id: 'ds1',
        name: 'SQLServer',
        type: 'datasource',
        subRelation: [
          {
            id: 'set1',
            name: 'sqlserver',
            type: 'dataset',
            subRelation: [{ id: 'dv1', name: '新建仪表板', type: 'dataV' }]
          }
        ]
      }
    ]
  }
}

createApp({
  setup() {
    const chart = ref()
    const chartSize = { width: '900px', height: '500px' }
    const render = () => chart.value?.getChartData({ info, res })
    const onClickNode = () => setTimeout(render, 50)

    onMounted(() => setTimeout(render, 0))

    return () => h(Chart, { ref: chart, chartSize, onClickNode })
  }
}).mount('#app')
