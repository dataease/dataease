<template>
<ms-container>
  <ms-aside-container>
    <dataset-group-selector @getTable="getTable"/>
  </ms-aside-container>

  <ms-main-container>
    <dataset-table-data :table="table"/>
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
import DatasetTableData from "../../dataset/common/DatasetTableData";

export default {
  name: "TableSelector",
  components: {
    DatasetTableData,
    MsMainContainer, MsContainer, MsAsideContainer, MsSettingMenu, MsCurrentUser, DatasetGroupSelector
  },
  data() {
    return {
      table: {},
    }
  },
  computed: {},
  created() {
  },
  mounted() {
  },
  methods: {
    getTable(table) {
      this.table = table;
      this.$emit("getTable", table);
    },
  }
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
