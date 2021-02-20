<template>
  <el-row :gutter="10" class="json-path-suggest-button">
    <el-button size="small" type="primary" @click="$emit('open')">
      {{openTip}}
    </el-button>
    <el-button size="small" type="danger" @click="$emit('clear')">
      {{clearTip}}
    </el-button>
  </el-row>
</template>

<script>
    export default {
      name: "ApiJsonPathSuggestButton",
      props: ['openTip', 'clearTip']
    }
</script>

<style scoped>

  .json-path-suggest-button {
    text-align: right;
  }

</style>
