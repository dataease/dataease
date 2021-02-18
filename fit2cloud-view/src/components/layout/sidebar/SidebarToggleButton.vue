<template>
  <el-button circle :class="['sidebar-toggle-button', icon]" @click="toggle"></el-button>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  name: "SidebarToggleButton",
  methods: {
    toggle() {
      this.$store.dispatch('app/toggleSideBar');
    }
  },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    icon() {
      return this.sidebar.opened ? "el-icon-s-fold" : "el-icon-s-unfold"
    },
  }
}
</script>

<style lang="scss" scoped>
.sidebar-toggle-button.el-button {
  font-size: 18px;
}

</style>
