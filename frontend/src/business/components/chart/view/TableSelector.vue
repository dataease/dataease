<template>
<ms-container>
  <ms-aside-container>
    <dataset-group-selector/>
  </ms-aside-container>

  <ms-main-container>
    <div>1111</div>
  </ms-main-container>
</ms-container>
</template>

<script>
import MsMainContainer from "../../common/components/MsMainContainer";
import MsContainer from "../../common/components/MsContainer";
import MsAsideContainer from "../../common/components/MsAsideContainer";
import MsSettingMenu from "../../settings/SettingMenu";
import MsCurrentUser from "../../settings/CurrentUser";
import DatasetGroupSelector from "../../dataset/common/DatasetGroupSelector";

export default {
  name: "TableSelector",
  components: {MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser, DatasetGroupSelector},
  data() {
    return {}
  },
  computed: {},
  created() {
  },
  mounted() {
  },
  methods: {}
}
</script>

<style scoped>
  .ms-aside-container {
    height: 50vh;
    min-width: 180px;
    max-width: 300px;
  }

  .ms-main-container {
    height: 50vh;
    border: 1px solid #E6E6E6;
    border-left: 0 solid;
  }
</style>
