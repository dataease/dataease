<script lang="tsx" setup>
import { ref, nextTick, reactive, shallowRef, computed, watch } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElIcon, ElMessageBox, ElMessage } from 'element-plus-secondary'
import type { Action } from 'element-plus-secondary'
import FieldMore from './FieldMore.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { Icon } from '@/components/icon-custom'
import CalcFieldEdit from './CalcFieldEdit.vue'
import { useRoute, useRouter } from 'vue-router'
import UnionEdit from './UnionEdit.vue'
import type { FormInstance } from 'element-plus-secondary'
import CreatDsGroup from './CreatDsGroup.vue'
import { guid, getFieldName, timeTypes } from './util.js'
import {
  getDatasourceList,
  getTables,
  getPreviewData,
  getDatasetDetails,
  saveDatasetTree
} from '@/api/dataset'
import type { Table } from '@/api/dataset'
import DatasetUnion from './DatasetUnion.vue'
import { cloneDeep } from 'lodash-es'
interface DragEvent extends MouseEvent {
  dataTransfer: DataTransfer
}

export interface DataSource {
  id: string
  name: string
}

interface Field {
  fieldShortName: string
  name: string
  dataeaseName: string
  originName: string
  deType: number
}

const { t } = useI18n()
const route = useRoute()
const { push } = useRouter()
const creatDsFolder = ref()
const editCalcField = ref(false)
const calcEdit = ref()
const editUnion = ref(false)
const datasetDrag = ref()
const datasetName = ref('新建数据集')
const tabActive = ref('preview')
const originName = ref('')
const activeName = ref('')
const dataSource = ref('')
const searchTable = ref('')
const showInput = ref(false)
const dsLoading = ref(false)
const LeftWidth = ref(240)
const offsetX = ref(0)
const offsetY = ref(0)
const showLeft = ref(true)
const maskShow = ref(false)
const loading = ref(false)
const nameExist = ref(false)
const updateCustomTime = ref(false)
const editerName = ref()
const currentField = ref({
  dateFormat: '',
  id: '',
  dateFormatType: '',
  name: ''
})

const ruleFormRef = ref<FormInstance>()

const rules = {
  name: [{ required: true, message: '自定义时间格式不能为空', trigger: 'blur' }]
}

const sqlNode = reactive<Table>({
  datasourceId: '',
  name: '',
  tableName: '自定义SQL',
  type: 'sql'
})

let nodeInfo = {
  id: '',
  pid: '',
  name: ''
}

const defaultProps = {
  children: 'children',
  label: 'label'
}

let tableList = []

const dragHeight = ref(280)
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const getDsName = (id: string) => {
  return (state.dataSourceList.find(ele => ele.id === id) || {}).name
}

const backToMain = () => {
  push('/data/dataset')
}

const closeCustomTime = () => {
  dimensions.value.concat(quota.value).some(ele => {
    if (ele.id === currentField.value.id) {
      delete currentField.value.name
      Object.assign(ele, currentField.value)
      return true
    }
    return false
  })
  updateCustomTime.value = false
}

const confirmCustomTime = () => {
  ruleFormRef.value.validate(valid => {
    if (valid) {
      dimensions.value.concat(quota.value).some(ele => {
        if (ele.id === currentField.value.id) {
          ele.dateFormat = currentField.value.name
          return true
        }
        return false
      })
      updateCustomTime.value = false
    }
  })
}

watch(searchTable, val => {
  state.tableData = tableList.filter(ele => ele.name.includes(val))
})
const editeSave = () => {
  const union = []
  loading.value = true
  dfsNodeList(union, datasetDrag.value.nodeList)
  saveDatasetTree({
    ...nodeInfo,
    name: datasetName.value,
    union,
    allFields: allfields.value,
    nodeType: 'dataset'
  })
    .then(() => {
      ElMessage.success('保存成功')
    })
    .finally(() => {
      loading.value = false
    })
}

