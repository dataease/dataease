<template>
  <div>
    <h4>功能概述</h4>
    <h5>
      此功能模块为分享创建好的仪表板，生成访问链接
    </h5>
    <div>1、选择需要分享的仪表板创建公共链接打开方式</div>
    <div class="img_box">
      <img src="../../assets/navgate/fore_1.png">
    </div>

    <div>2、链接设置方式</div>
    <div class="img_box">
      <img src="../../assets/navgate/fore_2.png">
    </div>
    <div class="img_box">
      <img src="../../assets/navgate/fore_3.png">
    </div>
    <div class="footer_btn">
      <el-button type="primary" @click="jumpPageTwo('/panel/index')">前往操作</el-button>
    </div>
  </div>
</template>
<script>
export default {
  data() {
    return {

    }
  },
  methods: {
    jumpPageTwo(key) {
      this.$router.push({ path: key })
    }
  }
}
</script>
<style lang="scss">
.img_box{
  width:100%;
  margin:20px 0;
  img{
    width:100%;
  }
}
.footer_btn{
  text-align:center;
}
</style>
