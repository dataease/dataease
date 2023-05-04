<script lang="ts" setup>
import { ref, reactive, shallowRef, computed } from 'vue'
import { ElIcon } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { Icon } from '@/components/icon-custom'
import { useRouter } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
// import type { Tree } from './form/CreatDsGroup.vue'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import type { TabPaneName } from 'element-plus-secondary'

interface DsType {
  type: string
  name: string
  id?: string
  isPlugin: boolean
  calculationMode: string
  databaseClassification: string
  extraParams: string
  charset: null
  targetCharset: null
  isJdbc: boolean
  keywordPrefix: string
  keywordSuffix: string
  aliasPrefix: string
  aliasSuffix: string
  jdbc: boolean
  plugin: boolean
  children?: Array<{}>
}

interface Node {
  name: string
  createBy: string
  id: string
  type: string
  nodeType: string
}
const { t } = useI18n()

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
  apiConfiguration: null,
  apiConfigurationStr: null,
  calculationMode: 'DIRECT',
  configuration:
    'eyJpbml0aWFsUG9vbFNpemUiOjUsImV4dHJhUGFyYW1zIjoiIiwibWluUG9vbFNpemUiOjUsIm1heFBvb2xTaXplIjo1MCwibWF4SWRsZVRpbWUiOjMwLCJhY3F1aXJlSW5jcmVtZW50Ijo1LCJpZGxlQ29ubmVjdGlvblRlc3RQZXJpb2QiOjUsImNvbm5lY3RUaW1lb3V0Ijo1LCJjdXN0b21Ecml2ZXIiOiJkZWZhdWx0IiwicXVlcnlUaW1lb3V0IjozMCwidXNlcm5hbWUiOiJoaXZlIiwicGFzc3dvcmQiOiJoaXZlIiwiaG9zdCI6IjEyMy41Ni44LjEzMiIsImRhdGFCYXNlIjoidGVzdGRiIiwiYXV0aE1ldGhvZCI6InBhc3N3ZCIsInBvcnQiOiIxMDAwMCJ9',
  configurationEncryption: false,
  createBy: 'zyy',
  createTime: 1664447626978,
  desc: null,
  id: '0f239be3-5aa2-4a9a-9d35-7e1f510ea344',
  name: 'zyy-hive',
  privileges: null,
  status: 'Success',
  type: 'hive',
  typeDesc: 'Apache Hive',
  remark: '',
  updateTime: 1664447626978
})

const nickName = ref('')
const router = useRouter()
const userDrawer = ref(false)

