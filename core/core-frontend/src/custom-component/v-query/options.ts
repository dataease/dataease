import type { ComponentInfo } from '@/api/chart'
import { guid } from '@/views/visualized/data/dataset/form/util.js'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()

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
    relativeToCurrentType: 'date',
    around: 'f',
    parametersStart: null,
    parametersEnd: null,
    arbitraryTime: new Date(),
    timeNumRange: 0,
    relativeToCurrentTypeRange: 'date',
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
    name: t('v_query.unnamed'),
    deType: 0,
    type: 'VARCHAR',
    datasetId: ''
  }
  return infoFormat(componentInfo)
}

const attributes = {
  timeType: 'fixed',
  hideConditionSwitching: false,
  required: false,
  defaultMapValue: [],
  mapValue: [],
  parametersStart: null,
  conditionType: 0,
  conditionValueOperatorF: 'eq',
  conditionValueF: '',
  conditionValueOperatorS: 'like',
  conditionValueS: '',
  resultMode: 0,
  defaultConditionValueOperatorF: 'eq',
  defaultConditionValueF: '',
  defaultConditionValueOperatorS: 'like',
  defaultConditionValueS: '',
  parametersEnd: null,
  relativeToCurrent: 'custom',
  timeNum: 0,
  relativeToCurrentRange: 'custom',
  relativeToCurrentType: 'year',
  around: 'f',
  arbitraryTime: new Date(),
  timeNumRange: 0,
  relativeToCurrentTypeRange: 'year',
  aroundRange: 'f',
  treeDatasetId: '',
  displayId: '',
  sortId: '',
  sort: 'asc',
  arbitraryTimeRange: new Date(),
  setTimeRange: false,
  showEmpty: false,
  defaultNumValueStart: null,
  defaultNumValueEnd: null,
  numValueEnd: null,
  numValueStart: null,
  displayFormat: 0,
  timeRange: {
    intervalType: 'none',
    dynamicWindow: false,
    maximumSingleQuery: 0,
    regularOrTrends: 'fixed',
    regularOrTrendsValue: '',
    relativeToCurrent: 'custom',
    timeNum: 0,
    relativeToCurrentType: 'year',
    around: 'f',
    timeNumRange: 0,
    relativeToCurrentTypeRange: 'year',
    aroundRange: 'f'
  },
  oldTreeLoad: false,
  treeCheckedList: [],
  defaultValueFirstItem: false,
  treeFieldList: []
}

export { infoFormat, addQueryCriteriaConfig, attributes }
