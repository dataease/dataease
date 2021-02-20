<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span>{{ report.projectName }} / </span>
        <router-link :to="path">{{ report.testName }}</router-link>
        <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
        <el-button :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleExport(report.name)"
                   style="margin-left: 1200px">
          {{$t('test_track.plan_view.export_report')}}
        </el-button>
      </el-col>
    </el-row>
  </header>
</template>

<script>
    import {checkoutTestManagerOrTestUser} from "../../../../common/js/utils";

    export default {
      name: "MsApiReportViewHeader",
      props: ['report'],
      computed: {
        path() {
          return "/api/test/edit?id=" + this.report.testId;
        },
      },
      data() {
        return {
          isReadOnly: false,
        }
      },
      created() {
        if (!checkoutTestManagerOrTestUser()) {
          this.isReadOnly = true;
        }
      },
      methods: {
        handleExport(name) {
          this.$emit('reportExport', name);
        }
      }
    }
</script>

<style scoped>

  .export-button {
    float: right;
  }

</style>
