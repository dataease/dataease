import { PluginAdapter } from '../../types/adapter'
import FilterEditor from './components/editor/FilterEditor.vue'

export class FilterAdapter extends PluginAdapter {
  constructor() {
    super('filter')
  }

  getEditor() {
    return FilterEditor
  }
}

export default new FilterAdapter()
