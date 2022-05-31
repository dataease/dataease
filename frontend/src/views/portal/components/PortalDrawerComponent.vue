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
          <div class="left">
            <i class="el-icon-close" @click="syncVisible = false"></i>
            <div class="name">{{ portalName || "未命名站点" }}</div>
          </div>
          <div class="right">
            <div class="wrapper">
              <i class="el-icon-setting"></i>
            </div>
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
                  active-text-color="#fff"
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
                  text-color="#fff"
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
              <el-main class="config-main">Main</el-main>
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
                  active-text-color="#fff"
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
                <input
                  class="name-input"
                  v-model="portalName"
                  size="small"
                  placeholder="未命名标题"
                />
              </div>
              <div class="wrapper">
                <div class="name">PC端样式</div>
                <div class="layout">
                  <div class="item" @click="setNavLayoutStyle(0)">
                    <img
                      :class="{ active: navLayoutStyle == 0 }"
                      src="./left-top-layout.png"
                      alt=""
                    />
                    <span class="item-name">双导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(1)">
                    <img
                      :class="{ active: navLayoutStyle == 1 }"
                      src="./only-left-layout.png"
                      alt=""
                    />
                    <span class="item-name">左导航布局</span>
                  </div>
                  <div class="item" @click="setNavLayoutStyle(2)">
                    <img
                      :class="{ active: navLayoutStyle == 2 }"
                      src="./only-top-layout.png"
                      alt=""
                    />
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
  </div>
</template>

<script>
import { mapState, mapMutations, mapGetters } from "vuex";
export default {
  props: {
    visible: {
      type: Boolean,
      default: false,
    },
  },

  data() {
    return {
      activeTab: "edit", // 当前最顶部nav中是编辑还是预览
      topActiveTab: "0", // 当前顶部导航栏选择的下标选项
      topNavPosRadio: "top", //  当前一级导航的位置
      themeColor: "#393c49", // 当前导航的颜色
      navLayoutStyle: "0", // 0 top-left 1 left 2 top
      portalName: "", // 当前门户的名称
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

  methods: {
    // ...mapMutations({
    //   setTopNavPosRadio: "SET_TOP_NAV_POS_RADIO",
    //   setThemeColor: "SET_THEME_COLOR",
    //   setNavLayoutStyle: "SET_NAV_LAYOUT_STYLE",
    // }),
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
    // 选择一级菜单
    handleTopSelect(active) {
      console.log("active", active);
      this.topActiveTab = active;
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
  ::v-deep .el-radio-group {
    .el-radio__label {
      color: #fff;
    }
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
      .left {
        display: flex;
        align-items: center;
        .name {
          margin-left: 14px;
        }
      }
      .right {
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
      color: #fff;
      min-height: calc(100vh - 60px);

      .config-container {
        flex: 3;
        .config-header {
          background-color: #242834;
          // min-height: 60px;
          display: flex;
          .title {
            // background-color: green;
            width: 200px;
            line-height: 60px;
            text-align: center;
          }
        }

        .config-aside {
          height: 100%;
        }

        .config-main {
          background-color: red;
        }
      }

      .right-config-container {
        flex: 1;
        background-color: #3b4760;
        .title {
          background-color: #273551;
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
            .name-input {
              width: 100%;
              border: 1px solid rgba(255, 255, 255, 0.5);
              background-color: #1e2b47;
              height: 28px;
              line-height: 28px;
              border-radius: 2px;
              padding: 0 6px;
              box-sizing: border-box;
              color: #fff;
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
                img {
                  width: 60px;
                  height: 60px;
                  margin-bottom: 8px;
                }
                .active {
                  border: 4px solid #409eff;
                  border-radius: 6px;
                  box-sizing: border-box;
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
