import { createApp, h, onMounted, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import zhCn from 'element-plus-secondary/es/locale/lang/zh-cn'
import 'element-plus-secondary/dist/index.css'
import '@/style/index.less'
import DatasetTableDetails from 'dataset-table-details'

createApp({
  setup() {
    const details = ref()
    onMounted(() => {
      details.value.open({
        tableName: 'excel_data_b2d2db2fd9',
        name: '源表备注',
        datasourceId: 'fixture-datasource',
        type: 'db'
      })
    })
    return () => h(DatasetTableDetails, { ref: details })
  }
})
  .use(ElementPlus, { namespace: 'ed', locale: zhCn })
  .mount('#app')