const handleFieldMore = (ele, type) => {
  const arr = ['text', 'time', 'number', 'float', 'location']
  if (arr.includes(type as string)) {
    ele.deType = arr.indexOf(type)
    ele.dateFormat = ''
    return
  }
  if (timeTypes.includes(type as string)) {
    currentField.value.dateFormat = ele.dateFormat
    currentField.value.dateFormatType = ele.dateFormatType

    ele.deType = 1
    ele.dateFormatType = type
    ele.dateFormat = type
  }
  switch (type) {
    case 'copy':
      copyField(ele)
      break
    case 'delete':
      deleteField(ele)
      break
    case 'translate':
      dqTrans(ele.id)
      break
    case 'editor':
      editField(ele)
      break
    case 'custom':
      currentField.value.id = ele.id
      updateCustomTime.value = true
      break
    default:
      break
  }
}

const dqTrans = id => {
  const obj = allfields.value.find(ele => ele.id === id)
  obj.groupType = obj.groupType === 'd' ? 'q' : 'd'
}

const copyField = item => {
  const param = { ...item }
  param.id = guid()
  param.extField = 2
  param.originName = item.extField === 2 ? item.originName : '[' + item.id + ']'
  param.name = getFieldName(dimensions.value.concat(quota.value), item.name)
  param.dataeaseName = null
  param.lastSyncTime = null
  allfields.value.push(param)
}

const deleteField = item => {
  ElMessageBox.confirm(t('dataset.confirm_delete'), {
    confirmButtonText: t('dataset.confirm'),
    cancelButtonText: t('common.cancel'),
    showCancelButton: true,
    tip: t('chart.tips'),
    confirmButtonType: 'primary',
    type: 'warning',
    autofocus: false,
    showClose: false,
    callback: (action: Action) => {
      if (action === 'confirm') {
        const idx = allfields.value.indexOf(item.id)
        allfields.value.splice(idx, 1)
        ElMessage({
          message: t('chart.delete_success'),
          type: 'success'
        })
      }
    }
  })
}

const addCalcField = groupType => {
  editCalcField.value = true
  nextTick(() => {
    calcEdit.value.initEdit({ groupType, id: guid() }, dimensions.value, quota.value)
  })
}

const editField = item => {
  editCalcField.value = true
  nextTick(() => {
    calcEdit.value.initEdit(item, dimensions.value, quota.value)
  })
}

const closeEditCalc = () => {
  editCalcField.value = false
}

const confirmEditCalc = () => {
  calcEdit.value.setFieldForm()
  const obj = cloneDeep(calcEdit.value.fieldForm)
  const result = allfields.value.findIndex(ele => obj.id === ele.id)
  if (result !== -1) {
    allfields.value.splice(result, 1, obj)
  } else {
    allfields.value.push(obj)
  }
  editCalcField.value = false
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.dataeaseName,
    deType: ele.deType,
    dataKey: ele.dataeaseName,
    title: ele.name,
    width: 150,
    headerCellRenderer: ({ column }) => (
      <div style={{ width: '100%', display: 'flex', alignItems: 'center' }}>
        <ElIcon>
          <Icon
            name={`field_${fieldType(column.deType)}`}
            className={`field-icon-${fieldType(column.deType)}`}
          ></Icon>
        </ElIcon>
        {column.title}
      </div>
    )
  }))

const initEdite = () => {
  const { id } = route.query
  if (!id) return
  loading.value = true
  getDatasetDetails(id)
    .then(res => {
      let arr = []
      const { id, pid, name } = res || {}
      nodeInfo = {
        id,
        pid,
        name
      }
      datasetName.value = name
      originName.value = name
      allfields.value = res.allFields || []
      dfsUnion(arr, res.union || [])
      datasetDrag.value.initState(arr)
    })
    .finally(() => {
      showTable.value = true
      loading.value = false
    })
}

initEdite()

const joinEditor = (arr: []) => {
  Object.assign(state.editArr, arr)
  editUnion.value = true
  nextTick(() => {
    fieldUnion.value.initState()
  })
}

const columns = shallowRef([])
const tableData = shallowRef([])
const showTable = ref(false)
const quota = computed(() => {
  return cloneDeep(allfields.value.filter(ele => ele.groupType === 'q'))
})

const dimensions = computed(() => {
  return cloneDeep(allfields.value.filter(ele => ele.groupType === 'd'))
})

