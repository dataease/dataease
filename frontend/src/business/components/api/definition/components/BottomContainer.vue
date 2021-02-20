<template>
  <el-aside :width="width" class="ms-aside-container"
              :style="{'margin-left': !enableAsideHidden ? 0 : '-' + width}">
    <slot></slot>
  </el-aside>
</template>

<script>
  export default {
    name: "MsBottomContainer",
    props: {
      width: {
        type: String,
        default: '100%'
      },
      enableAsideHidden: {
        type: Boolean,
        default: true
      },
    }
  }
</script>

<style scoped>
  .ms-aside-container {
    margin-top: 10px;
    height: 600px;
    box-sizing: border-box;
    background-color: #FFF;
  }

  .hiddenBottom i {
    margin-left: -2px;
  }

  .hiddenBottom:hover i {
    margin-left: 0;
    color: white;
  }

</style>
