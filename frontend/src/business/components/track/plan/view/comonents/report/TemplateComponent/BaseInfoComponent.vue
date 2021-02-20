<template>

  <common-component :title="$t('test_track.plan_view.base_info')">

    <template>

      <el-row type="flex" justify="space-between">
        <el-col :span="12">
          <span>{{$t('test_track.plan.plan_project')}}：</span>
          <span class="item-value">{{reportInfo.projectName}}</span>
        </el-col>
        <el-col :span="12">
          <span>{{$t('test_track.plan.plan_principal')}}：</span>
          <span class="item-value">{{reportInfo.principal}}</span>
        </el-col>
      </el-row>

      <el-row type="flex" justify="space-between" class="select-time">
        <el-col :span="12">
          <span>{{$t('report.test_start_time')}}：</span>
          <span v-if="!isReport">{{reportInfo.startTime}}</span>
          <el-date-picker @change="startTimeChange" v-if="isReport" size="mini" type="date" :placeholder="$t('commons.select_date')" v-model="reportInfo.startTime"/>
        </el-col>
        <el-col :span="12">
          <span>{{$t('report.test_end_time')}}：</span>
          <span v-if="!isReport">{{reportInfo.endTime}}</span>
          <el-date-picker @change="endTimeChange" v-if="isReport" size="mini" type="date" :placeholder="$t('commons.select_date')" v-model="reportInfo.endTime"/>
        </el-col>
      </el-row>

      <el-row type="flex" justify="space-between">
        <el-col :span="12">
          <span>{{$t('test_track.plan_view.executor')}}：</span>
          <span v-for="item in reportInfo.executors" :key="item">{{item}}</span>
        </el-col>
      </el-row>

    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    export default {
      name: "BaseInfoComponent",
      components: {CommonComponent},
      props: {
        reportInfo: {
          type: Object,
          default() {
            return {
              projectName: this.$t('test_track.project'),
              principal: 'Michael',
              executors: ['Michael','Tom','Jiessie'],
              startTime: '2020-6-18',
              endTime: '2020-6-19'
            }
          }
        },
        isReport: {
          type: Boolean,
          default: true
        }
      },
      methods: {
        startTimeChange(value) {
          if (!!this.reportInfo.endTime && this.reportInfo.endTime - this.reportInfo.startTime < 0) {
            this.reportInfo.startTime = undefined;
            this.$warning(this.$t('commons.date.data_time_error'));
          }
        },
        endTimeChange(value) {
          if (!!this.reportInfo.startTime && this.reportInfo.endTime - this.reportInfo.startTime < 0) {
            this.reportInfo.endTime = undefined;
            this.$warning(this.$t('commons.date.data_time_error'));
          }
        }
      }
    }
</script>

<style scoped>

  span {
    margin-right: 5px;
    display: inline-block;
  }

  .el-col span:first-child {
    font-weight: bold;
    width: 100px;
  }

  .el-row {
    height: 60px;
  }

  .select-time span {
    display: inline-block;
  }

  .el-date-editor {
    width: 150px;
  }

</style>
