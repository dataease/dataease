<template>
  <ms-table-header :is-tester-permission="true"
                   :condition="condition"
                   @search="$emit('refresh')"
                   :show-create="false"
                   :tip="$t('commons.search_by_id_name_tag')">
    <template v-slot:title>
       接口用例
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
      name: "TestPlanCaseListHeader",
      components: {MsEnvironmentSelect, MsTableButton, MsTableHeader},
      props: ['condition', 'projectId', 'isReadOnly', 'planId'],
      methods: {
        setEnvironment(data) {
          if (this.planId) {
            let param = {};
            param.id = this.planId;
            param.environmentId = data.id;
            this.$post('/test/plan/edit', param, () => {
              this.$emit('setEnvironment', data);
            });
          }
        }
      }
    }
</script>

<style scoped>

  /deep/ .environment-select {
    margin-right: 10px;
  }

</style>
