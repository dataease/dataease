<template>
  <div class="top-nav">
    <div
      v-loading="!axiosFinished"
      class="log"
    >
      <svg-icon
        v-if="!logoUrl && axiosFinished"
        icon-class="DataEase"
        custom-class="top-nav-logo-icon"
      />
      <img
        v-if="logoUrl && axiosFinished"
        :src="logoUrl"
        width="140"
        alt=""
        style="padding-top: 10px;"
      >
    </div>
    <el-menu
      class="de-top-menu"
      mode="horizontal"
      :style="{'margin-left': '260px', 'position': 'absolute'}"
      active-text-color="#FFFFFF"
      :default-active="activeMenu"
      @select="handleSelect"
    >
      <div
        v-for="item in permission_routes"
        :key="item.path"
        class="nav-item"
      >
        <app-link :to="resolvePath(item)">
          <el-menu-item
            v-if="!item.hidden"
            :index="item.path"
          >
            {{ item.meta ? item.meta.title : item.children[0].meta.title }}
          </el-menu-item>
        </app-link>
      </div>
    </el-menu>

    <div
      class="right-menu"
      style="color: var(--TopTextColor)"
    >
      <notification class="right-menu-item hover-effect" />
      <lang-select class="right-menu-item hover-effect" />
      <div
        style="height: 100%;padding: 0 8px;"
        class="right-menu-item hover-effect"
      >
        <a
          :href="helpLink"
          target="_blank"
          style="display: flex;height: 100%;width: 100%;justify-content: center;align-items: center;"
        >
          <svg-icon icon-class="docs" />
        </a>
      </div>

      <el-dropdown
        ref="my-drop"
        class="top-dropdown"
        style="display: flex;align-items: center; width:100px;"
        trigger="click"
      >
        <div
          class="el-dropdown-link"
          style="display: flex;color: var(--TopTextColor);font-size: 14px; width:100%;"
        >
          <span style="max-width:80px;white-space:nowrap;text-overflow:ellipsis;overflow:hidden;">{{ name }}</span>
          <span><i class="el-icon-arrow-down el-icon--right" /></span>
        </div>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/person-info/index">
            <el-dropdown-item>{{ $t('commons.personal_info') }}</el-dropdown-item>
          </router-link>

          <router-link
            v-if="$store.getters.validate"
            to="/ukey/index"
          >
            <el-dropdown-item>{{ $t('commons.ukey_title') }}</el-dropdown-item>
          </router-link>

          <router-link
            v-if="!isOtherPlatform"
            to="/person-pwd/index"
          >
            <el-dropdown-item>{{ $t('user.change_password') }}</el-dropdown-item>
          </router-link>

          <router-link to="/about/index">
            <el-dropdown-item>{{ $t('commons.about_us') }}</el-dropdown-item>
          </router-link>
          <el-dropdown-item
            v-if="!isOtherPlatform"
            divided
            @click.native="logout"
          >
            <span style="display:block;">{{ $t('commons.exit_system') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!--模板市场全屏显示框-->
    <el-dialog
      :visible="templateMarketShow"
      :show-close="false"
      class="dialog-css"
      :fullscreen="true"
      append-to-body
    >
      <template-market
        v-if="templateMarketShow"
        style="text-align: center"
        @closeDialog="changeTemplateMarketShow(false)"
      />
    </el-dialog>
  </div>

</template>

<script>
import { mapGetters } from 'vuex'
import AppLink from './Sidebar/Link'
import variables from '@/styles/variables.scss'
import { isExternal } from '@/utils/validate'
import Notification from '@/components/notification'
import bus from '@/utils/bus'
import LangSelect from '@/components/langSelect'
import { getSysUI } from '@/utils/auth'
import { pluginLoaded } from '@/api/user'
import { initTheme } from '@/utils/ThemeUtil'
import TemplateMarket from '@/views/panel/templateMarket'
import { changeFavicon, inOtherPlatform } from '@/utils/index'
export default {
  name: 'Topbar',
  components: {
    TemplateMarket,
    AppLink,
    Notification,
    LangSelect

  },
  props: {
    showTips: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      uiInfo: null,
      logoUrl: null,
      axiosFinished: false,
      isPluginLoaded: false,
      templateMarketShow: false
    }
  },

  computed: {
    theme() {
      return this.$store.state.settings.theme
    },

    topMenuColor() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.topMenuColor'] && this.$store.getters.uiInfo[
        'ui.topMenuColor'].paramValue) {
        return this.$store.getters.uiInfo['ui.topMenuColor'].paramValue
      }
      return this.variables.topBarBg
    },
    topMenuActiveColor() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.topMenuActiveColor'] && this.$store.getters
        .uiInfo['ui.topMenuActiveColor'].paramValue) {
        return this.$store.getters.uiInfo['ui.topMenuActiveColor'].paramValue
      }
      return this.variables.topBarMenuActive
    },
    topMenuTextColor() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.topMenuTextColor'] && this.$store.getters
        .uiInfo['ui.topMenuTextColor'].paramValue) {
        return this.$store.getters.uiInfo['ui.topMenuTextColor'].paramValue
      }
      return this.variables.topBarMenuText
    },
    topMenuTextActiveColor() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.topMenuTextActiveColor'] && this.$store.getters
        .uiInfo['ui.topMenuTextActiveColor'].paramValue) {
        return this.$store.getters.uiInfo['ui.topMenuTextActiveColor'].paramValue
      }
      return this.variables.topBarMenuTextActive
    },
    helpLink() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.helpLink'] && this.$store.getters.uiInfo['ui.helpLink'].paramValue) {
        return this.$store.getters.uiInfo['ui.helpLink'].paramValue
      }
      return 'https://dataease.io/docs/'
    },
    /* topMenuColor() {
        return this.$store.getters.uiInfo.topMenuColor
      }, */
    activeMenu() {
      const route = this.$route
      const {
        meta,
        path
      } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        // return meta.activeMenu
      }
      // 如果是首页，首页高亮
      if (path === '/panel') {
        return '/'
      }
      // 如果不是首页，高亮一级菜单
      const activeMenu = '/' + path.split('/')[1]
      return activeMenu
    },
    variables() {
      return variables
    },
    sidebar() {
      return this.$store.state.app.sidebar
    },
    isOtherPlatform() {
      return inOtherPlatform()
    },
    ...mapGetters([
      'avatar',
      'permission_routes',
      'name'
    ])
  },

  mounted() {
    window.addEventListener('beforeunload', (e) => this.beforeunloadHandler(e))
    window.addEventListener('unload', (e) => this.unloadHandler(e))

    this.initCurrentRoutes()
    bus.$on('set-top-menu-info', this.setTopMenuInfo)
    bus.$on('set-top-menu-active-info', this.setTopMenuActiveInfo)
    bus.$on('set-top-text-info', this.setTopTextInfo)
    bus.$on('set-top-text-active-info', this.setTopTextActiveInfo)
    bus.$on('sys-logout', this.logout)
    this.showTips && this.$nextTick(() => {
      const drop = this.$refs['my-drop']
      drop && drop.show && drop.show()
    })
  },
  beforeDestroy() {
    window.removeEventListener('beforeunload', (e) => this.beforeunloadHandler(e))
    window.removeEventListener('unload', (e) => this.unloadHandler(e))

    bus.$off('set-top-menu-info', this.setTopMenuInfo)
    bus.$off('set-top-menu-active-info', this.setTopMenuActiveInfo)
    bus.$off('set-top-text-info', this.setTopTextInfo)
    bus.$off('set-top-text-active-info', this.setTopTextActiveInfo)
    bus.$off('sys-logout', this.logout)
  },
  created() {
    this.loadUiInfo()
  },
  beforeCreate() {
    pluginLoaded().then(res => {
      this.isPluginLoaded = res.success && res.data
      if (this.isPluginLoaded) {
        initTheme()
      }
    })
  },
  methods: {
    beforeunloadHandler() {
      this.beforeUnload_time = new Date().getTime()
    },
    unloadHandler(e) {
      this.gap_time = new Date().getTime() - this.beforeUnload_time
      if (this.gap_time <= 5) {
        this.logout().then(res => {})
      }
    },

    // 通过当前路径找到二级菜单对应项，存到store，用来渲染左侧菜单
    initCurrentRoutes() {
      const {
        path
      } = this.$route
      let route = this.permission_routes.find(
        item => item.path === '/' + path.split('/')[1]
      )
      // 如果找不到这个路由，说明是首页
      if (!route) {
        route = this.permission_routes.find(item => item.path === '/')
      }
      this.$store.commit('permission/SET_CURRENT_ROUTES', route)
      this.setSidebarHide(route)
    },
    // 判断该路由是否只有一个子项或者没有子项，如果是，则在一级菜单添加跳转路由
    isOnlyOneChild(item) {
      if (item.children && item.children.length === 1) {
        return true
      }
      return false
    },
    resolvePath(item) {
      // 如果是个完成的url直接返回
      if (isExternal(item.path)) {
        return item.path
      }
      // 如果是首页，就返回重定向路由
      if (item.path === '/') {
        const path = item.redirect
        return path
      }

      // 如果有子项，默认跳转第一个子项路由
      let path = ''
      /**
       * item 路由子项
       * parent 路由父项
       */
      const getDefaultPath = (item, parent) => {
        // 如果path是个外部链接（不建议），直接返回链接，存在个问题：如果是外部链接点击跳转后当前页内容还是上一个路由内容
        if (isExternal(item.path)) {
          path = item.path
          return
        }
        // 第一次需要父项路由拼接，所以只是第一个传parent
        if (parent) {
          path += (parent.path + '/' + item.path)
        } else {
          path += ('/' + item.path)
        }
        // 如果还有子项，继续递归
        if (item.children) {
          getDefaultPath(item.children[0])
        }
      }

      if (item.children) {
        getDefaultPath(item.children[0], item)
        return path
      }

      return item.path
    },
    handleSelect(key, keyPath) {
      // 把选中路由的子路由保存store
      const route = this.permission_routes.find(item => item.path === key)
      this.$store.commit('permission/SET_CURRENT_ROUTES', route)
      this.setSidebarHide(route)
    },
    // 设置侧边栏的显示和隐藏
    setSidebarHide(route) {
      const hidePaths = ['/person-info', '/person-pwd', '/about']
      if (hidePaths.includes(route.path)) {
        this.$store.dispatch('app/toggleSideBarHide', true)
        return
      }
      //   if (!route.children || route.children.length === 1) {
      if (route.name !== 'system' && (!route.children || this.showChildLength(route) === 1)) {
        this.$store.dispatch('app/toggleSideBarHide', true)
      } else {
        this.$store.dispatch('app/toggleSideBarHide', false)
      }
    },
    // 获取非隐藏子路由的个数
    showChildLength(route) {
      if (!route || !route.children) {
        return 0
      }
      return route.children.filter(kid => !kid.hidden).length
    },
    async logout(param) {
      const result = await this.$store.dispatch('user/logout', param)
      if (result !== 'success' && result !== 'fail') {
        window.location.href = result
      } else {
        this.$router.push(`/login?redirect=${this.$route.fullPath}`)
      }
    },
    loadUiInfo() {
      this.$store.dispatch('user/getUI').then((res) => {
        this.uiInfo = getSysUI()
        if (!this.uiInfo || Object.keys(this.uiInfo).length === 0) {
          this.uiInfo = res
        }
        if (this.uiInfo['ui.logo'] && this.uiInfo['ui.logo'].paramValue) {
          this.logoUrl = '/system/ui/image/' + this.uiInfo['ui.logo'].paramValue
        }
        if (this.uiInfo['ui.theme'] && this.uiInfo['ui.theme'].paramValue) {
          const val = this.uiInfo['ui.theme'].paramValue
          this.$store.dispatch('settings/changeSetting', {
            key: 'theme',
            value: val
          })
        }

        if (this.uiInfo['ui.favicon'] && this.uiInfo['ui.favicon'].paramValue) {
          const faviconUrl = '/system/ui/image/' + this.uiInfo['ui.favicon'].paramValue
          changeFavicon(faviconUrl)
        }

        this.axiosFinished = true
      })
    },

    setTopMenuInfo(val) {
      this.loadUiInfo()
    },
    setTopMenuActiveInfo(val) {
      this.loadUiInfo()
    },
    setTopTextInfo(val) {
      this.loadUiInfo()
    },
    setTopTextActiveInfo(val) {
      this.loadUiInfo()
    },
    changeTemplateMarketShow(isShow) {
      this.templateMarketShow = isShow
    }

  }
}

</script>
<style lang="scss" scoped>
.el-dropdown-link {
  cursor: pointer;
  color: #1e212a;
}

.el-icon-arrow-down {
  font-size: 12px;
}

.top-dropdown {
  display: inline-block;
  padding: 10px 8px;
  height: 100%;
  font-size: 16px;
  color: #1e212a;
  vertical-align: text-bottom;
  margin-right: 10px;
}

.de-top-menu {
  background-color: var(--MainBG);

}

.template-market-item {
  display: flex;
  color: var(--MenuActiveBG, #409EFF);
  font-size: 14px !important;
  line-height: 38px !important;
}

.dialog-css ::v-deep .el-dialog__header {
  display: none;
}

</style>
