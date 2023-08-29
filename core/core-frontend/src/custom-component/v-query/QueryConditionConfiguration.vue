<script lang="ts" setup>
import { ref, reactive, nextTick, computed, shallowRef } from 'vue'
import { storeToRefs } from 'pinia'
import { dvMainStoreWithOut } from '@/store/modules/data-visualization/dvMain'
import { useI18n } from '@/hooks/web/useI18n'
import type { DatasetDetail } from '@/api/dataset'
import { getDsDetails } from '@/api/dataset'
import { cloneDeep } from 'lodash-es'
import Select from './Select.vue'
import Time from './Time.vue'
import { getDatasetTree } from '@/api/dataset'
import { Tree } from '@/views/visualized/data/dataset/form/CreatDsGroup.vue'
import draggable from 'vuedraggable'

const { t } = useI18n()
const dvMainStore = dvMainStoreWithOut()
const { componentData, canvasViewInfo } = storeToRefs(dvMainStore)

interface DatasetField {
  type: string
  title: string
  id: string
  tableId: string
}

const props = defineProps({
  addQueryCriteriaConfig: {
    type: Function,
    default: () => ({})
  }
})
const dialogVisible = ref(false)
const renameInput = ref([])
const valueSource = ref([])
const conditions = ref([])
const displayType = ref('')
const checkAll = ref(false)
const multiple = ref(false)
const activeConditionForRename = reactive({
  id: '',
  name: '',
  visible: false
})
const datasetMap = {}

const datasetFieldList = computed(() => {
  return Object.values(canvasViewInfo.value)
    .filter(ele => (ele as DatasetField).type !== 'VQuery')
    .map(ele => {
      const { id, title, tableId } = ele as DatasetField
      return {
        id,
        title,
        tableId
      }
    })
})
const curComponent = ref()
const manual = ref()
const activeCondition = ref('')
const isIndeterminate = ref(false)
const datasetTree = shallowRef([])
const fields = ref<DatasetDetail[]>()
let componentId = ''
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

const setType = () => {
  if (curComponent.value.checkedFields?.length) {
    const [id] = curComponent.value.checkedFields
    const arr = fields.value.find(ele => ele.componentId === id)
    const checkId = curComponent.value.checkedFieldsMap?.[id]
    const field = (arr?.list || []).find(ele => checkId === ele.id)
    field?.deType !== undefined && (displayType.value = `${field?.deType}`)
  }
}

const cancelClick = () => {
  dialogVisible.value = false
}

const initDataset = () => {
  getDatasetTree({}).then(res => {
    datasetTree.value = (res as unknown as Tree[]) || []
  })
}

const handleDatasetChange = () => {
  curComponent.value.field.id = ''
  getOptions(curComponent.value.dataset.id, curComponent.value)
}

const handleValueSourceChange = () => {
  curComponent.value.defaultValue = ''
  multipleChange(curComponent.value.multiple)
}

const multipleChange = (val: boolean, isTemporary = false, isMultipleChange = false) => {
  if (isMultipleChange) {
    curComponent.value.defaultValue = val ? [] : ''
  }
  const { defaultValue, temporaryValue } = curComponent.value
  const value = isTemporary ? temporaryValue : defaultValue
  if (Array.isArray(value)) {
    curComponent.value.selectValue = val ? value : ''
  } else {
    curComponent.value.selectValue = val ? (value ? [value] : []) : value
  }
  if (curComponent.value.field.deType === 1) {
    curComponent.value.multiple = val
    return
  }
  curComponent.value.multiple = val
  curComponent.value.operator = val ? 'in' : 'eq'
}

const confirmClick = () => {
  dialogVisible.value = false
  let obj = componentData.value.find(ele => ele.id === componentId)
  conditions.value.forEach(ele => {
    curComponent.value = ele
    if (curComponent.value.temporaryValue?.length) {
      multipleChange(curComponent.value.multiple, true)
    }
  })
  obj.propValue = cloneDeep(conditions.value)
}

const cancelValueSource = () => {
  manual.value.hide()
}

const confirmValueSource = () => {
  curComponent.value.valueSource = cloneDeep(valueSource.value.filter(ele => ele.trim()))
  handleValueSourceChange()
  cancelValueSource()
}

