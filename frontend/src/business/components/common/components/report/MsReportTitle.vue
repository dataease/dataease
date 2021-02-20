<template>
  <div class="clearfix report-title">
    <div class="report-left">
      <div class="title">
        【{{type}}】- {{title}}
      </div>
      <div>
        <span class="time">{{new Date() | timestampFormatDate}}</span>
      </div>
    </div>
    <div class="report-right">
      <img class="logo" src="@/assets/logo-MeterSphere.png">
    </div>

  </div>
</template>

<script>
    export default {
      name: "MsReportTitle",
      props: {title: String, type: String},
      data() {
        return {

        }
      }
    }
</script>

<style scoped>

  .report-left {
    float: left;
  }

  .report-right {
    float: right;
  }

  .logo {
    height: 30px;
    line-height: 30px;
    vertical-align: middle
  }


  .report-left {
    font-size: 15px;
  }

  .time {
    margin-left: 10px;
  }

  .title {
    margin-bottom: 5px;
  }



</style>
