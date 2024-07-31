getLastStart
<script lang="ts" setup>
import {
  ref,
  reactive,
  nextTick,
  computed,
  shallowRef,
  toRefs,
  watch,
  defineAsyncComponent,
  provide
} from 'vue'
import { storeToRefs } from 'pinia'
import { addQueryCriteriaConfig } from './options'
import { getCustomTime } from './time-format'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { getThisStart, getLastStart, getAround } from './time-format-dayjs'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useI18n } from '@/hooks/web/useI18n'
import { fieldType } from '@/utils/attr'
import { ElMessage, ElSelect, ElMessageBox } from 'element-plus-secondary'
import type { DatasetDetail } from '@/api/dataset'
import { getDsDetailsWithPerm, getSqlParams, listFieldsWithPermissions } from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import TreeFieldDialog from '@/custom-component/v-query/TreeFieldDialog.vue'
import { cloneDeep } from 'lodash-es'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import draggable from 'vuedraggable'
import type { ManipulateType } from 'dayjs'
import dayjs from 'dayjs'
import ConditionDefaultConfiguration from '@/custom-component/v-query/ConditionDefaultConfiguration.vue'

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasViewInfo } = storeToRefs(dvMainStore)
const defaultConfigurationRef = ref(null)
interface DatasetField {
  type?: string
  innerType?: string
  title: string
  id: string
  tableId: string
}

const props = defineProps({
  queryElement: {
    type: Object,
    default() {
      return {
        id: null,
        propValue: []
      }
    }
  }
})
const dialogVisible = ref(false)
const renameInput = ref([])
const valueSource = ref([])
const conditions = ref([])
const checkAll = ref(false)
const activeConditionForRename = reactive({
  id: '',
  name: '',
  visible: false
})
const datasetMap = {}
const snapshotStore = snapshotStoreWithOut()

const dfsComponentData = () => {
  let arr = componentData.value.filter(
    com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
  )
  componentData.value.forEach(ele => {
    if (ele.innerType === 'DeTabs') {
      ele.propValue.forEach(itx => {
        arr = [
          ...arr,
          ...itx.componentData.filter(
            com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
          )
        ]
      })
    } else if (ele.component === 'Group') {
      arr = [
        ...arr,
        ele.propValue.filter(
          com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
        )
      ]
      ele.propValue.forEach(element => {
        if (element.innerType === 'DeTabs') {
          element.propValue.forEach(itx => {
            arr = [
              ...arr,
              ...itx.componentData.filter(
                com => !['VQuery', 'DeTabs'].includes(com.innerType) && com.component !== 'Group'
              )
            ]
          })
        }
      })
    }
  })

  return arr.flat()
}

const datasetFieldList = computed(() => {
  return dfsComponentData()
    .map(ele => {
      const obj = canvasViewInfo.value[ele.id]
      if (!obj) return null
      const { id, title, tableId, type } = obj as DatasetField
      return !!id && !!tableId
        ? {
            id,
            type,
            title,
            tableId
          }
        : null
    })
    .filter(ele => !!ele)
})
const setCascadeDefault = val => {
  conditions.value.forEach(ele => {
    if (
      ele.optionValueSource === 1 &&
      [0, 2, 5].includes(+ele.displayType) &&
      val.includes(ele.id)
    ) {
      ele.selectValue = Array.isArray(ele.selectValue) ? [] : undefined
      ele.defaultValue = Array.isArray(ele.defaultValue) ? [] : undefined
      ele.mapValue = Array.isArray(ele.mapValue) ? [] : undefined
      ele.defaultMapValue = Array.isArray(ele.defaultMapValue) ? [] : undefined
    }
  })
}
let cascadeArr = []
const saveCascade = arr => {
  cascadeArr = arr
}
const getCascadeList = () => {
  return cascadeArr
}
provide('set-cascade-default', setCascadeDefault)
provide('cascade-list', getCascadeList)

const curComponent = ref()
const manual = ref()
const activeCondition = ref('')
const isIndeterminate = ref(false)
const datasetTree = shallowRef([])
const fields = ref<DatasetDetail[]>()

const { queryElement } = toRefs(props)
const getDetype = (id, arr) => {
  return arr.flat().find(ele => ele.id === id)?.deType
}
const showConfiguration = computed(() => {
  if (!curComponent.value) return false
  if (!curComponent.value.checkedFields?.length) return false
  return curComponent.value.checkedFields.some(ele => {
    return !!curComponent.value.checkedFieldsMap[ele]
  })
})

const showTypeError = computed(() => {
  if (!curComponent.value) return false
  if (!curComponent.value.checkedFields?.length) return false
  if (!fields.value?.length) return false
  if (!!curComponent.value.parameters.length && isTimeParameter.value) {
    const timeArr = curComponent.value.parameters.map(ele => ele.type[1])
    const [typeOne] = timeArr
    if (timeArr.some(ele => ele !== typeOne)) {
      return true
    }
  }
  let displayTypeField = null
  return curComponent.value.checkedFields.some(id => {
    const arr = fields.value.find(ele => ele.componentId === id)
    const checkId = curComponent.value.checkedFieldsMap?.[id]
    const field = Object.values(arr?.fields || {})
      .flat()
      .find(ele => checkId === ele.id)
    if (!field) return false
    if (displayTypeField === null) {
      displayTypeField = field?.deType
      return false
    }
    return displayTypeField !== field?.deType
  })
})

const showDatasetError = computed(() => {
  if (!curComponent.value || curComponent.value.displayType !== '9') return false
  if (!curComponent.value.checkedFields?.length) return false
  if (!fields.value?.length) return false
  let displayField = null
  return curComponent.value.checkedFields.some(id => {
    const arr = fields.value.find(itx => itx.componentId === id)
    const field = arr.id
    if (!field) return false
    if (displayField === null) {
      displayField = field
      return false
    }
    return displayField !== field
  })
})
const typeList = [
  {
    label: '重命名',
    command: 'rename'
  },
  {
    label: '删除',
    command: 'del'
  }
]

const handleCheckAllChange = (val: boolean) => {
  curComponent.value.checkedFields = val ? fields.value.map(ele => ele.componentId) : []
  isIndeterminate.value = false
}

const setTreeDefault = () => {
  if (!!curComponent.value.checkedFields.length) {
    let checkId = ''
    let tableId = ''
    let comId = ''
    fields.value.forEach(ele => {
      if (
        curComponent.value.checkedFields.includes(ele.componentId) &&
        curComponent.value.checkedFieldsMap[ele.componentId] &&
        !checkId
      ) {
        checkId = curComponent.value.checkedFieldsMap[ele.componentId]
        comId = ele.componentId
        tableId = datasetFieldList.value.find(itx => itx.id === ele.componentId)?.tableId
      }
    })
    if (checkId && tableId) {
      const componentObj = fields.value.find(ele => ele.componentId === comId)
      const fieldArr =
        curComponent.value.optionValueSource === 0
          ? componentObj?.fields?.dimensionList
          : (fields.value.find(itx => itx.id === tableId) || {}).fields?.dimensionList
      fields.value.forEach(ele => {
        if (curComponent.value.checkedFields.includes(ele.componentId)) {
          if (datasetFieldList.value.find(itx => itx.id === ele.componentId)?.tableId === tableId) {
            curComponent.value.checkedFieldsMap[ele.componentId] = checkId
          }
        }
      })
      const fieldObj = fieldArr.find(element => element.id === checkId)
      if (!!curComponent.value.treeFieldList.length) {
        const [fir] = curComponent.value.treeFieldList
        if (fir && fir.field !== checkId) {
          curComponent.value.treeFieldList = [fieldObj]
        }
      } else if (fieldObj) {
        curComponent.value.treeFieldList = [fieldObj]
      }
    }
  }
}
const handleCheckedFieldsChange = (value: string[]) => {
  handleDialogClick()
  const checkedCount = value.length
  checkAll.value = checkedCount === fields.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < fields.value.length
  if (curComponent.value.displayType === '8') return
  setType()
}

const handleCheckedFieldsChangeTree = (value: string[]) => {
  handleDialogClick()
  const checkedCount = value.length
  checkAll.value = checkedCount === fields.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < fields.value.length
  if (curComponent.value.displayType === '8') return
  if (curComponent.value.displayType === '9') {
    setTreeDefault()
    return
  }
  setType()
}

