import { Condition } from '@/components/widget/bean/Condition'
const state = {
  conditions: []
}

const mutations = {
  ADD_CONDITION: (state, condition) => {
    !condition && (condition = [])
    state.conditions.push(condition)
  },
  REDUCE_CONDITION: (state, index) => {
    state.conditions && state.conditions.length > index && state.conditions.splice(index, 1)
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
