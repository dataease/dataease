<template>
  <el-menu class="header-menu" text-color="inherit" mode="horizontal">
    <el-submenu index="none" popper-class="header-menu-popper">
      <template slot="title">
        <span>{{ name }}</span>
      </template>
      <el-menu-item @click="toPersonal">
        <span>{{ $t('commons.personal.personal_information') }}</span>
      </el-menu-item>
      <el-menu-item @click="toHelp">
        <span>{{ $t('commons.personal.help_documentation') }}</span>
      </el-menu-item>
      <el-menu-item @click="logout">
        <span>{{ $t('commons.personal.exit_system') }}</span>
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
import {mapGetters} from "vuex";

export default {
  name: "PersonalSetting",
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  methods: {
    toPersonal() {

    },
    toHelp() {
      window.open("https://github.com/fit2cloud-ui/samples", "_blank");
    },
    logout() {
      this.$store.dispatch("user/logout").then(() => {
        location.reload()
      })
    }
  }
}
</script>

<style lang="scss">
@import "~@/styles/business/header-menu.scss";
</style>
