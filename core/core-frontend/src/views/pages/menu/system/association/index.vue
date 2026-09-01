<script lang="ts" setup>
import icon_database from '@/assets/svg/icon_database.svg'
import icon_dataset from '@/assets/svg/icon_dataset.svg'
import icon_data_visualization from '@/assets/svg/icon_data-visualization.svg'
import icon_dashboard from '@/assets/svg/icon_dashboard.svg'
import icon_viewList_outlined from '@/assets/svg/icon_view-list_outlined.svg'
import association from '@/assets/svg/association.svg'
import icon_right_outlined from '@/assets/svg/icon_right_outlined.svg'
import icon_left_outlined from '@/assets/svg/icon_left_outlined.svg'
import relation_arrow_icon from '@/assets/svg/relation_arrow_icon.svg'
import icon_pc_outlined from '@/assets/svg/icon_pc_outlined.svg'
import {
  ref,
  reactive,
  shallowRef,
  computed,
  unref,
  onMounted,
  onBeforeMount,
  onBeforeUnmount,
  nextTick
} from 'vue'
import Chart from '@/views/menu/system/association/Chart.vue'
import { ElMessage } from 'element-plus-secondary'
import router from '@/router'
import { debounce, cloneDeep } from 'lodash-es'
import {
  getDatasourceRelationship as getDatasourceRelation,
  getDatasetRelationship as getDatasetRelation,
  getPanelRelationship as getPanelRelation,
  resourceCheckPermission
} from '@/api/relation/index'
import { queryTreeApi } from '@/api/visualization/dataVisualization'
import { getDatasetTree } from '@/api/dataset'
import { listDatasources } from '@/api/datasource'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()
const associationFrom = reactive({
  type: 'datasource',
  value: ''
})
const nodeData = reactive({
  name: '',
  id: ''
})
const typeArr = computed(() => {
  return typeList.value.filter(ele => {
    const includesChart = ['dataV', 'dashboard']
    if (includesChart.includes(currentNode.type)) {
      return !includesChart.includes(ele.value)
    }
    return ele.value !== currentNode.type
  })
})
const currentNode = reactive({
  creator: '',
  name: '',
  updateTime: 0,
  id: '',
  type: ''
})
const treeData = ref([])
const chartSize = reactive({
  height: 0,
  width: 0
})
const current = computed(() => {
  return {
    queryType: associationFrom.type,
    num: associationFrom.value,
    label: nodeData.name,
    nodeData
  }
})

onBeforeMount(() => {
  queryTypeChange(associationFrom.type)
})
const treeDefaultProps = {
  children: 'children',
  label: 'name',
  disabled: data => {
    return !data.leaf && !data.children?.length
  }
}
let isQuery = false

const newTabView = () => {
  console.log('currentNode', currentNode)
  resourceCheckPermission(currentNode.id, currentNode.type).then(res => {
    let pathMap = {
      dataset: '/data/dataset',
      datasource: '/data/datasource',
      dashboard: '/panel/index',
      dataV: '/screen/index'
    }
    if (res.data) {
      const routeData = router.resolve({
        path: pathMap[currentNode.type],
        query: {
          id: currentNode.id,
          dvId: currentNode.id
        }
      })
      window.open(routeData.href, '_blank')
    } else {
      ElMessage.error(t('relation.no_permission'))
    }
  })
}

const handleTreeChange = item => {
  if (!item.leaf) return
  Object.assign(nodeData, {
    name: item.name,
    id: item.id
  })
}
const associationData = computed(() => {
  const { currentPage, pageSize } = paginationConfig
  return treeData.value.slice((currentPage - 1) * pageSize, currentPage * pageSize)
})

const activeIcon = ref('list')

const data = shallowRef([])
const loading = ref(false)
const tableLoading = ref(false)
const paginationConfig = reactive({
  currentPage: 1,
  pageSize: 10,
  total: 0
})
const handleSizeChange = pageSize => {
  paginationConfig.currentPage = 1
  paginationConfig.pageSize = pageSize
}
const handleCurrentChange = currentPage => {
  paginationConfig.currentPage = currentPage
}

const formatterValue = (_, __, cellValue) => {
  return cellValue ?? '-'
}

const indexMethod = (index: number) => {
  return index + 1
}
onBeforeUnmount(() => {
  window.removeEventListener('resize', getChartSize)
})
onMounted(() => {
  window.addEventListener('resize', getChartSize)
  getChartSize()
})
const handleActiveIcon = type => {
  activeIcon.value = type

  if (associationFrom.value) {
    onSubmit()
  }
}

