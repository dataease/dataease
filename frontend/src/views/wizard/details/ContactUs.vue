<template>
  <el-row class="demo_main">
    <el-row
      style="margin-top: 5%"
      class="demo_content"
    >
      <span class="icon iconfont icon-fasongyoujian" />
      <span style="margin-left: 10px">dataease@fit2cloud.com</span>
    </el-row>
    <el-row class="demo_content">
      <span class="icon iconfont icon-dianhua" />
      <span style="margin-left: 10px">400-052-0755</span>
    </el-row>
    <el-row class="demo_content">
      <span class="icon iconfont icon-github" />
      <span class="demo_git"><a
        href="https://github.com/dataease/dataease/issues"
        target="_blank"
      >{{ $t('wizard.open_source_community') }}</a></span>
    </el-row>
  </el-row>
</template>

<script>

export default {
  name: 'ContactUs',
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
    height: 30%;
  }
  .demo_git{
    margin-left: 10px;
    color: var(--MenuActiveBG, #409EFF);
  }

  .demo_bottom{
    position: absolute;
    left: 30px;
    bottom: 10px;
    font-weight: 500;
    color: var(--MenuActiveBG, #409EFF);
  }

</style>
