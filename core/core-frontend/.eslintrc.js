module.exports = {
    "env": {
        "browser": true,
        "es2021": true,
        "node": true
    },
    "extends": [
        "eslint:recommended",
        "plugin:vue/vue3-essential",
        "plugin:@typescript-eslint/recommended",
        "plugin:prettier/recommended" // 解决ESlint和Prettier冲突
    ],
    "overrides": [
    ],
    // 配置解析vue文件
    "parser":"vue-eslint-parser",
    "parserOptions": {
        "ecmaVersion": "latest",
        "parser": "@typescript-eslint/parser",
        "sourceType": "module"
    },
    "plugins": [
        "vue",
        "@typescript-eslint"
    ],
   // 添加规则
   "rules": {
     "@typescript-eslint/ban-types": [
        "error",
        {
            "extendDefaults": true,
            "types": {
                "{}": false
            }
        }
      ],
      "vue/multi-word-component-names": 0,
      "@typescript-eslint/no-explicit-any": ["off"]
    }
  }
  