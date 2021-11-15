<template>
  <layout-content v-loading="$store.getters.loadingMap[$store.getters.currentPath]">
    <el-tabs v-model="activeName" @tab-click="handleClick">

      <el-tab-pane :lazy="true" :label="$t('system_parameter_setting.basic_setting')" name="zero">
        <basic-setting />
      </el-tab-pane>

      <el-tab-pane :lazy="true" :label="$t('system_parameter_setting.mailbox_service_settings')" name="first">
        <email-setting />
      </el-tab-pane>

      <el-tab-pane v-if="isPluginLoaded" :lazy="true" :label="$t('sysParams.display')" name="second">
        <plugin-com v-if="isPluginLoaded" ref="DisplaySetting" component-name="DisplaySetting" />
      </el-tab-pane>

      <el-tab-pane v-if="isPluginLoaded" :lazy="true" :label="$t('sysParams.theme')" name="third">
        <plugin-com v-if="isPluginLoaded" ref="ThemeSetting" component-name="ThemeSetting" />
      </el-tab-pane>

      <el-tab-pane v-if="isPluginLoaded" :lazy="true" :label="$t('sysParams.ldap')" name="fourth">
        <plugin-com v-if="isPluginLoaded" ref="DisplaySetting" component-name="LdapSetting" />
      </el-tab-pane>

      <el-tab-pane v-if="isPluginLoaded" :lazy="true" :label="$t('sysParams.oidc')" name="five">
        <plugin-com v-if="isPluginLoaded" ref="DisplaySetting" component-name="SsoSetting" />
      </el-tab-pane>

    </el-tabs>
  </layout-content>
</template>
<script>
import BasicSetting from './BasicSetting'
import EmailSetting from './EmailSetting'
import LayoutContent from '@/components/business/LayoutContent'
import PluginCom from '@/views/system/plugin/PluginCom'
import { pluginLoaded } from '@/api/user'
export default {

  components: { BasicSetting, EmailSetting, LayoutContent, PluginCom },
  data() {
    return {
      activeName: 'zero',
      isPluginLoaded: false
    }
  },
  beforeCreate() {
    pluginLoaded().then(res => {
      this.isPluginLoaded = res.success && res.data
    })
  },
  methods: {
    handleClick(tab, event) {
      console.log(tab, event)
    }
  }
}
</script>
