<template>
  <div v-loading="result.loading" class="pressure-config-container">
    <el-row>
      <el-col>
        <el-form :inline="true">
          <el-form-item :label="$t('load_test.select_resource_pool')">
            <el-select v-model="resourcePool" :disabled="isReadOnly" size="mini">
              <el-option
                v-for="item in resourcePools"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <ms-chart class="chart-container" ref="chart1" :options="options" :autoresize="true"></ms-chart>
      </el-col>
    </el-row>
    <el-row>
      <el-collapse v-model="activeNames">
        <el-collapse-item :title="threadGroup.attributes.testname" :name="index"
                          v-for="(threadGroup, index) in threadGroups"
                          :key="index">
          <el-col :span="10">
            <el-form :inline="true">
              <el-form-item :label="$t('load_test.thread_num')">
                <el-input-number
                  :disabled="isReadOnly"
                  v-model="threadGroup.threadNumber"
                  @change="calculateChart(threadGroup)"
                  :min="resourcePoolResourceLength"
                  size="mini"/>
              </el-form-item>
              <br>
              <el-form-item>
                <el-radio-group v-model="threadGroup.threadType">
                  <el-radio label="DURATION">{{ $t('load_test.by_duration') }}</el-radio>
                  <el-radio label="ITERATION">{{ $t('load_test.by_iteration') }}</el-radio>
                </el-radio-group>
              </el-form-item>
              <br>
              <div v-if="threadGroup.threadType === 'DURATION'">
                <el-form-item :label="$t('load_test.duration')">
                  <el-input-number
                    :disabled="isReadOnly"
                    v-model="threadGroup.duration"
                    :min="1"
                    @change="calculateChart(threadGroup)"
                    size="mini"/>
                </el-form-item>
                <br>
                <el-form-item :label="$t('load_test.rps_limit')">
                  <el-switch v-model="threadGroup.rpsLimitEnable" @change="calculateTotalChart()"/>
                  &nbsp;
                  <el-input-number
                    :disabled="isReadOnly || !threadGroup.rpsLimitEnable"
                    v-model="threadGroup.rpsLimit"
                    @change="calculateChart(threadGroup)"
                    :min="1"
                    :max="500"
                    size="mini"/>
                </el-form-item>
                <br>
                <el-form-item :label="$t('load_test.ramp_up_time_within')">
                  <el-input-number
                    :disabled="isReadOnly"
                    :min="1"
                    :max="threadGroup.duration"
                    v-model="threadGroup.rampUpTime"
                    @change="calculateChart(threadGroup)"
                    size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('load_test.ramp_up_time_minutes')">
                  <el-input-number
                    :disabled="isReadOnly"
                    :min="1"
                    :max="Math.min(threadGroup.threadNumber, threadGroup.rampUpTime)"
                    v-model="threadGroup.step"
                    @change="calculateChart(threadGroup)"
                    size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('load_test.ramp_up_time_times')"/>
              </div>
              <div v-if="threadGroup.threadType === 'ITERATION'">
                <el-form-item :label="$t('load_test.iterate_num')">
                  <el-input-number
                    :disabled="isReadOnly"
                    v-model="threadGroup.iterateNum"
                    :min="1"
                    :max="10000"
                    @change="calculateChart(threadGroup)"
                    size="mini"/>
                </el-form-item>
                <br>
                <el-form-item :label="$t('load_test.rps_limit')">
                  <el-switch v-model="threadGroup.rpsLimitEnable" @change="calculateTotalChart()"/>
                  &nbsp;
                  <el-input-number
                    :disabled="isReadOnly || !threadGroup.rpsLimitEnable"
                    v-model="threadGroup.rpsLimit"
                    :min="1"
                    :max="500"
                    size="mini"/>
                </el-form-item>
                <br>
                <el-form-item :label="$t('load_test.ramp_up_time_within')">
                  <el-input-number
                    :disabled="isReadOnly"
                    :min="1"
                    v-model="threadGroup.iterateRampUp"
                    size="mini"/>
                </el-form-item>
                <el-form-item :label="$t('load_test.ramp_up_time_seconds')"/>
              </div>
            </el-form>
          </el-col>
          <el-col :span="14">
            <div class="title">{{ $t('load_test.pressure_prediction_chart') }}</div>
            <ms-chart class="chart-container" :options="threadGroup.options" :autoresize="true"></ms-chart>
          </el-col>
        </el-collapse-item>
      </el-collapse>
    </el-row>
  </div>
</template>

<script>
import echarts from "echarts";
import MsChart from "@/business/components/common/chart/MsChart";
import {findTestPlan, findThreadGroup} from "@/business/components/performance/test/model/ThreadGroup";

