<template>
  <div id="menu-bar">
    <el-row type="flex">
      <project-change :project-name="currentProject"/>
      <el-col :span="9">
        <el-menu class="header-menu" :unique-opened="true" mode="horizontal" router :default-active='$route.path'>
          <el-menu-item :index="'/performance/home'">
            {{ $t("i18n.home") }}
          </el-menu-item>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']"
                      index="4" popper-class="submenu">
            <template v-slot:title>{{ $t('commons.test') }}</template>
            <ms-recent-list ref="testRecent" :options="testRecent"/>
            <el-divider/>
            <ms-show-all :index="'/performance/test/all'"/>
            <ms-create-button v-permission="['test_manager','test_user']" :index="'/performance/test/create'"
                              :title="$t('load_test.create')"/>
          </el-submenu>

          <el-submenu v-permission="['test_manager','test_user','test_viewer']"
                      index="5" popper-class="submenu">
            <template v-slot:title>{{ $t('commons.report') }}</template>
            <ms-recent-list ref="reportRecent" :options="reportRecent"/>
            <el-divider/>
            <ms-show-all :index="'/performance/report/all'"/>
          </el-submenu>
        </el-menu>
      </el-col>
      <el-col :span="4" >
        <el-row type="flex" justify="center">
          <ms-create-test :to="'/performance/test/create'"/>
        </el-row>
      </el-col>
      <el-col :span="11"/>
    </el-row>
  </div>
</template>

<script>

import MsCreateTest from "../../common/head/CreateTest";
import MsRecentList from "../../common/head/RecentList";
import MsCreateButton from "../../common/head/CreateButton";
import MsShowAll from "../../common/head/ShowAll";
import {LIST_CHANGE, PerformanceEvent} from "@/business/components/common/head/ListEvent";
import SearchList from "@/business/components/common/head/SearchList";
import ProjectChange from "@/business/components/common/head/ProjectSwitch";

export default {
  name: "PerformanceHeaderMenus",
  components: {
    ProjectChange,
    SearchList,
    MsCreateButton,
    MsShowAll,
    MsRecentList,
    MsCreateTest
  },
  data() {
    return {
      testRecent: {
        title: this.$t('load_test.recent'),
        url: "/performance/recent/5",
        index(item) {
          return '/performance/test/edit/' + item.id;
        },
        router(item) {
        }
      },
      reportRecent: {
        title: this.$t('report.recent'),
        url: "/performance/report/recent/5",
        showTime: true,
        index(item) {
          return '/performance/report/view/' + item.id;
        },
        router(item) {
        }
      },
      currentProject: ''
    }
  },
  methods: {
    registerEvents() {
      PerformanceEvent.$on(LIST_CHANGE, () => {
        this.$refs.testRecent.recent();
        this.$refs.reportRecent.recent();
      });
    }
  },
  mounted() {
    this.registerEvents();
  },
  beforeDestroy() {
    PerformanceEvent.$off(LIST_CHANGE);
  }
}

</script>

<style scoped>
.el-divider--horizontal {
  margin: 0;
}

.el-menu.el-menu--horizontal {
  border-bottom: none;
}

#menu-bar {
  border-bottom: 1px solid #E6E6E6;
  background-color: #FFF;
}
</style>
