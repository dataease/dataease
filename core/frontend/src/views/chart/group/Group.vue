<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <el-col>
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('chart.datalist') }}
        </span>
        <el-button
          icon="el-icon-plus"
          type="text"
          size="mini"
          style="float: right;"
          @click="add('group')"
        />
      </el-row>
      <el-divider/>
      <el-row style="margin-bottom: 10px">
        <el-col :span="16">
          <el-input
            v-model="filterText"
            size="mini"
            :placeholder="$t('commons.search')"
            prefix-icon="el-icon-search"
            clearable
            class="main-area-input"
          />
        </el-col>
        <el-col :span="8">
          <el-dropdown>
            <el-button
              size="mini"
              type="primary"
            >
              {{ searchMap[searchType] }}<i class="el-icon-arrow-down el-icon--right"/>
            </el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item @click.native="searchTypeClick('all')">{{ $t('commons.all') }}</el-dropdown-item>
              <el-dropdown-item @click.native="searchTypeClick('folder')">{{ $t('commons.folder') }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-col>
      </el-row>

      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="chartTreeRef"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            highlight-current
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
          >
            <span
              v-if="data.modelInnerType ==='group'"
              slot-scope="{ node, data }"
              class="custom-tree-node father"
            >
              <span style="display: flex;flex: 1;width: 0;">
                <span>
                  <i class="el-icon-folder"/>
                </span>
                <span
                  style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                  :title="data.name"
                >{{ data.name }}</span>
              </span>
              <span
                v-if="hasDataPermission('manage',data.privileges)"
                class="child"
              >
                <span
                  v-if="data.modelInnerType ==='group'"
                  @click.stop
                >
                  <el-dropdown
                    trigger="click"
                    size="small"
                    @command="clickAdd"
                  >
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-plus"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        icon="el-icon-folder-add"
                        :command="beforeClickAdd('group',data,node)"
                      >
                        {{ $t('chart.group') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-circle-plus"
                        :command="beforeClickAdd('chart',data,node)"
                      >
                        {{ $t('chart.add_chart') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
                <span
                  style="margin-left: 12px;"
                  @click.stop
                >
                  <el-dropdown
                    trigger="click"
                    size="small"
                    @command="clickMore"
                  >
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-more"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        icon="el-icon-edit-outline"
                        :command="beforeClickMore('rename',data,node)"
                      >
                        {{ $t('chart.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-right"
                        :command="beforeClickMore('move',data,node)"
                      >
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-delete"
                        :command="beforeClickMore('delete',data,node)"
                      >
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
            <span
              v-else
              slot-scope="{ node, data }"
              class="custom-tree-node-list father"
            >
              <span style="display: flex;flex: 1;width: 0;">
                <span><svg-icon :icon-class="data.modelInnerType"/></span>
                <span
                  style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;"
                  :title="data.name"
                >{{ data.name }}</span>
              </span>
              <span
                v-if="hasDataPermission('manage',data.privileges)"
                class="child"
              >
                <span
                  style="margin-left: 12px;"
                  @click.stop
                >
                  <el-dropdown
                    trigger="click"
                    size="small"
                    @command="clickMore"
                  >
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-more"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item
                        icon="el-icon-edit-outline"
                        :command="beforeClickMore('renameChart',data,node)"
                      >
                        {{ $t('chart.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-right"
                        :command="beforeClickMore('moveDs',data,node)"
                      >
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-delete"
                        :command="beforeClickMore('deleteChart',data,node)"
                      >
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>

      <!--group add/edit-->
      <el-dialog
        v-dialogDrag
        :title="dialogTitle"
        :visible="editGroup"
        :show-close="false"
        width="30%"
      >
        <el-form
          ref="groupForm"
          :model="groupForm"
          :rules="groupFormRules"
          @keypress.enter.native="saveGroup(groupForm)"
          @submit.native.prevent
        >
          <el-form-item
            :label="$t('commons.name')"
            prop="name"
          >
            <el-input v-model="groupForm.name"/>
          </el-form-item>
        </el-form>
        <div
          slot="footer"
          class="dialog-footer"
        >
          <el-button
            size="mini"
            @click="close()"
          >{{ $t('chart.cancel') }}
          </el-button>
          <el-button
            type="primary"
            size="mini"
            @click="saveGroup(groupForm)"
          >{{ $t('chart.confirm') }}
          </el-button>
        </div>
      </el-dialog>
    </el-col>

    <!--rename chart-->
    <el-dialog
      v-dialogDrag
      :title="$t('chart.chart')"
      :visible="editTable"
      :show-close="false"
      width="30%"
    >
      <el-form
        ref="tableForm"
        :model="tableForm"
        :rules="tableFormRules"
        @submit.native.prevent
        @keypress.enter.native="saveTable(tableForm)"
      >
        <el-form-item
          :label="$t('commons.name')"
          prop="name"
        >
          <el-input v-model="tableForm.name"/>
        </el-form-item>
      </el-form>
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeTable()"
        >{{ $t('chart.cancel') }}
        </el-button>
        <el-button
          type="primary"
          size="mini"
          @click="saveTable(tableForm)"
        >{{ $t('chart.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <!--添加视图-选择数据集-->
    <el-dialog
      v-if="selectTableFlag"
      v-dialogDrag
      :title="$t('chart.add_chart')"
      :visible="selectTableFlag"
      :show-close="false"
      width="1000px"
      class="dialog-css"
    >
      <el-row style="width: 800px;">
        <el-form
          ref="form"
          :model="table"
          label-width="80px"
          size="mini"
          class="form-item"
          @submit.native.prevent
        >
          <el-col :span="12">
            <el-form-item :label="$t('chart.view_name')">
              <el-input
                v-model="chartName"
                style="height: 34px"
                size="mini"
              />
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>

      <el-steps
        :active="createActive"
        align-center
      >
        <el-step :title="$t('chart.select_dataset')"/>
        <el-step :title="$t('chart.select_chart_type')"/>
      </el-steps>

      <table-selector
        v-show="createActive === 1"
        @getTable="getTable"
      />
      <el-row
        v-show="createActive === 2"
        style="padding: 0 20px;"
      >
        <el-row class="chart-box">
          <span>
            <span
              class="theme-border-class"
              style="font-size: 12px"
            >{{ $t('chart.chart_type') }}</span>
            <span style="float: right;">
              <el-select
                v-model="view.render"
                class="render-select"
                style="width: 70px"
                size="mini"
              >
                <el-option
                  v-for="item in pluginRenderOptions"
                  :key="item.value"
                  :value="item.value"
                  :label="item.name"
                />
              </el-select>
            </span>
          </span>
          <el-row>
            <div>
              <el-radio-group
                v-model="view.type"
                style="width: 100%"
              >
                <chart-type
                  ref="cu-chart-type"
                  :chart="view"
                  style="height: 350px;"
                />
              </el-radio-group>
            </div>
          </el-row>
        </el-row>
        <el-row
          class="chart-box"
          style="text-align: center;"
        >
          <svg-icon
            :icon-class="view.isPlugin && view.type && view.type !== 'buddle-map' ? ('/api/pluginCommon/staticInfo/' + view.type + '/svg') : view.type"
            class="chart-icon"
          />
        </el-row>
      </el-row>

      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeCreateChart"
        >{{ $t('chart.cancel') }}
        </el-button>
        <el-button
          v-if="createActive === 2"
          type="primary"
          size="mini"
          @click="createPreview"
        >{{
            $t('chart.preview')
          }}
        </el-button>
        <el-button
          v-if="createActive === 1"
          type="primary"
          size="mini"
          :disabled="!table.id || !currGroup.id"
          @click="createNext"
        >{{ $t('chart.next') }}
        </el-button>
        <el-button
          v-if="createActive === 2"
          type="primary"
          size="mini"
          :disabled="!table.id || !currGroup.id || !view.type"
          @click="createChart"
        >{{ $t('chart.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <!--移动分组-->
    <el-dialog
      v-dialogDrag
      :title="moveDialogTitle"
      :visible="moveGroup"
      :show-close="false"
      width="30%"
      class="dialog-css"
    >
      <group-move-selector
        :item="groupForm"
        @targetGroup="targetGroup"
      />
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeMoveGroup()"
        >{{ $t('dataset.cancel') }}
        </el-button>
        <el-button
          :disabled="groupMoveConfirmDisabled"
          type="primary"
          size="mini"
          @click="saveMoveGroup(tGroup)"
        >{{
            $t('dataset.confirm')
          }}
        </el-button>
      </div>
    </el-dialog>

    <!--移动视图-->
    <el-dialog
      v-dialogDrag
      :title="moveDialogTitle"
      :visible="moveDs"
      :show-close="false"
      width="30%"
      class="dialog-css"
    >
      <chart-move-selector
        :item="dsForm"
        @targetDs="targetDs"
      />
      <div
        slot="footer"
        class="dialog-footer"
      >
        <el-button
          size="mini"
          @click="closeMoveDs()"
        >{{ $t('dataset.cancel') }}
        </el-button>
        <el-button
          :disabled="dsMoveConfirmDisabled"
          type="primary"
          size="mini"
          @click="saveMoveDs(tDs)"
        >{{
            $t('dataset.confirm')
          }}
        </el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
import { chartGroupTree, pluginTypes, post } from '@/api/chart/chart'
import { queryAuthModel } from '@/api/authModel/authModel'
import TableSelector from '../view/TableSelector'
import GroupMoveSelector from '../components/treeSelector/GroupMoveSelector'
import ChartMoveSelector from '../components/treeSelector/ChartMoveSelector'
import ChartType from '@/views/chart/view/ChartType'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_FUNCTION_CFG,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_SIZE,
  DEFAULT_SPLIT,
  DEFAULT_THRESHOLD,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  DEFAULT_TOTAL,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_EXT_STYLE,
  DEFAULT_YAXIS_STYLE
} from '../chart/chart'
import { checkViewTitle } from '@/components/canvas/utils/utils'
import { adaptCurTheme } from '@/components/canvas/utils/style'

export default {
  name: 'Group',
  components: { ChartType, TableSelector, GroupMoveSelector, ChartMoveSelector },
  props: {
    saveStatus: {
      type: Object,
      required: false,
      default: null
    },
    // 操作来源 'panel' 为仪表板
    optFrom: {
      type: String,
      required: false,
      default: null
    },
    adviceGroupId: {
      type: String,
      required: false,
      default: null
    },
    mountedInit: {
      type: Boolean,
      required: false,
      default: true
    }
  },
  data() {
    return {
      sceneMode: false,
      dialogTitle: '',
      search: '',
      editGroup: false,
      editTable: false,
      tData: [],
      chartData: [],
      currGroup: {},
      expandedArray: [],
      groupForm: {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      tableForm: {
        name: '',
        sort: 'type asc,create_time desc,name asc'
      },
      groupFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' },
          { max: 50, message: this.$t('commons.char_can_not_more_50'), trigger: 'change' }
        ]
      },
      tableFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'change' },
          { max: 50, message: this.$t('commons.char_can_not_more_50'), trigger: 'change' }
        ]
      },
      selectTableFlag: false,
      table: {},
      tables: [],
      chartName: this.$t('chart.chartName'),
      treeProps: {
        label: 'name',
        children: 'children',
        isLeaf: 'isLeaf',
        id: 'id',
        parentId: 'pid'
      },
      dsForm: {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      moveGroup: false,
      tGroup: {},
      moveDs: false,
      tDs: {},
      groupMoveConfirmDisabled: true,
      dsMoveConfirmDisabled: true,
      moveDialogTitle: '',
      isTreeSearch: false,
      chartGroupTreeAvailable: [],
      createActive: 1,
      view: {
        render: 'antv',
        type: 'table-normal'
      },
      renderOptions: [
        { name: 'AntV', value: 'antv' },
        { name: 'ECharts', value: 'echarts' }
      ],
      searchPids: [], // 查询命中的pid
      filterText: '',
      searchType: 'all',
      searchMap: {
        all: this.$t('commons.all'),
        folder: this.$t('commons.folder')
      },
      currentViewNodeData: {},
      currentKey: null,
      pluginRenderOptions: []
    }
  },
  computed: {
    chartType() {
      return this.view.type
    },
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {
    saveStatus() {
    },
    adviceGroupId() {
      // 仪表板新建视图建议的存放路径
      if (this.optFrom === 'panel') {
        this.currGroup['id'] = this.adviceGroupId
      }
    },
    filterText(val) {
      this.searchPids = []
      this.$refs.chartTreeRef.filter(val)
    },
    searchType(val) {
      this.searchPids = []
      this.$refs.chartTreeRef.filter(this.filterText)
    },
    chartType(val) {
      this.view.isPlugin = val && this.$refs['cu-chart-type'] && this.$refs['cu-chart-type'].currentIsPlugin(val)
    }

  },
  created() {
    const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views'))
    if (plugins) {
      this.loadPluginType()
    } else {
      pluginTypes().then(res => {
        const plugins = res.data
        localStorage.setItem('plugin-views', JSON.stringify(plugins))
        this.loadPluginType()
      }).catch(e => {
        localStorage.setItem('plugin-views', null)
        this.loadPluginType()
      })
    }
  },
  mounted() {
    if (this.mountedInit) {
      this.treeNode(true)
    }
    this.refresh()
    this.getChartGroupTree()
  },
  methods: {
    loadPluginType() {
      const plugins = localStorage.getItem('plugin-views') && JSON.parse(localStorage.getItem('plugin-views')) || []
      const pluginOptions = plugins.filter(plugin => !this.renderOptions.some(option => option.value === plugin.render)).map(plugin => {
        return { name: plugin.render, value: plugin.render }
      })
      this.pluginRenderOptions = [...this.renderOptions, ...pluginOptions]
    },
    clickAdd(param) {
      this.currGroup = param.data
      if (param.type === 'group') {
        this.add(param.type)
        this.groupForm.pid = param.data.id
        this.groupForm.level = param.data.level + 1
      } else {
        this.selectTable()
      }
    },

    beforeClickAdd(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    clickMore(param) {
      switch (param.type) {
        case 'rename':
          this.add(param.data.modelInnerType)
          this.groupForm = JSON.parse(JSON.stringify(param.data))
          break
        case 'move':
          this.moveTo(param.data)
          this.groupForm = JSON.parse(JSON.stringify(param.data))
          break
        case 'moveDs':
          this.moveToDs(param.data)
          this.dsForm = JSON.parse(JSON.stringify(param.data))
          break
        case 'delete':
          this.delete(param.data)
          break
        case 'renameChart':
          this.editTable = true
          this.tableForm = JSON.parse(JSON.stringify(param.data))
          break
        case 'deleteChart':
          this.deleteChart(param.data)
          break
      }
    },

    beforeClickMore(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    add(type) {
      switch (type) {
        case 'group':
          this.dialogTitle = this.$t('chart.group')
          break
        case 'scene':
          this.dialogTitle = this.$t('chart.scene')
          break
      }
      this.groupForm.type = type
      this.editGroup = true
    },

    saveGroup(group) {
      this.$refs['groupForm'].validate((valid) => {
        if (valid) {
          post('/chart/group/save', group).then(response => {
            this.close()
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.treeNode()
          })
        } else {
          return false
        }
      })
    },

    saveTable(view) {
      this.$refs['tableForm'].validate((valid) => {
        if (valid) {
          view.title = view.name
          view.sceneId = view.pid
          post('/chart/view/save/' + this.panelInfo.id, view).then(response => {
            this.closeTable()
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            this.treeNode()
            this.$emit('switchComponent', { name: '' })
          })
        } else {
          // this.$message({
          //   message: this.$t('commons.input_content'),
          //   type: 'error',
          //   showClose: true
          // })
          return false
        }
      })
    },

    delete(data) {
      this.$confirm(this.$t('chart.confirm_delete'), this.$t('chart.tips'), {
        confirmButtonText: this.$t('chart.confirm'),
        cancelButtonText: this.$t('chart.cancel'),
        type: 'warning'
      }).then(() => {
        post('/chart/group/delete/' + data.id, null).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('chart.delete_success'),
            showClose: true
          })
          this.treeNode()
          this.$emit('switchComponent', { name: '' })
        })
      }).catch(() => {
      })
    },

    deleteChart(data) {
      this.$confirm(this.$t('chart.confirm_delete'), this.$t('chart.tips'), {
        confirmButtonText: this.$t('chart.confirm'),
        cancelButtonText: this.$t('chart.cancel'),
        type: 'warning'
      }).then(() => {
        post('/chart/view/delete/' + data.id, null).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('chart.delete_success'),
            showClose: true
          })
          this.treeNode()
          this.$emit('switchComponent', { name: '' })
          this.$store.dispatch('chart/setTable', null)
        })
      }).catch(() => {
      })
    },

    close() {
      this.$refs['groupForm'].resetFields()
      this.editGroup = false
      this.groupForm = {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      }
    },

    closeTable() {
      this.editTable = false
      this.tableForm = {
        name: ''
      }
    },

    groupTree(group) {
      post('/chart/group/tree', group).then(response => {
        this.tData = response.data
      })
    },

    initCurrentNode() {
      if (this.currentKey) {
        this.$nextTick(() => {
          this.$refs.chartTreeRef.setCurrentKey(this.currentKey)
          this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
          })
        })
      }
    },
    treeNode(cache = false) {
      const modelInfo = localStorage.getItem('chart-tree')
      const userCache = (modelInfo && cache)
      if (userCache) {
        this.tData = JSON.parse(modelInfo)
        this.initCurrentNode()
      }
      queryAuthModel({ modelType: 'chart' }, !userCache).then(res => {
        localStorage.setItem('chart-tree', JSON.stringify(res.data))
        if (!userCache) {
          this.tData = res.data
          this.initCurrentNode()
        }
        if (this.filterText) {
          this.$refs.chartTreeRef.filter(this.filterText)
        }
      })
    },

    chartTree() {
      this.tables = []
      this.chartData = []
      if (this.currGroup.id) {
        post('/chart/view/list', {
          sort: 'create_time desc,name asc',
          sceneId: this.currGroup.id
        }).then(response => {
          this.tables = response.data
          this.chartData = JSON.parse(JSON.stringify(this.tables))
        })
      }
    },

    nodeClick(data, node) {
      if (data.modelInnerType !== 'group') {
        this.currentViewNodeData = data
        this.$emit('switchComponent', { name: 'ChartEdit', param: data })
      }
    },

    back() {
      this.sceneMode = false
      this.$emit('switchComponent', { name: '' })
    },

    beforeClickAddData(type) {
      return {
        'type': type
      }
    },
    sceneClick(data, node) {
      this.$emit('switchComponent', { name: 'ChartEdit', param: data })
    },
    reviewChartList() {
      if (this.$store.state.chart.chartSceneData) {
        const that = this
        this.chartData.forEach(function(ele) {
          if (ele.id === that.$store.state.chart.chartSceneData.id) {
            ele.type = that.$store.state.chart.chartSceneData.type
            ele.name = that.$store.state.chart.chartSceneData.name
          }
        })
      }
    },

    selectTable() {
      this.selectTableFlag = true
      this.chartName = this.$t('chart.chartName')
    },

    closeCreateChart() {
      this.createActive = 1
      this.selectTableFlag = false
      this.table = {}
      this.view = {
        render: 'antv',
        type: 'table-normal'
      }
    },

    createChart() {
      if (!this.chartName || this.chartName === '') {
        this.$message({
          message: this.$t('chart.name_can_not_empty'),
          type: 'error',
          showClose: true
        })
        return
      }
      if (this.chartName.length > 50) {
        this.$message({
          showClose: true,
          message: this.$t('commons.char_can_not_more_50'),
          type: 'error'
        })
        return
      }

      if (checkViewTitle('new', null, this.chartName)) {
        this.$message({
          showClose: true,
          message: this.$t('chart.title_repeat'),
          type: 'error'
        })
        return
      }
      const view = {}
      view.name = this.chartName
      view.title = this.chartName
      view.sceneId = this.currGroup.id
      view.tableId = this.table.id
      view.type = this.view.type
      view.isPlugin = this.view.isPlugin
      view.render = this.view.render
      view.resultMode = 'custom'
      view.resultCount = 1000
      view.refreshViewEnable = false
      view.refreshUnit = 'minute'
      view.refreshTime = 5
      const customAttr = {
        color: DEFAULT_COLOR_CASE,
        tableColor: DEFAULT_COLOR_CASE,
        size: DEFAULT_SIZE,
        label: DEFAULT_LABEL,
        tooltip: DEFAULT_TOOLTIP,
        totalCfg: DEFAULT_TOTAL
      }
      const customStyle = {
        text: DEFAULT_TITLE_STYLE,
        legend: DEFAULT_LEGEND_STYLE,
        xAxis: DEFAULT_XAXIS_STYLE,
        yAxis: DEFAULT_YAXIS_STYLE,
        yAxisExt: DEFAULT_YAXIS_EXT_STYLE,
        split: DEFAULT_SPLIT
      }
      // 新建的视图应用当前主题
      adaptCurTheme(customStyle, customAttr)
      view.customAttr = JSON.stringify(customAttr)
      view.customStyle = JSON.stringify(customStyle)
      view.senior = JSON.stringify({
        functionCfg: DEFAULT_FUNCTION_CFG,
        assistLine: [],
        threshold: DEFAULT_THRESHOLD
      })
      view.stylePriority = 'view' // 默认样式优先级视图
      view.xaxis = JSON.stringify([])
      view.xaxisExt = JSON.stringify([])
      view.yaxis = JSON.stringify([])
      view.yaxisExt = JSON.stringify([])
      view.extStack = JSON.stringify([])
      view.customFilter = JSON.stringify([])
      view.drillFields = JSON.stringify([])
      view.extBubble = JSON.stringify([])
      view.viewFields = JSON.stringify([])
      this.setChartDefaultOptions(view)

      const _this = this
      post('/chart/view/newOne/' + this.panelInfo.id, view, true).then(response => {
        this.closeCreateChart()
        this.$store.dispatch('chart/setTableId', null)
        this.$store.dispatch('chart/setTableId', this.table.id)
        if (this.optFrom === 'panel') {
          this.$emit('newViewInfo', { 'id': response.data.id, 'type': response.data.type })
        } else {
          _this.expandedArray.push(response.data.sceneId)
          _this.currentKey = response.data.id
          _this.treeNode()
        }
      })
    },

    setChartDefaultOptions(view) {
      const type = view.type
      const attr = JSON.parse(view.customAttr)
      if (view.render === 'echarts') {
        attr.label.position = 'inside'
      } else {
        attr.label.position = 'middle'
      }
      if (type.includes('pie')) {
        if (view.render === 'echarts') {
          attr.label.position = 'inside'
        } else {
          const customStyle = JSON.parse(view.customStyle)
          customStyle.legend.show = false
          view.customStyle = JSON.stringify(customStyle)
          attr.label.show = true
          attr.label.position = 'outer'
        }
        if (type === 'pie-donut') {
          attr.size.pieInnerRadius = Math.round(attr.size.pieOuterRadius * 0.75)
        }
        if (type === 'pie-donut-rose') {
          attr.size.pieInnerRadius = Math.round(attr.size.pieOuterRadius * 0.5)
        }
      } else if (type.includes('bar')) {
        attr.label.labelContent = ['quota']
        const senior = JSON.parse(view.senior)
        senior.functionCfg.emptyDataStrategy = 'ignoreData'
        view.senior = JSON.stringify(senior)
      } else if (type.includes('line')) {
        attr.label.position = 'top'
      }
      view.customAttr = JSON.stringify(attr)
    },

    getTable(table) {
      this.table = JSON.parse(JSON.stringify(table))
    },

    refresh() {
      const path = this.$route.path
      if (path === '/chart/chart-edit') {
        this.sceneMode = true
        const sceneId = this.$store.state.chart.sceneId
        post('/chart/group/getScene/' + sceneId, null).then(response => {
          this.currGroup = response.data
        })
      }
    },

    nodeExpand(data) {
      if (data.id) {
        this.expandedArray.push(data.id)
      }
    },
    nodeCollapse(data) {
      if (data.id) {
        this.expandedArray.splice(this.expandedArray.indexOf(data.id), 1)
      }
    },

    loadNode(node, resolve) {
      if (!this.isTreeSearch) {
        if (node.level > 0) {
          this.tables = []
          this.chartData = []
          if (node.data.id) {
            post('/chart/view/listAndGroup', {
              sort: 'name asc,create_time desc',
              sceneId: node.data.id
            }).then(response => {
              this.tables = response.data
              this.chartData = JSON.parse(JSON.stringify(this.tables))
              resolve(this.chartData)
            })
          }
        }
      } else {
        node.data.children ? resolve(node.data.children) : resolve([])
      }
    },

    moveTo(data) {
      this.moveGroup = true
      this.moveDialogTitle = this.$t('dataset.m1') + (data.name.length > 10 ? (data.name.substr(0, 10) + '...') : data.name) + this.$t('dataset.m2')
    },
    closeMoveGroup() {
      this.moveGroup = false
      this.groupForm = {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      }
    },
    saveMoveGroup() {
      this.groupForm.pid = this.tGroup.id
      post('/chart/group/save', this.groupForm).then(res => {
        this.closeMoveGroup()
        this.treeNode()
      })
    },
    targetGroup(val) {
      this.tGroup = val
      this.groupMoveConfirmDisabled = false
    },

    moveToDs(data) {
      this.moveDs = true
      this.moveDialogTitle = this.$t('dataset.m1') + (data.name.length > 10 ? (data.name.substr(0, 10) + '...') : data.name) + this.$t('dataset.m2')
    },
    closeMoveDs() {
      this.moveDs = false
      this.dsForm = {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      }
    },
    saveMoveDs() {
      const newSceneId = this.tDs.id
      this.dsForm.sceneId = newSceneId
      post('/chart/view/save/' + this.panelInfo.id, this.dsForm).then(res => {
        this.closeMoveDs()
        this.expandedArray.push(newSceneId)
        this.treeNode()
      })
    },
    targetDs(val) {
      this.tDs = val
      if (this.tDs.type === 'group') {
        this.dsMoveConfirmDisabled = false
      } else {
        this.dsMoveConfirmDisabled = false
      }
    },

    getChartGroupTree() {
      chartGroupTree({}).then(res => {
        this.chartGroupTreeAvailable = res.data
      })
    },
    normalizer(node) {
      // 去掉children=null的属性
      if (node.children === null || node.children === 'null') {
        delete node.children
      }
    },

    createPreview() {
      if (this.createActive > 1) {
        this.createActive--
      }
    },
    createNext() {
      if (this.createActive < 2) {
        this.createActive++
      }
    },
    filterNode(value, data) {
      if (!value) return true
      if (this.searchType === 'folder') {
        if (data.modelInnerType === 'group' && data.label.indexOf(value) !== -1) {
          this.searchPids.push(data.id)
          return true
        }
        if (this.searchPids.indexOf(data.pid) !== -1) {
          if (data.modelInnerType === 'group') {
            this.searchPids.push(data.id)
          }
          return true
        }
      } else {
        return data.label.indexOf(value) !== -1
      }
      return false
    },
    searchTypeClick(searchTypeInfo) {
      this.searchType = searchTypeInfo
    },
    nodeTypeChange(newType) {
      if (this.currentViewNodeData) {
        this.currentViewNodeData.modelInnerType = newType
      }
    }
  }
}
</script>

<style scoped>
.el-divider--horizontal {
  margin: 12px 0
}

.search-input {
  padding: 12px 0;
}

.custom-tree-container {
  margin-top: 10px;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.custom-tree-node-list {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding: 0 8px;
}

.tree-list ::v-deep .el-tree-node__expand-icon.is-leaf {
  display: none;
}

.custom-position {
  flex: 1;
  display: flex;
  align-items: center;
  font-size: 14px;
  flex-flow: row nowrap;
}

.form-item {
  margin-bottom: 0;
}

.title-css {
  height: 26px;
}

.title-text {
  line-height: 26px;
}

.dialog-css ::v-deep .el-dialog__header {
  padding: 20px 20px 0;
}

.dialog-css ::v-deep .el-dialog__body {
  padding: 10px 20px 20px;
}

.form-item ::v-deep .el-form-item__label {
  font-size: 12px;
}

.scene-title {
  width: 100%;
  display: flex;
}

.scene-title-name {
  width: 100%;
  overflow: hidden;
  display: inline-block;
  white-space: nowrap;
  text-overflow: ellipsis;
}

.father .child {
  /*display: none;*/
  visibility: hidden;
}

.father:hover .child {
  /*display: inline;*/
  visibility: visible;
}

.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}

/deep/ .vue-treeselect__control {
  height: 28px;
}

/deep/ .vue-treeselect__single-value {
  color: #606266;
  line-height: 28px !important;
}

.render-select ::v-deep .el-input__suffix {
  width: 20px;
}

.render-select ::v-deep .el-input__inner {
  padding-right: 10px;
  padding-left: 6px;
}

.dialog-css ::v-deep .el-step__title {
  font-weight: 400;
  font-size: 12px;
}

.chart-icon {
  width: 200px !important;
  height: 200px !important;
}

.chart-box {
  box-sizing: border-box;
  -moz-box-sizing: border-box;
  -webkit-box-sizing: border-box;
  width: 50%;
  float: left;
  height: 380px;
}
</style>
