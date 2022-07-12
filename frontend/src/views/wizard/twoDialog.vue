<template>
  <div>
    <div>
      <h4>功能概述</h4>
      <h5>
        数据集设计模块，为下一步数据分析或报表制作进行相关的数据准备；
        目前支持创建的数据集类型有数据库数据集、SQL 数据集、Excel 数据集、自定
        义数据集、关联数据集、API 数据集六种
      </h5>
    </div>
    <div>1、创建数据集分组</div>
    <div class="img_box">
      <img src="../../assets/navgate/two_1.png">
    </div>
    <div>2、根据需求类型创建数据集（具体可详见操作手册）</div>
    <div class="img_box">
      <img src="../../assets/navgate/two_2.png">
    </div>
    <div class="footer_btn">
      <el-button type="primary" @click="jumpPageTwo('/dataset/index')">前往操作</el-button>
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
