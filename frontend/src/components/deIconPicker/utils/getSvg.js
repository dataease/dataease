const req = require.context(process.env.VUE_APP_SVG, false, /\.svg$/)

const requireAllFile = requireContext => requireContext.keys().map(requireContext)
requireAllFile(req)

const re = /\.\/(.*)\.svg/

const requireAll = requireContext => requireContext.keys()

const svgIcons = requireAll(req).map(i => {
  return '#' + i.match(re)[1]
})

export default svgIcons

