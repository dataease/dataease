
// 图层约定
// 1 2 层被底图层
// 3 层为 边界边界图层

import { DataType } from "@/utils/index"; // 工具函数

/*
 *  description:  添加图层
 *  param {Layer,Array<Layer>} layer  需添加的图层
 *  param {number,Array<number>} lever 添加图层的层数
 */
const addLayer = function addLayer(layer, lever) {
  // 判断是
  if (DataType(layer, "array")) {
    layer.forEach((item, index) => {
      lever ? this.map.addLayer(item, lever[index]) : this.map.addLayer(item);
    });
  } else {
    lever ? this.map.addLayer(layer, lever) : this.map.addLayer(layer);
  }
};

const removeLayer = function removeLayer(layer) {
  this.map.removeLayer(layer);
};

export { addLayer, removeLayer };
