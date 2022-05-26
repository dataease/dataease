
// 关闭测量工具
const MeasurementClose = function MeasurementClose() {
  this.measurement.clearResult(); // 清除测量图案
  // 拿到开启的工具名称 并关闭已开启的工具
  this.measurement.getTool() &&
    this.measurement.setTool(this.measurement.getTool().toolName, false);
};

export { MeasurementClose };