const addComplete = () => {
  state.nodeNameList = [...datasetDrag.value.nodeNameList]
}

const state = reactive({
  nameList: [],
  nodeNameList: [],
  editArr: [],
  nodeList: [],
  dataSourceList: [],
  tableData: []
})

const allfields = ref([])

let num = +new Date()

const expandedD = ref(false)
const expandedQ = ref(false)
const setGuid = (arr, id, datasourceId) => {
  arr.forEach(ele => {
    if (!ele.id) {
      ele.id = `${++num}`
      ele.datasetTableId = id
      ele.datasourceId = datasourceId
    }
  })
}

const dfsFields = (arr, list) => {
  list.forEach(ele => {
    if (ele.children?.length) {
      dfsFields(arr, ele.children)
    }
    const { currentDsFields } = ele
    arr.push(...cloneDeep(currentDsFields))
  })
}

const diffArr = (newArr, oldArr) => {
  const idMapNew = newArr.map(ele => ele.id)
  const idMapOld = oldArr.map(ele => ele.id)
  return newArr
    .filter(ele => !idMapOld.includes(ele.id))
    .concat(oldArr.filter(ele => idMapNew.includes(ele.id)))
}

const closeEditUnion = () => {
  notConfirmEditUnion()
  fieldUnion.value.clearState()
  editUnion.value = false
}
const fieldUnion = ref()
const confirmEditUnion = () => {
  const { node, parent } = fieldUnion.value
  setGuid(node.currentDsFields, node.id, node.datasourceId)
  setGuid(parent.currentDsFields, parent.id, parent.datasourceId)
  datasetDrag.value.setStateBack(cloneDeep(node), cloneDeep(parent))
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  allfields.value = diffArr(arr, allfields.value)
  fieldUnion.value.clearState()
  editUnion.value = false
}

const updateAllfields = () => {
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  allfields.value = diffArr(arr, allfields.value)
  fieldUnion.value?.clearState()
}

const notConfirmEditUnion = () => {
  datasetDrag.value.notConfirm()
}

const dragstart = (e: DragEvent, ele) => {
  offsetX.value = e.offsetX
  offsetY.value = e.offsetY
  e.dataTransfer.setData('text/plain', JSON.stringify(ele))
  maskShow.value = true
}
const setActiveName = (data: Table) => {
  if (data.unableCheck) return
  activeName.value = data.tableName
}

const mousedownDrag = () => {
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateWidth)
}
const mouseupDrag = () => {
  const dom = document.querySelector('.dataset-db')
  dom.removeEventListener('mousemove', calculateWidth)
  dom.removeEventListener('mousemove', calculateHeight)
}
const calculateWidth = (e: MouseEvent) => {
  if (e.pageX < 240) {
    LeftWidth.value = 240
    return
  }
  if (e.pageX > 500) {
    LeftWidth.value = 500
    return
  }
  LeftWidth.value = e.pageX
}

const mousedownDragH = () => {
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateHeight)
}
const calculateHeight = (e: MouseEvent) => {
  if (e.pageY - 64 < 280) {
    dragHeight.value = 280
    return
  }
  if (e.pageY - 64 > document.documentElement.clientHeight - 170) {
    dragHeight.value = document.documentElement.clientHeight - 170
    return
  }
  dragHeight.value = e.pageY - 64
}

const nameExistValidator = () => {
  if (!state.nameList || state.nameList.length === 0) {
    nameExist.value = false
    return
  }
  nameExist.value = state.nameList.some(
    name => name === datasetName.value && name !== originName.value
  )
}

const nameBlur = () => {
  nameExistValidator()
  showInput.value = nameExist.value
}

const getDatasource = () => {
  getDatasourceList().then(res => {
    state.dataSourceList = (res as unknown as DataSource[]) || []
  })
}

getDatasource()

const dsChange = (val: string) => {
  sqlNode.datasourceId = dataSource.value
  getTables(val).then(res => {
    tableList = res || []
    state.tableData = [...tableList]
  })
}
const datasetSave = () => {
  if (nameExist.value) return
  if (nodeInfo.id) {
    editeSave()
    return
  }
  const union = []
  dfsNodeList(union, datasetDrag.value.nodeList)
  const { pid } = route.query
  if (!union.length) {
    ElMessage.error('数据集不能为空')
    return
  }

  creatDsFolder.value.createInit(
    'dataset',
    { id: pid || '0', union, allfields: allfields.value },
    '',
    datasetName.value
  )
}

