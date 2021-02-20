<template>

  <common-component :title="$t('test_track.plan_view.result_statistics')">

    <div class="char-component">
      <div class="char-item" v-if="showFunctional">
        <ms-pie-chart v-if="isShow" :text="'功能测试用例'" @onClick="onFuncCharClick"
                      :name="$t('test_track.plan_view.test_result')" :data="functionalCharData"/>
      </div>

      <div class="char-item" v-if="showApi">
        <ms-pie-chart v-if="isShow" :text="'接口测试用例'"  @onClick="onApiCharClick"
                      :name="$t('test_track.plan_view.test_result')" :data="apiCharData"/>
      </div>

      <div class="char-item"  v-if="showScenario">
        <ms-pie-chart v-if="isShow" :text="'场景测试用例'"  @onClick="onScenarioCharClick"
                      :name="$t('test_track.plan_view.test_result')" :data="scenarioCharData"/>
      </div>

      <div class="char-item"  v-if="showLoad">
        <ms-pie-chart v-if="isShow" :text="'性能测试用例'"  @onClick="onLoadCharClick"
                      :name="$t('test_track.plan_view.test_result')" :data="loadCharData"/>
      </div>
    </div>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import MsPieChart from "../../../../../../common/components/MsPieChart";

    export default {
      name: "TestResultAdvanceChartComponent",
      components: {MsPieChart, CommonComponent},
      data() {
        return {
          dataMap: new Map([
            ["Pass", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
            ["Failure", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
            ["Blocking", {name: this.$t('test_track.plan_view.blocking'), itemStyle: {color: '#E6A23C'}}],
            ["Skip", {name: this.$t('test_track.plan_view.skip'), itemStyle: {color: '#909399'}}],
            ["Underway", {name: this.$t('test_track.plan.plan_status_running'), itemStyle: {color: 'lightskyblue'}}],
            ["Prepare", {name: this.$t('test_track.plan.plan_status_prepare'), itemStyle: {color: '#DEDE10'}}]
          ]),
          functionalCharData: [],
          apiCharData: [],
          scenarioCharData: [],
          loadCharData: [],
          isShow: true
        }
      },
      props: {
        planId:String,
        source:String,
        executeResult: {
          type: Object,
          default() {
            return {
              functionalResult: [
                {status: 'Pass', count: '0'},
                {status: 'Failure', count: '0'},
                {status: 'Blocking', count: '0'},
                {status: 'Skip', count: '0'},
                {status: 'Underway', count: '0'},
                {status: 'Prepare', count: '0'},
              ],
              apiResult: [
                {status: 'Pass', count: '0'},
                {status: 'Failure', count: '0'},
                {status: 'Underway', count: '0'},
              ],
              scenarioResult: [
                {status: 'Pass', count: '0'},
                {status: 'Failure', count: '0'},
                {status: 'Underway', count: '0'},
              ],
              loadResult: [
                {status: 'Pass', count: '0'},
                {status: 'Failure', count: '0'},
                {status: 'Underway', count: '0'},
              ],
            }
          }
        },
      },
     computed: {
       showFunctional() {
         return this.executeResult.functionalResult.length > 0
           || (this.executeResult.apiResult.length <= 0 && this.executeResult.scenarioResult.length <= 0 && this.executeResult.loadResult.length <= 0);
       },
       showApi() {
         return this.executeResult.apiResult.length > 0;
       },
       showScenario() {
         return this.executeResult.scenarioResult.length > 0;
       },
       showLoad() {
         return this.executeResult.loadResult.length > 0;
       }
     },
      watch: {
        executeResult() {
          this.getCharData();
        }
      },
      created() {
        this.getCharData();
      },
      methods: {
        getCharData() {
          this.getFunctionalCharData();
          this.getApiCharData();
          this.getScenarioCharData();
          this.getLoadCharData();
          this.reload();
        },
        getFunctionalCharData() {
          this.functionalCharData = [];
          if (this.executeResult.functionalResult) {
            this.executeResult.functionalResult.forEach(item => {
              let data = this.copyData(item.status);
              data.value = item.count;
              this.functionalCharData.push(data);
            });
          }
        },
        getApiCharData() {
          this.apiCharData = [];
          if (this.executeResult.apiResult) {
            this.executeResult.apiResult.forEach(item => {
              let data = this.copyData(item.status);
              data.value = item.count;
              this.apiCharData.push(data);
            });
          }
        },
        getScenarioCharData() {
          this.scenarioCharData = [];
          if (this.executeResult.apiResult) {
            this.executeResult.scenarioResult.forEach(item => {
              let data = this.copyData(item.status);
              data.value = item.count;
              this.scenarioCharData.push(data);
            });
          }
        },
        getLoadCharData() {
          this.loadCharData = [];
          if (this.executeResult.loadResult) {
            this.executeResult.loadResult.forEach(item => {
              let data = this.copyData(item.status);
              data.value = item.count;
              this.loadCharData.push(data);
            });
          }
        },
        copyData(status) {
          return JSON.parse(JSON.stringify(this.dataMap.get(status)))
        },
        reload() {
          this.isShow = false;
          this.$nextTick(function () {
            this.isShow = true;
          })
        },
        onvertDataStatus(status){
          if(status == this.$t('test_track.plan_view.pass')){
            status = "Pass";
          }else if(status == this.$t('test_track.plan_view.failure')){
            status = "Failure";
          }else if(status == this.$t('test_track.plan_view.blocking')){
            status = "Blocking";
          }else if(status == this.$t('test_track.plan.plan_status_prepare')){
            status = "Prepare";
          }else if (status == this.$t('test_track.plan.plan_status_running')){
            status = "running";
          }
          return status;
        },
        onFuncCharClick(params){
          let clickType = params['name'];
          clickType = this.onvertDataStatus(clickType);
          this.redirectPage('functional',clickType);
        },
        onApiCharClick(params){
          let clickType = params['name'];
          clickType = this.onvertDataStatus(clickType);
          if(clickType=="Failure"){
            clickType = "error";
          }else if(clickType=="Pass"){
            clickType = "success";
          }
          this.redirectPage('api',clickType);
        },
        onScenarioCharClick(params){
          let clickType = params['name'];
          clickType = this.onvertDataStatus(clickType);
          if(clickType=="Failure"){
            clickType = "Fail";
          }else if(clickType=="Pass"){
            clickType = "Success";
          }
          this.redirectPage('scenario',clickType);
        },
        onLoadCharClick(params){
          let clickType = params['name'];
          clickType = this.onvertDataStatus(clickType);
          if(clickType=="Failure"){
            clickType = "error";
          }
          this.redirectPage('load',clickType);
        },
        redirectPage(charType,clickType){
          if(this.source == "ReportView"){
            this.$router.push({name:'planView',params:{planId:this.planId,charType:charType,clickType:clickType}});
          }

        }
      }
    }
</script>

<style scoped>

  .echarts {
    margin: 0 auto;
  }

  .char-item {
    display: inline-block;
  }

  .char-component {
    text-align: center;
  }

</style>
