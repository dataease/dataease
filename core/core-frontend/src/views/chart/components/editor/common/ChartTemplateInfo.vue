<script lang="tsx" setup></script>

<template>
  <div class="template-check-info">
    <div>
      <Icon class-name="item-icon" name="dv-up-arrow" />
    </div>
    <div>当前为模版视图，请更换数据集...</div>
  </div>
</template>

<style lang="less" scoped>
.template-check-info {
  position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  font-size: 12px;
  color: #9ea6b2;
  width: 100%;
  height: 100%;
  flex-direction: column;
}

.item-icon {
  width: 50%;
  height: 50%;
  opacity: 0.3;
}
</style>