const datasetPreview = () => {
  const arr = []
  showTable.value = false
  dfsNodeList(arr, datasetDrag.value.nodeList)
  getPreviewData({ union: arr, allFields: allfields.value })
    .then(res => {
      columns.value = generateColumns((res.data.fields as Field[]) || [])
      tableData.value = (res.data.data as Array<{}>) || []
    })
    .finally(() => {
      showTable.value = true
    })
}

const dfsNodeList = (arr, list) => {
  list.forEach(ele => {
    const childrenDs = []
    if (ele.children?.length) {
      dfsNodeList(childrenDs, ele.children)
    }
    const {
      tableName,
      type,
      datasourceId,
      id,
      info,
      unionType,
      unionFields,
      currentDsFields,
      sqlVariableDetails
    } = ele
    arr.push({
      currentDs: {
        sqlVariableDetails,
        tableName,
        type,
        datasourceId,
        id,
        info
      },
      currentDsFields,
      childrenDs,
      unionToParent: {
        unionType,
        unionFields
      }
    })
  })
}

const dfsUnion = (arr, list) => {
  list.forEach(ele => {
    const children = []
    if (ele.childrenDs?.length) {
      dfsUnion(children, ele.childrenDs)
    }
    const { unionToParent, currentDsFields, currentDs, sqlVariableDetails } = ele
    const { tableName, type, datasourceId, id, info } = currentDs || {}
    const { unionType, unionFields } = unionToParent || {}
    arr.push({
      sqlVariableDetails,
      tableName,
      type,
      datasourceId,
      id,
      info,
      currentDsFields,
      children,
      unionType,
      unionFields
    })
  })
}
const handleClick = () => {
  showInput.value = true
  nextTick(() => {
    editerName.value.focus()
  })
}

const treeProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="de-dataset-form" v-loading="loading">
    <div class="top">
      <span class="name">
        <el-icon @click="backToMain">
          <Icon name="icon_left_outlined"></Icon>
        </el-icon>
        <template v-if="showInput">
          <el-input ref="editerName" v-model="datasetName" @blur="nameBlur" />
          <div v-if="nameExist" style="left: 55px" class="el-form-item__error">
            {{ t('deDataset.already_exists') }}
          </div>
        </template>
        <template v-else>
          <span @click="handleClick" class="dataset-name" style="margin: 0 5px">{{
            datasetName
          }}</span>
        </template>
      </span>
      <span class="oprate">
        <el-button type="primary" @click="datasetSave">保存</el-button>
      </span>
    </div>
    <div class="container dataset-db" @mouseup="mouseupDrag">
      <p v-show="!showLeft" class="arrow-right" @click="showLeft = true">
        <el-icon>
          <Icon name="icon_down-right_outlined"></Icon>
        </el-icon>
      </p>
      <div
        v-show="showLeft"
        :style="{ left: LeftWidth + 'px' }"
        class="drag-left"
        @mousedown="mousedownDrag"
      />
      <div
        v-loading="dsLoading"
        v-show="showLeft"
        class="table-list"
        :style="{ width: LeftWidth + 'px' }"
      >
        <p class="select-ds">
          选择数据源
          <el-icon @click="showLeft = false">
            <Icon name="icon_up-left_outlined"></Icon>
          </el-icon>
        </p>
        <el-tree-select
          :check-strictly="false"
          @change="dsChange"
          :placeholder="t('dataset.pls_slc_data_source')"
          class="ds-list"
          popper-class="tree-select-ds_popper"
          v-model="dataSource"
          node-key="id"
          :props="treeProps"
          :data="state.dataSourceList"
          :render-after-expand="false"
        />
        <p class="select-ds table-num">
          {{ t('datasource.data_table') }}
          <span class="num">
            <el-icon>
              <Icon name="reference-table"></Icon>
            </el-icon>
            {{ state.tableData.length }}
          </span>
        </p>
        <el-input
          v-model="searchTable"
          class="search"
          :placeholder="t('deDataset.by_table_name')"
          clearable
        >
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
        <div v-if="!state.tableData.length && searchTable !== ''" class="el-empty">
          <div class="el-empty__description" style="margin-top: 80px; color: #5e6d82">
            没有找到相关内容
          </div>
        </div>
        <div v-else class="table-checkbox-list">
          <div
            class="list-item_primary"
            v-if="dataSource"
            @dragstart="$event => dragstart($event, sqlNode)"
            @dragend="maskShow = false"
            :draggable="true"
            @click="setActiveName(sqlNode)"
          >
            <el-icon>
              <Icon name="reference-table"></Icon>
            </el-icon>
            <span class="label">自定义SQL</span>
          </div>
          <template v-for="ele in state.tableData" :key="ele.name">
            <div
              :class="[
                {
                  active: activeName === ele.tableName,
                  'not-allow': state.nodeNameList.includes(ele.tableName)
                }
              ]"
              class="list-item_primary"
              :title="ele.name"
              @dragstart="$event => dragstart($event, ele)"
              @dragend="maskShow = false"
              :draggable="!state.nodeNameList.includes(ele.tableName)"
              @click="setActiveName(ele)"
            >
              <el-icon>
                <Icon name="reference-table"></Icon>
              </el-icon>
              <span class="label">{{ ele.tableName }}</span>
            </div>
          </template>
        </div>
      </div>
      <div class="drag-right">
        <dataset-union
          @join-editor="joinEditor"
          :maskShow="maskShow"
          :dragHeight="dragHeight"
          :getDsName="getDsName"
          :offsetX="offsetX"
          :offsetY="offsetY"
          ref="datasetDrag"
          @updateAllfields="updateAllfields"
          @addComplete="addComplete"
        ></dataset-union>
        <div class="sql-result" :style="{ height: `calc(100% - ${dragHeight}px)` }">
          <div class="sql-title">
            <span class="drag" @mousedown="mousedownDragH" />
            <div class="field-data">
              <el-button :disabled="!allfields.length" @click="datasetPreview" secondary>
                <template #icon>
                  <el-icon>
                    <Icon name="scene"></Icon>
                  </el-icon>
                </template>
                刷新
              </el-button>
            </div>
          </div>
          <el-tabs class="padding-24" v-model="tabActive">
            <el-tab-pane :label="$t('deDataset.running_results')" name="preview" />
            <el-tab-pane :label="$t('dataset.task.record')" name="manage" />
          </el-tabs>
          <div v-if="tabActive === 'preview'" class="table-preview">
            <div class="preview-field">
              <div class="field-d">
                <div :class="['title', { expanded: expandedD }]" @click="expandedD = !expandedD">
                  <ElIcon class="expand">
                    <Icon name="icon_expand-right_filled"></Icon>
                  </ElIcon>
                  &nbsp;维度
                  <ElIcon class="add hover-icon" @click.stop="addCalcField('d')">
                    <Icon name="icon_add_outlined"></Icon>
                  </ElIcon>
                </div>
                <el-tree v-if="expandedD" :data="dimensions" :props="defaultProps">
                  <template #default="{ data }">
                    <span class="custom-tree-node">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType(data.deType)}`"
                          :className="`field-icon-${fieldType(data.deType)}`"
                        ></Icon>
                      </el-icon>
                      <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                      <div class="operate">
                        <field-more
                          :extField="data.extField"
                          :show-time="data.deType === 1 && data.deExtractType === 0"
                          @handle-command="type => handleFieldMore(data, type)"
                        ></field-more>
                      </div>
                    </span>
                  </template>
                </el-tree>
              </div>
              <div class="field-q">
                <div :class="['title', { expanded: expandedQ }]" @click="expandedQ = !expandedQ">
                  <ElIcon class="expand">
                    <Icon name="icon_expand-right_filled"></Icon>
                  </ElIcon>
                  &nbsp;指标
                  <ElIcon class="add hover-icon" @click.stop="addCalcField('q')">
                    <Icon name="icon_add_outlined"></Icon>
                  </ElIcon>
                </div>
                <el-tree v-if="expandedQ" :data="quota" :props="defaultProps">
                  <template #default="{ data }">
                    <span class="custom-tree-node">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType(data.deType)}`"
                          :className="`field-icon-${fieldType(data.deType)}`"
                        ></Icon>
                      </el-icon>
                      <span :title="data.name" class="label-tooltip">{{ data.name }}</span>
                      <div class="operate">
                        <field-more
                          :extField="data.extField"
                          @handle-command="type => handleFieldMore(data, type)"
                        ></field-more>
                      </div>
                    </span>
                  </template>
                </el-tree>
              </div>
            </div>
            <div class="preview-data">
              <el-auto-resizer>
                <template #default="{ height, width }">
                  <el-table-v2
                    :columns="columns"
                    header-class="header-cell"
                    :data="tableData"
                    :width="width"
                    :height="height"
                    fixed
                    ><template #empty>
                      <empty-background description="暂无数据" img-type="noneWhite" />
                    </template>
                  </el-table-v2>
                </template>
              </el-auto-resizer>
            </div>
          </div>
          <div v-else class="table-manage"></div>
        </div>
      </div>
    </div>
    <el-drawer
      :title="t('dataset.edit_union_relation')"
      v-model="editUnion"
      custom-class="union-dataset-drawer"
      size="840px"
      :before-close="closeEditUnion"
      direction="rtl"
    >
      <union-edit ref="fieldUnion" :editArr="state.editArr" />
      <template #footer>
        <el-button secondary @click="closeEditUnion()">{{ t('dataset.cancel') }} </el-button>
        <el-button type="primary" @click="confirmEditUnion()"
          >{{ t('dataset.confirm') }}
        </el-button>
      </template>
    </el-drawer>
  </div>
  <creat-ds-group ref="creatDsFolder"></creat-ds-group>
  <el-dialog v-model="editCalcField" width="1000px" title="新建计算字段">
    <calc-field-edit ref="calcEdit" />
    <template #footer>
      <el-button secondary @click="closeEditCalc()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmEditCalc()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
  <el-dialog v-model="updateCustomTime" width="1000px">
    <el-form ref="ruleFormRef" :rules="rules" :model="currentField" label-width="120px">
      <el-form-item prop="name" label="自定义时间格式">
        <el-input v-model="currentField.name" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button secondary @click="closeCustomTime()">{{ t('dataset.cancel') }} </el-button>
      <el-button type="primary" @click="confirmCustomTime()">{{ t('dataset.confirm') }} </el-button>
    </template>
  </el-dialog>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';
