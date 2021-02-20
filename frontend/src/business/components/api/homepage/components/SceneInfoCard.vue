<template>
  <el-card class="table-card" v-loading="result.loading" body-style="padding:10px;">
    <div slot="header" >
      <span class="title">
        {{$t('api_test.home_page.test_scene_count_card.title')}}
      </span>
    </div>
    <el-container>
      <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
        <div class="main-number-show" style="margin: 0px auto;">
          <span class="count-number">
            {{sceneCountData.allApiDataCountNumber}}
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
          {{$t('api_test.home_page.test_scene_details_card.this_week_add')}}
          <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">{{sceneCountData.thisWeekAddedCount}}
          </el-link>
          {{$t('api_test.home_page.unit_of_measurement')}}
        </el-col>
        <el-col :span="8" >
          {{$t('api_test.home_page.test_scene_details_card.this_week_execute',[sceneCountData.thisWeekExecutedCount])}}
        </el-col>
        <el-col :span="8" >
          {{$t('api_test.home_page.test_scene_details_card.executed',[sceneCountData.executedCount])}}
        </el-col>
      </el-row>
      <el-row :gutter="20" class="hidden-xl-only">
        <el-col  :span="8">
          {{$t('api_test.home_page.test_scene_details_card.this_week_add_sm')}}
          <br/>
          <el-link type="info" @click="redirectPage('thisWeekCount')" target="_blank" style="color: #000000">{{sceneCountData.thisWeekAddedCount}}
          </el-link>
          {{$t('api_test.home_page.unit_of_measurement')}}
        </el-col>
        <el-col :span="8" >
          <div class="count-info-div" v-html="$t('api_test.home_page.test_scene_details_card.this_week_execute_sm',[sceneCountData.thisWeekExecutedCount])"></div>
        </el-col>
        <el-col :span="8" >
          <div class="count-info-div" v-html="$t('api_test.home_page.test_scene_details_card.executed_sm',[sceneCountData.executedCount])"></div>
        </el-col>
      </el-row>
      </el-header>
      <el-main style="padding: 5px;margin-top: 10px">
        <el-container>
          <el-aside width="60%" class="count-number-show" style="margin-bottom: 0px;margin-top: 0px">
            <el-container>
              <el-aside width="30%">
                {{$t('api_test.home_page.detail_card.rate.pass')+":"}}
              </el-aside>
              <el-main style="padding: 0px 0px 0px 0px; line-height: 100px; text-align: center;">
                <span class="count-number">
                {{sceneCountData.passRage}}
              </span>
              </el-main>
            </el-container>
          </el-aside>
          <el-main style="padding: 5px">
            <el-card class="no-shadow-card" body-style="padding-left:5px;padding-right:5px">
              <main>
                <el-row>
                  <el-col>
                    <span class="defaultProperty">
                      {{$t('api_test.home_page.detail_card.unexecute')}}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('unExecute')" target="_blank" style="color: #000000">
                        {{sceneCountData.unexecuteCount}}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="defaultProperty">
                      {{$t('api_test.home_page.detail_card.execution_failed')}}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('executeFailed')" target="_blank" style="color: #000000">
                        {{sceneCountData.executionFailedCount}}
                      </el-link>
                    </span>
                  </el-col>
                  <el-col style="margin-top: 5px;">
                    <span class="main-property">
                      {{$t('api_test.home_page.detail_card.execution_pass')}}
                      {{"\xa0\xa0"}}
                      <el-link type="info" @click="redirectPage('executePass')" target="_blank" style="color: #000000">
                        {{sceneCountData.executionPassCount}}
                      </el-link>
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

export default {
  name: "MsSceneInfoCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  props:{
    sceneCountData:{},
  },

  methods: {
    redirectPage(clickType){
      this.$emit("redirectPage","scenario","scenario",clickType);
    }
  },
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
  margin:20px auto;
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
.defaultProperty{
  font-size: 12px
}
.main-property{
  color: #F39021;
  font-size: 12px
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
