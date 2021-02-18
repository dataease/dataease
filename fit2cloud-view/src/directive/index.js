import ClickOutside from "element-ui/src/utils/clickoutside";
import permission from "@/directive/permission";

export default {
  install(Vue) {
    Vue.directive('click-outside', ClickOutside);
    Vue.directive('permission', permission);
  }
}


