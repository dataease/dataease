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

      <el-tab-pane v-if="engineMode==='simple'" :lazy="true" :label="$t('system_parameter_setting.engine_mode_setting')" name="six">
        <simple-mode />
      </el-tab-pane>

      <el-tab-pane v-if="engineMode==='cluster'" :lazy="true" :label="$t('system_parameter_setting.engine_mode_setting')" name="seven">
        <cluster-mode />
      </el-tab-pane>

      <el-tab-pane  v-if="engineMode==='cluster'" :lazy="true" :label="$t('system_parameter_setting.kettle_setting')" name="eight">
        <kettle-setting />
      </el-tab-pane>

    </el-tabs>
  </layout-content>
</template>
<script>
import BasicSetting from './BasicSetting'
import EmailSetting from './EmailSetting'
import SimpleMode from './SimpleModeSetting'
import ClusterMode from './ClusterModeSetting'
import KettleSetting from './KettleSetting'
import LayoutContent from '@/components/business/LayoutContent'
import PluginCom from '@/views/system/plugin/PluginCom'
import { pluginLoaded } from '@/api/user'
import { engineMode } from '@/api/system/engine'
export default {

  components: { BasicSetting, EmailSetting, LayoutContent, PluginCom, SimpleMode, ClusterMode, KettleSetting},
  data() {
    return {
      activeName: 'zero',
      isPluginLoaded: false,
      engineMode: 'local'
    }
  },
  beforeCreate() {
    pluginLoaded().then(res => {
      this.isPluginLoaded = res.success && res.data
    })
    engineMode().then(res => {
      this.engineMode = res.data
    })
  },
  methods: {
    handleClick(tab, event) {
      // console.log(tab, event)
    }
  }
}
</script>
