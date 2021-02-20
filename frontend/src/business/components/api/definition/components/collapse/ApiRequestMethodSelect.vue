<template>
  <el-select :disabled="isReadOnly" v-model="request.method" class="request-method-select" @change="change">
    <el-option label="GET" value="GET"/>
    <el-option label="POST" value="POST"/>
    <el-option label="PUT" value="PUT"/>
    <el-option label="PATCH" value="PATCH"/>
    <el-option label="DELETE" value="DELETE"/>
    <el-option label="OPTIONS" value="OPTIONS"/>
    <el-option label="HEAD" value="HEAD"/>
    <el-option label="CONNECT" value="CONNECT"/>
  </el-select>
</template>

<script>
  export default {
    name: "ApiRequestMethodSelect",
    props: ['isReadOnly', 'request'],
    methods: {
      change(value) {
        this.$emit('change', value);
      }
    }
  }
</script>

<style scoped>
  .request-method-select {
    width: 110px;
  }
</style>
