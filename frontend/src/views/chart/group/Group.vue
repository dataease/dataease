<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col class="tree-style">
    <!-- group -->
    <el-col v-if="!sceneMode">
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('chart.datalist') }}
        </span>
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: right;" @click="add('group')" />
      </el-row>
      <el-divider />
      <el-row>
        <el-form>
          <el-form-item class="form-item">
            <el-input
              v-model="search"
              size="mini"
              :placeholder="$t('chart.search')"
              prefix-icon="el-icon-search"
              clearable
            />
          </el-form-item>
        </el-form>
      </el-row>

      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="asyncTree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :expand-on-click-node="true"
            :load="loadNode"
            lazy
            :props="treeProps"
            highlight-current
            @node-click="nodeClick"
          >
            <span v-if="data.type ==='group'" slot-scope="{ node, data }" class="custom-tree-node father">
              <span style="display: flex;flex: 1;width: 0;">
                <span>
                  <i class="el-icon-folder" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
              </span>
              <span v-if="hasDataPermission('manage',data.privileges)" class="child">
                <span v-if="data.type ==='group'" @click.stop>
                  <el-dropdown trigger="click" size="small" @command="clickAdd">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-plus"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-folder-add" :command="beforeClickAdd('group',data,node)">
                        {{ $t('chart.group') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-circle-plus" :command="beforeClickAdd('chart',data,node)">
                        {{ $t('chart.add_chart') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
                <span style="margin-left: 12px;" @click.stop>
                  <el-dropdown trigger="click" size="small" @command="clickMore">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-more"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('rename',data,node)">
                        {{ $t('chart.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-right" :command="beforeClickMore('move',data,node)">
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('delete',data,node)">
                        {{ $t('chart.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
            <span v-else slot-scope="{ node, data }" class="custom-tree-node-list father">
              <span style="display: flex;flex: 1;width: 0;">
                <span><svg-icon :icon-class="data.type" /></span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>
              </span>
              <span v-if="hasDataPermission('manage',data.privileges)" class="child">
                <span style="margin-left: 12px;" @click.stop>
                  <el-dropdown trigger="click" size="small" @command="clickMore">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-more"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('renameChart',data,node)">
                        {{ $t('chart.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-right" :command="beforeClickMore('moveDs',data,node)">
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('deleteChart',data,node)">
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
      <el-dialog v-dialogDrag :title="dialogTitle" :visible="editGroup" :show-close="false" width="30%">
        <el-form ref="groupForm" :model="groupForm" :rules="groupFormRules" @keypress.enter.native="saveGroup(groupForm)">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="groupForm.name" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="close()">{{ $t('chart.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveGroup(groupForm)">{{ $t('chart.confirm') }}</el-button>
        </div>
      </el-dialog>
    </el-col>

    <!--scene-->
    <!--    <el-col v-if="sceneMode">-->
    <!--      <el-row class="title-css scene-title">-->
    <!--        <span class="title-text scene-title-name" :title="currGroup.name">-->
    <!--          {{ currGroup.name }}-->
    <!--        </span>-->
    <!--        <el-button icon="el-icon-back" size="mini" style="float: right" circle @click="back">-->
    <!--          &lt;!&ndash;          {{ $t('chart.back') }}&ndash;&gt;-->
    <!--        </el-button>-->
    <!--      </el-row>-->
    <!--      <el-divider />-->
    <!--      <el-row>-->
    <!--        <el-button type="primary" size="mini" plain @click="selectTable">-->
    <!--          {{ $t('chart.add_chart') }}-->
    <!--        </el-button>-->
    <!--      </el-row>-->
    <!--      <el-row>-->
    <!--        <el-form>-->
    <!--          <el-form-item class="form-item">-->
    <!--            <el-input-->
    <!--              v-model="search"-->
    <!--              size="mini"-->
    <!--              :placeholder="$t('chart.search')"-->
    <!--              prefix-icon="el-icon-search"-->
    <!--              clearable-->
    <!--            />-->
    <!--          </el-form-item>-->
    <!--        </el-form>-->
    <!--      </el-row>-->
    <!--      <span v-show="false">{{ sceneData }}</span>-->
    <!--      <el-tree-->
    <!--        :data="chartData"-->
    <!--        node-key="id"-->
    <!--        :expand-on-click-node="true"-->
    <!--        class="tree-list"-->
    <!--        highlight-current-->
    <!--        @node-click="sceneClick"-->
    <!--      >-->
    <!--        <span slot-scope="{ node, data }" class="custom-tree-node-list father">-->
    <!--          <span style="display: flex;flex: 1;width: 0;">-->
    <!--            <span><svg-icon :icon-class="data.type" /></span>-->
    <!--            <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" :title="data.name">{{ data.name }}</span>-->
    <!--          </span>-->
    <!--          <span v-if="hasDataPermission('manage',data.privileges)" class="child">-->
    <!--            <span style="margin-left: 12px;" @click.stop>-->
    <!--              <el-dropdown trigger="click" size="small" @command="clickMore">-->
    <!--                <span class="el-dropdown-link">-->
    <!--                  <el-button-->
    <!--                    icon="el-icon-more"-->
    <!--                    type="text"-->
    <!--                    size="small"-->
    <!--                  />-->
    <!--                </span>-->
    <!--                <el-dropdown-menu slot="dropdown">-->
    <!--                  &lt;!&ndash;                  <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('renameChart',data,node)">&ndash;&gt;-->
    <!--                  &lt;!&ndash;                    {{ $t('chart.rename') }}&ndash;&gt;-->
    <!--                  &lt;!&ndash;                  </el-dropdown-item>&ndash;&gt;-->
    <!--                  <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('deleteChart',data,node)">-->
    <!--                    {{ $t('chart.delete') }}-->
    <!--                  </el-dropdown-item>-->
    <!--                </el-dropdown-menu>-->
    <!--              </el-dropdown>-->
    <!--            </span>-->
    <!--          </span>-->
    <!--        </span>-->
    <!--      </el-tree>-->

    <!--      &lt;!&ndash;rename chart&ndash;&gt;-->
    <!--      <el-dialog v-dialogDrag :title="$t('chart.chart')" :visible="editTable" :show-close="false" width="30%">-->
    <!--        <el-form ref="tableForm" :model="tableForm" :rules="tableFormRules" @keyup.enter.native="saveTable(tableForm)">-->
    <!--          <el-form-item :label="$t('commons.name')" prop="name">-->
    <!--            <el-input v-model="tableForm.name" />-->
    <!--          </el-form-item>-->
    <!--        </el-form>-->
    <!--        <div slot="footer" class="dialog-footer">-->
    <!--          <el-button size="mini" @click="closeTable()">{{ $t('chart.cancel') }}</el-button>-->
    <!--          <el-button type="primary" size="mini" @click="saveTable(tableForm)">{{ $t('chart.confirm') }}</el-button>-->
    <!--        </div>-->
    <!--      </el-dialog>-->

    <!--      &lt;!&ndash;添加视图-选择数据集&ndash;&gt;-->
    <!--      &lt;!&ndash;      <el-dialog&ndash;&gt;-->
    <!--      &lt;!&ndash;        v-dialogDrag&ndash;&gt;-->
    <!--      &lt;!&ndash;        :title="$t('chart.add_chart')"&ndash;&gt;-->
    <!--      &lt;!&ndash;        :visible="selectTableFlag"&ndash;&gt;-->
    <!--      &lt;!&ndash;        :show-close="false"&ndash;&gt;-->
    <!--      &lt;!&ndash;        width="70%"&ndash;&gt;-->
    <!--      &lt;!&ndash;        class="dialog-css"&ndash;&gt;-->
    <!--      &lt;!&ndash;        :destroy-on-close="true"&ndash;&gt;-->
    <!--      &lt;!&ndash;      >&ndash;&gt;-->
    <!--      &lt;!&ndash;        <el-row style="width: 400px;">&ndash;&gt;-->
    <!--      &lt;!&ndash;          <el-form ref="form" :model="table" label-width="80px" size="mini" class="form-item">&ndash;&gt;-->
    <!--      &lt;!&ndash;            <el-form-item :label="$t('chart.view_name')">&ndash;&gt;-->
    <!--      &lt;!&ndash;              <el-input v-model="chartName" size="mini" />&ndash;&gt;-->
    <!--      &lt;!&ndash;            </el-form-item>&ndash;&gt;-->
    <!--      &lt;!&ndash;          </el-form>&ndash;&gt;-->
    <!--      &lt;!&ndash;        </el-row>&ndash;&gt;-->
    <!--      &lt;!&ndash;        <table-selector @getTable="getTable" />&ndash;&gt;-->
    <!--      &lt;!&ndash;        <div slot="footer" class="dialog-footer">&ndash;&gt;-->
    <!--      &lt;!&ndash;          <el-button size="mini" @click="closeCreateChart">{{ $t('chart.cancel') }}</el-button>&ndash;&gt;-->
    <!--      &lt;!&ndash;          <el-button type="primary" size="mini" :disabled="!table.id" @click="createChart">{{ $t('chart.confirm') }}</el-button>&ndash;&gt;-->
    <!--      &lt;!&ndash;        </div>&ndash;&gt;-->
    <!--      &lt;!&ndash;      </el-dialog>&ndash;&gt;-->

    <!--    </el-col>-->

    <!--rename chart-->
    <el-dialog v-dialogDrag :title="$t('chart.chart')" :visible="editTable" :show-close="false" width="30%">
      <el-form ref="tableForm" :model="tableForm" :rules="tableFormRules" @keypress.enter.native="saveTable(tableForm)">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="tableForm.name" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeTable()">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveTable(tableForm)">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--添加视图-选择数据集-->
    <el-dialog
      v-if="selectTableFlag"
      v-dialogDrag
      :title="$t('chart.add_chart')"
      :visible="selectTableFlag"
      :show-close="false"
      width="70%"
      class="dialog-css"
    >
      <el-row style="width: 800px;">
        <el-form ref="form" :model="table" label-width="80px" size="mini" class="form-item">
          <el-col :span="12">
            <el-form-item :label="$t('chart.view_name')">
              <el-input v-model="chartName" style="height: 34px" size="mini" />
            </el-form-item>
          </el-col>
          <el-col v-if="optFrom==='panel'" :span="12">
            <el-form-item :label="$t('chart.belong_group')">
              <treeselect
                v-model="currGroup.id"
                :options="chartGroupTreeAvailable"
                :normalizer="normalizer"
                :placeholder="$t('chart.select_group')"
              />
            </el-form-item>
          </el-col>
        </el-form>
      </el-row>
      <table-selector @getTable="getTable" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeCreateChart">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" :disabled="!table.id || !currGroup.id" @click="createChart">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>

    <!--移动分组-->
    <el-dialog v-dialogDrag :title="moveDialogTitle" :visible="moveGroup" :show-close="false" width="30%" class="dialog-css">
      <group-move-selector :item="groupForm" @targetGroup="targetGroup" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeMoveGroup()">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="groupMoveConfirmDisabled" type="primary" size="mini" @click="saveMoveGroup(tGroup)">{{ $t('dataset.confirm') }}
        </el-button>
      </div>
    </el-dialog>

    <!--移动视图-->
    <el-dialog v-dialogDrag :title="moveDialogTitle" :visible="moveDs" :show-close="false" width="30%" class="dialog-css">
      <chart-move-selector :item="dsForm" @targetDs="targetDs" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeMoveDs()">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="dsMoveConfirmDisabled" type="primary" size="mini" @click="saveMoveDs(tDs)">{{ $t('dataset.confirm') }}
        </el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
import { post, chartGroupTree } from '@/api/chart/chart'
import TableSelector from '../view/TableSelector'
import GroupMoveSelector from '../components/TreeSelector/GroupMoveSelector'
import ChartMoveSelector from '../components/TreeSelector/ChartMoveSelector'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR,
  DEFAULT_SPLIT
} from '../chart/chart'

export default {
  name: 'Group',
  components: { TableSelector, GroupMoveSelector, ChartMoveSelector },
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
      chartGroupTreeAvailable: []
    }
  },
  computed: {
    // sceneData: function() {
    //   this.reviewChartList()
    //   return this.$store.state.chart.chartSceneData
    // }
  },
  watch: {
    search(val) {
      // if (val && val !== '') {
      //   this.chartData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.name.includes(val) })))
      // } else {
      //   this.chartData = JSON.parse(JSON.stringify(this.tables))
      // }
      this.$emit('switchComponent', { name: '' })
      this.tData = []
      this.expandedArray = []
      if (this.timer) {
        clearTimeout(this.timer)
      }
      this.timer = setTimeout(() => {
        this.getTreeData(val)
      }, (val && val !== '') ? 500 : 0)
    },
    saveStatus() {
      this.refreshNodeBy(this.saveStatus.sceneId)
    },
    adviceGroupId() {
      // 仪表板新建视图建议的存放路径
      if (this.optFrom === 'panel') {
        this.currGroup['id'] = this.adviceGroupId
      }
    }
  },
  mounted() {
    this.treeNode(this.groupForm)
    this.refresh()
    // this.chartTree()
    this.getChartGroupTree()
  },
  methods: {
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
          this.add(param.data.type)
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
            // this.treeNode(this.groupForm)
            this.refreshNodeBy(group.pid)
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

    saveTable(view) {
      this.$refs['tableForm'].validate((valid) => {
        if (valid) {
          view.title = view.name
          post('/chart/view/save', view).then(response => {
            this.closeTable()
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            // this.chartTree()
            this.refreshNodeBy(view.sceneId)
            // this.$router.push('/chart/home')
            // this.$emit('switchComponent', { name: '' })
            this.$store.dispatch('chart/setTable', null)
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
          // this.treeNode(this.groupForm)
          this.refreshNodeBy(data.pid)
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
          // this.chartTree()
          this.refreshNodeBy(data.sceneId)
          // this.$router.push('/chart/home')
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

    treeNode(group) {
      post('/chart/group/treeNode', group).then(res => {
        this.tData = res.data
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
      if (data.type !== 'group') {
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
      this.selectTableFlag = false
      this.table = {}
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
      const view = {}
      view.name = this.chartName
      view.title = this.chartName
      view.sceneId = this.currGroup.id
      view.tableId = this.table.id
      view.type = 'bar'
      view.customAttr = JSON.stringify({
        color: DEFAULT_COLOR_CASE,
        tableColor: DEFAULT_COLOR_CASE,
        size: DEFAULT_SIZE,
        label: DEFAULT_LABEL,
        tooltip: DEFAULT_TOOLTIP
      })
      view.customStyle = JSON.stringify({
        text: DEFAULT_TITLE_STYLE,
        legend: DEFAULT_LEGEND_STYLE,
        xAxis: DEFAULT_XAXIS_STYLE,
        yAxis: DEFAULT_YAXIS_STYLE,
        background: DEFAULT_BACKGROUND_COLOR,
        split: DEFAULT_SPLIT
      })
      view.xaxis = JSON.stringify([])
      view.yaxis = JSON.stringify([])
      view.extStack = JSON.stringify([])
      view.customFilter = JSON.stringify([])
      post('/chart/view/save', view).then(response => {
        this.closeCreateChart()
        this.$store.dispatch('chart/setTableId', null)
        this.$store.dispatch('chart/setTableId', this.table.id)
        // this.$router.push('/chart/chart-edit')
        if (this.optFrom === 'panel') {
          this.$emit('newViewInfo', { 'id': response.data.id })
        } else {
          this.$emit('switchComponent', { name: 'ChartEdit', param: response.data })
          // this.$store.dispatch('chart/setViewId', response.data.id)
          // this.chartTree()
          this.refreshNodeBy(view.sceneId)
        }
      })
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

    refreshNodeBy(id) {
      if (this.isTreeSearch) {
        this.tData = []
        this.expandedArray = []
        this.searchTree(this.search)
      } else {
        if (!id || id === '0') {
          this.treeNode(this.groupForm)
        } else {
          const node = this.$refs.asyncTree.getNode(id) // 通过节点id找到对应树节点对象
          node.loaded = false
          node.expand() // 主动调用展开节点方法，重新查询该节点下的所有子节点
        }
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
        // this.tree(this.groupForm)
        this.refreshNodeBy(this.groupForm.pid)
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
      // if (this.tDs && this.tDs.type === 'group') {
      //   return
      // }
      this.dsForm.sceneId = this.tDs.id
      post('/chart/view/save', this.dsForm).then(res => {
        this.closeMoveDs()
        // this.tableTree()
        this.refreshNodeBy(this.dsForm.sceneId)
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

    searchTree(val) {
      const queryCondition = {
        // withExtend: 'parent',
        // modelType: 'chart',
        name: val
      }
      // authModel(queryCondition).then(res => {
      //   // this.highlights(res.data)
      //   this.tData = this.buildTree(res.data)
      //   // console.log(this.tData)
      // })

      post('/chart/view/search', queryCondition).then(res => {
        this.tData = this.buildTree(res.data)
      })
    },

    buildTree(arrs) {
      const idMapping = arrs.reduce((acc, el, i) => {
        acc[el[this.treeProps.id]] = i
        return acc
      }, {})
      const roots = []
      arrs.forEach(el => {
        // 判断根节点 ###
        // el.type = el.modelInnerType
        // el.isLeaf = el.leaf
        if (el[this.treeProps.parentId] === null || el[this.treeProps.parentId] === 0 || el[this.treeProps.parentId] === '0') {
          roots.push(el)
          return
        }
        // 用映射表找到父元素
        const parentEl = arrs[idMapping[el[this.treeProps.parentId]]]
        // 把当前元素添加到父元素的`children`数组中
        parentEl.children = [...(parentEl.children || []), el]

        // 设置展开节点 如果没有子节点则不进行展开
        if (parentEl.children.length > 0) {
          this.expandedArray.push(parentEl[this.treeProps.id])
        }
      })
      return roots
    },

    // 高亮显示搜索内容
    highlights(data) {
      if (data && this.search && this.search.length > 0) {
        const replaceReg = new RegExp(this.search, 'g')// 匹配关键字正则
        const replaceString = '<span style="color: #0a7be0">' + this.search + '</span>' // 高亮替换v-html值
        data.forEach(item => {
          item.name = item.name.replace(replaceReg, replaceString) // 开始替换
          item.label = item.label.replace(replaceReg, replaceString) // 开始替换
        })
      }
    },

    getTreeData(val) {
      if (val) {
        this.isTreeSearch = true
        this.searchTree(val)
      } else {
        this.isTreeSearch = false
        this.treeNode(this.groupForm)
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

  .custom-tree-container{
    margin-top: 10px;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right:8px;
  }

  .custom-tree-node-list {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding:0 8px;
  }

  .tree-list>>>.el-tree-node__expand-icon.is-leaf{
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

  .dialog-css >>> .el-dialog__header {
    padding: 20px 20px 0;
  }

  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .form-item>>>.el-form-item__label{
    font-size: 12px;
  }

  .scene-title{
    width: 100%;
    display: flex;
  }
  .scene-title-name{
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
  /deep/ .vue-treeselect__control{
    height: 28px;
  }
  /deep/ .vue-treeselect__single-value{
    color:#606266;
    line-height: 28px!important;
  }
</style>
