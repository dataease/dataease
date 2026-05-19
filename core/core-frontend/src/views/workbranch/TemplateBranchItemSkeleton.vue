<template>
  <div class="template border-radius-12">
    <div class="photo">
      <div class="img"></div>
    </div>
    <div class="apply">
      <span class="name ellipsis"> </span>
    </div>
  </div>
</template>

<script setup lang="ts"></script>

<style scoped lang="less">
.template {
  overflow: hidden;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  display: flex;
  flex-direction: column;
  min-width: 181px;
  width: calc(20% - 16px);
  height: auto;
  margin-left: 16px;
  margin-bottom: 16px;

  .photo {
    box-sizing: border-box;
    padding: 4px;
    padding-bottom: 0;
    aspect-ratio: 1.774 / 1;
    width: 100%;
    overflow: hidden;

    .img {
      width: 100%;
      height: 100%;
      display: block;
      border-top-left-radius: 4px;
      border-top-right-radius: 4px;
      background: #eff0f199;
    }
  }

  .apply {
    box-sizing: border-box;
    padding: 8px 12px;
    background: #fff;
    border-top: 1px solid #d9d9d9;
    width: 100%;
    height: 39px;
    display: flex;
    flex-wrap: wrap;
    border-bottom-left-radius: 4px;
    border-bottom-right-radius: 4px;
    justify-content: space-between;

    .name {
      color: #1f2329;
      font-family: var(--de-custom_font, 'PingFang');
      font-size: 14px;
      font-style: normal;
      font-weight: 500;
      line-height: 22px;
      width: 100%;
      background-color: #eff0f1;
      border-radius: 6px;
    }
  }
}

@media screen and (max-width: 1420px) {
  .template {
    width: calc(25% - 16px);
  }
}

@media screen and (max-width: 1200px) {
  .template {
    width: calc(33.3333% - 16px);
  }
}

@media screen and (max-width: 920px) {
  .template {
    width: calc(50% - 16px);
  }
}
</style>
