const value = {
    description: null,
    maxProperties: null,
    minProperties: null
}
const attr = {
    description: {
      name: '描述',
      type: 'string',
    },
    maxProperties:{
        name:'最大元素个数',
        type:'integer'
    },
    minProperties:{
        name:'最小元素个数',
        type:'integer'
    }
}
const wrapper = {value, attr}
export default wrapper