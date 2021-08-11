import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/nolic/index.vue'),
      meta: {
        title: '首页'
      }
    }
  ]
})
