<script lang="tsx" setup>
import { computed, reactive, ref, shallowRef, nextTick } from 'vue'
import type { TabPaneName, Action } from 'element-plus-secondary'
import { ElIcon, ElMessageBox, ElMessage } from 'element-plus-secondary'
import GridTable from '@/components/grid-table/src/GridTable.vue'
import { HandleMore } from '@/components/handle-more'
import { Icon } from '@/components/icon-custom'
import CreatDsGroup from './form/CreatDsGroup.vue'
import type { Tree } from '../dataset/form/CreatDsGroup.vue'
import { getDatasetPreview } from '@/api/dataset'
import { useI18n } from '@/hooks/web/useI18n'
import EmptyBackground from '@/components/empty-background/src/EmptyBackground.vue'
import {
  listDatasources,
  getTableField,
  listDatasourceTables,
  deleteById,
  save
} from '@/api/datasource'
import { Base64 } from 'js-base64'
import type { Configuration, ApiConfiguration, SyncSetting } from './form/index.vue'
import EditorDatasource from './form/index.vue'
import ExcelInfo from './ExcelInfo.vue'
import SheetTabs from './SheetTabs.vue'
import BaseInfoItem from './BaseInfoItem.vue'
import BaseInfoContent from './BaseInfoContent.vue'
interface DsType {
  type: string
  name: string
  leaf: boolean
  id?: string
  catalog: string
  extraParams: string
  children?: Array<{}>
}

interface Field {
  fieldShortName: string
  name: string
  dataeaseName: string
  originName: string
  deType: number
}

export interface Node {
  name: string
  createBy: string
  id: number
  description: string
  type: string
  nodeType: string
  syncSetting?: SyncSetting
  editType?: number
  configuration?: Configuration
  apiConfiguration?: ApiConfiguration[]
}

const { t } = useI18n()

const state = reactive({
  datasourceTree: [] as DsType[],
  dsTableData: [],
  paginationConfig: {
    currentPage: 1,
    pageSize: 10,
    total: 0
  },
  filterTable: []
})

const dsTableDetail = reactive({
  tableName: '',
  remark: ''
})
const nickName = ref('')
const dsName = ref('')
const userDrawer = ref(false)
const rawDatasourceList = ref([])
const showPriority = ref(false)
const datasourceEditor = ref()
const menuList = [
  {
    label: '移动到',
    svgName: 'icon_into-item_outlined',
    command: 'move'
  },
  {
    label: '重命名',
    svgName: 'icon_rename_outlined',
    command: 'rename'
  },
  {
    label: '删除',
    divided: true,
    svgName: 'icon_delete-trash_outlined',
    command: 'delete'
  }
]

const datasetTypeList = [
  {
    label: '新建数据源',
    svgName: 'icon_dataset',
    command: 'datasource'
  },
  {
    label: '新建文件夹',
    divided: true,
    svgName: 'dv-folder',
    command: 'folder'
  }
]
const selectDataset = row => {
  Object.assign(dsTableDetail, row)
  userDrawer.value = true
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
const fieldMap = ['text', 'time', 'value', 'value', 'location']

const fieldType = (deType: number) => {
  return fieldMap[deType]
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
        <ElIcon style={{ marginRight: '6px' }}>
          <Icon
            name={`field_${fieldType(column.deType)}`}
            className={`field-icon-${fieldType(column.deType)}`}
          ></Icon>
        </ElIcon>
        {column.title}
      </div>
    )
  }))

const dataPreviewLoading = ref(false)
const columns = ref([])
const handleLoadExcel = () => {
  dataPreviewLoading.value = true
  getDatasetPreview('1679405733190893568')
    .then(res => {
      columns.value = generateColumns((res?.data?.fields as Field[]) || [])
      tableData.value = (res?.data?.data as Array<{}>) || []
    })
    .finally(() => {
      dataPreviewLoading.value = false
    })
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

const getDsIconName = data => {
  if (!data.leaf) return 'dv-folder'
  return 'mysql-frame'
}

const handleTabClick = tab => {
  handleLoadExcel()
  console.log('tab', tab)
}

const baseInfo = ref()

const tabList = shallowRef([])

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
  description: '',
  id: 0,
  nodeType: '',
  type: '',
  configuration: null,
  syncSetting: null,
  apiConfiguration: []
})

