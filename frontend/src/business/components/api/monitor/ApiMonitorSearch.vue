<template>
  <div>
    <el-input v-model="input" :placeholder="$t('api_monitor.please_search')" prefix-icon="el-icon-search" type="text"
              @input="searchAction"></el-input>

    <el-table
      :data="searchResult"
      :show-header="false"
      border
      class="question-tab2"
      style="width: 100%"
      @row-click="getRowInfo"
    >
      <el-table-column
        :label="$t('api_monitor.date')"
        prop="url"
        style="width: 100%">
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
function throttle(fn, delay) {
  let t = null,
    begin = new Date().getTime();

  return function () {
    const _self = this,
      args = arguments,
      cur = new Date().getTime();
    clearTimeout(t);
    if (cur - begin >= delay) {
      fn.apply(_self, args);
      begin = cur;
    } else {
      t = setTimeout(function () {
        fn.apply(_self, args);
      }, delay);
    }
  };

}

export default {
  name: 'MsApiMonitorSearch',
  data() {
    return {
      result: {},
      searchResult: [],
      items: [],
      input: '',
      rowData: '',
      apiUrl: '',
      firstUrl: '',
      rspTimeData: [],
      rspTimexAxis: [],
      rspCodeData: [],
      rspCodexAxis: [],
    }
  },
  methods: {
    searchAction: throttle(function (e) {
      this.searchResult = this.items.filter((item) => {
        if (item.url.includes(this.input)) {
          return item;
        }
      });
    }, 500),
    search() {
      this.result = this.$get('/api/monitor/list', response => {
        this.items = response.data;
      });
    },
    getRowInfo(val) {
      this.rowData = val;
      this.apiUrl = this.rowData.url;
      this.$emit('getApiUrl', this.apiUrl, this.firstUrl);
      this.$emit('getTodayData');
    },

  },
  activated() {
    this.searchResult=[];
    this.result = this.$get('/api/monitor/list', response => {
      if (response.data.length !== 0) {
        this.searchResult = response.data;
        this.firstUrl = response.data[0].url;
        this.$emit('initPage', this.firstUrl);
        this.search();
      }
    });
  }
}

</script>

<style scoped>

</style>
