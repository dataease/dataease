<template>
  <div class="portal-drawer-container">
    <el-drawer
      :visible.sync="syncVisible"
      size="80%"
      direction="rtl"
      :show-close="false"
      :withHeader="false"
    >
      <div class="portal-drawer-container-container">
        <div class="header">
          <div class="headerleft">
            <i class="el-icon-close" @click="syncVisible = false"></i>
            <div class="name">{{ portalName || "未命名站点" }}</div>
          </div>
          <div class="headerright">
            <div class="wrapper">
              <i class="el-icon-setting" @click="handleOpenConfigDrawer"></i>
            </div>
            <div class="wrapper">
              <el-radio-group
                v-model="activeTab"
                size="mini"
                round
                @change="handleEditPreivewTab"
              >
                <el-radio-button label="edit">编辑</el-radio-button>
                <el-radio-button label="preview" :disabled="true">预览</el-radio-button>
              </el-radio-group>
            </div>
            <div class="wrapper">
              <el-button type="primary" round size="mini" @click="handleSave"
                >保 存</el-button
              >
            </div>
          </div>
        </div>
        <div class="content">
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
                <el-menu
                  :default-active="topActiveTab"
                  class="top-nav-menu"
                  mode="horizontal"
                  :background-color="themeColor"
                  active-text-color="#333"
                  @select="handleTopSelect"
                >
                  <el-menu-item index="0">一级菜单</el-menu-item>
                </el-menu>
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
                  default-active="0-1"
                  class="el-menu-vertical-demo"
                  :background-color="themeColor"
                  text-color="#333"
                >
                  <el-submenu index="0">
                    <template slot="title">
                      <i class="el-icon-menu"></i>
                      <span slot="title">二级菜单</span>
                    </template>
                    <el-menu-item index="0-1">
                      <span slot="title">三级菜单</span>
                    </el-menu-item>
                  </el-submenu>
                </el-menu>
              </el-aside>
              <el-main class="config-main">
                <img src="./portal-trend-bg.png" alt="" />
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
                <el-menu
                  :default-active="topActiveTab"
                  class="top-nav-menu"
                  mode="horizontal"
                  :background-color="themeColor"
                  active-text-color="#333"
                  @select="handleTopSelect"
                >
                  <el-menu-item
                    index="0"
                    :style="{ borderColor: topActiveTab == 0 ? '#429eff' : '' }"
                    >一级菜单</el-menu-item
                  >
                  <!-- <el-menu-item index="1" :style="{ borderColor: topActiveTab == 1 ? '#429eff' : '' }">二级菜单</el-menu-item> -->
                </el-menu>
              </div>
            </el-header>
          </el-container>
          <div class="right-config-container">
            <div class="title">门户配置</div>
            <div class="config-wrapper">
              <div class="wrapper horization">
                <div class="name">标题设置</div>
                <el-input
                  v-model="portalName"
                  size="mini"
                  placeholder="未命名标题"
                />
              </div>
              <div class="wrapper">
                <div class="name">PC端样式</div>
                <div class="layout">
                  <div class="item" @click="setNavLayoutStyle(0)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 0 }"
                      src="./left-top-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="pltl"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 0 }"
                    ></svg-icon>
                    <span class="item-name">双导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(1)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 1 }"
                      src="./only-left-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="poll"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 1 }"
                    ></svg-icon>
                    <span class="item-name">左导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(2)">
                    <!-- <img
                      :class="{ active: navLayoutStyle == 2 }"
                      src="./only-top-layout.png"
                      alt=""
                    /> -->
                    <svg-icon
                      icon-class="potl"
                      class="icon-layout"
                      :class="{ active: navLayoutStyle == 2 }"
                    ></svg-icon>
                    <span class="item-name">顶部导航布局</span>
                  </div>
                </div>
              </div>
              <div class="wrapper">
                <div class="name">一级导航位置</div>
                <el-radio-group
                  v-model="topNavPosRadio"
                  @change="handleChangeTopNavPosRadio"
                >
                  <el-radio label="top">顶部</el-radio>
                  <el-radio label="bottom">底部</el-radio>
                </el-radio-group>
              </div>
              <div class="wrapper">
                <div class="name">主题设置</div>
                <el-color-picker
                  v-model="themeColor"
                  size="small"
                  @change="handleChangeThemeColor"
                ></el-color-picker>
              </div>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
    <template v-if="openType == 'add'">
      <PortConfigDrawerComponent
        :visible.sync="showPortConfigDrawerComponent"
        :portalName="portalName"
        :themeColor="themeColor"
        :navLayoutStyle="navLayoutStyle"
        :topNavPosRadio="topNavPosRadio"
        :lastTreeId="lastTreeId"
        :open-type="openType"
        :config="{}"
        :portalId="null"
        @treeData="handleGetTreeData"
        @close="syncVisible = false"
      ></PortConfigDrawerComponent>
    </template>
    <template v-else>
      <PortConfigDrawerComponent
        :visible.sync="showPortConfigDrawerComponent"
        :portalName="portalName"
        :themeColor="themeColor"
        :navLayoutStyle="navLayoutStyle"
        :topNavPosRadio="topNavPosRadio"
        :lastTreeId="lastTreeId"
        :open-type="openType"
        :config="item.config"
        :portalId="item.id"
        @treeData="handleGetTreeData"
        @close="syncVisible = false"
      ></PortConfigDrawerComponent>
    </template>
    <template v-if="privewPortal">
      <PanelViewShow
        ref="panelViewShow"
        :portal="privewPortal"
        @update="update"
      ></PanelViewShow>
    </template>
  </div>
