<template>
  <el-dropdown
    style="display: flex;align-items: center;"
    trigger="click"
    class="international"
    @command="handleSetLanguage"
  >
    <div>
      <svg-icon
        style="color: var(--TextPrimary)"
        class-name="international-icon"
        icon-class="language"
      />
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item
        :disabled="language==='zh_CN'"
        command="zh_CN"
      >
        简体中文
      </el-dropdown-item>
      <el-dropdown-item
        :disabled="language==='zh_TW'"
        command="zh_TW"
      >
        繁体中文
      </el-dropdown-item>
      <el-dropdown-item
        :disabled="language==='en_US'"
        command="en_US"
      >
        English
      </el-dropdown-item>
      <!-- <el-dropdown-item :disabled="language==='es'" command="es">
        Español
      </el-dropdown-item>
      <el-dropdown-item :disabled="language==='ja'" command="ja">
        日本語
      </el-dropdown-item> -->
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
/* import variables from '@/styles/variables.scss' */
export default {
  computed: {
    language() {
      return this.$store.getters.language
    }
    /* variables() {
      return variables
    }, */
    /* topMenuTextColor() {
      if (this.$store.getters.uiInfo && this.$store.getters.uiInfo['ui.topMenuTextColor'] && this.$store.getters.uiInfo['ui.topMenuTextColor'].paramValue) { return this.$store.getters.uiInfo['ui.topMenuTextColor'].paramValue }
      return this.variables.topBarMenuText
    } */
  },
  methods: {
    handleSetLanguage(lang) {
      this.$i18n.locale = lang
      this.$store.dispatch('user/setLanguage', lang)
      this.$message({
        message: this.$t('commons.switch_lang'),
        type: 'success'
      })
    }
  }
}
</script>
