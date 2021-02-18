import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'

Vue.use(Vuex)

// 自动从modules目录下获取模块
const MODULES_FILES = require.context('./modules', true, /\.js$/)

// 模块名为js文件名，例如user.js 则模块名为user
const modules = MODULES_FILES.keys().reduce((modules, modulePath) => {
  const value = MODULES_FILES(modulePath)
  const moduleName = modulePath.replace(/^\.\/(.*)\.\w+$/, '$1')
  modules[moduleName] = value.default
  return modules
}, {})

const store = new Vuex.Store({
  modules,
  getters
})

export default store