</template>

<script>
import PortConfigDrawerComponent from "./PortalConfigDrawerComponent.vue";
import PanelViewShow from "@/views/panel/list/PanelViewShow.vue";
import bus from "@/utils/bus";
export default {
  components: {
    PortConfigDrawerComponent,
    PanelViewShow
  },
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
    openType: {
      type: String,
      default: "add", //edit
    },
    item: {
      type: Object,
      default: () => {},
    },
  },

  data() {
    return {
      showPortConfigDrawerComponent: false,
      activeTab: "edit", // 当前最顶部nav中是编辑还是预览
      topActiveTab: "0", // 当前顶部导航栏选择的下标选项
      topNavPosRadio: "top", //  当前一级导航的位置
      themeColor: "#f1f3f8", // 当前导航的颜色
      navLayoutStyle: "0", // 0 top-left 1 left 2 top
      portalName: "", // 当前门户的名称
      lastTreeId: 0, // 当前的树节点的跟节点
      tmpTreeData: null, // 获取配置页面的treeData
      privewPortal: null,
      priviewBtnEnable: false,
    };
  },

  computed: {
    // ...mapGetters([
    //   "navLayoutStyle", // 0 top-left 1 left 2 top
    // ]),
    syncVisible: {
      get() {
        return this.visible;
      },
      set(val) {
        this.$emit("update:visible", val);
      },
    },
  },

  watch: {
    syncVisible(val) {
      if (val) {
        // 如果是新打开的状态
        this.__initData();
      }
    },
  },

  methods: {
    // ...mapMutations({
    //   setTopNavPosRadio: "SET_TOP_NAV_POS_RADIO",
    //   setThemeColor: "SET_THEME_COLOR",
    //   setNavLayoutStyle: "SET_NAV_LAYOUT_STYLE",
    // }),

    __initData() {
      this.activeTab = "edit"; // 当前最顶部nav中是编辑还是预览
      this.topActiveTab = "0"; // 当前顶部导航栏选择的下标选项
      this.tmpTreeData = null;
      if (this.openType == "add") {
        this.topNavPosRadio = "top"; //  当前一级导航的位置
        this.themeColor = "#f1f3f8"; // 当前导航的颜色
        this.navLayoutStyle = "0"; // 0 top-left 1 left 2 top
        this.portalName = ""; // 当前门户的名称
        this.lastTreeId = 0;
      } else {
        this.topNavPosRadio = this.item.topNavPosRadio; //  当前一级导航的位置
        this.themeColor = this.item.themeColor; // 当前导航的颜色
        this.navLayoutStyle = this.item.navLayoutStyle; // 0 top-left 1 left 2 top
        this.portalName = this.item.portalName; // 当前门户的名称
        this.lastTreeId = this.item.lastTreeId;
      }
      // this.priviewBtnEnable = !!this.item.config.treeData || !!this.tmpTreeData
    },

    /***************** 预览 start ****************/
    handleEditPreivewTab(evt) {
      console.log("evt -- ", evt);
      if (evt == "edit") {
        return;
      }
      this.privewPortal = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || "未命名站点", // 站点名称
        config: {
          treeData: Object.assign({}, this.item.config.treeData, this.tmpTreeData)
        },
      };
      this.$nextTick(() => {
        console.log("this.$refs.panelViewShow", this.$refs.panelViewShow)
        if (this.$refs.panelViewShow) {
          this.$refs.panelViewShow.clickFullscreen();

          this.$watch(
            () => this.$refs.panelViewShow.fullscreen,
            (val) => {
              if (!val) {
                this.activeTab = "edit";
                this.privewPortal = null;
              }
            }
          );
        }
      });
    },

    update(trendId) {
      if (trendId) {
        if (Object.prototype.toString.call(trendId) == "[object Array]") {
          trendId = trendId[trendId.length - 1];
        }
        initPanelData(trendId, function (response) {
          bus.$emit("set-panel-show-type", 0);
        });
      } else {
        this.$refs.panelViewShow.showMain = false;
      }
    },

    /***************** 预览 end ****************/

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
      this.navLayoutStyle = style.toString();
    },
    // 选择一级菜单
    handleTopSelect(active) {
      console.log("active", active);
      this.topActiveTab = active;
    },

    // 获取配置页面的treeData
    handleGetTreeData(treeData) {
      this.tmpTreeData = treeData;
    },
    // 打开配置
    handleOpenConfigDrawer() {
      this.showPortConfigDrawerComponent = true;
    },
    // 保存
    handleSave() {
      const getTreeData = () => {
        debugger;
        if (this.tmpTreeData) {
          return this.tmpTreeData;
        }
        if (this.item && this.item.config && this.item.config.treeData) {
          return this.item.config.treeData;
        }
        return [];
      };
      const params = {
        navLayoutStyle: this.navLayoutStyle, // 0-双导航布局 1-左导航布局 2-顶部导航布局
        topNavPosRadio: this.topNavPosRadio, // top-底部 bottom-底部
        themeColor: this.themeColor, // 默认
        portalName: this.portalName || "未命名站点", // 站点名称
        lastTreeId: this.lastTreeId,
        config: {
          treeData: getTreeData(),
        },
      };
      const positionJson = JSON.stringify(params);
      if (this.openType == "add") {
        bus.$emit("savePortal", { positionJson });
      } else {
        bus.$emit("updatePortal", { id: this.item.id, positionJson });
      }
      this.syncVisible = false;
    },
  },
};
</script>

