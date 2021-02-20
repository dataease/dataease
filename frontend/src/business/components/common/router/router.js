import Vue from "vue";
import VueRouter from 'vue-router'
import RouterSidebar from "./RouterSidebar";
import Setting from "@/business/components/settings/router";
import API from "@/business/components/api/router";
import Performance from "@/business/components/performance/router";
import Track from "@/business/components/track/router";

Vue.use(VueRouter);

const router = new VueRouter({
  routes: [
    {path: "/", redirect: '/setting/personsetting'},
    {
      path: "/sidebar",
      components: {
        sidebar: RouterSidebar
      }
    },
    Setting,
    API,
    Performance,
    Track,
  ]
});

router.beforeEach((to, from, next) => {

  redirectLoginPath(to.fullPath);

  //解决localStorage清空，cookie没失效导致的卡死问题
  if (!localStorage.getItem('Admin-Token')) {
    // axios.get("/signout");
    // console.log("signout");
    localStorage.setItem('Admin-Token', "{}");
    window.location.href = "/login";
    next();
  } else {
    next();
  }
});

//重复点击导航路由报错
const routerPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return routerPush.call(this, location).catch(error => error)
}


// 登入后跳转至原路径
function redirectLoginPath(originPath) {
  let redirectUrl = sessionStorage.getItem('redirectUrl');
  let loginSuccess = sessionStorage.getItem('loginSuccess');
  sessionStorage.setItem('redirectUrl', originPath);
  if (redirectUrl && loginSuccess) {
    sessionStorage.removeItem('loginSuccess');
    router.push(redirectUrl);
  }
  sessionStorage.removeItem('loginSuccess');
}


export default router
