<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col style="padding: 0 10px 0 10px;">
    <el-col>
      <el-row>
        <span class="header-title">{{ $t('panel.default_panel') }}</span>
        <div class="block">
          <el-tree
            :default-expanded-keys="expandedArray"
            :data="defaultData"
            node-key="id"
            :highlight-current="activeTree==='system'"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node">
              <span style="display: flex; flex: 1 1 0%; width: 0px;">
                <span>
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
              </span>
              <span style="margin-left: 12px;" @click.stop>
                <el-dropdown v-if="hasDataPermission('manage',data.privileges)" trigger="click" size="small" @command="clickMore">
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
                  </el-dropdown-menu>
                </el-dropdown>
              </span>
            </span>
          </el-tree>
        </div>
      </el-row>

      <el-row>
        <span class="header-title">
          {{ $t('panel.panel_list') }}
          <el-button style="float: right;padding-right: 7px;" icon="el-icon-plus" type="text" @click="showEditPanel(newFolder)" />
          <!--          <el-button style="float: right;" type="primary" size="mini" @click="showEditPanel(newFolder)">-->
          <!--            {{ $t('panel.groupAdd') }}-->
          <!--          </el-button>-->
        </span>
      </el-row>
      <el-col class="custom-tree-container">
        <div class="block">
          <el-tree
            ref="panel_list_tree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :highlight-current="activeTree==='self'"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node-list">
              <span style="display: flex; flex: 1 1 0%; width: 0px;">
                <span v-if="data.nodeType === 'panel'">
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span v-if="data.nodeType === 'folder'">
                  <i class="el-icon-folder" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
              </span>
              <span v-if="hasDataPermission('manage',data.privileges)">
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
                      <el-dropdown-item :command="beforeClickEdit('folder','new',data,node)">
                        <i class="el-icon-folder" /> &nbsp; <span>{{ $t('panel.groupAdd') }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item :command="beforeClickEdit('panel','new',data,node)">
                        <svg-icon icon-class="panel" class="ds-icon-scene" /> &nbsp; <span>{{ $t('panel.panelAdd') }}</span>
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
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-edit" :command="beforeClickMore('edit',data,node)">
                        {{ $t('panel.edit') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-share" :command="beforeClickMore('share',data,node)">
                        {{ $t('panel.share') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-paperclip" :command="beforeClickMore('link',data,node)">
                        {{ $t('panel.create_public_links') }}
                      </el-dropdown-item>
                      <el-dropdown-item v-if="data.nodeType==='panel'" icon="el-icon-paperclip" :command="beforeClickMore('toDefaultPanel',data,node)">
                        {{ $t('panel.to_default_panel') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('rename',data,node)">
                        {{ $t('panel.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('delete',data,node)">
                        {{ $t('panel.delete') }}
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
      </el-dialog>
      <!--新建仪表盘dialog-->
      <el-dialog :title="panelDialogTitle" :visible.sync="editPanel.visible" :show-close="true" width="600px">
        <edit-panel v-if="editPanel.visible" :edit-panel="editPanel" @closeEditPanelDialog="closeEditPanelDialog" @newPanelSave="newPanelSave" />
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
import { addGroup, delGroup, groupTree, defaultTree, findOne } from '@/api/panel/panel'
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
import {
  DEFAULT_COMMON_CANVAS_STYLE
} from '@/views/panel/panel'

import { DEFAULT_PANEL_STYLE } from '@/views/panel/panel'

export default {
  name: 'PanelList',
  components: { GrantAuth, LinkGenerate, EditPanel },
  data() {
    return {
      lastActiveNode: null, // 激活的节点 在这个节点下面动态放置子节点
      lastActiveNodeData: null,
      activeTree: 'self', // 识别当前操作的树类型self 是仪表盘列表树 system 是默认仪表盘树
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
            width: 1280,
            height: 720,
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
            width: 1280,
            height: 720,
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
      newFolder: {
        type: 'folder',
        data: {
          id: null,
          pid: null,
          level: 0
        },
        node: {},
        optType: 'newFirstFolder'
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
        panelType: 'self',
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
    this.$store.commit('setComponentData', [])
    this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE)
    this.defaultTree()
    this.tree(this.groupForm)
  },
  methods: {
    closeEditPanelDialog(panelInfo) {
      this.editPanel.visible = false
      debugger
      this.defaultTree()
      // 默认展开 同时点击 新增的节点
      if (panelInfo && this.lastActiveNodeData.id) {
        if (!this.lastActiveNodeData.children) {
          this.$set(this.lastActiveNodeData, 'children', [])
        }
        this.lastActiveNodeData.children.push(panelInfo)
        this.lastActiveNode.expanded = true
        this.activeNodeAndClick(panelInfo)
      }
    },
    showEditPanel(param) {
      this.lastActiveNode = param.node
      this.lastActiveNodeData = param.data
      this.editPanel = JSON.parse(JSON.stringify(this.editPanelModel))
      this.editPanel.optType = param.optType
      this.editPanel.panelInfo.nodeType = param.type
      this.editPanel.visible = true
      switch (param.optType) {
        case 'new':
          this.editPanel.titlePre = this.$t('commons.create')
          if (param.type === 'folder') {
            this.editPanel.panelInfo.name = this.$t('panel.groupAdd')
          } else {
            this.editPanel.panelInfo.name = this.$t('panel.panelAdd')
          }
          this.editPanel.panelInfo.pid = param.data.id
          this.editPanel.panelInfo.level = param.data.level + 1
          this.editPanel.panelInfo.panelType = 'self'
          break
        case 'newFirstFolder':
          this.editPanel.titlePre = this.$t('commons.create')
          this.editPanel.panelInfo.name = ''
          this.editPanel.panelInfo.pid = 'panel_list'
          this.editPanel.panelInfo.level = 0
          this.editPanel.panelInfo.panelType = 'self'
          break
        case 'edit':
        case 'rename':
          this.editPanel = {
            visible: true,
            titlePre: this.$t('commons.edit'),
            panelInfo: {
              id: param.data.id,
              pid: param.data.pid,
              name: param.data.name
            }
          }
          break
        case 'toDefaultPanel':
          this.editPanel = {
            visible: true,
            titlePre: this.$t('panel.to_default'),
            panelInfo: {
              id: param.data.id,
              name: param.data.name,
              optType: 'toDefaultPanel'
            }
          }
          break
      }
      switch (param.type) {
        case 'folder':
          this.editPanel.titleSuf = this.$t('panel.group')
          break
        case 'panel':
          this.editPanel.titleSuf = this.$t('panel.panel')
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
        case 'toDefaultPanel':
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
          this.edit(param.data, param.node)
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
            this.defaultTree()
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
          this.defaultTree()
        })
      }).catch(() => {
      })
    },

    close() {
      this.editGroup = false
      this.groupForm = {
        name: null,
        pid: null,
        panelType: 'self',
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
      this.lastActiveNode = node
      this.lastActiveNodeData = data
      this.activeTree = data.panelType
      if (data.nodeType === 'panel') {
        // 加载视图数据
        findOne(data.id).then(response => {
          this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
          //   this.$store.commit('setComponentData', sourceInfo.type === 'custom' ? sourceInfo : this.resetID(sourceInfo))
          const temp = JSON.parse(response.data.panelStyle)
          this.$store.commit('setCanvasStyle', temp)
          this.$store.dispatch('panel/setPanelInfo', data)
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
    edit(data, node) {
      this.lastActiveNodeData = data
      this.lastActiveNode = node
      // 清空当前缓存
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE)
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
          item.type !== 'custom' && (item.id = uuid.v1())
        })
      }
      return data
    },
    newPanelSave(id) {

    },
    // 激活并点击当前节点
    activeNodeAndClick(panelInfo) {
      if (panelInfo) {
        this.$nextTick(() => {
          // 延迟设置CurrentKey
          this.$refs.panel_list_tree.setCurrentKey(panelInfo.id)
          this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
            // 如果是仪表盘列表的仪表盘 直接进入编辑界面
            if (panelInfo.nodeType === 'panel' && panelInfo.panelType === 'self') {
              this.edit(this.lastActiveNodeData, this.lastActiveNode)
            }
          })
        })
      }
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
    display: block;
    height: 100%;
    /*line-height: 36px;*/
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

</style>
