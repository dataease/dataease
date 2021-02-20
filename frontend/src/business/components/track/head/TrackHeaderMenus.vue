<template>

  <div id="menu-bar" v-if="isRouterAlive">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="14">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router
                 :default-active='$route.path'>
          <el-menu-item :index="'/track/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']"
                      index="6" popper-class="submenu">
            <template v-slot:title>{{ $t('test_track.case.test_case') }}</template>
            <ms-recent-list ref="caseRecent" :options="caseRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/case/all'"/>
            <el-menu-item :index="testCaseEditPath" class="blank_item"></el-menu-item>
            <el-menu-item :index="testCaseProjectPath" class="blank_item"></el-menu-item>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/case/create'"
                              :title="$t('test_track.case.create_case')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']"
                      index="8" popper-class="submenu">
            <template v-slot:title>{{$t('test_track.review.test_review')}}</template>
            <ms-recent-list ref="reviewRecent" :options="reviewRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/review/all'"/>
            <el-menu-item :index="testCaseReviewEditPath" class="blank_item"/>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/review/create'" :title="$t('test_track.review.create_review')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="7" popper-class="submenu">
            <template v-slot:title>{{ $t('test_track.plan.test_plan') }}</template>
            <ms-recent-list ref="planRecent" :options="planRecent"/>
            <el-divider/>
            <ms-show-all :index="'/track/plan/all'"/>
            <el-menu-item :index="testPlanViewPath" class="blank_item"></el-menu-item>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/track/plan/create'"
                              :title="$t('test_track.plan.create_plan')"/>
          </el-submenu>

          <el-menu-item :index="'/track/testPlan/reportList'">
            {{ $t("commons.report") }}
          </el-menu-item>
        </el-menu>
      </el-col>
      <el-col :span="8"/>
    </el-row>
  </div>

</template>
<script>

import MsShowAll from "../../common/head/ShowAll";
import MsRecentList from "../../common/head/RecentList";
import MsCreateButton from "../../common/head/CreateButton";
import {LIST_CHANGE, TrackEvent} from "@/business/components/common/head/ListEvent";
import SearchList from "@/business/components/common/head/SearchList";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";

export default {
  name: "TrackHeaderMenus",
  components: {ProjectChange, SearchList, MsShowAll, MsRecentList, MsCreateButton},
  data() {
    return {
      testPlanViewPath: '',
      isRouterAlive: true,
      testCaseEditPath: '',
      testCaseReviewEditPath: '',
      testCaseProjectPath: '',
      isProjectActivation: true,
      currentProject: '',
      caseRecent: {
        title: this.$t('test_track.recent_case'),
        url: "/test/case/recent/5",
        index: function (item) {
          return '/track/case/edit/' + item.id;
        },
        router: function (item) {
        }
      },
      reviewRecent: {
        title: this.$t('test_track.recent_review'),
        url: "/test/case/review/recent/5",
        index: function (item) {
          return '/track/review/view/' + item.id;
        },
        router: function (item) {
        }
      },
      planRecent: {
        title: this.$t('test_track.recent_plan'),
        url: "/test/plan/recent/5",
        index: function (item) {
          return '/track/plan/view/' + item.id;
        },
        router: function (item) {
        }
      }
    }
  },
  watch: {
    '$route'(to) {
      this.init();
    }
  },
  mounted() {
    this.init();
    this.registerEvents();
  },
  methods: {
    reload() {
      this.isRouterAlive = false;
      this.$nextTick(function () {
        this.isRouterAlive = true;
      });
    },
    init() {
      let path = this.$route.path;
      if (path.indexOf("/track/plan/view") >= 0) {
        this.testPlanViewPath = path;
        this.reload();
      }
      if (path.indexOf("/track/case/edit") >= 0) {
        this.testCaseEditPath = path;
        this.reload();
      }
      if (path.indexOf("/track/review/view") >= 0) {
        this.testCaseReviewEditPath = path;
        this.reload();
      }
    },
    registerEvents() {
      TrackEvent.$on(LIST_CHANGE, () => {
        this.$refs.planRecent.recent();
        this.$refs.caseRecent.recent();
      });
    }
  },
  beforeDestroy() {
    TrackEvent.$off(LIST_CHANGE);
  }
}

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}

.blank_item {
  display: none;
}

.el-divider--horizontal {
  margin: 0;
}

.deactivation >>> .el-submenu__title {
  border-bottom: white !important;
}

/*.project-change {*/
/*  height: 40px;*/
/*  line-height: 40px;*/
/*  color: inherit;*/
/*  margin-left: 20px;*/
/*}*/

</style>