const saveDsFolder = (params, cmd) => {
  save(params).then(res => {
    listDs()
  })
}

const listDs = () => {
  rawDatasourceList.value = []
  listDatasources({}).then(array => {
    convertConfig(array)
    state.datasourceTree = array
  })
}

const convertConfig = array => {
  for (let index = 0; index < array.length; index++) {
    if (array[index].leaf) {
      if (array[index].configuration) {
        array[index].configuration = JSON.parse(Base64.decode(array[index].configuration))
      }
      if (array[index].apiConfigurationStr) {
        array[index].apiConfiguration = JSON.parse(Base64.decode(array[index].apiConfigurationStr))
      }
    } else if (array[index].children && array[index].children.length > 0) {
      convertConfig(array[index].children)
    }
  }
}

listDs()

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
const creatDsFolder = ref()

const tableData = shallowRef([])
const handleNodeClick = data => {
  if (!data.leaf) return
  const { name, createBy, id, type, configuration, syncSetting, apiConfiguration, description } =
    data
  Object.assign(nodeInfo, {
    name,
    description,
    createBy,
    id,
    type,
    configuration,
    syncSetting,
    apiConfiguration
  })
  activeName.value = 'config'
  handleCurrentChange(1)
  handleClick(activeName.value)
  nextTick(() => {
    baseInfo.value.active = true
  })
}
const createDatasource = (data?: Tree) => {
  datasourceEditor.value.init(null, data?.id)
}

const editDatasource = (editType?: number) => {
  if (nodeInfo.type === 'Excel') {
    nodeInfo.editType = editType
  }
  datasourceEditor.value.init(nodeInfo)
}

const rateValueMap = {
  SIMPLE: t('dataset.execute_once'),
  CRON: t('dataset.cron_config'),
  SIMPLE_CRON: t('dataset.simple_cron')
}

const handleDatasourceTree = (cmd: string, data?: Tree) => {
  if (cmd === 'datasource') {
    createDatasource(data)
  }
  if (cmd === 'folder') {
    creatDsFolder.value.createInit(cmd, data || {})
  }
}
const operation = (cmd: string, data: Tree, nodeType: string) => {
  if (cmd === 'delete') {
    ElMessageBox.confirm(
      nodeType === 'folder' ? '确定删除该文件夹吗' : t('datasource.this_data_source'),
      {
        confirmButtonText: t('common.sure'),
        cancelButtonText: t('common.cancel'),
        confirmButtonType: 'danger',
        type: 'warning',
        autofocus: false,
        showClose: false,
        callback: (action: Action) => {
          if (action === 'confirm') {
            deleteById(data.id as number).then(() => {
              listDs()
              ElMessage.success(t('dataset.delete_success'))
            })
          }
        }
      }
    )
  } else {
    creatDsFolder.value.createInit(nodeType, data, cmd)
  }
}
const handleClick = (tabName: TabPaneName) => {
  switch (tabName) {
    case 'config':
      break
    case 'table':
      listDatasourceTables(nodeInfo.id).then(res => {
        tableData.value = res.data
        initSearch()
      })
      break
    default:
      break
  }
}
const activeName = ref('table')
const defaultProps = {
  children: 'children',
  label: 'name'
}
</script>

