<template>
  <div v-loading="result.loading">
    <el-input :placeholder="$t('project.search_by_name')"
              prefix-icon="el-icon-search"
              v-model="searchString"
              clearable
              class="search-input"
              size="small"/>
    <div v-if="items.length === 0" style="text-align: center; margin: 15px 0">
        <span style="font-size: 15px; color: #8a8b8d;">
          {{ $t('project.no_data') }}
        </span>
    </div>
    <div v-else style="height: 150px;overflow: auto">
      <el-menu-item :key="i.id" v-for="i in items" @click="change(i.id)">
        <template slot="title">
          <div class="title">
            {{ i.name }}
          </div>
          <i class="el-icon-check" v-if="i.id === currentProjectId"></i>
        </template>
      </el-menu-item>
    </div>

  </div>
</template>

<script>
import {getCurrentProjectID, getCurrentUser, hasRoles} from "@/common/js/utils";
import {PROJECT_ID, ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "@/common/js/constants";

export default {
  name: "SearchList",
  props: {
    options: Object,
    currentProject: String
  },
  created() {
    this.init();
  },
  computed: {
    currentProjectId() {
      return localStorage.getItem(PROJECT_ID)
    }
  },
  data() {
    return {
      result: {},
      items: [],
      searchArray: [],
      searchString: '',
      userId: getCurrentUser().id,
    }
  },
  watch: {
    searchString(val) {
      this.query(val)
    }
  },
  methods: {
    init: function () {
      if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
        this.result = this.$get("/project/listAll", response => {
          this.items = response.data;
          this.searchArray = response.data;
          let userLastProjectId = getCurrentUser().lastProjectId;
          if (userLastProjectId) {
            // id 是否存在
            if (this.searchArray.length > 0 && this.searchArray.map(p => p.id).indexOf(userLastProjectId) !== -1) {
              localStorage.setItem(PROJECT_ID, userLastProjectId);
            }
          }
          let projectId = getCurrentProjectID();
          if (projectId) {
            // 保存的 projectId 在当前项目列表是否存在; 切换工作空间后
            if (this.searchArray.length > 0 && this.searchArray.map(p => p.id).indexOf(projectId) === -1) {
              this.change(this.items[0].id);
            }
          } else {
            if (this.items.length > 0) {
              this.change(this.items[0].id);
            }
          }
          this.changeProjectName(projectId);
        })
      }
    },
    query(queryString) {
      this.items = queryString ? this.searchArray.filter(this.createFilter(queryString)) : this.searchArray;
    },
    createFilter(queryString) {
      return item => {
        return (item.name.toLowerCase().indexOf(queryString.toLowerCase()) !== -1);
      };
    },
    change(projectId) {
      let currentProjectId = getCurrentProjectID();
      if (projectId === currentProjectId) {
        return;
      }
      this.$post("/user/update/current", {id: this.userId, lastProjectId: projectId}, () => {
        localStorage.setItem(PROJECT_ID, projectId);
        let path = this.$route.matched[0].path ? this.$route.matched[0].path : '/';
        if (path === '/api') {
          if (this.$store.state.switch.value === 'new') {
            path = "/api/home";
          } else if (this.$store.state.switch.value === 'old') {
            path = "/api/home_obsolete";
          } else {
            path = '/';
          }
        }
        this.$router.push(path).then(() => {
          window.location.reload()
        }).catch(err => err);
        this.changeProjectName(projectId);
      });
    },
    changeProjectName(projectId) {
      if (projectId) {
        let project = this.searchArray.filter(p => p.id === projectId);
        if (project.length > 0) {
          this.$emit("update:currentProject", project[0].name);
        }
      } else {
        this.$emit("update:currentProject", this.$t('project.select'));
      }
    }
  }
}
</script>

<style scoped>

.search-input {
  padding: 0;
  margin-top: -5px;
}

.search-input >>> .el-input__inner {
  border-radius: 0;
}

.title {
  display: inline-block;
  padding-left: 15px;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.el-icon-check {
  color: #773888;
  margin-left: 10px;
}
</style>
