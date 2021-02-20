<template>

  <common-component :title="$t('test_track.plan_view.result_statistics')">

    <template>

      <ms-pie-chart v-if="isShow" :text="$t('test_track.plan_view.result_statistics_chart')"
                    :name="$t('test_track.plan_view.test_result')" :data="charData"/>

    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import MsPieChart from "../../../../../../common/components/MsPieChart";

    export default {
      name: "TestResultChartComponent",
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
          charData: [],
          isShow: true
        }
      },
      props: {
        executeResult: {
          type: Array,
          default() {
            return [
              {status: 'Pass', count: '235'},
              {status: 'Failure', count: '310'},
              {status: 'Blocking', count: '274'},
              {status: 'Skip', count: '335'},
              {status: 'Underway', count: '245'},
              {status: 'Prepare', count: '265'},
            ]
          }
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
          this.charData = [];
          this.executeResult.forEach(item => {
            let data = this.dataMap.get(item.status);
            data.value = item.count;
            this.charData.push(data);
          });
          this.reload();


        },
        reload() {
          this.isShow = false;
          this.$nextTick(function () {
            this.isShow = true;
          })
        },
        onClick(params){
          this.$emit('onClick', params)
        },
      }
    }
</script>

<style scoped>

  .echarts {
    margin: 0 auto;
  }

</style>
