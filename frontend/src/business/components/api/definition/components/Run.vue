<template>
  <span></span>
</template>
<script>
  import {getUUID, getBodyUploadFiles} from "@/common/js/utils";
  import ThreadGroup from "./jmeter/components/thread-group";
  import TestPlan from "./jmeter/components/test-plan";

  export default {
    name: 'MsRun',
    components: {},
    props: {
      environment: Object,
      debug: Boolean,
      reportId: String,
      runData: Array,
      type: String
    },
    data() {
      return {
        result: {},
        loading: false,
        runId: "",
        reqNumber: 0,
      }
    },

    watch: {
      // 初始化
      reportId() {
        this.run()
      }
    },
    methods: {
      getResult() {
        if (this.runId) {
          let url = "";
          if (this.debug) {
            url = "/api/definition/report/get/" + this.runId + "/" + "debug";
          } else {
            url = "/api/definition/report/get/" + this.runId + "/" + "run";
          }
          this.$get(url, response => {
            if (response.data) {
              let data = JSON.parse(response.data.content);
              this.$emit('runRefresh', data);
            } else {
              if (this.reqNumber < 60) {
                this.reqNumber++;
                setTimeout(this.getResult, 2000);
              } else {
                this.$error("获取报告超时");
                this.$emit('runRefresh', {});
              }
            }
          });
        }
      },
      run() {
        let testPlan = new TestPlan();
        let threadGroup = new ThreadGroup();
        threadGroup.hashTree = [];
        testPlan.hashTree = [threadGroup];
        this.runData.forEach(item => {
          threadGroup.hashTree.push(item);
        })
        let reqObj = {id: this.reportId, testElement: testPlan, type: this.type};
        let bodyFiles = getBodyUploadFiles(reqObj, this.runData);
        let url = "";
        if (this.debug) {
          reqObj.reportId = this.reportId;
          url = "/api/definition/run/debug";
        } else {
          url = "/api/definition/run";
        }
        this.$fileUpload(url, null, bodyFiles, reqObj, response => {
          this.runId = response.data;
          this.getResult();
        }, erro => {
          this.$emit('runRefresh', {});
        });
      }
    }
  }
</script>
