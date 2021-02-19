<template>
  <div class="content-container">
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: "LayoutContent"
}
</script>

<style lang="scss">
@import "~@/styles/common/variables";

.content-container {
  transition: 0.3s;
  color: $--color-text-primary;
  background-color: #FFFFFF;
  overflow: auto;
  height: 100%;
  padding: 20px;
  border-radius: 4px;
  box-shadow: 0 1px 4px 0 rgba(0, 0, 0, 14%);
  box-sizing: border-box;
}
</style>
