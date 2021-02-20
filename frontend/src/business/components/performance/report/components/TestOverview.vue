<template>
  <div>
    <el-row :gutter="12">
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-1">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ maxUsers }}</span>
            <span class="ms-card-data-unit"> VU</span>
          </span>
          <span class="ms-card-desc">Max Users</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-2">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ avgTransactions }}</span>
            <span class="ms-card-data-unit"> Transactions/s</span>
          </span>
          <span class="ms-card-desc">Avg.Transactions</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-3">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ errors }}</span>
            <span class="ms-card-data-unit"> %</span>
          </span>
          <span class="ms-card-desc">Errors</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-4">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ avgResponseTime }}</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">Avg.Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-5">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ responseTime90 }}</span>
            <span class="ms-card-data-unit"> s</span>
          </span>
          <span class="ms-card-desc">90% Response Time</span>
        </el-card>
      </el-col>
      <el-col :span="4">
        <el-card shadow="always" class="ms-card-index-6">
          <span class="ms-card-data">
            <span class="ms-card-data-digital">{{ avgBandwidth }}</span>
            <span class="ms-card-data-unit"> KiB/s</span>
          </span>
          <span class="ms-card-desc">Avg.Bandwidth</span>
        </el-card>
      </el-col>
    </el-row>

    <el-row>
      <el-col :span="12">
        <ms-chart ref="chart1" :options="loadOption" class="chart-config" :autoresize="true"></ms-chart>
      </el-col>
      <el-col :span="12">
        <ms-chart ref="chart2" :options="resOption" class="chart-config" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
    <el-row>
      <el-col :span="12">
        <ms-chart ref="chart3" :options="errorOption" class="chart-config" :autoresize="true"></ms-chart>
      </el-col>
      <el-col :span="12">
        <ms-chart ref="chart3" :options="resCodeOption" class="chart-config" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "TestOverview",
  components: {MsChart},
  data() {
    return {
      maxUsers: "0",
      avgThroughput: "0",
      avgTransactions: "0",
      errors: "0",
      avgResponseTime: "0",
      responseTime90: "0",
      avgBandwidth: "0",
      loadOption: {},
      resOption: {},
      errorOption: {},
      resCodeOption: {},
      id: ''
    }
  },
  methods: {
    initTableData() {
      this.$get("/performance/report/content/testoverview/" + this.id).then(res => {
        let data = res.data.data;
        this.maxUsers = data.maxUsers;
        this.avgThroughput = data.avgThroughput;
        this.avgTransactions = data.avgTransactions;
        this.errors = data.errors;
        this.avgResponseTime = data.avgResponseTime;
        this.responseTime90 = data.responseTime90;
        this.avgBandwidth = data.avgBandwidth;
      }).catch(() => {
        this.maxUsers = '0';
        this.avgThroughput = '0';
        this.avgTransactions = '0';
        this.errors = '0';
        this.avgResponseTime = '0';
        this.responseTime90 = '0';
        this.avgBandwidth = '0';
        // this.$warning(this.$t('report.generation_error'));
      })
      this.getLoadChart();
      this.getResChart();
      this.getErrorChart();
      this.getResponseCodeChart();
    },
    getLoadChart() {
      this.$get("/performance/report/content/load_chart/" + this.id).then(res => {
        let data = res.data.data;
        let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
        let yAxis2List = data.filter(m => m.yAxis === -1).map(m => m.yAxis2);
        let yAxisListMax = this._getChartMax(yAxisList);
        let yAxis2ListMax = this._getChartMax(yAxis2List);

        let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
        yAxisIndex0List = this._unique(yAxisIndex0List);
        let yAxisIndex1List = data.filter(m => m.yAxis === -1).map(m => m.groupName);
        yAxisIndex1List = this._unique(yAxisIndex1List);

        let loadOption = {
          title: {
            text: 'Load',
            left: 'center',
            top: 20,
            textStyle: {
              color: '#65A2FF'
            },
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            // extraCssText: 'z-index: 999;',
            confine: true,
          },
          legend: {},
          xAxis: {},
          yAxis: [{
            name: 'User',
            type: 'value',
            min: 0,
            max: yAxisListMax,
            splitNumber: 5,
            interval: yAxisListMax / 5
          },
            {
              name: 'Transactions/s',
              type: 'value',
              splitNumber: 5,
              min: 0,
              max: yAxis2ListMax,
              interval: yAxis2ListMax / 5
            }
          ],
          series: []
        };
        let setting = {
          series: [
            {
              name: 'users',
              color: '#0CA74A',
            },
            {
              name: 'hits',
              yAxisIndex: '1',
              color: '#65A2FF',
            },
            {
              name: 'errors',
              yAxisIndex: '1',
              color: '#E6113C',
            }
          ]
        }
        yAxisIndex0List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '0'})
        })

        yAxisIndex1List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '1'})
        })
        this.loadOption = this.generateOption(loadOption, data, setting);
      }).catch(() => {
        this.loadOption = {};
      })
    },
    getResChart() {
      this.$get("/performance/report/content/res_chart/" + this.id).then(res => {
        let data = res.data.data;
        let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
        let yAxis2List = data.filter(m => m.yAxis === -1).map(m => m.yAxis2);
        let yAxisListMax = this._getChartMax(yAxisList);
        let yAxis2ListMax = this._getChartMax(yAxis2List);

        let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
        yAxisIndex0List = this._unique(yAxisIndex0List);
        let yAxisIndex1List = data.filter(m => m.yAxis === -1).map(m => m.groupName);
        yAxisIndex1List = this._unique(yAxisIndex1List);

        let resOption = {
          title: {
            text: 'Response Time',
            left: 'center',
            top: 20,
            textStyle: {
              color: '#99743C'
            },
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            // extraCssText: 'z-index: 999;',
            confine: true,
            formatter: function (params, ticket, callback) {
              let result = "";
              let name = params[0].name;
              result += name + "<br/>";
              for (let i = 0; i < params.length; i++) {
                let seriesName = params[i].seriesName;
                if (seriesName.length > 100) {
                  seriesName = seriesName.substring(0, 100);
                }
                let value = params[i].value;
                let marker = params[i].marker;
                result += marker + seriesName + ": " + value[1] + "<br/>";
              }

              return result;
            }
          },
          legend: {},
          xAxis: {},
          yAxis: [{
            name: 'User',
            type: 'value',
            min: 0,
            max: yAxisListMax,
            interval: yAxisListMax / 5
          },
            {
              name: 'Response Time',
              type: 'value',
              min: 0,
              max: yAxis2ListMax,
              interval: yAxis2ListMax / 5
            }
          ],
          series: []
        }
        let setting = {
          series: [
            {
              name: 'users',
              color: '#0CA74A',
            }
          ]
        }

        yAxisIndex0List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '0'})
        })

        yAxisIndex1List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '1'})
        })

        this.resOption = this.generateOption(resOption, data, setting);
      }).catch(() => {
        this.resOption = {};
      })
    },
    getErrorChart() {
      this.$get("/performance/report/content/error_chart/" + this.id).then(res => {
        let data = res.data.data;
        let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
        let yAxisListMax = this._getChartMax(yAxisList);

        let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
        yAxisIndex0List = this._unique(yAxisIndex0List);

        let errorOption = {
          title: {
            text: 'Errors',
            left: 'center',
            top: 20,
            textStyle: {
              color: '#99743C'
            },
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            // extraCssText: 'z-index: 999;',
            confine: true,
            formatter: function (params, ticket, callback) {
              let result = "";
              let name = params[0].name;
              result += name + "<br/>";
              for (let i = 0; i < params.length; i++) {
                let seriesName = params[i].seriesName;
                if (seriesName.length > 100) {
                  seriesName = seriesName.substring(0, 100);
                }
                let value = params[i].value;
                let marker = params[i].marker;
                result += marker + seriesName + ": " + value[1] + "<br/>";
              }

              return result;
            }
          },
          legend: {},
          xAxis: {},
          yAxis: [
            {
              name: 'No',
              type: 'value',
              min: 0,
              max: yAxisListMax,
              interval: yAxisListMax / 5
            }
          ],
          series: []
        }
        let setting = {
          series: [
            {
              name: 'users',
              color: '#0CA74A',
            }
          ]
        }

        yAxisIndex0List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '0'})
        })

        this.errorOption = this.generateOption(errorOption, data, setting);
      }).catch(() => {
        this.errorOption = {};
      })
    },
    getResponseCodeChart() {
      this.$get("/performance/report/content/response_code_chart/" + this.id).then(res => {
        let data = res.data.data;
        let yAxisList = data.filter(m => m.yAxis2 === -1).map(m => m.yAxis);
        let yAxisListMax = this._getChartMax(yAxisList);

        let yAxisIndex0List = data.filter(m => m.yAxis2 === -1).map(m => m.groupName);
        yAxisIndex0List = this._unique(yAxisIndex0List);

        let resCodeOption = {
          title: {
            text: 'Response code',
            left: 'center',
            top: 20,
            textStyle: {
              color: '#99743C'
            },
          },
          tooltip: {
            show: true,
            trigger: 'axis',
            // extraCssText: 'z-index: 999;',
            confine: true,
            formatter: function (params, ticket, callback) {
              let result = "";
              let name = params[0].name;
              result += name + "<br/>";
              for (let i = 0; i < params.length; i++) {
                let seriesName = params[i].seriesName;
                if (seriesName.length > 100) {
                  seriesName = seriesName.substring(0, 100);
                }
                let value = params[i].value;
                let marker = params[i].marker;
                result += marker + seriesName + ": " + value[1] + "<br/>";
              }

              return result;
            }
          },
          legend: {},
          xAxis: {},
          yAxis: [
            {
              name: 'No',
              type: 'value',
              min: 0,
              max: yAxisListMax,
              interval: yAxisListMax / 5
            }
          ],
          series: []
        }
        let setting = {
          series: [
            {
              name: 'users',
              color: '#0CA74A',
            }
          ]
        }

        yAxisIndex0List.forEach(item => {
          setting["series"].splice(0, 0, {name: item, yAxisIndex: '0'})
        })

        this.resCodeOption = this.generateOption(resCodeOption, data, setting);
      }).catch(() => {
        this.resCodeOption = {};
      })
    },
    generateOption(option, data, setting) {
      let chartData = data;
      let seriesArray = [];
      for (let set in setting) {
        if (set === "series") {
          seriesArray = setting[set];
          continue;
        }
        this.$set(option, set, setting[set]);
      }
      let legend = [], series = {}, xAxis = [], seriesData = [];
      chartData.forEach(item => {
        if (!xAxis.includes(item.xAxis)) {
          xAxis.push(item.xAxis);
        }
        xAxis.sort()
        let name = item.groupName
        if (!legend.includes(name)) {
          legend.push(name)
          series[name] = []
        }
        if (item.yAxis === -1) {
          series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis2.toFixed(2)]);
        } else {
          series[name].splice(xAxis.indexOf(item.xAxis), 0, [item.xAxis, item.yAxis.toFixed(2)]);
        }
      })
      this.$set(option.legend, "data", legend);
      this.$set(option.legend, "type", "scroll");
      this.$set(option.legend, "bottom", "10px");
      this.$set(option.xAxis, "data", xAxis);
      for (let name in series) {
        let d = series[name];
        d.sort((a, b) => a[0].localeCompare(b[0]));
        let items = {
          name: name,
          type: 'line',
          data: d
        };
        let seriesArrayNames = seriesArray.map(m => m.name);
        if (seriesArrayNames.includes(name)) {
          for (let j = 0; j < seriesArray.length; j++) {
            let seriesObj = seriesArray[j];
            if (seriesObj['name'] === name) {
              Object.assign(items, seriesObj);
            }
          }
        }
        seriesData.push(items);
      }
      this.$set(option, "series", seriesData);
      return option;
    },
    _getChartMax(arr) {
      const max = Math.max(...arr);
      return Math.ceil(max / 4.5) * 5;
    },
    _unique(arr) {
      return Array.from(new Set(arr));
    }
  },
  watch: {
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Completed" || status === "Running") {
          this.initTableData();
        } else {
          this.maxUsers = '0';
          this.avgThroughput = '0';
          this.avgTransactions = '0';
          this.errors = '0';
          this.avgResponseTime = '0';
          this.responseTime90 = '0';
          this.avgBandwidth = '0';
          this.loadOption = {};
          this.resOption = {};
          this.errorOption = {};
          this.resCodeOption = {};
        }
      },
      deep: true
    }
  },
  props: ['report']
}
</script>

