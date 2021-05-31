import { validateLic } from '@/api/system/lic'
const state = {
  validate: true,
  licMsg: null
}

const mutations = {
  SET_VALIDATE: (state, data) => {
    state.validate = data
  },
  SET_LIC_MSG: (state, msg) => {
    state.licMsg = msg
  }
}

const actions = {
  setValidate({ commit }, data) {
    commit('SET_VALIDATE', data)
  },

  getLicInfo({ commit, state }) {
    return new Promise((resolve, reject) => {
      validateLic().then(response => {
        const { data } = response
        commit('SET_VALIDATE', true)
        commit('SET_LIC_MSG', null)
        resolve(data)
      }).catch(error => {
        commit('SET_VALIDATE', false)
        commit('SET_LIC_MSG', error.response.data.message)
        reject(error)
      })
    })
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
