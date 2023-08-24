import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import Pie3D from '@/views/highcharts/3dpie/type'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
        path: '/3d-pie',
        name: '3d-pie',
        component: Pie3D
    }
  ]
})
