const webpack = require('webpack')
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

    'buddle-map-view': resolve('/src/views/echarts/map/buddle/index.vue'),
    'buddle-map-data': resolve('/src/views/echarts/map/buddle/data.vue'),
    'buddle-map-type': resolve('/src/views/echarts/map/buddle/type.vue'),
    'buddle-map-style': resolve('/src/views/echarts/map/buddle/style.vue')

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
    ])

  ]
}
