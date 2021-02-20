<template>
  <div class="schedule-config">
    <div>
        <span class="cron-ico">
          <i class="el-icon-date" size="small"></i>
          <span class="character">SCHEDULER</span>
        </span>
<!--      <el-switch :disabled="!schedule.value || isReadOnly" v-model="schedule.enable" @change="scheduleChange"/>-->
<!--      <el-switch :disabled="!schedule.value || isReadOnly" v-model="schedule.enable" />-->
      <el-switch :disabled="!schedule.value" v-model="schedule.enable" @change="scheduleChange"/>
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
import CrontabResult from "@/business/components/common/cron/CrontabResult";

export default {
  name: "ScheduleSwitch",
  components: {CrontabResult},
  data() {
    return {
    }
  },
  props: {
    testId: String,
    schedule: Object,
    isReadOnly: {
      type: Boolean,
      default: false
    }
  },
  methods: {
    scheduleChange() {
      this.$emit('scheduleChange');
    },
  },
  watch: {
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