const filterTypeCom = (deType: number) => {
  return deType === 1 ? Time : Select
}

const init = (id: string, queryId: string) => {
  componentId = id
  if (!datasetTree.value.length) {
    initDataset()
  }
  renameInput.value = []
  conditions.value = cloneDeep(componentData.value.find(ele => ele.id === id).propValue) || []
  handleCondition({ id: queryId })
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
    return
  }
  getDsDetails([
    ...new Set(
      datasetFieldList.value.map(ele => ele.tableId).filter(ele => !datasetMapKeyList.includes(ele))
    )
  ])
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

const handleCondition = item => {
  activeCondition.value = item.id
  curComponent.value = conditions.value.find(ele => ele.id === item.id)
  multiple.value = curComponent.value.multiple
  if (!curComponent.value.dataset.fields.length) {
    getOptions(curComponent.value.dataset.id, curComponent.value)
  }
  datasetFieldList.value.forEach(ele => {
    if (!curComponent.value.checkedFieldsMap[ele.id]) {
      curComponent.value.checkedFieldsMap[ele.id] = ''
    }
  })
  if (!!fields.value?.length) {
    handleCheckedFieldsChange(curComponent.value.checkedFields)
  }
  multipleChange(curComponent.value.multiple, false)
  valueSource.value = cloneDeep(curComponent.value.valueSource)
  if (!valueSource.value.length) {
    valueSource.value.push('')
  }
}

const getOptions = (id, component) => {
  getDsDetails([id]).then(res => {
    res.forEach(ele => {
      if (!ele) return
      const { dimensionList, quotaList } = ele.fields
      component.dataset.fields = [...dimensionList, ...quotaList]
    })
  })
}

const setRenameInput = val => {
  renameInput.value.push(val)
}

