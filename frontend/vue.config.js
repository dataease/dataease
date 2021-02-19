const path = require('path')

function resolve(dir) {
  return path.join(__dirname, dir)
}

module.exports = {
  productionSourceMap: true,
  // 使用mock-server
  devServer: {
    port: 8080,
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    before: require('./mock/mock-server.js')
  },
  // 不使用mock-server，直接连接开发服务器
  // devServer: {
  //   port: 8080,
  //   proxy: {
  //     ['^(?!/login)']: {
  //       target: 'http://localhost:8081',
  //       ws: true,
  //     }
  //   }
  // },
  configureWebpack: {
    // devtool: 'source-map',
    devtool: 'eval-source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  }
};