<style lang="less" scoped>
.portal-drawer-container {
  ::v-deep .el-menu,
  .el-menu.el-menu--horizontal {
    border: unset;
  }
  ::v-deep .el-header {
    padding: 0;
  }
  i {
    cursor: pointer;
  }
  .portal-drawer-container-container {
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
    .content {
      display: flex;
      justify-content: space-between;
      font-size: 14px;
      color: #333;
      min-height: calc(100vh - 60px);

      .config-container {
        flex: 3;
        .config-header {
          background-color: var(--TopBG, #f1f3f8);
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
          display: flex;
          align-items: center;
          justify-content: center;
          background-color: var(--TopBG, #e6e6e6);
          img {
            width: 400px;
          }
        }
      }

      .right-config-container {
        flex: 1;
        background-color: var(--TopBG, #f1f3f8);
        .title {
          // background-color: var(--TopBG, #e6e6e6);
          border-bottom: 1px solid var(--TopBG, #e6e6e6);
          border-left: 1px solid var(--TopBG, #e6e6e6);
          height: 60px;
          line-height: 60px;
          padding-left: 20px;
          box-sizing: border-box;
        }
        .config-wrapper {
          .wrapper.horization {
            display: flex;
            align-items: center;
            .name {
              margin-right: 10px;
              margin-bottom: 0;
              flex-shrink: 0;
            }
          }
          padding: 14px;
          box-sizing: border-box;
          .wrapper {
            margin-bottom: 20px;
            .name {
              margin-bottom: 8px;
            }
            .layout {
              display: flex;
              align-items: center;
              justify-content: space-around;
              .item {
                display: flex;
                flex-direction: column;
                align-items: center;
                justify-content: center;
                .icon-layout {
                  font-size: 69px;
                  font-weight: 400;
                  color: #666;
                  margin-bottom: 8px;
                }
                .active {
                  color: #409eff;
                  // border: 2px solid #409eff;
                  // border-radius: 4px;
                  // box-sizing: border-box;
                }
              }
            }
          }
        }
      }
    }
  }
}
</style>
