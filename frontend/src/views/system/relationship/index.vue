<template>
  <div
    class="consanguinity"
    @click.stop="resetFilter"
  >
    <div class="route-title">{{ $t('commons.consanguinity') }}</div>
    <div class="container-wrapper">
      <el-form
        ref="form"
        :inline="true"
        :model="formInline"
        class="de-form-inline"
      >
        <el-form-item
          prop="queryType"
          :label="$t('commons.adv_search.search') + $t('table.type')"
          :required="true"
        >
          <el-select
            v-model="formInline.queryType"
            :placeholder="$t('fu.search_bar.please_select')"
            @change="queryTypeChange"
            @focus="resetFilter"
          >
            <el-option
              v-for="item in queryTypeNameList"
              :key="item.value"
              :label="$t(item.label)"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          :label="queryTypeTitle"
          :error="errorMsg"
        >
          <el-popover
            v-model="showTree"
            placement="bottom"
            trigger="manual"
            :width="popoverSize"
            popper-class="relation-popover"
          >
            <el-tree
              v-show="showTree"
              ref="resourceTree"
              accordion
              node-key="id"
              :indent="8"
              :data="resourceTreeData"
              :highlight-current="true"
              :filter-node-method="filterNodeMethod"
              :current-node-key="formInline.dataSourceName"
              @node-click="nodeClick"
            >
              <span
                slot-scope="{ data }"
              >
                <span>
                  <span>
                    <svg-icon
                      :icon-class="getIconClass(formInline.queryType, data)"
                      :class="getNodeClass(formInline.queryType, data)"
                    />
                  </span>
                  <span
                    :title="data.name"
                    style="padding-right: 8px"
                  >
                    {{ data.name }}
                  </span>
                </span>
              </span>
            </el-tree>
            <el-input
              ref="treeSelect"
              slot="reference"
              v-model="querySelected"
              :placeholder="queryPlaceholder"
              @click.native.stop="showTree = true"
              @input="onQueryInput"
              @focus="onQueryFocus"
            />
          </el-popover>
        </el-form-item>
        <el-form-item style="float: right">
          <de-btn
            type="primary"
            @click="onSubmit"
          >
            {{ $t('commons.adv_search.search') }}
          </de-btn>
        </el-form-item>
      </el-form>
      <div class="select-icon">
        <i
          :class="[activeIcon === 'date' ? 'active-icon' : '']"
          class="el-icon-date"
          @click="activeQueryType('date')"
        />
        <svg-icon
          icon-class="sys-relationship"
          :class="[activeIcon === 'share' ? 'active-icon' : '']"
          @click="activeQueryType('share')"
        />
      </div>
      <div
        v-show="activeIcon === 'share'"
        id="consanguinity"
      >
        <consanguinity
          ref="consanguinity"
          :chart-size="chartSize"
          :current="current"
        />
      </div>
      <div
        v-show="activeIcon === 'date'"
        class="consanguinity-table"
      >
        <grid-table
          v-loading="loading"
          :table-data="tableData"
          :pagination="paginationConfig"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
          <el-table-column
            :label="$t('table.id')"
            type="index"
            width="50"
          />
          <el-table-column
            prop="datasource"
            :formatter="formatter"
            show-overflow-tooltip
            :label="$t('commons.datasource') + $t('commons.name')"
          />
          <el-table-column
            prop="dataset"
            show-overflow-tooltip
            :formatter="formatter"
            :label="$t('dataset.name')"
          />
          <el-table-column
            prop="panel"
            show-overflow-tooltip
            :formatter="formatter"
            :label="$t('app_template.panel_name')"
          />
        </grid-table>
      </div>
    </div>
  </div>
</template>

