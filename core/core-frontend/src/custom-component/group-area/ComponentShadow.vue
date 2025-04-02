<template>
  <div class="areaShadow"></div>
</template>

<script setup lang="ts"></script>

<style lang="less" scoped>
.areaShadow {
  position: absolute;
  z-index: 1;
  width: 100%;
  height: 100%;
  border: 2px solid var(--ed-color-primary) !important;
  pointer-events: none;
}
</style>
