'use strict'
const path = require('path')
const defaultSettings = require('./src/settings.js')

const CopyWebpackPlugin = require('copy-webpack-plugin')

function resolve(dir) {
  return path.join(__dirname, dir)
}

const name = defaultSettings.title || 'vue Admin Template' // page title

const port = process.env.port || process.env.npm_config_port || 9528 // dev port
module.exports = {
  productionSourceMap: true,
  // 使用mock-server
  devServer: {
    port: port,
    proxy: {
      '^(?!/login)': {
        target: 'http://localhost:8081/',
        ws: false
      }
    },
    open: true,
    overlay: {
      warnings: false,
      errors: true
    },
    before: require('./mock/mock-server.js')
  },

  pages: {
    index: {
      entry: 'src/main.js',
      template: 'public/index.html',
      filename: 'index.html'
    },
    link: {
      entry: 'src/link/link.js',
      template: 'public/link.html',
      filename: 'link.html'
    }
  },
  configureWebpack: {
    name: name,
    devtool: 'source-map',
    resolve: {
      alias: {
        '@': resolve('src')
      }
    },
    plugins: [
      new CopyWebpackPlugin([
        {
          from: path.join(__dirname, 'static'),
          to: path.join(__dirname, 'dist/static')
        }
      ])
    ]
  },
  chainWebpack: config => {
    config.module.rules.delete('svg') // 删除默认配置中处理svg,
    // const svgRule = config.module.rule('svg')
    // svgRule.uses.clear()
    config.module
      .rule('svg-sprite-loader')
      .test(/\.svg$/)
      .include
      .add(resolve('src/icons')) // 处理svg目录
      .end()
      .use('svg-sprite-loader')
      .loader('svg-sprite-loader')
      .options({
        symbolId: 'icon-[name]'
      })
  }

}