.de-dataset-form {
  color: #1f2329;
  .top {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    background: #050e21;
    box-shadow: 0px 2px 4px 0px rgba(31, 35, 41, 0.12);

    .name {
      color: #fff;
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 400;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;
      .dataset-name {
        cursor: pointer;
      }

      .ed-input {
        min-width: 96px;
        .ed-input__inner {
          line-height: 24px;
          height: 24px;
        }
      }
      i {
        cursor: pointer;
      }
    }
  }

  .container {
    width: 100%;
    height: calc(100vh - 56px);
    position: relative;
    .drag-left {
      position: absolute;
      height: calc(100vh - 56px);
      width: 2px;
      top: 0;
      z-index: 5;
      cursor: col-resize;
    }

    .arrow-right {
      position: absolute;
      top: 15px;
      z-index: 2;
      cursor: pointer;
      margin: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      left: 0;
      height: 24px;
      width: 20px;
      box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
      border: 1px solid var(--deCardStrokeColor, #dee0e3);
      border-top-right-radius: 13px;
      border-bottom-right-radius: 13px;
    }

    .table-list {
      p {
        margin: 0;
      }

      height: 100%;
      width: 240px;
      padding: 16px 12px;
      font-family: PingFang SC;
      border-right: 1px solid rgba(31, 35, 41, 0.15);

      .select-ds {
        font-size: 14px;
        font-weight: 500;
        display: flex;
        justify-content: space-between;
        align-items: center;
        color: var(--deTextPrimary, #1f2329);

        i {
          cursor: pointer;
          font-size: 12px;
          color: var(--deTextPlaceholder, #8f959e);
        }
      }

      .table-num {
        .num {
          display: flex;
          align-items: center;
          font-weight: 400;
          font-size: 14px;
          color: #646a73;
          .ed-icon {
            margin-right: 5.33px;
          }
        }

        i {
          cursor: auto;
          font-size: 13.3px;
          color: var(--deTextPlaceholder, #8f959e);
        }
      }

      .search {
        margin: 12px 0;
      }

      .ds-list {
        margin: 12px 0 24px 0;
        width: 100%;
      }

      .table-checkbox-list {
        height: calc(100% - 190px);
        overflow-y: auto;

        .not-allow {
          cursor: not-allowed;
          color: var(--deTextDisable, #bbbfc4);
        }
      }
    }
  }

  .dataset-db {
    display: flex;
    .drag-right {
      flex: 1;
      height: calc(100vh - 56px);
      .sql-result {
        font-family: PingFang SC;
        font-size: 14px;
        overflow-y: auto;
        box-sizing: border-box;

        .sql-title {
          user-select: none;
          height: 10px;
          position: relative;
          color: var(--deTextPrimary, #1f2329);

          .field-data {
            position: absolute;
            right: 24px;
            top: 13px;
            width: 50%;
            z-index: 2;
            text-align: right;
          }

          .drag {
            position: absolute;
            top: 0;
            left: 50%;
            transform: translateX(-50%);
            height: 7px;
            width: 100px;
            border-radius: 3.5px;
            background: rgba(31, 35, 41, 0.1);
            cursor: row-resize;
          }
        }

        .padding-24 {
          .border-bottom-tab(24px);
        }

        .table-preview {
          height: calc(100% - 56px);
          box-sizing: border-box;

          .preview-data {
            float: right;
            height: 100%;
            width: calc(100% - 260px);

            :deep(.ed-table-v2__header-cell) {
              background-color: #f5f6f7 !important;
            }
          }

          .preview-field {
            float: left;
            width: 260px;
            height: 100%;
            border-right: 1px solid rgba(31, 35, 41, 0.15);

            :deep(.ed-tree-node__content) {
              border-radius: 4px;
              &:hover {
                background: rgba(31, 35, 41, 0.1);
              }
            }

            :deep(.ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after) {
              display: none;
            }

            .custom-tree-node {
              flex: 1;
              display: flex;
              align-items: center;
              padding-right: 8px;
              box-sizing: content-box;

              .label-tooltip {
                margin-left: 5.33px;
                width: 60%;
                overflow: hidden;
                white-space: nowrap;
                text-overflow: ellipsis;
              }

              .operate {
                margin-left: auto;
                position: relative;
                z-index: 5;
              }
            }

            @keyframes hg {
              from {
                height: 0;
              }

              to {
                height: 30px;
              }
            }

            .field-d,
            .field-q {
              padding: 0 8px;
              position: relative;
              .title {
                cursor: pointer;
                position: sticky;
                margin: 1px;
                top: 1px;
                height: 49px;
                font-family: 'PingFang SC';
                font-style: normal;
                font-weight: 500;
                font-size: 14px;
                line-height: 22px;
                color: #1f2329;
                display: flex;
                align-items: center;
                z-index: 10;
                background: #fff;

                .add {
                  margin-left: auto;
                }
                i {
                  color: #646a73;
                }

                &.expanded {
                  .expand {
                    transform: rotate(90deg);
                  }
                }
              }
              max-height: 200px;
              overflow-y: auto;
              .ed-tree {
                animation: hg 0.5s;
              }
            }

            .field-d {
              border-bottom: 1px solid rgba(31, 35, 41, 0.15);
            }
          }
        }
      }
    }
  }
}
</style>

<style lang="less">
.ed-select-dropdown__item {
  display: flex;
  align-items: center;
  .ed-icon {
    font-size: 14px;
    margin-right: 5.25px;
  }
}
.tree-select-ds_popper {
  .ed-tree-node.is-current > .ed-tree-node__content:not(.is-menu):after {
    display: none !important;
  }
}
</style>