<template>
  <div class="datasource-manage">
    <div class="datasource-list datasource-height">
      <div class="filter-datasource">
        <div class="icon-methods">
          <span class="title"> {{ t('datasource.datasource') }} </span>
          <el-tooltip effect="dark" content="新建文件夹" placement="top">
            <el-button @click="() => handleDatasourceTree('folder')" text>
              <template #icon>
                <Icon name="dv-new-folder"></Icon>
              </template>
            </el-button>
          </el-tooltip>
          <el-tooltip effect="dark" :content="t('datasource.create')" placement="top">
            <el-button @click="() => createDatasource()" text>
              <template #icon>
                <Icon name="icon_dataset_outlined"></Icon>
              </template>
            </el-button>
          </el-tooltip>
        </div>

        <div class="search-input">
          <el-input class="w100" v-model="dsName" clearable @blur="searchDs" @clear="searchDs">
            <template #prefix>
              <el-icon>
                <Icon name="icon_search-outline_outlined"></Icon>
              </el-icon>
            </template>
          </el-input>
        </div>
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
            <el-icon :class="data.leaf && 'icon-border'" style="width: 18px; height: 18px">
              <Icon :name="getDsIconName(data)"></Icon>
            </el-icon>
            <span :title="node.label" class="label-tooltip">{{ node.label }}</span>
            <div class="icon-more">
              <handle-more
                @handle-command="cmd => handleDatasourceTree(cmd, data)"
                :menu-list="datasetTypeList"
                icon-name="icon_add_outlined"
                placement="bottom-start"
                v-if="!data.leaf"
              ></handle-more>
              <handle-more
                @handle-command="cmd => operation(cmd, data, data.leaf ? 'datasource' : 'folder')"
                :menu-list="menuList"
              ></handle-more>
            </div>
          </span>
        </template>
      </el-tree>
    </div>
    <div class="datasource-content">
      <template v-if="!state.datasourceTree.length">
        <empty-background description="暂无数据源" img-type="none">
          <el-button @click="() => createDatasource()" type="primary">
            <template #icon>
              <Icon name="icon_add_outlined"></Icon>
            </template>
            {{ t('datasource.create') }}</el-button
          >
        </empty-background>
      </template>
      <template v-else-if="!!nodeInfo.id">
        <div class="datasource-info">
          <div class="info-method">
            <el-icon class="icon-border">
              <Icon name="mysql-frame"></Icon>
            </el-icon>
            <span class="name">
              {{ nodeInfo.name }}
            </span>
            <el-popover placement="bottom" width="420" trigger="hover">
              <template #reference>
                <el-icon class="create-user">
                  <Icon name="icon_info_outlined"></Icon>
                </el-icon>
              </template>
              123
            </el-popover>
            <div class="right-btn">
              <el-button secondary>
                <template #icon>
                  <Icon name="icon_dataset_outlined"></Icon>
                </template>
                新建数据集
              </el-button>

              <template v-if="nodeInfo.type === 'Excel'">
                <el-button @click="editDatasource(0)" type="primary">
                  <template #icon>
                    <Icon name="icon_edit_outlined"></Icon>
                  </template>
                  替换数据
                </el-button>
                <el-button @click="editDatasource(1)" type="primary">
                  <template #icon>
                    <Icon name="icon_edit_outlined"></Icon>
                  </template>
                  追加数据
                </el-button>
              </template>
              <el-button v-else @click="editDatasource()" type="primary">
                <template #icon>
                  <Icon name="icon_edit_outlined"></Icon>
                </template>
                编辑
              </el-button>
            </div>
          </div>
          <div class="tab-border">
            <el-tabs v-model="activeName" @tab-change="handleClick">
              <el-tab-pane :label="t('datasource.config')" name="config"></el-tab-pane>
              <el-tab-pane :label="t('datasource.table')" name="table"></el-tab-pane>
            </el-tabs>
          </div>
        </div>
        <div v-if="activeName === 'table'" class="datasource-table">
          <div class="search-operate">
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
          </div>
          <div class="info-table">
            <grid-table
              :pagination="state.paginationConfig"
              :table-data="pagingTable"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
            >
              <el-table-column
                key="tableName"
                prop="tableName"
                :label="t('datasource.table_name')"
              />
              <el-table-column
                key="__operation"
                :label="t('common.operating')"
                fixed="right"
                width="108"
              >
                <template #default="scope">
                  <el-tooltip effect="dark" content="新建数据集" placement="top">
                    <el-button @click.stop text>
                      <template #icon>
                        <Icon name="icon_dataset_outlined"></Icon>
                      </template>
                    </el-button>
                  </el-tooltip>
                  <el-tooltip effect="dark" :content="t('visualization.details')" placement="top">
                    <el-button @click.stop="selectDataset(scope.row)" text>
                      <template #icon>
                        <Icon name="icon_describe_outlined"></Icon>
                      </template>
                    </el-button>
                  </el-tooltip>
                </template>
              </el-table-column>
            </grid-table>
          </div>
        </div>
        <template v-else>
          <BaseInfoContent ref="baseInfo" v-slot="slotProps" :name="t('datasource.base_info')">
            <template v-if="slotProps.active">
              <el-row :gutter="24">
                <el-col :span="12">
                  <BaseInfoItem :label="t('common.name') + t('auth.datasource')">{{
                    nodeInfo.name
                  }}</BaseInfoItem>
                </el-col>
                <el-col :span="12">
                  <BaseInfoItem :label="t('datasource.type')">{{ nodeInfo.type }}</BaseInfoItem>
                </el-col>
              </el-row>
              <el-row>
                <el-col v-if="nodeInfo.type === 'Excel'" :span="12">
                  <BaseInfoItem label="文件">
                    <ExcelInfo :name="nodeInfo.type" :size="nodeInfo.type"></ExcelInfo>
                  </BaseInfoItem>
                </el-col>
                <el-col v-else :span="24">
                  <BaseInfoItem :label="t('common.description')">{{
                    nodeInfo.description
                  }}</BaseInfoItem>
                </el-col>
              </el-row>
              <template v-if="!['Excel', 'Api'].includes(nodeInfo.type)">
                <el-row :gutter="24">
                  <!-- <el-col :span="12">
                  <BaseInfoItem label="驱动">驱动</BaseInfoItem>
                </el-col> -->
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.host')">{{
                      nodeInfo.configuration.host
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.port')">{{
                      nodeInfo.configuration.port
                    }}</BaseInfoItem>
                  </el-col>
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.data_base')">{{
                      nodeInfo.configuration.dataBase
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <el-row :gutter="24">
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.user_name')">{{
                      nodeInfo.configuration.username
                    }}</BaseInfoItem>
                  </el-col>
                  <el-col :span="12">
                    <BaseInfoItem :label="t('datasource.password')">{{
                      nodeInfo.configuration.password
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <el-row>
                  <el-col :span="24">
                    <BaseInfoItem :label="t('datasource.extra_params')">{{
                      nodeInfo.configuration.extraParams
                    }}</BaseInfoItem>
                  </el-col>
                </el-row>
                <span
                  v-if="!['es', 'api', 'mongo'].includes(nodeInfo.type.toLowerCase())"
                  class="de-expand"
                  @click="showPriority = !showPriority"
                  >{{ t('datasource.priority') }}
                  <el-icon>
                    <Icon
                      :name="showPriority ? 'icon_down_outlined' : 'icon_down_outlined-1'"
                    ></Icon>
                  </el-icon>
                </span>
                <template v-if="showPriority">
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.initial_pool_size')">{{
                        nodeInfo.configuration.initialPoolSize
                      }}</BaseInfoItem>
                    </el-col>
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.min_pool_size')">{{
                        nodeInfo.configuration.minPoolSize
                      }}</BaseInfoItem>
                    </el-col>
                  </el-row>
                  <el-row :gutter="24">
                    <el-col :span="12">
                      <BaseInfoItem :label="t('datasource.max_pool_size')">{{
                        nodeInfo.configuration.maxPoolSize
                      }}</BaseInfoItem>
                    </el-col>
                    <el-col :span="12">
                      <BaseInfoItem
                        :value="nodeInfo.configuration.queryTimeout"
                        :label="t('datasource.query_timeout')"
                        >{{ nodeInfo.configuration.queryTimeout }}</BaseInfoItem
                      >
                    </el-col>
                  </el-row>
                </template>
              </template>
            </template>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'API'"
            v-slot="slotProps"
            :name="t('datasource.data_table')"
          >
            <div class="api-card-content" v-if="slotProps.active">
              <div v-for="api in nodeInfo.apiConfiguration" :key="api.id" class="api-card">
                <el-row>
                  <el-col :span="19">
                    <span class="name ellipsis">{{ api.name }}</span>
                    <span v-if="api.status === 'Error'" class="de-tag error-color">{{
                      t('datasource.invalid')
                    }}</span>
                    <span v-if="api.status === 'Success'" class="de-tag success-color">{{
                      t('datasource.valid')
                    }}</span>
                  </el-col>
                </el-row>
                <div class="req-title">
                  <span>{{ t('datasource.method') }}</span>
                  <span>{{ t('datasource.url') }}</span>
                </div>
                <div class="req-value">
                  <span>{{ api.method }}</span>
                  <span :title="api.url">{{ api.url }}</span>
                </div>
              </div>
            </div>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'API'"
            v-slot="slotProps"
            :name="t('dataset.update_setting')"
          >
            <template v-if="slotProps.active">
              <el-row :gutter="24">
                <el-col :span="12">
                  <BaseInfoItem :label="t('dataset.update_type')">{{
                    t(`dataset.${nodeInfo.syncSetting.updateType}`)
                  }}</BaseInfoItem>
                </el-col>
                <el-col :span="12">
                  <BaseInfoItem :label="t('dataset.execute_rate')">{{
                    rateValueMap[nodeInfo.syncSetting.syncRate]
                  }}</BaseInfoItem>
                </el-col>
              </el-row>
            </template>
          </BaseInfoContent>
          <BaseInfoContent
            v-if="nodeInfo.type === 'Excel'"
            v-slot="slotProps"
            :name="t('dataset.data_preview')"
          >
            <template v-if="slotProps.active">
              <div class="excel-table">
                <SheetTabs @tab-click="handleTabClick" :tab-list="tabList"></SheetTabs>
                <div class="sheet-table-content">
                  <el-auto-resizer>
                    <template #default="{ height, width }">
                      <el-table-v2
                        :columns="columns"
                        v-loading="dataPreviewLoading"
                        header-class="excel-header-cell"
                        :data="tableData"
                        :width="width"
                        :height="height"
                        fixed
                        ><template #empty>
                          <empty-background
                            description="暂无数据"
                            img-type="noneWhite"
                          /> </template
                      ></el-table-v2>
                    </template>
                  </el-auto-resizer>
                </div>
              </div>
            </template>
          </BaseInfoContent>
        </template>
      </template>
      <template v-else>
        <empty-background :description="t('datasource.select_ds')" img-type="select" />
      </template>
    </div>
    <EditorDatasource ref="datasourceEditor"></EditorDatasource>
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
        <el-table-column prop="originName" :label="t('datasource.column_name')" />
        <el-table-column prop="type" :label="t('datasource.field_type')">
          <template #default="scope">
            <span v-if="nodeInfo.type !== 'api'">
              {{ scope.row.type }}
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
    <creat-ds-group @finish="saveDsFolder" ref="creatDsFolder"></creat-ds-group>
  </div>
