<template>
  <div>
    <h4>功能概述</h4>
    <h5>
      仪表板功能模块。此模块支持仪表板的添加、编辑、
      重命名、预览、查看等，仪表板制作完成后，还可支持仪表板的收藏、分享、
      下载等等。
    </h5>
    <div>1、创建仪表板视图目录</div>
    <div class="img_box">
      <img src="../../assets/navgate/three_1.png">
    </div>
    <div class="img_box">
      <img src="../../assets/navgate/three_2.png">
    </div>
    <div>2、创建仪表板</div>
    <div class="img_box">
      <img src="../../assets/navgate/three_3.png">
    </div>
    <div>3、仪表板创建有三种模式，①自定义，即创建一张空白仪表板，②导入模板，上传导入别人创建好的模板，③复用模板，即复用当前项目中的模板</div>
    <div class="img_box">
      <img src="../../assets/navgate/three_4.png">
    </div>
    <div>4、仪表板创建成功后会跳入仪表板编辑页面，在此页面可对仪表板进行编辑操作（具体可详见操作手册）</div>
    <div class="img_box">
      <img src="../../assets/navgate/three_5.png">
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
