<template>
  <el-col
    :span="8"
    class="card_main"
  >
    <el-row class="card_content">
      <slot />
    </el-row>
  </el-col>
</template>

<script>

export default {
  name: 'InfoCard',
  props: {
  },
  data() {
    return {}
  }
}

</script>

<style lang="scss" scoped>
  .card_main{
    padding: 5% 25px 30px 25px;
    min-height: 250px;
    height: 33vh;
    max-height: 300px;
  }
  .card_content{
    border: lightgray solid 1px;
    border-radius: 5px;
    height: 100%;
    overflow: hidden;
  }

</style>
