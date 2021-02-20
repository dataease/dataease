<template>
  <ms-container>

    <ms-aside-container>
      <ms-api-monitor-search @getApiUrl="getApiUrl(arguments)" @getTodayData="getTodayData"
                             @initPage="initPage(arguments)"></ms-api-monitor-search>
    </ms-aside-container>

    <ms-main-container>

      <div class="menu-wrapper">

        <div class="menu-left">
          <el-radio-group id="date-radio" v-model=radioSelect size="small">
            <el-radio-button :label="$t('api_monitor.today')" @click.native.prevent="getTodayData()"/>
            <el-radio-button :label="$t('api_monitor.this_week')" @click.native.prevent="getWeekData()"/>
            <el-radio-button :label="$t('api_monitor.this_mouth')" @click.native.prevent="getMouthData()"/>
          </el-radio-group>
          <el-date-picker id="date-picker"
                          v-model="datePicker"
                          :end-placeholder="$t('api_monitor.end_time')"
                          :range-separator="$t('api_monitor.to')"
                          :start-placeholder="$t('api_monitor.start_time')"
                          class="sales-view-date-picker"
                          size="small"
                          type="daterange"
                          unlink-panels
                          value-format="yyyy-MM-dd HH:mm:ss" @blur="getDatePicker"
          />
          <el-tag id="apiInfo" type="info"><a id="api-url-title">{{ apiUrl }}</a></el-tag>
        </div>
      </div>
      <ms-api-monitor-chart :api-url="apiUrl" :rspCodeData=this.rspCodeData
                            :rspCodexAxis=this.rspCodexAxis :rspTimeData=this.rspTimeData
                            :rspTimexAxis=this.rspTimexAxis></ms-api-monitor-chart>
    </ms-main-container>
  </ms-container>
</template>

<script>

import ApiMonitorCharts from '@/business/components/api/monitor/ApiMonitorChart';
import MsApiMonitorChart from '@/business/components/api/monitor/ApiMonitorChart';
import MsApiMonitorSearch from '@/business/components/api/monitor/ApiMonitorSearch';
import MsMainContainer from '@/business/components/common/components/MsMainContainer';
import MsAsideContainer from '@/business/components/common/components/MsAsideContainer';
import MsContainer from '@/business/components/common/components/MsContainer';
import {formatTime} from '@/common/js/format-utils';

export default {
  name: 'MsApiMonitor',
  data() {
    return {
      datePicker: null,
      rspTimeData: [],
      rspTimexAxis: [],
      rspCodeData: [],
      rspCodexAxis: [],
      apiUrl: '',
      radioSelect: this.$t('api_monitor.today'),
    };
  },
  activated() {
    this.initData();
  },
  methods: {
    initData() {
      this.rspTimeData = [];
      this.rspTimexAxis = [];
      this.rspCodeData = [];
      this.rspCodexAxis = [];
    },
    initPage(url) {
      this.apiUrl = url[0];
      let date1 = new Date();
      let today1 = formatTime(new Date(date1.getFullYear(), date1.getMonth(), date1.getDate()));
      let today2 = formatTime(new Date(date1.getFullYear(), date1.getMonth(), date1.getDate() + 1));
      this.initData();
      this.getResponseTime(this.apiUrl, today1, today2);
      this.getResponseCode(this.apiUrl, today1, today2);
    },
    getResponseTime(apiUrl, startTime, endTime) {
      return this.$post('/api/monitor/getResponseTime',
        {'startTime': startTime, 'endTime': endTime, 'apiUrl': apiUrl}, response => {
          Object.values(response.data).forEach(value => {
            this.rspTimexAxis.push(value.startTime);
            this.rspTimeData.push(value.responseTime);
          });
        });
    },
    getResponseCode(apiUrl, startTime, endTime) {
      return this.$post('/api/monitor/getResponseCode',
        {'startTime': startTime, 'endTime': endTime, 'apiUrl': this.apiUrl}, response => {
          Object.values(response.data).forEach(value => {
            this.rspCodexAxis.push(value.startTime);
            this.rspCodeData.push(value.responseCode);
          });
        });
    },
    getDatePicker() {
      this.initData();
      this.getResponseTime(this.apiUrl, this.datePicker[0], this.datePicker[1]);
      this.getResponseCode(this.apiUrl, this.datePicker[0], this.datePicker[1]);
    },
    //获取周一日期
    getFirstDayOfWeek(date) {
      let day = date.getDay() || 7;
      return new Date(date.getFullYear(), date.getMonth(), date.getDate() + 1 - day);
    },
    getApiUrl(payload) {
      this.apiUrl = payload[0];
    },
    //获取今日数据
    getTodayData() {
      this.radioSelect = this.$t('api_monitor.today');
      let date1 = new Date();
      let today1 = formatTime(new Date(date1.getFullYear(), date1.getMonth(), date1.getDate()));
      let today2 = formatTime(new Date(date1.getFullYear(), date1.getMonth(), date1.getDate() + 1));
      this.initData();
      this.getResponseTime(this.apiUrl, today1, today2);
      this.getResponseCode(this.apiUrl, today1, today2);
    },
    //获取周数据
    getWeekData() {
      this.radioSelect = this.$t('api_monitor.this_week');
      const date1 = new Date();
      let today = formatTime(date1);
      let monday = formatTime(this.getFirstDayOfWeek(date1));
      this.initData();
      this.getResponseTime(this.apiUrl, monday, today);
      this.getResponseCode(this.apiUrl, monday, today);
    },
    //获取月数据
    getMouthData() {
      this.radioSelect = this.$t('api_monitor.this_mouth');
      const day = new Date(), y = day.getFullYear(), m = day.getMonth();
      let firstDay = formatTime(new Date(y, m, 1));
      let today = formatTime(day);
      this.initData();
      this.getResponseTime(this.apiUrl, firstDay, today);
      this.getResponseCode(this.apiUrl, firstDay, today);
    },
  },

  components: {
    MsApiMonitorChart,
    MsContainer,
    MsAsideContainer,
    MsMainContainer,
    MsApiMonitorSearch,
    ApiMonitorCharts,
  },

};

</script>

<style scoped>
.menu-left #apiInfo {
  margin-left: 20px;
}

#api-url-title {
  font-size: 15px;
}
</style>
