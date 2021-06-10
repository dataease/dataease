<template>
  <el-card>
    <ui-setting v-if="!test" />
    <!-- <el-tabs v-model="activeName" class="system-setting">
      <el-tab-pane label="显示设置" name="ui">
        <ui-setting />
      </el-tab-pane>
      <el-tab-pane :label="$t('system_parameter_setting.mailbox_service_settings')" name="email">
        <email-setting />
      </el-tab-pane>
    </el-tabs> -->
    <async-component v-if="test" url="http://localhost:8081/PluginDemo.js" @execute-axios="executeAxios" />
  </el-card>
</template>

<script>
import UiSetting from './UiSetting'
import AsyncComponent from '@/components/AsyncComponent'
export default {
  name: 'SystemParameterSetting',
  components: {
    UiSetting,
    AsyncComponent

    // 'MsDisplay': display.default,
    // 'MsAuth': auth.default
  },
  data() {
    return {
      activeName: 'ui',
      test: false
    }
  },
  methods: {
    // hasLicense
    executeAxios(options) {
      // console.log(options)
    }
  }
}
</script>

<style scoped>

</style>
