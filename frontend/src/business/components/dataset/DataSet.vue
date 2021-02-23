<template>
  <ms-container>

    <ms-aside-container>
      <group/>
    </ms-aside-container>

    <ms-main-container>
      <keep-alive>
        <router-view/>
      </keep-alive>
    </ms-main-container>
  </ms-container>
</template>

<script>
  import MsMainContainer from "../common/components/MsMainContainer";
  import MsContainer from "../common/components/MsContainer";
  import MsAsideContainer from "../common/components/MsAsideContainer";
  import MsSettingMenu from "../settings/SettingMenu";
  import MsCurrentUser from "../settings/CurrentUser";
  import Group from "./group/Group";

  export default {
    name: "DataSet",
    components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser, Group},
    data() {
      return {}
    }
  }
</script>

<style scoped>
  .ms-aside-container {
    height: calc(100vh - 40px);
    padding: 15px;
    min-width: 300px;
    max-width: 500px;
  }

  .ms-main-container {
    height: calc(100vh - 40px);
  }

</style>
