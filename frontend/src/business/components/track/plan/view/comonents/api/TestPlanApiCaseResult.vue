<template>
  <el-dialog :close-on-click-modal="false" :title="'测试结果'" width="30%"
             :visible.sync="visible" class="api-import" @close="close">
  <ms-request-result-tail :response="response" ref="debugResult"/>
  </el-dialog>
</template>

<script>
    import MsRequestResultTail from "../../../../../api/definition/components/response/RequestResultTail";
    export default {
      name: "TestPlanApiCaseResult",
      components: {MsRequestResultTail},
      props: ['response'],
      data() {
        return {
          visible: false
        }
      },
      methods: {
        close() {
          this.visible = false;
        },
        open() {
          this.visible = true;
        }
      }
    }
</script>

<style scoped>

</style>
