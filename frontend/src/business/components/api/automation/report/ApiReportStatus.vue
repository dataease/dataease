<template>
  <div>
    <el-tag size="mini" type="info" v-if="row.status === 'Starting'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="primary" effect="plain" v-else-if="row.status === 'Running'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="success" v-else-if="row.status === 'Success'">
      {{ row.status }}
    </el-tag>
    <el-tag size="mini" type="warning" v-else-if="row.status === 'Reporting'">
      {{ row.status }}
    </el-tag>
    <el-tooltip placement="top" v-else-if="row.status === 'Error'" effect="light">
      <template v-slot:content>
        <div>{{row.description}}</div>
      </template>
      <el-tag size="mini" type="danger">
        {{ row.status }}
      </el-tag>
    </el-tooltip>
    <el-tag v-else size="mini" type="info">
      {{ row.status }}
    </el-tag>
  </div>
</template>

<script>
  export default {
    name: "MsApiReportStatus",

    props: {
      row: Object
    }
  }
</script>

<style scoped>

</style>
