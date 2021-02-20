import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex);

const Common = {
  state: {
    projectId: ""
  },
  mutations: {
    setProjectId(state, projectId) {
      state.projectId = projectId;
    }
  }
}

const API = {
  state: {
    test: {}
  },
  mutations: {
    setTest(state, test) {
      state.test = test;
    },
    clearTest(state) {
      state.test = {};
    }
  }
}

const Switch = {
  state: {
    value: "new"
  },
  mutations: {
    setValue(state, value) {
      state.value = value;
    }
  }
}

const IsReadOnly = {
  state: {
    flag: true
  },
  mutations: {
    setFlag(state, value) {
      state.flag = value;
    }
  }
}

export default new Vuex.Store({
  modules: {
    api: API,
    common: Common,
    switch: Switch,
    isReadOnly: IsReadOnly,
  }
})
