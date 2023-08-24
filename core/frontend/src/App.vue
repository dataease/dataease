<template>
  <div id="app">
    <router-view />
    <plugin-com
      v-show="false"
      ref="de-theme"
      component-name="ThemeSetting"
    />
    <el-dialog
      v-if="$route.path !== '/login'"
      :visible.sync="showPasswordModifiedDialog"
      append-to-body
      :title="$t('user.change_password')"
      :show-close="false"
    >
      <PasswordUpdateForm oldPwd="dataease" />
    </el-dialog>
  </div>
</template>

<script>
import PluginCom from '@/views/system/plugin/PluginCom'
import { mapState } from 'vuex'
import PasswordUpdateForm from '@/views/system/user/PasswordUpdateForm.vue'

export default {
  name: 'App',
  components: { PluginCom, PasswordUpdateForm },
  computed: {
    ...mapState('user', [
      'passwordModified',
    ])
  },
  data() {
    return {
      showPasswordModifiedDialog: false
    }
  },
  mounted() {
    const passwordModified = JSON.parse(localStorage.getItem('passwordModified'))
    if (typeof passwordModified === 'boolean') {
      this.$store.commit('user/SET_PASSWORD_MODIFIED', passwordModified)
    }
  },
  watch: {
    passwordModified: {
      handler(val) {
        this.showPasswordModifiedDialog = !val
      },
      immediate: true
    }
  }
}
</script>
