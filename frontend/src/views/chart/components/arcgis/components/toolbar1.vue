<template>
  <div class="toolbar">
    <!-- 地图切换按钮 -->
    <el-dropdown @command="baseMapChange">
      <el-button type="primary" class="el-dropdown-link">
        底图<i class="el-icon-arrow-down el-icon--right"></i>
      </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="1" > 矢量 </el-dropdown-item>
        <el-dropdown-item command="2" > 影像 </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
    <!-- 地图切换按钮 END-->

    <el-dropdown @command="measurement">
      <el-button type="primary" class="el-dropdown-link">
        测量<i class="el-icon-arrow-down el-icon--right"></i>
      </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="1" > 开始测量 </el-dropdown-item>
        <el-dropdown-item command="2" > 取消测量 </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>

    <!-- 标绘 GO-->
    <el-dropdown @command="draw">
      <el-button type="primary" class="el-dropdown-link">
        标绘<i class="el-icon-arrow-down el-icon--right"></i>
      </el-button>
      <el-dropdown-menu slot="dropdown">
        <el-dropdown-item command="POINT" > 点 </el-dropdown-item>
        <el-dropdown-item command="POLYLINE" > 线 </el-dropdown-item>
        <el-dropdown-item command="POLYGON" > 面 </el-dropdown-item>
        <el-dropdown-item command="CIRCLE" > 圆形 </el-dropdown-item>
        <el-dropdown-item command="RECTANGLE" > 长方形 </el-dropdown-item>
        <el-dropdown-item command="stop" > 停止标绘 </el-dropdown-item>
        <el-dropdown-item command="delete" > 清除标绘 </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
      <!-- 标绘 END-->
    <!-- 使用按钮组 -->
    <!-- <el-button-group>
      <el-button type="primary" @click="showLegend">
        图例
      </el-button>

      <el-button type="primary" @click="showLayerList">
        图层
      </el-button>
    </el-button-group> -->
  </div>
</template>

<script>
export default {
  name: "toolbar1",
  methods: {
    // 开启测量
    measurement(type) {
      this.$emit("measurement", type)
    },
    // 底图切换
    baseMapChange(type) {
      this.$emit("baseMapChange", type)
    },
    // 标绘
    draw(type) {
      this.$emit("draw", type)
    },
    // 显示图例
    showLegend() {
      this.$emit("showLegend")
    },
    // 显示图层
    showLayerList() {
      this.$emit("showLayerList")
    },
  },
};
</script>

<style lang="less">
.toolbar {
  position: absolute;
  right: 40px;
  height: 40px;
  width: auto;
  z-index: 99;
}
</style>