const setParameters = () => {
  const fieldArr = Object.values(curComponent.value.checkedFieldsMap).filter(ele => !!ele)
  curComponent.value.parameters = fields.value
    .map(ele => Object.values(ele?.fields || {}).flat())
    .flat()
    .filter(ele => fieldArr.includes(ele.id) && !!ele.variableName)
  nextTick(() => {
    if (isTimeParameter.value) {
      curComponent.value.timeGranularity = typeTimeMap[curComponent.value.parameters[0].type[1]]
    }

    if (!!curComponent.value.parameters.length) {
      curComponent.value.conditionType = 0
      if (curComponent.value.optionValueSource === 0) {
        curComponent.value.optionValueSource = 1
      }
    }
  })
  setType()
  if (curComponent.value.displayType === '9') {
    setTreeDefault()
  }
}

const setType = () => {
  if (curComponent.value.checkedFields?.length) {
    const [id] = curComponent.value.checkedFields
    const arr = fields.value.find(ele => ele.componentId === id)
    const checkId = curComponent.value.checkedFieldsMap?.[id]
    const field = Object.values(arr?.fields || {})
      .flat()
      .find(ele => checkId === ele.id)

    if (field?.deType !== undefined) {
      let displayType = curComponent.value.displayType
      if (curComponent.value.displayType === '9') {
        return
      }
      if (!(field?.deType === 1 && curComponent.value.displayType === '7')) {
        curComponent.value.displayType = `${[3, 4].includes(field?.deType) ? 2 : field?.deType}`
      }
      if (
        displayType !== curComponent.value.displayType &&
        !([3, 4].includes(+displayType) && +curComponent.value.displayType === 2)
      ) {
        setTypeChange()
      }
    }
  }
}

const setTypeChange = () => {
  handleDialogClick()
  nextTick(() => {
    curComponent.value.field.id = ''
    defaultConfigurationRef.value?.displayTypeChange?.()
    if (
      +curComponent.value.displayType === 7 &&
      ['yearrange', 'monthrange', 'daterange', 'datetimerange'].includes(
        curComponent.value.timeGranularity
      )
    ) {
      curComponent.value.timeGranularityMultiple = curComponent.value.timeGranularity
    }

    if (curComponent.value.displayType === '9') {
      setTreeDefault()
    }
  })
}

const isTimeParameter = computed(() => {
  return curComponent.value.parameters?.some(ele => ele.deType === 1 && !!ele.variableName)
})

const timeList = [
  {
    label: '年',
    value: 'year'
  },
  {
    label: '年月',
    value: 'month'
  },
  {
    label: '年月日',
    value: 'date'
  },
  {
    label: '年月日时分秒',
    value: 'datetime'
  }
]

const typeTimeMap = {
  'DATETIME-YEAR': 'year',
  'YYYY-MM': 'month',
  'YYYY/MM': 'month',
  'YYYY-MM-DD': 'date',
  'YYYY/MM/DD': 'date',
  'YYYY-MM-DD HH:mm:ss': 'datetime',
  'YYYY/MM/DD HH:mm:ss': 'datetime'
}

const timeParameterList = computed(() => {
  if (!isTimeParameter.value) return timeList
  const [year, y] = curComponent.value.parameters?.filter(
    ele => ele.deType === 1 && !!ele.variableName
  )[0].type
  return timeList.filter(ele => ele.value === (typeTimeMap[y] || typeTimeMap[year]))
})

const cancelClick = () => {
  handleDialogClick()
  dialogVisible.value = false
}

const initDataset = () => {
  getDatasetTree({}).then(res => {
    datasetTree.value = (res as unknown as Tree[]) || []
  })
}

const computedTree = computed(() => {
  if (datasetTree.value[0]?.id === '0') {
    return dfs(datasetTree.value[0].children)
  }
  return dfs(datasetTree.value)
})
let newDatasetId = ''
let oldDatasetId = ''
const handleCurrentChange = node => {
  if (!curComponent.value.dataset?.id) return
  let id = `${curComponent.value.dataset?.id}--${curComponent.value.id}`
  let isChange = false
  for (let i in cascadeArr) {
    const [fir, sec] = cascadeArr[i]
    if (fir?.datasetId.includes(id) || sec?.datasetId.includes(id)) {
      isChange = true
    }
  }
  if (!isChange) return
  oldDatasetId = curComponent.value.dataset?.id
  newDatasetId = node.id
}

const confirmIdChange = () => {
  curComponent.value.dataset.id = newDatasetId
  clearCascadeArrDataset(`${oldDatasetId}--${curComponent.value.id}`)
  newDatasetId = ''
  oldDatasetId = ''
  handleDatasetChange()
}

const handleDatasetChange = () => {
  if (!!newDatasetId && !!oldDatasetId) {
    curComponent.value.dataset.id = oldDatasetId
    ElMessageBox.confirm(
      '数据集的修改，会导致级联配置失效，因此对应的级联关系将被清除，确定修改吗？',
      {
        confirmButtonType: 'primary',
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        autofocus: false,
        showClose: false
      }
    ).then(() => {
      confirmIdChange()
    })
    return
  }
  curComponent.value.field.id = ''
  curComponent.value.displayId = ''
  curComponent.value.sortId = ''
  getOptions(curComponent.value.dataset.id, curComponent.value)
}

const handleFieldChange = () => {
  if (!curComponent.value.defaultValueCheck) return
  curComponent.value.defaultValue = curComponent.value.multiple ? [] : undefined
  if (!curComponent.value.displayId) {
    curComponent.value.displayId = curComponent.value.field.id
  }
}

const handleValueSourceChange = () => {
  curComponent.value.defaultValue = curComponent.value.multiple ? [] : undefined
  multipleChange(curComponent.value.multiple)
}

const multipleChange = (val: boolean, isMultipleChange = false) => {
  if (isMultipleChange) {
    curComponent.value.defaultValue = val ? [] : undefined
  }
  const { defaultValue } = curComponent.value
  if (Array.isArray(defaultValue)) {
    curComponent.value.selectValue = val ? defaultValue : undefined
  } else {
    curComponent.value.selectValue = val
      ? defaultValue !== undefined
        ? [defaultValue]
        : []
      : defaultValue
  }

  curComponent.value.multiple = val
}

