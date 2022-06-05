<template>
  <div class="portal-nav-menu-container">
    <el-container class="config-container">
      <el-header
        class="config-header config-top-header"
        :style="{ backgroundColor: portal.themeColor }"
        v-if="
          (portal.navLayoutStyle == 0 || portal.navLayoutStyle == 2) &&
          portal.topNavPosRadio == 'top'
        "
      >
        <div class="title">{{ portal.portalName || "未命名站点" }}</div>
        <div class="tabs">
          <template v-if="portal.navLayoutStyle == 0">
            <el-menu
              :default-active="currentTreeNode.id"
              class="top-nav-menu"
              mode="horizontal"
              menu-trigger="click"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
            >
              <el-menu-item
                v-for="item in portal.config.treeData"
                :key="item.id"
                :index="item.id"
              >
                <template slot="title">
                  <i :class="[item.iconName]"></i>
                  <span slot="title">{{ item.label }}</span>
                </template>
              </el-menu-item>
            </el-menu>
          </template>
          <template v-else-if="portal.navLayoutStyle == 2">
            <el-menu
              :default-active="currentTreeNode.id"
              mode="horizontal"
              menu-trigger="click"
              class="el-menu-vertical-demo"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
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
          :style="{ backgroundColor: portal.themeColor }"
          v-if="portal.navLayoutStyle == 0 || portal.navLayoutStyle == 1"
        >
          <el-menu
            :default-active="currentTreeNode.id"
            class="el-menu-vertical-demo"
            :background-color="portal.themeColor"
            active-text-color="#409eff"
            @select="handleMenuSelect"
          >
            <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu>
          </el-menu>
        </el-aside>
        <el-main class="config-main">
          <slot></slot>
        </el-main>
      </el-container>
      <el-header
        class="config-header config-bottom-header"
        :style="{ backgroundColor: portal.themeColor }"
        v-if="
          (portal.navLayoutStyle == 0 || portal.navLayoutStyle == 2) &&
          portal.topNavPosRadio == 'bottom'
        "
      >
        <div class="title">{{ portal.portalName || "未命名站点" }}</div>
        <div class="tabs">
          <template v-if="portal.navLayoutStyle == 0">
            <el-menu
              :default-active="currentTreeNode.id"
              class="top-nav-menu"
              mode="horizontal"
              menu-trigger="click"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
            >
              <el-menu-item
                v-for="item in portal.config.treeData"
                :key="item.id"
                :index="item.id"
              >
                <template slot="title">
                  <i :class="[item.iconName]"></i>
                  <span slot="title">{{ item.label }}</span>
                </template>
              </el-menu-item>
            </el-menu>
          </template>
          <template v-else-if="portal.navLayoutStyle == 2">
            <el-menu
              :default-active="currentTreeNode.id"
              mode="horizontal"
              menu-trigger="click"
              class="el-menu-vertical-demo"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
            >
              <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu>
            </el-menu>
          </template>
        </div>
      </el-header>
    </el-container>
  </div>
</template>

<script>
import PortalMenu from "./PortalMenu.vue";
export default {
  components: {
    PortalMenu,
  },
  props: {
    portal: {
      type: Object,
      default: () => {},
    },
  },

  data() {
    return {
      currentTreeNode: {},
    };
  },

  mounted() {
    const that = this;
    function getTreedDataFirstTrendId(treeData) {
      for (let i = 0; i < treeData.length; i++) {
        const item = treeData[i];
        if (item.trendId && !that.currentTreeNode.id) {
          console.log("item.label", item.label);
          that.currentTreeNode = item;
        } else {
          getTreedDataFirstTrendId(item.children);
        }
      }
    }
    getTreedDataFirstTrendId(this.portal.config.treeData);
  },

  computed: {
    subTreeDatas() {
      const treeData = this.portal.config.treeData.slice(0);
      if (this.portal.navLayoutStyle == 0) {
        const subs = [];
        treeData.forEach((item) => {
          item.children.forEach((sub) => {
            subs.push(sub);
          });
        });
        return subs;
      } else if (
        this.portal.navLayoutStyle == 1 &&
        this.portal.navLayoutStyle == 2
      ) {
        return treeData;
      }
      return treeData;
    },
  },

  methods: {
    handleMenuSelect(active) {
      console.log("----- active", active);
      const that = this;
      function getTreedDataActive(treeData) {
        for (let i = 0; i < treeData.length; i++) {
          const item = treeData[i];
          if (item.id == active) {
            console.log("item.label", item.label);
            that.$emit("update", item.trendId);
          } else {
            getTreedDataActive(item.children);
          }
        }
      }
      getTreedDataActive(this.portal.config.treeData);
    },
  },
};
</script>

<style lang="scss" scoped>
.portal-nav-menu-container {

  display: flex;
  justify-content: space-between;
  font-size: 14px;
  color: #333;
  min-height: calc(100vh);

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

  ::v-deep .el-menu,
  .el-menu.el-menu--horizontal {
    border: unset;
  }
  ::v-deep .el-header {
    padding: 0;
  }

  .config-container {
    flex: 3;
    position: relative;
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
    .config-bottom-header {
      position: fixed;
      left: 0;
      right: 0;
      bottom: 0;
      z-index: 9999;
    }
    .config-top-header {
      position: fixed;
      left: 0;
      right: 0;
      top: 0;
      z-index: 9999;
    }

    .config-aside {
      height: 100%;
    }

    .config-main {
      padding: 0 !important;
      background-color: var(--TopBG, #e6e6e6);
    }
  }
}
</style>
