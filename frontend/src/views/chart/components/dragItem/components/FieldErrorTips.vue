<template>
  <div class="tips-class">
    <el-tooltip
      class="item"
      effect="dark"
      :content="$t('chart.field_error_tips')"
      placement="bottom"
    >
      <span><svg-icon icon-class="icon_info_filled" /></span>
    </el-tooltip>
  </div>
</template>

<script>
export default {
  name: 'FieldErrorTips'
}
</script>

<style scoped>
.tips-class{
  position: absolute;
  right: 10px;
  z-index: 10;
}
</style>
