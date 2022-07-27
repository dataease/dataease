<template>
  <div class="portal-config-drawer-container">
    <el-drawer
      :visible.sync="syncVisible"
      size="80%"
      direction="rtl"
      :show-close="false"
      :with-header="false"
    >
      <div v-if="syncVisible" class="portal-config-drawer-container-container">
        <div class="header">
          <div class="headerleft">
            <i class="el-icon-close" @click="close" />
            <div class="name">{{ portalName || "未命名站点" }}</div>
          </div>
          <div class="headerright">
            <div class="wrapper">
              <el-radio-group
                v-model="activeTab"
                size="mini"
                round
                @change="handleEditPreivewTab"
              >
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button
                  label="preview"
                  :disabled="!priviewBtnEnable"
                >预览</el-radio-button>
              </el-radio-group>
            </div>
            <div class="wrapper">
              <el-button
                type="primary"
                round
                size="mini"
                @click="handleSave"
              >保 存</el-button>
            </div>
          </div>
        </div>
        <div class="portal-config-drawer-container-container-content">
          <!-- <PortalNavMenu :portal="portal">
            <PanelViewShow ref="panelViewShow"></PanelViewShow>
          </PortalNavMenu> -->
          <el-container class="config-container">
            <el-header
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                  topNavPosRadio == 'top'
              "
              class="config-header"
              :style="{ backgroundColor: themeColor,justifyContent:floatPosition,backgroundImage:`url(${navBageImg})` }"
            >
              <div
                v-for="(item,index) in treeData"
                v-show="headerNavStyle==='1'&&( floatPosition ==='center'||floatPosition ==='flex-end')&&JudgmentTwo(floatPosition,index)"
                :key="item.id"
                :style="menuStyleSet"
                @click="changePage(item,index)"
              >
                {{ item.label }}
              </div>
              <div v-if="headerNavStyle==='1'" :style="titleStyleSet">{{ portalName || "未命名站点" }}</div>
              <div
                v-for="(item,indexs) in treeData"
                v-show="headerNavStyle==='1'&&( floatPosition ==='center'||floatPosition ==='flex-end')&&Judgment(floatPosition,indexs)"
                :key="item.id"
                :style="menuStyleSet"
                @click="changePage(item,indexs)"
              >
                {{ item.label }}
              </div>
              <div v-if="headerNavStyle==='0'" class="title">{{ portalName || "未命名站点" }}</div>

              <div v-if="headerNavStyle==='0'" class="tabs">
                <template v-if="navLayoutStyle == 0">
                  <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    :background-color="themeColor"
                    active-text-color="#333"
                    @select="handleTopSelect"
                  >
                    <el-menu-item
                      v-for="item in treeData"
                      :key="item.id"
                      :index="item.id"
                    >
                      <template slot="title">
                        <i :class="[item.iconName]" />
                        <span slot="title">{{ item.label }}</span>
                      </template>
                    </el-menu-item>
                  </el-menu>
                </template>
                <template v-else-if="navLayoutStyle == 2">
                  <!-- <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    :background-color="themeColor"
                    active-text-color="#333"
                  > -->
                  <PortalMenu
                    :sub-tree-datas="subTreeDatas"
                    :current-tree-node="currentTreeNode"
                    :theme-color="themeColor"
                  />
                  <!-- </el-menu> -->
                </template>
              </div>
            </el-header>
            <el-container>
              <el-aside
                v-if="navLayoutStyle == 0 || navLayoutStyle == 1"
                class="config-aside"
                width="200px"
                :style="{ backgroundColor: themeColor }"
              >
                <!-- <el-menu
                  :default-active="currentTreeNode.id"
                  :background-color="themeColor"
                  active-text-color="#333"
                > -->
                <!-- <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu> -->
                <PortalMenu
                  mode="vertical"
                  :sub-tree-datas="subTreeDatas"
                  :current-tree-node="currentTreeNode"
                  :theme-color="themeColor"
                />

                <!-- </el-menu> -->
              </el-aside>
              <el-main v-loading="panelLoading" class="config-main">
                <!-- <div>11111111111111111</div> -->
                <PanelViewShow
                  ref="panelViewShow"
                  :portal="privewPortal"
                  @update="update"
                />
              </el-main>
            </el-container>
            <el-header
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                  topNavPosRadio == 'bottom'
              "
              class="config-header"
              :style="{ backgroundColor: themeColor }"
            >
              <div>头部设置44444</div>
              <div class="title">{{ portalName || "未命名站点" }}</div>

              <div class="tabs">
                <template v-if="navLayoutStyle == 0">
                  <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    :background-color="themeColor"
                    active-text-color="#333"
                    @select="handleTopSelect"
                  >
                    <el-menu-item
                      v-for="item in treeData"
                      :key="item.id"
                      :index="item.id"
                    >
                      <template slot="title">
                        <i :class="[item.iconName]" />
                        <span slot="title">{{ item.label }}</span>
                      </template>
                    </el-menu-item>
                  </el-menu>
                </template>
                <template v-else-if="navLayoutStyle == 2">
                  <!-- <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    :background-color="themeColor"
                    text-color="#333"
                  > -->
                  <!-- <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu> -->

                  <PortalMenu
                    :sub-tree-datas="subTreeDatas"
                    :current-tree-node="currentTreeNode"
                    :theme-color="themeColor"
                  />

                  <!-- </el-menu> -->
                </template>
              </div>
            </el-header>
          </el-container>
          <div class="right-config-container">
            <div class="right-config-container-left">
              <div class="title">菜单配置</div>
              <div class="left-content">
                <el-button
                  type="primary"
                  size="mini"
                  @click="handleAddMainMenu"
                >+添加主菜单</el-button>
                <el-tree
                  ref="tree"
                  node-key="id"
                  draggable
                  :data="treeData"
                  :highlight-current="true"
                  default-expand-all
                  :expand-on-click-node="false"
                  check-on-click-node
                  :allow-drop="handleAllowDrop"
                  :current-node-key="currentTreeNode.id"
                  @node-click="handleNodeClick"
                >
                  <span
                    slot-scope="{ node }"
                    class="custom-tree-node"
                    @mouseenter="handleTreeNodeMouseEnter(node)"
                    @mouseleave="handleTreeNodeMouseLeave(node)"
                  >
                    <span class="custom-tree-node-label">{{ node.label }}</span>
                    <span v-if="node.data.showOption" class="icon-wrapper">
                      <i
                        class="el-icon-plus"
                        @click.stop="handleAddTreeSubNode(node)"
                      />
                      <i
                        class="el-icon-delete"
                        @click.stop="handleDeleteTreeSubNode(node)"
                      />
                    </span>
                  </span>
                </el-tree>
              </div>
            </div>
            <div class="right-config-container-right">
              <div class="title">内容设置</div>
              <div v-if="currentTreeNode.id" class="right-content">
                <div class="config-title">菜单显示名称</div>
                <el-input
                  v-model="currentTreeNode.label"
                  class="config-input"
                  size="mini"
                  placeholder="显示名称"
                />
                <el-checkbox
                  v-model="currentTreeNode.showMenuIcon"
                  class="config-checkbox"
                >显示菜单icon</el-checkbox>
                <el-select
                  v-model="currentTreeNode.iconName"
                  class="config-input"
                  size="mini"
                >
                  <el-option
                    v-for="item in iconData"
                    :key="item.value"
                    :value="item.value"
                  >
                    <i :class="[item.value]" />
                  </el-option>
                </el-select>
                <el-checkbox
                  v-model="currentTreeNode.isMenuFoldindg"
                  class="config-checkbox"
                >菜单允许折叠</el-checkbox>
                <el-checkbox
                  v-model="currentTreeNode.isMenuDefaultFolding"
                  class="config-checkbox"
                >菜单默认折叠</el-checkbox>
                <!-- <el-checkbox class="config-checkbox" v-model="currentTreeNode.isNodeNull">设为空节点</el-checkbox> -->
                <div class="config-title">选择仪表盘</div>
                <el-cascader
                  v-model="currentTreeNode.trendId"
                  :options="tData"
                  :props="{ expandTrigger: 'click' }"
                  class="config-input"
                  size="mini"
                  @change="handleTrendChange"
                />
                <!-- <el-select
                  v-model="currentTreeNode.panelId"
                  class="config-input"
                  size="mini"
                >
                  <el-option
                    v-for="item in tData"
                    :key="item.id"
                    :value="item.id"
                    :label="item.label"
                  ></el-option>
                </el-select>
                <div class="config-title">选择趋势图</div>
                <el-select
                  v-model="currentTreeNode.trendId"
                  class="config-input"
                  size="mini"
                  @change="handleTrendChange"
                >
                  <el-option
                    v-for="item in trendData"
                    :key="item.id"
                    :value="item.id"
                    :label="item.label"
                  ></el-option>
                </el-select> -->
                <el-button
                  class="config-btn"
                  type="primary"
                  size="mini"
                  @click="handleUpdateTrend"
                >更新</el-button>
                <div class="config-title">查看方式</div>
                <el-radio-group v-model="currentTreeNode.viewMode">
                  <el-radio
                    class="config-radio"
                    label="current"
                  >当前页面打开</el-radio>
                  <el-radio
                    class="config-radio"
                    label="_blank"
                  >新窗口打开</el-radio>
                </el-radio-group>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import PortalMenu from './PortalMenu.vue'
