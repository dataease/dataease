<template>
  <span class="box">
    <el-tooltip
      class="item"
      effect="dark"
      :content="tips"
      placement="right"
    >
      <el-button
        type="primary"
        size="mini"
        circle
        @click="exec()"
      >
        <font-awesome-icon :icon="['fas', 'plus']" />
      </el-button>
    </el-tooltip>
  </span>
</template>

<script>
export default {
  name: 'MsCreateBox',
  props: {
    tips: String,
    exec: Function
  }
}
</script>

<style scoped>
  .box {
    margin-left: 5px;
    margin-right: 5px;
  }
</style>
