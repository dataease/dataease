const path = require('path')
const defaultSettings = require('./src/settings.js')

function resolve(dir) {
  return path.join(__dirname, dir)
}
const name = defaultSettings.title // 网址标题
const port = 8080
module.exports = {
  productionSourceMap: true,
  devServer: {
    port: port,
    proxy: {
      ['^(?!/login)']: {
        target: process.env.VUE_APP_BASE_API,
        ws: true,
      }
    }
  },
  pages: {
    business: {
      entry: "src/business/main.js",
      template: "src/business/index.html",
      filename: "index.html"
    },
    login: {
      entry: "src/login/login.js",
      template: "src/login/login.html",
      filename: "login.html"
    }
  },
  configureWebpack: {
    // devtool: 'source-map',
    devtool: 'eval-source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    }
  },
  chainWebpack(config) {
    config.plugins.delete('prefetch')
    // set svg-sprite-loader
    config.module
      .rule('svg')
      .exclude.add(resolve('src/assets/icons'))
      .end()
    config.module
      .rule('icons')
      .test(/\.svg$/)
      .include.add(resolve('src/assets/icons'))
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
      .end()
  }
};
