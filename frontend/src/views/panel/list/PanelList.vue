<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col>
    <!-- panel list -->
    <el-col>
      <el-row>
        <span class="header-title">默认仪表盘</span>
        <div class="block">
          <el-tree
            :default-expanded-keys="expandedArray"
            :data="defaultData"
            node-key="id"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ data }" class="custom-tree-node">
              <span>
                <span>
                  <el-button
                    icon="el-icon-picture-outline"
                    type="text"
                  />
                </span>
                <span style="margin-left: 6px">{{ data.name }}</span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-row>

      <el-row>
        <span class="header-title">仪表盘列表</span>
      </el-row>
      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node">
              <span>
                <span v-if="data.nodeType === 'panel'">
                  <el-button
                    icon="el-icon-picture-outline"
                    type="text"
                  />
                </span>
                <span style="margin-left: 6px">{{ data.name }}</span>
              </span>
              <span>
                <span v-if="data.nodeType ==='folder'" @click.stop>
                  <el-dropdown trigger="click" size="small" @command="showEditPanel">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-plus"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-circle-plus" :command="beforeClickEdit('folder','new',data,node)">
                        {{ $t('panel.groupAdd') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-folder-add" :command="beforeClickEdit('panel','new',data,node)">
                        {{ $t('panel.panelAdd') }}
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
                        {{ $t('panel.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('delete',data,node)">
                        {{ $t('panel.delete') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-share" :command="beforeClickMore('share',data,node)">
                        {{ $t('panel.share') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-edit" :command="beforeClickMore('edit',data,node)">
                        {{ $t('panel.edit') }}
                      </el-dropdown-item>

                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-paperclip" :command="beforeClickMore('link',data,node)">
                        创建公共链接
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>

      <el-dialog :title="dialogTitle" :visible="editGroup" :show-close="false" width="30%">
        <el-form ref="groupForm" :model="groupForm" :rules="groupFormRules">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="groupForm.name" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="close()">{{ $t('panel.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveGroup(groupForm)">{{ $t('panel.confirm') }}
          </el-button>
        </div>
      </el-dialog>

      <el-dialog
        :title="authTitle"
        :visible.sync="authVisible"
        custom-class="de-dialog"
      >
        <grant-auth v-if="authVisible" :resource-id="authResourceId" @close-grant="closeGrant" />
      </el-dialog>

      <el-dialog
        :title="linkTitle"
        :visible.sync="linkVisible"
        width="500px"
        @closed="removeLink"
      >
        <link-generate v-if="linkVisible" :resource-id="linkResourceId" />
        <!-- <span slot="footer" class="dialog-footer">
          <el-button @click="copyUri">复制链接</el-button>
        </span> -->
      </el-dialog>
      <!--新建仪表盘dialog-->
      <el-dialog :title="panelDialogTitle" :visible.sync="editPanel.visible" :show-close="true" width="600px">
        <edit-panel v-if="editPanel.visible" :edit-panel="editPanel" @closeEditPanelDialog="closeEditPanelDialog" />
      </el-dialog>
    </el-col>
  </el-col>
</template>

<script>
import GrantAuth from '../GrantAuth'
import LinkGenerate from '@/views/link/generate'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import EditPanel from './EditPanel'
import { addGroup, delGroup, groupTree, defaultTree, get } from '@/api/panel/panel'
import {
  DEFAULT_COLOR_CASE,
  DEFAULT_SIZE,
  DEFAULT_TITLE_STYLE,
  DEFAULT_LEGEND_STYLE,
  DEFAULT_LABEL,
  DEFAULT_TOOLTIP,
  DEFAULT_XAXIS_STYLE,
  DEFAULT_YAXIS_STYLE,
  DEFAULT_BACKGROUND_COLOR
} from '@/views/chart/chart/chart'

import { DEFAULT_PANEL_STYLE } from '@/views/panel/panel'

export default {
  name: 'PanelList',
  components: { GrantAuth, LinkGenerate, EditPanel },
  data() {
    return {
      editPanelModel: {
        titlePre: null,
        titleSuf: null,
        visible: false,
        optType: null,
        panelInfo: {
          name: null,
          pid: null,
          level: null,
          nodeType: null,
          panelType: null,
          panelStyle: JSON.stringify({
            width: 1680,
            height: 1050,
            scale: 100,
            openCommonStyle: true,
            panel: DEFAULT_PANEL_STYLE,
            chart: {
              xaxis: '[]',
              yaxis: '[]',
              show: true,
              type: 'panel',
              title: '',
              customAttr: JSON.stringify({
                color: DEFAULT_COLOR_CASE,
                size: DEFAULT_SIZE,
                label: DEFAULT_LABEL,
                tooltip: DEFAULT_TOOLTIP
              }),
              customStyle: JSON.stringify({
                text: DEFAULT_TITLE_STYLE,
                legend: DEFAULT_LEGEND_STYLE,
                xAxis: DEFAULT_XAXIS_STYLE,
                yAxis: DEFAULT_YAXIS_STYLE,
                background: DEFAULT_BACKGROUND_COLOR
              }),
              customFilter: '[]'
            }}),
          panelData: '[]'
        }
      },
      editPanel: {
        titlePre: null,
        titleSuf: '仪表盘',
        visible: false,
        optType: 'new',
        panelInfo: {
          name: null,
          pid: null,
          level: null,
          nodeType: null,
          panelType: null,
          panelStyle: JSON.stringify({
            width: 1680,
            height: 1050,
            scale: 100,
            openCommonStyle: true,
            panel: DEFAULT_PANEL_STYLE,
            chart: {
              xaxis: '[]',
              yaxis: '[]',
              show: true,
              type: 'panel',
              title: '',
              customAttr: JSON.stringify({
                color: DEFAULT_COLOR_CASE,
                size: DEFAULT_SIZE,
                label: DEFAULT_LABEL,
                tooltip: DEFAULT_TOOLTIP
              }),
              customStyle: JSON.stringify({
                text: DEFAULT_TITLE_STYLE,
                legend: DEFAULT_LEGEND_STYLE,
                xAxis: DEFAULT_XAXIS_STYLE,
                yAxis: DEFAULT_YAXIS_STYLE,
                background: DEFAULT_BACKGROUND_COLOR
              }),
              customFilter: '[]'
            }}),
          panelData: '[]'
        }
      },
      linkTitle: '链接分享',
      linkVisible: false,
      linkResourceId: null,
      authTitle: null,
      authResourceId: null,
      authVisible: false,
      defaultData: [],
      dialogTitle: '',
      search: '',
      editGroup: false,
      tData: [],
      tableData: [],
      currGroup: {},
      expandedArray: [],
      groupForm: {
        name: null,
        pid: null,
        level: 0,
        nodeType: null,
        children: [],
        sort: 'node_type desc,name asc'
      },
      tableForm: {
        name: '',
        mode: '',
        sort: 'node_type asc,create_time desc,name asc'
      },
      groupFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'blur' }
        ]
      },
      tableFormRules: {
        name: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'blur' }
        ],
        mode: [
          { required: true, message: this.$t('commons.input_content'), trigger: 'blur' }
        ]
      }
    }
  },
  computed: {
    panelDialogTitle() {
      return this.editPanel.titlePre + this.editPanel.titleSuf
    }
  },
  watch: {

  },
  mounted() {
    this.defaultTree()
    this.tree(this.groupForm)
  },
  methods: {
    closeEditPanelDialog() {
      this.editPanel.visible = false
      this.tree(this.groupForm)
    },
    showEditPanel(param) {
      debugger
      this.editPanel = JSON.parse(JSON.stringify(this.editPanelModel))
      this.editPanel.optType = param.optType
      this.editPanel.panelInfo.nodeType = param.type
      this.editPanel.visible = true
      switch (param.optType) {
        case 'new':
          this.editPanel.titlePre = '新建'
          this.editPanel.panelInfo.name = '新建仪表盘'
          this.editPanel.panelInfo.pid = param.data.id
          this.editPanel.panelInfo.level = param.data.level + 1
          break
        case 'edit':
        case 'rename':
          this.editPanel.titlePre = '编辑'
          this.editPanel.panelInfo.id = param.data.id
          this.editPanel.panelInfo.name = param.data.name
          break
      }
      switch (param.type) {
        case 'folder':
          this.editPanel.titleSuf = '目录'
          break
        case 'panel':
          this.editPanel.titleSuf = '仪表盘'
          break
      }
    },
    beforeClickEdit(type, optType, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node,
        'optType': optType
      }
    },

    clickMore(param) {
      switch (param.optType) {
        case 'rename':
          this.showEditPanel(param)
          break
        case 'move':
          break
        case 'delete':
          this.delete(param.data)
          break
        case 'share':
          this.share(param.data)
          break
        case 'edit':
          this.edit(param.data)
          break
        case 'link':
          this.link(param.data)
          break
      }
    },

    beforeClickMore(optType, data, node) {
      return {
        'type': data.nodeType,
        'data': data,
        'node': node,
        'optType': optType
      }
    },

    add(nodeType) {
      this.groupForm.nodeType = nodeType
      switch (nodeType) {
        case 'folder':
          this.dialogTitle = this.$t('panel.groupAdd')
          this.editGroup = true
          break
        case 'panel':
          this.editPanel.title = this.$t('panel.panelAdd')
          this.editPanel.visible = true
          break
      }
    },

    saveGroup(group) {
      // console.log(group);
      this.$refs['groupForm'].validate((valid) => {
        if (valid) {
          addGroup(group).then(res => {
            this.close()
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.tree(this.groupForm)
          })
        } else {
          this.$message({
            message: this.$t('commons.input_content'),
            type: 'error',
            showClose: true
          })
          return false
        }
      })
    },

    delete(data) {
      this.$confirm(this.$t('panel.confirm_delete'), this.$t('panel.tips'), {
        confirmButtonText: this.$t('panel.confirm'),
        cancelButtonText: this.$t('panel.cancel'),
        type: 'warning'
      }).then(() => {
        delGroup(data.id).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('panel.delete_success'),
            showClose: true
          })
          this.tree(this.groupForm)
        })
      }).catch(() => {
      })
    },

    close() {
      this.editGroup = false
      this.groupForm = {
        name: null,
        pid: null,
        level: 0,
        nodeType: null,
        children: [],
        sort: 'node_type desc,name asc'
      }
    },
    tree(group) {
      groupTree(group).then(res => {
        this.tData = res.data
      })
    },
    defaultTree() {
      const requestInfo = {
        panelType: 'system'
      }
      defaultTree(requestInfo).then(res => {
        this.defaultData = res.data
      })
    },

    nodeClick(data, node) {
      if (data.nodeType === 'panel') {
        // 加载视图数据
        get('panel/group/findOne/' + data.id).then(response => {
          debugger
          this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
          const temp = JSON.parse(response.data.panelStyle)
          this.$store.commit('setCanvasStyle', temp)
          this.$store.dispatch('panel/setPanelInfo', data)
          this.currGroup = data
        })
      }
      if (node.expanded) {
        this.expandedArray.push(data.id)
      } else {
        const index = this.expandedArray.indexOf(data.id)
        if (index > -1) {
          this.expandedArray.splice(index, 1)
        }
      }
    },
    back() {
      this.sceneMode = false
      this.$store.dispatch('dataset/setSceneData', null)
      this.$emit('switchComponent', { name: '' })
    },
    beforeClickAddData(type) {
      return {
        'type': type
      }
    },

    share(data) {
      this.authResourceId = data.id
      this.authTitle = '把[' + data.label + ']分享给'
      this.authVisible = true
    },
    closeGrant() {
      this.authResourceId = null
      this.authVisible = false
    },
    edit(data) {
      this.$store.dispatch('panel/setPanelInfo', data)
      bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
    },
    link(data) {
      this.linkVisible = true
      this.linkResourceId = data.id
    },
    removeLink() {
      this.linkVisible = false
      this.linkResourceId = null
    },
    resetID(data) {
      if (data) {
        data.forEach(item => {
          item.id = uuid.v1()
        })
      }

      return data
    }
  }
}
</script>

<style scoped>
  .header-title {
    font-size: 14px;
    flex: 1;
    color: #606266;
    font-weight: bold;

  }

  .el-divider--horizontal {
    margin: 12px 0
  }

  .search-input {
    padding: 12px 0;
  }

  .custom-tree-node {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 14px;
    padding-right: 8px;
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

</style>
