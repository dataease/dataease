<template>
  <el-col class="tree-style de-dataset-search">
    <!-- group -->
    <el-col>
      <div style="margin: 6px 0 16px 0" class="title-css">
        <span class="title-text">
          {{ $t('dataset.datalist') }}
        </span>
        <el-dropdown
          size="small"
          placement="bottom-start"
          @command="(type) => clickAddData(type)"
        >
          <span class="el-dropdown-link">
            <i class="el-icon-plus" @click.stop />
          </span>
          <el-dropdown-menu
            slot="dropdown"
            class="de-dataset-dropdown de-card-dropdown"
          >
            <el-dropdown-item command="db">
              <svg-icon icon-class="ds-db" class="ds-icon-db" />
              {{ $t('dataset.db_data') }}
            </el-dropdown-item>
            <el-dropdown-item command="sql">
              <svg-icon icon-class="ds-sql" class="ds-icon-sql" />
              {{ $t('dataset.sql_data') }}
            </el-dropdown-item>
            <el-dropdown-item
              command="excel"
              :disabled="!kettleRunning && engineMode !== 'simple'"
            >
              <svg-icon icon-class="ds-excel" class="ds-icon-excel" />
              {{ $t('dataset.excel_data') }}
            </el-dropdown-item>
            <el-dropdown-item command="union">
              <svg-icon icon-class="ds-union" class="ds-icon-union" />
              {{ $t('dataset.union_data') }}
            </el-dropdown-item>
            <el-dropdown-item command="api">
              <svg-icon icon-class="ds-api" class="ds-icon-api" />
              {{ $t('dataset.api_data') }}
            </el-dropdown-item>
            <el-dropdown-item class="de-top-border" command="group">
              <svg-icon icon-class="scene" class="ds-icon-db" />
              {{ $t('deDataset.new_folder') }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <el-row style="margin-bottom: 6px">
        <el-input
          v-model="filterText"
          size="small"
          :placeholder="$t('deDataset.search_by_name')"
          prefix-icon="el-icon-search"
          clearable
          class="main-area-input"
        >
          <el-select
            slot="append"
            v-model="searchType"
            :placeholder="searchMap[searchType]"
          >
            <el-option :label="$t('commons.all')" value="all" />
            <el-option :label="$t('commons.folder')" value="folder" />
          </el-select>
        </el-input>
      </el-row>
      <el-col class="custom-tree-container de-tree">
        <div class="block">
          <div v-if="!tData.length && !treeLoading" class="no-tdata">
            {{ $t('deDataset.no_dataset_click') }}
            <span class="no-tdata-new" @click="() => clickAdd()">{{
              $t('deDataset.create')
            }}</span>
          </div>
          <el-tree
            v-else
            ref="datasetTreeRef"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            highlight-current
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            @node-click="nodeClick"
          >
            <span
              v-if="data.modelInnerType === 'group'"
              slot-scope="{ node, data }"
              class="custom-tree-node father"
            >
              <span style="display: flex; flex: 1; width: 0">
                <span>
                  <svg-icon icon-class="scene" />
                </span>
                <span
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  :title="data.name"
                >{{ data.name }}</span>
              </span>
              <span
                v-if="hasDataPermission('manage', data.privileges)"
                class="child"
              >
                <span v-if="data.modelInnerType === 'group'" @click.stop>
                  <el-dropdown
                    size="small"
                    placement="bottom-start"
                    @command="(type) => clickAddData(type, data)"
                  >
                    <span class="el-dropdown-link">
                      <i class="el-icon-plus" @click.stop />
                    </span>
                    <el-dropdown-menu
                      slot="dropdown"
                      class="de-dataset-dropdown de-card-dropdown"
                    >
                      <el-dropdown-item command="db">
                        <svg-icon icon-class="ds-db" class="ds-icon-db" />
                        {{ $t('dataset.db_data') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="sql">
                        <svg-icon icon-class="ds-sql" class="ds-icon-sql" />
                        {{ $t('dataset.sql_data') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        command="excel"
                        :disabled="!kettleRunning && engineMode !== 'simple'"
                      >
                        <svg-icon icon-class="ds-excel" class="ds-icon-excel" />
                        {{ $t('dataset.excel_data') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="union">
                        <svg-icon icon-class="ds-union" class="ds-icon-union" />
                        {{ $t('dataset.union_data') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="api">
                        <svg-icon icon-class="ds-api" class="ds-icon-api" />
                        {{ $t('dataset.api_data') }}
                      </el-dropdown-item>
                      <el-dropdown-item class="de-top-border" command="group">
                        <svg-icon icon-class="scene" class="ds-icon-db" />
                        {{ $t('deDataset.new_folder') }}
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
                <span style="margin-left: 12px" @click.stop>
                  <el-dropdown
                    trigger="click"
                    size="small"
                    placement="bottom-start"
                    @command="(type) => clickMore(type, data, node)"
                  >
                    <span class="el-dropdown-link">
                      <el-button icon="el-icon-more" type="text" size="small" />
                    </span>
                    <el-dropdown-menu class="de-card-dropdown" slot="dropdown">
                      <el-dropdown-item command="rename">
                        <svg-icon icon-class="de-ds-rename" />
                        {{ $t('dataset.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="move">
                        <svg-icon icon-class="de-ds-move" />
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="delete">
                        <svg-icon icon-class="de-ds-trash" />
                        {{ $t('dataset.delete') }}
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
              <span style="display: flex; flex: 1; width: 0">
                <span>
                  <svg-icon
                    v-if="data.modelInnerType === 'db'"
                    icon-class="ds-db"
                    class="ds-icon-db"
                  />
                  <svg-icon
                    v-if="data.modelInnerType === 'sql'"
                    icon-class="ds-sql"
                    class="ds-icon-sql"
                  />
                  <svg-icon
                    v-if="data.modelInnerType === 'excel'"
                    icon-class="ds-excel"
                    class="ds-icon-excel"
                  />
                  <svg-icon
                    v-if="data.modelInnerType === 'custom'"
                    icon-class="ds-custom"
                    class="ds-icon-custom"
                  />
                  <svg-icon
                    v-if="data.modelInnerType === 'union'"
                    icon-class="ds-union"
                    class="ds-icon-union"
                  />
                  <svg-icon
                    v-if="data.modelInnerType === 'api'"
                    icon-class="ds-api"
                    class="ds-icon-api"
                  />
                </span>
                <span v-if="['db', 'sql'].includes(data.modelInnerType)">
                  <span
                    v-if="data.mode === 0"
                    style="margin-left: 6px"
                  ><i
                    class="el-icon-s-operation"
                  /></span>
                  <span
                    v-if="data.mode === 1"
                    style="margin-left: 6px"
                  ><i
                    class="el-icon-alarm-clock"
                  /></span>
                </span>
                <span
                  style="
                    margin-left: 6px;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                  "
                  :class="[
                    {
                      'de-fill-block': !['db', 'sql'].includes(
                        data.modelInnerType
                      )
                    }
                  ]"
                  :title="data.name"
                >{{ data.name }}</span>
              </span>
              <span
                v-if="hasDataPermission('manage', data.privileges)"
                class="child"
              >
                <span style="margin-left: 12px" @click.stop>
                  <el-dropdown
                    trigger="click"
                    size="small"
                    placement="bottom-start"
                    @command="(type) => clickMore(type, data, node)"
                  >
                    <span class="el-dropdown-link">
                      <el-button icon="el-icon-more" type="text" size="small" />
                    </span>
                    <el-dropdown-menu class="de-card-dropdown" slot="dropdown">
                      <el-dropdown-item command="editTable">
                        <svg-icon icon-class="de-ds-rename" />
                        {{ $t('dataset.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="moveDs">
                        <svg-icon icon-class="de-ds-move" />
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item command="deleteTable">
                        <svg-icon icon-class="de-ds-trash" />
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

      <el-dialog
        :title="dialogTitle"
        class="de-dialog-form"
        :visible.sync="editGroup"
        width="600px"
      >
        <el-form
          ref="groupForm"
          class="de-form-item"
          :model="groupForm"
          :rules="groupFormRules"
          :before-close="close"
          @submit.native.prevent
          @keypress.enter.native="saveGroup(groupForm)"
        >
          <el-form-item :label="$t('deDataset.folder_name')" prop="name">
            <el-input v-model.trim="groupForm.name" placeholder="请输入文件夹名称" />
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <deBtn secondary @click="close()">{{ $t('dataset.cancel') }}</deBtn>
          <deBtn
            type="primary"
            @click="saveGroup(groupForm)"
          >{{ $t('dataset.confirm') }}
          </deBtn>
        </div>
      </el-dialog>
    </el-col>

    <el-dialog
      :title="$t('deDataset.edit_dataset')"
      :visible.sync="editTable"
      class="de-dialog-form"
      width="600px"
    >
      <el-form
        ref="tableForm"
        :model="tableForm"
        class="de-form-item"
        :rules="tableFormRules"
        @submit.native.prevent
        @keypress.enter.native="saveTable(tableForm)"
      >
        <el-form-item :label="$t('dataset.name')" prop="name">
          <el-input v-model="tableForm.name" />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <deBtn secondary @click="closeTable()">{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn
          type="primary"
          @click="saveTable(tableForm)"
        >{{ $t('dataset.confirm') }}
        </deBtn>
      </div>
    </el-dialog>

    <!--移动分组-->
    <el-drawer
      :visible.sync="moveGroup"
      custom-class="user-drawer sql-dataset-drawer"
      size="600px"
      direction="rtl"
    >
      <template slot="title">
        {{ $t('dataset.m1') }}
        <span :title="moveDialogTitle" class="text-overflow">{{
          moveDialogTitle
        }}</span>
        {{ $t('dataset.m2') }}
      </template>
      <group-move-selector
        move-dir
        :item="groupForm"
        @targetGroup="targetGroup"
      />
      <div class="de-foot">
        <deBtn secondary @click="closeMoveGroup()">{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn
          :disabled="groupMoveConfirmDisabled"
          type="primary"
          @click="saveMoveGroup(tGroup)"
        >{{ $t('dataset.confirm') }}
        </deBtn>
      </div>
    </el-drawer>

    <!--移动数据集-->
    <el-drawer
      :visible.sync="moveDs"
      custom-class="user-drawer sql-dataset-drawer"
      size="600px"
      direction="rtl"
    >
      <template slot="title">
        {{ $t('dataset.m1') }}
        <span :title="moveDialogTitle" class="text-overflow">{{
          moveDialogTitle
        }}</span>
        {{ $t('dataset.m2') }}
      </template>
      <group-move-selector :item="groupForm" @targetGroup="targetDs" />
      <div class="de-foot">
        <deBtn secondary @click="closeMoveDs()">{{
          $t('dataset.cancel')
        }}</deBtn>
        <deBtn
          :disabled="dsMoveConfirmDisabled"
          type="primary"
          @click="saveMoveDs(tDs)"
        >{{ $t('dataset.confirm') }}
        </deBtn>
      </div>
    </el-drawer>

    <!-- 新增数据集文件夹 -->
    <CreatDsGroup ref="CreatDsGroup" />
  </el-col>
</template>

<script>
import {
  loadTable,
  getScene,
  addGroup,
  delGroup,
  delTable,
  post,
  isKettleRunning,
  alter
} from '@/api/dataset/dataset'
import GroupMoveSelector from './GroupMoveSelector'
import CreatDsGroup from './CreatDsGroup'
import { queryAuthModel } from '@/api/authModel/authModel'
import { engineMode } from '@/api/system/engine'
import _ from 'lodash'
import msgCfm from '@/components/msgCfm/index'

export default {
  name: 'Group',
  components: { GroupMoveSelector, CreatDsGroup },
  mixins: [msgCfm],
  props: {
    saveStatus: {
      type: Object,
      required: false,
      default: null
    },
    currentNodeId: {
      type: String,
      default: ''
    }
  },
  data() {
    return {
      sceneMode: false,
      treeLoading: false,
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
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'change'
          },
          {
            max: 50,
            message: this.$t('commons.char_can_not_more_50'),
            trigger: 'change'
          },
          { required: true, trigger: 'blur', validator: this.filedValidator }
        ]
      },
      tableFormRules: {
        name: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'change'
          },
          {
            max: 50,
            message: this.$t('commons.char_can_not_more_50'),
            trigger: 'change'
          },
          { required: true, trigger: 'blur', validator: this.filedValidator }
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
      kettleRunning: false,
      engineMode: 'local',
      searchPids: [], // 查询命中的pid
      filterText: '',
      fileList: [],
      originName: '',
      searchType: 'all',
      searchMap: {
        all: this.$t('commons.all'),
        folder: this.$t('commons.folder')
      }
    }
  },
  computed: {
    hideCustomDs: function() {
      return this.$store.getters.hideCustomDs
    }
  },
  watch: {
    saveStatus() {
      this.treeNode()
    },
    filterText() {
      this.loadTree()
    },
    searchType(val) {
      this.searchPids = []
      this.$refs.datasetTreeRef.filter(this.filterText)
    }
  },
  created() {
    this.kettleState()
    engineMode().then((res) => {
      this.engineMode = res.data
    })
  },
  mounted() {
    const { id, name } = this.$route.params
    this.treeLoading = true
    queryAuthModel({ modelType: 'dataset' }, true)
      .then((res) => {
        localStorage.setItem('dataset-tree', JSON.stringify(res.data))
        this.tData = res.data || []
        this.$nextTick(() => {
          this.$refs.datasetTreeRef?.filter(this.filterText)
          if (id && name.includes(this.filterText)) {
            this.dfsTableData(this.tData, id)
          } else {
            const currentNodeId = sessionStorage.getItem('dataset-current-node')
            if (currentNodeId) {
              sessionStorage.setItem('dataset-current-node', '')
              this.dfsTableData(this.tData, currentNodeId)
            }
          }
        })
      })
      .finally(() => {
        this.treeLoading = false
      })
    this.refresh()
  },
  beforeDestroy() {
    sessionStorage.setItem('dataset-current-node', this.currentNodeId)
  },
  methods: {
    dfsTableData(arr, id) {
      arr.some((ele) => {
        if (ele.id === id) {
          this.$refs.datasetTreeRef?.setCurrentNode(ele)
          this.nodeClick(ele)
          this.expandedArray.push(id)
          return true
        } else if (ele.children?.length) {
          this.dfsTableData(ele.children, id)
        }
        return false
      })
    },
    nameRepeat(value) {
      if (!this.fileList || this.fileList.length === 0) {
        return false
      }
      // 编辑场景 不能 因为名称重复而报错
      if (
        (this.groupForm.id || this.tableForm.id) &&
        this.originName === value
      ) {
        return false
      }
      return this.fileList.some((role) => role === value)
    },
    filedValidator(rule, value, callback) {
      if (this.nameRepeat(value)) {
        callback(
          new Error(
            this.$t(
              this.editGroup
                ? 'deDataset.name_already_exists'
                : 'deDataset.already_exists'
            )
          )
        )
      } else {
        callback()
      }
    },
    clickAdd(param) {
      this.dialogTitle = this.$t('deDataset.new_folder')
      if (!param || !param.id) {
        this.fileList = (this.tData || []).map((ele) => ele.label)
        this.add('group')
        return
      }
      this.fileList = (param?.children || []).map((ele) => ele.label)
      this.add(param.modelInnerType)
      this.groupForm.pid = param.id
      this.groupForm.level = param.level + 1
    },
    loadTree: _.debounce(function() {
      this.searchPids = []
      this.$refs.datasetTreeRef.filter(this.filterText)
    }, 600),
    kettleState() {
      isKettleRunning().then((res) => {
        this.kettleRunning = res.data
      })
    },
    clickMore(type, data, node) {
      switch (type) {
        case 'rename':
          this.originName = data.label
          this.dialogTitle = this.$t('编辑文件夹')
          this.dfsTdata(this.tData, data.id)
          this.add(data.modelInnerType)
          this.groupForm = JSON.parse(JSON.stringify(data))
          break
        case 'move':
          this.moveTo(data)
          this.groupForm = JSON.parse(JSON.stringify(data))
          break
        case 'moveDs':
          this.moveToDs(data)
          this.dsForm = JSON.parse(JSON.stringify(data))
          break
        case 'delete':
          this.delete(data)
          break
        case 'editTable':
          this.editTable = true
          this.originName = data.label
          this.dfsTdata(this.tData, data.id)
          this.tableForm = JSON.parse(JSON.stringify(data))
          this.tableForm.mode = this.tableForm.mode + ''
          break
        case 'deleteTable':
          this.deleteTable(data)
          break
      }
    },
    dfsTdata(arr, id) {
      arr.some((ele) => {
        if (ele.id === id) {
          this.fileList = arr.map((item) => item.label)
          return true
        } else if (ele.children?.length) {
          this.dfsTdata(ele.children, id)
        }
        return false
      })
    },
    add(type) {
      this.groupForm.type = type
      this.editGroup = true
    },
    saveGroup(group) {
      this.$refs['groupForm'].validate((valid) => {
        if (valid) {
          addGroup(group).then((res) => {
            this.close()
            this.openMessageSuccess('dataset.save_success')
            this.expandedArray.push(group.pid)
            this.treeNode()
          })
        } else {
          return false
        }
      })
    },

    saveTable(table) {
      table.mode = parseInt(table.mode)
      const _this = this
      this.$refs['tableForm'].validate((valid) => {
        if (valid) {
          table.isRename = true
          table.sceneId = table.pid
          alter(table).then((response) => {
            this.closeTable()
            this.openMessageSuccess('dataset.save_success')
            _this.expandedArray.push(table.sceneId)
            _this.$refs.datasetTreeRef.setCurrentKey(table.id)
            _this.treeNode()
            this.$emit('switchComponent', { name: '' })
          })
        } else {
          return false
        }
      })
    },

    delete(data) {
      this.$confirm(
        this.$t('dataset.confirm_delete'),
        this.$t('dataset.tips'),
        {
          confirmButtonText: this.$t('dataset.confirm'),
          cancelButtonText: this.$t('dataset.cancel'),
          type: 'warning'
        }
      )
        .then(() => {
          delGroup(data.id).then((response) => {
            this.openMessageSuccess('dataset.delete_success')
            this.treeNode()
            this.$emit('switchComponent', { name: '' })
          })
        })
        .catch(() => {})
    },

    deleteTable(data) {
      let confirm_delete_msg = ''
      if (data.modelInnerType === 'union' || data.modelInnerType === 'custom') {
        confirm_delete_msg = this.$t('dataset.confirm_delete')
      } else {
        confirm_delete_msg = this.$t('dataset.confirm_delete_msg')
      }
      const options = {
        title: '确定删除该数据集吗？',
        content: confirm_delete_msg,
        type: 'primary',
        confirmButtonText: this.$t('commons.confirm'),
        cb: () => {
          delTable(data.id).then((response) => {
            this.openMessageSuccess('dataset.delete_success')
            this.treeNode()
            this.$emit('switchComponent', { name: '' })
            this.$store.dispatch('dataset/setTable', new Date().getTime())
          })
        }
      }
      this.handlerConfirm(options)
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

    treeNode(cache) {
      const modelInfo = localStorage.getItem('dataset-tree')
      const userCache = modelInfo && cache
      if (userCache) {
        this.tData = JSON.parse(modelInfo)
      }
      this.treeLoading = true
      queryAuthModel({ modelType: 'dataset' }, !userCache)
        .then((res) => {
          localStorage.setItem('dataset-tree', JSON.stringify(res.data))
          if (!userCache) {
            this.tData = res.data || []
          }
          this.$nextTick(() => {
            this.$refs.datasetTreeRef?.filter(this.filterText)
          })
        })
        .finally(() => {
          this.treeLoading = false
        })
    },

    tableTree() {
      this.tables = []
      this.tableData = []
      if (this.currGroup.id) {
        loadTable({
          sort: 'type asc,create_time desc,name asc',
          sceneId: this.currGroup.id
        }).then((res) => {
          this.tables = res.data
          this.tableData = JSON.parse(JSON.stringify(this.tables))
        })
      }
    },

    nodeClick(data, node) {
      if (data.modelInnerType !== 'group') {
        this.$emit('switchComponent', { name: 'ViewTable', param: data })
      }
    },

    back() {
      this.sceneMode = false
      this.$emit('switchComponent', { name: '' })
    },

    clickAddData(datasetType, param = {}) {
      this.currGroup = param
      if (datasetType === 'group') {
        this.clickAdd(param)
        return
      }
      this.$store.dispatch('dataset/setSceneData', this.currGroup.id)
      if (!this.tData?.length) {
        this.openMessageSuccess('deDataset.new_folder_first', 'error')
        return
      }
      switch (datasetType) {
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
        case 'union':
          this.addData('AddUnion')
          break
        case 'api':
          this.addData('AddApi')
          break
      }

      if (!param.id) {
        this.$refs.CreatDsGroup.init(datasetType)
        return
      }

      this.$router.push({
        path: '/dataset-form',
        query: {
          datasetType,
          sceneId: param.id
        }
      })
    },
    addData(name) {
      this.$emit('switchComponent', { name: name, param: this.currGroup })
    },
    refresh() {
      const path = this.$route.path
      if (path === '/dataset/table') {
        this.sceneMode = true
        const sceneId = this.$store.state.dataset.sceneData
        getScene(sceneId).then((res) => {
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
      this.moveDialogTitle = data.name
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
      addGroup(this.groupForm).then((res) => {
        this.openMessageSuccess('dept.move_success')
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
      this.moveDialogTitle = data.name
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
      this.dsForm.isRename = true
      alter(this.dsForm).then((res) => {
        this.closeMoveDs()
        this.expandedArray.push(newSceneId)
        this.treeNode()
        this.openMessageSuccess('移动成功')
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
            }).then((res) => {
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

    getTreeData(val) {
      if (val) {
        this.isTreeSearch = true
        this.searchTree(val)
      } else {
        this.isTreeSearch = false
        this.treeNode()
      }
    },
    filterNode(value, data) {
      if (!value) return true
      if (this.searchType === 'folder') {
        if (
          data.modelInnerType === 'group' &&
          data.label.indexOf(value) !== -1
        ) {
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
    }
  }
}
</script>

<style scoped lang="scss">
.de-fill-block {
  margin-left: 25px !important;
}

.custom-tree-container {
  margin-top: 10px;
  .no-tdata {
    text-align: center;
    margin-top: 80px;
    font-family: PingFang SC;
    font-size: 14px;
    color: var(--deTextSecondary, #646a73);
    font-weight: 400;
    .no-tdata-new {
      cursor: pointer;
      color: var(--primary, #3370ff);
    }
  }
}

.tree-list ::v-deep .el-tree-node__expand-icon.is-leaf {
  display: none;
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
  visibility: hidden;
}
.father:hover .child {
  visibility: visible;
}

.inner-dropdown-menu {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}
.tree-style {
  padding: 10px 15px;
  height: 100%;
  overflow-y: auto;
}
</style>
<style lang="scss">
.de-dataset-search {
  .main-area-input {
    .el-input-group__append {
      width: 70px;
      background: transparent;
      .el-input__inner {
        padding-left: 12px;
      }
    }
  }
  .title-css {
    display: flex;
    justify-content: space-between;
  }
  .el-icon-plus {
    width: 28px;
    height: 28px;
    line-height: 28px;
    text-align: center;
    font-size: 17px;
    color: #646a73;
    cursor: pointer;

    &:hover {
      background: rgba(31, 35, 41, 0.1);
      border-radius: 4px;
    }

    &:active {
      background: rgba(31, 35, 41, 0.2);
      border-radius: 4px;
    }
  }
}
.de-dataset-dropdown {
  .el-dropdown-menu__item {
    height: 40px;
    color: var(--deTextPrimary, #1f2329);
    display: flex;
    align-items: center;
    padding: 12px 9px;
    font-family: PingFang SC;
    font-size: 14px;
    font-weight: 400;
    .svg-icon {
      margin-right: 8.75px;
      width: 16.5px;
      height: 18px;
    }

    &:hover {
      background: rgba(31, 35, 41, 0.1);
      color: var(--deTextPrimary, #1f2329);
    }
  }
  .de-top-border {
    border-top: 1px solid rgba(31, 35, 41, 0.15);
  }
}
</style>
