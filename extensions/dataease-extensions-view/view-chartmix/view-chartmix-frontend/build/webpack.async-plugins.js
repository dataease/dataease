const webpack = require('webpack')
const CompressionPlugin = require('compression-webpack-plugin');
const path = require('path')
const utils = require('./utils')
const CopyPlugin = require("copy-webpack-plugin");
const VueLoaderPlugin = require('vue-loader/lib/plugin');
function resolve (dir) {
  return path.join(__dirname, '..', dir)
}

module.exports = {
    mode: 'development',
    entry: {

    // PluginDemo: resolve('/src/views/PluginDemo.vue')
    'chart-mix-view': resolve('/src/views/antv/chartmix/index.vue'),
    'chart-mix-data': resolve('/src/views/antv/chartmix/data.vue'),
    'chart-mix-type': resolve('/src/views/antv/chartmix/type.vue'),
    'chart-mix-style': resolve('/src/views/antv/chartmix/style.vue')

  },
  output: {
    path: resolve('/static/'),
    filename: '[name].js'
  },
  resolve: {
    extensions: ['.js', '.vue', '.json'],
    alias: {
      'vue$': 'vue/dist/vue.esm.js',
      '@': resolve('src')
    }
  },
  externals: {
    vue: 'vue'
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          transformAssetUrls: {
            video: 'src',
            source: 'src',
            img: 'src',
            image: 'xlink:href'
          }
        }
      },
      {
        test: /.(sa|sc|c)ss$/,
        use: [
         {loader: 'vue-style-loader'},
         'css-loader',
         'sass-loader'
       ]
           },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        include: [resolve('src'), resolve('test')]
      },
      {
        test: /\.(png|jpe?g|gif|svg)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('img/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(mp4|webm|ogg|mp3|wav|flac|aac)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('media/[name].[hash:7].[ext]')
        }
      },
      {
        test: /\.(woff2?|eot|ttf|otf)(\?.*)?$/,
        loader: 'url-loader',
        options: {
          limit: 10000,
          name: utils.assetsPath('fonts/[name].[hash:7].[ext]')
        }
      }
    ]
  },
  plugins: [
    new VueLoaderPlugin(),
    new webpack.DefinePlugin({
      'process.env.NODE_ENV': '"production"'
    }),
    new CopyPlugin([
        {from: 'src/icons/svg/'}
    ]),
    new CompressionPlugin({
      test: /\.js(\?.*)?$/i,
    }),
  ]
}
