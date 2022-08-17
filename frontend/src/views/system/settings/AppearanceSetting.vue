<template>
  <de-layout-content
    v-loading="$store.getters.loadingMap[$store.getters.currentPath]"
  >
    <div class="display-setting">
      <el-tabs v-model="activeName" class="de-tabs">
        <el-tab-pane :label="$t('sysParams.display')" name="display">
        </el-tab-pane>
        <el-tab-pane :label="$t('sysParams.theme')" name="theme"> </el-tab-pane>
      </el-tabs>
      <div
        class="tabs-container"
        :class="[activeName !== 'theme' ? 'is-center' : 'pad-center']"
      >
        <div class="min-w600">
          <plugin-com
            v-if="activeName === 'display'"
            ref="DisplaySetting"
            component-name="DisplaySetting"
          />
          <plugin-com
            v-if="activeName === 'theme'"
            ref="ThemeSetting"
            component-name="ThemeSetting"
          />
        </div>
      </div>
    </div>
  </de-layout-content>
</template>

<script>
import DeLayoutContent from "@/components/business/DeLayoutContent";
import PluginCom from "@/views/system/plugin/PluginCom";

export default {
  name: "AppearanceSetting",
  components: { DeLayoutContent, PluginCom },
  data() {
    return {
      activeName: "display",
    };
  },
};
</script>


<style lang="scss" scoped>
.display-setting {
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
