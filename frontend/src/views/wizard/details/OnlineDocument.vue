<template>
  <el-row class="demo_main">
    <el-row class="demo_content">
      <span>{{ $t('wizard.online_document_hint') }}</span>
    </el-row>
    <el-row class="demo_bottom">
      <a
        href="https://dataease.io/docs/dev_manual/dev_manual/"
        target="_blank"
      >{{ $t('wizard.click_show') }}</a>
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
    max-height: 80%;
    overflow: hidden;
  }

  .demo_bottom{
    position: absolute;
    left: 30px;
    bottom: 10px;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);
  }

</style>
