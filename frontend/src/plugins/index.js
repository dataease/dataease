import message from "@/plugins/message";
import request from "@/plugins/request";

export default {
  install(Vue) {
    Vue.use(message);
    Vue.use(request);
  }
}