const TARGET_LEVEL = "TargetLevel";
const RAMP_UP = "RampUp";
const ITERATE_RAMP_UP = "iterateRampUpTime";
const STEPS = "Steps";
const DURATION = "duration";
const RPS_LIMIT = "rpsLimit";
const RPS_LIMIT_ENABLE = "rpsLimitEnable";
const HOLD = "Hold";
const THREAD_TYPE = "threadType";
const ITERATE_NUM = "iterateNum";

const hexToRgba = function (hex, opacity) {
  return 'rgba(' + parseInt('0x' + hex.slice(1, 3)) + ',' + parseInt('0x' + hex.slice(3, 5)) + ','
    + parseInt('0x' + hex.slice(5, 7)) + ',' + opacity + ')';
}
const hexToRgb = function (hex) {
  return 'rgb(' + parseInt('0x' + hex.slice(1, 3)) + ',' + parseInt('0x' + hex.slice(3, 5))
    + ',' + parseInt('0x' + hex.slice(5, 7)) + ')';
}

export default {
  name: "PerformancePressureConfig",
  components: {MsChart},
  props: {
    test: {
      type: Object
    },
    testId: {
      type: String
    },
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      result: {},
      threadNumber: 0,
      duration: 0,
      rampUpTime: 0,
      step: 0,
      rpsLimit: 0,
      rpsLimitEnable: false,
      options: {},
      resourcePool: null,
      resourcePools: [],
      activeNames: ["0"],
      threadGroups: [],
      serializeThreadgroups: false,
      resourcePoolResourceLength: 1
    }
  },
  mounted() {
    if (this.testId) {
      this.getJmxContent();
    } else {
      this.calculateTotalChart();
    }
    this.resourcePool = this.test.testResourcePoolId;
    this.getResourcePools();
  },
  watch: {
    test(n) {
      this.resourcePool = n.testResourcePoolId;
    },
    testId() {
      if (this.testId) {
        this.getJmxContent();
      } else {
        this.calculateTotalChart();
      }
      this.getResourcePools();
    },
  },
  methods: {
    getResourcePools() {
      this.result = this.$get('/testresourcepool/list/quota/valid', response => {
        this.resourcePools = response.data;
        // 如果当前的资源池无效 设置 null
        if (response.data.filter(p => p.id === this.resourcePool).length === 0) {
          this.resourcePool = null;
        }
      })
    },
    getLoadConfig() {
      this.$get('/performance/get-load-config/' + this.testId, (response) => {
        if (response.data) {
          let data = JSON.parse(response.data);
          for (let i = 0; i < data.length; i++) {
            let d = data[i];
            if (d instanceof Array) {
              d.forEach(item => {
                switch (item.key) {
                  case TARGET_LEVEL:
                    this.threadGroups[i].threadNumber = item.value;
                    break;
                  case RAMP_UP:
                    this.threadGroups[i].rampUpTime = item.value;
                    break;
                  case ITERATE_RAMP_UP:
                    this.threadGroups[i].iterateRampUp = item.value;
                    break;
                  case DURATION:
                    if (item.unit) {
                      this.threadGroups[i].duration = item.value;
                    } else {
                      this.threadGroups[i].duration = item.value * 60;
                    }
                    break;
                  case STEPS:
                    this.threadGroups[i].step = item.value;
                    break;
                  case RPS_LIMIT:
                    this.threadGroups[i].rpsLimit = item.value;
                    break;
                  case RPS_LIMIT_ENABLE:
                    this.threadGroups[i].rpsLimitEnable = item.value;
                    break;
                  case THREAD_TYPE:
                    this.threadGroups[i].threadType = item.value;
                    break;
                  case ITERATE_NUM:
                    this.threadGroups[i].iterateNum = item.value;
                    break;
                  default:
                    break;
                }
                //
                this.$set(this.threadGroups[i], "threadType", this.threadGroups[i].threadType || 'DURATION');
                this.$set(this.threadGroups[i], "iterateNum", this.threadGroups[i].iterateNum || 1);
                this.$set(this.threadGroups[i], "iterateRampUp", this.threadGroups[i].iterateRampUp || 10);
              })
              this.calculateChart(this.threadGroups[i]);
            } else {
              switch (d.key) {
                case TARGET_LEVEL:
                  this.threadGroups[0].threadNumber = d.value;
                  break;
                case RAMP_UP:
                  this.threadGroups[0].rampUpTime = d.value;
                  break;
                case ITERATE_RAMP_UP:
                  this.threadGroups[0].iterateRampUp = d.value;
                  break;
                case DURATION:
                  if (d.unit) {
                    this.threadGroups[0].duration = d.value;
                  } else {
                    this.threadGroups[0].duration = d.value * 60;
                  }
                  break;
                case STEPS:
                  this.threadGroups[0].step = d.value;
                  break;
                case RPS_LIMIT:
                  this.threadGroups[0].rpsLimit = d.value;
                  break;
                case RPS_LIMIT_ENABLE:
                  this.threadGroups[0].rpsLimitEnable = d.value;
                  break;
                case THREAD_TYPE:
                  this.threadGroups[0].threadType = d.value;
                  break;
                case ITERATE_NUM:
                  this.threadGroups[0].iterateNum = d.value;
                  break;
                default:
                  break;
              }
              this.$set(this.threadGroups[0], "threadType", this.threadGroups[0].threadType || 'DURATION');
              this.$set(this.threadGroups[0], "iterateNum", this.threadGroups[0].iterateNum || 1);
              this.$set(this.threadGroups[0], "iterateRampUp", this.threadGroups[0].iterateRampUp || 10);
              this.calculateChart(this.threadGroups[0]);
            }
          }
          this.calculateTotalChart();
        }
      });
    },
    getJmxContent() {
      if (this.testId) {
        this.$get('/performance/get-jmx-content/' + this.testId, (response) => {
          if (response.data) {
            let testPlan = findTestPlan(response.data);
            testPlan.elements.forEach(e => {
              if (e.attributes.name === 'TestPlan.serialize_threadgroups') {
                this.serializeThreadgroups = Boolean(e.elements[0].text);
              }
            });
            this.threadGroups = findThreadGroup(response.data);
            this.threadGroups.forEach(tg => {
              tg.options = {};
            });
            this.getLoadConfig();
          }
        });
      }
    },
    calculateTotalChart() {
      let handler = this;
      if (handler.duration < handler.rampUpTime) {
        handler.rampUpTime = handler.duration;
      }
      if (handler.rampUpTime < handler.step) {
        handler.step = handler.rampUpTime;
      }
      let color = ['#60acfc', '#32d3eb', '#5bc49f', '#feb64d', '#ff7c7c', '#9287e7', '#ca8622', '#bda29a', '#6e7074', '#546570', '#c4ccd3'];
      handler.options = {
        color: color,
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value'
        },
        tooltip: {
          trigger: 'axis',
        },
        series: []
      };

      for (let i = 0; i < handler.threadGroups.length; i++) {
        let seriesData = {
          name: handler.threadGroups[i].attributes.testname,
          data: [],
          type: 'line',
          step: 'start',
          smooth: false,
          symbolSize: 5,
          showSymbol: false,
          lineStyle: {
            normal: {
              width: 1
            }
          },
          areaStyle: {
            normal: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                offset: 0,
                color: hexToRgba(color[i % color.length], 0.3),
              }, {
                offset: 0.8,
                color: hexToRgba(color[i % color.length], 0),
              }], false),
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              shadowBlur: 10
            }
          },
          itemStyle: {
            normal: {
              color: hexToRgb(color[i % color.length]),
              borderColor: 'rgba(137,189,2,0.27)',
              borderWidth: 12
            }
          },
        };

        let tg = handler.threadGroups[i];

        let timePeriod = Math.floor(tg.rampUpTime / tg.step);
        let timeInc = timePeriod;

        let threadPeriod = Math.floor(tg.threadNumber / tg.step);
        let threadInc1 = Math.floor(tg.threadNumber / tg.step);
        let threadInc2 = Math.ceil(tg.threadNumber / tg.step);
        let inc2count = tg.threadNumber - tg.step * threadInc1;
        for (let j = 0; j <= tg.duration; j++) {

          if (j > timePeriod) {
            timePeriod += timeInc;
            if (inc2count > 0) {
              threadPeriod = threadPeriod + threadInc2;
              inc2count--;
            } else {
              threadPeriod = threadPeriod + threadInc1;
            }
            if (threadPeriod > tg.threadNumber) {
              threadPeriod = tg.threadNumber;
            }
          }
          // x 轴
          let xAxis = handler.options.xAxis.data;
          if (xAxis.indexOf(j) < 0) {
            xAxis.push(j);
          }
          seriesData.data.push(threadPeriod);
        }
        handler.options.series.push(seriesData);
      }
    },
    calculateChart(threadGroup) {
      let handler = this;
      if (threadGroup) {
        handler = threadGroup;
      }
      if (handler.duration < handler.rampUpTime) {
        handler.rampUpTime = handler.duration;
      }
      if (handler.rampUpTime < handler.step) {
        handler.step = handler.rampUpTime;
      }
      // 线程数不能小于资源池节点的数量
      let resourcePool = this.resourcePools.filter(v => v.id === this.resourcePool)[0];
      if (resourcePool) {
        this.resourcePoolResourceLength = resourcePool.resources.length;
      }
      handler.options = {
        xAxis: {
          type: 'category',
          boundaryGap: false,
          data: []
        },
        yAxis: {
          type: 'value'
        },
        tooltip: {
          trigger: 'axis',
          formatter: '{a}: {c0}',
          axisPointer: {
            lineStyle: {
              color: '#57617B'
            }
          }
        },
        series: [{
          name: 'User',
          data: [],
          type: 'line',
          step: 'start',
          smooth: false,
          symbolSize: 5,
          showSymbol: false,
          lineStyle: {
            normal: {
              width: 1
            }
          },
          areaStyle: {
            normal: {
              color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                offset: 0,
                color: 'rgba(137, 189, 27, 0.3)'
              }, {
                offset: 0.8,
                color: 'rgba(137, 189, 27, 0)'
              }], false),
              shadowColor: 'rgba(0, 0, 0, 0.1)',
              shadowBlur: 10
            }
          },
          itemStyle: {
            normal: {
              color: 'rgb(137,189,27)',
              borderColor: 'rgba(137,189,2,0.27)',
              borderWidth: 12
            }
          },
        }]
      };
      let timePeriod = Math.floor(handler.rampUpTime / handler.step);
      let timeInc = timePeriod;

      let threadPeriod = Math.floor(handler.threadNumber / handler.step);
      let threadInc1 = Math.floor(handler.threadNumber / handler.step);
      let threadInc2 = Math.ceil(handler.threadNumber / handler.step);
      let inc2count = handler.threadNumber - handler.step * threadInc1;
      for (let i = 0; i <= handler.duration; i++) {
        // x 轴
        handler.options.xAxis.data.push(i);
        if (i > timePeriod) {
          timePeriod += timeInc;
          if (inc2count > 0) {
            threadPeriod = threadPeriod + threadInc2;
            inc2count--;
          } else {
            threadPeriod = threadPeriod + threadInc1;
          }
          if (threadPeriod > handler.threadNumber) {
            threadPeriod = handler.threadNumber;
          }
          handler.options.series[0].data.push(threadPeriod);
        } else {
          handler.options.series[0].data.push(threadPeriod);
        }
      }
      this.calculateTotalChart();
    },
    validConfig() {
      if (!this.resourcePool) {
        this.$warning(this.$t('load_test.resource_pool_is_null'));
        // 资源池为空，设置参数为资源池所在Tab的name
        this.$emit('changeActive', '1');
        return false;
      }

      for (let i = 0; i < this.threadGroups.length; i++) {
        if (!this.threadGroups[i].threadNumber || !this.threadGroups[i].duration
          || !this.threadGroups[i].rampUpTime || !this.threadGroups[i].step || !this.threadGroups[i].iterateNum) {
          this.$warning(this.$t('load_test.pressure_config_params_is_empty'));
          this.$emit('changeActive', '1');
          return false;
        }

        if (this.threadGroups[i].rpsLimitEnable && !this.threadGroups[i].rpsLimit) {
          this.$warning(this.$t('load_test.pressure_config_params_is_empty'));
          this.$emit('changeActive', '1');
          return false;
        }
      }

      return true;
    },
    convertProperty() {
      /// todo：下面4个属性是jmeter ConcurrencyThreadGroup plugin的属性，这种硬编码不太好吧，在哪能转换这种属性？
      let result = [];
      for (let i = 0; i < this.threadGroups.length; i++) {
        result.push([
          {key: TARGET_LEVEL, value: this.threadGroups[i].threadNumber},
          {key: RAMP_UP, value: this.threadGroups[i].rampUpTime},
          {key: STEPS, value: this.threadGroups[i].step},
          {key: DURATION, value: this.threadGroups[i].duration, unit: 'S'},
          {key: RPS_LIMIT, value: this.threadGroups[i].rpsLimit},
          {key: RPS_LIMIT_ENABLE, value: this.threadGroups[i].rpsLimitEnable},
          {key: HOLD, value: this.threadGroups[i].duration - this.threadGroups[i].rampUpTime},
          {key: THREAD_TYPE, value: this.threadGroups[i].threadType},
          {key: ITERATE_NUM, value: this.threadGroups[i].iterateNum},
          {key: ITERATE_RAMP_UP, value: this.threadGroups[i].iterateRampUp},
        ]);
      }
      return result;
    }
  }
}
</script>


<style scoped>
.pressure-config-container .el-input {
  width: 130px;

}

.pressure-config-container .config-form-label {
  width: 130px;
}

.pressure-config-container .input-bottom-border input {
  border: 0;
  border-bottom: 1px solid #DCDFE6;
}

.chart-container {
  width: 100%;
  height: 300px;
}

.el-col .el-form {
  margin-top: 15px;
  text-align: left;
}

.el-col {
  margin-top: 15px;
  text-align: left;
}

.title {
  margin-left: 60px;
}
</style>
