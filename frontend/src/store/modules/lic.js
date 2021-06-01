import { validateLic } from '@/api/system/lic'
const state = {
  validate: true,
  licStatus: null,
  licMsg: null
}

const mutations = {
  SET_VALIDATE: (state, data) => {
    state.validate = data
  },
  SET_LIC_MSG: (state, msg) => {
    state.licMsg = msg
  },
  SET_LIC_STATUS: (state, data) => {
    state.licStatus = data
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
        if (data && data.status && data.status === 'no_record') {
          commit('SET_VALIDATE', false)
          commit('SET_LIC_MSG', data.message)
          commit('SET_LIC_STATUS', data.status)
        } else {
          commit('SET_VALIDATE', true)
          commit('SET_LIC_MSG', null)
        }

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
