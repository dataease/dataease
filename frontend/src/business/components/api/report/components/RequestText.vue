<template>
  <div class="text-container">
    <div @click="active" class="collapse">
      <i class="icon el-icon-arrow-right" :class="{'is-active': isActive}"/>
      {{$t('api_report.request')}}
    </div>
    <el-collapse-transition>
      <el-tabs v-model="activeName" v-show="isActive">
        <el-tab-pane label="Body" name="body" class="pane">
          <pre>{{request.body}}</pre>
        </el-tab-pane>
        <el-tab-pane label="Headers" name="headers" class="pane">
          <pre>{{request.headers}}</pre>
        </el-tab-pane>
        <el-tab-pane label="Cookies" name="cookies" class="pane">
          <pre>{{request.cookies}}</pre>
        </el-tab-pane>
      </el-tabs>
    </el-collapse-transition>
  </div>
</template>

<script>
  export default {
    name: "MsRequestText",

    props: {
      request: Object
    },

    data() {
      return {
        isActive: true,
        activeName: "body",
      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      }
    },
  }
</script>

<style scoped>
  .text-container .icon {
    padding: 5px;
  }

  .text-container .collapse {
    cursor: pointer;
  }

  .text-container .collapse:hover {
    opacity: 0.8;
  }

  .text-container .icon.is-active {
    transform: rotate(90deg);
  }

  .text-container .pane {
    background-color: #F9F9F9;
    padding: 10px;
    height: 250px;
    overflow-y: auto;
  }

  pre {
    margin: 0;
  }
</style>
