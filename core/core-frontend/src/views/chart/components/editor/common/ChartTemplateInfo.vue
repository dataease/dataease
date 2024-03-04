<script lang="tsx" setup></script>

<template>
  <div class="view-panel-Mask">
    <Icon class-name="item-icon" name="dv-up-arrow" />
    <div>
      <el-button style="opacity: 1 !important" type="warning" size="mini" round>
        <span style="font-weight: bold; opacity: 1"> 当前为模板图表，请更换数据集...</span>
      </el-button>
    </div>
  </div>
</template>

<style lang="less" scoped>
.view-panel-Mask {
  display: flex;
  height: calc(100vh - 148px);
  width: 100%;
  background-color: rgba(92, 94, 97, 0.7);
  position: absolute;
  top: 85px;
  left: 0px;
  z-index: 2;
  cursor: not-allowed;
  display: flex;
  align-items: center;
  justify-content: center;
}

.item-icon {
  position: absolute;
  top: 10px;
  left: 300px;
  width: 40px;
  height: 40px;
  opacity: 1;
  color: #ff8800;
}
</style>
