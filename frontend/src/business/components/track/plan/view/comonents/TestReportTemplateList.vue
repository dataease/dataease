<template>

  <el-dialog :title="$t('test_track.plan_view.select_template')"
             :visible.sync="templateVisible"
             class="report-template-list"
             width="50%">

    <el-main>
      <testcase-template-item v-for="item in templates" :key="item.id"
                              :template="item" @templateEdit="createTemplate" @refresh="initData"/>
    </el-main>

  </el-dialog>
</template>

<script>
    import TestcaseTemplateItem from "./report/TestcaseTemplateItem";
    import {WORKSPACE_ID} from "../../../../../../common/js/constants";
    export default {
      name: "TestReportTemplateList",
      components: {TestcaseTemplateItem},
      data() {
        return {
          templateVisible: false,
          templates: [],
          planId: ''
        }
      },
      methods: {
        initData() {
          let condition = {};
          condition.queryDefault = true;
          condition.workspaceId = localStorage.getItem(WORKSPACE_ID);
          this.result = this.$post('/case/report/template/list', condition, response => {
            this.templates = response.data;
          });
        },
        createTemplate(templateId) {
          let param = {};
          param.planId = this.planId;
          param.templateId = templateId;
          this.$post('/case/report/add', param, response => {
            this.$emit('openReport', param.planId, response.data);
            this.templateVisible = false;
          });
        },
        open(planId) {
          this.templateVisible = true;
          this.planId = planId;
          this.initData();
        }

      }
    }
</script>

<style scoped>

  .el-main >>> .testcase-template:hover i{
    display: none;
  }

  .report-template-list {
    text-align: left;
  }
</style>