const handleIconClick = ele => {
  ele.active = !ele.active
}
const typeList = ref([
  {
    label: t('relation.datasource'),
    value: 'datasource',
    active: true
  },
  {
    label: t('relation.dataset'),
    value: 'dataset',
    active: true
  },
  {
    label: t('relation.dashboard'),
    value: 'dashboard',
    active: true
  },
  {
    label: t('relation.dataV'),
    value: 'dataV',
    active: true
  }
])

const resRef = shallowRef({})
const getChartData = () => {
  const { type, value } = associationFrom
  tableLoading.value = true
  switch (type) {
    case 'datasource':
      getDatasourceRelationship(value)
      break
    case 'dataset':
      getDatasetRelationship(value)
      break
    case 'dataV':
    case 'dashboard': {
      getPanelRelationship(value)
      break
    }
    default:
      break
  }
}

const iconMap = {
  datasource: icon_database,
  dataset: icon_dataset,
  dataV: icon_data_visualization,
  dashboard: icon_dashboard
}
const groupByArr = ref<{ name: string; type: string; id: string }[]>([])
const dfsDetail = (arr = []) => {
  arr.forEach(ele => {
    const { name, type, id, subRelation = [] } = ele
    if (subRelation?.length) {
      dfsDetail(subRelation)
    }
    if (id) {
      groupByArr.value.push({ name, type, id })
    }
  })
}
const getDatasourceRelationship = id => {
  getDatasourceRelation(id)
    .then(res => {
      const arr = res?.data?.list || []
      Object.assign(currentNode, res?.data?.current || {})
      if (currentNode.id === associationFrom.value) {
        resRef.value = cloneDeep(res || {})
        treeData.value = []
        dfsTree(arr, current.value, {})
        initTable()
      }
      groupByArr.value = []
      dfsDetail(arr)
    })
    .finally(() => {
      tableLoading.value = false
      if (!isQuery) return
      isQuery = false
      nextTick(() => {
        if (activeIcon.value !== 'list' && resRef.value.data) {
          consanguinity.value.getChartData({
            info: current.value,
            res: resRef.value
          })
        }
      })
    })
}
const getDatasetRelationship = id => {
  getDatasetRelation(id)
    .then(res => {
      const { dsList, dvList, current: obj } = res?.data
      Object.assign(currentNode, obj || {})
      if (currentNode.id === associationFrom.value) {
        resRef.value = cloneDeep(res || {})
        treeData.value = []
        dfsTree(dvList, current.value, {})
        treeData.value = dsList
          .map(ele => {
            return cloneDeep(unref(treeData)).map(item => ({
              ...(item as object),
              datasource: ele.name
            }))
          })
          .flat()
        if (!dvList.length && !!dsList.length) {
          treeData.value = dsList.map(item => ({
            ...(item as object),
            dataset: current.value.label,
            datasource: item.name
          }))
        }
        initTable()
      }
      groupByArr.value = []
      dfsDetail(dsList.concat(dvList))
    })
    .finally(() => {
      tableLoading.value = false
      if (!isQuery) return
      isQuery = false
      nextTick(() => {
        if (activeIcon.value !== 'list' && resRef.value.data) {
          consanguinity.value.getChartData({
            info: current.value,
            res: resRef.value
          })
        }
      })
    })
}
const getPanelRelationship = id => {
  getPanelRelation(id)
    .then(res => {
      const arr = res?.data?.list || []
      Object.assign(currentNode, res?.data?.current || {})
      if (currentNode.id === associationFrom.value) {
        resRef.value = cloneDeep(res || {})
        treeData.value = []
        dfsTreeFlip(arr, current.value)
        initTable()
      }
      groupByArr.value = []
      dfsDetail(arr)
    })
    .finally(() => {
      tableLoading.value = false
      if (!isQuery) return
      isQuery = false
      nextTick(() => {
        if (activeIcon.value !== 'list' && resRef.value.data) {
          consanguinity.value.getChartData({
            info: current.value,
            res: resRef.value
          })
        }
      })
    })
}
const initTable = () => {
  paginationConfig.total = treeData.value.length
}
const dfsTreeFlip = (arr = [], { label }) => {
  arr.forEach(ele => {
    const { name, type, subRelation = [] } = ele
    if (subRelation?.length) {
      dfsTreeFlip(subRelation, { label: name })
    } else if (type === 'dataset') {
      const obj = {}
      obj[type] = name
      obj.datasource = label
      obj[associationFrom.type] = current.value.label
      treeData.value.push(obj)
    }
  })
}
const dfsTree = (arr = [], { queryType, label }, item) => {
  arr.forEach(ele => {
    const { name, type, subRelation = [] } = ele
    const obj = {}
    obj[type] = name
    obj[queryType] = label
    if (subRelation?.length) {
      dfsTree(subRelation, { queryType: type, label: name }, obj)
    } else {
      treeData.value.push({ ...item, ...obj })
    }
  })
}
const getChartSize = debounce(function () {
  const dom = document.querySelector(
    activeIcon.value === 'list' ? '.association-table_content' : '.association-tree_content'
  )
  if (!dom) return
  Object.assign(chartSize, {
    height: dom.offsetHeight + 'px',
    width: dom.offsetWidth + 'px'
  })
}, 200)
const listDatasource = () => {
  loading.value = true
  listDatasources({ busiFlag: 'datasource' })
    .then(res => {
      const [ele] = res
      if (!ele) return
      dfsId(res)
      data.value = ele.id === '0' ? ele.children : []
    })
    .finally(() => {
      loading.value = false
    })
}
const dfsId = arr => {
  arr.forEach(ele => {
    if (ele.children?.length) {
      dfsId(ele.children)
    }
    ele.value = ele.id
  })
}
const getDatasetList = () => {
  loading.value = true
  getDatasetTree({ busiFlag: 'dataset' })
    .then(res => {
      const [ele] = res
      if (!ele) return
      dfsId(res)
      data.value = ele.id === '0' ? ele.children : []
    })
    .finally(() => {
      loading.value = false
    })
}
const getPanelGroupList = () => {
  loading.value = true
  queryTreeApi({
    busiFlag: associationFrom.type,
    resourceTable: 'core '
  })
    .then(res => {
      const [root] = res || []
      if (root.id === '0') {
        dfsId(root.children)
      } else {
        dfsId(res)
      }
      data.value = root.id === '0' ? root.children : res || []
    })
    .finally(() => {
      loading.value = false
    })
}

