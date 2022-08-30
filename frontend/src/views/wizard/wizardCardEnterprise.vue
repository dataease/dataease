<template>
  <el-col :style='{background:details.bgColor}' class="card_main">
    <el-col style="width: 230px">
      <el-row class="card_head">
        {{details.head}}
      </el-row>
      <el-row  class="card_content">
        <span v-html="details.content"></span>
      </el-row>
      <el-row class="card_bottom">
        <a target="_blank" :href="details.href">
          {{$t('wizard.apply') }}
          <i class="el-icon-arrow-right" />
        </a>
      </el-row >
    </el-col>
    <el-col style="width: 106px">
      <img  style="width: 106px" src="../../assets/wizard_enterprise.png"></img>
    </el-col>
  </el-col>
</template>

<script>

export default {
  name: 'WizardCardEnterprise',
  props: {
    details: {
      type: Object,
      required: true
    }
  },
  data() {
    return {}
  },
  computed:{
  }
}

</script>

<style lang="scss" scoped>
  .card_main{
    width: 384px;
    height: 190px;
    padding: 24px;
    opacity: 0.9;
    border-radius: 8px;
  }

  .card_main:hover {
    box-shadow: 0px 6px 24px rgba(31, 35, 41, 0.5)
  }
  .card_head{
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 500;
    font-size: 20px;
    line-height: 28px;
    width: 230px;
  }
  .card_content{
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    line-height: 22px;
    width: 230px;
    height: 66px;
    margin-top: 12px;
    color: #646A73;
    overflow-y: auto;
  }
  .card_bottom{
    font-family: 'PingFang SC';
    font-style: normal;
    font-weight: 400;
    font-size: 14px;
    margin-top: 14px;
    line-height: 22px;
    color: #3370FF;
  }
  .triangle{
    width: 0;
    height: 0;
    border-bottom: 30px solid var(--MenuActiveBG, #409EFF);
    border-right: 30px solid transparent;
    -webkit-transform: rotate(180deg);
    transform: rotate(0deg);
  }

</style>
