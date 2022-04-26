<template>
  <el-row class="demo_main">
    <div class="demo_title" />
    <div class="demo_content">
      <a href="https://www.bilibili.com/video/BV1i34y1v7hq/" target="_blank">{{ $t('wizard.demo_video_hint') }}</a>
    </div>
  </el-row>
</template>

<script>

export default {
  name: 'DemoVideo',
  props: {
    headInfo: {
      type: String,
      required: false
    }
  },
  data() {
    return {}
  }
}

</script>

<style lang="scss" scoped>
  .demo_main{
    height: 100%;
    padding: 10px 20px 10px 20px;
  }
  .demo_title{
    float: left;
    height: 100%;
    width: 50%;
    background-size: 100% 100% !important;
    background-image: url('../../../assets/deV.png');
  }
  .demo_content{
    margin: auto;
    padding-left: 10px;
    float: left;
    width: 50%;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);

  }

</style>
