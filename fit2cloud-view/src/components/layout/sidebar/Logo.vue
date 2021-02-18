<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}">
    <transition name="sidebar-logo-fade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="collapseLogo" :src="collapseLogo" class="sidebar-logo" alt="Sidebar Logo">
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo" alt="Sidebar Logo">
      </router-link>
    </transition>
  </div>
</template>

<script>
export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      title: 'FIT2CLOUD',
      logo: require('@/assets/RackShift-white.png'),
      collapseLogo: require('@/assets/RackShift-assist-white.png')
    }
  }
}
</script>

<style lang="scss">
@import "~@/styles/common/variables";

.sidebar-logo-container {
  position: relative;
  height: $header-height;
  line-height: $header-height;
  overflow: hidden;

  &:after {
    content: "";
    position: absolute;
    bottom: 0;
    right: #{$sidebar-close-width / 4};
    height: 1px;
    width: calc(100% - #{$sidebar-close-width / 2});
    background-color: hsla(0, 0%, 100%, .5);
  }

  & .sidebar-logo-link {
    display: flex;
    align-items: center;
    height: 100%;
    width: 100%;

    & .sidebar-logo {
      margin-left: #{$sidebar-close-width / 4};
      height: $logo-height;
      vertical-align: middle;
    }
  }

  &.collapse {
    .sidebar-logo-link {
      justify-content: center;
    }

    .sidebar-logo {
      margin: 0;
    }
  }
}

.sidebar-logo-fade-enter-active {
  transition: opacity 0.3s;
  transition-delay: 0.1s
}

.sidebar-logo-fade-enter,
.sidebar-logo-fade-leave-to {
  opacity: 0;
}
</style>
