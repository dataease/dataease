export let Setting = {
  // 示例
  // ThreadGroup: {
  //   tree: {
  //     drag: false, // 禁止拖拽
  //     drop: false, // 禁止放置
  //   },
  //   menu: {
  //     disabled: true, //禁止显示菜单
  //   },
  //   edit: {
  //     disabled: true, // 禁止编辑页面
  //   }
  // }
};

export const use = function (s) {
  Setting = s || Setting;
}

export default {use}
