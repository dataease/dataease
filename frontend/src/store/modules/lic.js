import { validateLic } from '@/api/system/lic'
const state = {
  validate: true
}

const mutations = {
  SET_VALIDATE: (state, data) => {
    state.validate = data
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
        resolve(data)
      }).catch(error => {
        commit('SET_VALIDATE', false)
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
