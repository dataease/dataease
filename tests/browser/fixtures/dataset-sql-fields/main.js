import { Base64 } from 'js-base64'
import { createApp, h, provide, reactive, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import AddSql from 'add-sql-real'

createApp({
  setup() {
    provide('isCross', ref(false))
    provide('singleVal', ref(false))
    provide('currentDsId', ref(''))
    const sqlNode = reactive({
      sql: Base64.encode('SELECT 1'),
      tableName: '',
      datasourceId: 'ds1',
      id: 'sql-1',
      variables: []
    })
    return () => h(AddSql, { sqlNode })
  }
})
  .use(ElementPlus, { namespace: 'ed' })
  .mount('#app')
