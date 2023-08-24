import Vue from 'vue'
import Router from 'vue-router'
import HelloWorld from '@/components/HelloWorld'
import BuddleType from '@/views/echarts/map/buddle/type'
import BuddleData from '@/views/echarts/map/test'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'HelloWorld',
      component: HelloWorld
    },
    {
        path: '/buddle-type',
        name: 'buddle-type',
        component: BuddleType
    },
    {
        path: '/buddle-data',
        name: 'buddle-data',
        component: BuddleData
    }
  ]
})