<style scoped>

.ms-card-data {
  text-align: left;
  display: block;
  margin-bottom: 5px;
}

.ms-card-desc {
  display: block;
  text-align: left;
}

.ms-card-data-digital {
  font-size: 21px;
}

.ms-card-data-unit {
  color: #8492a6;
  font-size: 15px;
}

.ms-card-index-1 .ms-card-data-digital {
  color: #44b349;
}

.ms-card-index-1 {
  border-left-color: #44b349;
  border-left-width: 3px;
}

.ms-card-index-2 .ms-card-data-digital {
  color: #65A2FF;
}

.ms-card-index-2 {
  border-left-color: #65A2FF;
  border-left-width: 3px;
}

.ms-card-index-3 .ms-card-data-digital {
  color: #E6113C;
}

.ms-card-index-3 {
  border-left-color: #E6113C;
  border-left-width: 3px;
}

.ms-card-index-4 .ms-card-data-digital {
  color: #99743C;
}

.ms-card-index-4 {
  border-left-color: #99743C;
  border-left-width: 3px;
}

.ms-card-index-5 .ms-card-data-digital {
  color: #99743C;
}

.ms-card-index-5 {
  border-left-color: #99743C;
  border-left-width: 3px;
}

.ms-card-index-6 .ms-card-data-digital {
  color: #3C9899;
}

.ms-card-index-6 {
  border-left-color: #3C9899;
  border-left-width: 3px;
}

.chart-config {
  width: 100%;
}

</style>
