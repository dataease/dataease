<template>
  <div>
    <el-row>
      <el-link type="primary" class="download-case"
               @click="downloadCase"
      >{{$t('test_track.case.import.download_case')}}
      </el-link>
    </el-row>
  </div>

</template>

<script>
  export default {
    name: "TestCaseExport",
    methods: {
      downloadCase() {

      }
    }
  }

</script>

<style scoped>

</style>
