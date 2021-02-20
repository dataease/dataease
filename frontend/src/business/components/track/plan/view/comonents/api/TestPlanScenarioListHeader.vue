<template>
  <ms-table-header :is-tester-permission="true"
                   :condition="condition"
                   @search="$emit('refresh')"
                   :show-create="false"
                   :tip="$t('commons.search_by_id_name_tag')">
    <template v-slot:title>
       场景用例
    </template>
    <template v-slot:button>
      <ms-table-button :is-tester-permission="true" icon="el-icon-connection"
                       :content="$t('test_track.plan_view.relevance_test_case')"
                       @click="$emit('relevanceCase')"/>
    </template>

  </ms-table-header>
</template>

<script>
    import MsTableHeader from "../../../../../common/components/MsTableHeader";
    import MsTableButton from "../../../../../common/components/MsTableButton";
    import MsEnvironmentSelect from "../../../../../api/definition/components/case/MsEnvironmentSelect";
    export default {
      name: "TestPlanScenarioListHeader",
      components: {MsEnvironmentSelect, MsTableButton, MsTableHeader},
      props: ['condition', 'isReadOnly'],
      methods: {
      }
    }
</script>

<style scoped>

  /deep/ .environment-select {
    margin-right: 10px;
  }

</style>
