<template>
  <el-col class="tree-style">
    <!-- group -->
    <el-col v-if="!sceneMode">
      <el-row class="title-css">
        <span class="title-text">
          {{ $t('dataset.datalist') }}
        </span>
        <el-button icon="el-icon-plus" type="text" size="mini" style="float: right;" @click="add('group')">
          <!--          {{ $t('dataset.add_group') }}-->
        </el-button>
      </el-row>
      <el-divider />

      <!--      <el-row>-->
      <!--        <el-button type="primary" size="mini" @click="add('group')">-->
      <!--          {{ $t('dataset.add_group') }}-->
      <!--        </el-button>-->
      <!--        <el-button type="primary" size="mini" @click="add('scene')">-->
      <!--          {{ $t('dataset.add_scene') }}-->
      <!--        </el-button>-->
      <!--      </el-row>-->

      <el-row>
        <el-form>
          <el-form-item class="form-item">
            <el-input
              v-model="search"
              size="mini"
              :placeholder="$t('dataset.search')"
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
            <span v-if="data.type === 'group'" slot-scope="{ node, data }" class="custom-tree-node father">
              <span style="display: flex;flex: 1;width: 0;">
                <!--                <span v-if="data.type === 'scene'">-->
                <!--                  &lt;!&ndash;                  <el-button&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    icon="el-icon-folder-opened"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    type="text"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                    size="mini"&ndash;&gt;-->
                <!--                  &lt;!&ndash;                  />&ndash;&gt;-->
                <!--                  <svg-icon icon-class="scene" class="ds-icon-scene" />-->
                <!--                </span>-->
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
                        <span style="font-size: 13px;">{{ $t('dataset.group') }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-circle-plus">
                        <!--                        {{ $t('dataset.scene') }}-->
                        <el-dropdown size="small" placement="right-start" @command="clickAddData">
                          <!--                          <el-button type="primary" size="mini" plain>-->
                          <!--                            {{ $t('dataset.add_table') }}-->
                          <!--                          </el-button>-->
                          <span class="el-dropdown-link inner-dropdown-menu">
                            <span>
                              <!--                              <i class="el-icon-sort" />-->
                              <span style="font-size: 13px;">{{ $t('dataset.add_table') }}</span>
                            </span>
                            <i class="el-icon-arrow-right el-icon--right" />
                          </span>
                          <el-dropdown-menu slot="dropdown">
                            <el-dropdown-item :command="beforeClickAddData('db',data)">
                              <svg-icon icon-class="ds-db" class="ds-icon-db" />
                              {{ $t('dataset.db_data') }}
                            </el-dropdown-item>
                            <el-dropdown-item :command="beforeClickAddData('sql',data)">
                              <svg-icon icon-class="ds-sql" class="ds-icon-sql" />
                              {{ $t('dataset.sql_data') }}
                            </el-dropdown-item>
                            <el-dropdown-item :command="beforeClickAddData('excel',data)" :disabled="!kettleRunning">
                              <svg-icon icon-class="ds-excel" class="ds-icon-excel" />
                              {{ $t('dataset.excel_data') }}
                            </el-dropdown-item>
                            <el-dropdown-item :command="beforeClickAddData('custom',data)">
                              <svg-icon icon-class="ds-custom" class="ds-icon-custom" />
                              {{ $t('dataset.custom_data') }}
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </el-dropdown>
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
                        {{ $t('dataset.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-right" :command="beforeClickMore('move',data,node)">
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('delete',data,node)">
                        {{ $t('dataset.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
            <span v-else slot-scope="{ node, data }" class="custom-tree-node-list father">
              <span style="display: flex;flex: 1;width: 0;">
                <span>
                  <svg-icon v-if="data.type === 'db'" icon-class="ds-db" class="ds-icon-db" />
                  <svg-icon v-if="data.type === 'sql'" icon-class="ds-sql" class="ds-icon-sql" />
                  <svg-icon v-if="data.type === 'excel'" icon-class="ds-excel" class="ds-icon-excel" />
                  <svg-icon v-if="data.type === 'custom'" icon-class="ds-custom" class="ds-icon-custom" />
                </span>
                <span v-if="data.type === 'db' || data.type === 'sql'">
                  <span v-if="data.mode === 0" style="margin-left: 6px"><i class="el-icon-s-operation" /></span>
                  <span v-if="data.mode === 1" style="margin-left: 6px"><i class="el-icon-alarm-clock" /></span>
                </span>
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
                      <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('editTable',data,node)">
                        {{ $t('dataset.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-right" :command="beforeClickMore('moveDs',data,node)">
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('deleteTable',data,node)">
                        {{ $t('dataset.delete') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
              </span>
            </span>
          </el-tree>
        </div>
      </el-col>

      <el-dialog v-dialogDrag :title="dialogTitle" :visible="editGroup" :show-close="false" width="30%">
        <el-form ref="groupForm" :model="groupForm" :rules="groupFormRules" @keypress.enter.native="saveGroup(groupForm)">
          <el-form-item :label="$t('commons.name')" prop="name">
            <el-input v-model="groupForm.name" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button size="mini" @click="close()">{{ $t('dataset.cancel') }}</el-button>
          <el-button type="primary" size="mini" @click="saveGroup(groupForm)">{{ $t('dataset.confirm') }}
          </el-button>
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
    <!--          &lt;!&ndash;          {{ $t('dataset.back') }}&ndash;&gt;-->
    <!--        </el-button>-->
    <!--      </el-row>-->
    <!--      <el-divider />-->
    <!--      <el-row>-->
    <!--        <el-dropdown style="margin-right: 10px;" size="small" trigger="click" @command="clickAddData">-->
    <!--          <el-button type="primary" size="mini" plain>-->
    <!--            {{ $t('dataset.add_table') }}-->
    <!--          </el-button>-->
    <!--          <el-dropdown-menu slot="dropdown">-->
    <!--            <el-dropdown-item :command="beforeClickAddData('db')">-->
    <!--              <svg-icon icon-class="ds-db" class="ds-icon-db" />-->
    <!--              {{ $t('dataset.db_data') }}-->
    <!--            </el-dropdown-item>-->
    <!--            <el-dropdown-item :command="beforeClickAddData('sql')">-->
    <!--              <svg-icon icon-class="ds-sql" class="ds-icon-sql" />-->
    <!--              {{ $t('dataset.sql_data') }}-->
    <!--            </el-dropdown-item>-->
    <!--            <el-dropdown-item :command="beforeClickAddData('excel')">-->
    <!--              <svg-icon icon-class="ds-excel" class="ds-icon-excel" />-->
    <!--              {{ $t('dataset.excel_data') }}-->
    <!--            </el-dropdown-item>-->
    <!--            <el-dropdown-item :command="beforeClickAddData('custom')">-->
    <!--              <svg-icon icon-class="ds-custom" class="ds-icon-custom" />-->
    <!--              {{ $t('dataset.custom_data') }}-->
    <!--            </el-dropdown-item>-->
    <!--          </el-dropdown-menu>-->
    <!--        </el-dropdown>-->
    <!--        &lt;!&ndash; <el-button type="primary" size="mini" plain>-->
    <!--          {{ $t('dataset.update') }}-->
    <!--        </el-button>-->
    <!--        <el-button type="primary" size="mini" plain>-->
    <!--          {{ $t('dataset.process') }}-->
    <!--        </el-button> &ndash;&gt;-->
    <!--      </el-row>-->
    <!--      <el-row>-->
    <!--        <el-form>-->
    <!--          <el-form-item class="form-item">-->
    <!--            <el-input-->
    <!--              v-model="search"-->
    <!--              size="mini"-->
    <!--              :placeholder="$t('dataset.search')"-->
    <!--              prefix-icon="el-icon-search"-->
    <!--              clearable-->
    <!--            />-->
    <!--          </el-form-item>-->
    <!--        </el-form>-->
    <!--      </el-row>-->
    <!--      <span v-show="false">{{ sceneData }}</span>-->
    <!--      <el-tree-->
    <!--        :data="tableData"-->
    <!--        node-key="id"-->
    <!--        :expand-on-click-node="true"-->
    <!--        class="tree-list"-->
    <!--        highlight-current-->
    <!--        @node-click="sceneClick"-->
    <!--      >-->
    <!--        <span slot-scope="{ node, data }" class="custom-tree-node-list father">-->
    <!--          <span style="display: flex;flex: 1;width: 0;">-->
    <!--            <span>-->
    <!--              <svg-icon v-if="data.type === 'db'" icon-class="ds-db" class="ds-icon-db" />-->
    <!--              <svg-icon v-if="data.type === 'sql'" icon-class="ds-sql" class="ds-icon-sql" />-->
    <!--              <svg-icon v-if="data.type === 'excel'" icon-class="ds-excel" class="ds-icon-excel" />-->
    <!--              <svg-icon v-if="data.type === 'custom'" icon-class="ds-custom" class="ds-icon-custom" />-->
    <!--            </span>-->
    <!--            <span v-if="data.type === 'db' || data.type === 'sql'">-->
    <!--              <span v-if="data.mode === 0" style="margin-left: 6px"><i class="el-icon-s-operation" /></span>-->
    <!--              <span v-if="data.mode === 1" style="margin-left: 6px"><i class="el-icon-alarm-clock" /></span>-->
    <!--            </span>-->
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
    <!--                  <el-dropdown-item icon="el-icon-edit-outline" :command="beforeClickMore('editTable',data,node)">-->
    <!--                    {{ $t('dataset.rename') }}-->
    <!--                  </el-dropdown-item>-->
    <!--                  <el-dropdown-item icon="el-icon-right" :command="beforeClickMore('moveDs',data,node)">-->
    <!--                    {{ $t('dataset.move_to') }}-->
    <!--                  </el-dropdown-item>-->
    <!--                  <el-dropdown-item icon="el-icon-delete" :command="beforeClickMore('deleteTable',data,node)">-->
    <!--                    {{ $t('dataset.delete') }}-->
    <!--                  </el-dropdown-item>-->
    <!--                </el-dropdown-menu>-->
    <!--              </el-dropdown>-->
    <!--            </span>-->
    <!--          </span>-->
    <!--        </span>-->
    <!--      </el-tree>-->
    <!--    </el-col>-->

    <el-dialog v-dialogDrag :title="$t('dataset.table')" :visible="editTable" :show-close="false" width="30%">
      <el-form ref="tableForm" :model="tableForm" :rules="tableFormRules" @keypress.enter.native="saveTable(tableForm)">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="tableForm.name" />
        </el-form-item>
        <!--          <el-form-item :label="$t('dataset.mode')" prop="mode">-->
        <!--            <el-radio v-model="tableForm.mode" label="0">{{ $t('dataset.direct_connect') }}</el-radio>-->
        <!--            <el-radio v-model="tableForm.mode" label="1">{{ $t('dataset.sync_data') }}</el-radio>-->
        <!--          </el-form-item>-->
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeTable()">{{ $t('dataset.cancel') }}</el-button>
        <el-button type="primary" size="mini" @click="saveTable(tableForm)">{{ $t('dataset.confirm') }}
        </el-button>
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

    <!--移动数据集-->
    <el-dialog v-dialogDrag :title="moveDialogTitle" :visible="moveDs" :show-close="false" width="30%" class="dialog-css">
      <ds-move-selector :item="dsForm" @targetDs="targetDs" />
      <div slot="footer" class="dialog-footer">
        <el-button size="mini" @click="closeMoveDs()">{{ $t('dataset.cancel') }}</el-button>
        <el-button :disabled="dsMoveConfirmDisabled" type="primary" size="mini" @click="saveMoveDs(tDs)">{{ $t('dataset.confirm') }}
        </el-button>
      </div>
    </el-dialog>
  </el-col>
</template>

<script>
import { loadTable, getScene, addGroup, delGroup, addTable, delTable, post, isKettleRunning} from '@/api/dataset/dataset'
import GroupMoveSelector from './GroupMoveSelector'
import DsMoveSelector from './DsMoveSelector'

export default {
  name: 'Group',
  components: { GroupMoveSelector, DsMoveSelector },
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
      tData: [],
      tableData: [],
      tables: [],
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
      dsForm: {
        name: '',
        pid: '0',
        level: 0,
        type: '',
        children: [],
        sort: 'type desc,name asc'
      },
      tableForm: {
        name: '',
        mode: '',
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
      moveGroup: false,
      tGroup: {},
      moveDs: false,
      tDs: {},
      groupMoveConfirmDisabled: true,
      dsMoveConfirmDisabled: true,
      moveDialogTitle: '',
      treeProps: {
        label: 'name',
        children: 'children',
        isLeaf: 'isLeaf',
        id: 'id',
        parentId: 'pid'
      },
      isTreeSearch: false,
      kettleRunning: false
    }
  },
  computed: {
    // sceneData: function() {
    //   // this.tableTree()
    //   // console.log(this.$store.state.dataset.sceneData)
    //   this.refreshNodeBy(this.currGroup.id)
    //   return this.$store.state.dataset.sceneData
    // }
  },
  created() {
    this.kettleState()
  },
  watch: {
    search(val) {
      // if (val && val !== '') {
      //   this.tableData = JSON.parse(JSON.stringify(this.tables.filter(ele => { return ele.name.includes(val) })))
      // } else {
      //   this.tableData = JSON.parse(JSON.stringify(this.tables))
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
    }
  },
  mounted() {
    this.treeNode(this.groupForm)
    this.refresh()
    // this.tableTree()
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
    kettleState() {
      isKettleRunning().then(res => {
        this.kettleRunning = res.data
      })
    },
    clickMore(param) {
      // console.log(param)
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
        case 'editTable':
          this.editTable = true
          this.tableForm = JSON.parse(JSON.stringify(param.data))
          this.tableForm.mode = this.tableForm.mode + ''
          break
        case 'deleteTable':
          this.deleteTable(param.data)
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
          this.dialogTitle = this.$t('dataset.group')
          break
        case 'scene':
          this.dialogTitle = this.$t('dataset.scene')
          break
      }
      this.groupForm.type = type
      this.editGroup = true
    },

    saveGroup(group) {
      // console.log(group);
      this.$refs['groupForm'].validate((valid) => {
        if (valid) {
          addGroup(group).then(res => {
            this.close()
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            // this.tree(this.groupForm)
            this.refreshNodeBy(group.pid)
          })
        } else {
          // this.$message({
          //   message: this.$t('commons.input_error'),
          //   type: 'error',
          //   showClose: true
          // })
          return false
        }
      })
    },

    saveTable(table) {
      //   console.log(table)
      table.mode = parseInt(table.mode)
      this.$refs['tableForm'].validate((valid) => {
        if (valid) {
          table.isRename = true
          addTable(table).then(response => {
            this.closeTable()
            this.$message({
              message: this.$t('dataset.save_success'),
              type: 'success',
              showClose: true
            })
            // this.tableTree()
            this.refreshNodeBy(table.sceneId)
            // this.$router.push('/dataset/home')
            // this.$emit('switchComponent', { name: 'ViewTable', param: table.id })
            this.$store.dispatch('dataset/setTable', new Date().getTime())
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
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('dataset.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        delGroup(data.id).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('dataset.delete_success'),
            showClose: true
          })
          // this.tree(this.groupForm)
          this.refreshNodeBy(data.pid)
        })
      }).catch(() => {
      })
    },

    deleteTable(data) {
      this.$confirm(this.$t('dataset.confirm_delete'), this.$t('dataset.tips'), {
        confirmButtonText: this.$t('dataset.confirm'),
        cancelButtonText: this.$t('dataset.cancel'),
        type: 'warning'
      }).then(() => {
        delTable(data.id).then(response => {
          this.$message({
            type: 'success',
            message: this.$t('dataset.delete_success'),
            showClose: true
          })
          // this.tableTree()
          this.refreshNodeBy(data.sceneId)
          // this.$router.push('/dataset/home')
          // this.$emit('switchComponent', { name: '' })
          this.$store.dispatch('dataset/setTable', new Date().getTime())
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

    // tree(group) {
    //   groupTree(group).then(res => {
    //     this.tData = res.data
    //   })
    // },

    treeNode(group) {
      post('/dataset/group/treeNode', group).then(res => {
        this.tData = res.data
      })
    },

    tableTree() {
      this.tables = []
      this.tableData = []
      if (this.currGroup.id) {
        loadTable({
          sort: 'type asc,create_time desc,name asc',
          sceneId: this.currGroup.id
        }).then(res => {
          this.tables = res.data
          this.tableData = JSON.parse(JSON.stringify(this.tables))
        })
      }
    },

    nodeClick(data, node) {
      // console.log(data);
      // console.log(node);
      // if (data.type === 'scene') {
      //   this.sceneMode = true
      //   this.currGroup = data
      //   this.$store.dispatch('dataset/setSceneData', this.currGroup.id)
      // }

      if (data.type !== 'group') {
        this.$emit('switchComponent', { name: 'ViewTable', param: data })
      }
      // if (node.expanded) {
      //   this.expandedArray.push(data.id)
      // } else {
      //   const index = this.expandedArray.indexOf(data.id)
      //   if (index > -1) {
      //     this.expandedArray.splice(index, 1)
      //   }
      // }
      // console.log(this.expandedArray);
    },

    back() {
      this.sceneMode = false
      this.$emit('switchComponent', { name: '' })
    },

    clickAddData(param) {
      // console.log(param);
      this.currGroup = param.data
      this.$store.dispatch('dataset/setSceneData', this.currGroup.id)
      switch (param.type) {
        case 'db':
          this.addData('AddDB')
          break
        case 'sql':
          this.addData('AddSQL')
          break
        case 'excel':
          this.addData('AddExcel')
          break
        case 'custom':
          this.addData('AddCustom')
          break
      }
    },

    beforeClickAddData(type, data) {
      return {
        'type': type,
        'data': data
      }
    },

    addData(name) {
      this.$emit('switchComponent', { name: name, param: this.currGroup })
    },

    sceneClick(data, node) {
      this.$emit('switchComponent', { name: 'ViewTable', param: data.id })
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
      addGroup(this.groupForm).then(res => {
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
      this.dsForm.isRename = true
      addTable(this.dsForm).then(res => {
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

    loadNode(node, resolve) {
      if (!this.isTreeSearch) {
        if (node.level > 0) {
          this.tables = []
          this.tableData = []
          if (node.data.id) {
            post('/dataset/table/listAndGroup', {
              sort: 'type asc,name asc,create_time desc',
              sceneId: node.data.id
            }).then(res => {
              this.tables = res.data
              this.tableData = JSON.parse(JSON.stringify(this.tables))
              resolve(this.tableData)
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

    searchTree(val) {
      const queryCondition = {
        // withExtend: 'parent',
        // modelType: 'dataset',
        name: val
      }
      // authModel(queryCondition).then(res => {
      //   // this.highlights(res.data)
      //   this.tData = this.buildTree(res.data)
      //   // console.log(this.tData)
      // })

      post('/dataset/table/search', queryCondition).then(res => {
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

  .tree-list>>>.el-tree-node__expand-icon.is-leaf{
    display: none;
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

  .dialog-css >>> .el-dialog__body {
    padding: 10px 20px 20px;
  }

  .inner-dropdown-menu{
    display: flex;
    justify-content: space-between;
    align-items: center;
    width: 100%
  }
  .tree-style {
    padding: 10px 15px;
    height: 100%;
    overflow-y: auto;
  }
</style>
