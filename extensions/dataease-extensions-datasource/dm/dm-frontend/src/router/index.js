import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import maxcompute from '@/views/maxcompute'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
        path: '/maxcompute',
        name: 'maxcompute',
        component: maxcompute
    }
  ]
})
