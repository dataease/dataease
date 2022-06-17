<template>
  <div class="portal-nav-menu-container">
    <el-container class="config-container">
      <el-header
        v-if="
          (portal.navLayoutStyle == 0 || portal.navLayoutStyle == 2) &&
            portal.topNavPosRadio == 'top'
        "
        class="config-header"
        :style="{ backgroundColor: portal.themeColor,
                  justifyContent:portal.headerNavStyle==='1'?portal.floatPosition:'flex-start',
                  backgroundImage:`url(${portal.navBageImg})`,
                  height:portal.titleFontSet.height+'px'
        }"
      >
        <div
          v-for="(item,index) in portal.config.treeData"
          v-show="portal.headerNavStyle==='1'&&( portal.floatPosition ==='center'||portal.floatPosition ==='flex-end')&&JudgmentTwo(portal.floatPosition,index)"
          :key="item.id"
          :style="menuStyleSet"
          @click="changePage(item,index)"
        >
          {{ item.label }}
        </div>
        <div v-if="portal.headerNavStyle==='1'" :style="titleStyleSet">{{ portal.portalName || "未命名站点" }}</div>
        <div
          v-for="(item,indexs) in portal.config.treeData"
          v-show="portal.headerNavStyle==='1'&&( portal.floatPosition ==='center'||portal.floatPosition ==='flex-end')&&Judgment(portal.floatPosition,indexs)"
          :key="item.id"
          :style="menuStyleSet"
          @click="changePage(item,indexs)"
        >
          {{ item.label }}
        </div>
        <!-- <div>{{ portal }}</div> -->
        <!-- <div
          v-for="item in portal.config.treeData"
          :key="item.id"
        >212</div> -->

        <div v-if="portal.headerNavStyle==='0'" class="title">{{ portal.portalName || "未命名站点" }}</div>
        <!-- <div>55555</div> 此处为全屏预览头部模式下的切换tab -->
        <div v-if="portal.headerNavStyle==='0'" class="tabs">
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
                  <i :class="[item.iconName]" />
                  <span slot="title">{{ item.label }}</span>
                </template>
              </el-menu-item>
            </el-menu>
          </template>
          <template v-else-if="portal.navLayoutStyle == 2">
            <!-- <el-menu
              :default-active="currentTreeNode.id"
              mode="horizontal"
              menu-trigger="click"
              class="el-menu-vertical-demo"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
            > -->
            <!-- <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu> -->
            <PortalMenu
              :sub-tree-datas="subTreeDatas"
              :current-tree-node="currentTreeNode"
              :theme-color="portal.themeColor"
              @handleMenuSelect="handleMenuSelect"
            />

            <!-- </el-menu> -->
          </template>
        </div>
      </el-header>
      <el-container>
        <el-aside
          v-if="portal.navLayoutStyle == 0 || portal.navLayoutStyle == 1"
          class="config-aside"
          width="200px"
          :style="{ backgroundColor: portal.themeColor }"
        >

          <!-- <el-menu
            :default-active="currentTreeNode.id"
            class="el-menu-vertical-demo"
            :background-color="portal.themeColor"
            active-text-color="#409eff"
            @select="handleMenuSelect"
          > -->
          <!-- <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu> -->
          <PortalMenu
            mode="vertical"
            :sub-tree-datas="subTreeDatas"
            :current-tree-node="currentTreeNode"
            :theme-color="portal.themeColor"
            @handleMenuSelect="handleMenuSelect"
          />

          <!-- </el-menu> -->
        </el-aside>
        <el-main class="config-main">
          <!-- config-main -->
          <slot />
        </el-main>
      </el-container>
      <el-header
        v-if="
          (portal.navLayoutStyle == 0 || portal.navLayoutStyle == 2) &&
            portal.topNavPosRadio == 'bottom'
        "
        class="config-header config-bottom-header"
        :style="{ backgroundColor: portal.themeColor }"
      >
        <div>头部设置4444</div>
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
                  <i :class="[item.iconName]" />
                  <span slot="title">{{ item.label }}</span>
                </template>
              </el-menu-item>
            </el-menu>
          </template>
          <template v-else-if="portal.navLayoutStyle == 2">
            <!-- <el-menu
              :default-active="currentTreeNode.id"
              mode="horizontal"
              menu-trigger="click"
              class="el-menu-vertical-demo"
              :background-color="portal.themeColor"
              active-text-color="#409eff"
              @select="handleMenuSelect"
            > -->
            <!-- <PortalMenu :subTreeDatas="subTreeDatas"></PortalMenu> -->
            <PortalMenu
              :sub-tree-datas="subTreeDatas"
              :current-tree-node="currentTreeNode"
              :theme-color="portal.themeColor"
              @handleMenuSelect="handleMenuSelect"
            />

            <!-- </el-menu> -->
          </template>
        </div>
      </el-header>
    </el-container>
  </div>
