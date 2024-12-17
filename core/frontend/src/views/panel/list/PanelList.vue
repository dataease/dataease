<template xmlns:el-col="http://www.w3.org/1999/html">
  <el-col style="padding: 1px 24px 16px 24px">
    <el-col>
      <el-row
        class="sticky-de-search-panel"
        style="margin-bottom: 10px"
      >
        <el-col :span="24">
          <el-input
            v-model="filterText"
            size="small"
            :placeholder="$t('commons.search')"
            prefix-icon="el-icon-search"
            clearable
            class="main-area-input"
          >
            <el-select
              slot="append"
              v-model="searchType"
              :placeholder="searchMap[searchType]"
            >
              <el-option
                :label="$t('commons.all')"
                value="all"
              />
              <el-option
                :label="$t('commons.folder')"
                value="folder"
              />
            </el-select>
          </el-input>
          <el-dropdown
            trigger="click"
            @command="sortTypeChange"
          >
            <div class="insert-filter filter-icon-span">
              <svg-icon
                v-show="curSortType.includes('asc')"
                class="opt-icon"
                icon-class="dv-sort-asc"
              />
              <svg-icon
                v-show="curSortType.includes('desc')"
                class="opt-icon"
                icon-class="dv-sort-desc"
              />
            </div>
            <template #dropdown>
              <el-dropdown-menu style="width: 160px">
                <el-dropdown-item
                  class="sort-type-normal"
                  :class="{ 'sort-type-checked': curSortType === 'time_asc' }"
                  :command="beforeClickItem('time_asc')"
                >
                  <span>按创建时间升序</span>
                  <i
                    style="margin-left: 4px; line-height: 32px"
                    class="el-icon-check"
                  />
                </el-dropdown-item>
                <el-dropdown-item
                  class="sort-type-normal"
                  :class="{ 'sort-type-checked': curSortType === 'time_desc' }"
                  :command="beforeClickItem('time_desc')"
                >
                  <span>按创建时间降序</span><i
                    style="margin-left: 4px; line-height: 32px"
                    class="el-icon-check"
                  />
                </el-dropdown-item>
                <el-divider class="custom-driver" />
                <el-dropdown-item
                  class="sort-type-normal"
                  :class="{ 'sort-type-checked': curSortType === 'name_asc' }"
                  :command="beforeClickItem('name_asc')"
                >
                  <span>按照名称升序</span><i
                    style="margin-left: 4px; line-height: 32px"
                    class="el-icon-check"
                  />
                </el-dropdown-item>
                <el-dropdown-item
                  class="sort-type-normal"
                  :class="{ 'sort-type-checked': curSortType === 'name_desc' }"
                  :command="beforeClickItem('name_desc')"
                >
                  <span>按照名称降序</span><i
                    style="margin-left: 4px; line-height: 32px"
                    class="el-icon-check"
                  />
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </el-col>
      </el-row>
      <el-row class="de-tree" v-if="hasDataPermission('view', rootDefaultAuth)">
        <span class="header-title">{{ $t('panel.default_panel') }}</span>
        <div class="block">
          <el-tree
            ref="default_panel_tree"
            :data="expandedData"
            node-key="id"
            :highlight-current="activeTree === 'system'"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-click="nodeClick"
          >
            <span
              slot-scope="{ node, data }"
              class="custom-tree-node father"
            >
              <span style="display: flex; flex: 1 1 0%; width: 0px">
                <span>
                  <svg-icon
                    v-if="!data.mobileLayout"
                    :icon-class="'panel-' + data.status"
                    class="ds-icon-scene"
                  />
                  <svg-icon
                    v-if="data.mobileLayout"
                    :icon-class="'panel-mobile-' + data.status"
                    class="ds-icon-scene"
                  />
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
                :title="$t('panel.to_top')"
                class="child"
                @click.stop
              >
                <el-button
                  icon="el-icon-upload2"
                  type="text"
                  size="small"
                  @click="toTop(data, node)"
                />
              </span>
              <span
                style="margin-left: 12px"
                class="child"
                @click.stop
              >
                <el-dropdown
                  v-if="hasDataPermission('manage', data.privileges)"
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
                      :command="beforeClickMore('rename', data, node)"
                    >
                      {{ $t('panel.rename') }}
                    </el-dropdown-item>
                    <el-dropdown-item
                      icon="el-icon-delete"
                      :command="beforeClickMore('delete', data, node)"
                    >
                      {{ $t('commons.cancel') + $t('emailtask.default') }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </el-dropdown>
              </span>
            </span>
          </el-tree>
          <p
            v-if="defaultData && defaultData.length > 3"
            class="default-expansion"
            @click="defaultExpansion = !defaultExpansion"
          >
            {{ defaultExpansion ? $t('panel.fold') : $t('panel.expand') }}
            <i
              :class="[
                defaultExpansion ? 'el-icon-arrow-up' : 'el-icon-arrow-down'
              ]"
            />
          </p>
        </div>
      </el-row>

      <el-row>
        <span class="header-title">
          {{ $t('panel.panel_list') }}
          <el-button
            v-if="hasDataPermission('manage', rootAuth) || isLicNone"
            style="float: right; padding-right: 7px; margin-top: -8px; height: 12px"
            icon="el-icon-plus"
            type="text"
            @click="showEditPanel(newFolder)"
          />
        </span>
      </el-row>
      <el-col class="custom-tree-container de-tree">
        <div class="block">
          <el-tree
            ref="panel_list_tree"
            :default-expanded-keys="expandedArray"
            :data="tData"
            node-key="id"
            :highlight-current="activeTree === 'self'"
            :expand-on-click-node="true"
            :filter-node-method="filterNode"
            @node-expand="nodeExpand"
            @node-collapse="nodeCollapse"
            @node-click="nodeClick"
          >
            <span
              slot-scope="{ node, data }"
              class="custom-tree-node-list father"
            >
              <span style="display: flex; flex: 1 1 0%; width: 0px">
                <span v-if="data.nodeType === 'panel'">
                  <svg-icon
                    v-if="!data.mobileLayout"
                    :icon-class="'panel-' + data.status"
                    class="ds-icon-scene"
                  />
                  <svg-icon
                    v-if="data.mobileLayout"
                    :icon-class="'panel-mobile-' + data.status"
                    class="ds-icon-scene"
                  />
                </span>
                <span v-if="data.nodeType === 'folder'">
                  <svg-icon icon-class="scene" />
                </span>
                <span
                  :class="data.status"
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
                <span
                  v-if="data.nodeType === 'folder'"
                  @click.stop
                >
                  <el-dropdown
                    trigger="click"
                    size="small"
                    @command="showEditPanel"
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
                        :command="beforeClickEdit('folder', 'new', data, node)"
                      >
                        <svg-icon icon-class="scene" />
                        <span style="margin-left: 5px">{{ $t('panel.groupAdd') }}</span>
                      </el-dropdown-item>
                      <el-dropdown-item
                        :command="beforeClickEdit('panel', 'new', data, node)"
                      >
                        <svg-icon
                          icon-class="panel"
                          class="ds-icon-scene"
                        />
                        <span>{{ $t('panel.panelAdd') }}</span>
                      </el-dropdown-item>
                    </el-dropdown-menu>
                  </el-dropdown>
                </span>
                <span
                  v-if="data.nodeType === 'panel'"
                  @click.stop
                >
                  <el-button
                    icon="el-icon-edit"
                    type="text"
                    size="small"
                    @click="edit(data, node)"
                  />
                </span>
                <span
                  style="margin-left: 12px"
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
                        v-if="data.nodeType === 'panel'"
                        icon="el-icon-edit"
                        :command="beforeClickMore('edit', data, node)"
                      >
                        {{ $t('panel.edit') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="data.nodeType === 'panel'"
                        icon="el-icon-share"
                        :command="beforeClickMore('share', data, node)"
                      >
                        {{ $t('panel.share') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="data.nodeType === 'panel'"
                        icon="el-icon-document-copy"
                        :command="beforeClickMore('copy', data, node)"
                      >
                        {{ $t('panel.copy') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-right"
                        :command="beforeClickMore('move', data, node)"
                      >
                        {{ $t('dataset.move_to') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="data.nodeType === 'panel'"
                        icon="el-icon-paperclip"
                        :command="beforeClickMore('link', data, node)"
                      >
                        {{ $t('panel.create_public_links') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        v-if="data.nodeType === 'panel' && hasDataPermission('manage', rootDefaultAuth)"
                        :disabled="data.isDefault"
                        icon="el-icon-copy-document"
                        :command="beforeClickMore('toDefaultPanel', data, node)"
                      >
                        {{ $t('panel.to_default_panel') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-edit-outline"
                        :command="beforeClickMore('rename', data, node)"
                      >
                        {{ $t('panel.rename') }}
                      </el-dropdown-item>
                      <el-dropdown-item
                        icon="el-icon-delete"
                        :command="beforeClickMore('delete', data, node)"
                      >
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

      <el-dialog
        v-dialogDrag
        :title="dialogTitle"
        :visible="editGroup"
        :show-close="false"
        width="500px"
      >
        <el-form
          ref="groupForm"
          :model="groupForm"
          :rules="groupFormRules"
          @keypress.enter.native="saveGroup(groupForm)"
        >
          <el-form-item
            :label="$t('commons.name')"
            prop="name"
          >
            <el-input v-model.trim="groupForm.name" />
          </el-form-item>
        </el-form>
        <div
          slot="footer"
          class="dialog-footer"
        >
          <el-button
            size="mini"
            @click="close()"
          >{{
            $t('panel.cancel')
          }}
          </el-button>
          <el-button
            type="primary"
            size="mini"
            @click="saveGroup(groupForm)"
          >{{ $t('panel.confirm') }}
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
        <grant-auth
          v-if="authVisible"
          :resource-id="authResourceId"
          @close-grant="closeGrant"
        />
      </el-dialog>

      <el-dialog
        v-dialogDrag
        :title="$t('panel.link_share')"
        :visible.sync="linkVisible"
        class="link-dialog"
        width="500px"
        @closed="removeLink"
      >
        <link-generate
          v-if="linkVisible"
          :resource-id="linkResourceId"
        />
      </el-dialog>
      <!--新建仪表板dialog-->
      <el-dialog
        v-dialogDrag
        :title="panelDialogTitle"
        :visible.sync="editPanel.visible"
        :show-close="true"
        width="600px"
      >
        <edit-panel
          v-if="editPanel.visible"
          :edit-panel-out="editPanel"
          @closeEditPanelDialog="closeEditPanelDialog"
          @newPanelSave="newPanelSave"
        />
      </el-dialog>

      <!--移动-->
      <el-dialog
        v-if="moveGroup"
        v-dialogDrag
        :title="moveDialogTitle"
        :visible="moveGroup"
        :show-close="false"
        width="30%"
        class="dialog-css"
      >
        <tree-selector
          :item="moveInfo"
          :t-data="tGroupData"
          @targetGroup="targetGroup"
        />
        <div
          slot="footer"
          class="dialog-footer"
        >
          <el-button
            size="mini"
            @click="closeMoveGroup()"
          >{{
            $t('dataset.cancel')
          }}
          </el-button>
          <el-button
            :disabled="groupMoveConfirmDisabled"
            type="primary"
            size="mini"
            @click="saveMoveGroup(tGroup)"
          >{{ $t('dataset.confirm') }}
          </el-button>
        </div>
      </el-dialog>
    </el-col>
  </el-col>
</template>

<script>
import GrantAuth from '../grantAuth'
import LinkGenerate from '@/views/link/generate'
import { uuid } from 'vue-uuid'
import bus from '@/utils/bus'
import EditPanel from './editPanel'
import {
  addGroup,
  defaultTree,
  delGroup,
  groupTree,
  initPanelData, panelMove,
  panelToTop,
  viewPanelLog
} from '@/api/panel/panel'
import { mapState } from 'vuex'
import { DEFAULT_COMMON_CANVAS_STYLE_STRING } from '@/views/panel/panel'
import TreeSelector from '@/components/treeSelector'
import { queryAuthModel } from '@/api/authModel/authModel'
import msgCfm from '@/components/msgCfm/index'
import { deepCopy, updateCacheTree } from '@/components/canvas/utils/utils'
import treeSort from '@/utils/treeSortUtils'

export default {
  name: 'PanelList',
  components: { GrantAuth, LinkGenerate, EditPanel, TreeSelector },
  mixins: [msgCfm],
  data() {
    return {
      rootAuth: '',
      rootDefaultAuth: 'view',
      originResourceTree: [],
      curSortType: 'time_desc',
      localSortParams: null,
      lastActiveDefaultPanelId: null, // 激活的节点 在这个节点下面动态放置子节点
      responseSource: 'panelQuery',
      defaultExpansion: false,
      clearLocalStorage: ['dataset-tree'],
      historyRequestId: null,
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
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'blur'
          }
        ]
      },
      tableFormRules: {
        name: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'blur'
          }
        ],
        mode: [
          {
            required: true,
            message: this.$t('commons.input_content'),
            trigger: 'blur'
          }
        ]
      },
      moveGroup: false,
      groupMoveConfirmDisabled: true,
      moveDialogTitle: '',
      moveInfo: {},
      tGroup: {},
      tGroupData: [], // 所有目录
      searchPids: [], // 查询命中的pid
      filterText: '',
      searchType: 'all',
      searchMap: {
        all: this.$t('commons.all'),
        folder: this.$t('commons.folder')
      },
      initLocalStorage: ['dataset']
    }
  },
  computed: {
    isLicNone() {
      return this.$store.state.lic.licStatus === 'no_record'
    },
    panelDialogTitle() {
      return this.editPanel.titlePre + this.editPanel.titleSuf
    },
    expandedData() {
      return (!this.defaultExpansion && this.defaultData && this.defaultData.length > 3)
        ? this.defaultData.slice(0, 3)
        : this.defaultData
    },
    ...mapState(['nowPanelTrackInfo'])
  },
  watch: {
    // 切换展示页面后 重新点击一下当前节点
    '$store.state.panel.mainActiveName': function(newVal, oldVal) {
      if (newVal === 'PanelMain' && this.lastActiveNodeData) {
        this.activeNodeAndClickOnly(this.lastActiveNodeData)
      }
    },
    filterText(val) {
      this.searchPids = []
      this.$refs.default_panel_tree?.filter(val)
      this.$refs.panel_list_tree?.filter(val)
    },
    searchType(val) {
      this.searchPids = []
      this.$refs.default_panel_tree?.filter(this.filterText)
      this.$refs.panel_list_tree?.filter(this.filterText)
    }
  },
  beforeDestroy() {
  },
  mounted() {
    this.loadInit()
    this.clearCanvas()
    this.initCache()
    const routerParam = this.$router.currentRoute.params
    if (routerParam && routerParam.responseSource === 'appApply') {
      this.responseSource = routerParam.responseSource
      this.lastActiveNode = routerParam
      this.tree()
      this.defaultTree(true, false)
    } else if (
      routerParam &&
      routerParam.nodeType === 'panel' &&
      this.historyRequestId !== routerParam.requestId
    ) {
      this.historyRequestId = routerParam.requestId
      this.tree()
      this.defaultTree(true, false)
      this.edit(routerParam, null)
    } else {
      this.defaultTree(true, true)
      this.tree(true)
    }
  },
  methods: {
    loadInit() {
      const historyLocalSortType = localStorage.getItem('TreeSort-panel')
      if (historyLocalSortType) {
        this.localSortParams = { sortType: historyLocalSortType }
      } else {
        this.localSortParams = { sortType: this.curSortType }
      }
    },
    beforeClickItem(type) {
      return {
        sortType: type
      }
    },
    sortTypeChange(params) {
      this.tData = treeSort(this.originResourceTree, this.curSortType, params.sortType)
      this.curSortType = params.sortType
      localStorage.setItem('TreeSort-panel', this.curSortType)
    },
    activeLastNode() {
      this.$nextTick(() => {
        document.querySelector('.is-current').firstChild.click()
      })
    },
    toTop(data, node) {
      panelToTop(data.id).then(() => {
        this.defaultTree()
      })
    },
    fromAppActive() {
      this.activeNodeAndClickOnly(this.lastActiveNode)
      this.clearLocalStorage.forEach((item) => {
        localStorage.removeItem(item)
      })
      this.responseSource = 'panelQuery'
    },
    initCache() {
      // 初始化时提前加载视图和数据集的缓存
      this.initLocalStorage.forEach((item) => {
        if (!localStorage.getItem(item + '-tree')) {
          queryAuthModel({ modelType: item }, false).then((res) => {
            localStorage.setItem(item + '-tree', JSON.stringify(res.data || []))
          })
        }
      })
    },
    closeEditPanelDialog(panelInfo) {
      this.editPanel.visible = false
      if (panelInfo) {
        if (this.editPanel.optType === 'toDefaultPanel') {
          this.defaultTree(false)
        }

        updateCacheTree(this.editPanel.optType,
          panelInfo.panelType === 'system' ? 'panel-default-tree' : 'panel-main-tree', panelInfo,
          panelInfo.panelType === 'system' ? this.defaultData : this.tData)
        if (this.editPanel.optType === 'rename' && panelInfo.id === this.$store.panel.panelInfo.id) {
          this.$store.panel.panelInfo.name = panelInfo.name
        }
        this.originResourceTree = deepCopy(this.tData)
        // 默认展开 同时点击 新增的节点
        if (
          panelInfo &&
          panelInfo.panelType === 'self' &&
          this.lastActiveNodeData.id
        ) {
          if (this.editPanel.optType === 'rename') {
            this.lastActiveNodeData.name = panelInfo.name
            return
          }
          // 复制后的仪表板 放在父节点下面
          if (this.editPanel.optType !== 'copy') {
            this.lastActiveNode.expanded = true
          }
          this.activeNodeAndClick(panelInfo)
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
              name: param.data.name,
              nodeType: param.type
            }
          }
          break
        case 'toDefaultPanel':
          this.editPanel = {
            visible: true,
            titlePre: this.$t('panel.to_default'),
            optType: 'toDefaultPanel',
            panelInfo: {
              id: param.data.id,
              name: param.data.name,
              optType: 'toDefaultPanel',
              nodeType: param.type
            }
          }
          break
        case 'copy':
          this.editPanel = {
            visible: true,
            titlePre: this.$t('panel.copy'),
            optType: 'copy',
            panelInfo: {
              id: param.data.id,
              name: param.data.name,
              optType: 'copy',
              nodeType: param.type
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
        type: type,
        data: data,
        node: node,
        optType: optType
      }
    },

    clickMore(param) {
      switch (param.optType) {
        case 'copy':
        case 'toDefaultPanel':
        case 'rename':
          this.showEditPanel(param)
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
        case 'move':
          this.moveTo(param.data)
          break
      }
    },

    beforeClickMore(optType, data, node) {
      return {
        type: data.nodeType,
        data: data,
        node: node,
        optType: optType
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
          addGroup(group).then((res) => {
            this.close()
            this.$message({
              message: this.$t('commons.save_success'),
              type: 'success',
              showClose: true
            })
            this.tree()
            this.defaultTree(false)
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
      const title = data.source ? 'commons.cancel_this_dashboard' : (data.nodeType === 'folder' ? 'commons.delete_this_folder' : 'commons.delete_this_dashboard')
      const params = {
        title: title,
        type: 'danger',
        cb: () => {
          delGroup(data.id).then((response) => {
            this.$message({
              type: 'success',
              message: this.$t('panel.delete_success'),
              showClose: true
            })
            this.clearCanvas()
            updateCacheTree('delete', 'panel-main-tree', data.id, this.tData)
            this.originResourceTree = deepCopy(this.tData)
            this.defaultTree(false)
          })
        }
      }
      this.handlerConfirm(params, this.$t('commons.delete'))
    },

    clearCanvas() {
      // 清空当前缓存,快照
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      this.$store.dispatch('panel/setPanelInfo', {
        id: null,
        name: '',
        preStyle: null
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
    tree(cache = false) {
      const modelInfo = localStorage.getItem('panel-main-tree')
      let preParse
      if (modelInfo) {
        try {
          preParse = JSON.parse(modelInfo)
        } catch (e) {
          console.warn('panel-main-tree cache error')
        }
      }
      const userCache = preParse && cache
      if (userCache) {
        this.originResourceTree = preParse
        this.sortTypeChange(this.localSortParams)
      }
      groupTree(this.groupForm, !userCache).then((res) => {
        this.rootAuth = res.data ? res.data[0]?.privileges || '' : ''
        const resMainData = res.data ? res.data[0]?.children || [] : []
        localStorage.setItem('panel-main-tree', JSON.stringify(resMainData))
        if (!userCache) {
          this.originResourceTree = resMainData
          this.sortTypeChange(this.localSortParams)
        }
        if (this.responseSource === 'appApply') {
          this.fromAppActive()
        }
        if (this.filterText) {
          this.$nextTick(() => {
            this.$refs.panel_list_tree.filter(this.filterText)
          })
        }
      })
    },
    defaultTree(cache = false, showFirst = false) {
      const requestInfo = {
        panelType: 'system'
      }
      const modelInfo = localStorage.getItem('panel-default-tree')
      let preParse
      if (modelInfo) {
        try {
          preParse = JSON.parse(modelInfo)
        } catch (e) {
          console.warn('panel-default-tree cache error')
        }
      }
      const userCache = preParse && cache

      if (userCache) {
        this.defaultData = preParse
        if (showFirst && this.defaultData && this.defaultData.length > 0) {
          this.activeDefaultNodeAndClickOnly(this.defaultData[0].id)
        }
      }
      defaultTree(requestInfo, false).then((res) => {
        this.rootDefaultAuth = res.data ? res.data[0]?.privileges || '' : ''
        const resDefaultData = res.data ? res.data[0]?.children || [] : []
        localStorage.setItem('panel-default-tree', JSON.stringify(resDefaultData))
        if (!userCache) {
          this.defaultData = resDefaultData
          if (showFirst && this.defaultData && this.defaultData.length > 0) {
            this.activeDefaultNodeAndClickOnly(this.defaultData[0].id)
          }
        }
        if (this.filterText) {
          this.$nextTick(() => {
            this.$refs.default_panel_tree?.filter(this.filterText)
          })
        }
          this.$nextTick(() => {
            const currentKey = this.$refs.default_panel_tree?.getCurrentKey()
            if (currentKey) {
              this.$nextTick(() =>{
                this.$refs.default_panel_tree?.setCurrentKey(currentKey)
              })
            }
          })
      })
    },

    nodeClick(data, node) {
      if (data.panelType === 'self') {
        this.$refs.default_panel_tree?.setCurrentKey(null)
      } else {
        this.$refs.panel_list_tree?.setCurrentKey(null)
      }
      this.lastActiveNode = node
      this.lastActiveNodeData = data
      this.activeTree = data.panelType
      if (data.nodeType === 'panel') {
        // 清理pc布局缓存
        this.$store.commit('setComponentDataCache', null)
        initPanelData(data.id, false, function(response) {
          viewPanelLog({ panelId: data.id }).then((res) => {
            bus.$emit('set-panel-show-type', 0)
            data.mobileLayout = response.data.mobileLayout
          })
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
    back() {
      this.sceneMode = false
      this.$store.dispatch('dataset/setSceneData', null)
      this.$emit('switchComponent', { name: '' })
    },
    beforeClickAddData(type) {
      return {
        type: type
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
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      this.$store.dispatch('panel/setPanelInfo', {
        id: data.id,
        name: data.name,
        privileges: data.privileges,
        sourcePanelName: data.sourcePanelName,
        status: data.status,
        createBy: data.createBy,
        createTime: data.createTime,
        updateBy: data.updateBy,
        updateTime: data.updateTime,
        watermarkOpen: data.watermarkOpen
      })
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
        data.forEach((item) => {
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
        const _this = this
        _this.$nextTick(() => {
          // 延迟设置CurrentKey
          _this.$refs.panel_list_tree.setCurrentKey(panelInfo.id)
          // 去除default_tree 的影响
          _this.$refs.default_panel_tree?.setCurrentKey(null)
          _this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
            // 如果是仪表板列表的仪表板 直接进入编辑界面
            if (panelInfo.nodeType === 'panel' && this.editPanel.optType !== 'copy') {
              _this.edit(this.lastActiveNodeData, this.lastActiveNode)
            }
          })
        })
      }
    },
    // 激活当前节点
    activeNodeAndClickOnly(panelInfo) {
      if (panelInfo) {
        const _this = this
        _this.$nextTick(() => {
          // 延迟设置CurrentKey
          _this.$refs.panel_list_tree?.setCurrentKey(panelInfo.id)
          // 去除default_tree 的影响
          _this.$refs.default_panel_tree?.setCurrentKey(null)
          if (panelInfo.parents) {
            _this.expandedArray = panelInfo.parents
          }
          _this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
          })
        })
      }
    },
    // 激活当前默认仪表板节点节点
    activeDefaultNodeAndClickOnly(panelId) {
      if (panelId) {
        const _this = this
        _this.$nextTick(() => {
          _this.$refs.panel_list_tree?.setCurrentKey(null)
          // 延迟设置CurrentKey
          _this.$refs.default_panel_tree?.setCurrentKey(panelId)
          _this.$nextTick(() => {
            document.querySelector('.is-current').firstChild.click()
          })
        })
      }
    },
    moveTo(data) {
      const _this = this
      this.moveInfo = data
      this.moveDialogTitle =
        this.$t('dataset.m1') +
        (data.name.length > 10 ? data.name.substr(0, 10) + '...' : data.name) +
        this.$t('dataset.m2')
      const queryInfo = JSON.parse(JSON.stringify(this.groupForm))
      queryInfo['nodeType'] = 'folder'
      groupTree(queryInfo).then((res) => {
        if (data.nodeType === 'folder') {
          _this.tGroupData = [
            {
              id: 'panel_list',
              name: _this.$t('panel.panel_list'),
              label: _this.$t('panel.panel_list'),
              children: res.data || []
            }
          ]
        } else {
          _this.tGroupData = res.data || []
        }
        _this.moveGroup = true
      })
    },
    closeMoveGroup() {
      this.moveGroup = false
      this.tGroup = {}
    },
    saveMoveGroup() {
      this.moveInfo.pid = this.tGroup.id
      this.moveInfo['optType'] = 'move'
      panelMove(this.moveInfo).then((response) => {
        updateCacheTree('move', 'panel-main-tree', response.data, this.tData)
        this.originResourceTree = deepCopy(this.tData)
        this.closeMoveGroup()
      })
    },
    targetGroup(val) {
      this.tGroup = val
      this.groupMoveConfirmDisabled = false
    },
    filterNode(value, data) {
      if (!value) return true
      const result = data.label.toLowerCase().indexOf(value.toLowerCase()) !== -1
      if (this.searchType === 'folder') {
        if (data.nodeType === 'folder' && result) {
          this.searchPids.push(data.id)
          return true
        }
        if (this.searchPids.indexOf(data.pid) !== -1) {
          if (data.nodeType === 'folder') {
            this.searchPids.push(data.id)
          }
          return true
        }
      } else {
        return result
      }
      return false
    },
    searchTypeClick(searchTypeInfo) {
      this.searchType = searchTypeInfo
    },
    editFromPanelViewShow() {
      this.$store.commit('setComponentData', [])
      this.$store.commit('setCanvasStyle', DEFAULT_COMMON_CANVAS_STYLE_STRING)
      bus.$emit('PanelSwitchComponent', { name: 'PanelEdit' })
    },
    editPanelBashInfo(params) {
      if (params.operation === 'status') {
        this.lastActiveNodeData.status = params.value
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.sticky-de-search-panel {
  position: sticky;
  top: 55px;
  z-index: 10;
  background: #fff;
  &::before {
    content: '';
    position: absolute;
    top: -15px;
    height: 15px;
    width: 100%;
    background: #fff;
  }
}

.default-expansion {
  height: 40px;
  width: 232px;
  cursor: pointer;
  margin: 0;
  font-family: AlibabaPuHuiTi;
  font-size: 14px;
  font-weight: 400;
  color: var(--deTextSecondary, #646a73);
  padding-left: 22px;
  display: flex;
  align-items: center;

  i {
    margin-left: 5px;
  }
}

.main-area-input {
  width: calc(100% - 40px);
  ::v-deep.el-input-group__append {
    width: 70px;
    background: transparent;

    .el-input__inner {
      padding-left: 12px;
    }
  }
}

.header-title {
  font-size: 14px;
  flex: 1;
  color: var(--TextPrimary, #606266);
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

.dialog-css ::v-deep .el-dialog__body {
  padding: 15px 20px;
}

.dialog-css ::v-deep .el-dialog__body {
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
.link-dialog ::v-deep .el-dialog__title {
  display: none;
}
.link-dialog ::v-deep .el-dialog__body {
  height: 350px;
}
.link-dialog ::v-deep .el-dialog__headerbtn {
  z-index: 1;
}

.insert-filter {
  display: inline-block;
  font-weight: 400 !important;
  font-family: '阿里巴巴普惠体 3.0 55 Regular L3';
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  color: var(--TextPrimary, #1f2329);
  -webkit-appearance: none;
  text-align: center;
  box-sizing: border-box;
  outline: 0;
  margin: 0;
  transition: 0.1s;
  border-radius: 3px;

  &:active {
    color: #000;
    border-color: #3a8ee6;
    background-color: red;
    outline: 0;
  }

  &:hover {
    background-color: rgba(31, 35, 41, 0.1);
    color: #3a8ee6;
  }
}

.filter-icon-span {
  border: 1px solid #dcdfe6;
  width: 32px;
  height: 32px;
  border-radius: 4px;
  padding: 7px;
  margin-left: 8px;
}

.menu-outer-dv_popper {
  width: 140px;
  margin-top: -2px !important;

  .ed-icon {
    border-radius: 4px;
  }
}

.sort-type-normal {
  display: flex;
  i {
    display: none;
  }
}

.sort-type-checked {
  color: #3370ff;
  i {
    display: block;
  }
}

.custom-driver {
  margin: 12px 0
}
</style>
