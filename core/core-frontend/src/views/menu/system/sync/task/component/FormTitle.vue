<template>
  <div class="title">
    <div class="title-left"></div>
    <div class="title-right">
      <div class="title-right-content">
        <div class="title-right-content-left">
          <slot name="title"></slot>
        </div>
        <div class="title-right-content-right">
          <slot name="button"></slot>
        </div>
      </div>
    </div>
  </div>
</template>
<style scoped lang="less">
//表单标题
.title {
  display: flex;
  margin-bottom: 16px;

  .title-left {
    height: 16px;
    float: left;
    text-align: left;
    border-radius: 0;
    width: 2px;
    background: var(--ed-color-primary);
    flex: none;
    order: 1;
    flex-grow: 0;
    align-self: center;
  }

  .title-right {
    padding: 0 0 0 8px;
    float: right;
    width: 100%;
    font-family: var(--de-custom_font, 'PingFang');
    font-style: normal;
    font-weight: 500;
    font-size: 16px;
    line-height: 24px;
    color: #1f2329;
    flex: none;
    order: 2;
    flex-grow: 0;

    .title-right-content {
      display: flex;
      width: 100%;

      .title-right-content-left {
        float: left;
        align-self: center;
        width: 100%;
      }

      .title-right-content-right {
        float: right;
        align-items: center;
        text-align: end;
        align-self: center;
        width: 100%;
      }
    }
  }
}
</style>
<script setup lang="ts">
</script>