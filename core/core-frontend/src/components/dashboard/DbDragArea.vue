<template>
  <div class="dragHandle shadow-t"></div>
  <div class="dragHandle shadow-r"></div>
  <div class="dragHandle shadow-b"></div>
  <div class="dragHandle shadow-l"></div>
</template>

<script setup lang="ts"></script>

<style lang="less" scoped>
.dragHandle {
  position: absolute;
  z-index: 10;
  cursor: move;
}
.shadow-t {
  width: 100%;
  height: 10px;
  left: 0;
  top: 0;
}
.shadow-r {
  width: 10px;
  height: calc(100% - 15px);
  right: 0;
  top: 0;
}
.shadow-b {
  width: calc(100% - 15px);
  height: 10px;
  left: 0;
  bottom: 0;
}
.shadow-l {
  width: 10px;
  height: 100%;
  left: 0;
  top: 0;
}
</style>