const queryTypeChange = val => {
  associationFrom.value = ''
  Object.assign(nodeData, {
    name: '',
    id: ''
  })
  switch (val) {
    case 'datasource': {
      listDatasource()
      break
    }
    case 'dataset': {
      getDatasetList()
      break
    }
    case 'dataV':
    case 'dashboard': {
      getPanelGroupList()
      break
    }
    default:
      break
  }
}
const consanguinity = ref()
const onSubmit = () => {
  isQuery = true
  resRef.value = {}
  getChartData()
  if (activeIcon.value !== 'list' && resRef.value.data) {
    consanguinity.value.getChartData({
      info: current.value,
      res: resRef.value
    })
  }
}

const clickNode = ({ id, type }) => {
  switch (type) {
    case 'datasource':
      getDatasourceRelationship(id)
      break
    case 'dataset':
      getDatasetRelationship(id)
      break
    case 'dataV':
    case 'dashboard': {
      getPanelRelationship(id)
      break
    }
    default:
      break
  }
}
const showLeft = ref(false)
const handleShowLeft = val => {
  showLeft.value = val
  chartSize.width = val ? chartSize.width - 240 : chartSize.width + 240
  nextTick(() => {
    if (activeIcon.value !== 'list' && resRef.value.data) {
      consanguinity.value.reRender({
        info: current.value,
        res: resRef.value
      })
    }
  })
}
</script>

