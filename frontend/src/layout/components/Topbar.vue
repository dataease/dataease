<template>
  <div class="top-nav" :style="{'background-color': '#f1f3f8'}">
    <div v-loading="!axiosFinished" class="log">
      <!--      <img      v-if="!logoUrl" src="@/assets/DataEase-color.png" width="140" alt="" style="padding-top: 10px;">-->
      <svg-icon v-if="!logoUrl && axiosFinished" icon-class="DataEase" custom-class="top-nav-logo-icon" />
      <img v-if="logoUrl && axiosFinished" :src="logoUrl" width="140" alt="" style="padding-top: 10px;">
    </div>
    <el-menu
      :active-text-color="variables.topMenuActiveText"
      :default-active="activeMenu"
      mode="horizontal"
      :style="{'background-color': '#f1f3f8', 'margin-left': '260px', 'position': 'absolute'}"
      @select="handleSelect"
    >
      <div v-for="item in permission_routes" :key="item.path" class="nav-item">
        <app-link :to="resolvePath(item)">
          <el-menu-item
            v-if="!item.hidden"
            :index="item.path"
          >{{ item.meta ? item.meta.title : item.children[0].meta.title }}</el-menu-item>
        </app-link>
      </div>
    </el-menu>

    <div class="right-menu">
      <template>
        <!--        <el-tooltip content="项目文档" effect="dark" placement="bottom">-->
        <!--          <doc class="right-menu-item hover-effect" />-->
        <!--        </el-tooltip>-->

        <!--        <el-tooltip content="全屏缩放" effect="dark" placement="bottom">-->
        <!--          <screenfull id="screenfull" class="right-menu-item hover-effect" />-->
        <!--        </el-tooltip>-->

        <!-- <el-tooltip :content="$t('navbar.size')" effect="dark" placement="bottom">
          <size-select id="size-select" class="right-menu-item hover-effect" />
        </el-tooltip> -->
        <notification class="right-menu-item hover-effect" />
        <lang-select class="right-menu-item hover-effect" />
        <div style="height: 100%;padding: 0 8px;" class="right-menu-item hover-effect">
          <a href="https://dataease.io/docs/" target="_blank" style="display: flex;height: 100%;width: 100%;justify-content: center;align-items: center;">
            <svg-icon icon-class="docs" />
          </a>
        </div>
      </template>

      <el-dropdown class="top-dropdown" style="display: flex;align-items: center; width:100px;">
        <span class="el-dropdown-link" style="font-size: 14px;max-width: 80px;overflow: hidden;text-overflow: ellipsis;white-space: nowrap;">
          {{ name }}
          <i class="el-icon-arrow-down el-icon--right" />
        </span>
        <el-dropdown-menu slot="dropdown">
          <router-link to="/person-info/index">
            <el-dropdown-item>{{ $t('commons.personal_info') }}</el-dropdown-item>
          </router-link>
          <router-link to="/person-pwd/index">
            <el-dropdown-item>{{ $t('user.change_password') }}</el-dropdown-item>
          </router-link>
          <!--          <a href="https://de-docs.fit2cloud.com/" target="_blank">-->
          <!--            <el-dropdown-item>{{ $t('commons.help_documentation') }} </el-dropdown-item>-->
          <!--          </a>-->
          <router-link to="/about/index">
            <el-dropdown-item>{{ $t('commons.about_us') }}</el-dropdown-item>
          </router-link>
          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">{{ $t('commons.exit_system') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import AppLink from './Sidebar/Link'
import variables from '@/styles/variables.scss'
import { isExternal } from '@/utils/validate'
import Notification from '@/components/Notification'
// import Screenfull from '@/components/Screenfull'
// import SizeSelect from '@/components/SizeSelect'
import LangSelect from '@/components/LangSelect'
import { getSysUI } from '@/utils/auth'
export default {
  name: 'Topbar',
  components: {
    AppLink,
    // Screenfull,
    // SizeSelect,
    Notification,
    LangSelect
    // Doc
  },
  data() {
    return {
      uiInfo: null,
      logoUrl: null,
      axiosFinished: false
    }
  },

  computed: {
    theme() {
      return this.$store.state.settings.theme
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
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
    ...mapGetters([
      'avatar',
      'permission_routes',
      'name'
    ])
  },

  mounted() {
    this.initCurrentRoutes()
  },
  created() {
    this.$store.dispatch('user/getUI').then(() => {
      this.uiInfo = getSysUI()
      if (this.uiInfo['ui.logo'] && this.uiInfo['ui.logo'].paramValue) {
        this.logoUrl = '/system/ui/image/' + this.uiInfo['ui.logo'].paramValue
      }
      this.axiosFinished = true
    })
  },
  methods: {
    // 通过当前路径找到二级菜单对应项，存到store，用来渲染左侧菜单
    initCurrentRoutes() {
      const { path } = this.$route
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
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login?redirect=${this.$route.fullPath}`)
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
</style>
