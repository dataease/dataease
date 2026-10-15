import { createApp, h, ref, shallowRef } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import { store } from '@/store'
import { setupI18n } from '@/plugins/vue-i18n'
import { useLocaleStoreWithOut } from '@/store/modules/locale'

const left = { id: 'salary', name: '薪资', summary: 'sum' }
const right = { id: 'commission', name: '提成', summary: 'sum' }
const fixed = {
  name: '历史阈值',
  field: '0',
  fieldId: left.id,
  curField: left,
  boundFieldId: left.id,
  followField: true,
  yAxisType: 'left',
  value: 100,
  summary: 'avg',
  color: '#ff0000',
  lineType: 'solid',
  fontSize: '10'
}
let AssistLineEdit
const app = createApp({
  setup() {
    const mode = ref('group')
    const key = ref(0)
    const lines = shallowRef([])
    const draft = shallowRef([])
    const cases = {
      group: '单指标分组图',
      dual: '双轴图',
      multi: '多指标双轴图',
      legacy: '历史分组图',
      stale: '失效绑定双轴图',
      unbound: '历史未绑定双轴图'
    }
    const open = kind => {
      mode.value = kind
      lines.value =
        kind === 'legacy'
          ? [structuredClone(fixed)]
          : kind === 'stale'
          ? [{ ...structuredClone(fixed), boundFieldId: 'removed' }]
          : kind === 'unbound'
          ? [{ ...structuredClone(fixed), boundFieldId: '' }]
          : []
      key.value++
    }
    return () =>
      h('main', { style: 'max-width:980px;margin:28px auto;font:14px sans-serif' }, [
        h('h1', '辅助线配置回归'),
        h(
          'nav',
          { style: 'display:flex;gap:12px;margin-bottom:24px' },
          Object.entries(cases).map(([kind, label]) =>
            h('button', { onClick: () => open(kind) }, label)
          )
        ),
        h('h2', cases[mode.value]),
        h(AssistLineEdit, {
          key: key.value,
          chart: { type: ['group', 'legacy'].includes(mode.value) ? 'bar-group' : 'chart-mix' },
          line: lines.value,
          quotaFields: mode.value === 'multi' ? [left, { ...right, id: 'bonus' }] : [left],
          quotaExtFields: [right],
          useQuotaExt: !['group', 'legacy'].includes(mode.value),
          onOnAssistLineChange: value => {
            draft.value = JSON.parse(JSON.stringify(value))
          }
        }),
        h(
          'button',
          {
            onClick: () => {
              lines.value = structuredClone(draft.value)
              key.value++
            }
          },
          '保存并重开'
        ),
        h('pre', { id: 'draft' }, JSON.stringify(draft.value, null, 2))
      ])
  }
})
app.use(store)
app.use(ElementPlus)
const locale = useLocaleStoreWithOut()
locale.customLoaded = true
locale.setLang('zh-CN')
await setupI18n(app)
AssistLineEdit = (
  await import('@/views/chart/components/editor/editor-senior/components/dialog/AssistLineEdit.vue')
).default
const { checkVisibility } = await import('./visibility')
document.body.dataset.visibilityChecks = String(checkVisibility())
app.mount('#app')
