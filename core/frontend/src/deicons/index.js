const req = require.context('./svg', false, /\.svg$/)

const requireAllFile = requireContext => requireContext.keys().map(requireContext)
requireAllFile(req)

const re = /\.\/(.*)\.svg/

const requireAll = requireContext => requireContext.keys()

const deSvgIcons = requireAll(req).map(i => {
  return '#' + i.match(re)[1]
})

export default deSvgIcons

