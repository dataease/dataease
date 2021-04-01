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
            @node-click="panelDefaultClick"
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
                  <el-dropdown trigger="click" size="small" @command="clickAdd">
                    <span class="el-dropdown-link">
                      <el-button
                        icon="el-icon-plus"
                        type="text"
                        size="small"
                      />
                    </span>
                    <el-dropdown-menu slot="dropdown">
                      <el-dropdown-item icon="el-icon-circle-plus" :command="beforeClickAdd('folder',data,node)">
                        {{ $t('panel.groupAdd') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-folder-add" :command="beforeClickAdd('panel',data,node)">
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
    </el-col>
  </el-col>
</template>

<script>
import GrantAuth from '../GrantAuth'
import LinkGenerate from '@/views/link/generate'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import { loadTable, getScene, addGroup, delGroup, addTable, delTable, groupTree, defaultTree, get } from '@/api/panel/panel'

export default {
  name: 'PanelList',
  components: { GrantAuth, LinkGenerate },
  data() {
    return {
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
      editTable: false,
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
  },
  watch: {
    // search(val){
    //   this.groupForm.name = val;
    //   this.tree(this.groupForm);
    // }
  },
  mounted() {
    this.defaultTree()
    this.tree(this.groupForm)
    this.refresh()
    this.tableTree()
    // this.$router.push('/dataset');
  },
  methods: {
    clickAdd(param) {
      // console.log(param);
      this.add(param.type)
      this.groupForm.pid = param.data.id
      this.groupForm.level = param.data.level + 1
    },

    beforeClickAdd(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    clickMore(param) {
      console.log(param)
      switch (param.type) {
        case 'rename':
          this.add(param.data.nodeType)
          this.groupForm = JSON.parse(JSON.stringify(param.data))
          break
        case 'move':
          break
        case 'delete':
          this.delete(param.data)
          break
        case 'editTable':
          this.editTable = true
          this.tableForm = JSON.parse(JSON.stringify(param.data))
          this.tableForm.mode = this.tableForm.mode + ''
          break
        case 'deleteTable':
          this.deleteTable(param.data)
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

    beforeClickMore(type, data, node) {
      return {
        'type': type,
        'data': data,
        'node': node
      }
    },

    add(nodeType) {
      switch (nodeType) {
        case 'folder':
          this.dialogTitle = this.$t('panel.groupAdd')
          break
        case 'panel':
          this.dialogTitle = this.$t('panel.panelAdd')
          break
      }
      this.groupForm.nodeType = nodeType
      this.editGroup = true
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

    saveTable(table) {
      //   console.log(table)
      table.mode = parseInt(table.mode)
      this.$refs['tableForm'].validate((valid) => {
        if (valid) {
          addTable(table).then(response => {
            this.closeTable()
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.tableTree()
            // this.$router.push('/dataset/home')
            this.$emit('switchComponent', { name: '' })
            this.$store.dispatch('dataset/setTable', null)
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

    deleteTable(data) {
      this.$confirm(this.$t('panel.confirm_delete'), this.$t('panel.tips'), {
        confirmButtonText: this.$t('panel.confirm'),
        cancelButtonText: this.$t('panel.cancel'),
        type: 'warning'
      }).then(() => {
        delTable(data.id).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('panel.delete_success'),
            showClose: true
          })
          this.tableTree()
          // this.$router.push('/dataset/home')
          this.$emit('switchComponent', { name: '' })
          this.$store.dispatch('dataset/setTable', null)
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

    closeTable() {
      this.editTable = false
      this.tableForm = {
        name: ''
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

    tableTree() {
      this.tableData = []
      if (this.currGroup.id) {
        loadTable({
          sort: 'type asc,create_time desc,name asc',
          sceneId: this.currGroup.id
        }).then(res => {
          this.tableData = res.data
        })
      }
    },

    nodeClick(data, node) {
      if (data.nodeType === 'panel') {
        this.currGroup = data
        // 加载视图数据
        this.$nextTick(() => {
          localStorage.setItem('canvasData', null)
          localStorage.setItem('canvasStyle', null)
          get('panel/group/findOne/' + data.id).then(response => {
            this.$store.commit('setComponentData', this.resetID(JSON.parse(response.data.panelData)))
            this.$store.commit('setCanvasStyle', JSON.parse(response.data.panelStyle))
          })
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

    clickAddData(param) {
      // console.log(param);
      switch (param.type) {
        case 'db':
          this.addDB()
          break
        case 'sql':
          this.$message(param.type)
          break
        case 'excel':
          this.$message(param.type)
          break
        case 'custom':
          this.$message(param.type)
          break
      }
    },

    beforeClickAddData(type) {
      return {
        'type': type
      }
    },

    addDB() {
      // this.$router.push({
      //   name: 'add_db',
      //   params: {
      //     scene: this.currGroup

      //   }
      // })
      this.$emit('switchComponent', { name: 'AddDB', param: this.currGroup })
    },

    sceneClick(data, node) {
      // console.log(data);
      this.$store.dispatch('dataset/setTable', null)
      this.$store.dispatch('dataset/setTable', data.id)
      // this.$router.push({
      //   name: 'table',
      //   params: {
      //     table: data
      //   }
      // })
      this.$emit('switchComponent', { name: 'ViewTable' })
    },

    refresh() {
      const path = this.$route.path
      if (path === '/dataset/table') {
        this.sceneMode = true
        const sceneId = this.$store.state.dataset.sceneData
        getScene(sceneId).then(res => {
          this.currGroup = res.data
        })
      }
    },
    panelDefaultClick(data, node) {
      console.log(data)
      console.log(node)
      this.$store.dispatch('panel/setPanelInfo', data)
      // 切换view
      this.$emit('switchComponent', { name: 'PanelView' })
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
      // 清空临时画布
      localStorage.setItem('canvasDataEditTmp', null)
      localStorage.setItem('canvasStyleEditTmp', null)
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
      data.forEach(item => {
        item.id = uuid.v1()
      })

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
