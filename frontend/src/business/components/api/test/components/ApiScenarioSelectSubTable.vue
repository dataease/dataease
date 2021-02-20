<template>
  <el-table stripe :data="scenarios" class="adjust-table" @select-all="select" @select="select" size="small"
            :show-header="false" ref="table" v-loading="result.loading">
    <el-table-column type="selection" width="50"/>
    <el-table-column prop="name" width="350" show-overflow-tooltip/>
    <el-table-column width="150">
      <template v-slot:default="{row}">
        1 / {{ row.requests.length }}
      </template>
    </el-table-column>
    <el-table-column prop="null" width="150">
      {{ row.userName }}
    </el-table-column>
    <el-table-column prop="enable">
      <template v-slot:default="{row}">
        <el-tag type="success" size="small" v-if="row.enable !== false">{{ $t('api_test.scenario.enable') }}</el-tag>
        <el-tag type="info" size="small" v-else>{{ $t('api_test.scenario.disable') }}</el-tag>
      </template>
    </el-table-column>

    <el-table-column/>
  </el-table>
</template>

<script>

export default {
  name: "MsApiScenarioSelectSubTable",
  props: {
    row: Object
  },
  data() {
    return {
      result: {loading: true},
      scenarios: [],
    }
  },
  methods: {
    getTest() {
      this.result = this.$get("/api/get/" + this.row.id, response => {
        if (response.data) {
          this.scenarios = JSON.parse(response.data.scenarioDefinition);
        }
      });
    },
    select(selection) {
      this.$emit('input', selection);
    }
  },
  mounted() {
    this.getTest();
  }
}
</script>

<style scoped>

</style>
