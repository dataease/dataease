<template>
  <div>
    <h4>功能概述</h4>
    <h5>
      用来管理各类数据源连接信息，是后续数据分析操作中数据的来源。
    </h5>
    <div>1、创建数据源类型，选择创建不同类型的数据源，不同类型下数据源所需填写的内容不一样，填写好后需要先检验所填写内容是否正确</div>
    <div class="img_box">
      <img src="../../assets/navgate/one_1.png">
    </div>
    <div>2、创建当前类型数据源，不同类型的数据源创建的方式不同（具体可详见操作手册）</div>
    <div class="img_box">
      <img src="../../assets/navgate/one_2.png">
    </div>
    <div class="footer_btn">
      <el-button type="primary" @click="jumpPageTwo('/datasource/index')">前往操作</el-button>
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
