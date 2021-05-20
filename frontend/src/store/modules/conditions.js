import { Condition } from '@/components/widget/bean/Condition'
const state = {
  conditions: []
}

const mutations = {
  ADD_CONDITION: (state, condition) => {
    condition && valueValid(condition) && state.conditions.push(condition)
  },
  REDUCE_CONDITION: (state, index) => {
    state.conditions && state.conditions.length > index && state.conditions.splice(index, 1)
  },
  CLEAR: (state) => {
    state.conditions = []
  }
}

const actions = {
  add({ commit }, data) {
    const condition = formatCondition(data)
    if (!state.conditions || state.conditions.length === 0) {
      state.conditions = []
    }
    const validResult = isValid(condition)
    if (!validResult.statu && validResult.hasOwnProperty('existIndex') && validResult.existIndex !== -1) {
      commit('REDUCE_CONDITION', validResult.existIndex)
      commit('ADD_CONDITION', condition)
    }
    if (validResult.statu) {
      commit('ADD_CONDITION', condition)
    }
  },
  reduce({ commit }, index) {
    commit('ADD_CONDITION', index)
  },
  delete({ commit }, component) {
    for (let index = 0; index < state.conditions.length; index++) {
      const element = state.conditions[index]
      if (element.componentId === component.componentId) {
        commit('REDUCE_CONDITION', index)
      }
    }
  },
  clear({ commit }) {
    commit('CLEAR')
  }

}
// 判断条件condition是否有效
const isValid = condition => {
  const nullResult = {
    statu: false,
    msg: 'condition is null'
  }
  const repeatResult = {
    statu: false,
    existIndex: -1,
    msg: 'condition is exist'
  }
  const validResult = {
    statu: true,
    msg: null
  }
  if (!condition) {
    return nullResult
  }
  for (let index = 0; index < state.conditions.length; index++) {
    const item = state.conditions[index]
    if (item.componentId === condition.componentId) {
      repeatResult.existIndex = index
      return repeatResult
    }
  }
  return validResult
}

const valueValid = condition => {
  return condition && condition.value && condition.value.length > 0 && condition.value[0]
}

const formatCondition = obj => {
  const { component, value, operator } = obj
  const fieldId = component.options.attrs.fieldId
  const viewIds = component.options.attrs.viewIds
  const condition = new Condition(component.id, fieldId, operator, value, viewIds)
  return condition
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}