<template>
  <div class="association-content">
    <div class="search-association_form">
      <el-form inline :model="associationFrom" ref="associationFormRef">
        <el-form-item :label="t('relation.resource_type')">
          <el-select
            style="width: 240px"
            v-model="associationFrom.type"
            @change="queryTypeChange"
            :placeholder="t('relation.pls_choose')"
          >
            <el-option
              :key="ele.value"
              v-for="ele in typeList"
              :label="ele.label"
              :value="ele.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-loading="loading" :label="t('relation.choose_resource')">
          <el-tree-select
            v-model="associationFrom.value"
            :data="data"
            @node-click="handleTreeChange"
            :props="treeDefaultProps"
            filterable
            :render-after-expand="false"
            style="width: 240px"
            :placeholder="t('relation.pls_choose')"
          />
        </el-form-item>
        <el-form-item>
          <el-button :disabled="!associationFrom.value" type="primary" @click="onSubmit">{{
            t('commons.adv_search.search')
          }}</el-button>
        </el-form-item>
      </el-form>
      <div class="mode-switch">
        <el-tooltip effect="dark" :content="t('relation.list_chart')" placement="top">
          <el-icon @click="handleActiveIcon('list')" :class="activeIcon === 'list' && 'active'">
            <Icon name="icon_view-list_outlined"><icon_viewList_outlined /></Icon>
          </el-icon>
        </el-tooltip>
        <el-tooltip effect="dark" :content="t('relation.mind_map')" placement="top">
          <el-icon
            @click="handleActiveIcon('association')"
            :class="activeIcon === 'association' && 'active'"
          >
            <Icon name="association"><association class="svg-icon" /></Icon>
          </el-icon>
        </el-tooltip>
      </div>
    </div>
    <div v-show="activeIcon === 'list'" class="association-table_content">
      <grid-table
        v-loading="tableLoading"
        :table-data="associationData"
        :show-pagination="!!associationData.length"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :pagination="paginationConfig"
      >
        <el-table-column
          :index="indexMethod"
          type="index"
          min-width="66px"
          :label="t('relation.index')"
        >
        </el-table-column>
        <el-table-column
          :formatter="formatterValue"
          prop="datasource"
          show-overflow-tooltip
          :label="t('relation.datasource_name')"
        />
        <el-table-column
          :formatter="formatterValue"
          prop="dataset"
          show-overflow-tooltip
          :label="t('relation.dataset_name')"
        />
        <el-table-column
          :formatter="formatterValue"
          prop="dashboard"
          show-overflow-tooltip
          :label="t('relation.dashboard_name')"
        />
        <el-table-column
          :formatter="formatterValue"
          prop="dataV"
          show-overflow-tooltip
          :label="t('relation.dataV_name')"
        />
      </grid-table>
    </div>
    <div v-show="activeIcon === 'association'" class="association-tree_content">
      <Chart
        @clickNode="clickNode"
        ref="consanguinity"
        :chart-size="chartSize"
        :current="current"
      />
      <div
        class="node-info"
        :class="showLeft && 'border-none'"
        v-if="!!resRef.data"
        :style="{ width: showLeft ? 0 : '240px' }"
      >
        <el-tooltip effect="dark" :content="t('relation.retract')" placement="left">
          <p v-show="!showLeft" class="arrow-right" @click="handleShowLeft(true)">
            <el-icon>
              <Icon name="icon_right_outlined"><icon_right_outlined class="svg-icon" /></Icon>
            </el-icon>
          </p>
        </el-tooltip>

        <el-tooltip effect="dark" :content="t('relation.expand')" placement="left">
          <p v-show="showLeft" class="left-outlined" @click="handleShowLeft(false)">
            <el-icon>
              <Icon name="icon_left_outlined"><icon_left_outlined class="svg-icon" /></Icon>
            </el-icon>
          </p>
        </el-tooltip>
        <template v-if="!showLeft">
          <div class="node-info_title">{{ t('relation.node_info') }}</div>
          <div class="info-detail">
            <div class="name">{{ t('relation.node_name') }}</div>
            <div class="value">
              <el-icon>
                <Icon><component :is="iconMap[currentNode.type]"></component></Icon> </el-icon
              ><span :title="currentNode.name">{{ currentNode.name }}</span>
            </div>
            <div class="name">{{ t('relation.creator') }}</div>
            <div class="value">{{ currentNode.creator }}</div>
            <div class="name">{{ t('relation.last_update_time') }}</div>
            <div class="value">
              {{ new Date(currentNode.updateTime).toLocaleString() }}
            </div>
            <div class="line"></div>
            <div class="relation-detail">
              <div class="resource-title">{{ t('relation.dependent') }}</div>
              <template v-for="type in typeArr" :key="type.value">
                <div class="resource-item_title">
                  <el-icon @click="handleIconClick(type)" :class="!type.active && 'active'">
                    <Icon name="relation_arrow_icon"><relation_arrow_icon /></Icon>
                  </el-icon>
                  {{ type.label }} ({{ groupByArr.filter(ele => ele.type === type.value).length }})
                </div>
                <div
                  class="resource-item_list"
                  v-for="ele in groupByArr.filter(ele => ele.type === type.value && type.active)"
                  :key="ele.id"
                >
                  <el-icon>
                    <Icon><component :is="iconMap[ele.type]"></component></Icon>
                  </el-icon>
                  <span :title="ele.name">{{ ele.name }}</span>
                </div>
              </template>
            </div>
          </div>
          <div class="new-tab_view">
            <el-button @click="newTabView" text>
              <template #icon>
                <Icon name="icon_pc_outlined"><icon_pc_outlined class="svg-icon" /></Icon>
              </template>
              {{ t('relation.new_page') }}
            </el-button>
          </div>
        </template>
      </div>
    </div>
  </div>
</template>

