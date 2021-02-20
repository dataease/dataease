<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header" >
      <span class="title">
        {{$t('api_test.home_page.schedule_task_count_card.title')}}
      </span>
    </div>
    <el-container>
      <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
        <div class="main-number-show" style="margin: 0px auto;">
          <span class="count-number">
            {{scheduleTaskCountData.allApiDataCountNumber}}
          </span>
          <span style="color: #6C317C;">
            {{$t('api_test.home_page.unit_of_measurement')}}
          </span>
        </div>
      </el-main>
    </el-container>

    <el-container class="detail-container">
      <el-header style="height:20px;padding: 0px;margin-bottom: 10px;">
        <el-row :gutter="20" class="hidden-lg-and-down ">
          <el-col  :span="8">
            {{$t('api_test.home_page.schedule_task_details_card.this_week_add',[scheduleTaskCountData.thisWeekAddedCount])}}
          </el-col>
          <el-col :span="8" >
            {{$t('api_test.home_page.schedule_task_details_card.this_week_execute',[scheduleTaskCountData.thisWeekExecutedCount])}}
          </el-col>
          <el-col :span="8" >
            {{$t('api_test.home_page.schedule_task_details_card.executed',[scheduleTaskCountData.executedCount])}}
          </el-col>
        </el-row>
        <el-row :gutter="20" class="hidden-xl-only">
          <el-col  :span="8">
            <div class="count-info-div" v-html="$t('api_test.home_page.schedule_task_details_card.this_week_add_sm',[scheduleTaskCountData.thisWeekAddedCount])"></div>
          </el-col>
          <el-col :span="8" >
            <div class="count-info-div" v-html="$t('api_test.home_page.schedule_task_details_card.this_week_execute_sm',[scheduleTaskCountData.thisWeekExecutedCount])"></div>
          </el-col>
          <el-col :span="8" >
            <div class="count-info-div" v-html="$t('api_test.home_page.schedule_task_details_card.executed_sm',[scheduleTaskCountData.executedCount])"></div>
          </el-col>
        </el-row>
      </el-header>
      <el-main style="padding: 5px;margin-top: 10px">
        <el-container>
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container>
              <el-aside width="30%">
                {{$t('api_test.home_page.detail_card.rate.success')+":"}}
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
                <span class="count-number">
                {{scheduleTaskCountData.successRage}}
              </span>
              </el-main>
            </el-container>
          </el-aside>
          <el-main style="padding: 5px">
            <el-card class="no-shadow-card" body-style="padding-left:5px;padding-right:5px;">
              <main>
                <el-row>
                  <el-col>
                    <span class="default-property">
                      {{$t('api_test.home_page.detail_card.failed')}}
                      {{"\xa0\xa0"}}
                      {{scheduleTaskCountData.failedCount}}
                    </span>
                  </el-col>
                  <el-col style=" height: 20px;margin-top: 3px;">
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{$t('api_test.home_page.detail_card.success')}}
                      {{"\xa0\xa0"}}
                      {{scheduleTaskCountData.successCount}}
                    </span>
                  </el-col>
                </el-row>
              </main>
            </el-card>
          </el-main>
        </el-container>
      </el-main>
    </el-container>
  </el-card>
</template>

<script>
import 'element-ui/lib/theme-chalk/display.css';


export default {

  name: "MsScheduleTaskInfoCard",

  components: {},

  data() {
    return {
      result: {},
      apiCountData: {},
      loading: false
    }
  },
  props:{
    scheduleTaskCountData:{},
  },
  methods: {
  },

  created() {
  },
  activated() {
  }
}
</script>
<style scoped>
.el-aside {
  line-height: 100px;
  text-align: center;
}
.count-number{
  font-family:'ArialMT', 'Arial', sans-serif;
  font-size:33px;
  color: #6C317C;
}

.main-number-show {
  width: 100px;
  height: 100px;
  border-style: solid;
  border-width: 7px;
  border-color: #CDB9D2;
  border-radius:50%;

}

.count-number-show{
  margin:20px auto;
}
.detail-container{
  margin-top: 30px
}
.no-shadow-card{
  -webkit-box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
  box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
}
.default-property{
  font-size: 12px
}
.main-property{
  color: #F39021;
  font-size: 12px;
}

.el-card /deep/ .el-card__header {
  border-bottom: 0px solid #EBEEF5;
}

.count-info-div{
  margin: 3px;
}
.count-info-div >>>p{
  font-size: 10px;
}
</style>
