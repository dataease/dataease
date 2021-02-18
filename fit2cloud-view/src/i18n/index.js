import Vue from 'vue';
import VueI18n from "vue-i18n";

Vue.use(VueI18n);

// 直接加载翻译的语言文件
const LOADED_LANGUAGES = ['zh-CN', 'en-US'];
const LANG_FILES = require.context('./lang', true, /\.js$/)
// 自动加载lang目录下语言文件，默认只加载LOADED_LANGUAGES中规定的语言文件，其他的语言动态加载
const messages = LANG_FILES.keys().reduce((messages, path) => {
  const value = LANG_FILES(path)
  const lang = path.replace(/^\.\/(.*)\.\w+$/, '$1');
  if (LOADED_LANGUAGES.includes(lang)) {
    messages[lang] = value.default
  }
  return messages;
}, {})

export const getLanguage = () => {
  let language = localStorage.getItem('language')
  if (!language) {
    language = (navigator.language || navigator.browserLanguage).toLowerCase()
  }
  return language;
}

const i18n = new VueI18n({
  locale: getLanguage(),
  messages,
});

const importLanguage = lang => {
  if (!LOADED_LANGUAGES.includes(lang)) {
    return import(`./lang/${lang}`).then(response => {
      i18n.mergeLocaleMessage(lang, response.default);
      LOADED_LANGUAGES.push(lang);
      return Promise.resolve(lang)
    })
  }
  return Promise.resolve(lang)
}

const setLang = lang => {
  localStorage.setItem('language', lang)
  i18n.locale = lang;
}

export const setLanguage = lang => {
  if (i18n.locale !== lang) {
    importLanguage(lang).then(setLang)
  }
}

// 组合翻译，例如key为'请输入{0}'，keys为login.username，则自动将keys翻译并替换到{0} {1}...
Vue.prototype.$tm = function (key, ...keys) {
  let values = [];
  for (const k of keys) {
    values.push(i18n.t(k))
  }
  return i18n.t(key, values);
};

// 忽略警告，即：不存在Key直接返回Key
Vue.prototype.$tk = function (key) {
  const hasKey = i18n.te(key)
  if (hasKey) {
    return i18n.t(key)
  }
  return key
};

// 设置当前语言，LOADED_LANGUAGES以外的翻译文件会自动从lang目录获取(如果有的话), 如果不需要动态加载语言文件，直接用setLang
Vue.prototype.$setLang = setLanguage;

export default i18n;