import { groupTree, initPanelData } from '@/api/panel/panel'
import PanelViewShow from '@/views/panel/list/PanelViewShow.vue'
import bus from '@/utils/bus'

export default {
  components: {
    PortalMenu,
    PanelViewShow
  },
  props: {
    visible: {
      type: Boolean,
      default: false
    },
    titleFontSet: {
      type: Object,
      default: () => {}
    },
    menuFontSet: {
      type: Object,
      default: () => {}
    },
    navBageImg: String,
    headerNavStyle: String,
    titleWidth: Number,
    menuWidth: Number,
    floatPosition: String,
    themeColor: String,
    topNavPosRadio: String,
    navLayoutStyle: String,
    portalName: String, // 当前门户的名称
    openType: {
      type: String,
      default: 'add' // edit
    },
    lastTreeId: Number | String,
    portalId: String | Number,
    config: {
      type: Object,
      default: () => {}
    }
  },

  data() {
    return {
      activeTab: 'edit', // 当前最顶部nav中是编辑还是预览
      topActiveTab: '0', // 当前顶部导航栏选择的下标选项
      treeData: [],
      treeId: 0,
      currentTreeNode: {},
      groupForm: {
        name: null,
        pid: null,
        panelType: 'self',
        nodeType: null,
        children: [],
        sort: 'create_time desc,node_type desc,name asc'
      },
      tData: [],
      iconData: [
        {
          value: 'el-icon-menu'
        },
        {
          value: 'el-icon-s-opportunity'
        },
        {
          value: 'el-icon-s-finance'
        },
        {
          value: 'el-icon-s-claim'
        }
      ],
      showPanelView: false,
      panelLoading: false,
      privewPortal: null,
      priviewBtnEnable: false
    }
  },
  computed: {
    titleStyleSet() {
      // {width:titleWidth+'%',textAlign:'center'}
      console.log('this.titleFontSet', this.titleFontSet)
      const style = {}
      style.width = this.titleWidth + '%'
      style.textAlign = 'center'
      if (this.titleFontSet) {
        style.color = this.titleFontSet.color
        style.fontSize = this.titleFontSet.fontSize + 'px'
        style.fontFamily = this.titleFontSet.fontFamily
        style.opacity = this.titleFontSet.opacity
      }

      return style
    },
    menuStyleSet() {
      // {width:menuWidth+'%',textAlign:'center'}
      const style = {}
      style.width = this.menuWidth / 10 + '%'
      style.textAlign = 'center'
      if (this.menuFontSet) {
        style.color = this.menuFontSet.color
        style.fontSize = this.menuFontSet.fontSize + 'px'
        style.fontFamily = this.menuFontSet.fontFamily
      }
      style.cursor = 'pointer'

      return style
    },
    syncVisible: {
      get() {
        return this.visible
      },
      set(val) {
        this.$emit('update:visible', val)
      }
    },

    subTreeDatas() {
      const treeData = this.treeData.slice(0)
      if (this.navLayoutStyle == 0) {
        const subs = []
        treeData.forEach((item) => {
          item.children.forEach((sub) => {
            subs.push(sub)
          })
        })
        return subs
      } else if (this.navLayoutStyle == 1 && this.navLayoutStyle == 2) {
        return treeData
      }
      return treeData
    },

    trendData() {
      const found = this.tData.find(
        (item) => item.id == this.currentTreeNode.panelId
      )
      if (found) {
        return found.children
      }
      return []
    }

    // 预览按钮是否可以点击
  },

  watch: {
    syncVisible(val) {
      if (val) {
        this.activeTab = 'edit' // 当前最顶部nav中是编辑还是预览
        this.topActiveTab = '0' // 当前顶部导航栏选择的下标选项
        this.currentTreeNode = {}
        this.$nextTick(() => {
          if (this.$refs.panelViewShow) {
            this.$refs.panelViewShow.showMain = false
            // 检测panel是否变化
            this.$watch(
              () => this.$refs.panelViewShow.showMain,
              (val) => {
                console.log('this.$refs.panelViewShow.showMain ----', val)
                this.priviewBtnEnable = val
              }
            )
          }
        })
        if (this.openType == 'add') {
          this.treeData = []
          this.treeId = 0
        } else {
          this.treeData = this.config.treeData
          this.treeId = Number(this.lastTreeId)
          let currentTreeId = ''
          function getTreedDataFirstTrendId(treeData) {
            for (let i = 0; i < treeData.length; i++) {
              const item = treeData[i]
              if (item.trendId && !currentTreeId) {
                currentTreeId = item.id
              } else {
                getTreedDataFirstTrendId(item.children)
              }
            }
          }

          getTreedDataFirstTrendId(this.treeData)
          this.$nextTick(() => {
            this.handleDynamicMenuAndTree(currentTreeId)
          })
        }
      } else {
        this.$emit('treeData', this.treeData)
      }
    }
  },

  mounted() {
    this.tree(true)
  },

  methods: {
    Judgment(key, index) {
      if (key === 'center') {
        if (index % 2 !== 0) {
          return true
        } else {
          return false
        }
      } else {
        return true
      }
    },
    JudgmentTwo(key, index) {
      if (key === 'center') {
        if (index % 2 !== 0) {
          return false
        } else {
          return true
        }
      } else {
        return true
      }
    },
    close() {
      this.syncVisible = false
    },
    // 保存
    async handleSave() {
      const params = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || '未命名站点', // 站点名称
        lastTreeId: this.treeId,
        headerNavStyle: this.headerNavStyle, // 0 默认 1浮动
        floatPosition: this.floatPosition, // 浮动位置
        titleWidth: this.titleWidth, // 标题宽度
        menuWidth: this.menuWidth, // 菜单宽度
        navBageImg: this.navBageImg,
        titleFontSet: this.titleFontSet,
        menuFontSet: this.menuFontSet,
        config: {
          treeData: this.treeData
        }
      }
      const positionJson = JSON.stringify(params)
      if (positionJson.includes(`"label":""`)) {
        this.$message.error('请输入节点的名称')
        return
      }

      if (this.openType === 'add') {
        bus.$emit('savePortal', { positionJson })
      } else {
        bus.$emit('updatePortal', { id: this.portalId, positionJson })
      }
      this.close()
      this.$emit('close')
    },

    // 点击编辑和预览
    handleEditPreivewTab(evt) {
      console.log('evt -- ', evt)
      if (evt === 'edit') {
        return
      }
      this.privewPortal = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || '未命名站点', // 站点名称
        lastTreeId: this.treeId,
        headerNavStyle: this.headerNavStyle, // 0 默认 1浮动
        floatPosition: this.floatPosition, // 浮动位置
        titleWidth: this.titleWidth, // 标题宽度
        menuWidth: this.menuWidth, // 菜单宽度
        navBageImg: this.navBageImg,
        titleFontSet: this.titleFontSet,
        menuFontSet: this.menuFontSet,
        config: {
          treeData: this.treeData
        }
      }
      this.$nextTick(() => {
        if (this.$refs.panelViewShow && this.$refs.panelViewShow.showMain) {
          this.$refs.panelViewShow.clickFullscreen()

          this.$watch(
            () => this.$refs.panelViewShow.fullscreen,
            (val) => {
              if (!val) {
                this.activeTab = 'edit'
                this.privewPortal = null
              }
            }
          )
        }
      })
    },
    // 选择主题设置
    handleChangeThemeColor(color) {
      console.log('color', color)
      // this.setThemeColor(color);
    },
    setNavLayoutStyle(style) {
      this.navLayoutStyle = style
    },
    // 动态关联菜单和tree的同步
    handleDynamicMenuAndTree(currentTreeId) {
      if (currentTreeId) {
        this.$refs.tree.setCurrentKey(currentTreeId)
        const currentNode = this.$refs.tree.getCurrentNode()
        if (currentNode) {
          this.handleNodeClick(currentNode)
        }
      }
    },
    // 添加主菜单
    handleAddMainMenu() {
      const treeId = (this.treeId += 1)
      this.treeData.push({
        id: treeId.toString(),
        label: '一级菜单',
        iconName: 'el-icon-menu',
        children: [],
        panelId: '',
        trendId: '',
        showMenuIcon: true, // 显示菜单icon
        isMenuFoldindg: true, // 菜单允许折叠
        isMenuDefaultFolding: true, // 菜单默认折叠
        isNodeNull: false, // 设置为空节点
        viewMode: 'current', // _blank
        showOption: false,
        level: 1
      })
    },
    _checkArrayHasValue(arr) {
      return arr && arr.length
    },
    // 点击节点时候触发
    handleNodeClick(node) {
      // console.log("node", node);
      this.currentTreeNode = node
      if (this.currentTreeNode.trendId && this.currentTreeNode.trendId.length) {
        this.handleUpdateTrend()
      } else {
        this.$refs.panelViewShow.showMain = false
      }
    },
    // 鼠标移入该节点的时候触发
    handleTreeNodeMouseEnter(node) {
      // console.log("node enter", node);
      node.data.showOption = true
    },
    // 鼠标移除该节点的时候触发
    handleTreeNodeMouseLeave(node) {
      // console.log("node leave", node);
      node.data.showOption = false
    },
    // 添加子节点
    handleAddTreeSubNode(node) {
      if (node.data.trendId) {
        this.$message.warning(
          '该菜单下已配置过仪表盘，不能配置子菜单，请删除后重新配置'
        )
        return
      }
      console.log('handleAddTreeSubNode node', node)
      this.treeId += 1
      const treeId = this.treeId
      const level = node.data.level + 1

      if (node.data.level == 1) {
        const foundIndex = this.treeData.findIndex(
          (item) => item.id == node.data.id
        )
        this.treeData[foundIndex].children.push({
          id: treeId.toString(),
          label: '二级菜单',
          iconName: 'el-icon-menu',
          children: [],
          showMenuIcon: true, // 显示菜单icon
          isMenuFoldindg: true, // 菜单允许折叠
          isMenuDefaultFolding: true, // 菜单默认折叠
          isNodeNull: false, // 设置为空节点
          viewMode: 'current', // _blank
          showOption: false,
          level
        })
      } else {
        const foundIndex = this.treeData.findIndex(
          (item) => item.id == node.parent.data.id
        )
        console.log('foundINdex', foundIndex)
        const children = this.treeData[foundIndex].children
        console.log('children', children)
        const foundChildIndex = children.findIndex(
          (item) => item.id == node.data.id
        )
        console.log('foundChildIndex', foundChildIndex)
        this.treeData[foundIndex].children[foundChildIndex].children.push({
          id: treeId.toString(),
          label: '三级菜单',
          iconName: 'el-icon-menu',
          // children: [],
          showMenuIcon: true, // 显示菜单icon
          isMenuFoldindg: true, // 菜单允许折叠
          isMenuDefaultFolding: true, // 菜单默认折叠
          isNodeNull: false, // 设置为空节点
          viewMode: 'current', // _blank
          showOption: false,
          level
        })
      }
    },
    // 删除一个节点
    handleDeleteTreeSubNode(node, data) {
      console.log('handleDeleteTreeSubNode node', node)
      this.$confirm('删除该节点', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
        .then(() => {
          const id = node.data.id
          if (node.data.level == 1) {
            const foundIndex = this.treeData.findIndex((item) => item.id == id)
            if (foundIndex > -1) {
              this.treeData.splice(foundIndex, 1)
            }
            return
          }
          const parent = node.parent
          const children = parent.data.children
          const index = children.findIndex((d) => d.id === id)
          children.splice(index, 1)
        })
        .catch(() => {})
    },
    // 选择一级菜单
    changePage(item, index) {
      this.handleTopSelect(item.id)
    },
    handleTopSelect(active) {
      console.log('active', active)
      this.topActiveTab = active
    },
    // 节点拖拽进入到其他的节点
    handleAllowDrop(node, targetNode, type) {
      if (targetNode.level == 3) {
        return false
      }
      return true
    },
    // 选择一个趋势图
    handleTrendChange(data) {
      console.log('data', data)
      //   this.$store.commit("setComponentDataCache", null);
      //   initPanelData(data, function (response) {
      //     bus.$emit("set-panel-show-type", 0);
      //   });
    },

    handleUpdateTrend() {
      // this.showPanelView = true;
      // this.$store.commit("setComponentDataCache", null);
      this.panelLoading = true
      const trendId =
        this.currentTreeNode.trendId[this.currentTreeNode.trendId.length - 1]
      const that = this
      initPanelData(trendId, function(response) {
        bus.$emit('set-panel-show-type', 0)
        that.panelLoading = false
      })
    },

    tree(cache = false) {
      // const modelInfo = localStorage.getItem("panel-main-tree");
      // const userCache = modelInfo && cache;
      // if (userCache) {
      //   this.tData = JSON.parse(modelInfo);
      // }
      groupTree(this.groupForm, false).then((res) => {
        localStorage.setItem('panel-main-tree', JSON.stringify(res.data))
        // if (!userCache) {
        // const tData = res.data.map((item) => {});
        this.tData = this._deepLooptData(res.data)
        // }
      })
    },

    update(trendId) {
      if (trendId) {
        if (Object.prototype.toString.call(trendId) == '[object Array]') {
          trendId = trendId[trendId.length - 1]
        }
        const that = this
        initPanelData(trendId, function(response) {
          bus.$emit('set-panel-show-type', 0)
        })
      } else {
        this.$refs.panelViewShow.showMain = false
      }
    },

    _deepLooptData(data) {
      return data.map((item) => {
        return {
          label: item.label,
          value: item.id,
          children: item.children && this._deepLooptData(item.children)
        }
      })
    }
  }
}
</script>

