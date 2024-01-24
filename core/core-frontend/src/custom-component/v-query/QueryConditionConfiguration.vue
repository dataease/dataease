<script lang="ts" setup>
import { ref, reactive, nextTick, computed, shallowRef, toRefs, watch } from 'vue'
import { storeToRefs } from 'pinia'
import { addQueryCriteriaConfig } from './options'
import { getCustomTime } from './time-format'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { snapshotStoreWithOut } from '@/store/modules/data-visualization/snapshot'
import { useI18n } from '@/hooks/web/useI18n'
import { fieldType } from '@/utils/attr'
import { ElMessage } from 'element-plus-secondary'
import type { DatasetDetail } from '@/api/dataset'
import { getDsDetailsWithPerm, getSqlParams, listFieldsWithPermissions } from '@/api/dataset'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { cloneDeep } from 'lodash-es'
import Select from './Select.vue'
import Time from './Time.vue'
import DynamicTime from './DynamicTime.vue'
import DynamicTimeRange from './DynamicTimeRange.vue'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import draggable from 'vuedraggable'

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasViewInfo } = storeToRefs(dvMainStore)

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
const multiple = ref(false)
const activeConditionForRename = reactive({
  id: '',
  name: '',
  visible: false
})
const datasetMap = {}
const snapshotStore = snapshotStoreWithOut()

