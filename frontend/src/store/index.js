import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import user from './modules/user'
import permission from './modules/permission'
import dataset from './modules/dataset'
import chart from './modules/chart'
import request from './modules/request'
Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    settings,
    user,
    permission,
    dataset,
    chart,
    request
  },
  getters
})

export default store
