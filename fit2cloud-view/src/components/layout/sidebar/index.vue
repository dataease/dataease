<template>
  <div class="sidebar">
    <logo :collapse="isCollapse"/>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        :unique-opened="false"
        mode="vertical">
        <sidebar-item v-for="route in permission_routes" :key="route.path" :item="route" :base-path="route.path"/>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import SidebarItem from './SidebarItem'
import Logo from "@/components/layout/sidebar/Logo";

export default {
  name: "Sidebar",
  components: {Logo, SidebarItem},
  computed: {
    ...mapGetters([
      'permission_routes',
      'sidebar'
    ]),
    activeMenu() {
      const route = this.$route
      const {meta, path} = route
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    isCollapse() {
      return !this.sidebar.opened
    },
  }
}
</script>

<style lang="scss">
@import "~@/styles/common/variables";

@mixin sidebar-base-item {
  padding-left: 10px !important;
  border-radius: 2px;
  color: $menu-color;
}

@mixin menu-item {
  @include sidebar-base-item;
  margin: 2px 10px;
  line-height: $menu-height;
  height: $menu-height;
}

@mixin submenu-item {
  @include sidebar-base-item;
  margin: 2px 10px;
  line-height: $submenu-height;
  height: $submenu-height;
}

@mixin popper-submenu-item {
  @include sidebar-base-item;
  margin: 2px 0;
  line-height: $submenu-height;
  height: $submenu-height;
}

@mixin menu-item-active {
  color: $menu-active-color;
  background-color: $menu-active-bg-color;
}

@mixin submenu-item-active {
  color: $submenu-active-color;
  background-color: $submenu-active-bg-color;
}

.sidebar {
  height: 100%;

  .horizontal-collapse-transition {
    transition: 0s width ease-in-out, 0s padding-left ease-in-out, 0s padding-right ease-in-out;
  }

  .el-scrollbar {
    box-sizing: border-box;
    padding: 10px 0;
    height: calc(100% - #{$header-height});

    .el-scrollbar__bar {
      &.is-vertical {
        right: 0;
      }

      &.is-horizontal {
        display: none;
      }
    }

    .scrollbar-wrapper {
      height: 100%;
      overflow-x: hidden;
    }
  }

  a {
    width: 100%;
    overflow: hidden;
  }

  .el-menu {
    border: none;
    height: 100%;
    width: 100%;
    background-color: $menu-bg-color;

    .submenu-title-no-dropdown {
      @include menu-item;

      &:hover {
        background-color: $menu-bg-color-hover;
      }

      &.is-active {
        @include menu-item-active;
      }
    }

    .el-submenu {
      .el-submenu__title {
        @include menu-item;

        &:hover {
          background-color: $menu-bg-color-hover;
        }
      }

      &.is-active {
        .el-submenu__title, {
          @include menu-item-active;

          .sub-el-icon, span {
            color: #FFF;
          }
        }
      }

      .el-menu-item {
        @include submenu-item;

        &:hover {
          background-color: $menu-bg-color-hover;
        }

        &.is-active {
          @include submenu-item-active
        }
      }
    }

    .nest-menu, .el-submenu__title, .submenu-title-no-dropdown {
      span {
        padding-left: 30px;
      }

      .sub-el-icon {
        margin-right: 10px;

        + span {
          padding-left: 0;
        }
      }
    }

    &.el-menu--collapse {
      .el-tooltip {
        padding: 0 !important;
        text-align: center;
        line-height: $menu-height;
      }

      .el-submenu__title {
        padding-left: 20px !important;
      }

      .submenu-title-no-dropdown, .el-submenu__title {
        max-width: 60px;
        text-align: center;

        span {
          display: none;
        }

        .sub-el-icon {
          margin: 0;
        }

        .el-submenu__icon-arrow {
          display: none;
        }
      }
    }
  }
}

.sidebar-popper {
  & > .el-menu {
    display: block;
    background-color: $sidebar-bg-color;

    .sub-el-icon {
      margin-right: 12px;
      margin-left: -2px;
    }
  }

  .nest-menu .el-submenu > .el-submenu__title, .el-menu-item {
    &.is-active {
      @include submenu-item-active
    }

    @include popper-submenu-item;

    span {
      padding-left: 30px;
    }

    .sub-el-icon {
      margin-right: 10px;

      + span {
        padding-left: 0;
      }
    }

    &:hover {
      background-color: $menu-bg-color-hover;
    }
  }

  > .el-menu--popup {
    max-height: 100vh;
    overflow-y: auto;

    &::-webkit-scrollbar-track-piece {
      background: #d3dce6;
    }

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-thumb {
      background: #99a9bf;
      border-radius: 20px;
    }
  }
}
</style>
