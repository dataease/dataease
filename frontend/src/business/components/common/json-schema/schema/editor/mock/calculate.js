import Mock from "mockjs";
import {funcFilters} from "@/common/js/func-filter";

export const calculate = function (itemValue) {
  if (!itemValue) {
    return;
  }
  try {
    if (itemValue.trim().startsWith("${")) {
      // jmeter 内置函数不做处理
      return itemValue;
    }
    let funcs = itemValue.split("|");
    let value = Mock.mock(funcs[0].trim());
    if (funcs.length === 1) {
      return value;
    }
    for (let i = 1; i < funcs.length; i++) {
      let func = funcs[i].trim();
      let args = func.split(":");
      let strings = [];
      if (args[1]) {
        strings = args[1].split(",");
      }
      value = funcFilters[args[0].trim()](value, ...strings);
    }
    return value;
  } catch (e) {
    return itemValue;
  }
}
