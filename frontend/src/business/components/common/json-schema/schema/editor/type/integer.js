const value = {
    description: null,
    maximum: null,
    minimum: null,
    exclusiveMaximum:null,
    exclusiveMinimum:null
}
const attr = {
    description: {
      name: '描述',
      type: 'string',
    },
    maximum:{
        name:'最大值',
        type:'integer'
    },
    minimum:{
        name:'最小值',
        type:'integer'
    },
    exclusiveMaximum:{
        name:'不包含最大值',
        type:'boolean'
    },
    exclusiveMinimum:{
        name:'不包含最小值',
        type:'boolean'
    }
}
const wrapper = {value, attr}
export default wrapper