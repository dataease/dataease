<template>
  <div id="demo">
    <div class="head">
      <span style="color: green">{{ renderState }}</span>
      <span style="color: green">仪表板布局测试</span>
    </div>
  </div>
</template>

<style lang="less" scoped>
body {
  overflow-x: hidden;
  & * {
    box-sizing: border-box;
  }
}

#demo {
  width: 100%;
  padding: 1.5em 0 1.5em 0;

  .head {
    border-bottom: 1px dashed;
    width: 100%;

    padding-left: 20px;

    height: 50px;

    a {
      text-decoration: none;
      color: black;
    }
  }

  .arrow {
    font-size: 20px;

    position: relative;
    margin-right: 10px;
    top: 2px;
  }
}
</style>
