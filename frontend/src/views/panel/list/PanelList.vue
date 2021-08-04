<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col style="padding: 0 5px 0 5px;">
    <el-col>
      <el-row>
        <span class="header-title">{{ $t('panel.default_panel') }}</span>
        <div class="block">
          <el-tree
            ref="default_panel_tree"
            :default-expanded-keys="expandedArray"
            :data="defaultData"
            node-key="id"
            :highlight-current="activeTree==='system'"
            :expand-on-click-node="true"
            @node-click="nodeClick"
          >
            <span slot-scope="{ node, data }" class="custom-tree-node father">
              <span style="display: flex; flex: 1 1 0%; width: 0px;">
                <span>
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
              </span>
              <span style="margin-left: 12px;" class="child" @click.stop>
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
          <el-button style="float: right;padding-right: 7px;margin-top: -8px" icon="el-icon-plus" type="text" @click="showEditPanel(newFolder)" />
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
            <span slot-scope="{ node, data }" class="custom-tree-node-list father">
              <span style="display: flex; flex: 1 1 0%; width: 0px;">
                <span v-if="data.nodeType === 'panel'">
                  <svg-icon icon-class="panel" class="ds-icon-scene" />
                </span>
                <span v-if="data.nodeType === 'folder'">
                  <i class="el-icon-folder" />
                </span>
                <span style="margin-left: 6px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;">{{ data.name }}</span>
              </span>
              <span v-if="hasDataPermission('manage',data.privileges)" class="child">
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
                        <i class="el-icon-folder" />
                        <span>{{ $t('panel.groupAdd') }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item :command="beforeClickEdit('panel','new',data,node)">
                        <svg-icon icon-class="panel" class="ds-icon-scene" />
                        <span>{{ $t('panel.panelAdd') }}</span>
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
                      <el-dropdown-item v-if="data.nodeType==='panel'" :disabled="data.isDefault" icon="el-icon-copy-document" :command="beforeClickMore('toDefaultPanel',data,node)">
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

      <el-dialog v-dialogDrag :title="dialogTitle" :visible="editGroup" :show-close="false" width="500px">
        <el-form ref="groupForm" :model="groupForm" :rules="groupFormRules" @keypress.enter.native="saveGroup(groupForm)">
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
        v-dialogDrag
        :title="authTitle"
        :visible.sync="authVisible"
        width="800px"
        class="dialog-css"
      >
        <grant-auth v-if="authVisible" :resource-id="authResourceId" @close-grant="closeGrant" />
      </el-dialog>

      <el-dialog
        v-dialogDrag
        :title="linkTitle"
        :visible.sync="linkVisible"
        width="500px"
        @closed="removeLink"
      >
        <link-generate v-if="linkVisible" :resource-id="linkResourceId" />
      </el-dialog>
      <!--新建仪表板dialog-->
      <el-dialog v-dialogDrag :title="panelDialogTitle" :visible.sync="editPanel.visible" :show-close="true" width="600px">
        <edit-panel v-if="editPanel.visible" :edit-panel-out="editPanel" @closeEditPanelDialog="closeEditPanelDialog" @newPanelSave="newPanelSave" />
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
  DEFAULT_COMMON_CANVAS_STYLE_STRING
} from '@/views/panel/panel'

export default {
  name: 'PanelList',
  components: { GrantAuth, LinkGenerate, EditPanel },
  data() {
    return {
      lastActiveNode: null, // 激活的节点 在这个节点下面动态放置子节点
      lastActiveNodeData: null,
      activeTree: 'self', // 识别当前操作的树类型self 是仪表板列表树 system 是默认仪表板树
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
          panelStyle: JSON.stringify(DEFAULT_COMMON_CANVAS_STYLE_STRING),
          panelData: '[]'
        }
      },
      editPanel: {
        titlePre: null,
        titleSuf: '仪表板',
        visible: false,
        optType: 'new',
        panelInfo: {
          name: null,
          pid: null,
          level: null,
          nodeType: null,
          panelType: null,
          panelStyle: JSON.stringify(DEFAULT_COMMON_CANVAS_STYLE_STRING),
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
        sort: 'create_time desc,node_type desc,name asc'
      },
      tableForm: {
        name: '',
        mode: '',
        sort: 'create_time desc,node_type asc,create_time desc,name asc'
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
    // 切换展示页面后 重新点击一下当前节点
    '$store.state.panel.mainActiveName': function(newVal, oldVal) {
      if (newVal === 'PanelMain' && this.lastActiveNode && this.lastActiveNodeData) {
        this.activeNodeAndClickOnly(this.lastActiveNodeData)
      }
    }
  },
  mounted() {
    this.$store.commit('setComponentData', [])
    this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
    this.defaultTree()
    this.tree(this.groupForm)
  },
  methods: {
    closeEditPanelDialog(panelInfo) {
      this.editPanel.visible = false
      if (panelInfo) {
        this.defaultTree()
        // 默认展开 同时点击 新增的节点
        if (panelInfo && panelInfo.panelType === 'self' && this.lastActiveNodeData.id) {
          if (this.editPanel.optType === 'rename') {
            this.lastActiveNodeData.name = panelInfo.name
            return
          }
          if (!this.lastActiveNodeData.children) {
            this.$set(this.lastActiveNodeData, 'children', [])
          }
          this.lastActiveNodeData.children.push(panelInfo)
          this.lastActiveNode.expanded = true
          this.activeNodeAndClick(panelInfo)
        } else {
          this.tree(this.groupForm)
        }
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
            optType: 'rename',
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
          const componentDatas = JSON.parse(response.data.panelData)
          componentDatas.forEach(item => {
            item.filters = (item.filters || [])
          })
          this.$store.commit('setComponentData', this.resetID(componentDatas))
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
      // 清空当前缓存,快照
      this.$store.commit('refreshSnapshot')
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      // 清空临时画布数据
      this.$store.dispatch('panel/setComponentDataTemp', null)
      this.$store.dispatch('panel/setCanvasStyleDataTemp', null)
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
          // 去除default_tree 的影响
          this.$refs.default_panel_tree.setCurrentKey(null)
          this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
            // 如果是仪表板列表的仪表板 直接进入编辑界面
            if (panelInfo.nodeType === 'panel') {
              this.edit(this.lastActiveNodeData, this.lastActiveNode)
            }
          })
        })
      }
    },
    // 激活当前节点
    activeNodeAndClickOnly(panelInfo) {
      if (panelInfo) {
        this.$nextTick(() => {
          // 延迟设置CurrentKey
          this.$refs.panel_list_tree.setCurrentKey(panelInfo.id)
          // 去除default_tree 的影响
          this.$refs.default_panel_tree.setCurrentKey(null)
          this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
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

  .dialog-css>>>.el-dialog__body {
    padding: 15px 20px;
  }
  .dialog-css >>>.el-dialog__body {
    padding: 10px 20px 20px;
  }

  .father .child {
    /*display: none;*/
    visibility: hidden;
  }
  .father:hover .child {
    /*display: inline;*/
    visibility: visible;
  }

</style>
