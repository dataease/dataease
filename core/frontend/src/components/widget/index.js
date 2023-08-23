// import store from '@/store'

import Vue from 'vue'
import upperFirst from 'lodash/upperFirst'
import camelCase from 'lodash/camelCase'

const requireComponent = require.context('./deWidget', true, /\.vue$/)

requireComponent.keys().forEach(fileName => {
  // 获取组件配置
  const componentConfig = requireComponent(fileName)
  // 这个地方直接传入filename其实就是内部会调用了resolve方法，会返回对应的文件内容（不理解可以console一下看看）
  // 获取组件的 PascalCase 命名
  const componentName = upperFirst(
    camelCase(
      // 获取和目录深度无关的文件名
      fileName
        .split('/')
        .pop()
        .replace(/\.\w+$/, '')
    )
  )

  // 全局注册组件
  Vue.component(
    componentName,
    // 如果这个组件选项是通过 `export default` 导出的，
    // 那么就会优先使用 `.default`，
    // 否则回退到使用模块的根。
    componentConfig.default || componentConfig
  )
})

const req = require.context('./serviceImpl', false, /\.js$/)
// const req = require.context('./drawServiceImpl', false, /\.js$/)
const requireAll = requireContext => requireContext.keys()

const widgets = {}
requireAll(req).forEach(key => {
  widgets[key.replace(/(\.\/|\.js)/g, '')] = req(key).default
})

export default widgets
