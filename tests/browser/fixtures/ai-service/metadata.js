import { createApp, h, ref } from 'vue'
import ElementPlus from 'element-plus-secondary'
import 'element-plus-secondary/dist/index.css'
import DatasourceSemantic from '@/views/visualized/data/datasource/DatasourceSemantic.vue'
import DatasourceRelation from '@/views/visualized/data/datasource/DatasourceRelation.vue'
import DatasetEditorAssistant from '@/views/visualized/data/dataset/form/DatasetEditorAssistant.vue'

const view = new URLSearchParams(location.search).get('view')
const selection = ref('')
const editor = {
  host: document.body,
  resourceId: '11',
  resourceLabel: 'Controlled customer orders',
  isCross: false,
  draftRevision: 0,
  systemVariableNames: [],
  readDraft: () => ''
}
const relation = view === 'relations'
createApp({
  render: () =>
    view === 'editor'
      ? h(DatasetEditorAssistant, editor)
      : h(relation ? DatasourceRelation : DatasourceSemantic, {
          datasourceId: '11',
          datasourceName: editor.resourceLabel,
          canManage: true,
          [relation ? 'tableFilter' : 'selectedTable']: selection.value,
          [relation ? 'onUpdate:tableFilter' : 'onUpdate:selectedTable']: value => {
            selection.value = value
          }
        })
})
  .use(ElementPlus, { namespace: 'ed' })
  .component('Icon', { render: () => h('span') })
  .mount('#app')
