<template>
  <ms-container>

    <ms-aside-container>
      <ms-current-user/>
      <el-divider/>
      <h1>{{$t('commons.setting')}}</h1>
      <ms-setting-menu/>
    </ms-aside-container>

    <ms-main-container>
      <keep-alive>
        <router-view/>
      </keep-alive>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsCurrentUser from "./CurrentUser";
  import MsSettingMenu from "./SettingMenu";
  import MsAsideContainer from "../common/components/MsAsideContainer";
  import MsContainer from "../common/components/MsContainer";
  import MsMainContainer from "../common/components/MsMainContainer";

  export default {
    name: "MsSetting",
    components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser},
  }
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 40px);
    padding: 20px;
  }

  .ms-main-container {
    height: calc(100vh - 40px);
  }

  h1 {
    font-size: 20px;
    font-weight: 500;
  }

</style>

