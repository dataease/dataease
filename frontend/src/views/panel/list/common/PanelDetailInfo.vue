<template>
  <el-row>
    <el-col class="info-item">
      <p class="info-title">{{ $t('panel.create_by') }}</p>
      <p class="info-content">{{ panelInfo.creatorName }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ $t('panel.create_time') }}</p>
      <p class="info-content">{{ panelInfo.createTime |timestampFormatDate }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ $t('panel.update_by') }}</p>
      <p class="info-content">{{ panelInfo.updateName || 'N/A' }}</p>
    </el-col>
    <el-col class="info-item">
      <p class="info-title">{{ $t('panel.update_time') }}</p>
      <p
        v-if="panelInfo.updateTime"
        class="info-content"
      >{{ panelInfo.updateTime | timestampFormatDate }}</p>
      <p
        v-if="!panelInfo.updateTime"
        class="info-content"
      >N/A</p>
    </el-col>
  </el-row>
</template>

<script>
export default {
  name: 'PanelDetailInfo',
  data() {
    return {
      tabActive: 'chart',
      detail: {
        chart: {},
        table: {},
        datasource: {}
      },
      info: {}
    }
  },
  computed: {
    panelInfo() {
      return this.$store.state.panel.panelInfo
    }
  },
  watch: {

  },
  mounted() {
  },
  methods: {

  }
}
</script>

<style scoped>
  .info-tab ::v-deep .el-tabs__item{
    font-weight: 400;
    font-size: 12px;
  }
  .info-item{
     margin: 6px 0;
   }
  .info-title{
    margin: 0!important;
    font-weight: 600;
    font-size: 12px;
  }
  .info-content{
    margin: 0!important;
    font-size: 12px;
  }
</style>
