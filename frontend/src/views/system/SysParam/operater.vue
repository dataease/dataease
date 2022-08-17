<template>
  <div class="operater-bar">
    <p class="title">{{ $t(title) }}</p>
    <div class="btn-grounp">
      <slot></slot>
    </div>
  </div>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
      default: "",
    },
  },
};
</script>

<style lang="scss" scoped>
.operater-bar {
  width: 100%;
  height: 70px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  .title {
    font-family: PingFang SC;
    font-size: 16px;
    font-weight: 500;
    line-height: 24px;
    color: #1f2329;
  }
}
</style>