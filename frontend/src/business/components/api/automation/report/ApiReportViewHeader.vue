<template>
  <header class="report-header">
    <el-row>
      <el-col>
        <span v-if="!debug"><el-input size="mini" style="width: 200px" v-model="report.name"/> </span>
        <span class="time"> {{ report.createTime | timestampFormatDate }}</span>

        <el-button v-if="!debug" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleExport(report.name)" style="margin-right: 10px">
          {{$t('test_track.plan_view.export_report')}}
        </el-button>

        <el-button v-if="!debug" :disabled="isReadOnly" class="export-button" plain type="primary" size="mini" @click="handleSave(report.name)" style="margin-right: 10px">
          {{$t('commons.save')}}
        </el-button>

      </el-col>
    </el-row>
  </header>
</template>

<script>
  import {checkoutTestManagerOrTestUser} from "@/common/js/utils";

  export default {
    name: "MsApiReportViewHeader",
    props: {
      report: {},
      debug: Boolean,
    },
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
      },
      handleSave(name) {
        this.$emit('reportSave', name);
      }
    }
  }
</script>

<style scoped>

  .export-button {
    float: right;
  }

</style>
