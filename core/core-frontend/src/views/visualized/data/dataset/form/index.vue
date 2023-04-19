<script lang="ts" setup>
import { ref, nextTick, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import CalcFieldEdit from './CalcFieldEdit.vue'
import AddSql from './AddSql.vue'
import { useRoute } from 'vue-router'
import UnionEdit from './UnionEdit.vue'
import CreatDsGroup from './CreatDsGroup.vue'
import { getDatasourceList, getTables, getPreviewData } from '@/api/dataset'
import type { Table } from '@/api/dataset'
import DatasetUnion from './DatasetUnion.vue'
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
}

const { t } = useI18n()
const route = useRoute()
const creatDsFolder = ref()
const isCreatGroup = ref(false)
const editCalcField = ref(false)
const editSqlField = ref(false)
const editUnion = ref(false)

const datasetDrag = ref()
const datasetName = ref('新建数据源')
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

// onMounted(() => {
//   const { pid } = route.query
//   creatDsFolder.value.init('dataset', { id: pid })
// })
const joinEditor = (arr: []) => {
  state.editArr = arr
  editUnion.value = true
}

let columns = []
let tableData = []

const showTable = ref(false)

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

const closeEditUnion = () => {
  notConfirmEditUnion()
  editUnion.value = false
}
const fieldUnion = ref()
const confirmEditUnion = () => {
  const { node, parent } = fieldUnion.value
  datasetDrag.value.setStateBack(node, parent)
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
  document.querySelector('.dataset-db').addEventListener('mousemove', calculateHeight)
}
const mouseupDrag = () => {
  document.querySelector('.dataset-db').removeEventListener('mousemove', calculateHeight)
}
const calculateHeight = (e: MouseEvent) => {
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
  const arr = []
  dfsNodeList(arr, datasetDrag.value.nodeList)
  getPreviewData({ union: arr }).then(res => {
    columns = (res.data.fields as unknown as Field[]).map(ele => {
      return {
        key: ele.fieldShortName,
        dataKey: ele.fieldShortName,
        title: ele.name,
        width: 150
      }
    })
    tableData = (res.data.data as unknown as []) || []
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
          :offsetX="offsetX"
          :offsetY="offsetY"
          ref="datasetDrag"
          @addComplete="addComplete"
        ></dataset-union>
        <div v-if="showTable" class="table-preview">
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
      .table-preview {
        height: calc(100vh - 500px);
        position: fixed;
        width: 907px;
        bottom: 0;
        right: 0;
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
