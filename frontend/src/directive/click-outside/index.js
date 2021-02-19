import ClickOutside from "element-ui/src/utils/clickoutside";

const install = function (Vue) {
  Vue.directive("click-outside", ClickOutside)
}

ClickOutside.install = install
export default ClickOutside
