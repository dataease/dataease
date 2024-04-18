import type { ComponentInfo } from '@/api/chart'
import { guid } from '@/views/visualized/data/dataset/form/util.js'

const infoFormat = (obj: ComponentInfo) => {
  const { id, name, deType, type, datasetId } = obj
  return {
    id: guid(),
    name,
    showError: true,
    timeGranularity: 'date',
    timeGranularityMultiple: 'datetimerange',
    field: {
      id,
      type,
      name,
      deType
    },
    displayId: id,
    sortId: '',
    sort: 'asc',
    defaultMapValue: [],
    mapValue: [],
    conditionType: 0,
    conditionValueOperatorF: 'eq',
    conditionValueF: '',
    conditionValueOperatorS: 'like',
    conditionValueS: '',
    defaultConditionValueOperatorF: 'eq',
    defaultConditionValueF: '',
    defaultConditionValueOperatorS: 'like',
    defaultConditionValueS: '',
    timeType: 'fixed',
    relativeToCurrent: 'custom',
    required: false,
    timeNum: 0,
    relativeToCurrentType: 'year',
    around: 'f',
    parametersStart: null,
    parametersEnd: null,
    arbitraryTime: new Date(),
    timeNumRange: 0,
    relativeToCurrentTypeRange: 'year',
    aroundRange: 'f',
    arbitraryTimeRange: new Date(),
    auto: false,
    defaultValue: undefined,
    selectValue: undefined,
    optionValueSource: 0,
    valueSource: [],
    dataset: {
      id: datasetId,
      name: '',
      fields: []
    },
    visible: true,
    defaultValueCheck: false,
    multiple: false,
    displayType: '0',
    checkedFields: [],
    parameters: [],
    parametersCheck: false,
    parametersList: [],
    checkedFieldsMap: {}
  }
}

const addQueryCriteriaConfig = () => {
  const componentInfo: ComponentInfo = {
    id: '',
    name: '未命名',
    deType: 0,
    type: 'VARCHAR',
    datasetId: ''
  }
  return infoFormat(componentInfo)
}

export { infoFormat, addQueryCriteriaConfig }
