import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import SymbolMap from '@/views/antv/sankey/type'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
        path: '/sankey',
        name: 'sankey',
        component: SymbolMap
    }
  ]
})
