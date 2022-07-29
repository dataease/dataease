<template>
  <!-- <div :class="{'has-logo':showLogo}" :style="{'--active-bg': activeBg, '--theme':$store.state.settings.theme , '--left-menu-hovor': variables.leftMenuHovor}"> -->
  <div :class="{ 'has-logo': showLogo }" class="de-sidebar-container">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-menu
      :default-active="activeMenu"
      :collapse="isCollapse"
      :unique-opened="false"
      :collapse-transition="false"
      class="de-el-menu"
      mode="vertical"
    >
      <sidebar-item
        v-for="route in routes"
        :key="route.path"
        :item="route"
        :base-path="route.path"
      />
    </el-menu>
    <div
      :style="{ width: isCollapse ? '64px' : '260px' }"
      class="sidebar-collapse-btn"
      @click="changeSideWidth"
    >
      <i
        :style="{ transform: isCollapse ? 'rotate(90deg)' : 'rotate(-90deg)' }"
        class="el-icon-upload2"
      ></i>
      {{ isCollapse ? "" : "收起导航" }}
    </div>
  </div>
</template>

<script>
import { mapGetters, mapState } from "vuex";
import Logo from "./Logo";
import SidebarItem from "./SidebarItem";
import variables from "@/styles/variables.scss";
import path from "path";
import { isExternal } from "@/utils/validate";
export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters(["sidebar"]),
    ...mapState({
      isCollapse: state => state.isCollapse,
    }),
    routes() {
      // return this.$router.options.routes
      if (this.isCollapse) {
        return this.flatterRouter(
          JSON.parse(JSON.stringify(this.$store.state.permission.currentRoutes.children))
        );
      }
      return this.$store.state.permission.currentRoutes.children;
    },
    activeMenu() {
      const route = this.$route;
      const { meta, path } = route;
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu;
      }
      return path;
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo;
    },
    variables() {
      return variables;
    },
  },
  methods: {
    changeSideWidth() {
      this.$store.commit('setIsCollapse', !this.isCollapse);
      this.$emit("changeSideWidth", this.isCollapse ? "70px" : "");
    },
    resolvePath(routePath) {
      if (isExternal(routePath)) {
        return routePath;
      }
      if (isExternal(this.basePath)) {
        return this.basePath;
      }
      const currentRoutes = this.$store.state.permission.currentRoutes;
      if (currentRoutes && currentRoutes.path) {
        return path.resolve(currentRoutes.path, this.basePath, routePath);
      }
    },
    flatterRouter(arr = [], route = [], path = "") {
      arr.forEach((ele) => {
        this.formaterRoutePath(ele, path, route);
      });
      return route;
    },
    pathEndwith(path = "") {
      return path.endsWith("/") || !path ? path : path + "/";
    },
    formaterRoutePath(ele, routePath = "", route) {
      if (!ele.hidden) {
        if (!ele.children?.length) {
          ele.path = routePath + ele.path;
          route.push(ele);
        } else {
          this.flatterRouter(ele.children, route, this.pathEndwith(ele.path));
        }
      }
    },
  },
};
</script>

<style lang="scss" scoped>
.de-sidebar-container {
  position: relative;
  overflow: hidden;
}

.de-el-menu {
  overflow-y: auto;
  padding-bottom: 50px;
}
.sidebar-collapse-btn {
  height: 48px;
  position: absolute;
  bottom: 0;
  left: 0;
  border-top: 1px solid #1f232926;
  display: flex;
  align-items: center;
  justify-content: center;
  //styleName: 中文/桌面端/正文 14 22 Regular;
  font-family: PingFang SC;
  font-size: 14px;
  font-weight: 400;
  color: #646a73;
  background: #fff;
  cursor: pointer;

  i {
    margin-right: 8.8px;
  }
}
::v-deep .el-menu {
  .el-menu-item,
  .el-submenu__title {
    height: 48px;
    line-height: 48px;
  }
  .el-tooltip {
    padding-left: 25px !important;
  }
}
</style>
