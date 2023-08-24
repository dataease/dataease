<template>
  <el-col
    :span="8"
    class="card_main"
  >
    <el-row>
      <el-col
        :span="8"
        class="card_head"
      >
        <span>{{ headInfo }}</span>
      </el-col>
      <el-col
        :span="1"
        class="triangle"
      />
    </el-row>
    <el-row class="card_content">
      <slot />
    </el-row>
  </el-col>
</template>

<script>

export default {
  name: 'Card',
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
  .card_main{
    padding: 40px 30px 30px 30px;
    height: 33vh;
  }
  .card_head{
    padding-left: 15px;
    color: white;
    line-height: 30px;
    font-weight: bold;
    font-size: 18px;
    background-color: var(--MenuActiveBG, #409EFF);
  }
  .triangle{
    width: 0;
    height: 0;
    border-bottom: 30px solid var(--MenuActiveBG, #409EFF);
    border-right: 30px solid transparent;
    -webkit-transform: rotate(180deg);
    transform: rotate(0deg);
  }
  .card_content{
    border: lightgray solid 1px;
    border-radius: 0px 5px 5px 5px;
    height: 100%;
    overflow-y: auto;
  }

</style>
