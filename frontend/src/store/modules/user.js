import { login, logout, deLogout, getInfo, getUIinfo, languageApi } from '@/api/user'
import { getToken, setToken, removeToken, setSysUI } from '@/utils/auth'
import { resetRouter } from '@/router'
import { format } from '@/utils/formatUi'
import { getLanguage } from '@/lang/index'
import Cookies from 'js-cookie'
import router from '@/router'
import i18n from '@/lang'
import { $alert, $confirm } from '@/utils/message'
const getDefaultState = () => {
  return {
    token: getToken(),
    name: '',
    user: {},
    roles: [],
    passwordModified: true,
    avatar: '',
    // 第一次加载菜单时用到
    loadMenus: false,
    // 当前用户拥有哪些资源权限
    permissions: [],
    language: getLanguage(),
    uiInfo: null,
    linkToken: null
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_LINK_TOKEN: (state, linkToken) => {
    state.linkToken = linkToken
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_USER: (state, user) => {
    state.user = user
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_LOAD_MENUS: (state, loadMenus) => {
    state.loadMenus = loadMenus
  },
  SET_PERMISSIONS: (state, permissions) => {
    state.permissions = permissions
  },
  SET_LOGIN_MSG: (state, msg) => {
    state.loginMsg = msg
  },
  SET_UI_INFO: (state, info) => {
    state.uiInfo = info
  },
  SET_LANGUAGE: (state, language) => {
    state.language = language
    Cookies.set('language', language)
    if (language && i18n.locale !== language) {
      i18n.locale = language
    }
  },
  SET_PASSWORD_MODIFIED: (state, passwordModified) => {
    state.passwordModified = passwordModified
  },
}

const actions = {
  // user login
  login({ commit }, userInfo) {
    const { username, password, loginType } = userInfo
    return new Promise((resolve, reject) => {
      login({ username: username.trim(), password: password, loginType: loginType }).then(response => {
        const { data } = response
        commit('SET_TOKEN', data.token)
        commit('SET_LOGIN_MSG', null)
        setToken(data.token)
        let passwordModified = true
        if (data.hasOwnProperty('passwordModified')) {
          passwordModified = data.passwordModified
        }
        commit('SET_PASSWORD_MODIFIED', passwordModified)
        localStorage.setItem('passwordModified', passwordModified)
        resolve()
      }).catch(error => {
        reject(error)
      })
    })
  },
  refreshToken({ commit }, token) {
    commit('SET_TOKEN', token)
    setToken(token)
  },

  // get user info
  getInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      getInfo(state.token).then(response => {
        const { data } = response

        if (!data) {
          reject('Verification failed, please Login again.')
        }
        const historyUserId = localStorage.getItem('userId')
        if(historyUserId && historyUserId !== data.userId+''){
          const clearLocalStorage = [ 'panel-main-tree', 'panel-default-tree','chart-tree','dataset-tree']
          clearLocalStorage.forEach((item) => {
            localStorage.removeItem(item)
          })
        }
        localStorage.setItem('userId', data.userId)
        const currentUser = data
        commit('SET_USER', currentUser)

        const { roles, nickName, permissions, language } = data
        commit('SET_ROLES', roles)

        commit('SET_NAME', nickName)
        // commit('SET_AVATAR', avatar)

        commit('SET_PERMISSIONS', permissions)

        commit('SET_LANGUAGE', language)

        // axios.defaults.headers.common['Accept-Language'] = language || 'zh_CN'
        // document.querySelector('html').setAttribute('lang', language || 'zh_CN')
        resolve(data)
      }).catch(error => {
        reject(error)
      })
    })
  },

  getUI({ commit, state }) {
    return new Promise((resolve, reject) => {
      getUIinfo().then(response => {
        const { data } = response
        const uiInfo = format(data)
        commit('SET_UI_INFO', uiInfo)
        setSysUI(uiInfo)
        resolve(uiInfo)
      }).catch(error => {
        reject(error)
      })
    })
  },

  // user logout
  logout({ commit, state }, param) {
    const method = param && param.casEnable ? deLogout : logout
    return new Promise((resolve, reject) => {
      method(state.token).then(res => {
        removeToken() // must remove  token  first
        resetRouter()
        commit('RESET_STATE')
        resolve(res.data)
      }).catch(error => {
        reject(error)
        if (error?.response?.data?.message) {
          if (error.response.data.message === ('oidc_logout_error')) {
            const message = i18n.t('logout.' + error.response.data.message)
            $confirm(message, () => {
              removeToken() // must remove  token  first
              resetRouter()
              commit('RESET_STATE')
              window.location.href = '/'
            }, {
              confirmButtonText: i18n.t('commons.confirm')
            })
          }
          if (error.response.data.message === ('cas_logout_error')) {
            const message = i18n.t('logout.' + error.response.data.message)
            $alert(message, () => {

            }, {
              confirmButtonText: i18n.t('commons.confirm'),
              showClose: false
            })
          }
        }
      })
    })
  },
  updateLoadMenus({ commit }) {
    return new Promise((resolve, reject) => {
      commit('SET_LOAD_MENUS', false)
    })
  },

  // remove token
  resetToken({ commit }) {
    return new Promise(resolve => {
      removeToken() // must remove  token  first
      commit('RESET_STATE')
      resolve()
    })
  },
  setLoginMsg({ commit, msg }) {
    commit('SET_LOGIN_MSG', msg)
  },
  setLanguage({ commit }, language) {
    languageApi(language).then(() => {
      commit('SET_LANGUAGE', language)
      location.reload()
    })
  },
  setLinkToken({ commit }, linkToken) {
    commit('SET_LINK_TOKEN', linkToken)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

