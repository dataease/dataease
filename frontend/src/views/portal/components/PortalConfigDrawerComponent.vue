<template>
  <div class="portal-config-drawer-container">
    <el-drawer
      :visible.sync="syncVisible"
      size="80%"
      direction="rtl"
      :show-close="false"
      :withHeader="false"
    >
      <div class="portal-config-drawer-container-container">
        <div class="header">
          <div class="headerleft">
            <i class="el-icon-close" @click="close"></i>
            <div class="name">{{ portalName || "未命名站点" }}</div>
          </div>
          <div class="headerright">
            <div class="wrapper">
              <el-radio-group v-model="activeTab" size="mini" round>
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview">预览</el-radio-button>
              </el-radio-group>
            </div>
            <div class="wrapper">
              <el-button type="primary" round size="mini">保 存</el-button>
            </div>
          </div>
        </div>
        <div class="portal-config-drawer-container-container-content">
          <el-container class="config-container">
            <el-header
              class="config-header"
              :style="{ backgroundColor: themeColor }"
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                topNavPosRadio == 'top'
              "
            >
              <div class="title">{{ portalName || "未命名站点" }}</div>
              <div class="tabs">
                <template v-if="navLayoutStyle == 0">
                  <el-menu
                    :default-active="topActiveTab"
                    class="top-nav-menu"
                    mode="horizontal"
                    :background-color="themeColor"
                    active-text-color="#333"
                    @select="handleTopSelect"
                  >
                    <el-menu-item
                      v-for="(item, index) in treeData"
                      :key="item.id"
                      :index="index"
                    >
                      <template slot="title">
                        <i class="el-icon-menu" v-if="item.showMenuIcon"></i>
                        <span slot="title">{{ item.label }}</span>
                      </template>
                    </el-menu-item>
                  </el-menu>
                </template>
                <template v-else-if="navLayoutStyle == 2">
                  <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    class="el-menu-vertical-demo"
                    :background-color="themeColor"
                    active-text-color="#333"
                  >
                    <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu>
                  </el-menu>
                </template>
              </div>
            </el-header>
            <el-container>
              <el-aside
                class="config-aside"
                width="200px"
                :style="{ backgroundColor: themeColor }"
                v-if="navLayoutStyle == 0 || navLayoutStyle == 1"
              >
                <el-menu
                  :default-active="currentTreeNode.id"
                  class="el-menu-vertical-demo"
                  :background-color="themeColor"
                  active-text-color="#333"
                >
                  <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu>
                </el-menu>
              </el-aside>
              <el-main class="config-main">
                <PanelViewShow ref="panelViewShow"></PanelViewShow>
              </el-main>
            </el-container>
            <el-header
              class="config-header"
              :style="{ backgroundColor: themeColor }"
              v-if="
                (navLayoutStyle == 0 || navLayoutStyle == 2) &&
                topNavPosRadio == 'bottom'
              "
            >
              <div class="title">{{ portalName || "未命名站点" }}</div>
              <div class="tabs">
                <template v-if="navLayoutStyle == 0">
                  <el-menu
                    :default-active="topActiveTab"
                    class="top-nav-menu"
                    mode="horizontal"
                    :background-color="themeColor"
                    active-text-color="#333"
                    @select="handleTopSelect"
                  >
                    <el-menu-item
                      v-for="(item, index) in treeData"
                      :key="item.id"
                      :index="index"
                    >
                      <template slot="title">
                        <i class="el-icon-menu" v-if="item.showMenuIcon"></i>
                        <span slot="title">{{ item.label }}</span>
                      </template>
                    </el-menu-item>
                  </el-menu>
                </template>
                <template v-else-if="navLayoutStyle == 2">
                  <el-menu
                    :default-active="currentTreeNode.id"
                    mode="horizontal"
                    class="el-menu-vertical-demo"
                    :background-color="themeColor"
                    text-color="#333"
                  >
                    <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu>
                  </el-menu>
                </template>
              </div>
            </el-header>
          </el-container>
          <div class="right-config-container">
            <div class="right-config-container-left">
              <div class="title">菜单配置</div>
              <div class="left-content">
                <el-button type="primary" size="mini" @click="handleAddMainMenu"
                  >+添加主菜单</el-button
                >
                <el-tree
                  node-key="id"
                  draggable
                  :data="treeData"
                  :highlight-current="true"
                  default-expand-all
                  :expand-on-click-node="false"
                  check-on-click-node
                  @node-click="handleNodeClick"
                  :allow-drop="handleAllowDrop"
                >
                  <span
                    class="custom-tree-node"
                    slot-scope="{ node, data }"
                    @mouseenter="handleTreeNodeMouseEnter(node)"
                    @mouseleave="handleTreeNodeMouseLeave(node)"
                  >
                    <span>{{ node.label }}</span>
                    <span v-if="node.data.showOption && data.level < 3">
                      <i
                        class="el-icon-plus"
                        @click.stop="handleAddTreeSubNode(node)"
                      ></i>
                      <i
                        class="el-icon-delete"
                        @click.stop="handleDeleteTreeNode(node)"
                      ></i>
                    </span>
                  </span>
                </el-tree>
              </div>
            </div>
            <div class="right-config-container-right">
              <div class="title">内容设置</div>
              <div class="right-content" v-if="currentTreeNode.id">
                <div class="config-title">菜单显示名称</div>
                <el-input
                  class="config-input"
                  v-model="currentTreeNode.label"
                  size="mini"
                  placeholder="显示名称"
                ></el-input>
                <el-checkbox
                  class="config-checkbox"
                  v-model="currentTreeNode.showMenuIcon"
                  >显示菜单icon</el-checkbox
                >
                <el-select
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
                <el-checkbox
                  class="config-checkbox"
                  v-model="currentTreeNode.isMenuFoldindg"
                  >菜单允许折叠</el-checkbox
                >
                <el-checkbox
                  class="config-checkbox"
                  v-model="currentTreeNode.isMenuDefaultFolding"
                  >菜单默认折叠</el-checkbox
                >
                <!-- <el-checkbox class="config-checkbox" v-model="currentTreeNode.isNodeNull">设为空节点</el-checkbox> -->
                <div class="config-title">选择仪表盘</div>
                <el-select
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
                </el-select>
                <el-button
                  class="config-btn"
                  type="primary"
                  size="mini"
                  @click="handleUpdateTrend"
                  >更新</el-button
                >
                <div class="config-title">查看方式</div>
                <el-radio-group v-model="currentTreeNode.viewMode">
                  <el-radio class="config-radio" label="current"
                    >当前页面打开</el-radio
                  >
                  <el-radio class="config-radio" label="_blank"
                    >新窗口打开</el-radio
                  >
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
import { mapState, mapMutations, mapGetters } from "vuex";
import PortalMenu from "./PortalMenu.vue";
import { groupTree, initPanelData } from "@/api/panel/panel";
import PanelViewShow from "@/views/panel/list/PanelViewShow.vue";
import bus from "@/utils/bus";
export default {
  components: {
    PortalMenu,
    PanelViewShow,
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    themeColor: String,
    topNavPosRadio: String,
    navLayoutStyle: String,
    portalName: String, // 当前门户的名称
  },

  data() {
    return {
      activeTab: "edit", // 当前最顶部nav中是编辑还是预览
      topActiveTab: "0", // 当前顶部导航栏选择的下标选项
      treeData: [],
      treeId: 0,
      currentTreeNode: {},
      groupForm: {
        name: null,
        pid: null,
        panelType: "self",
        nodeType: null,
        children: [],
        sort: "create_time desc,node_type desc,name asc",
      },
      tData: [],
      showPanelView: false,
    };
  },
  computed: {
    syncVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },

    subTreeDatas() {
      const treeData = this.treeData.slice(0);
      if (this.navLayoutStyle == 0) {
        const subs = [];
        treeData.forEach((item) => {
          item.children.forEach((sub) => {
            subs.push(sub);
          });
        });
        return subs;
      } else if (this.navLayoutStyle == 1 && this.navLayoutStyle == 2) {
        return treeData;
      }
      return treeData;
    },

    trendData() {
      const found = this.tData.find(
        (item) => item.id == this.currentTreeNode.panelId
      );
      if (found) {
        return found.children;
      }
      return [];
    },
  },

  watch: {
    syncVisible(val) {
      if (val) {
        this.$nextTick(() => {
          if (this.$refs.panelViewShow) {
            this.$refs.panelViewShow.showMain = false;
          }
        });
      }
    },
  },

  mounted() {
    this.tree(true);
  },

  methods: {
    close() {
      this.syncVisible = false;
    },
    // 一级导航位置
    handleChangeTopNavPosRadio(radio) {
      console.log("radio", radio);
      // this.setTopNavPosRadio(radio);
    },
    // 选择主题设置
    handleChangeThemeColor(color) {
      console.log("color", color);
      // this.setThemeColor(color);
    },
    setNavLayoutStyle(style) {
      this.navLayoutStyle = style;
    },
    // 添加主菜单
    handleAddMainMenu() {
      this.treeData.push({
        id: (this.treeId += 1),
        label: "一级菜单",
        children: [],
        panelId: "",
        trendId: "",
        showMenuIcon: true, // 显示菜单icon
        isMenuFoldindg: true, // 菜单允许折叠
        isMenuDefaultFolding: true, // 菜单默认折叠
        isNodeNull: false, // 设置为空节点
        viewMode: "current", // _blank
        showOption: false,
        level: 1,
      });
    },
    _checkArrayHasValue(arr) {
      return arr && arr.length;
    },
    // 点击节点时候触发
    handleNodeClick(node) {
      // console.log("node", node);
      this.currentTreeNode = node;
    },
    // 鼠标移入该节点的时候触发
    handleTreeNodeMouseEnter(node) {
      // console.log("node enter", node);
      node.data.showOption = true;
    },
    // 鼠标移除该节点的时候触发
    handleTreeNodeMouseLeave(node) {
      // console.log("node leave", node);
      node.data.showOption = false;
    },
    // 添加子节点
    handleAddTreeSubNode(node) {
      console.log("handleAddTreeSubNode node", node);
      this.treeId += 1;
      const treeId = this.treeId;
      const level = node.data.level + 1;

      if (node.data.level == 1) {
        const foundIndex = this.treeData.findIndex(
          (item) => item.id == node.data.id
        );
        this.treeData[foundIndex].children.push({
          id: treeId,
          label: "二级菜单",
          children: [],
          showMenuIcon: true, // 显示菜单icon
          isMenuFoldindg: true, // 菜单允许折叠
          isMenuDefaultFolding: true, // 菜单默认折叠
          isNodeNull: false, // 设置为空节点
          viewMode: "current", // _blank
          showOption: false,
          level,
        });
      } else {
        const foundIndex = this.treeData.findIndex(
          (item) => item.id == node.parent.data.id
        );
        console.log("foundINdex", foundIndex);
        const children = this.treeData[foundIndex].children;
        console.log("children", children);
        const foundChildIndex = children.findIndex(
          (item) => item.id == node.data.id
        );
        console.log("foundChildIndex", foundChildIndex);
        this.treeData[foundIndex].children[foundChildIndex].children.push({
          id: treeId,
          label: "三级菜单",
          // children: [],
          showMenuIcon: true, // 显示菜单icon
          isMenuFoldindg: true, // 菜单允许折叠
          isMenuDefaultFolding: true, // 菜单默认折叠
          isNodeNull: false, // 设置为空节点
          viewMode: "current", // _blank
          showOption: false,
          level,
        });
      }
    },
    // 删除一个节点
    handleDeleteTreeSubNode(node) {
      console.log("handleDeleteTreeSubNode node", node);
    },
    // 选择一级菜单
    handleTopSelect(active) {
      console.log("active", active);
      this.topActiveTab = active;
    },
    // 节点拖拽进入到其他的节点
    handleAllowDrop(node, targetNode, type) {
      if (targetNode.level == 3) {
        return false;
      }
      return true;
    },
    // 选择一个趋势图
    handleTrendChange(data) {
      console.log("data", data);
      //   this.$store.commit("setComponentDataCache", null);
      //   initPanelData(data, function (response) {
      //     bus.$emit("set-panel-show-type", 0);
      //   });
    },

    handleUpdateTrend() {
      this.showPanelView = true;
      this.$store.commit("setComponentDataCache", null);
      initPanelData(this.currentTreeNode.trendId, function (response) {
        bus.$emit("set-panel-show-type", 0);
      });
    },

    tree(cache = false) {
      const modelInfo = localStorage.getItem("panel-main-tree");
      const userCache = modelInfo && cache;
      if (userCache) {
        this.tData = JSON.parse(modelInfo);
      }
      groupTree(this.groupForm, !userCache).then((res) => {
        localStorage.setItem("panel-main-tree", JSON.stringify(res.data));
        if (!userCache) {
          this.tData = res.data;
        }
      });
    },
  },
};
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
          display: flex;
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
    i {
      margin-right: 8px;
    }
    i:last-child {
      margin-right: 0;
    }
  }
}
</style>