const isInRange = (ele, startWindowTime, timeStamp) => {
  const {
    intervalType,
    regularOrTrends,
    regularOrTrendsValue,
    relativeToCurrent,
    timeNum,
    relativeToCurrentType,
    around,
    dynamicWindow,
    maximumSingleQuery,
    timeNumRange,
    relativeToCurrentTypeRange,
    aroundRange
  } = ele.timeRange || {}
  let isDynamicWindowTime = false
  const noTime = ele.timeGranularityMultiple.split('time').join('').split('range')[0]
  const queryTimeType = noTime === 'date' ? 'day' : (noTime as ManipulateType)
  if (startWindowTime && dynamicWindow) {
    isDynamicWindowTime =
      dayjs(startWindowTime)
        .add(maximumSingleQuery, queryTimeType)
        .startOf(queryTimeType)
        .valueOf() -
        1000 <
      timeStamp
  }

  if (intervalType === 'none') {
    if (dynamicWindow) return isDynamicWindowTime
    return false
  }
  let startTime
  if (relativeToCurrent === 'custom') {
    startTime = getAround(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
  } else {
    switch (relativeToCurrent) {
      case 'thisYear':
        startTime = getThisStart('year')
        break
      case 'lastYear':
        startTime = getLastStart('year')
        break
      case 'thisMonth':
        startTime = getThisStart('month')
        break
      case 'lastMonth':
        startTime = getLastStart('month')
        break
      case 'today':
        startTime = getThisStart('day')
        break
      case 'yesterday':
        startTime = getLastStart('day')
        break
      case 'monthBeginning':
        startTime = getThisStart('month')
        break
      case 'yearBeginning':
        startTime = getThisStart('year')
        break

      default:
        break
    }
  }
  const startValue = regularOrTrends === 'fixed' ? regularOrTrendsValue : startTime
  if (intervalType === 'start') {
    return startWindowTime < +new Date(startValue) || isDynamicWindowTime
  }

  if (intervalType === 'end') {
    return timeStamp > +new Date(startValue) || isDynamicWindowTime
  }

  if (intervalType === 'timeInterval') {
    const startTime =
      regularOrTrends === 'fixed'
        ? regularOrTrendsValue[0]
        : getAround(relativeToCurrentType, around === 'f' ? 'subtract' : 'add', timeNum)
    const endTime =
      regularOrTrends === 'fixed'
        ? regularOrTrendsValue[1]
        : getAround(
            relativeToCurrentTypeRange,
            aroundRange === 'f' ? 'subtract' : 'add',
            timeNumRange
          )
    return (
      startWindowTime < +new Date(startTime) - 1000 ||
      timeStamp > +new Date(endTime) ||
      isDynamicWindowTime
    )
  }
}

const CascadeDialog = defineAsyncComponent(() => import('./QueryCascade.vue'))
const cascadeDialog = ref()
const openCascadeDialog = () => {
  const cascadeMap = conditions.value
    .filter(
      ele =>
        [0, 2, 5].includes(+ele.displayType) &&
        ele.optionValueSource === 1 &&
        !!ele.checkedFields?.length &&
        !!Object.values(ele.checkedFieldsMap).filter(item => !!item).length
    )
    .reduce((pre, next) => {
      pre[next.id] = {
        datasetId: next.dataset.id,
        name: next.name,
        queryId: next.id,
        fieldId: next.field.id,
        deType: (datasetMap[next.dataset.id]?.fields?.dimensionList || next.dataset.fields).find(
          ele => ele.id === next.field.id
        )?.deType
      }
      return pre
    }, {})
  cascadeDialog.value.init(cascadeMap, cascadeArr)
}

const clearCascadeArrDataset = id => {
  for (let i in cascadeArr) {
    const [fir, sec] = cascadeArr[i]
    if (fir?.datasetId.includes(id)) {
      cascadeArr[i] = []
    } else if (sec?.datasetId.includes(id)) {
      cascadeArr[i] = [fir]
    }
  }
  cascadeArr = cascadeArr.filter(ele => !!ele.length)
}

const indexCascade = ' 一二三四五'

const validateConditionType = ({
  defaultConditionValueF,
  defaultConditionValueS,
  conditionType
}) => {
  if (conditionType === 0) {
    return defaultConditionValueF === ''
  } else {
    return defaultConditionValueF === '' || defaultConditionValueS === ''
  }
}

const setParams = ele => {
  const {
    defaultConditionValueOperatorF,
    defaultConditionValueF,
    defaultConditionValueOperatorS,
    defaultConditionValueS
  } = ele
  ele.conditionValueOperatorF = defaultConditionValueOperatorF
  ele.conditionValueF = defaultConditionValueF
  ele.conditionValueOperatorS = defaultConditionValueOperatorS
  ele.conditionValueS = defaultConditionValueS
}

const validate = () => {
  return conditions.value.some(ele => {
    if (ele.auto) return false
    if (!ele.checkedFields?.length || ele.checkedFields.some(itx => !ele.checkedFieldsMap[itx])) {
      ElMessage.error('请先勾选需要联动的图表及字段')
      return true
    }

    if (ele.required) {
      if (ele.displayType === '8') {
        setParams(ele)
        const result = validateConditionType(ele)
        if (result) {
          ElMessage.error('查询条件为必填项,默认值不能为空')
        }
        return result
      }

      if (!ele.defaultValueCheck) {
        ElMessage.error('查询条件为必填项,默认值不能为空')
        return true
      }

      if (
        (Array.isArray(ele.defaultValue) && !ele.defaultValue.length) ||
        (ele.defaultValue !== 0 && !ele.defaultValue)
      ) {
        ElMessage.error('查询条件为必填项,默认值不能为空')
        return true
      }
    }

    if (ele.displayType === '8') {
      setParams(ele)
      return false
    }

    if (!ele.defaultValueCheck) {
      const isMultiple = +ele.displayType === 7 || ele.multiple
      ele.selectValue = isMultiple ? [] : undefined
      ele.defaultValue = isMultiple ? [] : undefined
    }

    if (ele.displayType === '1') {
      if (!ele.defaultValueCheck) return false
      if (ele.timeType === 'fixed') {
        if (!ele.defaultValue) {
          ElMessage.error('默认时间不能为空!')
          return true
        }
      }
    }

    if (+ele.displayType === 7) {
      if (!ele.defaultValueCheck) return false
      if (ele.timeType === 'fixed') {
        const [s, e] = ele.defaultValue || []
        if (!s || !e) {
          ElMessage.error('默认时间不能为空!')
          return true
        }
      }
      const {
        timeNum,
        relativeToCurrentType,
        around,
        timeGranularityMultiple,
        arbitraryTime,
        timeGranularity,
        timeNumRange,
        relativeToCurrentTypeRange,
        aroundRange,
        arbitraryTimeRange,
        timeType
      } = ele

      const startTime =
        timeType === 'dynamic'
          ? getCustomTime(
              timeNum,
              relativeToCurrentType,
              timeGranularity,
              around,
              arbitraryTime,
              timeGranularityMultiple,
              'start-config'
            )
          : new Date(ele.defaultValue[0])
      const endTime =
        timeType === 'dynamic'
          ? getCustomTime(
              timeNumRange,
              relativeToCurrentTypeRange,
              timeGranularity,
              aroundRange,
              arbitraryTimeRange,
              timeGranularityMultiple,
              'end-config'
            )
          : new Date(ele.defaultValue[1])
      if (+startTime > +endTime) {
        ElMessage.error('结束时间必须大于开始时间!')
        return true
      }
      if (!ele.setTimeRange) return false
      if (
        isInRange(
          ele,
          timeGranularityMultiple.includes('time')
            ? dayjs(+startTime).startOf('day').valueOf()
            : +startTime,
          timeGranularityMultiple.includes('time')
            ? dayjs(+endTime).startOf('day').valueOf()
            : +endTime
        )
      ) {
        ElMessage.error('默认值超出日期筛选范围内，请重新设置！')
        return true
      }
      return false
    }

    if ([1].includes(+ele.displayType)) {
      return false
    }

    if (
      ele.displayType !== '9' &&
      ele.optionValueSource === 2 &&
      !ele.valueSource?.filter(ele => !!ele).length
    ) {
      ElMessage.error('手工输入-选项值不能为空')
      return true
    }

    if (ele.displayType !== '9' && ele.optionValueSource === 1 && !ele.field.id) {
      ElMessage.error('请选择数据集的选项值字段')
      return true
    }

    let displayTypeField = null
    if (
      ele.checkedFields.some(id => {
        const arr = fields.value.find(itx => itx.componentId === id)
        const checkId = ele.checkedFieldsMap?.[id]
        const field = Object.values(arr?.fields || {})
          .flat()
          .find(itx => checkId === itx.id)
        if (!field) return false
        if (displayTypeField === null) {
          displayTypeField = field?.deType
          return false
        }
        return displayTypeField !== field?.deType
      })
    ) {
      ElMessage.error('所选字段类型不一致，无法进行查询配置')
      return true
    }
  })
}

const handleBeforeClose = () => {
  defaultConfigurationRef.value?.mult()
  defaultConfigurationRef.value?.single()
  handleDialogClick()
  dialogVisible.value = false
}
const emits = defineEmits(['queryData'])
const confirmClick = () => {
  if (validate()) return
  defaultConfigurationRef.value?.mult()
  defaultConfigurationRef.value?.single()
  handleDialogClick()
  dialogVisible.value = false
  conditions.value.forEach(ele => {
    curComponent.value = ele
    multipleChange(
      ['1', '7'].includes(curComponent.value.displayType)
        ? curComponent.value.displayType === '7'
        : curComponent.value.multiple
    )
  })
  queryElement.value.propValue = []
  nextTick(() => {
    conditions.value.forEach(itx => {
      cascadeArr.forEach(ele => {
        ele.forEach(item => {
          if (item.datasetId.split('--')[1] === itx.id && itx.defaultValueCheck) {
            const val = itx.mapValue
            item.selectValue = Array.isArray(val) ? [...val] : val
            item.currentSelectValue = Array.isArray(val) ? [...val] : val
          }
        })
      })
    })
    queryElement.value.cascade = cloneDeep(cascadeArr)
    cascadeArr = []
    queryElement.value.propValue = cloneDeep(conditions.value)
    snapshotStore.recordSnapshotCache()
    nextTick(() => {
      emits('queryData')
    })
  })
}

const cancelValueSource = () => {
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
  }
  manual.value.hide()
}

const confirmValueSource = () => {
  if (
    valueSource.value.some(ele => {
      if (typeof ele === 'string') {
        return !ele.trim()
      }
      return false
    })
  ) {
    ElMessage.error('手工输入-选项值不能为空')
    return
  }

  curComponent.value.valueSource = cloneDeep(
    valueSource.value.filter(ele => {
      if (typeof ele === 'string') {
        return ele.trim()
      }
      return true
    })
  )
  handleValueSourceChange()
  cancelValueSource()
}

