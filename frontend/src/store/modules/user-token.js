import {login, getCurrentUser, updateInfo, logout} from '@/api/user-token'
import {resetRouter} from '@/router'
import {getToken, setToken, removeToken} from '@/utils/token'
import {getLanguage, setLanguage} from "@/i18n";

/* 前后端不分离的登录办法*/
const state = {
  token: getToken(),
  name: "",
  language: getLanguage(),
  roles: []
}

const mutations = {
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_LANGUAGE: (state, language) => {
    state.language = language
    setLanguage(language)
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  }
}

const actions = {
  login({commit}, userInfo) {
    const {username, password} = userInfo
    return new Promise((resolve, reject) => {
      login({username: username.trim(), password: password}).then(response => {
        let token = response.data
        commit('SET_TOKEN', token)
        setToken(token)
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  isLogin({commit}) {
    return new Promise((resolve, reject) => {
      let token = getToken()
      if (token) {
        commit('SET_TOKEN', token);
        resolve(true)
      } else {
        reject(false)
      }
    });
  },

  getCurrentUser({commit}) {
    return new Promise((resolve, reject) => {
      getCurrentUser().then(response => {
        const {name, roles, language} = response.data
        commit('SET_NAME', name)
        commit('SET_ROLES', roles)
        commit('SET_LANGUAGE', language)
        resolve(response.data)
      }).catch(error => {
        reject(error)
      })
    });
  },

  setLanguage({commit, state}, language) {
    commit('SET_LANGUAGE', language)
    return new Promise((resolve, reject) => {
      updateInfo(state.id, {language: language}).then(response => {
        resolve(response)
      }).catch(error => {
        reject(error)
      })
    })
  },

  logout({commit}) {
    logout().then(() => {
      commit('SET_TOKEN', "");
      commit('SET_ROLES', [])
      removeToken()
      resetRouter()
    })
  },
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