</template>

<style lang="less" scoped>
@import '@/style/mixin.less';

.datasource-manage {
  display: flex;
  width: 100%;
  height: 100%;
  background: #fff;

  .icon-border {
    padding: 3px;
    border: 1px solid #dee0e3;
    border-radius: 3px;
    width: 24px;
    height: 24px;
  }

  .excel-table {
    margin-top: 16px;

    .sheet-table-content {
      height: 400px;
    }
  }

  .api-card-content {
    display: flex;
    flex-wrap: wrap;
    margin-top: 16px;
    margin-left: -16px;
  }

  .api-card {
    width: 528px;
    height: 120px;
    border-radius: 4px;
    border: 1px solid var(--deCardStrokeColor, #dee0e3);
    border-radius: 4px;
    margin: 0 0 16px 16px;
    padding: 16px;
    font-family: PingFang SC;
    .name {
      font-size: 16px;
      font-weight: 500;
      margin-right: 8px;
      max-width: 80%;
    }
    .req-title,
    .req-value {
      display: flex;
      font-size: 14px;
      font-weight: 400;
      :nth-child(1) {
        width: 100px;
      }

      :nth-child(2) {
        margin-left: 24px;
        max-width: 200px;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
    .req-title {
      color: var(--deTextPrimary, #1f2329);
      margin: 16px 0 4px 0;
    }
    .req-value {
      color: var(--deTextSecondary, #646a73);
    }
    .de-copy-icon {
      cursor: pointer;
      margin-right: 20px;
      color: var(--deTextSecondary, #646a73);
    }
    .de-delete-icon:not(.not-allow) {
      cursor: pointer;
      &:hover {
        color: var(--deDanger, #f54a45);
      }
    }
    .de-tag {
      display: inline-flex;
      justify-content: center;
      align-items: center;
      border-radius: 2px;
      padding: 1px 6px;
      height: 24px;
    }

    .error-color {
      color: #646a73;
      background-color: rgba(31, 35, 41, 10%);
    }
    .success-color {
      color: green;
      background-color: rgba(52, 199, 36, 20%);
    }
  }

  .de-expand {
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    line-height: 22px;
    color: #3370ff;
    cursor: pointer;
    margin-top: 16px;
    display: inline-flex;
    align-items: center;

    .ed-icon {
      margin-left: 4px;
    }
  }

  .datasource-height,
  .datasource-content {
    height: calc(100vh - 56px);
    overflow: auto;
    position: relative;
  }

  .datasource-list {
    width: 269px;
    padding: 16px;
  }

  .datasource-content {
    background: #f5f6f7;
    overflow-y: auto;
  }

  .filter-datasource {
    position: sticky;
    top: 0;
    left: 16px;
    z-index: 5;
    background: white;
    &::before {
      content: '';
      width: 100%;
      height: 16px;
      top: -16px;
      position: absolute;
      z-index: 5;
      left: 0;
      background: white;
    }

    .icon-methods {
      display: flex;
      align-items: center;
      justify-content: flex-end;
      font-family: PingFang SC;
      font-size: 20px;
      font-weight: 500;
      color: var(--TextPrimary, #1f2329);

      .title {
        margin-right: auto;
      }
      .ed-button.is-text {
        line-height: 28px;
        font-size: 20px;
        height: 28px;
        width: 28px;
      }
    }

    .search-input {
      margin: 16px 8px 8px 8px;
      display: flex;
      justify-content: space-between;
      .ed-input {
        flex: 1;
      }

      .filter-button {
        width: 32px;
        height: 32px;
        margin-left: 8px;
        border: 1px solid #bbbfc4;
        border-radius: 4px;
        line-height: 32px;
        text-align: center;
        cursor: pointer;
      }
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
    border-left: 1px solid rgba(31, 35, 41, 0.15);
    position: relative;

    .datasource-info {
      background: #fff;
      padding: 0 24px;
      padding-top: 12px;
      height: 90px;
      position: sticky;
      top: 0;
      z-index: 6;
      .info-method {
        width: 100%;
        display: flex;
        align-items: center;
        font-family: PingFang SC;
        font-size: 16px;
        font-weight: 500;

        .name {
          margin: 0 8px;
        }

        .create-user {
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          color: #646a73;
          margin-right: 4px;
        }

        .mr8 {
          margin-left: 8px;
        }

        .right-btn {
          margin-left: auto;
        }
      }
      .tab-border {
        .border-bottom-tab(24px);
        :deep(.ed-tabs__nav-wrap::after) {
          border-color: rgba(31, 35, 41, 0.15);
        }
        margin-left: 0;
      }
    }

    .datasource-table {
      padding: 24px;
      margin: 24px;
      background: #fff;
      height: calc(100vh - 190px);

      .search-operate {
        width: 280px;
        margin-bottom: 16px;
      }
    }

    .info-table {
      height: calc(100% - 49px);
    }
  }
}
</style>

<style scoped lang="less">
.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  padding-right: 4px;
  box-sizing: content-box;

  .label-tooltip {
    width: calc(100% - 66px);
    margin-left: 8.75px;
    overflow: hidden;
    white-space: nowrap;
    text-overflow: ellipsis;
  }

  .icon-more {
    margin-left: auto;
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
