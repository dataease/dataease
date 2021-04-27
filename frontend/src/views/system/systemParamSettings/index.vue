<template>
  <el-card>
    <el-tabs v-model="activeName" class="system-setting">
      <el-tab-pane label="显示设置" name="ui">
        <ui-setting />
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.mailbox_service_settings')" name="email">
        <email-setting />
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import EmailSetting from './EmailSetting'
import UiSetting from './UiSetting'
export default {
  name: 'SystemParameterSetting',
  components: {
    UiSetting,
    EmailSetting
    // 'MsDisplay': display.default,
    // 'MsAuth': auth.default
  },
  data() {
    return {
      activeName: 'ui'
    }
  },
  methods: {
    // hasLicense
  }
}
</script>

<style scoped>

</style>
