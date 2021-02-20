<template>
  <el-menu class="header-menu" :unique-opened="true" mode="horizontal" default-active="1" router>
    <!-- 不激活项目路由-->
    <el-menu-item index="1" v-show="false">Placeholder</el-menu-item>
    <el-submenu v-permission="['test_manager','test_user','test_viewer']" index="2" popper-class="submenu">
      <template v-slot:title>
        <span class="project-name" :title="currentProject">
          {{ $t('commons.project') }}: {{currentProject}}
        </span>
      </template>
      <search-list :current-project.sync="currentProject"/>
      <el-divider/>
      <el-menu-item :index="'/setting/project/create'">
        <font-awesome-icon :icon="['fa', 'plus']"/>
        <span style="padding-left: 7px;">{{ $t("project.create") }}</span>
      </el-menu-item>
      <el-menu-item :index="'/setting/project/all'">
        <font-awesome-icon :icon="['fa', 'list-ul']"/>
        <span style="padding-left: 7px;">{{ $t('commons.show_all') }}</span>
      </el-menu-item>
    </el-submenu>
  </el-menu>
</template>

<script>
import SearchList from "@/business/components/common/head/SearchList";
import {PROJECT_NAME} from "../../../../common/js/constants";

export default {
  name: "ProjectSwitch",
  props: {
    projectName: String
  },
  components: {SearchList},
  watch: {
    currentProject() {
      localStorage.setItem(PROJECT_NAME, this.currentProject);
    }
  },
  data() {
    return {
      currentProject: this.projectName
    }
  }
}
</script>

<style scoped>
.project-name {
  display: inline-block;
  width: 130px;
  white-space:nowrap;
  overflow:hidden;
  text-overflow:ellipsis;
}

.el-divider--horizontal {
  margin: 0;
}
</style>