const setCondition = (queryId: string) => {
  conditions.value = cloneDeep(props.queryElement.propValue) || []
  init(queryId)
}

const setConditionOut = () => {
  conditions.value = cloneDeep(props.queryElement.propValue) || []
  addQueryCriteria()
  init(conditions.value[conditions.value.length - 1].id)
}

const setActiveSelectTab = (arr, id) => {
  let activelist = 'dimensionList'
  arr.some((ele, index) => {
    if ((ele || []).some(itx => itx.id === id)) {
      activelist = ['dimensionList', 'quotaList', 'parameterList'][index]
      return true
    }
    return false
  })

  return activelist
}

const init = (queryId: string) => {
  initDataset()
  renameInput.value = []
  handleCondition({ id: queryId })
  cascadeArr = cloneDeep(queryElement.value.cascade || [])
  dialogVisible.value = true
  const datasetFieldIdList = datasetFieldList.value.map(ele => ele.tableId)
  for (const i in datasetMap) {
    if (!datasetFieldIdList.includes(i)) {
      delete datasetMap[i]
    }
  }

  const datasetMapKeyList = Object.keys(datasetMap)

  if (datasetFieldIdList.every(ele => datasetMapKeyList.includes(ele))) {
    fields.value = datasetFieldList.value
      .map(ele => {
        if (!datasetMap[ele.tableId]) return null
        return { ...datasetMap[ele.tableId], componentId: ele.id }
      })
      .filter(ele => !!ele)
  }
  const params = [...new Set(datasetFieldList.value.map(ele => ele.tableId).filter(ele => !!ele))]
  if (!params.length) return
  Promise.all([getDsDetailsWithPerm(params), getSqlParams(params)])
    .then(([dq, p]) => {
      dq.filter(ele => !!ele).forEach(ele => {
        ele.activelist = 'dimensionList'
        ele.fields.parameterList = p.filter(itx => itx.datasetGroupId === ele.id)
        ele.hasParameter = !!ele.fields.parameterList.length
        datasetMap[ele.id] = ele
      })
      fields.value = datasetFieldList.value
        .map(ele => {
          if (!datasetMap[ele.tableId]) return null
          const activeCom = datasetMap[ele.tableId].fields || {}
          const activelist = setActiveSelectTab(
            [activeCom.dimensionList, activeCom.quotaList, activeCom.parameterList],
            curComponent.value.checkedFieldsMap[ele.id]
          )
          return { ...datasetMap[ele.tableId], componentId: ele.id, activelist }
        })
        .filter(ele => !!ele)
    })
    .finally(() => {
      handleCheckedFieldsChange(curComponent.value.checkedFields)
    })
}

const weightlessness = () => {
  valueSource.value = Array.from(new Set(valueSource.value))
}

const parameterCompletion = () => {
  const attributes = {
    timeType: 'fixed',
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
    relativeToCurrentType: 'year',
    around: 'f',
    arbitraryTime: new Date(),
    timeNumRange: 0,
    relativeToCurrentTypeRange: 'year',
    aroundRange: 'f',
    displayId: '',
    sortId: '',
    sort: 'asc',
    arbitraryTimeRange: new Date(),
    setTimeRange: false,
    showEmpty: false,
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
    treeFieldList: []
  }
  Object.entries(attributes).forEach(([key, val]) => {
    !curComponent.value[key] && (curComponent.value[key] = val)
  })
}

const handleCondition = item => {
  handleDialogClick()
  if (activeConditionForRename.id) return
  activeCondition.value = item.id
  curComponent.value = conditions.value.find(ele => ele.id === item.id)
  curComponent.value.dataset.fields = []
  nextTick(() => {
    defaultConfigurationRef.value.changeMultiple(curComponent.value.multiple)
  })
  if (curComponent.value.dataset.id) {
    listFieldsWithPermissions(curComponent.value.dataset.id).then(res => {
      curComponent.value.dataset.fields = res.data
    })
  }
  datasetFieldList.value.forEach(ele => {
    if (!curComponent.value.checkedFieldsMap[ele.id]) {
      curComponent.value.checkedFieldsMap[ele.id] = ''
    }
  })

  const idMap = datasetFieldList.value.map(ele => ele.id)
  curComponent.value.checkedFields = curComponent.value.checkedFields.filter(ele =>
    idMap.includes(ele)
  )
  if (!!fields.value?.length) {
    fields.value.forEach(ele => {
      const activeCom = ele.fields
      ele.activelist = setActiveSelectTab(
        [activeCom.dimensionList, activeCom.quotaList, activeCom.parameterList],
        curComponent.value.checkedFieldsMap[ele.componentId]
      )
    })
    handleCheckedFieldsChange(curComponent.value.checkedFields)
  }
  multipleChange(curComponent.value.multiple)
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
  }
  parameterCompletion()
  nextTick(() => {
    curComponent.value.showError = showError.value
    curComponent.value.auto && (document.querySelector('.chart-field').scrollTop = 0)
  })
}

const getOptions = (id, component) => {
  listFieldsWithPermissions(id).then(res => {
    component.dataset.fields = res.data
  })
}

const treeDialog = ref()
const startTreeDesign = () => {
  const [comId] = curComponent.value.checkedFields
  const componentObj = fields.value.find(ele => ele.componentId === comId)
  treeDialog.value.init(
    (curComponent.value.optionValueSource === 0
      ? componentObj?.fields?.dimensionList
      : curComponent.value.dataset?.fields
    ).filter(ele => ele.deType === +curComponent.value.field.deType),
    curComponent.value.treeFieldList
  )
}
const saveTree = arr => {
  curComponent.value.treeFieldList = arr
}
const showError = computed(() => {
  if (!curComponent.value) return false
  const { optionValueSource, checkedFieldsMap, checkedFields, field, valueSource, displayType } =
    curComponent.value
  const arr = checkedFields.filter(ele => !!checkedFieldsMap[ele])
  if (!checkedFields.length || !arr.length) {
    return true
  }
  if ([1, 7, 8].includes(+displayType)) {
    return false
  }

  if (displayType === '9') {
    let displayField = null
    return checkedFields.some(id => {
      const arr = (fields.value || []).find(itx => itx.componentId === id)
      const field = arr?.id
      if (!field) return false
      if (displayField === null) {
        displayField = field
        return false
      }
      return displayField !== field
    })
  }
  return (optionValueSource === 1 && !field.id) || (optionValueSource === 2 && !valueSource.length)
})
const handleDialogClick = () => {
  defaultConfigurationRef.value?.handleDialogClick()
}

const relativeToCurrentList = computed(() => {
  let list = []
  if (!curComponent.value) return list
  switch (curComponent.value.timeGranularity) {
    case 'year':
      list = [
        {
          label: '今年',
          value: 'thisYear'
        },
        {
          label: '去年',
          value: 'lastYear'
        }
      ]
      break
    case 'month':
      list = [
        {
          label: '本月',
          value: 'thisMonth'
        },
        {
          label: '上月',
          value: 'lastMonth'
        }
      ]
      break
    case 'date':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
          value: 'yearBeginning'
        }
      ]
      break
    case 'datetime':
      list = [
        {
          label: '今天',
          value: 'today'
        },
        {
          label: '昨天',
          value: 'yesterday'
        },
        {
          label: '月初',
          value: 'monthBeginning'
        },
        {
          label: '年初',
          value: 'yearBeginning'
        }
      ]
      break

    default:
      break
  }

  return [
    ...list,
    {
      label: '自定义',
      value: 'custom'
    }
  ]
})

const timeGranularityChange = (val: string) => {
  if (
    ['year', 'month', 'date', 'datetime'].indexOf(val) <
    ['year', 'month', 'date'].indexOf(curComponent.value.relativeToCurrentType)
  ) {
    curComponent.value.relativeToCurrentType = 'year'
  }
  if (curComponent.value.relativeToCurrent !== 'custom') {
    curComponent.value.relativeToCurrent = relativeToCurrentList.value[0]?.value
  }
}

