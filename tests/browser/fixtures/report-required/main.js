import ReportForm from '@/views/menu/system/report/form/ReportForm.vue'
import { reportSaveState } from './report-api'
import { createApp, reactive, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import ConditionDefaultSet from '@/views/menu/system/report/form/ConditionDefaultSet.vue'
import {
  hasRequiredFilterValue,
  missingRequiredFilters,
} from '@/views/menu/system/report/form/requiredFilter'
const source = {
  id: 'date',
  name: '必填日期',
  required: true,
  displayType: '1',
  timeType: 'fixed',
  defaultValueCheck: true,
  defaultValue: '2026-11-13',
  auto: true,
}
createApp({
  components: { ConditionDefaultSet, ReportForm },
  setup() {
    const drawer = ref()
    const reportForm = ref()
    const filters = reactive({
      sourceFilter: [source],
      defaultFilter: [{ ...source }],
      sourceDefaultFilter: [{ ...source }],
    })
    const confirmed = ref(0)
    window.reportRequired = {
      hasRequiredFilterValue,
      missingRequiredFilters,
      filters,
    }
    window.reportSaveState = reportSaveState
    return { drawer, filters, confirmed, reportForm, reportSaveState }
  },
  template: `<button @click="reportForm.reportFormVisible = true">打开报告</button><output data-testid="saved">{{ reportSaveState.saved }}</output><ReportForm ref="reportForm" /><button @click="drawer.open()">打开参数</button><button @click="filters.defaultFilter[0].defaultValue = null">清空日期</button><button @click="filters.defaultFilter[0].defaultValue = '2026-11-13'">恢复日期</button><output data-testid="confirmed">{{ confirmed }}</output><ConditionDefaultSet ref="drawer" :filter-info="filters" @reportFilterChange="confirmed++" />`,
})
  .use(ElementPlus)
  .mount('#app')
