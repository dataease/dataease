<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true" @tab-click="selectTab">
      <el-tab-pane v-for="item in resource" :key="item.resourceId" :label="item.resourceName" class="logging-content">
        <ul class="infinite-list" v-infinite-scroll="load(item.resourceId)" infinite-scroll-disabled="disabled">
          <li class="infinite-list-item" v-for="(log, index) in logContent" :key="index">{{ log.content }}</li>
        </ul>
        <el-link type="primary" @click="downloadLogFile(item)">{{ $t('load_test.download_log_file') }}</el-link>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
export default {
  name: "LogDetails",
  data() {
    return {
      resource: [],
      logContent: [],
      result: {},
      id: '',
      page: 1,
      pageCount: 5,
      loading: false,
    }
  },

  computed: {
    disabled() {
      return this.loading || this.page > this.pageCount;
    }
  },

  methods: {
    getResource() {
      this.result = this.$get("/performance/report/log/resource/" + this.id, data => {
        this.resource = data.data;
        this.page = 1;
        this.logContent = [];
      })
    },
    load(resourceId) {
      if (this.loading || this.page > this.pageCount) return;
      this.loading = true;
      let url = "/performance/report/log/" + this.id + "/" + resourceId + "/" + this.page;
      this.$get(url, res => {
        let data = res.data;
        data.listObject.forEach(log => {
          this.logContent.push(log);
        })
        this.page++;
        this.loading = false;
      })
    },
    selectTab(tab) {
      let resourceId = tab.$vnode.key;
      this.loading = false;
      this.page = 1;
      this.logContent = [];
      this.load(resourceId);
    },
    downloadLogFile(item) {
      let config = {
        url: '/performance/report/log/download/' + this.id + '/' + item.resourceId,
        method: 'get',
        responseType: 'blob'
      };
      this.result = this.$request(config).then(response => {
        const filename = 'jmeter.log'
        const blob = new Blob([response.data]);
        if ("download" in document.createElement("a")) {
          // 非IE下载
          //  chrome/firefox
          let aTag = document.createElement('a');
          aTag.download = filename;
          aTag.href = URL.createObjectURL(blob);
          aTag.click();
          URL.revokeObjectURL(aTag.href)
        } else {
          // IE10+下载
          navigator.msSaveBlob(blob, filename);
        }
      });
    }
  },
  watch: {
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Completed" || status === "Running") {
          this.getResource();
        } else {
          this.resource = [];
        }
      },
      deep: true
    }
  },
  props: ['report']
}
</script>

<style scoped>
.logging-content {
  white-space: pre-line;
  overflow: auto;
}

.infinite-list {
  height: 500px;
  padding: 0;
  margin: 0;
  list-style: none;
  overflow: auto
}

.infinite-list-item {
  overflow: hidden;
}

</style>