<script>
import {
  getDatasourceRelationship,
  getDatasetRelationship,
  getPanelRelationship
} from '@/api/chart/chart.js'
import { listDatasource } from '@/api/dataset/dataset'
import _ from 'lodash'
import GridTable from '@/components/gridTable/index.vue'
import consanguinity from './consanguinity.vue'
import { defaultTree, groupTree } from '@/api/panel/panel'
import { queryAuthModel } from '@/api/authModel/authModel'
import { data } from 'vue2-ace-editor'

export default {
  name: 'Consanguinity',
  components: { GridTable, consanguinity },
  data() {
    return {
      formInline: {
        queryType: 'datasource',
        dataSourceName: ''
      },
      queryTypeNameList: [
        {
          label: 'commons.datasource',
          value: 'datasource'
        },
        {
          label: 'dataset.datalist',
          value: 'dataset'
        },
        {
          label: 'panel.panel',
          value: 'panel'
        }
      ],
      chartSize: {
        height: 0,
        width: 0
      },
      treeData: [],
      loading: false,
      routerWithParams: {},
      activeIcon: 'date',
      paginationConfig: {
        currentPage: 1,
        pageSize: 10,
        total: 0
      },
      resourceTreeData: [],
      showTree: false,
      nodeData: {},
      popoverSize: 400,
      currentNode: {},
      querySelected: '',
      queryPlaceholder: '',
      errorMsg: ''
    }
  },
  computed: {
    current() {
      return {
        queryType: this.formInline.queryType,
        num: this.formInline.dataSourceName,
        label: this.nodeData.name,
        nodeData: this.nodeData
      }
    },
    queryTypeTitle() {
      return (
        this.$t(
          this.queryTypeNameList.find(
            (ele) => ele.value === this.formInline.queryType
          ).label
        ) + this.$t('commons.name')
      )
    },
    tableData() {
      const { currentPage, pageSize } = this.paginationConfig
      return this.treeData.slice(
        (currentPage - 1) * pageSize,
        currentPage * pageSize
      )
    }
  },
  created() {
    this.routerWithParams = this.$route.query
    const { id, queryType, name } = this.routerWithParams
    if (id && queryType) {
      this.searchDetail(id, queryType, name)
      return
    }
    this.listDatasource()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.getChartSize)
  },
  mounted() {
    window.addEventListener('resize', this.getChartSize)
    this.getChartSize()
  },
  methods: {
    data,
    activeQueryType(activeIcon) {
      this.activeIcon = activeIcon
      if (this.formInline.dataSourceName) {
        this.onSubmit()
      }
    },
    async searchDetail(id, queryType, name) {
      switch (queryType) {
        case 'datasource':
          await this.listDatasource()
          break
        case 'dataset':
          await this.getDatasetList()
          break
        case 'panel':
          await this.getPanelGroupList()
          break
        default:
          break
      }
      this.formInline = { queryType, dataSourceName: id }
      this.nodeData = { id, name }
      this.querySelected = this.queryPlaceholder = name
      this.$refs.resourceTree.setCurrentKey(id)
      const currentParents = this.$refs.resourceTree.getNodePath(this.nodeData)
      currentParents.forEach((node) => {
        this.$refs.resourceTree.store.nodesMap[node.id].expanded = true
      })
      this.getChartData()
    },
    getChartData() {
      const { queryType, dataSourceName: id } = this.formInline
      switch (queryType) {
        case 'datasource':
          this.getDatasourceRelationship(id)
          break
        case 'dataset':
          this.getDatasetRelationship(id)
          break
        case 'panel': {
          let targetId = id
          if (this.nodeData.source) {
            targetId = this.nodeData.source
          }
          this.getPanelRelationship(targetId)
          break
        }
        default:
          break
      }
    },
    getDatasourceRelationship(id) {
      getDatasourceRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTree(arr, this.current)
        this.initTable()
      })
    },
    getDatasetRelationship(id) {
      getDatasetRelationship(id).then((res) => {
        const { id, name } = res.data
        res.data.id = this.current.num
        res.data.name = this.current.label
        res.data.type = this.current.queryType
        const arr = res.data ? [res.data] : []
        this.treeData = []
        this.dfsTree(arr, { num: id, label: name, queryType: 'datasource' })
        this.initTable()
      })
    },
    getPanelRelationship(id) {
      getPanelRelationship(id).then((res) => {
        const arr = res.data || []
        this.treeData = []
        this.dfsTreeFlip(arr, this.current)
        this.initTable()
      })
    },
    formatter(row, column, cellValue) {
      return cellValue || '-'
    },
    initTable() {
      this.paginationConfig.total = this.treeData.length
    },
    dfsTreeFlip(arr = [], { queryType, label }) {
      arr.forEach((ele) => {
        const { name, type, subRelation = [] } = ele
        if (subRelation.length) {
          this.dfsTreeFlip(subRelation, { label: name })
        } else if (type === 'dataset') {
          const obj = {}
          obj[type] = name
          obj.datasource = label
          obj.panel = this.current.label
          this.treeData.push(obj)
        }
      })
    },
    dfsTree(arr = [], { queryType, label }, item) {
      arr.forEach((ele) => {
        const { name, type, subRelation = [] } = ele
        const obj = {}
        obj[type] = name
        obj[queryType] = label
        if (subRelation.length) {
          this.dfsTree(subRelation, { queryType: type, label: name }, obj)
        } else {
          this.treeData.push({ ...item, ...obj })
        }
      })
    },
    getChartSize: _.debounce(function() {
      const dom = document.querySelector(
        this.activeIcon === 'date' ? '.consanguinity-table' : '#consanguinity'
      )
      this.chartSize = {
        height: dom.offsetHeight + 'px',
        width: dom.offsetWidth + 'px'
      }
    }, 200),
    listDatasource() {
      return listDatasource().then((res) => {
        const dsArr = res?.data || []
        const typeMap = {}
        dsArr.forEach((item) => {
          if (!typeMap[item.type]) {
            typeMap[item.type] = [{ id: item.id, name: item.name, type: 'datasource' }]
            this.resourceTreeData.push({
              id: item.type,
              name: item.typeDesc,
              type: 'folder',
              children: typeMap[item.type]
            })
          } else {
            typeMap[item.type].push({ id: item.id, name: item.name, type: 'datasource' })
          }
        })
      })
    },
    getDatasetList() {
      return queryAuthModel({ modelType: 'dataset' }, false).then((res) => {
        this.resourceTreeData = res.data
      })
    },
    getPanelGroupList() {
      return defaultTree({ panelType: 'system' }, true).then((res) => {
        if (res.data?.length > 0) {
          const defaultPanelTree = {
            id: 'defaultPanel',
            name: this.$t('panel.default_panel'),
            nodeType: 'folder',
            children: res.data
          }
          this.resourceTreeData.push(defaultPanelTree)
        }
      }).then(() => {
        const form = {
          panelType: 'self',
          sort: 'create_time desc,node_type desc,name asc'
        }
        groupTree(form, true).then((res) => {
          this.resourceTreeData = [...this.resourceTreeData, ...res.data]
        })
      })
    },
    queryTypeChange(val) {
      this.formInline.dataSourceName = ''
      this.resourceTreeData = []
      this.nodeData = {}
      this.currentNode = {}
      this.querySelected = this.queryPlaceholder = ''
      switch (val) {
        case 'datasource': {
          this.listDatasource()
          break
        }
        case 'dataset': {
          this.getDatasetList()
          break
        }
        case 'panel': {
          this.getPanelGroupList()
          break
        }
        default:
          break
      }
    },
    onSubmit() {
      if (!this.formInline.dataSourceName) {
        this.errorMsg = this.$t('chart.name_can_not_empty')
        return
      }
      this.errorMsg = ''
      if (this.activeIcon === 'date') {
        this.getChartData()
      } else {
        this.$refs.consanguinity.getChartData(this.current)
      }
    },
    handleSizeChange(pageSize) {
      this.paginationConfig.currentPage = 1
      this.paginationConfig.pageSize = pageSize
      this.onSubmit()
    },
    handleCurrentChange(currentPage) {
      this.paginationConfig.currentPage = currentPage
      this.onSubmit()
    },
    getIconClass(queryType, nodeData) {
      switch (queryType) {
        case 'datasource': {
          if (nodeData.type === 'folder') {
            return 'scene'
          }
          return 'db-de'
        }
        case 'dataset': {
          if (nodeData.modelInnerType === 'group') {
            return 'scene'
          }
          return `ds-${nodeData.modelInnerType}`
        }
        case 'panel':
          if (nodeData.nodeType === 'panel') {
            let iconClass = 'panel-'
            if (nodeData.mobileLayout) {
              iconClass += 'mobile-'
            }
            iconClass += nodeData.status
            return iconClass
          }
          return 'scene'
        default:
          break
      }
    },
    getNodeClass(queryType, nodeData) {
      switch (queryType) {
        case 'dataset': {
          if (nodeData.modelInnerType !== 'group') {
            return `ds-icon-${nodeData.modelInnerType}`
          }
          return ''
        }
        case 'panel': {
          if (nodeData.nodeType === 'panel') {
            return 'ds-icon-scene'
          }
          return ''
        }
        default: {
          return ''
        }
      }
    },
    filterNodeMethod(value, data) {
      if (!value) {
        return true
      }
      return data.name.toLowerCase().indexOf(value.toLowerCase()) !== -1
    },
    onQueryInput(filterText) {
      this.$refs.resourceTree.filter(filterText)
    },
    onQueryFocus() {
      this.querySelected = ''
    },
    nodeClick(data, node) {
      if (node.isLeaf) {
        this.formInline.dataSourceName = data.id
        this.showTree = false
        this.nodeData = data
        this.currentNode = node
        this.querySelected = this.queryPlaceholder = data.name
        this.errorMsg = ''
      }
    },
    resetFilter() {
      if (this.showTree) {
        this.showTree = false
        this.querySelected = this.queryPlaceholder = this.nodeData.name
        this.$refs.resourceTree.filter()
        this.$refs.resourceTree.setCurrentKey(this.formInline.dataSourceName)
        if (this.formInline.dataSourceName === '') {
          this.$refs.resourceTree.setCurrentKey(null)
        }
        const nodesMap = this.$refs.resourceTree.store.nodesMap || {}
        let currentParents = []
        if (this.formInline.dataSourceName) {
          currentParents = this.$refs.resourceTree.getNodePath(this.nodeData).map((item) => item.id)
        }
        for (const key in nodesMap) {
          nodesMap[key].expanded = currentParents.includes(key)
        }
      }
    }
  }
}
</script>

<style lang="less">
.consanguinity {
  .active-icon {
    color: var(--primary, #3370ff);
  }
  box-sizing: border-box;
  background-color: var(--MainBG, #f5f6f7);
  overflow: hidden;
  padding: 24px 24px 24px 24px;
  height: 100%;
  display: flex;
  flex-direction: column;
  position: relative;

  .consanguinity-table {
    height: calc(100% - 100px);
    width: 100%;
  }

  .route-title {
    font-family: PingFang SC;
    font-size: 20px;
    font-weight: 500;
    line-height: 28px;
    text-align: left;
    color: var(--TextPrimary, #1f2329);
    width: 100%;
    margin: 0;
  }

  .container-wrapper {
    width: 100%;
    overflow: auto;
    background-color: var(--ContentBG, #ffffff);
    margin-top: 24px;
    padding: 24px;
    flex: 1;

    .select-icon {
      margin-bottom: 16px;
      i,
      svg {
        font-size: 16px;
        cursor: pointer;
        margin-right: 10px;
      }
    }
  }
}
.relation-popover {
  max-height: 70%;
  overflow-y: scroll;
}
</style>