const dfsComponentData = () => {
  const isMain = componentData.value.some(ele => ele.id === queryElement.value.id)
  let arr = componentData.value.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
  let tabArr = []
  componentData.value.forEach(ele => {
    if (ele.innerType === 'DeTabs') {
      ele.propValue.forEach(itx => {
        if (itx.componentData.some(item => item.id === queryElement.value.id) && !isMain) {
          tabArr = itx.componentData.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
        } else {
          arr = [
            ...arr,
            ...itx.componentData.filter(com => !['VQuery', 'DeTabs'].includes(com.innerType))
          ]
        }
      })
    }
  })
  return isMain ? arr : tabArr
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

const curComponent = ref()
const manual = ref()
const activeCondition = ref('')
const isIndeterminate = ref(false)
const datasetTree = shallowRef([])
const fields = ref<DatasetDetail[]>()
const parameters = ref([])
const parametersFilter = computed(() => {
  return parameters.value.filter(ele => {
    if (curComponent.value.displayType === '2') {
      return [2, 3].includes(ele.deType)
    }

    if (curComponent.value.displayType === '7') {
      return [1, 7].includes(ele.deType)
    }
    return ele.deType === +curComponent.value.displayType
  })
})
const { queryElement } = toRefs(props)

const getDetype = (id, arr) => {
  return arr.find(ele => ele.id === id)?.deType
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
  let displayTypeField = null
  return curComponent.value.checkedFields.some(id => {
    const arr = fields.value.find(ele => ele.componentId === id)
    const checkId = curComponent.value.checkedFieldsMap?.[id]
    const field = (arr?.list || []).find(ele => checkId === ele.id)
    if (!field) return false
    if (displayTypeField === null) {
      displayTypeField = field?.deType
      return false
    }
    return displayTypeField !== field?.deType
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
const handleCheckedFieldsChange = (value: string[]) => {
  const checkedCount = value.length
  checkAll.value = checkedCount === fields.value.length
  isIndeterminate.value = checkedCount > 0 && checkedCount < fields.value.length
  setType()
}

const inputCom = ref()

const setType = () => {
  if (curComponent.value.checkedFields?.length) {
    const [id] = curComponent.value.checkedFields
    const arr = fields.value.find(ele => ele.componentId === id)
    const checkId = curComponent.value.checkedFieldsMap?.[id]
    const field = (arr?.list || []).find(ele => checkId === ele.id)

    if (field?.deType !== undefined) {
      let displayType = curComponent.value.displayType
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
  nextTick(() => {
    curComponent.value.field.id = ''
    inputCom.value?.displayTypeChange?.()
    if (
      +curComponent.value.displayType === 7 &&
      ['yearrange', 'monthrange', 'datetimerange'].includes(curComponent.value.timeGranularity)
    ) {
      curComponent.value.timeGranularityMultiple = curComponent.value.timeGranularity
    }
  })
}

const cancelClick = () => {
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

const handleDatasetChange = () => {
  curComponent.value.field.id = ''
  getOptions(curComponent.value.dataset.id, curComponent.value)
}

const handleFieldChange = () => {
  if (!curComponent.value.defaultValueCheck) return
  curComponent.value.defaultValue = curComponent.value.multiple ? [] : undefined
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
  if (curComponent.value.field.deType === 1) {
    curComponent.value.multiple = val
    return
  }
  curComponent.value.multiple = val
}

const validate = () => {
  return conditions.value.some(ele => {
    if (ele.auto) return false
    if (!ele.checkedFields?.length || ele.checkedFields.some(itx => !ele.checkedFieldsMap[itx])) {
      ElMessage.error('请先勾选需要联动的图表及字段')
      return true
    }

    if (ele.required) {
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

    if (+ele.displayType === 7) {
      if (!ele.defaultValueCheck) {
        return false
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
        arbitraryTimeRange
      } = ele

      const startTime = getCustomTime(
        timeNum,
        relativeToCurrentType,
        timeGranularity,
        around,
        arbitraryTime,
        timeGranularityMultiple,
        'start-config'
      )
      const endTime = getCustomTime(
        timeNumRange,
        relativeToCurrentTypeRange,
        timeGranularity,
        aroundRange,
        arbitraryTimeRange,
        timeGranularityMultiple,
        'end-config'
      )
      if (+startTime > +endTime) {
        ElMessage.error('结束时间必须大于开始时间!')
        return true
      }
      return false
    }

    if ([1].includes(+ele.displayType)) {
      return false
    }

    if (ele.optionValueSource === 2 && !ele.valueSource?.length) {
      ElMessage.error('手工输入-选项值不能为空')
      return true
    }

    if (ele.optionValueSource === 1 && !ele.field.id) {
      ElMessage.error('请选择数据集的选项值字段')
      return true
    }

    let displayTypeField = null
    if (
      ele.checkedFields.some(id => {
        const arr = fields.value.find(itx => itx.componentId === id)
        const checkId = ele.checkedFieldsMap?.[id]
        const field = (arr?.list || []).find(itx => checkId === itx.id)
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
  inputCom.value?.mult?.handleClickOutside?.()
  dialogVisible.value = false
}

const confirmClick = () => {
  if (validate()) return
  inputCom.value?.mult?.handleClickOutside?.()
  dialogVisible.value = false
  conditions.value.forEach(ele => {
    curComponent.value = ele
    multipleChange(
      ['1', '7'].includes(curComponent.value.displayType)
        ? curComponent.value.displayType === '7'
        : curComponent.value.multiple
    )
  })
  queryElement.value.propValue = cloneDeep(conditions.value)
  snapshotStore.recordSnapshotCache()
}

const cancelValueSource = () => {
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
    valueSource.value.push('')
  }
  manual.value.hide()
}

const confirmValueSource = () => {
  if (valueSource.value.some(ele => !ele.trim())) {
    ElMessage.error('手工输入-选项值不能为空')
    return
  }
  curComponent.value.valueSource = cloneDeep(valueSource.value.filter(ele => ele.trim()))
  handleValueSourceChange()
  cancelValueSource()
}

const filterTypeCom = computed(() => {
  const { displayType, timeType = 'fixed' } = curComponent.value
  return ['1', '7'].includes(displayType)
    ? timeType === 'dynamic'
      ? displayType === '1'
        ? DynamicTime
        : DynamicTimeRange
      : Time
    : Select
})

const setCondition = (queryId: string) => {
  conditions.value = cloneDeep(props.queryElement.propValue) || []
  init(queryId)
}

const setConditionOut = () => {
  conditions.value = cloneDeep(props.queryElement.propValue) || []
  addQueryCriteria()
  init(conditions.value[conditions.value.length - 1].id)
}

const init = (queryId: string) => {
  if (!datasetTree.value.length) {
    initDataset()
  }
  renameInput.value = []
  handleCondition({ id: queryId })

  dialogVisible.value = true
  const datasetFieldIdList = datasetFieldList.value.map(ele => ele.tableId)
  for (const i in datasetMap) {
    if (!datasetFieldIdList.includes(i)) {
      delete datasetMap[i]
    }
  }

  const datasetMapKeyList = Object.keys(datasetMap)

  nextTick(() => {
    getSqlParams([
      ...new Set(datasetFieldList.value.map(ele => ele.tableId).filter(ele => !!ele))
    ]).then(res => {
      parameters.value = res || []
    })
  })

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
  getDsDetailsWithPerm(params)
    .then(res => {
      res
        .filter(ele => !!ele)
        .forEach(ele => {
          const { dimensionList, quotaList } = ele.fields
          ele.list = [...dimensionList, ...quotaList]
          if (!datasetMap[ele.id]) {
            datasetMap[ele.id] = ele
          }
        })
      fields.value = datasetFieldList.value
        .map(ele => {
          if (!datasetMap[ele.tableId]) return null
          return { ...datasetMap[ele.tableId], componentId: ele.id }
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
    parametersStart: null,
    parametersEnd: null,
    relativeToCurrent: 'custom',
    timeNum: 0,
    relativeToCurrentType: 'year',
    around: 'f',
    arbitraryTime: new Date(),
    timeNumRange: 0,
    relativeToCurrentTypeRange: 'year',
    aroundRange: 'f',
    arbitraryTimeRange: new Date()
  }
  Object.entries(attributes).forEach(([key, val]) => {
    !curComponent.value[key] && (curComponent.value[key] = val)
  })
}

const handleCondition = item => {
  if (activeConditionForRename.id) return
  activeCondition.value = item.id
  curComponent.value = conditions.value.find(ele => ele.id === item.id)

  multiple.value = curComponent.value.multiple
  if (!curComponent.value.dataset.fields.length && curComponent.value.dataset.id) {
    getOptions(curComponent.value.dataset.id, curComponent.value)
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
    handleCheckedFieldsChange(curComponent.value.checkedFields)
  }
  multipleChange(curComponent.value.multiple)
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
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

const showError = computed(() => {
  if (!curComponent.value) return false
  const { optionValueSource, checkedFieldsMap, checkedFields, field, valueSource, displayType } =
    curComponent.value
  const arr = checkedFields.filter(ele => !!checkedFieldsMap[ele])
  if (!checkedFields.length || !arr.length) {
    return true
  }
  if ([1, 7].includes(+displayType)) {
    return false
  }
  return (optionValueSource === 1 && !field.id) || (optionValueSource === 2 && !valueSource.length)
})

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

const dynamicTime = computed(() => {
  return curComponent.value.timeType === 'dynamic'
})

const relativeToCurrentTypeList = computed(() => {
  if (!curComponent.value) return []
  let index = ['year', 'month', 'date', 'datetime'].indexOf(curComponent.value.timeGranularity) + 1
  if (+curComponent.value.displayType === 7) {
    index =
      ['yearrange', 'monthrange', 'datetimerange'].indexOf(
        curComponent.value.timeGranularityMultiple
      ) + 1
  }
  return [
    {
      label: '年',
      value: 'year'
    },
    {
      label: '月',
      value: 'month'
    },
    {
      label: '日',
      value: 'date'
    }
  ].slice(0, index)
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
  if (
    ['yearrange', 'monthrange', 'datetimerange'].indexOf(val) <
    ['year', 'month', 'date'].indexOf(curComponent.value.relativeToCurrentType)
  ) {
    curComponent.value.relativeToCurrentType = 'year'
  }
}
const aroundList = [
  {
    label: '前',
    value: 'f'
  },
  {
    label: '后',
    value: 'b'
  }
]

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
    <div class="container">
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
            @change="handleCheckedFieldsChange"
          >
            <div v-for="field in fields" :key="field.componentId" class="list-item">
              <el-checkbox :label="field.componentId"
                ><el-icon class="component-type">
                  <Icon :name="canvasViewInfo[field.componentId].type"></Icon> </el-icon
                ><span class="checkbox-name ellipsis">{{
                  canvasViewInfo[field.componentId].title
                }}</span></el-checkbox
              >
              <span class="dataset ellipsis">{{ field.name }}</span>
              <el-select
                @change="setType"
                style="margin-left: 12px"
                v-if="curComponent.checkedFields.includes(field.componentId)"
                v-model="curComponent.checkedFieldsMap[field.componentId]"
                clearable
              >
                <template v-if="curComponent.checkedFieldsMap[field.componentId]" #prefix>
                  <el-icon>
                    <Icon
                      :name="`field_${
                        fieldType[
                          getDetype(curComponent.checkedFieldsMap[field.componentId], field.list)
                        ]
                      }`"
                      :className="`field-icon-${
                        fieldType[
                          getDetype(curComponent.checkedFieldsMap[field.componentId], field.list)
                        ]
                      }`"
                    ></Icon>
                  </el-icon>
                </template>
                <el-option
                  v-for="ele in field.list"
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
        <div v-show="showConfiguration && !showTypeError" class="configuration-list">
          <div class="list-item">
            <div class="label">展示类型</div>
            <div class="value">
              <el-select @change="setTypeChange" v-model="curComponent.displayType">
                <el-option
                  :disabled="curComponent.displayType !== '0'"
                  label="文本下拉"
                  value="0"
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
                  :disabled="!['1', '7'].includes(curComponent.displayType)"
                  label="时间范围"
                  value="7"
                />
              </el-select>
            </div>
          </div>
          <div class="list-item" v-if="['1', '7'].includes(curComponent.displayType)">
            <div class="label">时间粒度</div>
            <div class="value">
              <template v-if="curComponent.displayType === '7'">
                <el-select
                  @change="timeGranularityMultipleChange"
                  placeholder="请选择时间粒度"
                  v-model="curComponent.timeGranularityMultiple"
                >
                  <el-option label="年" value="yearrange" />
                  <el-option label="年月" value="monthrange" />
                  <el-option label="年月日时分秒" value="datetimerange" />
                </el-select>
              </template>
              <template v-else>
                <el-select
                  @change="timeGranularityChange"
                  placeholder="请选择时间粒度"
                  v-model="curComponent.timeGranularity"
                >
                  <el-option label="年" value="year" />
                  <el-option label="年月" value="month" />
                  <el-option label="年月日" value="date" />
                  <el-option label="年月日时分秒" value="datetime" />
                </el-select>
              </template>
            </div>
          </div>
          <div class="list-item top-item" v-if="!['1', '7'].includes(curComponent.displayType)">
            <div class="label">选项值来源</div>
            <div class="value">
              <div class="value">
                <el-radio-group
                  class="larger-radio"
                  @change="handleValueSourceChange"
                  v-model="curComponent.optionValueSource"
                >
                  <el-radio :label="0">{{ t('chart.margin_model_auto') }}</el-radio>
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
                  <el-select
                    @change="handleFieldChange"
                    placeholder="请选择字段"
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
          </div>
          <div v-if="!['1', '7'].includes(curComponent.displayType)" class="list-item">
            <div class="label">选项类型</div>
            <div class="value">
              <el-radio-group
                class="larger-radio"
                @change="val => multipleChange(val as boolean, true)"
                v-model="multiple"
              >
                <el-radio :label="false">{{ t('visualization.single_choice') }}</el-radio>
                <el-radio :label="true">{{ t('visualization.multiple_choice') }}</el-radio>
              </el-radio-group>
            </div>
          </div>
          <div class="list-item">
            <div class="label">
              <el-checkbox v-model="curComponent.parametersCheck" label="绑定参数" />
            </div>
            <template v-if="curComponent.parametersCheck">
              <div v-if="curComponent.displayType !== '7'" class="parameters">
                <el-select
                  popper-class="dataset-parameters"
                  value-key="id"
                  multiple
                  v-model="curComponent.parameters"
                  clearable
                >
                  <el-option
                    v-for="item in parametersFilter"
                    :key="item.id"
                    :label="item.variableName"
                    :value="item"
                  >
                    <div class="variable-name ellipsis">{{ item.variableName }}</div>
                    <el-tooltip effect="dark" :content="item.datasetFullName" placement="top">
                      <div class="dataset-full-name ellipsis">{{ item.datasetFullName }}</div>
                    </el-tooltip>
                  </el-option>
                </el-select>
              </div>
              <div v-else class="parameters-range">
                <div class="range-title">开始时间</div>
                <div class="range-title">结束时间</div>
                <div class="params-start">
                  <el-select
                    popper-class="dataset-parameters"
                    value-key="id"
                    v-model="curComponent.parametersStart"
                    clearable
                  >
                    <el-option
                      v-for="item in parametersFilter"
                      :key="item.id"
                      :label="item.variableName"
                      :value="item"
                    >
                      <div class="variable-name ellipsis">{{ item.variableName }}</div>
                      <el-tooltip effect="dark" :content="item.datasetFullName" placement="top">
                        <div class="dataset-full-name ellipsis">{{ item.datasetFullName }}</div>
                      </el-tooltip>
                    </el-option>
                  </el-select>
                </div>
                <div class="params-end">
                  <el-select
                    popper-class="dataset-parameters"
                    value-key="id"
                    v-model="curComponent.parametersEnd"
                    clearable
                  >
                    <el-option
                      v-for="item in parametersFilter"
                      :key="item.id"
                      :label="item.variableName"
                      :value="item"
                    >
                      <div class="variable-name ellipsis">{{ item.variableName }}</div>
                      <el-tooltip effect="dark" :content="item.datasetFullName" placement="top">
                        <div class="dataset-full-name ellipsis">{{ item.datasetFullName }}</div>
                      </el-tooltip>
                    </el-option>
                  </el-select>
                </div>
              </div>
            </template>
          </div>
          <div class="list-item">
            <div class="label">
              <el-checkbox v-model="curComponent.defaultValueCheck" label="设置默认值" />
            </div>
            <div
              class="setting-content"
              v-if="curComponent.defaultValueCheck && ['1', '7'].includes(curComponent.displayType)"
            >
              <div class="setting">
                <el-radio-group v-model="curComponent.timeType">
                  <el-radio label="fixed">固定时间</el-radio>
                  <el-radio label="dynamic">动态时间</el-radio>
                </el-radio-group>
              </div>
              <template v-if="dynamicTime && curComponent.displayType === '1'">
                <div class="setting">
                  <div class="setting-label">相对当前</div>
                  <div class="setting-value select">
                    <el-select v-model="curComponent.relativeToCurrent">
                      <el-option
                        v-for="item in relativeToCurrentList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                  </div>
                </div>
                <div class="setting" v-if="curComponent.relativeToCurrent === 'custom'">
                  <div
                    class="setting-input"
                    :class="curComponent.timeGranularity === 'datetime' && 'with-date'"
                  >
                    <el-input-number
                      v-model="curComponent.timeNum"
                      :min="0"
                      controls-position="right"
                    />
                    <el-select v-model="curComponent.relativeToCurrentType">
                      <el-option
                        v-for="item in relativeToCurrentTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-select v-model="curComponent.around">
                      <el-option
                        v-for="item in aroundList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-time-picker
                      v-if="curComponent.timeGranularity === 'datetime'"
                      v-model="curComponent.arbitraryTime"
                    />
                  </div>
                </div>
              </template>
              <template v-else-if="dynamicTime && curComponent.displayType === '7'">
                <div
                  class="setting"
                  :class="
                    ['yearrange', 'monthrange'].includes(curComponent.timeGranularityMultiple) &&
                    'is-year-month-range'
                  "
                >
                  <div class="setting-label">开始时间</div>
                  <div class="setting-input with-date range">
                    <el-input-number
                      v-model="curComponent.timeNum"
                      :min="0"
                      controls-position="right"
                    />
                    <el-select v-model="curComponent.relativeToCurrentType">
                      <el-option
                        v-for="item in relativeToCurrentTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-select v-model="curComponent.around">
                      <el-option
                        v-for="item in aroundList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-time-picker v-model="curComponent.arbitraryTime" />
                  </div>
                </div>
                <div
                  class="setting"
                  :class="
                    ['yearrange', 'monthrange'].includes(curComponent.timeGranularityMultiple) &&
                    'is-year-month-range'
                  "
                >
                  <div class="setting-label">结束时间</div>
                  <div class="setting-input with-date range">
                    <el-input-number
                      v-model="curComponent.timeNumRange"
                      :min="0"
                      controls-position="right"
                    />
                    <el-select v-model="curComponent.relativeToCurrentTypeRange">
                      <el-option
                        v-for="item in relativeToCurrentTypeList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-select v-model="curComponent.aroundRange">
                      <el-option
                        v-for="item in aroundList"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </el-select>
                    <el-time-picker v-model="curComponent.arbitraryTimeRange" />
                  </div>
                </div>
              </template>
            </div>
            <div
              v-if="curComponent.defaultValueCheck"
              class="parameters"
              :class="dynamicTime && 'setting'"
            >
              <div class="setting-label" v-if="dynamicTime">预览</div>
              <div :class="dynamicTime ? 'setting-value' : 'w100'">
                <component
                  :config="curComponent"
                  isConfig
                  ref="inputCom"
                  :is="filterTypeCom"
                ></component>
              </div>
            </div>
          </div>
        </div>
        <div v-if="showTypeError && showConfiguration" class="empty">
          <empty-background description="所选字段类型不一致，无法进行查询配置" img-type="error" />
        </div>
        <div v-else-if="!showConfiguration" class="empty">
          <empty-background description="请先勾选需要联动的图表及字段" img-type="noneWhite" />
        </div>
      </div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="cancelClick">{{ t('chart.cancel') }} </el-button>
        <el-button @click="confirmClick" type="primary">{{ t('chart.confirm') }} </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<style lang="less">
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
    .ed-checkbox__label:hover {
      color: #1f2329;
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
          color: #3370ff;
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
          background: rgba(51, 112, 255, 0.1);
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
          color: #3370ff;
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
      }

      .configuration-list {
        .list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 10.5px;
          flex-wrap: wrap;

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
            width: 321px;
            .value {
              margin-top: 8px;
              &:first-child {
                margin-top: -0.5px;
              }
            }
            .ed-select {
              width: 321px;
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
