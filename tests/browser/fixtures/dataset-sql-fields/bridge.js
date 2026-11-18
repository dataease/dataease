import { defineComponent, h } from 'vue'

const Empty = defineComponent({ name: 'EmptyStub', render: () => null })

const codeCom = {
  state: { doc: { length: 0, toString: () => '' } },
  viewState: { state: { selection: { ranges: [{ from: 0 }] } } },
  dispatch: () => {},
  focus: () => {},
  destroy: () => {}
}

export default defineComponent({
  name: 'CodeMirrorStub',
  setup(_, { expose }) {
    expose({ codeComInit: () => codeCom })
    return () => h('div', { class: 'code-mirror-stub' })
  }
})

export const Icon = Empty
export const EmptyBackground = Empty
export const useI18n = () => ({ t: key => key })
export const isDesktop = () => false
export const fieldType = { 0: 'text', 1: 'value', 2: 'value', 3: 'time', 4: 'time' }
export const iconFieldMap = { text: Empty, value: Empty, time: Empty }
export const timestampFormatDate = value => value
export const defaultValueScopeList = []
export const fieldOptions = []
export const removeSqlComments = sql => sql
export const searchVariableApi = () => Promise.resolve({ data: [] })

const tables = [
  {
    datasourceId: 'ds1',
    name: '订单主表',
    tableName: 'orders',
    type: 'TABLE'
  }
]

export const getDatasourceList = () =>
  Promise.resolve([{ id: 'ds1', name: 'PG', children: [] }])

export const getTables = () => Promise.resolve(tables)

export const getTableField = () =>
  Promise.resolve([
    { originName: 'order_id', description: '订单 ID', deType: 0 },
    { originName: 'order_date', description: '下单日期', deType: 3 },
    { originName: 'amount', description: '订单金额', deType: 1 }
  ])

export const getPreviewSql = () => Promise.resolve({ data: { data: [], fields: [] } })
