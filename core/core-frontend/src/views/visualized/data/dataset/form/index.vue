<script lang="tsx" setup>
import { ref, nextTick, reactive, shallowRef, computed, toRaw } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElIcon } from 'element-plus-secondary'
import { Icon } from '@/components/icon-custom'
import CalcFieldEdit from './CalcFieldEdit.vue'
import AddSql from './AddSql.vue'
import { useRoute } from 'vue-router'
import UnionEdit from './UnionEdit.vue'
import CreatDsGroup from './CreatDsGroup.vue'
// import { guid } from './util.js'
import {
  getDatasourceList,
  getTables,
  getPreviewData,
  getDatasetDetails,
  saveDatasetTree
} from '@/api/dataset'
import type { Table } from '@/api/dataset'
import DatasetUnion from './DatasetUnion.vue'
import { clone } from 'lodash-es'
interface DragEvent extends MouseEvent {
  dataTransfer: DataTransfer
}

interface DataSource {
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
const creatDsFolder = ref()
const editCalcField = ref(false)
const editSqlField = ref(false)
const editUnion = ref(false)
const datasetDrag = ref()
const datasetName = ref('新建数据源')
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
const datasetType = ref('sql')
const editerName = ref()

let nodeInfo = {
  id: '',
  pid: '',
  name: ''
}

const dragHeight = ref(280)
const fieldType = (deType: number) => {
  return ['text', 'time', 'value', 'value', 'location'][deType]
}

const editeSave = () => {
  const union = []
  loading.value = true
  dfsNodeList(union, datasetDrag.value.nodeList)
  saveDatasetTree({ ...nodeInfo, union, allFields: allfields.value, nodeType: 'dataset' }).finally(
    () => {
      loading.value = false
    }
  )
}

const generateColumns = (arr: Field[]) =>
  arr.map(ele => ({
    key: ele.fieldShortName,
    deType: ele.deType,
    dataKey: ele.fieldShortName,
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
  state.editArr = arr
  editUnion.value = true
}

const columns = shallowRef([])
const tableData = shallowRef([])
const showTable = ref(false)
const quota = computed(() => {
  return allfields.value.filter(ele => ele.groupType === 'q')
})

const dimensions = computed(() => {
  return allfields.value.filter(ele => ele.groupType === 'd')
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
  tableData: [],
  table: {
    name: '',
    id: ''
  }
})

const allfields = ref([])

let num = +new Date()

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
    arr.push(...clone(currentDsFields))
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
  editUnion.value = false
}
const fieldUnion = ref()
const confirmEditUnion = () => {
  const { node, parent } = fieldUnion.value
  setGuid(node.currentDsFields, node.id, node.datasourceId)
  setGuid(parent.currentDsFields, parent.id, parent.datasourceId)
  datasetDrag.value.setStateBack(node, parent)
  const arr = []
  dfsFields(arr, datasetDrag.value.nodeList)
  allfields.value = diffArr(arr, allfields.value)
  editUnion.value = false
  // 校验关联关系与字段，必填
  // if (this.checkUnion()) {
  //   this.editUnion = false
  // } else {
  //   this.openMessageSuccess('dataset.union_error')
  // }
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
    name => name === state.table.name && name !== originName.value
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
  getTables(val).then(res => {
    state.tableData = (res as unknown as Table[]) || []
  })
}
const datasetSave = () => {
  if (nodeInfo.id) {
    editeSave()
    return
  }
  const union = []
  dfsNodeList(union, datasetDrag.value.nodeList)
  const { pid } = route.query
  if (!pid || !union.length) {
    return
  }
  creatDsFolder.value.createInit('dataset', { id: pid, union, allfields: allfields.value })
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
</script>

<template>
  <div class="de-dataset-form" v-loading="loading">
    <div class="top">
      <span class="name">
        <el-icon>
          <Icon :name="`de-${datasetType}-new`"></Icon>
        </el-icon>
        <template v-if="showInput">
          <el-input ref="editerName" v-model="state.table.name" @blur="nameBlur" />
          <div v-if="nameExist" style="left: 55px" class="el-form-item__error">
            {{ t('deDataset.already_exists') }}
          </div>
        </template>
        <template v-else>
          <span style="margin: 0 5px">{{ datasetName }}</span>
          <el-icon style="margin-left: 5px" @click="handleClick">
            <Icon name="icon_edit_outlined"></Icon>
          </el-icon>
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
        <el-select
          v-model="dataSource"
          class="ds-list"
          filterable
          @change="dsChange"
          :placeholder="t('dataset.pls_slc_data_source')"
        >
          <el-option
            v-for="item in state.dataSourceList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
        <p class="select-ds">{{ t('datasource.data_table') }}</p>
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
          :offsetX="offsetX"
          :offsetY="offsetY"
          ref="datasetDrag"
          @addComplete="addComplete"
        ></dataset-union>
        <div class="sql-result" :style="{ height: `calc(100% - ${dragHeight}px)` }">
          <div class="sql-title">
            <span class="drag" @mousedown="mousedownDragH" />
            <div class="field-data">
              <el-button @click="datasetPreview" secondary>
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
              <el-collapse accordion>
                <el-collapse-item name="1">
                  <template #title>
                    维度
                    <ElIcon>
                      <Icon name="icon_add_outlined"></Icon>
                    </ElIcon>
                  </template>
                  <div class="field-d">
                    <div :key="ele.id" v-for="ele in quota" class="list-item_primary">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType(ele.deType)}`"
                          :className="`field-icon-${fieldType(ele.deType)}`"
                        ></Icon>
                      </el-icon>
                      {{ ele.name }}
                    </div>
                  </div>
                </el-collapse-item>
                <el-collapse-item title="Feedback" name="2">
                  <template #title>
                    指标
                    <ElIcon>
                      <Icon name="icon_add_outlined"></Icon>
                    </ElIcon>
                  </template>
                  <div class="field-q">
                    <div :key="ele.id" v-for="ele in dimensions" class="list-item_primary">
                      <el-icon>
                        <Icon
                          :name="`field_${fieldType(ele.deType)}`"
                          :className="`field-icon-${fieldType(ele.deType)}`"
                        ></Icon>
                      </el-icon>
                      {{ ele.name }}
                    </div>
                  </div>
                </el-collapse-item>
              </el-collapse>
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
                  />
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
    <calc-field-edit :param="{ id: 0 }" />
  </el-dialog>
  <el-dialog fullscreen class="sql-dialog-fullscreen" append-to-body v-model="editSqlField">
    <add-sql></add-sql>
  </el-dialog>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';
.de-dataset-form {
  .top {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24px;
    box-shadow: 0 2px 2px 0 rgb(0 0 0 / 10%);

    .name {
      font-family: PingFang SC;
      font-size: 16px;
      font-weight: 500;
      display: flex;
      align-items: center;
      width: 50%;
      position: relative;

      .el-input {
        min-width: 96px;
        .el-input__inner {
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
        color: var(--deTextPrimary, #1f2329);

        i {
          cursor: pointer;
          font-size: 12px;
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
          }

          .preview-field {
            float: left;
            width: 260px;
            height: 100%;
            border-right: 1px solid rgba(31, 35, 41, 0.15);

            .field-d,
            .field-q {
              height: 200px;
              padding: 0 8px;
              overflow-y: auto;
            }
          }
        }
      }
    }
  }
}
</style>

<style lang="less">
.sql-dialog-fullscreen {
  .el-dialog__header {
    display: none;
  }
  .el-dialog__body {
    padding: 0;
  }
}
</style>
