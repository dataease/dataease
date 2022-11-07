<template lang="html">
  <div
    class="cron"
    :val="value_"
  >
    <el-tabs v-model="activeName">
      <el-tab-pane
        :label="$t('cron.second')"
        name="s"
      >
        <second-and-minute
          v-model="sVal"
          :label="$t('cron.second')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.minute')"
        name="m"
      >
        <second-and-minute
          v-model="mVal"
          :label="$t('cron.minute')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.hour')"
        name="h"
      >
        <hour
          v-model="hVal"
          :label="$t('cron.hour')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.day')"
        name="d"
      >
        <day
          v-model="dVal"
          :label="$t('cron.day')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.month')"
        name="month"
      >
        <month
          v-model="monthVal"
          :label="$t('cron.month')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.week')"
        name="week"
      >
        <week
          v-model="weekVal"
          :label="$t('cron.week')"
        />
      </el-tab-pane>
      <el-tab-pane
        :label="$t('cron.year')"
        name="year"
      >
        <year
          v-model="yearVal"
          :label="$t('cron.year')"
        />
      </el-tab-pane>
    </el-tabs>
    <!-- table -->
    <el-table
      :data="tableData"
      size="mini"
      border
      style="width: 100%;"
    >
      <el-table-column
        prop="sVal"
        :label="$t('cron.second')"
        width="70"
      />
      <el-table-column
        prop="mVal"
        :label="$t('cron.minute')"
        width="70"
      />
      <el-table-column
        prop="hVal"
        :label="$t('cron.hour')"
        width="70"
      />
      <el-table-column
        prop="dVal"
        :label="$t('cron.day')"
        width="70"
      />
      <el-table-column
        prop="monthVal"
        :label="$t('cron.month')"
        width="70"
      />
      <el-table-column
        prop="weekVal"
        :label="$t('cron.week')"
        width="70"
      />
      <el-table-column
        prop="yearVal"
        :label="$t('cron.year')"
      />
    </el-table>
  </div>
</template>

<script>
import SecondAndMinute from './cron/SecondAndMinute'
import hour from './cron/Hour'
import day from './cron/Day'
import month from './cron/Month'
import week from './cron/Week'
import year from './cron/Year'
export default {
  components: {
    SecondAndMinute, hour, day, month, week, year
  },
  props: {
    value: {
      type: String
    },
    isRate: {
      type: Boolean
    }
  },
  data() {
    return {
      //
      activeName: 's',
      sVal: '',
      mVal: '',
      hVal: '',
      dVal: '',
      monthVal: '',
      weekVal: '',
      yearVal: ''
    }
  },
  computed: {
    tableData() {
      return [{
        sVal: this.sVal,
        mVal: this.mVal,
        hVal: this.hVal,
        dVal: this.dVal,
        monthVal: this.monthVal,
        weekVal: this.weekVal,
        yearVal: this.yearVal
      }]
    },
    value_() {
      if (!this.dVal && !this.weekVal) {
        return ''
      }
      if (this.dVal === '?' && this.weekVal === '?') {
        this.$message({
          message: this.$t('cron.d_w_cant_not_set'),
          type: 'error'
        })
      }
      if (this.dVal !== '?' && this.weekVal !== '?') {
        this.$message({
          message: this.$t('cron.d_w_must_one_set'),
          type: 'error'
        })
      }
      const v = `${this.sVal} ${this.mVal} ${this.hVal} ${this.dVal} ${this.monthVal} ${this.weekVal} ${this.yearVal}`
      // if (v !== this.value) {
      this.$emit('input', v)
      // }
      return v
    }
  },
  watch: {
    value: {
      handler() {
        if (!this.isRate) return
        this.updateVal()
      },
      immediate: true
    }
  },
  methods: {
    updateVal() {
      if (!this.value) {
        return
      }
      const arrays = this.value.split(' ')
      this.sVal = arrays[0]
      this.mVal = arrays[1]
      this.hVal = arrays[2]
      this.dVal = arrays[3]
      this.monthVal = arrays[4]
      this.weekVal = arrays[5]
      this.yearVal = arrays[6]
    }
  }
}
</script>

<style lang="css" scoped>
.cron {
  text-align: left;
  padding: 10px;
  background: #fff;
  border: 1px solid #dcdfe6;
  box-shadow: 0 2px 4px 0 rgba(0,0,0,.12), 0 0 6px 0 rgba(0,0,0,.04);
}
</style>
