<template>
  <div class="ace"></div>
</template>

<script setup lang="ts"></script>

<style lang="less">
.ace {
  position: absolute;
  height: calc(100% - 80px);
  width: 550px;
  top: 63px;
  background-color: #fff;
  border: 1px solid #ddd;
  z-index: 100;
}

.editor {
  margin: 10px 5px;
}
.btn {
  margin-top: 5px;
}
</style>
