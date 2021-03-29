// import store from '@/store'
const req = require.context('./serviceImpl', false, /\.js$/)
const requireAll = requireContext => requireContext.keys()

const widgets = {}
requireAll(req).forEach(key => {
  widgets[key.replace(/(\.\/|\.js)/g, '')] = req(key).default
})

export default widgets