<style lang="less" scoped>
.association-content {
  width: 100%;
  background: var(--ContentBG, #ffffff);
  height: calc(100vh - 105px);
  border-radius: 12px;
  padding: 24px;
  margin-top: 8px;

  .search-association_form {
    display: flex;
    .ed-form-item {
      margin-bottom: 16px;
      margin-right: 24px;
    }

    .ed-button.is-disabled {
      background: #bbbfc4;
      border-color: #bbbfc4;
    }
  }
  .mode-switch {
    border: 1px solid #bbbfc4;
    width: 64px;
    height: 32px;
    border-radius: 4px;
    margin-left: auto;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 4px;

    .ed-icon {
      cursor: pointer;
      font-size: 16px;
      width: 24px;
      height: 24px;
      border-radius: 4px;
      color: #1f2329;
    }

    .active {
      color: var(--ed-color-primary, #3370ff) !important;
      background-color: var(--ed-color-primary-1a, rgba(51, 112, 255, 0.1));
    }
  }

  .association-table_content,
  .association-tree_content {
    height: calc(100vh - 211px);
  }

  .association-tree_content {
    border: 1px solid #dee0e3;
    background: #f5f6f7;
    border-radius: 4px;
    position: relative;

    .node-info {
      width: 240px;
      height: 100%;
      position: absolute;
      right: 0;
      top: 0;
      background-color: #ffffff;
      border-left: 1px solid #1f232926;
      &.border-none {
        border: none;
      }

      .left-outlined {
        position: absolute;
        font-size: 12px;
        cursor: pointer;
        left: -20px;
        top: 16px;
        width: 20px;
        height: 24px;
        border-top-left-radius: 50%;
        border-bottom-left-radius: 50%;
        box-shadow: 0px 4px 8px 0px #0000001a;
        border: 1px solid #dee0e3;
        background: #fff;
        display: flex;
        align-items: center;
        & > .ed-icon {
          margin-left: 6px;
        }

        &:hover {
          width: 24px;
          left: -24px;
          color: var(--ed-color-primary, #3370ff);
          & > .ed-icon {
            margin-left: 8px;
          }
        }
      }

      .arrow-right {
        position: absolute;
        top: 16px;
        z-index: 2;
        cursor: pointer;
        margin: 0;
        display: flex;
        align-items: center;
        justify-content: center;
        left: -12px;
        height: 24px;
        width: 24px;
        box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
        border: 1px solid #dee0e3;
        background: #fff;
        font-size: 12px;
        border-radius: 50%;
        &:hover {
          color: var(--ed-color-primary, #3370ff);
        }
      }

      .node-info_title {
        height: 38px;
        border-bottom: 1px solid #1f232926;
        padding-left: 16px;
        display: flex;
        align-items: center;
        font-size: 14px;
        font-weight: 500;
        line-height: 22px;
      }
      .info-detail {
        padding: 15px;
        max-height: calc(100% - 80px);
        overflow-y: auto;
        .name {
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          color: #646a73;
          margin-bottom: 4px;
        }
        .value {
          margin-bottom: 16px;
          font-size: 14px;
          font-weight: 400;
          line-height: 22px;
          display: flex;
          align-items: center;
          span {
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
            width: calc(100% - 30px);
          }
          .ed-icon {
            font-size: 18px;
            margin-right: 8px;
          }
        }
        .line {
          background: #1f232926;
          height: 1px;
          width: 100%;
        }
        .relation-detail {
          margin-top: 16px;
          .resource-title {
            font-size: 14px;
            font-weight: 500;
            line-height: 22px;
            margin-bottom: 8px;
          }
          .resource-item_title {
            font-size: 14px;
            font-weight: 400;
            line-height: 22px;
            color: #646a73;
            margin-bottom: 8px;
            display: flex;
            align-items: center;
            .ed-icon {
              height: 10px;
              width: 10px;
              cursor: pointer;
              margin-right: 8px;

              &.active {
                transform: rotate(-90deg);
              }
            }
          }

          .resource-item_list {
            height: 32px;
            border: 1px solid #dee0e3;
            border-radius: 4px;
            margin-bottom: 8px;
            padding-left: 12px;
            display: flex;
            font-size: 14px;
            align-items: center;
            span {
              white-space: nowrap;
              overflow: hidden;
              text-overflow: ellipsis;
              width: calc(100% - 30px);
            }
            .ed-icon {
              font-size: 18px;
              margin-right: 8px;
            }
          }
        }
      }

      .new-tab_view {
        border-top: 1px solid #1f232926;
        height: 38px;
        width: 100%;
        display: flex;
        align-items: center;
        padding-left: 10px;
        position: absolute;
        bottom: 0;
        left: 0;
      }
    }
  }
}
</style>