<style lang="less" scoped>
.portal-config-drawer-container {
  ::v-deep .el-menu,
  .el-menu.el-menu--horizontal {
    border: unset;
  }
  ::v-deep .el-header {
    padding: 0;
  }
  ::v-deep .el-tree {
    margin-top: 20px;
    background-color: transparent;
    // color: #fff;
  }
  ::v-deep .panel-design-head {
    display: none !important;
  }
  ::v-deep .panel-design-preview {
    height: 100% !important;
  }
  ::v-deep
    .el-menu--horizontal
    .el-submenu
    .el-submenu__title
    .el-submenu__icon-arrow {
    position: static;
    vertical-align: middle;
    margin-left: 8px;
    margin-top: -3px;
  }

  .el-icon-plus,
  .el-icon-delete {
    cursor: pointer;
    color: #333;
  }
  .el-icon-close,
  .el-icon-setting {
    cursor: pointer;
    color: unset;
  }
  .portal-config-drawer-container-container {
    position: relative;
    .header {
      position: sticky;
      left: 0;
      right: 0;
      padding: 16px;
      box-sizing: border-box;
      border-bottom: 1px solid var(--TopBG, #e6e6e6);
      background-color: var(--TopBG, #f1f3f8);
      box-sizing: border-box;
      display: flex;
      align-items: center;
      justify-content: space-between;
      .headerleft {
        display: flex;
        align-items: center;
        .name {
          margin-left: 14px;
        }
      }
      .headerright {
        display: flex;
        align-items: center;
        .wrapper {
          margin-right: 10px;
        }
        .wrapper:last-child {
          margin-right: 0;
        }
      }
    }
    .portal-config-drawer-container-container-content {
      display: flex;
      justify-content: space-between;
      font-size: 14px;
      color: #333;
      min-height: calc(100vh - 60px);

      .config-container {
        flex: 3;
        .config-header {
          // background-color: #242834;
          // min-height: 60px;
          background-size:100% 100%;
          background-repeat: no-repeat;
          display: flex;
          align-items:center;
          .title {
            // background-color: green;
            width: 200px;
            line-height: 60px;
            text-align: center;
            border-bottom: 1px solid var(--TopBG, #e6e6e6);
          }
        }

        .config-aside {
          height: 100%;
        }

        .config-main {
          padding: 0 !important;
          background-color: var(--TopBG, #e6e6e6);
        }
      }

      .right-config-container {
        flex: 1;
        background-color: var(--TopBG, #f1f3f8);
        display: flex;
        .right-config-container-left,
        .right-config-container-right {
          flex: 1;
          border-left: 1px solid var(--TopBG, #e6e6e6);
          box-sizing: border-box;
          .title {
            // font-size: 16px;
            text-align: center;
            line-height: 60px;
            border-bottom: 1px solid var(--TopBG, #e6e6e6);
            box-sizing: border-box;
          }
        }
        .right-config-container-right {
          // background-color: var(--TopBG, #e6e6e6);
        }
        .left-content,
        .right-content {
          padding: 10px;
          box-sizing: border-box;
          display: flex;
          flex-direction: column;
          // align-items: center;
          .config-title {
            text-align: left;
            align-self: left;
            margin-bottom: 8px;
          }
          .config-input {
            // width: 100%;
            // border: 1px solid rgba(255, 255, 255, 0.5);
            // background-color: #1e2b47;
            // height: 28px;
            // line-height: 28px;
            // border-radius: 2px;
            // padding: 0 6px;
            // box-sizing: border-box;
            // color: #fff;
            margin-bottom: 12px;
          }
          .config-checkbox {
            margin-bottom: 10px;
            // color: #fff;
          }
          .config-btn {
            margin-bottom: 12px;
          }
          .config-radio {
            margin-bottom: 8px;
          }
        }
      }
    }
  }

  .custom-tree-node {
    width: 100%;
    display: flex;
    justify-content: space-between;
    position: relative;
    // width: 200px;
    .custom-tree-node-label {
      text-overflow: ellipsis;
      overflow: hidden;
      // width: 160px;
    }
    .icon-wrapper {
      position: absolute;
      right: 0;
      top: 50%;
      transform: translateY(-50%);
    }
    i {
      margin-right: 8px;
      color: #409eff;
    }
    i:last-child {
      margin-right: 0;
      color: #409eff;
    }
  }
}
</style>
