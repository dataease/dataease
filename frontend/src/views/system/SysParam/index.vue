<template>
  <de-layout-content
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <div class="sys-setting">
      <el-tabs v-model="activeName" class="de-tabs">
        <el-tab-pane
          :lazy="true"
          :label="$t('system_parameter_setting.basic_setting')"
          name="zero"
        >
        </el-tab-pane>

        <el-tab-pane
          :lazy="true"
          :label="$t('system_parameter_setting.mailbox_service_settings')"
          name="first"
        >
        </el-tab-pane>

        <el-tab-pane :lazy="true" :label="$t('sysParams.map')" name="ten">
        </el-tab-pane>

        <el-tab-pane
          v-if="isPluginLoaded"
          :lazy="true"
          :label="$t('sysParams.ldap')"
          name="fourth"
        >
        </el-tab-pane>

        <el-tab-pane
          v-if="isPluginLoaded"
          :lazy="true"
          :label="$t('sysParams.oidc')"
          name="five"
        >
        </el-tab-pane>

        <el-tab-pane
          v-if="engineMode === 'simple'"
          :lazy="true"
          :label="$t('system_parameter_setting.engine_mode_setting')"
          name="six"
        >
        </el-tab-pane>

        <el-tab-pane
          v-if="engineMode === 'cluster'"
          :lazy="true"
          :label="$t('system_parameter_setting.engine_mode_setting')"
          name="seven"
        >
        </el-tab-pane>

        <el-tab-pane
          v-if="engineMode === 'cluster'"
          :lazy="true"
          :label="$t('system_parameter_setting.kettle_setting')"
          name="eight"
        >
        </el-tab-pane>

        <el-tab-pane
          v-if="isPluginLoaded"
          :lazy="true"
          :label="$t('sysParams.cas')"
          name="nine"
        >
        </el-tab-pane>
      </el-tabs>
      <div
        class="tabs-container"
        :class="[activeName !== 'eight' ? 'is-center' : 'pad-center']"
      >
        <div class="min-w600">
          <basic-setting v-if="activeName === 'zero'" />
          <email-setting v-if="activeName === 'first'" />
          <map-setting v-if="activeName === 'ten'" ref="mapSetting" />
          <plugin-com
            v-if="activeName === 'fourth'"
            ref="DisplaySetting"
            component-name="LdapSetting"
          />
          <plugin-com
            v-if="activeName === 'five'"
            ref="DisplaySetting"
            component-name="SsoSetting"
          />
          <simple-mode v-if="activeName === 'six'" />
          <cluster-mode v-if="activeName === 'seven'" />
          <kettle-setting v-if="activeName === 'eight'" />
          <plugin-com
            v-if="activeName === 'nine'"
            ref="CasSetting"
            component-name="CasSetting"
          />
        </div>
      </div>
    </div>
  </de-layout-content>
</template>
<script>
import BasicSetting from "./BasicSetting";
import MapSetting from "./MapSetting";
import EmailSetting from "./EmailSetting";
import SimpleMode from "./SimpleModeSetting";
import ClusterMode from "./ClusterModeSetting";
import KettleSetting from "./KettleSetting";
import DeLayoutContent from "@/components/business/DeLayoutContent";
import PluginCom from "@/views/system/plugin/PluginCom";
import { pluginLoaded } from "@/api/user";
import { engineMode } from "@/api/system/engine";
export default {
  components: {
    BasicSetting,
    EmailSetting,
    DeLayoutContent,
    PluginCom,
    SimpleMode,
    ClusterMode,
    KettleSetting,
    MapSetting,
  },
  data() {
    return {
      activeName: "zero",
      isPluginLoaded: false,
      engineMode: "local",
    };
  },
  beforeCreate() {
    pluginLoaded().then((res) => {
      this.isPluginLoaded = res.success && res.data;
    });
    engineMode().then((res) => {
      this.engineMode = res.data;
    });
  },
  computed: {
    btnList() {},
  },
  methods: {
    handlerBtn(btn) {
    },
  },
};
</script>
<style lang="scss" scoped>
.sys-setting {
  height: 100%;
  background-color: var(--MainBG, #f5f6f7);

  .tabs-container {
    height: calc(100% - 48px);
    background: var(--ContentBG, #ffffff);
    overflow-x: auto;

    .min-w600 {
      min-width: 600px;
      height: 100%;
      & > :nth-child(1) {
        box-sizing: border-box;
        padding-bottom: 20px;
      }
    }
  }

  .is-center {
    display: flex;
    flex-direction: column;
    align-items: center;
    padding: 0 20px 20px 20px;
  }

  .pad-center {
    padding: 24px;
  }
}
</style>