state.dsTableData = [
  {
    fieldName: 'id',
    remarks: 'id',
    fieldType: 'VARCHAR',
    fieldSize: 50,
    accuracy: 0
  },
  {
    fieldName: 'name',
    remarks: 'name',
    fieldType: 'VARCHAR',
    fieldSize: 16,
    accuracy: 0
  },
  {
    fieldName: 'longitude',
    remarks: 'longitude',
    fieldType: 'VARCHAR',
    fieldSize: 50,
    accuracy: 0
  },
  {
    fieldName: 'latitude',
    remarks: 'latitude',
    fieldType: 'VARCHAR',
    fieldSize: 50,
    accuracy: 0
  },
  {
    fieldName: 'unit_price',
    remarks: 'unit_price',
    fieldType: 'VARCHAR',
    fieldSize: 50,
    accuracy: 0
  },
  {
    fieldName: 'count',
    remarks: 'count',
    fieldType: 'TINYINT',
    fieldSize: 3,
    accuracy: 0
  }
]
const selectDataset = row => {
  Object.assign(dsTableDetail, row)
  userDrawer.value = true
  let table = { dataSourceId: nodeInfo.id, info: '' }
  table.info = JSON.stringify({ table: row.name })
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
const initSearch = () => {
  handleCurrentChange(1)
  state.filterTable = tableData.value.filter(ele => ele.name.includes(nickName.value))
  state.paginationConfig.total = state.filterTable.length
}

const pagingTable = computed(() => {
  const { currentPage, pageSize } = state.paginationConfig
  return state.filterTable.slice((currentPage - 1) * pageSize, currentPage * pageSize)
})
const nodeInfo = reactive<Node>({
  name: '',
  createBy: '',
  id: '',
  nodeType: '',
  type: ''
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

const dsType: DsType[] = [
  {
    type: 'mysql',
    name: 'MySQL',
    isPlugin: false,
    calculationMode: 'DIRECT_AND_SYNC',
    databaseClassification: 'OLTP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
    charset: null,
    targetCharset: null,
    isJdbc: true,
    keywordPrefix: '`',
    keywordSuffix: '`',
    aliasPrefix: '',
    aliasSuffix: '',
    jdbc: true,
    plugin: false
  },
  {
    type: 'TiDB',
    name: 'TiDB',
    isPlugin: false,
    calculationMode: 'DIRECT_AND_SYNC',
    databaseClassification: 'OLTP',
    extraParams:
      'characterEncoding=UTF-8&connectTimeout=5000&useSSL=false&allowPublicKeyRetrieval=true',
    charset: null,
    targetCharset: null,
    isJdbc: true,
    keywordPrefix: '`',
    keywordSuffix: '`',
    aliasPrefix: '',
    aliasSuffix: '',
    jdbc: true,
    plugin: false
  },
  {
    type: 'hive',
    name: 'Apache Hive',
    isPlugin: false,
    calculationMode: 'DIRECT',
    databaseClassification: 'DL',
    extraParams: '',
    charset: null,
    targetCharset: null,
    isJdbc: true,
    keywordPrefix: '`',
    keywordSuffix: '`',
    aliasPrefix: '',
    aliasSuffix: '',
    jdbc: true,
    plugin: false
  }
]
const dsTypeMap = dsType.reduce((pre, next, index) => {
  pre[next.type] = index
  return pre
}, {})

const datasourceList = [
  {
    apiConfiguration: null,
    apiConfigurationStr: null,
    calculationMode: 'DIRECT',
    configuration:
      'eyJpbml0aWFsUG9vbFNpemUiOjUsImV4dHJhUGFyYW1zIjoiIiwibWluUG9vbFNpemUiOjUsIm1heFBvb2xTaXplIjo1MCwibWF4SWRsZVRpbWUiOjMwLCJhY3F1aXJlSW5jcmVtZW50Ijo1LCJpZGxlQ29ubmVjdGlvblRlc3RQZXJpb2QiOjUsImNvbm5lY3RUaW1lb3V0Ijo1LCJjdXN0b21Ecml2ZXIiOiJkZWZhdWx0IiwicXVlcnlUaW1lb3V0IjozMCwidXNlcm5hbWUiOiJoaXZlIiwicGFzc3dvcmQiOiJoaXZlIiwiaG9zdCI6IjEyMy41Ni44LjEzMiIsImRhdGFCYXNlIjoidGVzdGRiIiwiYXV0aE1ldGhvZCI6InBhc3N3ZCIsInBvcnQiOiIxMDAwMCJ9',
    configurationEncryption: false,
    createBy: 'zyy',
    createTime: 1664447626978,
    desc: null,
    id: '0f239be3-5aa2-4a9a-9d35-7e1f510ea344',
    name: 'zyy-hive',
    privileges: null,
    status: 'Success',
    type: 'hive',
    typeDesc: 'Apache Hive',
    updateTime: 1664447626978
  }
]

datasourceList.forEach(ele => {
  const index = dsTypeMap[ele.type]
  if (index !== undefined) {
    dsType[index].children = [...(dsType[index]?.children || []), ele]
  }
})

state.datasourceTree = dsType

const columns = shallowRef([])
const tableData = shallowRef([
  {
    datasourceId: '5f6c9416-9aac-4396-bf1d-36423c1fa62e',
    name: 'employee',
    remark: null,
    enableCheck: true,
    datasetPath: null
  },
  {
    datasourceId: '5f6c9416-9aac-4396-bf1d-36423c1fa62e',
    name: 'report_csv',
    remark: null,
    enableCheck: true,
    datasetPath: null
  }
])

const handleNodeClick = data => {
  if (data.databaseClassification) return
  const { name, createBy, id, type } = data
  Object.assign(nodeInfo, { name, createBy, id, type })
  initSearch()
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
      if (columnsPreview.length) {
        columns.value = columnsPreview
        tableData.value = dataPreview
        break
      }
      break
    case 'table':
      columns.value = allFieldsColumns
      tableData.value = allFields
      break
    default:
      break
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
        数据源
        <el-button @click="() => createDatasource()" type="primary">
          <template #icon> <Icon name="icon_add_outlined"></Icon> </template>新建数据源
        </el-button>
        <el-input class="m24 w100" v-model="nickName" clearable>
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
              <el-icon class="hover-icon">
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
          <el-tab-pane label="数据源配置" name="config"></el-tab-pane>
          <el-tab-pane label="数据源表" name="table"></el-tab-pane>
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
                :placeholder="t('system_parameter_setting.search_keywords')"
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
            <el-table-column key="name" prop="name" :label="t('datasource.table_name')" />
            <el-table-column key="description" prop="description" label="备注" />
            <el-table-column
              key="__operation"
              :label="t('commons.operating')"
              fixed="right"
              width="108"
            >
              <template #default="scope">
                <el-button text @click="selectDataset(scope.row)"
                  >{{ t('dataset.detail') }}
                </el-button>
              </template>
            </el-table-column>
          </grid-table>
        </div>
        <div v-else class="form-editor"></div>
      </template>
      <template v-else>
        <empty-background description="请在左侧选择数据源" img-type="select" />
      </template>
    </div>
    <el-dialog
      :title="t('dataset.detail')"
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
            {{ dsTableDetail.name }}
          </p>
        </el-col>
        <el-col :span="12">
          <p class="table-name">
            {{ t('datasource.table_description') }}
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
        <el-table-column prop="fieldName" :label="t('panel.column_name')" />
        <el-table-column prop="fieldType" :label="t('dataset.field_type')">
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
