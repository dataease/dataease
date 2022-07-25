<template>
  <span>
    <el-popover
      placement="bottom"
      trigger="hover"
      width="300"
      popper-class="remark-pop"
      :visible-arrow="false"
    >
      <div class="remark-style" :style="{backgroundColor:remarkCfg.bgFill}" v-html="remarkCfg.content" />
      <i slot="reference" class="el-icon-info" style="cursor: pointer;color: gray;font-size: 12px;" />
    </el-popover>
  </span>
</template>

<script>
export default {
  name: 'TitleRemark',
  props: {
    remarkCfg: {
      type: Object,
      required: true
    }
  }
}
</script>

<style scoped>
.remark-style{
  min-height: 100px;
  max-height: 200px;
  overflow-y: auto;
  padding: 10px;
  border-radius: 4px;
}
</style>
<style>
.el-popover.remark-pop{
  padding: 0;
  border: none;
  margin-top: 8px!important;
  background: transparent;
}
</style>