const timeGranularityMultipleChange = (val: string) => {
  handleDialogClick()
  if (
    ['yearrange', 'monthrange', 'daterange', 'datetimerange'].indexOf(val) <
    ['year', 'month', 'date'].indexOf(curComponent.value.relativeToCurrentType)
  ) {
    curComponent.value.relativeToCurrentType = 'year'
  }
  if (
    ['yearrange', 'monthrange', 'daterange', 'datetimerange'].indexOf(val) <
    ['year', 'month', 'date'].indexOf(curComponent.value.relativeToCurrentTypeRange)
  ) {
    curComponent.value.relativeToCurrentTypeRange = 'year'
  }

  curComponent.value.timeRange = {
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
  }
}
watch(
  () => showError.value,
  val => {
    curComponent.value.showError = val
  }
)

const setRenameInput = val => {
  renameInput.value.push(val)
}

const addOperation = (cmd, condition, index) => {
  switch (cmd) {
    case 'del':
      renameInput.value = []
      conditions.value.splice(index, 1)
      curComponent.value = null
      break
    case 'rename':
      renameInput.value = []
      Object.assign(activeConditionForRename, condition)
      setTimeout(() => {
        nextTick(() => {
          renameInput.value[0].focus()
        })
      }, 400)
      break
    default:
      break
  }
}
const dsSelectProps = {
  label: 'name',
  children: 'children',
  value: 'id',
  isLeaf: node => !node.children?.length
}

const dfs = arr => {
  return arr.filter(ele => {
    if (!!ele.children?.length && !ele.leaf) {
      ele.children = dfs(ele.children)
      return !!ele.children?.length
    }
    return ele.leaf
  })
}

const renameInputBlur = () => {
  if (activeConditionForRename.name.trim() === '') {
    ElMessage.error('字段名称不能为空')
    renameInput.value[0]?.focus()
    return
  }
  conditions.value.some(ele => {
    if (activeConditionForRename.id === ele.id) {
      ele.name = activeConditionForRename.name
      return true
    }
    return false
  })
  activeConditionForRename.id = ''
}

const addQueryCriteria = () => {
  conditions.value.push(addQueryCriteriaConfig())
}

const addCriteriaConfig = () => {
  addQueryCriteria()
  return conditions.value[conditions.value.length - 1].id
}

defineExpose({
  setCondition,
  addCriteriaConfig,
  setConditionOut
})
</script>

