<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <!-- group -->
    <el-col v-if="!sceneMode">
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('chart.datalist') }}
        </span>
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: right;" @click="add('group')">
          <!--          {{ $t('chart.add_group') }}-->
        </el-button>
      </el-row>
      <el-divider />

      <!--      <el-row>-->
      <!--        <el-button type="primary" size="mini" @click="add('group')">-->
      <!--          {{ $t('chart.add_group') }}-->
      <!--        </el-button>-->
      <!--        <el-button type="primary" size="mini" @click="add('scene')">-->
      <!--          {{ $t('chart.add_scene') }}-->
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

      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="asyncTree"
            :default-expanded-keys="expandedArray"
            :data="data"
            node-key="id"
            :expand-on-click-node="true"
            :load="loadNode"
            lazy
            :props="treeProps"
            highlight-current
            @node-click="nodeClick"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
          >
            <span v-if="data.type ==='group'" slot-scope="{ node, data }" class="custom-tree-node father">
              <span style="display: flex;flex: 1;width: 0;">
                <!--                <span v-if="data.type === 'scene'">-->
                <!--                  &lt;!&ndash;                  <el-button&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    icon="el-icon-folder-opened"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    type="text"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    size="mini"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                  />&ndash;&gt;-->
                <!--                  <svg-icon icon-class="scene" class="ds-icon-scene" />-->
                <!--                </span>-->
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
                      <el-dropdown-item icon="el-icon-circle-plus" :command="beforeClickAdd('group',data,node)">
                        {{ $t('chart.group') }}
                      </el-dropdown-item>
                      <!--                      <el-dropdown-item icon="el-icon-folder-add" :command="beforeClickAdd('scene',data,node)">-->
                      <!--                        {{ $t('chart.scene') }}-->
                      <!--                      </el-dropdown-item>-->
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
                      <!--                  <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('renameChart',data,node)">-->
                      <!--                    {{ $t('chart.rename') }}-->
                      <!--                  </el-dropdown-item>-->
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
        <el-form ref="groupForm" :model="groupForm" :rules="groupFormRules" @keyup.enter.native="saveGroup(groupForm)">
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
      <el-form ref="tableForm" :model="tableForm" :rules="tableFormRules" @keyup.enter.native="saveTable(tableForm)">
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
      v-dialogDrag
      :title="$t('chart.add_chart')"
      :visible="selectTableFlag"
      :show-close="false"
      width="70%"
      class="dialog-css"
      :destroy-on-close="true"
    >
      <el-row style="width: 400px;">
        <el-form ref="form" :model="table" label-width="80px" size="mini" class="form-item">
          <el-form-item :label="$t('chart.view_name')">
            <el-input v-model="chartName" size="mini" />
          </el-form-item>
        </el-form>
      </el-row>
      <table-selector @getTable="getTable" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeCreateChart">{{ $t('chart.cancel') }}</el-button>
        <el-button type="primary" size="mini" :disabled="!table.id" @click="createChart">{{ $t('chart.confirm') }}</el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
import { post } from '@/api/chart/chart'
import TableSelector from '../view/TableSelector'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_LABEL,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR
} from '../chart/chart'

export default {
  name: 'Group',
  components: { TableSelector },
  props: {
    saveStatus: {
      type: Object,
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
      data: [],
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
        isLeaf: 'isLeaf'
      }
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
      if (val && val !== '') {
        this.chartData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.name.includes(val) })))
      } else {
        this.chartData = JSON.parse(JSON.stringify(this.tables))
      }
    },
    saveStatus() {
      this.refreshNodeBy(this.saveStatus.sceneId)
    }
  },
  mounted() {
    this.treeNode(this.groupForm)
    this.refresh()
    // this.chartTree()
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
            this.treeNode(this.groupForm)
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
            this.$emit('switchComponent', { name: '' })
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
          this.treeNode(this.groupForm)
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
        this.data = response.data
      })
    },

    treeNode(group) {
      post('/chart/group/treeNode', group).then(res => {
        this.data = res.data
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
        this.$emit('switchComponent', { name: 'ChartEdit', param: { 'id': data.id }})
      }
      // if (data.type === 'scene') {
      //   this.sceneMode = true
      //   this.currGroup = data
      //   this.$store.dispatch('chart/setSceneId', this.currGroup.id)
      //   this.chartTree()
      // }
      // if (node.expanded) {
      //   this.expandedArray.push(data.id)
      // } else {
      //   const index = this.expandedArray.indexOf(data.id)
      //   if (index > -1) {
      //     this.expandedArray.splice(index, 1)
      //   }
      // }
    },

    back() {
      this.sceneMode = false
      // this.$router.push('/chart/home')
      this.$emit('switchComponent', { name: '' })
    },

    beforeClickAddData(type) {
      return {
        'type': type
      }
    },

    sceneClick(data, node) {
      // this.$store.dispatch('chart/setViewId', null)
      // this.$store.dispatch('chart/setViewId', data.id)
      this.$emit('switchComponent', { name: 'ChartEdit', param: { 'id': data.id }})
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
        background: DEFAULT_BACKGROUND_COLOR
      })
      view.customFilter = JSON.stringify([])
      post('/chart/view/save', view).then(response => {
        this.closeCreateChart()
        this.$store.dispatch('chart/setTableId', null)
        this.$store.dispatch('chart/setTableId', this.table.id)
        // this.$router.push('/chart/chart-edit')
        this.$emit('switchComponent', { name: 'ChartEdit', param: { 'id': response.data.id }})
        // this.$store.dispatch('chart/setViewId', response.data.id)
        // this.chartTree()
        this.refreshNodeBy(view.sceneId)
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
      // if (!this.isTreeSearch) {
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
      // }
    },

    refreshNodeBy(id) {
      if (!id || id === '0') {
        this.treeNode(this.groupForm)
      } else {
        const node = this.$refs.asyncTree.getNode(id) // 通过节点id找到对应树节点对象
        node.loaded = false
        node.expand() // 主动调用展开节点方法，重新查询该节点下的所有子节点
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
    display: none;
  }
  .father:hover .child {
    display: inline;
  }
</style>
