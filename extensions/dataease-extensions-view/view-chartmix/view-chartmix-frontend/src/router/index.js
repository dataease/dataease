import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import SymbolMap from '@/views/antv/chartmix/type'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
        path: '/chart-mix',
        name: 'chart-mix',
        component: SymbolMap
    }
  ]
})
