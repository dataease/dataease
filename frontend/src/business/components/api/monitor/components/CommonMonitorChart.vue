<template>
  <div class="common-card">
    <div class="chart">
      <slot></slot>
    </div>
    <div class="line"/>
  </div>
</template>

<script>
export default {
  props: {
    title: String,
  }
};
</script>

<style scoped>
.title {
  font-size: 20px;
  color: #999;
}

.line {
  margin: 10px 0;
  border-top: 1px solid #eee;
}

</style>

<style>

</style>
