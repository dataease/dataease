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
      <PasswordUpdateForm />
    </el-dialog>
    <ExportExcel ref="ExportExcelRef" />
  </div>
</template>

<script>
import ExportExcel from '@/views/dataset/exportExcel/index.vue'
import PluginCom from '@/views/system/plugin/PluginCom'
import { mapState } from 'vuex'
import PasswordUpdateForm from '@/views/system/user/PasswordUpdateForm.vue'
import bus from '@/utils/bus'

export default {
  name: 'App',
  components: { PluginCom, PasswordUpdateForm, ExportExcel },
  data() {
    return {
      showPasswordModifiedDialog: false,
      defaultPwd: 'dataease'
    }
  },
  computed: {
    ...mapState('user', [
      'passwordModified'
    ])
  },
  watch: {
    passwordModified: {
      handler(val) {
        this.showPasswordModifiedDialog = !val
      },
      immediate: true
    }
  },
  mounted() {
    bus.$on('data-export-center', this.downloadClick)
    const passwordModified = JSON.parse(localStorage.getItem('passwordModified'))
    this.defaultPwd = localStorage.getItem('defaultPwd')
    if (typeof passwordModified === 'boolean') {
      this.$store.commit('user/SET_PASSWORD_MODIFIED', passwordModified)
    }
  },
  beforeDestroy() {
    bus.$off('data-export-center', this.downloadClick)
  },
  methods: {
    downloadClick() {
      this.$refs.ExportExcelRef.init()
    }
  }
}
</script>
