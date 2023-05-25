<script lang="ts" setup>
import { computed, reactive, ref, shallowRef } from 'vue'
import type { TabPaneName } from 'element-plus-secondary'
import { ElIcon } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { Icon } from '@/components/icon-custom'
import { useRouter } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import { listDatasources, getTableField, listDatasourceTables } from '../../../../api/datasource.ts'
import { Base64 } from 'js-base64'
import { Configuration, ApiConfiguration, SyncSetting } from './form/index.vue'
import EditorDetail from './form/EditorDetail.vue'
interface DsType {
  type: string
  name: string
  id?: string
  catalog: string
  extraParams: string
  children?: Array<{}>
}

interface Node {
  name: string
  createBy: string
  id: number
  type: string
  nodeType: string
  syncSetting?: SyncSetting

  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
}

const { t } = useI18n()

const detail = ref()

const state = reactive({
  addeddatasourceList: [],
  datasourceTree: [] as DsType[],
  datasourceTypeList: [],
  dsTableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  filterTable: []
})

const dsTableDetail = reactive({
  tableName: ''
})
const True = ref(true)
const nickName = ref('')
const dsName = ref('')
const router = useRouter()
const userDrawer = ref(false)
const rawDatasourceList = ref([])
const selectDataset = row => {
  Object.assign(dsTableDetail, row)
  userDrawer.value = true
  let table = { dataSourceId: nodeInfo.id, info: '' }
  table.info = JSON.stringify({ table: row.name })
  getTableField(nodeInfo.id, row.tableName).then(res => {
    state.dsTableData = res.data
  })
}
const handleSizeChange = pageSize => {
  state.paginationConfig.currentPage = 1
  state.paginationConfig.pageSize = pageSize
}
const handleCurrentChange = currentPage => {
  state.paginationConfig.currentPage = currentPage
}

const getFieldType = (fieldType: string | number) => {
  return [
    t('dataset.text'),
    '',
    t('dataset.value'),
    t('dataset.value') + '(' + t('dataset.float') + ')'
  ][fieldType]
}

const searchDs = () => {
  buildTree(rawDatasourceList.value.filter(ele => ele.name.includes(dsName.value)))
}

const initSearch = () => {
  handleCurrentChange(1)
  state.filterTable = tableData.value.filter(ele => ele.tableName.includes(nickName.value))
  state.paginationConfig.total = state.filterTable.length
}

const pagingTable = computed(() => {
  const { currentPage, pageSize } = state.paginationConfig
  return state.filterTable.slice((currentPage - 1) * pageSize, currentPage * pageSize)
})
const nodeInfo = reactive<Node>({
  name: '',
  createBy: '',
  id: 0,
  nodeType: '',
  type: '',
  configuration: {},
  syncSetting: {},
  apiConfiguration: []
})

let allFields = []
let columnsPreview = []
let dataPreview = []

const allFieldsColumns = [
  {
    key: 'name',
    dataKey: 'name',
    title: '字段名称',
    width: 150
  },
  {
    key: 'deType',
    dataKey: 'deType',
    title: '字段类型',
    width: 150
  },
  {
    key: 'description',
    dataKey: 'description',
    title: '备注',
    width: 150
  }
]

listDatasources().then(array => {
  for (let index = 0; index < array.length; index++) {
    const element = array[index]
    if (element.configuration) {
      element.configuration = JSON.parse(Base64.decode(element.configuration))
    }
    if (element.apiConfigurationStr) {
      element.apiConfiguration = JSON.parse(Base64.decode(element.apiConfigurationStr))
    }
    rawDatasourceList.value.push(element)
  }
  buildTree(rawDatasourceList.value)
})

const buildTree = array => {
  const types = {}
  const dsType = []
  for (let index = 0; index < array.length; index++) {
    const element = array[index]
    if (!(element.type in types)) {
      types[element.type] = []
      dsType.push({
        type: element.type,
        databaseClassification: element.catalog,
        name: element.typeAlias
      })
    }
    types[element.type].push(element)
  }
  const dsTypeMap = dsType.reduce((pre, next, index) => {
    pre[next.type] = index
    return pre
  }, {})
  array.forEach(ele => {
    const index = dsTypeMap[ele.type]
    if (index !== undefined) {
      dsType[index].children = [...(dsType[index]?.children || []), ele]
    }
  })
  state.datasourceTree = dsType
}

const columns = shallowRef([])
const tableData = shallowRef([])
const handleNodeClick = data => {
  if (data.databaseClassification) return
  const { name, createBy, id, type, configuration, syncSetting, apiConfiguration } = data
  Object.assign(nodeInfo, {
    name,
    createBy,
    id,
    type,
    configuration,
    syncSetting,
    apiConfiguration
  })
  initSearch()
  if (activeName.value === 'config') {
    detail.value.initEditForm()
  }
  handleClick(activeName.value)
}
const createDatasource = (data?: DsType) => {
  router.push({
    path: '/ds-form',
    query: {}
  })
}
const handleClick = (tabName: TabPaneName) => {
  switch (tabName) {
    case 'config':
      break
    case 'table':
      listDatasourceTables(nodeInfo.id).then(res => {
        tableData.value = res.data
      })
      columns.value = allFieldsColumns
      break
    default:
      break
  }
  if (activeName.value === 'config') {
    detail.value.initEditForm()
  }
}
const activeName = ref('table')
state.addeddatasourceList = Array(40)
  .fill(1)
  .map((_, index) => ({
    datasourcename: 'test' + index,
    nickName: index + 'nickName'
  }))

const defaultProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="datasource-manage">
    <div class="datasource-list datasource-height">
      <div class="title">
        {{ t('datasource.datasource') }}
        <el-button @click="() => createDatasource()" type="primary">
          <template #icon>
            <Icon name="icon_add_outlined"></Icon>
          </template>
          {{ t('datasource.create') }}
        </el-button>
        <el-input class="m24 w100" v-model="dsName" clearable @blur="searchDs" @clear="searchDs">
          <template #prefix>
            <el-icon>
              <Icon name="icon_search-outline_outlined"></Icon>
            </el-icon>
          </template>
        </el-input>
      </div>

      <el-tree
        :expand-on-click-node="false"
        menu
        :data="state.datasourceTree"
        :props="defaultProps"
        @node-click="handleNodeClick"
      >
        <template #default="{ node, data }">
          <span class="custom-tree-node">
            <el-icon v-if="data.databaseClassification">
              <Icon name="scene"></Icon>
            </el-icon>
            <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
            <div>
              <el-icon>
                <Icon name="icon_add_outlined"></Icon>
              </el-icon>
            </div>
          </span>
        </template>
      </el-tree>
    </div>
    <div class="datasource-content">
      <template v-if="!!nodeInfo.id">
        <el-tabs v-model="activeName" @tab-change="handleClick">
          <el-tab-pane :label="t('datasource.config')" name="config"></el-tab-pane>
          <el-tab-pane :label="t('datasource.table')" name="table"></el-tab-pane>
        </el-tabs>
        <div class="info-table de-search-table" v-if="activeName === 'table'">
          <el-row class="top-operate">
            <el-col :span="10">
              <span class="table-name-top">{{ nodeInfo.name }}</span>
            </el-col>
            <el-col :span="14" class="right-user">
              <el-input
                ref="search"
                v-model="nickName"
                :placeholder="t('common.search_keywords')"
                clearable
                style="width: 240px"
                @blur="initSearch"
                @clear="initSearch"
              >
                <template #prefix>
                  <el-icon>
                    <Icon name="icon_search-outline_outlined"></Icon>
                  </el-icon>
                </template>
              </el-input>
            </el-col>
          </el-row>
          <grid-table
            :pagination="state.paginationConfig"
            :table-data="pagingTable"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          >
            <el-table-column key="tableName" prop="tableName" :label="t('datasource.table_name')" />
            <el-table-column key="description" prop="description" :label="t('common.label')" />
            <el-table-column
              key="__operation"
              :label="t('common.operating')"
              fixed="right"
              width="108"
            >
              <template #default="scope">
                <el-button text @click="selectDataset(scope.row)"
                  >{{ t('common.detail') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
        <div v-else class="form-editor">
          <editor-detail
            ref="detail"
            :form="nodeInfo"
            :ds-form-disabled="True"
            :edit-ds="True"
          ></editor-detail>
        </div>
      </template>
      <template v-else>
        <empty-background :description="t('datasource.select_ds')" img-type="select" />
      </template>
    </div>
    <el-dialog
      :title="t('common.detail')"
      v-model="userDrawer"
      class="ds-table-drawer"
      width="840px"
    >
      <el-row :gutter="24">
        <el-col :span="12">
          <p class="table-name">
            {{ t('datasource.table_name') }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.tableName }}
          </p>
        </el-col>
        <el-col :span="12">
          <p class="table-name">
            {{ t('datasource.remark') }}
          </p>
          <p class="table-value">
            {{ dsTableDetail.remark || '-' }}
          </p>
        </el-col>
      </el-row>
      <el-table
        header-cell-class-name="header-cell"
        :data="state.dsTableData"
        stripe
        style="width: 100%"
      >
        <el-table-column prop="fieldName" :label="t('datasource.column_name')" />
        <el-table-column prop="fieldType" :label="t('datasource.field_type')">
          <template #default="scope">
            <span v-if="nodeInfo.type !== 'api'">
              {{ scope.row.fieldType }}
            </span>
            <span v-if="nodeInfo.type === 'api'">{{ getFieldType(scope.row.fieldType) }}</span>
          </template>
        </el-table-column>
        <el-table-column
          prop="remarks"
          show-overflow-tooltip
          :label="t('datasource.field_description')"
        />
      </el-table>
    </el-dialog>
  </div>
</template>

<style lang="less" scoped>
.datasource-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;

  .datasource-height,
  .datasource-content {
    height: calc(100vh - 110px);
    overflow: auto;
    position: relative;
  }

  .datasource-list {
    width: 269px;
    padding: 24px;
  }

  .title {
    display: flex;
    justify-content: space-between;
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    color: var(--TextPrimary, #1f2329);
    box-sizing: border-box;
    flex-wrap: wrap;
    position: sticky;
    top: 0;
    left: 24px;
    z-index: 5;
    background: white;

    &::before {
      content: '';
      width: 100%;
      height: 24px;
      top: -24px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }
  }

  .m24 {
    margin: 24px 0;
  }

  .w100 {
    width: 100%;
  }

  .datasource-content {
    flex: 1;
    padding: 24px;
    padding-top: 8px;
    border-left: 1px solid rgba(31, 35, 41, 0.15);
    position: relative;

    .info-table {
      padding-top: 12px;

      .right-user {
        text-align: right;
      }

      .table-name-top {
        font-family: PingFang SC;
        font-size: 16px;
        font-weight: 500;
        line-height: 24px;
        color: var(--deTextPrimary, #1f2329);
      }

      height: calc(100% - 100px);
    }
  }
}
</style>

<style lang="less">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 8px;
  box-sizing: content-box;

  .label-tooltip {
    width: 60%;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }
}
</style>
<style lang="less">
.ds-table-drawer {
  .table-value,
  .table-name {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    margin: 0;
  }

  .table-name {
    color: var(--deTextSecondary, #646a73);
  }

  .table-value {
    margin: 4px 0 24px 0;
    color: var(--deTextPrimary, #1f2329);
  }
}
</style>
