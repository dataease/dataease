<template>
  <div class="schedule-config">
    <div>
        <span class="cron-ico" @click="scheduleEdit">
          <i class="el-icon-date" size="small"></i>
          <span class="character">SCHEDULER</span>
        </span>
      <el-switch :disabled="!schedule.value || isReadOnly" v-model="schedule.enable" @change="scheduleChange"/>
      <ms-schedule-edit :is-read-only="isReadOnly" :schedule="schedule" :test-id="testId" :save="save"
                        :custom-validate="customValidate"
                        ref="scheduleEdit"/>

    </div>
    <div>
        <span>
          {{ $t('schedule.next_execution_time') }}：
          <span :class="{'disable-character': !schedule.enable}"
                v-if="!schedule.enable">{{ $t('schedule.not_set') }}</span>
          <crontab-result v-if="schedule.enable" :enable-simple-mode="true" :ex="schedule.value" ref="crontabResult"/>
        </span>
    </div>
  </div>
</template>

<script>
  import MsScheduleEdit from "./MsScheduleEdit";
  import CrontabResult from "../cron/CrontabResult";

  function defaultCustomValidate() {
    return {pass: true};
  }

  export default {
    name: "MsScheduleConfig",
    components: {CrontabResult, MsScheduleEdit},
    data() {
      return {
        recentList: [],
        refreshScheduler: null,
      }
    },
    props: {
      testId: String,
      save: Function,
      schedule: {},
      checkOpen: {
        type: Function,
        default() {
          return {
            checkOpen() {
              return true;
            }
          }
        }
      },
      customValidate: {
        type: Function,
        default: defaultCustomValidate
      },
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },
    methods: {
      scheduleEdit() {
        if (!this.checkOpen()) {
          return;
        }
        this.$refs.scheduleEdit.open();
      },
      scheduleChange() {
        this.$emit('scheduleChange');
      },
      flashResultList() {
        if (this.$refs.crontabResult) {
          this.$refs.crontabResult.expressionChange();
        }
      },
      cancelRefresh() {
        if (this.refreshScheduler) {
          clearInterval(this.refreshScheduler);
        }
      }
    },
    beforeDestroy() {
      this.cancelRefresh();
    },
    watch: {
      schedule() {
        if (this.schedule.enable) {
          this.refreshScheduler = setInterval(this.flashResultList, 2000);
        } else {
          this.cancelRefresh();
        }
      }
    }
  }
</script>

<style scoped>

  .schedule-config {
    float: right;
    width: 250px;
    height: 15px;
    line-height: 25px;
  }

  .el-icon-date {
    font-size: 20px;
    margin-left: 5px;
  }

  .character {
    font-weight: bold;
    margin: 0 5px;
  }

  .disable-character {
    color: #cccccc;
  }

  .el-switch {
    margin: 0 5px;
  }

  .cron-ico {
    cursor: pointer;
  }

</style>
