<template>
  <el-card>
    <template v-slot:header>
      <span class="title">{{$t('commons.calendar_heatmap')}}</span>
    </template>
    <calendar-heatmap :end-date="endDate" :values="values" :locale="locale"
                      :tooltip-unit="unit"
                      :range-color="colorRange"/>
  </el-card>
</template>

<script>
  export default {
    name: "MsTestHeatmap",
    props: ['values'],
    data() {
      return {
        endDate: (new Date().getTime() + 7*24*60*60*1000),
        colorRange: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127'],
      }
    },
    computed: {
      locale() {
        return {
          months: [
            this.$t('commons.months_1'),
            this.$t('commons.months_2'),
            this.$t('commons.months_3'),
            this.$t('commons.months_4'),
            this.$t('commons.months_5'),
            this.$t('commons.months_6'),
            this.$t('commons.months_7'),
            this.$t('commons.months_8'),
            this.$t('commons.months_9'),
            this.$t('commons.months_10'),
            this.$t('commons.months_11'),
            this.$t('commons.months_12')
          ],
          days: [
            this.$t('commons.weeks_0'),
            this.$t('commons.weeks_1'),
            this.$t('commons.weeks_2'),
            this.$t('commons.weeks_3'),
            this.$t('commons.weeks_4'),
            this.$t('commons.weeks_5'),
            this.$t('commons.weeks_6')
          ],
          No: 'No',
          on: ',',
          less: 'Less',
          more: 'More'
        }
      },
      unit() {
        return this.$t('commons.test_unit')
      }
    },
  }
</script>

<style scoped>

  .el-card {
    height: 370px;
  }

</style>
