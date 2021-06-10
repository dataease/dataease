# 项目说明

> 这是一个极简的 vue admin 管理后台，基于vue-admin-template进行了细节改造，主要是把侧边导航改造为了顶部和侧边两个导航。
>
> 本项目默认开启了css：sourceMap和devtool('source-map')，便于在开发中调试，除非编译速度过慢，否则开发环境不建议修改。

## IDE

编辑器建议使用VS Code，格式化时可以统一代码风格，配置项建议统一设置为默认不自动保存，手动保存后自动修复部分错误。具体参数如下：

```js
{    
    "emmet.triggerExpansionOnTab": true,
    "files.autoSave": "off",
    "vetur.format.defaultFormatterOptions": {
        "js-beautify-html": {
            "wrap_attributes": "force-aligned"
        },
        "prettyhtml": {
            "printWidth": 100,
            "singleQuote": false,
            "wrapAttributes": false,
            "sortAttributes": true
        },
        "prettier": {
            "semi": false,
            "singleQuote": true
        }
    },
    "eslint.run": "onSave",
    "editor.codeActionsOnSave": {
        "source.fixAll.eslint": true
    }
}
```


## 目录结构

```bash
├── build                      # 构建相关
├── public                     # 静态资源
│   │── favicon.ico            # favicon图标
│   └── index.html             # html模板
├── src                        # 源代码
│   ├── api                    # 所有请求
│   ├── assets                 # 主题 字体等静态资源
│   ├── components             # 全局公用组件
│   ├── directive              # 全局指令
│   ├── filters                # 全局 filter
│   ├── icons                  # 项目所有 svg icons
│   ├── lang                   # 国际化 language
│   ├── layout                 # 全局 layout
│   ├── router                 # 路由
│   ├── store                  # 全局 store管理
│   ├── styles                 # 全局样式
│   ├── utils                  # 全局公用方法
│   ├── vendor                 # 公用vendor
│   ├── views                  # views 所有页面
│   ├── App.vue                # 入口页面
│   ├── main.js                # 入口文件 加载组件 初始化等
│   └── permission.js          # 权限管理
├── tests                      # 测试
├── .env.xxx                   # 环境变量配置
├── .eslintrc.js               # eslint 配置项
├── .babelrc                   # babel-loader 配置
├── .travis.yml                # 自动化CI配置
├── vue.config.js              # vue-cli 配置
├── postcss.config.js          # postcss 配置
└── package.json               # package.json
```



## 构建步骤

```bash
# 克隆项目
git clone 项目地址

# 进入项目目录
cd admin-web

# 安装依赖
npm install

# 建议不要直接使用 cnpm 安装依赖，会有各种诡异的 bug。可以通过如下操作解决 npm 下载速度慢的问题
npm install --registry=https://registry.npm.taobao.org

# 启动服务
npm run dev
```

浏览器访问 [http://localhost:9528](http://localhost:9528)

## 发布

```bash
# 构建测试环境
npm run build:stage

# 构建生产环境
npm run build:prod
```

## 其它

```bash
# 预览发布环境效果
npm run preview

# 预览发布环境效果 + 静态资源分析
npm run preview -- --report

# 代码格式检查
npm run lint

# 代码格式检查并自动修复
npm run lint -- --fix
```

更多信息请参考花裤衩大佬的vue-element-admin [使用文档](https://panjiachen.github.io/vue-element-admin-site/zh/)

[线上地址](http://panjiachen.github.io/vue-admin-template)

[国内访问](https://panjiachen.gitee.io/vue-admin-template)

