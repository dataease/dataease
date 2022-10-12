<template>
  <el-row class="demo_main">
    <el-row class="demo_content">
      <span class="title">
        <a
          href="https://live.vhall.com/v3/lives/subscribe/533874762"
          target="_blank"
        >40分钟带你玩转 DataEase </a>
      </span>
      <br>
      <span class="title">
        <a
          href="https://live.vhall.com/v3/lives/subscribe/903960272"
          target="_blank"
        >用DataEase开源工具可视化 ClickHouse数据 </a>
      </span>
    </el-row>
    <el-row class="demo_bottom">
      <span style="color: black">{{ $t('wizard.teaching_video_bottom_hint') }}</span><a
        href="https://e.vhall.com/v3/user/home/45637107"
        target="_blank"
      >{{ $t('wizard.click_inner') }}</a>
    </el-row>
  </el-row>
</template>

<script>

export default {
  name: 'OnlineDocument',
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
  .demo_content{
    padding-left: 10px;
    float: left;
    font-weight: 400;
    color: var(--MenuActiveBG, #409EFF);
  }

  .demo_bottom{
    position: absolute;
    left: 30px;
    bottom: 10px;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);
  }

  .title{
    line-height: 40px;
  }

</style>