<template>
  <el-dialog
    class="query-condition-configuration"
    v-model="dialogVisible"
    width="1200px"
    title="查询条件设置"
    @click.stop
    :before-close="handleBeforeClose"
    @mousedown.stop
    @mousedup.stop
  >
    <div class="container" @click="handleDialogClick">
      <div class="query-condition-list">
        <div class="title">
          查询条件
          <el-icon @click="addQueryCriteria">
            <Icon name="icon_add_outlined"></Icon>
          </el-icon>
        </div>
        <draggable tag="div" :list="conditions" handle=".handle">
          <template #item="{ element, index }">
            <div
              :key="element.id"
              @dblclick.stop="addOperation('rename', element, index)"
              @click.stop="handleCondition(element)"
              class="list-item_primary"
              :class="element.id === activeCondition && 'active'"
            >
              <el-icon class="handle">
                <Icon name="icon_drag_outlined"></Icon>
              </el-icon>
              <div class="label flex-align-center icon" :title="element.name">
                <el-icon
                  v-if="!element.auto && element.showError"
                  style="font-size: 16px; color: #f54a45"
                >
                  <icon name="icon_warning_filled"></icon>
                </el-icon>
                {{ element.name }}
              </div>
              <div class="condition-icon flex-align-center">
                <handle-more
                  @handle-command="cmd => addOperation(cmd, element, index)"
                  :menu-list="typeList"
                  icon-name="more_v"
                  placement="bottom-end"
                ></handle-more>
                <el-icon
                  class="hover-icon"
                  @click.stop="element.visible = !element.visible"
                  v-if="element.visible"
                >
                  <Icon name="icon_visible_outlined"></Icon>
                </el-icon>
                <el-icon class="hover-icon" @click.stop="element.visible = !element.visible" v-else>
                  <Icon name="de_pwd_invisible"></Icon>
                </el-icon>
              </div>
              <div @click.stop v-if="activeConditionForRename.id === element.id" class="rename">
                <el-input
                  @blur="renameInputBlur"
                  :ref="setRenameInput"
                  v-model="activeConditionForRename.name"
                ></el-input>
              </div>
            </div>
          </template>
        </draggable>
      </div>
      <div v-if="!!curComponent" class="chart-field" :class="curComponent.auto && 'hidden'">
        <div class="mask" v-if="curComponent.auto"></div>
        <div class="title flex-align-center">
          选择关联图表及字段
          <el-radio-group class="ml-4 larger-radio" v-model="curComponent.auto">
            <el-radio :disabled="!curComponent.auto" :label="true">
              <div class="flex-align-center">
                自动
                <el-tooltip effect="dark" placement="top">
                  <template #content>
                    <div>
                      注意:自动模式支持同数据集自动关联字段，可切换到
                      <br />
                      自定义模式。切换到自定义模式后无法再切换为自动！
                    </div>
                  </template>
                  <el-icon style="margin-left: 4px; color: #646a73">
                    <icon name="icon_info_outlined"></icon>
                  </el-icon>
                </el-tooltip>
              </div>
            </el-radio>
            <el-radio :label="false">{{ t('commons.custom') }}</el-radio>
          </el-radio-group>
        </div>
        <div class="select-all">
          <el-checkbox
            v-model="checkAll"
            :indeterminate="isIndeterminate"
            @change="handleCheckAllChange"
            >{{ t('dataset.check_all') }}</el-checkbox
          >
        </div>
        <div class="field-list">
          <el-checkbox-group
            v-model="curComponent.checkedFields"
            @change="handleCheckedFieldsChangeTree"
          >
            <div v-for="field in fields" :key="field.componentId" class="list-item">
              <el-checkbox :label="field.componentId"
                ><el-icon class="component-type">
                  <Icon :name="canvasViewInfo[field.componentId].type"></Icon> </el-icon
                ><span
                  :title="canvasViewInfo[field.componentId].title"
                  class="checkbox-name ellipsis"
                  >{{ canvasViewInfo[field.componentId].title }}</span
                ></el-checkbox
              >
              <span :title="field.name" class="dataset ellipsis">{{ field.name }}</span>
              <el-select
                @change="setParameters"
                @focus="handleDialogClick"
                style="margin-left: 12px"
                popper-class="field-select--dqp"
                v-if="curComponent.checkedFields.includes(field.componentId)"
                v-model="curComponent.checkedFieldsMap[field.componentId]"
                clearable
              >
                <template v-if="curComponent.checkedFieldsMap[field.componentId]" #prefix>
                  <el-icon>
                    <Icon
                      :name="`field_${
                        fieldType[
                          getDetype(
                            curComponent.checkedFieldsMap[field.componentId],
                            Object.values(field.fields)
                          )
                        ]
                      }`"
                      :className="`field-icon-${
                        fieldType[
                          getDetype(
                            curComponent.checkedFieldsMap[field.componentId],
                            Object.values(field.fields)
                          )
                        ]
                      }`"
                    ></Icon>
                  </el-icon>
                </template>
                <template #header>
                  <el-tabs stretch class="params-select--header" v-model="field.activelist">
                    <el-tab-pane label="维度" name="dimensionList"></el-tab-pane>
                    <el-tab-pane
                      :disabled="curComponent.displayType === '9'"
                      label="指标"
                      name="quotaList"
                    ></el-tab-pane>
                    <el-tab-pane
                      v-if="field.hasParameter"
                      label="参数"
                      :disabled="curComponent.displayType === '9'"
                      name="parameterList"
                    ></el-tab-pane>
                  </el-tabs>
                </template>
                <el-option
                  v-for="ele in field.fields[field.activelist]"
                  :key="ele.id"
                  :label="ele.name || ele.variableName"
                  :value="ele.id"
                  :disabled="
                    ele.desensitized || (curComponent.displayType === '9' && ele.deType === 1)
                  "
                >
                  <div
                    class="flex-align-center icon"
                    :title="ele.desensitized ? '脱敏字段，不能被设置为查询条件' : ''"
                  >
                    <el-icon>
                      <Icon
                        :name="`field_${fieldType[ele.deType]}`"
                        :className="`field-icon-${fieldType[ele.deType]}`"
                      ></Icon>
                    </el-icon>
                    <span :title="ele.name || ele.variableName" class="ellipsis">
                      {{ ele.name || ele.variableName }}
                    </span>
                  </div>
                </el-option>
              </el-select>
              <span style="width: 172px; margin-left: 12px" v-else></span>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <div v-if="!!curComponent" class="condition-configuration">
        <div class="mask condition" v-if="curComponent.auto"></div>
        <div class="title flex-align-center">
          查询条件配置
          <el-checkbox
            :disabled="curComponent.auto"
            v-model="curComponent.required"
            label="必填项"
          />
        </div>
        <div
          v-show="showConfiguration && !showTypeError && !showDatasetError"
          class="configuration-list"
        >
          <div class="list-item">
            <div class="label">展示类型</div>
            <div class="value">
              <el-select
                @focus="handleDialogClick"
                @change="setTypeChange"
                v-model="curComponent.displayType"
              >
                <el-option
                  :disabled="!['0', '8', '9'].includes(curComponent.displayType)"
                  label="文本下拉"
                  value="0"
                />
                <el-option
                  :disabled="!['0', '8', '9'].includes(curComponent.displayType)"
                  label="文本搜索"
                  value="8"
                />
                <el-option
                  :disabled="
                    !['0', '8', '9'].includes(curComponent.displayType) ||
                    !!curComponent.parameters.length
                  "
                  label="下拉树"
                  value="9"
                />
                <el-option
                  v-if="curComponent.displayType === '2'"
                  :disabled="curComponent.displayType !== '2'"
                  label="数字下拉"
                  value="2"
                />
                <el-option
                  v-else
                  :disabled="curComponent.displayType !== '5'"
                  label="数字下拉"
                  value="5"
                />
                <el-option
                  :disabled="!['1', '7'].includes(curComponent.displayType)"
                  label="时间"
                  value="1"
                />
                <el-option
                  :disabled="!['1', '7'].includes(curComponent.displayType) || isTimeParameter"
                  label="时间范围"
                  value="7"
                />
              </el-select>
            </div>
          </div>
          <div class="list-item" v-if="curComponent.displayType === '9'">
            <div class="label">选项值数量</div>
            <div class="value">
              <el-radio-group class="larger-radio" v-model="curComponent.resultMode">
                <el-radio :label="0">默认</el-radio>
                <el-radio :label="1">全部</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="list-item" v-if="curComponent.displayType === '9'">
            <div class="label" style="width: 135px; height: 26px; line-height: 26px">
              下拉树结构设计
              <el-button
                v-if="curComponent.treeFieldList && !!curComponent.treeFieldList.length"
                text
                @click="startTreeDesign"
              >
                <template #icon>
                  <icon name="icon_edit_outlined"></icon>
                </template>
              </el-button>
            </div>
            <div class="search-tree">
              <template v-if="curComponent.treeFieldList && !!curComponent.treeFieldList.length">
                <div
                  v-for="(ele, index) in curComponent.treeFieldList"
                  :key="ele.id"
                  class="tree-field"
                >
                  <span class="level-index">层级{{ indexCascade[index + 1] }}</span>
                  <span class="field-type"
                    ><el-icon>
                      <Icon
                        :name="`field_${fieldType[ele.deType]}`"
                        :className="`field-icon-${fieldType[ele.deType]}`"
                      ></Icon> </el-icon
                  ></span>
                  <span class="field-tree_name">{{ ele.name }}</span>
                </div>
              </template>
              <el-button @click="startTreeDesign" v-else text>
                <template #icon>
                  <Icon name="icon_add_outlined"></Icon>
                </template>
                点击进行树结构设计
              </el-button>
            </div>
            <TreeFieldDialog ref="treeDialog" @save-tree="saveTree"></TreeFieldDialog>
          </div>
          <div class="list-item" v-if="['1', '7'].includes(curComponent.displayType)">
            <div class="label">时间粒度</div>
            <div class="value">
              <template v-if="curComponent.displayType === '7' && !isTimeParameter">
                <el-select
                  @change="timeGranularityMultipleChange"
                  placeholder="请选择时间粒度"
                  @focus="handleDialogClick"
                  v-model="curComponent.timeGranularityMultiple"
                >
                  <el-option label="年" value="yearrange" />
                  <el-option label="年月" value="monthrange" />
                  <el-option label="年月日" value="daterange" />
                  <el-option label="年月日时分秒" value="datetimerange" />
                </el-select>
              </template>
              <template v-else>
                <el-select
                  @change="timeGranularityChange"
                  placeholder="请选择时间粒度"
                  v-model="curComponent.timeGranularity"
                >
                  <el-option
                    v-for="ele in timeParameterList"
                    :key="ele.value"
                    :label="ele.label"
                    :value="ele.value"
                  />
                </el-select>
              </template>
            </div>
          </div>
          <div
            class="list-item top-item"
            v-if="!['1', '7', '8', '9'].includes(curComponent.displayType)"
          >
            <div class="label">选项值来源</div>
            <div class="value">
              <div class="value">
                <el-radio-group
                  class="larger-radio"
                  @change="handleValueSourceChange"
                  v-model="curComponent.optionValueSource"
                >
                  <el-radio :disabled="!!curComponent.parameters.length" :label="0">{{
                    t('chart.margin_model_auto')
                  }}</el-radio>
                  <el-radio :label="1">{{ t('chart.select_dataset') }}</el-radio>
                  <el-radio :label="2">手动输入</el-radio>
                </el-radio-group>
              </div>
              <template v-if="curComponent.optionValueSource === 1">
                <div class="value">
                  <el-tree-select
                    v-model="curComponent.dataset.id"
                    :data="computedTree"
                    placeholder="请选择数据集"
                    @change="handleDatasetChange"
                    @current-change="handleCurrentChange"
                    :props="dsSelectProps"
                    placement="bottom"
                    :render-after-expand="false"
                    filterable
                    popper-class="dataset-tree"
                  >
                    <template #default="{ node, data }">
                      <div class="content">
                        <el-icon size="18px" v-if="!data.leaf">
                          <Icon name="dv-folder"></Icon>
                        </el-icon>
                        <el-icon size="18px" v-if="data.leaf">
                          <Icon name="icon_dataset"></Icon>
                        </el-icon>
                        <span class="label ellipsis" style="margin-left: 8px" :title="node.label">{{
                          node.label
                        }}</span>
                      </div>
                    </template>
                  </el-tree-select>
                </div>
                <div class="value">
                  <span class="label">查询字段</span>
                  <el-select
                    @change="handleFieldChange"
                    placeholder="查询字段"
                    class="search-field"
                    v-model="curComponent.field.id"
                  >
                    <template v-if="curComponent.field.id" #prefix>
                      <el-icon>
                        <Icon
                          :name="`field_${
                            fieldType[getDetype(curComponent.field.id, curComponent.dataset.fields)]
                          }`"
                          :className="`field-icon-${
                            fieldType[getDetype(curComponent.field.id, curComponent.dataset.fields)]
                          }`"
                        ></Icon>
                      </el-icon>
                    </template>
                    <el-option
                      v-for="ele in curComponent.dataset.fields.filter(
                        ele =>
                          ele.deType === +curComponent.displayType ||
                          ([3, 4].includes(ele.deType) && +curComponent.displayType === 2)
                      )"
                      :key="ele.id"
                      :label="ele.name"
                      :value="ele.id"
                      :disabled="ele.desensitized"
                    >
                      <div
                        class="flex-align-center icon"
                        :title="ele.desensitized ? '脱敏字段，不能被设置为查询条件' : ''"
                      >
                        <el-icon>
                          <Icon
                            :name="`field_${fieldType[ele.deType]}`"
                            :className="`field-icon-${fieldType[ele.deType]}`"
                          ></Icon>
                        </el-icon>
                        <span>
                          {{ ele.name }}
                        </span>
                      </div>
                    </el-option>
                  </el-select>
                </div>
                <div class="value">
                  <span class="label">显示字段</span>
                  <el-select
                    placeholder="显示字段"
                    class="search-field"
                    v-model="curComponent.displayId"
                  >
                    <template v-if="curComponent.displayId" #prefix>
                      <el-icon>
                        <Icon
                          :name="`field_${
                            fieldType[
                              getDetype(curComponent.displayId, curComponent.dataset.fields)
                            ]
                          }`"
                          :className="`field-icon-${
                            fieldType[
                              getDetype(curComponent.displayId, curComponent.dataset.fields)
                            ]
                          }`"
                        ></Icon>
                      </el-icon>
                    </template>
                    <el-option
                      v-for="ele in curComponent.dataset.fields.filter(
                        ele =>
                          ele.deType === +curComponent.displayType ||
                          ([3, 4].includes(ele.deType) && +curComponent.displayType === 2)
                      )"
                      :key="ele.id"
                      :label="ele.name"
                      :value="ele.id"
                      :disabled="ele.desensitized"
                    >
                      <div
                        class="flex-align-center icon"
                        :title="ele.desensitized ? '脱敏字段，不能被设置为查询条件' : ''"
                      >
                        <el-icon>
                          <Icon
                            :name="`field_${fieldType[ele.deType]}`"
                            :className="`field-icon-${fieldType[ele.deType]}`"
                          ></Icon>
                        </el-icon>
                        <span>
                          {{ ele.name }}
                        </span>
                      </div>
                    </el-option>
                  </el-select>
                </div>
                <div class="value">
                  <span class="label">排序字段</span>
                  <el-select
                    clearable
                    placeholder="请选择排序字段"
                    v-model="curComponent.sortId"
                    class="sort-field"
                    @change="handleFieldChange"
                  >
                    <template v-if="curComponent.sortId" #prefix>
                      <el-icon>
                        <Icon
                          :name="`field_${
                            fieldType[getDetype(curComponent.sortId, curComponent.dataset.fields)]
                          }`"
                          :className="`field-icon-${
                            fieldType[getDetype(curComponent.sortId, curComponent.dataset.fields)]
                          }`"
                        ></Icon>
                      </el-icon>
                    </template>
                    <el-option
                      v-for="ele in curComponent.dataset.fields"
                      :key="ele.id"
                      :label="ele.name"
                      :value="ele.id"
                      :disabled="ele.desensitized"
                    >
                      <div
                        class="flex-align-center icon"
                        :title="ele.desensitized ? '脱敏字段，不能被设置为查询条件' : ''"
                      >
                        <el-icon>
                          <Icon
                            :name="`field_${fieldType[ele.deType]}`"
                            :className="`field-icon-${fieldType[ele.deType]}`"
                          ></Icon>
                        </el-icon>
                        <span>
                          {{ ele.name }}
                        </span>
                      </div>
                    </el-option>
                  </el-select>
                  <el-select
                    class="sort-type"
                    v-model="curComponent.sort"
                    @change="handleFieldChange"
                  >
                    <el-option label="升序" value="asc" />
                    <el-option label="降序" value="desc" />
                  </el-select>
                </div>
              </template>
              <div v-if="curComponent.optionValueSource === 2" class="value flex-align-center">
                <el-popover
                  placement="bottom-start"
                  popper-class="manual-input"
                  ref="manual"
                  :width="358"
                  trigger="click"
                >
                  <template #reference>
                    <el-button text>
                      <template #icon>
                        <Icon name="icon_edit_outlined"></Icon>
                      </template>
                      {{ t('common.edit') }}
                    </el-button>
                  </template>
                  <div class="manual-input-container">
                    <div class="title">{{ t('auth.manual_input') }}</div>
                    <div class="select-value">
                      <span> 选项值 </span>
                      <div :key="index" v-for="(_, index) in valueSource" class="select-item">
                        <el-input
                          maxlength="20"
                          v-if="curComponent.displayType === '2'"
                          @blur="weightlessness"
                          v-model.number="valueSource[index]"
                        ></el-input>
                        <el-input
                          maxlength="20"
                          v-else
                          @blur="weightlessness"
                          v-model="valueSource[index]"
                        ></el-input>
                        <el-button
                          v-if="valueSource.length !== 1"
                          @click="valueSource.splice(index, 1)"
                          class="value"
                          text
                        >
                          <template #icon>
                            <Icon name="icon_delete-trash_outlined"></Icon>
                          </template>
                        </el-button>
                      </div>
                    </div>
                    <div class="add-btn">
                      <el-button @click="valueSource.push('')" text>
                        <template #icon>
                          <Icon name="icon_add_outlined"></Icon>
                        </template>
                        添加选项值
                      </el-button>
                    </div>
                    <div class="manual-footer flex-align-center">
                      <el-button @click="cancelValueSource">{{ t('chart.cancel') }} </el-button>
                      <el-button @click="confirmValueSource" type="primary"
                        >{{ t('chart.confirm') }}
                      </el-button>
                    </div>
                  </div>
                </el-popover>
                <div v-if="!!curComponent.valueSource.length" class="config-flag flex-align-center">
                  已配置
                </div>
              </div>
            </div>
            <div class="label" style="margin-top: 10.5px">选项值数量</div>
            <div class="value" style="margin-top: 10.5px">
              <el-radio-group class="larger-radio" v-model="curComponent.resultMode">
                <el-radio :label="0">默认</el-radio>
                <el-radio :label="1">全部</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="list-item top-item" v-if="curComponent.displayType === '8'">
            <div class="label">条件类型</div>
            <div class="value">
              <div class="value">
                <el-radio-group class="larger-radio" v-model="curComponent.conditionType">
                  <el-radio :label="0">单条件</el-radio>
                  <el-radio :label="1" :disabled="!!curComponent.parameters.length"
                    >与条件</el-radio
                  >
                  <el-radio :label="2" :disabled="!!curComponent.parameters.length"
                    >或条件</el-radio
                  >
                </el-radio-group>
              </div>
            </div>
          </div>
          <condition-default-configuration
            ref="defaultConfigurationRef"
            :cur-component="curComponent"
          ></condition-default-configuration>
        </div>
        <div v-if="showTypeError && showConfiguration" class="empty">
          <empty-background description="所选字段类型不一致，无法进行查询配置" img-type="error" />
        </div>
        <div v-else-if="showDatasetError && showConfiguration" class="empty">
          <empty-background description="图表所使用的数据集不同, 无法展示配置项" img-type="error" />
        </div>
        <div v-else-if="!showConfiguration" class="empty">
          <empty-background description="请先勾选需要联动的图表及字段" img-type="noneWhite" />
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button class="query-cascade" @click="openCascadeDialog">查询组件级联配置</el-button>
        <el-button @click="cancelClick">{{ t('chart.cancel') }} </el-button>
        <el-button @click="confirmClick" type="primary">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
  <CascadeDialog @saveCascade="saveCascade" ref="cascadeDialog"></CascadeDialog>
</template>

<style lang="less">
.field-select--dqp {
  min-width: 210px !important;
}
.ed-select-dropdown__header {
  padding: 0 8px;
  .params-select--header {
    --ed-tabs-header-height: 32px;
    .ed-tabs__item {
      font-weight: 400;
      font-size: 15px;
    }
  }
}
.condition-value-select-popper {
  .ed-select-dropdown__item.selected::after {
    display: none;
  }
}
.dataset-parameters {
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  font-style: normal;
  font-weight: 400;
  .ed-select-dropdown__item {
    height: 50px;
    line-height: 50px;
    padding-top: 4px;
    &.selected::after {
      top: 30% !important;
    }
  }
  .variable-name {
    font-size: 14px;
    line-height: 22px;
  }
  .dataset-full-name {
    color: #8d9199;
    font-size: 12px;
    line-height: 20px;
  }
}
.query-condition-configuration {
  --ed-font-weight-primary: 400;

  .query-cascade {
    position: absolute;
    left: 24px;
    bottom: 24px;
  }

  .ed-dialog__headerbtn {
    top: 21px;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .ed-input .ed-select__prefix--light {
    border-right: none;
    padding: 0;
    font-size: 16px;
    margin-right: 4px;
  }
  .container {
    font-size: 14px;
    font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
    width: 1152px;
    height: 454px;
    border-radius: 4px;
    border: 1px solid #dee0e3;
    display: flex;
    .ed-checkbox:not(.is-disabled) {
      .ed-checkbox__label:hover {
        color: #1f2329;
      }
    }
    .query-condition-list {
      height: 100%;
      background: #f5f6f7;
      border-right: 1px solid #dee0e3;
      width: 208px;
      overflow-y: auto;

      .title {
        padding: 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;

        .ed-icon {
          cursor: pointer;
          font-size: 16px;
          color: var(--ed-color-primary);
        }
      }
      .list-item_primary {
        border-radius: 0;
        position: relative;
        .label {
          width: 75%;
        }

        .rename {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          background: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
          padding: 4px 10px;
          z-index: 5;
        }
      }
    }

    .mask {
      position: absolute;
      top: 30px;
      left: 0;
      width: 100%;
      z-index: 5;
      background: rgba(255, 255, 255, 0.6);
      height: calc(100% - 30px);

      &.condition {
        height: calc(100% - 45px);
        top: 45px;
      }
    }

    .chart-field {
      height: calc(100% - 16px);
      padding: 0 16px 16px 16px;
      width: 474px;
      position: relative;
      overflow-y: auto;
      margin-top: 16px;

      .flex-align-center {
        position: sticky;
        top: 0;
        justify-content: space-between;
        background: #fff;
        z-index: 5;
        .ed-radio {
          height: 20px;
        }
      }

      .title {
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        margin-bottom: 8px;
      }

      .select-all {
        height: 40px;
      }

      .field-list {
        .component-type {
          margin-right: 4px;
          font-size: 20px;
          color: var(--ed-color-primary);
        }
        .list-item {
          height: 32px;
          display: flex;
          align-items: center;
          margin-bottom: 8px;

          .ed-checkbox__label {
            display: inline-flex;
            align-items: center;
            .checkbox-name {
              width: 110px;
            }
          }

          .dataset {
            color: #646a73;
            font-size: 14px;
            height: 22px;
            width: 90px;
            line-height: 22px;
            margin-left: 8px;
          }

          .ed-select {
            width: 172px;
          }
        }
      }
    }
    .hidden {
      overflow-y: hidden;
    }

    .condition-configuration {
      padding: 16px;
      border-left: 1px solid #dee0e3;
      width: 467px;
      position: relative;
      overflow-y: auto;
      .mask {
        left: -1px;
        width: calc(100% + 2px);
      }

      .config-flag {
        color: #646a73;
        height: 16px;
        padding: 0px 4px;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 10px;
        font-style: normal;
        font-weight: 500;
        line-height: 13px;
        border-radius: 2px;
        background: rgba(31, 35, 41, 0.1);
        margin-left: 8px;

        &.range-filter-time-flag {
          display: inline-block;
          padding: 1px 4px;
          line-height: 14px;
          margin-left: 4px;
        }
      }

      .flex-align-center {
        position: sticky;
        top: 0;
        justify-content: space-between;
        background: #fff;
        z-index: 5;
        .ed-checkbox {
          height: 20px;
        }
      }
      .title {
        margin-bottom: 16px;
        font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
        font-size: 14px;
        font-style: normal;
        font-weight: 500;
        line-height: 22px;
        position: relative;

        &.flex-align-center::after {
          content: '';
          position: absolute;
          width: 100%;
          height: 16px;
          background: #fff;
          top: -16px;
          left: 0;
        }
      }

      .configuration-list {
        .list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10.5px;
          flex-wrap: wrap;
          .search-tree {
            width: 100%;
            height: 200px;
            margin-top: 8px;
            position: relative;
            padding: 16px;
            box-shadow: 0px 0px 12px rgba(0, 0, 0, 0.12);

            .ed-button {
              position: absolute;
              left: 50%;
              top: 50%;
              transform: translate(-50%, -50%);
            }

            .tree-field {
              display: flex;
              align-items: center;
              margin-bottom: 16px;
              .level-index {
                margin-right: 40px;
              }

              .field-type {
                display: flex;
                align-items: center;
                justify-content: center;
                font-size: 16px;
              }

              .field-tree_name {
                margin-left: 8px;
              }
            }
          }

          .setting-content {
            width: 100%;
            padding-left: 24px;
          }

          &.top-item {
            .label {
              margin-bottom: auto;
              padding-top: 5.5px;
            }
          }
          .label {
            width: 100px;
            color: #1f2329;
          }

          .value {
            .ed-select {
              width: 321px;
            }
            width: 321px;
            .value {
              margin-top: 8px;
              &:first-child {
                margin-top: -0.5px;
              }
              .search-field {
                width: 257px;
              }

              .sort-field {
                width: 176px;
              }

              .label {
                line-height: 32px;
                font-size: 14px;
                margin-right: 8px;
              }
            }
          }

          .value {
            width: 321px;
            .condition-type {
              margin-top: 3px !important;
              display: flex;
              position: relative;
              .ed-input__wrapper {
                border: none;
                border-radius: 0;
                box-shadow: none;
                height: 26px;
                font-family: '阿里巴巴普惠体 3.0 55 Regular L3', Hiragino Sans GB, Microsoft YaHei,
                  sans-serif;
                word-wrap: break-word;
                text-align: left;
                color: rgba(0, 0, 0, 0.65);
                list-style: none;
                user-select: none;
                cursor: pointer;
                line-height: 26px;
                box-sizing: border-box;
                max-width: 100%;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
                opacity: 1;
              }

              .ed-select .ed-input.is-focus .ed-input__wrapper,
              .ed-select:hover:not(.ed-select--disabled) .ed-input__wrapper,
              .ed-select .ed-input__wrapper.is-focus {
                box-shadow: none !important;
              }

              .ed-select {
                width: 120px;
                .ed-input__wrapper {
                  padding: 0;
                }
              }

              .condition-type-tip {
                font-size: 12px;
                color: #646a73;
                line-height: 26px;
                margin-right: 8px;
              }

              .bottom-line {
                box-sizing: border-box;
                height: 1px;
                background-color: #000;
                opacity: 0.3;
                position: absolute;
                right: 5px;
                bottom: 3px;
                width: 220px;
                z-index: 10;

                &.next-line {
                  width: 206px;
                }
              }
              &:first-child {
                margin-top: -0.5px;
              }
            }
          }
          .value {
            .sort-field {
              width: 240px;
            }
            .sort-type {
              width: 73px;
              margin-left: 8px;
            }
          }
          .parameters {
            margin-left: auto;
            margin-top: 8px;

            .w100 {
              width: 100%;
            }
            .ed-select,
            .ed-date-editor,
            .ed-date-editor--datetime .ed-input__wrapper,
            .ed-select-v2 {
              width: 415px;
            }

            .ed-date-editor {
              .ed-input__wrapper {
                width: 100%;
              }
            }
          }
          .parameters-range {
            width: 100%;
            padding-left: 24px;
            display: flex;
            flex-wrap: wrap;

            .range-title,
            .params-start,
            .params-end {
              width: 50%;
            }

            .params-start,
            .params-end {
              margin-top: 8px;
              .ed-select {
                width: 100%;
              }
            }

            .params-end {
              padding-left: 4px;
            }

            .params-start {
              padding-right: 4px;
            }
          }

          .setting {
            &.setting {
              margin-top: 8px;
            }
            &.parameters {
              width: 100%;
              padding-left: 24px;
              .ed-date-editor {
                width: 325px !important;
              }
            }
            margin-left: auto;
            display: flex;
            justify-content: space-between;
            align-items: center;
            .setting-label {
              width: 80px;
              margin-right: 8px;
            }

            .setting-value {
              margin: 8px 0;
              &.select {
                margin-top: 0;
                .ed-select {
                  width: 325px;
                }
              }
            }

            .setting-input {
              display: flex;
              padding-left: 86px;
              justify-content: flex-end;
              align-items: center;
              &.range {
                padding-left: 0px;
              }
              & > div + div {
                margin-left: 8px;
              }

              &.with-date {
                .ed-input-number {
                  width: 71px;
                }
                .ed-select {
                  width: 62px;
                }

                .ed-date-editor.ed-input {
                  width: 106px;
                }
              }
            }

            &.is-year-month-range {
              .setting-input {
                &.with-date {
                  .ed-input-number,
                  .ed-select {
                    width: 103px;
                  }
                }
                .ed-date-editor.ed-input {
                  display: none;
                }
              }
            }
          }
        }
      }
    }
  }
}
.manual-input {
  height: 405px;
  padding: 0 !important;

  .manual-input-container {
    .title {
      padding: 16px;
    }

    .add-btn {
      padding: 8px 16px;
    }
    .select-value {
      padding-left: 16px;
      max-height: 246px;
      overflow-y: auto;
      .value {
        color: #646a73;
        margin-left: 6px;
        font-size: 20px;
      }

      .select-item {
        margin: 8px 0;
        &:last-child {
          margin-bottom: 0;
        }
        .ed-input {
          width: 298px;
        }
      }
    }
    .manual-footer {
      position: absolute;
      bottom: 0;
      padding: 16px;
      height: 63px;
      width: 100%;
      border-top: 1px solid rgba(31, 35, 41, 0.15);
      justify-content: flex-end;
    }
  }
}
.dataset-tree {
  .content {
    display: flex;
    align-items: center;
    .label {
      margin-left: 5px;
      width: calc(100% - 45px);
    }
  }
  max-width: 321px;
  .ed-select-dropdown__item.selected {
    font-weight: 400;
  }
}
.larger-radio {
  .ed-radio__inner {
    width: 16px;
    height: 16px;
  }
}
</style>
