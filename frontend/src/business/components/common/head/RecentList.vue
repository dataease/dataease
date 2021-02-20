<template>
  <div v-loading="result.loading">
    <div class="recent-text">
      <i class="el-icon-time"/>
      <span>{{options.title}}</span>
      <i class="el-icon-refresh" @click="recent"/>
    </div>
    <el-menu-item :key="i.id" v-for="i in items" :index="getIndex(i)" :route="getRouter(i)">
      <template slot="title">
        <div class="title">{{ i.name }}</div>
        <div class="time" v-if="options.showTime && i.updateTime">{{ i.updateTime | timestampFormatDate}}</div>
      </template>
    </el-menu-item>
  </div>
</template>

<script>
  import {hasRoles} from "../../../../common/js/utils";
  import {ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER} from "../../../../common/js/constants";

  export default {
    name: "MsRecentList",
    props: {
      options: Object
    },
    mounted() {
      this.recent();
    },
    data() {
      return {
        result: {},
        items: []
      }
    },
    computed: {
      getIndex: function () {
        return function (item) {
          return this.options.index(item);
        }
      },
      getRouter: function () {
        return function (item) {
          if (this.options.router) {
            return this.options.router(item);
          }
        }
      }
    },

    methods: {
      recent: function () {
        if (hasRoles(ROLE_TEST_VIEWER, ROLE_TEST_USER, ROLE_TEST_MANAGER)) {
          this.result = this.$get(this.options.url, (response) => {
            this.items = response.data;
          });
        }
      }
    }
  }
</script>

<style scoped>
  .recent-text {
    padding: 0 10px;
    margin-top: -5px;
    line-height: 36px;
    color: #777777;
    background-color: #F5F5F5;
  }

  .recent-text span {
    padding-left: 6px;
    line-height: 36px;
  }

  .recent-text .el-icon-refresh {
    cursor: pointer;
    float: right;
    line-height: 36px;
  }

  .recent-text .el-icon-refresh:hover {
    color: #BBBBBB;
  }

  .title {
    display: inline-block;
    padding-left: 20px;
    max-width: 200px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .time {
    color: #C0C4CC;
    display: inline-block;
    padding-left: 20px;
    float: right;
  }
</style>
