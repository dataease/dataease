<template>
    <el-card  v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="initData"
                         :title="$t('test_track.plan_view.report_template')"
                         :create-tip="$t('test_track.plan_view.create_template')" @create="templateEdit">
        </ms-table-header>
      </template>

      <testcase-template-item v-for="item in templates" :key="item.id"
                              :template="item" @templateEdit="templateEdit" @refresh="initData"/>

      <test-case-report-template-edit ref="templateEdit" @refresh="initData"/>

    </el-card>
</template>

<script>

    import MsTableHeader from "../../common/components/MsTableHeader";
    import TestCaseReportTemplateEdit from "../../track/plan/view/comonents/report/TestCaseReportTemplateEdit";
    import TestcaseTemplateItem from "../../track/plan/view/comonents/report/TestcaseTemplateItem";
    import {WORKSPACE_ID} from '../../../../common/js/constants';
    import MsMainContainer from "../../common/components/MsMainContainer";

    export default {
      name: "TestCaseReportTemplate",
      components: {MsMainContainer, TestcaseTemplateItem, TestCaseReportTemplateEdit, MsTableHeader},
      data() {
        return {
          result: {},
          condition: {},
          templates: []
        }
      },
      mounted() {
        this.initData();
      },
      watch: {
        '$route'(to) {
          if (to.path.indexOf("setting/testcase/report/template") >= 0) {
            this.initData();
          }
        }
      },
      methods: {
        initData() {
          this.condition.workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.result = this.$post('/case/report/template/list', this.condition, response => {
            this.templates = response.data;
          });
        },
        templateEdit(id) {
          this.$refs.templateEdit.open(id);
        }
      }
    }
</script>

<style scoped>

  .el-card {
    min-height: 300px;
  }

</style>