</template>

<script>
import PortalMenu from './PortalMenu.vue'
export default {
  components: {
    PortalMenu
  },
  props: {
    portal: {
      type: Object,
      default: () => {}
    }
  },

  data() {
    return {
      currentTreeNode: {}
    }
  },

  computed: {
    // titleStyleSet(){
    //   // {width:portal.titleWidth+'%',textAlign:'center'}

    // },
    titleStyleSet() {
      // {width:titleWidth+'%',textAlign:'center'}
      console.log('this.titleFontSet', this.titleFontSet)
      const style = {}
      style.width = this.portal.titleWidth + '%'
      style.textAlign = 'center'
      if (this.portal.titleFontSet) {
        style.color = this.portal.titleFontSet.color
        style.fontSize = this.portal.titleFontSet.fontSize + 'px'
        style.fontFamily = this.portal.titleFontSet.fontFamily
        style.opacity = this.portal.titleFontSet.opacity
      }

      return style
    },
    menuStyleSet() {
      // {width:menuWidth+'%',textAlign:'center'}
      const style = {}
      style.width = this.portal.menuWidth / 10 + '%'
      style.textAlign = 'center'
      if (this.portal.menuFontSet) {
        style.color = this.portal.menuFontSet.color
        style.fontSize = this.portal.menuFontSet.fontSize + 'px'
        style.fontFamily = this.portal.menuFontSet.fontFamily
      }
      style.cursor = 'pointer'

      return style
    },
    subTreeDatas() {
      const treeData = this.portal.config.treeData.slice(0)
      if (this.portal.navLayoutStyle === 0) {
        const subs = []
        treeData.forEach((item) => {
          item.children.forEach((sub) => {
            subs.push(sub)
          })
        })
        return subs
      } else if (
        this.portal.navLayoutStyle === 1 ||
        this.portal.navLayoutStyle === 2
      ) {
        return treeData
      }
      return treeData
    }
  },

  mounted() {
    const that = this
    function getTreedDataFirstTrendId(treeData) {
      for (let i = 0; i < treeData.length; i++) {
        const item = treeData[i]
        if (item.trendId && !that.currentTreeNode.id) {
          console.log('item.label', item.label)
          that.currentTreeNode = item
        } else {
          getTreedDataFirstTrendId(item.children)
        }
      }
    }
    getTreedDataFirstTrendId(this.portal.config.treeData)
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
    changePage(item, index) {
      console.log('item切换页面', item, index, this.portal)
      this.handleMenuSelect(item.id)
    },
    handleMenuSelect(active) {
      console.log('----- active', active)
      const that = this
      function getTreedDataActive(treeData) {
        for (let i = 0; i < treeData.length; i++) {
          const item = treeData[i]
          if (item.id === active) {
            console.log('item.label', item.label)
            that.$emit('update', item.trendId)
          } else {
            getTreedDataActive(item.children)
          }
        }
      }
      // console.log('getTreedDataActive(this.portal.config.treeData)', getTreedDataActive(this.portal.config.treeData))
      getTreedDataActive(this.portal.config.treeData)
    }
  }
}
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
    .menu_class{
      flex:1;
      display:flex;
      align-items:center;
      justify-content:flex-end;
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
    .config-aside-fixed {
      position: fixed;
      left: 0;
      right: 0;
      top: 60px;
      z-index: 9999;
      height: calc(100% - 60px);
    }

    .config-main {
      padding: 0 !important;
      background-color: var(--TopBG, #e6e6e6);
    }
  }
}
</style>