const addOperation = (cmd, condition, index) => {
  switch (cmd) {
    case 'del':
      renameInput.value = []
      conditions.value.splice(index, 1)
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

const renameInputBlur = () => {
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
  conditions.value.push(props.addQueryCriteriaConfig())
}
defineExpose({
  init
})
</script>

<template>
  <el-dialog
    class="query-condition-configuration"
    v-model="dialogVisible"
    width="1200px"
    title="查询条件设置"
    @click.stop
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
              <div class="label" :title="element.name">
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
      <div class="chart-field">
        <div class="title">选择图表及字段</div>
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
                ><el-icon> <Icon name="icon_add_outlined"></Icon> </el-icon
                ><span class="checkbox-name ellipsis">{{
                  canvasViewInfo[field.componentId].title
                }}</span></el-checkbox
              >
              <span class="dataset ellipsis">{{ field.name }}</span>
              <el-select
                @change="setType"
                v-if="curComponent.checkedFields.includes(field.componentId)"
                v-model="curComponent.checkedFieldsMap[field.componentId]"
                clearable
              >
                <el-option
                  v-for="ele in field.list"
                  :key="ele.id"
                  :label="ele.name"
                  :value="ele.id"
                />
              </el-select>
            </div>
          </el-checkbox-group>
        </div>
      </div>
      <div class="condition-configuration">
        <div class="title">查询条件配置</div>
        <div class="configuration-list">
          <div class="list-item">
            <div class="label">展示类型</div>
            <div class="value">
              <el-select v-model="displayType">
                <el-option v-if="displayType === '0'" label="文本下拉" value="0" />
                <el-option v-if="displayType === '2'" label="数字下拉" value="2" />
                <el-option v-if="displayType === '3'" label="数字下拉" value="3" />
                <el-option v-if="displayType === '1'" label="时间" value="1" />
              </el-select>
            </div>
          </div>
          <div class="list-item top-item" v-if="displayType !== '1'">
            <div class="label">选项值来源</div>
            <div class="value">
              <div class="value">
                <el-radio-group
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
                    :data="datasetTree"
                    @change="handleDatasetChange"
                    :props="dsSelectProps"
                    :render-after-expand="false"
                    filterable
                    popper-class="dataset-tree"
                  >
                    <template #default="{ node, data }">
                      <div class="content">
                        <el-icon v-if="!data.leaf">
                          <Icon name="dv-folder"></Icon>
                        </el-icon>
                        <el-icon v-if="data.leaf">
                          <Icon name="icon_dataset"></Icon>
                        </el-icon>
                        <span class="label" :title="node.label">{{ node.label }}</span>
                      </div>
                    </template>
                  </el-tree-select>
                </div>
                <div class="value">
                  <el-select v-model="curComponent.field.id" clearable>
                    <el-option
                      v-for="ele in curComponent.dataset.fields"
                      :key="ele.id"
                      :label="ele.name"
                      :value="ele.id"
                    />
                  </el-select>
                </div>
              </template>
              <div v-if="curComponent.optionValueSource === 2" class="value">
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
                        <Icon name="edit-in"></Icon>
                      </template>
                      {{ t('common.edit') }}
                    </el-button>
                  </template>
                  <div class="manual-input-container">
                    <div class="title">{{ t('auth.manual_input') }}</div>
                    <div class="select-value">
                      <span> 选项值 </span>
                      <div :key="index" v-for="(_, index) in valueSource" class="select-item">
                        <el-input @blur="weightlessness" v-model="valueSource[index]"></el-input>
                        <el-button @click="valueSource.splice(index, 1)" class="value" text>
                          <template #icon>
                            <Icon name="icon_delete-trash_outlined"></Icon>
                          </template>
                        </el-button>
                      </div>
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
              </div>
            </div>
          </div>
          <div class="list-item">
            <div class="label">选项类型</div>
            <div class="value">
              <el-radio-group
                @change="val => multipleChange(val as boolean, false, true)"
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
            <div class="parameters">
              <el-select multiple v-model="curComponent.parameters" clearable>
                <el-option label="Zone one" value="shanghai" />
                <el-option label="Zone two" value="beijing" />
              </el-select>
            </div>
          </div>
          <div class="list-item">
            <div class="label">
              <el-checkbox v-model="curComponent.defaultValueCheck" label="设置默认值" />
            </div>
            <div class="parameters">
              <component
                :config="curComponent"
                isConfig
                :is="filterTypeCom(curComponent.field.deType)"
              ></component>
            </div>
          </div>
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
.query-condition-configuration {
  .container {
    font-size: 14px;
    font-family: PingFang SC;
    width: 1152px;
    height: 454px;
    border-radius: 4px;
    border: 1px solid #dee0e3;
    display: flex;
    .query-condition-list {
      height: 100%;
      background: #f5f6f7;
      border-right: 1px solid #dee0e3;
      width: 208px;

      .title {
        padding: 16px;
        display: flex;
        align-items: center;
        justify-content: space-between;

        .ed-icon {
          cursor: pointer;
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

    .chart-field {
      border-right: 1px solid #dee0e3;
      height: 100%;
      padding: 16px;
      width: 474px;
      overflow-y: auto;

      .select-all {
        height: 40px;
      }

      .field-list {
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

    .condition-configuration {
      padding: 16px;
      width: 467px;
      .title {
        margin-bottom: 16px;
      }

      .configuration-list {
        .list-item {
          display: flex;
          align-items: center;
          justify-content: space-between;
          margin-bottom: 16px;
          flex-wrap: wrap;

          &.top-item {
            .label {
              margin-bottom: auto;
              padding-top: 10px;
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
            }
            .ed-select {
              width: 321px;
            }
          }

          .parameters {
            margin-left: auto;
            .ed-select,
            .ed-date-editor,
            .ed-date-editor--datetime .ed-input__wrapper,
            .ed-select-v2 {
              width: 415px;
            }
          }
        }
      }
    }
  }
}
.manual-input {
  height: 490px;
  padding: 0 !important;

  .manual-input-container {
    .title {
      padding: 16px;
    }
    .select-value {
      padding: 0 0 16px 16px;
      height: 374px;
      overflow-y: auto;
      .value {
        color: #646a73;
        margin-left: 6px;
        font-size: 20px;
      }

      .select-item {
        margin: 8px 0;
        .ed-input {
          width: 298px;
        }
      }
    }
    .manual-footer {
      padding: 16px;
      height: 63px;
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
    }
  }
}
</style>
